/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.annotation;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandControl;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.internal.command.requirements.ICommandModifier;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandSource;

public @interface CommandModifier {

    CommandModifiers value();

    String exemptPermission() default "";

    /**
     * If set, uses the options from the targetted class.
     *
     * @return The class, or {@link ICommandExecutor} if otherwise.
     */
    Class<? extends ICommandExecutor> useFrom() default ICommandExecutor.class;

    Class<? extends CommandSource> target() default CommandSource.class;

    /**
     * If false, don't generate configuration
     *
     * @return normally true
     */
    boolean generateConfig() default true;

    /**
     * If true, runs the {@link ICommandModifier#preExecute(ICommandContext, CommandControl, INucleusServiceCollection)}
     * before the command executes.
     *
     * @return true by default
     */
    boolean onExecute() default true;

    /**
     * If true, runs the {@link ICommandModifier#onCompletion(ICommandContext, CommandControl, INucleusServiceCollection)}
     * after a success result.
     *
     * @return true by default
     */
    boolean onCompletion() default true;

}
