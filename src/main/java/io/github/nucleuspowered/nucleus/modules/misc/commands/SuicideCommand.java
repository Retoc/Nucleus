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
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@EssentialsEquivalent("suicide")
@NonnullByDefault
@Command(aliases = { "suicide" }, basePermission = MiscPermissions.BASE_SUICIDE, commandDescriptionKey = "suicide")
public class SuicideCommand implements ICommandExecutor<Player> {

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        Player src = context.getIfPlayer();
        DataTransactionResult dtr = src.offer(Keys.HEALTH, 0d);
        if (!dtr.isSuccessful()) {
            return context.errorResult("command.suicide.error");
        }
        return context.successResult();
    }
}
