/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.api.teleport.TeleportResult;
import io.github.nucleuspowered.nucleus.argumentparsers.IfConditionElseArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfig;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * NOTE: TeleportHere is considered an admin command, as there is a potential
 * for abuse for non-admin players trying to pull players. No cost or warmups
 * will be applied. /tpahere should be used instead in these circumstances.
 */
@EssentialsEquivalent(value = {"tphere", "s", "tpohere"}, isExact = false,
        notes = "If you have permission, this will override '/tptoggle' automatically.")
@NonnullByDefault
@Command(
        aliases = {"tphere", "tph"},
        basePermission = TeleportPermissions.BASE_TPHERE,
        commandDescriptionKey = "tphere"
)
public class TeleportHereCommand implements ICommandExecutor<Player>, Reloadable {

    private boolean isDefaultQuiet = false;

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        this.isDefaultQuiet =
                serviceCollection.moduleDataProvider()
                        .getModuleConfig(TeleportConfig.class)
                        .isDefaultQuiet();
    }

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.flags().flag("q", "-quiet").buildWith(
                        IfConditionElseArgument.permission(
                                serviceCollection.permissionService(),
                                TeleportPermissions.TPHERE_OFFLINE,
                                NucleusParameters.ONE_USER_PLAYER_KEY,
                                NucleusParameters.ONE_PLAYER))
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        boolean beQuiet = context.getOne("q", Boolean.class).orElse(this.isDefaultQuiet);
        User target = context.requireOne(NucleusParameters.Keys.PLAYER, User.class);
        PlayerTeleporterService sts = context.getServiceCollection().getServiceUnchecked(PlayerTeleporterService.class);
        if (target.getPlayer().isPresent()) {
            Player to = target.getPlayer().get();
            TeleportResult result = sts.teleportWithMessage(
                    context.getIfPlayer(),
                    to,
                    context.getIfPlayer(),
                    false,
                    beQuiet,
                    false
            );
            return result.isSuccessful() ? context.successResult() : context.failResult();
        } else {
            if (context.testPermission(TeleportPermissions.TPHERE_OFFLINE)) {
                return context.errorResult("command.tphere.noofflineperms");
            }

            Player src = context.getIfPlayer();
            // Update the offline player's next location
            target.setLocation(src.getPosition(), src.getWorld().getUniqueId());
            context.sendMessage("command.tphere.offlinesuccess", target.getName());
        }

        return context.successResult();
    }
}
