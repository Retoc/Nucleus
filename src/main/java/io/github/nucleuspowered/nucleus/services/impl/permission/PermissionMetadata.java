/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.permission;

import io.github.nucleuspowered.nucleus.services.impl.permission.SuggestedLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides metadata about a permission.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PermissionMetadata {

    /**
     * The replacements for the description key.
     *
     * @return The replacements, if any.
     */
    String[] replacements() default {};

    /**
     * The message key that describes this permission
     *
     * @return The description key
     */
    String descriptionKey();

    /**
     * The {@link SuggestedLevel} that this permission belongs to.
     *
     * @return The level
     */
    SuggestedLevel level() default SuggestedLevel.ADMIN;

    /**
     * Whether the permission is simply a prefix (such as nucleus.kits).
     *
     * @return If a prefix.
     */
    boolean isPrefix() default false;

    /**
     * Whether the console can ignore this permission when used as an exemption
     *
     * @return true if so
     */
    boolean isConsoleBypassable() default false;
}
