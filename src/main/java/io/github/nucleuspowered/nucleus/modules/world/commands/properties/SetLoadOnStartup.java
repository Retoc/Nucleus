/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.properties;

import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import io.github.nucleuspowered.nucleus.modules.world.commands.WorldCommand;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

@Command(
        aliases = {"setloadonstartup"},
        basePermission = WorldPermissions.BASE_WORLD_SETLOADONSTARTUP,
        commandDescriptionKey = "world.setloadonstartup",
        parentCommand = WorldCommand.class
)
@NonnullByDefault
public class SetLoadOnStartup extends AbstractPropertiesSetCommand {

    public SetLoadOnStartup() {
        super("load on startup");
    }

    @Override protected void setter(WorldProperties worldProperties, boolean set) {
        worldProperties.setLoadOnStartup(set);
    }
}
