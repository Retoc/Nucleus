/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.message.MessagePermissions;
import io.github.nucleuspowered.nucleus.modules.message.config.MessageConfig;
import io.github.nucleuspowered.nucleus.modules.message.events.InternalNucleusHelpOpEvent;
import io.github.nucleuspowered.nucleus.modules.message.services.MessageHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory.NucleusTextTemplateImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;

@EssentialsEquivalent({"helpop", "amsg", "ac"})
@NonnullByDefault
@Command(
        aliases = { "helpop" },
        basePermission = MessagePermissions.BASE_HELPOP,
        commandDescriptionKey = "helpop",
        async = true,
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = MessagePermissions.EXEMPT_COOLDOWN_HELPOP),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = MessagePermissions.EXEMPT_WARMUP_HELPOP),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = MessagePermissions.EXEMPT_COST_HELPOP)
        }
)
public class HelpOpCommand implements ICommandExecutor<Player>, Reloadable {

    @Nullable private NucleusTextTemplateImpl prefix = null;

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.MESSAGE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        String message = context.requireOne(NucleusParameters.Keys.MESSAGE, String.class);

        // Message is about to be sent. Send the event out. If canceled, then
        // that's that.
        if (Sponge.getEventManager().post(new InternalNucleusHelpOpEvent(context.getCommandSource(), message))) {
            return context.errorResult("message.cancel");
        }

        Text prefix = this.prefix == null ? Text.EMPTY : this.prefix.getForCommandSource(context.getCommandSource());

        context.getServiceCollection()
                .getServiceUnchecked(MessageHandler.class)
                .getHelpopMessageChannel()
                .send(context.getCommandSource(),
                    TextParsingUtils.joinTextsWithColoursFlowing(prefix, Text.of(message)));

        context.sendMessage("command.helpop.success");

        return context.successResult();
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        this.prefix = serviceCollection.moduleDataProvider().getModuleConfig(MessageConfig.class).getHelpOpPrefix(serviceCollection.textTemplateFactory());
    }
}
