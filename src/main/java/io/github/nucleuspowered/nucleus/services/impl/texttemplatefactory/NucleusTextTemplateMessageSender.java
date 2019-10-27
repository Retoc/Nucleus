/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.api.events.NucleusTextTemplateEvent;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.services.IMessageTokenService;
import io.github.nucleuspowered.nucleus.services.INucleusTextTemplateFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class NucleusTextTemplateMessageSender {

    private final INucleusTextTemplateFactory textTemplateFactory;
    private final IMessageTokenService tokenService;
    private final NucleusTextTemplate textTemplate;
    private final CommandSource sender;

    public NucleusTextTemplateMessageSender(
            INucleusTextTemplateFactory textTemplateFactory,
            NucleusTextTemplate textTemplate,
            IMessageTokenService tokenService,
            CommandSource sender) {
        this.textTemplateFactory = textTemplateFactory;
        this.textTemplate = textTemplate;
        this.tokenService = tokenService;
        this.sender = sender;
    }

    public boolean send(Cause cause) {
        List<CommandSource> members = Lists.newArrayList(Sponge.getServer().getConsole());
        members.addAll(Sponge.getServer().getOnlinePlayers());
        return send(members, true, cause);
    }

    public boolean send(Collection<CommandSource> source, Cause cause) {
        return send(source, false, cause);
    }

    private boolean send(Collection<CommandSource> source, boolean isBroadcast, Cause cause) {
        NucleusTextTemplateEvent event;
        if (isBroadcast) {
            event = new NucleusTextTemplateEventImpl.Broadcast(
                    this.textTemplate,
                    source,
                    this.textTemplateFactory,
                    cause
            );
        } else {
            event = new NucleusTextTemplateEventImpl(
                    this.textTemplate,
                    source,
                    this.textTemplateFactory,
                    cause
            );
        }

        if (Sponge.getEventManager().post(event)) {
            return false;
        }

        NucleusTextTemplate template = event.getMessage();
        if (!template.containsTokens()) {
            Text text = this.textTemplate.getForCommandSource(Sponge.getServer().getConsole());
            event.getRecipients().forEach(x -> x.sendMessage(text));
        } else {
            Map<String, Function<CommandSource, Optional<Text>>> m = Maps.newHashMap();
            m.put("sender", cs -> this.tokenService.applyPrimaryToken("displayname", this.sender));
            event.getRecipients().forEach(x -> x.sendMessage(this.textTemplate.getForCommandSource(x, m, null)));
        }
        return true;
    }
}
