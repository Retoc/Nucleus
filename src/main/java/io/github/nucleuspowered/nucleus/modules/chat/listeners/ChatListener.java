/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.listeners;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.EventContexts;
import io.github.nucleuspowered.nucleus.api.chat.NucleusNoFormatChannel;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.internal.interfaces.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.chat.ChatPermissions;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatTemplateConfig;
import io.github.nucleuspowered.nucleus.modules.chat.services.ChatService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import io.github.nucleuspowered.nucleus.services.ITextStyleService;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.text.transform.SimpleTextFormatter;
import org.spongepowered.api.text.transform.SimpleTextTemplateApplier;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * A listener that modifies all chat messages. Uses the
 * {@link NucleusMessageTokenService}, which
 * should be used if tokens need to be registered.
 */
public class ChatListener implements Reloadable, ListenerBase.Conditional {

    private final ChatService chatService;
    private final ITextStyleService textStyleService;
    private final IPermissionService permissionService;

    private ChatConfig chatConfig;

    @Inject
    public ChatListener(INucleusServiceCollection serviceCollection) {
        this.chatService = serviceCollection.getServiceUnchecked(ChatService.class);
        this.textStyleService = serviceCollection.textStyleService();
        this.chatConfig = serviceCollection.moduleDataProvider().getModuleConfig(ChatConfig.class);
        this.permissionService = serviceCollection.permissionService();
    }

    // We do this first so that other plugins can alter it later if needs be.
    @Listener(order = Order.EARLY, beforeModifications = true)
    public void onPlayerChat(MessageChannelEvent.Chat event) {
        Util.onPlayerSimulatedOrPlayer(event, this::onPlayerChatInternal);
    }

    private boolean shouldNotFormat(MessageChannelEvent.Chat event) {
        if (!event.getContext().get(EventContexts.SHOULD_FORMAT_CHANNEL).orElse(true)) {
            return true;
        }

        return event.getChannel().map(this::shouldNotFormat).orElseGet(() -> shouldNotFormat(event.getOriginalChannel()));
    }

    private boolean shouldNotFormat(MessageChannel channel) {
        return channel instanceof NucleusNoFormatChannel && !((NucleusNoFormatChannel) channel).formatMessages();
    }

    @Nullable
    private NucleusNoFormatChannel getChannel(MessageChannelEvent.Chat event) {
        if (event.getChannel().filter(x -> x instanceof NucleusNoFormatChannel).isPresent()) {
            return (NucleusNoFormatChannel) event.getChannel().get();
        }
        return event.getOriginalChannel() instanceof NucleusNoFormatChannel ? (NucleusNoFormatChannel) event.getOriginalChannel() : null;
    }

    private void onPlayerChatInternal(MessageChannelEvent.Chat event, Player player) {
        if (shouldNotFormat(event)) {
            @Nullable NucleusNoFormatChannel channel = getChannel(event);
            if (channel != null && channel.removePrefix()) {
                event.getFormatter().setHeader(Text.EMPTY);
            }

            // Not interested in applying these transforms.
            return;
        }

        MessageEvent.MessageFormatter eventFormatter = event.getFormatter();
        Text rawMessage = eventFormatter.getBody().isEmpty() ? event.getRawMessage() : eventFormatter.getBody().toText();

        SimpleTextFormatter headerFormatter = eventFormatter.getHeader();
        SimpleTextFormatter footerFormatter = eventFormatter.getFooter();
        if (this.chatConfig.isOverwriteEarlyPrefixes()) {
            eventFormatter.setHeader(Text.EMPTY);
            headerFormatter.clear();
        } else if (this.chatConfig.isTryRemoveMinecraftPrefix()) { // Avoid adding <name>.
            // We should remove the applier.
            for (SimpleTextTemplateApplier stta : eventFormatter.getHeader()) {
                if (stta instanceof MessageEvent.DefaultHeaderApplier) {
                    eventFormatter.getHeader().remove(stta); // the iterator is read only, so we have to do this...
                }
            }
        }

        if (this.chatConfig.isOverwriteEarlySuffixes()) {
            footerFormatter.clear();
        }

        final ChatTemplateConfig ctc;
        if (this.chatConfig.isUseGroupTemplates()) {
            ctc = this.chatService.getTemplateNow(player);
        } else {
            ctc = this.chatConfig.getDefaultTemplate();
        }

        if (!ctc.getPrefix().isEmpty()) {
            SimpleTextTemplateApplier headerApplier = new SimpleTextTemplateApplier();
            headerApplier.setTemplate(TextTemplate.of(ctc.getPrefix().getForCommandSource(player)));
            event.getFormatter().getHeader().add(headerApplier);
        }

        if (!ctc.getSuffix().isEmpty()) {
            SimpleTextTemplateApplier footerApplier = new SimpleTextTemplateApplier();
            footerApplier.setTemplate(TextTemplate.of(ctc.getSuffix().getForCommandSource(player)));
            event.getFormatter().getFooter().add(footerApplier);
        }

        event.getFormatter().setBody(this.chatConfig.isModifyMainMessage() ? useMessage(player, rawMessage, ctc) : rawMessage);
    }

    @Override
    public boolean shouldEnable(INucleusServiceCollection serviceCollection) {
        return serviceCollection.moduleDataProvider().getModuleConfig(ChatConfig.class).isModifychat();
    }

    private Text useMessage(Player player, Text rawMessage, ChatTemplateConfig chatTemplateConfig) {
        String m = TextSerializers.FORMATTING_CODE.serialize(rawMessage);
        if (this.chatConfig.isRemoveBlueUnderline()) {
            m = m.replaceAll("&9&n([A-Za-z0-9-.]+)(&r)?", "$1");
        }

        m = this.textStyleService.stripPermissionless(ChatPermissions.CHAT_COLOR, ChatPermissions.CHAT_STYLE, player, m);

        Text result;
        if (this.permissionService.hasPermission(player, ChatPermissions.CHAT_URLS)) {
            result = this.textStyleService.addUrls(m, !this.chatConfig.isRemoveBlueUnderline());
        } else {
            result = TextSerializers.FORMATTING_CODE.deserialize(m);
        }

        String chatcol = this.permissionService.getOptionFromSubject(player, "chatcolour", "chatcolor").orElseGet(chatTemplateConfig::getChatcolour);
        String chatstyle = this.permissionService.getOptionFromSubject(player, "chatstyle").orElseGet(chatTemplateConfig::getChatstyle);

        return Text.of(this.textStyleService.getColourFromString(chatcol), this.textStyleService.getTextStyleFromString(chatstyle), result);
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.chatConfig = serviceCollection.moduleDataProvider().getModuleConfig(ChatConfig.class);
    }
}
