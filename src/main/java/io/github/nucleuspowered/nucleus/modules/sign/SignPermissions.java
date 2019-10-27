/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.sign;

import io.github.nucleuspowered.nucleus.annotationprocessor.RegisterPermissions;
import io.github.nucleuspowered.nucleus.services.impl.permission.PermissionMetadata;
import io.github.nucleuspowered.nucleus.services.impl.permission.SuggestedLevel;

@RegisterPermissions
public class SignPermissions {
    private SignPermissions() {
        throw new AssertionError("Nope");
    }

    @PermissionMetadata(descriptionKey = "permission.sign.formatting", level = SuggestedLevel.ADMIN)
    public static final String SIGN_FORMATTING = "nucleus.sign.formatting";

}
