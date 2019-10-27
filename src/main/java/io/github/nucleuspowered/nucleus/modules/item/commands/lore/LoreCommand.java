/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.lore;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.item.ItemPermissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = { "lore" },
        basePermission = ItemPermissions.BASE_LORE,
        commandDescriptionKey = "lore",
        hasExecutor = false
)
public class LoreCommand implements ICommandExecutor<Player> {

    // Not executed.
    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        return context.failResult(); // no-op
    }
}
