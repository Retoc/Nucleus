/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.modules.misc.MiscPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = {"extinguish", "ext"},
        basePermission = MiscPermissions.BASE_EXTINGUISH,
        commandDescriptionKey = "extinguish",
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = MiscPermissions.EXEMPT_COOLDOWN_EXTINGUISH),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = MiscPermissions.EXEMPT_WARMUP_EXTINGUISH),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = MiscPermissions.EXEMPT_COST_EXTINGUISH)
        }
)
public class ExtinguishCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.commandElementSupplier()
                    .createOnlyOtherUserPermissionElement(true, MiscPermissions.OTHERS_EXTINGUISH)
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Player target = context.getPlayerFromArgs();
                // this.getUserFromArgs(Player.class, src, NucleusParameters.Keys.PLAYER, args);
        if (target.get(Keys.FIRE_TICKS).orElse(-1) > 0 && target.offer(Keys.FIRE_TICKS, 0).isSuccessful()) {
            context.sendMessage("command.extinguish.success", target.getName());
            return context.successResult();
        }

        return context.errorResult("command.extinguish.failed", target.getName());
    }
}
