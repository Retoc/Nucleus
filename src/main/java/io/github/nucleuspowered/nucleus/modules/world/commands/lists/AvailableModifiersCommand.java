/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.lists;

import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import io.github.nucleuspowered.nucleus.modules.world.commands.WorldCommand;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

@NonnullByDefault
@Command(
        aliases = {"modifiers", "listmodifiers"},
        basePermission = WorldPermissions.BASE_WORLD_CREATE,
        commandDescriptionKey = "world.modifiers",
        parentCommand = WorldCommand.class
)
public class AvailableModifiersCommand extends AvailableBaseCommand {

    public AvailableModifiersCommand() {
        super(WorldGeneratorModifier.class, "command.world.modifiers.title");
    }

}
