/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connectionmessages;

import io.github.nucleuspowered.nucleus.annotationprocessor.RegisterPermissions;
import io.github.nucleuspowered.nucleus.services.impl.permission.PermissionMetadata;
import io.github.nucleuspowered.nucleus.services.impl.permission.SuggestedLevel;

@RegisterPermissions
public class ConnectionMessagesPermissions {
    private ConnectionMessagesPermissions() {
        throw new AssertionError("Nope");
    }

    @PermissionMetadata(descriptionKey = "permission.connectionmesssages.disable", level = SuggestedLevel.NONE)
    public static final String CONNECTIONMESSSAGES_DISABLE = "nucleus.connectionmessages.disable";

}
