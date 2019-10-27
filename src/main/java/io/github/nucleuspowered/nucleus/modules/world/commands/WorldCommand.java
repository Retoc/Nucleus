/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = { "world", "nworld", "nucleusworld" },
        basePermission = WorldPermissions.BASE_WORLD,
        commandDescriptionKey = "world",
        hasExecutor = false,
        prefixAliasesWithN = false
)
public class WorldCommand implements ICommandExecutor<CommandSource> {

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        return context.failResult();
    }
}
