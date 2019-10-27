/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.quickstart;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.exceptions.ModulesLoadedException;
import io.github.nucleuspowered.nucleus.api.exceptions.NoModuleException;
import io.github.nucleuspowered.nucleus.api.exceptions.UnremovableModuleException;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.Plugin;
import uk.co.drnaylor.quickstart.ModuleHolder;
import uk.co.drnaylor.quickstart.enums.ConstructionPhase;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.exceptions.UndisableableModuleException;

import java.util.Set;

import javax.inject.Inject;

public class ModuleRegistrationProxyService implements NucleusModuleService {

    private final INucleusServiceCollection serviceCollection;
    private final ModuleHolder<?, ?> moduleHolder;

    @Inject
    public ModuleRegistrationProxyService(INucleusServiceCollection serviceCollection, ModuleHolder<?, ?> holder) {
        this.serviceCollection = serviceCollection;
        this.moduleHolder = holder;
    }

    @Override
    public Set<String> getModulesToLoad() {
        return Nucleus.getNucleus().getModuleHolder().getModules(ModuleHolder.ModuleStatusTristate.ENABLE);
    }

    @Override
    public boolean canDisableModules() {
        return Nucleus.getNucleus().getModuleHolder().getCurrentPhase() == ConstructionPhase.DISCOVERED;
    }

    @Override
    public void removeModule(String module, Object plugin) throws ModulesLoadedException, UnremovableModuleException, NoModuleException {
        if (!canDisableModules()) {
            throw new ModulesLoadedException();
        }

        // The plugin must actually be a plugin.
        Preconditions.checkNotNull(plugin);
        Plugin pluginAnnotation = plugin.getClass().getAnnotation(Plugin.class);
        if (pluginAnnotation == null) {
            throw new IllegalArgumentException("plugin must be your plugin instance.");
        }

        Logger logger = this.serviceCollection.logger();
        IMessageProviderService messageProviderService = this.serviceCollection.messageProvider();
        try {
            this.moduleHolder.disableModule(module);
            logger.info(messageProviderService.getMessageString("nucleus.module.disabled.modulerequest", pluginAnnotation.name(), pluginAnnotation.id(), module));
        } catch (IllegalStateException e) {
            throw new ModulesLoadedException();
        } catch (UndisableableModuleException e) {
            logger.warn(messageProviderService.getMessageString("nucleus.module.disabled.forceload", pluginAnnotation.name(), pluginAnnotation.id(),
                    module));
            logger.warn(messageProviderService.getMessageString("nucleus.module.disabled.forceloadtwo", pluginAnnotation.name()));
            throw new UnremovableModuleException();
        } catch (uk.co.drnaylor.quickstart.exceptions.NoModuleException e) {
            throw new NoModuleException();
        } catch (QuickStartModuleLoaderException e) {
            e.printStackTrace();
        }
    }
}
