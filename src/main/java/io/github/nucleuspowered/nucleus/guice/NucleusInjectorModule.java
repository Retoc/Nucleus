/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.guice;

import com.google.inject.AbstractModule;
import io.github.nucleuspowered.nucleus.services.IModuleDataProvider;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * The base module that Nucleus will use to construct its basic services.
 */
public class NucleusInjectorModule extends AbstractModule {

    private final Supplier<Path> dataDirectory;
    private final Path configDirectory;
    private final IModuleDataProvider moduleDataProvider;

    public NucleusInjectorModule(Supplier<Path> dataDirectory, Path configDirectory, IModuleDataProvider moduleDataProvider) {
        this.dataDirectory = dataDirectory;
        this.configDirectory = configDirectory;
        this.moduleDataProvider = moduleDataProvider;
    }

    @Override protected void configure() {
        bind(Path.class).annotatedWith(DataDirectory.class).toProvider(this.dataDirectory::get);
        bind(Path.class).annotatedWith(ConfigDirectory.class).toInstance(this.configDirectory);
        bind(IModuleDataProvider.class).toInstance(this.moduleDataProvider);
    }
}
