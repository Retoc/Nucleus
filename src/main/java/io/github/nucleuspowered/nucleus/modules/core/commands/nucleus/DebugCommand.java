/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.nucleus;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.core.CorePermissions;
import io.github.nucleuspowered.nucleus.modules.core.commands.NucleusCommand;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = "debug",
        basePermission = CorePermissions.BASE_NUCLEUS_DEBUG,
        commandDescriptionKey = "nucleus.debug",
        parentCommand = NucleusCommand.class,
        hasExecutor = false
)
public class DebugCommand implements ICommandExecutor<CommandSource> {

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) {
        return context.failResult();
    }

}
