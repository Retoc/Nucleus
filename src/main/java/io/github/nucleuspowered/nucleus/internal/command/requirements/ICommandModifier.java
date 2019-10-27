/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.requirements;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.config.CommandModifiersConfig;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandControl;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.interfaces.SimpleReloadable;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public interface ICommandModifier extends Reloadable {

    default void setupConfig(CommandModifiersConfig config, ConfigurationNode node) { }

    /**
     * Returns whether this can execute and therefore modify the command.
     *
     * @return if so.
     */
    default boolean canExecuteModifier(INucleusServiceCollection serviceCollection, ICommandContext<? extends CommandSource> source) throws CommandException {
        return true;
    }

    /**
     * Tests to see if the state fulfills this requirement.
     *
     * <p>This will return an empty optional if the requirement is met, or
     * a {@link Text} object otherwise, explaining the problem.</p>
     */
    default Optional<Text> testRequirement(ICommandContext<? extends CommandSource> source,
            CommandControl control,
            INucleusServiceCollection serviceCollection) throws CommandException {
        return Optional.empty();
    }

    /**
     * Defines whether a command should continue after pre-execute.
     *
     * @param source The source
     * @param control The {@link CommandControl}
     * @param serviceCollection The {@link INucleusServiceCollection}
     * @return a success if everything is OK but we need to stop, a fail if we're stopping, empty to continue.
     */
    default Optional<ICommandResult> preExecute(ICommandContext<? extends CommandSource> source,
            CommandControl control,
            INucleusServiceCollection serviceCollection) {
        return Optional.empty();
    }

    default void onCompletion(ICommandContext<? extends CommandSource> source,
            CommandControl control,
            INucleusServiceCollection serviceCollection) throws CommandException {
    }

    @Override
    default void onReload(INucleusServiceCollection serviceCollection) { }

}
