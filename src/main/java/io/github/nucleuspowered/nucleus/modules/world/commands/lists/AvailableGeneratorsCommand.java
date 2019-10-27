/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.lists;

import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import io.github.nucleuspowered.nucleus.modules.world.commands.WorldCommand;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;

@NonnullByDefault
@Command(
        aliases = {"generators", "listgenerators"},
        basePermission = WorldPermissions.BASE_WORLD_CREATE,
        commandDescriptionKey = "world.generators",
        parentCommand = WorldCommand.class
)
public class AvailableGeneratorsCommand extends AvailableBaseCommand {

    public AvailableGeneratorsCommand() {
        super(GeneratorType.class, "command.world.generators.title");
    }
}
