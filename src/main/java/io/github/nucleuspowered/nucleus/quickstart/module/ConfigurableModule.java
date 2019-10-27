/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.quickstart.module;

import io.github.nucleuspowered.nucleus.quickstart.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class ConfigurableModule<C, A extends NucleusConfigAdapter.Standard<C>> extends StandardModule {

    private A adapter;

    public ConfigurableModule(Supplier<DiscoveryModuleHolder<?, ?>> moduleHolder, INucleusServiceCollection collection) {
        super(moduleHolder, collection);
    }

    /**
     * Gets a new instance of the unattached config adapter.
     *
     * @return The adapter.
     */
    public abstract A createAdapter();

    protected final A getAdapter() {
        if (this.adapter == null) {
            this.adapter = createAdapter();
        }

        return this.adapter;
    }

    @Override
    public final Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        // We need to use the right type...
        return Optional.of(getAdapter());
    }

    @Override public void configTasks() {
        // Register the config on the config provider
        getServiceCollection().moduleDataProvider().registerModuleConfig(
                getClass().getAnnotation(ModuleData.class).id(),
                getAdapter().getConfigClass(),
                () -> getAdapter().getNodeOrDefault());
        // this.plugin.getDocGenCache().ifPresent(x -> x.addConfigurableModule(this.getClass().getAnnotation(ModuleData.class).id(), this));
    }

}
