/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail;

import io.github.nucleuspowered.nucleus.annotationprocessor.RegisterPermissions;
import io.github.nucleuspowered.nucleus.services.impl.permission.PermissionMetadata;
import io.github.nucleuspowered.nucleus.services.impl.permission.SuggestedLevel;

@RegisterPermissions
public class MailPermissions {
    private MailPermissions() {
        throw new AssertionError("Nope");
    }

    @PermissionMetadata(descriptionKey = "permission.base", replacements = { "mail clear" }, level = SuggestedLevel.USER)
    public static final String BASE_MAIL = "nucleus.mail.base";

    @PermissionMetadata(descriptionKey = "permission.base", replacements = { "mail other" }, level = SuggestedLevel.ADMIN)
    public static final String BASE_MAIL_OTHER = "nucleus.mail.other.base";

    @PermissionMetadata(descriptionKey = "permission.base", replacements = { "mail send" }, level = SuggestedLevel.USER)
    public static final String BASE_MAIL_SEND = "nucleus.mail.send.base";

    @PermissionMetadata(descriptionKey = "permission.exempt.cooldown", replacements = { "mail send" }, level = SuggestedLevel.ADMIN)
    public static final String EXEMPT_COOLDOWN_MAIL_SEND = "nucleus.mail.send.exempt.cooldown";

    @PermissionMetadata(descriptionKey = "permission.exempt.cost", replacements = { "mail send" }, level = SuggestedLevel.ADMIN)
    public static final String EXEMPT_COST_MAIL_SEND = "nucleus.mail.send.exempt.cost";

    @PermissionMetadata(descriptionKey = "permission.exempt.warmup", replacements = { "mail send" }, level = SuggestedLevel.ADMIN)
    public static final String EXEMPT_WARMUP_MAIL_SEND = "nucleus.mail.send.exempt.warmup";

}
