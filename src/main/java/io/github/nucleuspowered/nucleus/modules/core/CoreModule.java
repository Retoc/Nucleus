/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.catalogkeys.NucleusTeleportHelperFilters;
import io.github.nucleuspowered.nucleus.internal.CatalogTypeFinalStaticProcessor;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.quickstart.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.teleport.filters.NoCheckFilter;
import io.github.nucleuspowered.nucleus.modules.core.teleport.filters.WallCheckFilter;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;

import java.util.function.Supplier;

@ModuleData(id = CoreModule.ID, name = "Core", isRequired = true)
public class CoreModule extends ConfigurableModule<CoreConfig, CoreConfigAdapter> {

    public static final String ID = "core";

    @Inject
    public CoreModule(Supplier<DiscoveryModuleHolder<?, ?>> moduleHolder,
            INucleusServiceCollection collection) {
        super(moduleHolder, collection);
    }

    @Override
    public CoreConfigAdapter createAdapter() {
        return new CoreConfigAdapter();
    }

    @Override public void performPreTasks(INucleusServiceCollection serviceCollection) throws Exception {
        super.performPreTasks(serviceCollection);

        CatalogTypeFinalStaticProcessor.setFinalStaticFields(
                NucleusTeleportHelperFilters.class,
                ImmutableMap.<String, TeleportHelperFilter>builder()
                    .put("NO_CHECK", new NoCheckFilter())
                    .put("WALL_CHECK", new WallCheckFilter())
                    .build()
        );

        // TODO: Reload provider?
        this.serviceCollection.messageProvider().reloadMessageFile();
    }

    @Override public void performPostTasks(INucleusServiceCollection serviceCollection) {
        super.performPostTasks(serviceCollection);
        this.serviceCollection.userPreferenceService().postInit();
    }
}
