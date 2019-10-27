/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connection;

import io.github.nucleuspowered.nucleus.annotationprocessor.RegisterPermissions;
import io.github.nucleuspowered.nucleus.services.impl.permission.PermissionMetadata;
import io.github.nucleuspowered.nucleus.services.impl.permission.SuggestedLevel;

@RegisterPermissions
public class ConnectionPermissions {
    private ConnectionPermissions() {
        throw new AssertionError("Nope");
    }

    @PermissionMetadata(descriptionKey = "permission.connection.joinfullserver", level = SuggestedLevel.MOD)
    public static final String CONNECTION_JOINFULLSERVER = "nucleus.connection.joinfullserver";

}
