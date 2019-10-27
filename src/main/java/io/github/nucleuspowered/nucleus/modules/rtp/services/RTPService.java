/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.services;

import io.github.nucleuspowered.nucleus.api.rtp.RTPKernel;
import io.github.nucleuspowered.nucleus.api.service.NucleusRTPService;
import io.github.nucleuspowered.nucleus.internal.annotations.APIService;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.interfaces.ServiceBase;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfig;
import io.github.nucleuspowered.nucleus.modules.rtp.options.RTPOptionsBuilder;
import io.github.nucleuspowered.nucleus.modules.rtp.registry.RTPRegistryModule;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.annotation.Nullable;

@APIService(NucleusRTPService.class)
public class RTPService implements NucleusRTPService, Reloadable, ServiceBase {

    private RTPConfig config = new RTPConfig();

    @Override
    public RTPOptions options(@Nullable WorldProperties world) {
        @Nullable String name = world == null ? null : world.getWorldName();
        return new io.github.nucleuspowered.nucleus.modules.rtp.options.RTPOptions(this.config, name);
    }

    @Override
    public RTPOptions.Builder optionsBuilder() {
        return new RTPOptionsBuilder();
    }

    @Override
    public RTPKernel getDefaultKernel() {
        return this.config.getKernel();
    }

    @Override
    public void registerKernel(RTPKernel kernel) {
        RTPRegistryModule.getInstance().registerAdditionalCatalog(kernel);
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        // create the new RTPOptions
        this.config = serviceCollection.moduleDataProvider().getModuleConfig(RTPConfig.class);
    }
}
