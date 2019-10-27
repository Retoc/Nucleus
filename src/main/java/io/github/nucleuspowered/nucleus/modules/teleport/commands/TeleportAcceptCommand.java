/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@EssentialsEquivalent({"tpaccept", "tpyes"})
@Command(
        aliases = {"tpaccept", "teleportaccept", "tpyes"},
        basePermission = TeleportPermissions.BASE_TPACCEPT,
        commandDescriptionKey = "tpaccept"
)
public class TeleportAcceptCommand implements ICommandExecutor<Player> {

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        return context.getResultFromBoolean(context.getServiceCollection()
                .getServiceUnchecked(PlayerTeleporterService.class)
                .accept(context.getIfPlayer()));
    }

}
