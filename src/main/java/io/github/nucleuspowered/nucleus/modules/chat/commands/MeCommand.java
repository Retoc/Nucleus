/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.EventContexts;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.chat.ChatPermissions;
import io.github.nucleuspowered.nucleus.modules.chat.config.ChatConfig;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.ITextStyleService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

@Command(
        aliases = {"me", "action"},
        basePermission = ChatPermissions.BASE_ME,
        commandDescriptionKey = "me",
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = ChatPermissions.EXEMPT_COOLDOWN_ME),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = ChatPermissions.EXEMPT_WARMUP_ME),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = ChatPermissions.EXEMPT_COST_ME)
        }
)
@EssentialsEquivalent({"me", "action", "describe"})
public class MeCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private ChatConfig config = new ChatConfig();
    private final MeChannel channel = new MeChannel();

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.MESSAGE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        ITextStyleService textStyleService = context.getServiceCollection().textStyleService();
        String message = textStyleService.stripPermissionless(
                ChatPermissions.CHAT_COLOR,
                ChatPermissions.CHAT_STYLE,
                context.getCommandSource(),
                context.requireOne(NucleusParameters.Keys.MESSAGE, String.class));

        Text header = config.getMePrefix().getForCommandSource(context.getCommandSource());
        ITextStyleService.TextFormat t = textStyleService.getLastColourAndStyle(header, null);
        Text originalMessage = TextSerializers.FORMATTING_CODE.deserialize(message);
        MessageEvent.MessageFormatter formatter = new MessageEvent.MessageFormatter(
            Text.builder().color(t.colour()).style(t.style())
                .append(TextSerializers.FORMATTING_CODE.deserialize(message)).toText()
        );

        // Doing this here rather than in the constructor removes the < > notation.
        formatter.setHeader(header);

        // We create an event so that other plugins can provide transforms, such as Boop, and that we
        // can catch it in ignore and mutes, and so can other plugins.
        CommandSource src = context.getCommandSource();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContexts.SHOULD_FORMAT_CHANNEL, false);
            if (frame.getCurrentCause().root() != src) {
                frame.pushCause(src);
            }

            MessageChannelEvent.Chat event =
                    SpongeEventFactory.createMessageChannelEventChat(
                            frame.getCurrentCause(),
                            this.channel,
                            Optional.of(this.channel),
                            formatter,
                            originalMessage,
                            false);

            if (Sponge.getEventManager().post(event)) {
                return context.errorResult("command.me.cancel");
            }

            event.getChannel().orElse(channel).send(src, Util.applyChatTemplate(event.getFormatter()), ChatTypes.CHAT);
        }
        return context.successResult();
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        this.config = serviceCollection.moduleDataProvider().getModuleConfig(ChatConfig.class);
    }

    public static class MeChannel implements NucleusChatChannel.ActionMessage {

        @Override
        @Nonnull
        public Collection<MessageReceiver> getMembers() {
            return MessageChannel.TO_ALL.getMembers();
        }

        @Override public boolean removePrefix() {
            return false;
        }
    }
}
