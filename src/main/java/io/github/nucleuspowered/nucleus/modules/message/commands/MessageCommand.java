/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.MessageTargetArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.annotation.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.modules.message.MessagePermissions;
import io.github.nucleuspowered.nucleus.modules.message.services.MessageHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@EssentialsEquivalent({"msg", "tell", "m", "t", "whisper"})
@NonnullByDefault
@NotifyIfAFK(MessageCommand.TO)
@Command(
        aliases = { "message", "m", "msg", "whisper", "w", "t" },
        basePermission = MessagePermissions.BASE_MESSAGE,
        commandDescriptionKey = "message",
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = MessagePermissions.EXEMPT_COOLDOWN_MESSAGE),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = MessagePermissions.EXEMPT_WARMUP_MESSAGE),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = MessagePermissions.EXEMPT_COST_MESSAGE)
        }
)
public class MessageCommand implements ICommandExecutor<CommandSource> {
    final static String TO = "to";

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
            GenericArguments.onlyOne(GenericArguments.firstParsing(
                    new MessageTargetArgument(serviceCollection.messageProvider(), Text.of(TO)),
                    new SelectorArgument(new NicknameArgument(Text.of(TO), NicknameArgument.Target.PLAYER_CONSOLE), Player.class)
            )),
            NucleusParameters.MESSAGE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        boolean b = context.getServiceCollection()
                .getServiceUnchecked(MessageHandler.class)
                .sendMessage(context.getCommandSource(),
                        context.requireOne(TO, CommandSource.class),
                        context.requireOne(NucleusParameters.Keys.MESSAGE, String.class));
        return b ? context.successResult() : context.failResult();
    }
}
