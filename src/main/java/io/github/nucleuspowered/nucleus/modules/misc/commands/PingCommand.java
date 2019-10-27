/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.misc.MiscPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(aliases = { "ping" }, basePermission = MiscPermissions.BASE_PING, commandDescriptionKey = "ping")
@EssentialsEquivalent(value = { "ping", "pong", "echo" }, isExact = false, notes = "Returns your latency, not your message.")
public class PingCommand implements ICommandExecutor<CommandSource> { // extends AbstractCommand.SimpleTargetOtherPlayer {

    @Override public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.commandElementSupplier()
                        .createOnlyOtherUserPermissionElement(true, MiscPermissions.OTHERS_PING)
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Player player = context.getPlayerFromArgs();
        if (context.is(player)) {
            context.sendMessage("command.ping.current.self", player.getConnection().getLatency());
        } else {
            context.sendMessage("command.ping.current.other", player.getName(), player.getConnection().getLatency());
        }

        return context.successResult();
    }
}
