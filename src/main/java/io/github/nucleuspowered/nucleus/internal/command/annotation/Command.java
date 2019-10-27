/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.annotation;

import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

    /**
     * The command alias. If prefixed with $, a root command.
     *
     * @return The command.
     */
    String[] aliases();

    /**
     * Whether to add `/n[]` variants of the commands.
     *
     * @return whether to do so.
     */
    boolean prefixAliasesWithN() default true;

    /**
     * The parent of the subcommand.
     *
     * @return The {@link Class} of the command
     */
    Class<? extends ICommandExecutor> parentCommand() default ICommandExecutor.class;

    /**
     * Sets whether the command should register it's executor. This can be false if there are only child commands.
     *
     * @return <code>true</code> if the executor should be registered.
     */
    boolean hasExecutor() default true;

    /**
     * The basic permissions required for a command
     *
     * @return The permissions required
     */
    String[] basePermission();

    /**
     * If set, this key will be used instead of the command key when
     * probing the modifiers.
     *
     * @return The modifier override.
     */
    String modifierOverride() default "";

    /**
     * The modifiers for this command, if any
     *
     * @return The modifiers.
     */
    CommandModifier[] modifiers() default {};

    /**
     * Whether the command has a help subcommand
     *
     * @return Whether it has one
     */
    boolean hasHelpCommand() default true;

    /**
     * The basic command description key. Must have a corresponding
     * <code>[key].desc</code> entry in <code>commands.properties</code>,
     * optionally having a <code>[key].extended</code>.
     *
     * @return The root of the key
     */
    String commandDescriptionKey();

    /**
     * Determines whether to run the command async
     *
     * @return true if so
     */
    boolean async() default false;

}
