/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportUserPrefKeys;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IUserPreferenceService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@EssentialsEquivalent("tptoggle")
@Command(
        aliases = "tptoggle",
        basePermission = TeleportPermissions.BASE_TPTOGGLE,
        commandDescriptionKey = "tptoggle",
        async = true
)
public class TeleportToggleCommand implements ICommandExecutor<Player> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection service) {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_ONE_TRUE_FALSE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        IUserPreferenceService ups = context.getServiceCollection().userPreferenceService();
        Player pl = context.getIfPlayer();
        boolean toggle = ups.get(pl.getUniqueId(), TeleportUserPrefKeys.TELEPORT_TARGETABLE).get(); // we know it's always there
        boolean flip = context.getOne(NucleusParameters.Keys.BOOL, Boolean.class).orElseGet(() -> !toggle);
        ups.set(pl.getUniqueId(), TeleportUserPrefKeys.TELEPORT_TARGETABLE, flip);
        context.sendMessage(
                "command.tptoggle.success", flip ? "loc:standard.enabled" : "loc:standard.disabled"
        );
        return context.successResult();
    }
}
