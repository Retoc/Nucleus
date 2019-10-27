/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Provides config objects.
 */
public interface IModuleDataProvider {

    boolean isLoaded(String id);

    /**
     * Registers how to obtain a config object.
     *
     * @param moduleId The module ID of the config.
     * @param typeOfConfig The type of configuration object.
     * @param configGetter The {@link Supplier} that grabs the config.
     * @param <T> The type of config
     */
    <T> void registerModuleConfig(String moduleId, Class<T> typeOfConfig, Supplier<T> configGetter);

    /**
     * Gets the configuration of the given type.
     *
     * @param configType The type of configuration
     * @param <T> The type
     * @return The configuration
     * @throws IllegalArgumentException If the given type is invalid
     */
    <T> T getModuleConfig(Class<T> configType) throws IllegalArgumentException;

    Map<String, Class<?>> getModuleToConfigType();

    <T> T getDefaultModuleConfig(Class<T> configType) throws IllegalArgumentException;

}
