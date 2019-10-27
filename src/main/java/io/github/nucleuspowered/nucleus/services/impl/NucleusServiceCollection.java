/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl;

import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.guice.ConfigDirectory;
import io.github.nucleuspowered.nucleus.guice.DataDirectory;
import io.github.nucleuspowered.nucleus.services.ICommandElementSupplier;
import io.github.nucleuspowered.nucleus.services.ICommandMetadataService;
import io.github.nucleuspowered.nucleus.services.IConfigurateHelper;
import io.github.nucleuspowered.nucleus.services.ICooldownService;
import io.github.nucleuspowered.nucleus.services.IEconomyServiceProvider;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.IMessageTokenService;
import io.github.nucleuspowered.nucleus.services.IModuleDataProvider;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.INucleusTeleportService;
import io.github.nucleuspowered.nucleus.services.INucleusTextTemplateFactory;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import io.github.nucleuspowered.nucleus.services.IPlayerDisplayNameService;
import io.github.nucleuspowered.nucleus.services.IPlayerInformationService;
import io.github.nucleuspowered.nucleus.services.IPlayerOnlineService;
import io.github.nucleuspowered.nucleus.services.IReloadableService;
import io.github.nucleuspowered.nucleus.services.IStorageManager;
import io.github.nucleuspowered.nucleus.services.ITextFileControllerCollection;
import io.github.nucleuspowered.nucleus.services.ITextStyleService;
import io.github.nucleuspowered.nucleus.services.IUserCacheService;
import io.github.nucleuspowered.nucleus.services.IUserPreferenceService;
import io.github.nucleuspowered.nucleus.services.IWarmupService;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class NucleusServiceCollection implements INucleusServiceCollection {

    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> suppliers = new HashMap<>();

    private final Provider<IMessageProviderService> messageProviderService;
    private final Provider<IEconomyServiceProvider> economyServiceProvider;
    private final Provider<IWarmupService> warmupService;
    private final Provider<ICooldownService> cooldownService;
    private final Provider<IPermissionService> permissionCheckService;
    private final Provider<IReloadableService> reloadableService;
    private final Provider<IPlayerOnlineService> playerOnlineService;
    private final Provider<IMessageTokenService> messageTokenService;
    private final Provider<IStorageManager> storageManager;
    private final Provider<IUserPreferenceService> userPreferenceService;
    private final Provider<ICommandMetadataService> commandMetadataService;
    private final Provider<IPlayerDisplayNameService> playerDisplayNameService;
    private final Provider<IModuleDataProvider> moduleConfigProvider;
    private final Provider<INucleusTeleportService> nucleusTeleportServiceProvider;
    private final Provider<ICommandElementSupplier> commandElementSupplierProvider;
    private final Provider<INucleusTextTemplateFactory> nucleusTextTemplateFactoryProvider;
    private final Provider<ITextFileControllerCollection> textFileControllerCollectionProvider;
    private final Provider<IUserCacheService> userCacheServiceProvider;
    private final Provider<IPlayerInformationService> playerInformationServiceProvider;
    private final Provider<IConfigurateHelper> configurateHelperProvider;
    private final Injector injector;
    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Provider<ITextStyleService> textStyleServiceProvider;
    private final Path dataDir;
    private final Path configPath;

    @Inject
    public NucleusServiceCollection(
            Provider<IMessageProviderService> messageProviderService,
            Provider<IEconomyServiceProvider> economyServiceProvider,
            Provider<IWarmupService> warmupService,
            Provider<ICooldownService> cooldownService,
            Provider<IUserPreferenceService> userPreferenceService,
            Provider<IPermissionService> permissionCheckService,
            Provider<IReloadableService> reloadableService,
            Provider<IPlayerOnlineService> playerOnlineService,
            Provider<IMessageTokenService> messageTokenService,
            Provider<IStorageManager> storageManager,
            Provider<ICommandMetadataService> commandMetadataService,
            Provider<IPlayerDisplayNameService> playerDisplayNameService,
            Provider<IModuleDataProvider> moduleConfigProvider,
            Provider<INucleusTeleportService> nucleusTeleportServiceProvider,
            Provider<ITextStyleService> textStyleServiceProvider,
            Provider<ICommandElementSupplier> commandElementSupplierProvider,
            Provider<INucleusTextTemplateFactory> nucleusTextTemplateFactoryProvider,
            Provider<ITextFileControllerCollection> textFileControllerCollectionProvider,
            Provider<IUserCacheService> userCacheServiceProvider,
            Provider<IPlayerInformationService> playerInformationServiceProvider,
            Provider<IConfigurateHelper> configurateHelperProvider,
            Injector injector,
            PluginContainer pluginContainer,
            Logger logger,
            @DataDirectory Path dataPath,
            @ConfigDirectory Path configPath) {
        this.messageProviderService = messageProviderService;
        this.economyServiceProvider = economyServiceProvider;
        this.warmupService = warmupService;
        this.cooldownService = cooldownService;
        this.userPreferenceService = userPreferenceService;
        this.permissionCheckService = permissionCheckService;
        this.reloadableService = reloadableService;
        this.playerOnlineService = playerOnlineService;
        this.messageTokenService = messageTokenService;
        this.storageManager = storageManager;
        this.commandMetadataService = commandMetadataService;
        this.playerDisplayNameService = playerDisplayNameService;
        this.moduleConfigProvider = moduleConfigProvider;
        this.nucleusTeleportServiceProvider = nucleusTeleportServiceProvider;
        this.textStyleServiceProvider = textStyleServiceProvider;
        this.commandElementSupplierProvider = commandElementSupplierProvider;
        this.nucleusTextTemplateFactoryProvider = nucleusTextTemplateFactoryProvider;
        this.textFileControllerCollectionProvider = textFileControllerCollectionProvider;
        this.userCacheServiceProvider = userCacheServiceProvider;
        this.playerInformationServiceProvider = playerInformationServiceProvider;
        this.configurateHelperProvider = configurateHelperProvider;
        this.injector = injector;
        this.pluginContainer = pluginContainer;
        this.logger = logger;
        this.dataDir = dataPath;
        this.configPath = configPath;
    }

    @Override
    public IMessageProviderService messageProvider() {
        return this.messageProviderService.get();
    }

    @Override
    public IPermissionService permissionService() {
        return this.permissionCheckService.get();
    }

    @Override
    public IEconomyServiceProvider economyServiceProvider() {
        return this.economyServiceProvider.get();
    }

    @Override
    public IWarmupService warmupService() {
        return this.warmupService.get();
    }

    @Override
    public ICooldownService cooldownService() {
        return this.cooldownService.get();
    }

    @Override
    public IUserPreferenceService userPreferenceService() {
        return this.userPreferenceService.get();
    }

    @Override
    public IReloadableService reloadableService() {
        return this.reloadableService.get();
    }

    @Override public IPlayerOnlineService playerOnlineService() {
        return this.playerOnlineService.get();
    }

    @Override public IMessageTokenService messageTokenService() {
        return this.messageTokenService.get();
    }

    @Override public IStorageManager storageManager() {
        return this.storageManager.get();
    }

    @Override public ICommandMetadataService commandMetadataService() {
        return this.commandMetadataService.get();
    }

    @Override public IPlayerDisplayNameService playerDisplayNameService() {
        return this.playerDisplayNameService.get();
    }

    @Override public IModuleDataProvider moduleDataProvider() {
        return this.moduleConfigProvider.get();
    }

    @Override public INucleusTeleportService teleportService() {
        return this.nucleusTeleportServiceProvider.get();
    }

    @Override public ICommandElementSupplier commandElementSupplier() {
        return this.commandElementSupplierProvider.get();
    }

    @Override public INucleusTextTemplateFactory textTemplateFactory() {
        return this.nucleusTextTemplateFactoryProvider.get();
    }

    @Override public ITextFileControllerCollection textFileControllerCollection() {
        return this.textFileControllerCollectionProvider.get();
    }

    @Override public ITextStyleService textStyleService() {
        return this.textStyleServiceProvider.get();
    }

    @Override public IPlayerInformationService playerInformationService() {
        return this.playerInformationServiceProvider.get();
    }

    @Override public IConfigurateHelper configurateHelper() {
        return this.configurateHelperProvider.get();
    }

    @Override public IUserCacheService userCacheService() {
        return this.userCacheServiceProvider.get();
    }

    @Override
    public Injector injector() {
        return this.injector;
    }

    @Override
    public PluginContainer pluginContainer() {
        return this.pluginContainer;
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public <I, C extends I> void registerService(Class<I> key, C service, boolean rereg) {
        if (!rereg && (this.instances.containsKey(key) || this.suppliers.containsKey(key))) {
            return;
        }

        this.suppliers.remove(key);
        this.instances.put(key, service);
    }

    @Override
    public <I, C extends I> void registerServiceSupplier(Class<I> key, Supplier<C> service, boolean rereg) {
        if (!rereg && (this.instances.containsKey(key) || this.suppliers.containsKey(key))) {
            return;
        }

        this.instances.remove(key);
        this.suppliers.put(key, service);
    }

    @Override @SuppressWarnings("unchecked")
    public <I> Optional<I> getService(Class<I> key) {
        if (this.instances.containsKey(key)) {
            return Optional.of((I) this.instances.get(key));
        } else if (this.suppliers.containsKey(key)) {
            return Optional.of((I) this.suppliers.get(key).get());
        }

        return Optional.empty();
    }

    @Override @SuppressWarnings("unchecked")
    public <I> I getServiceUnchecked(Class<I> key) {
        if (this.instances.containsKey(key)) {
            return (I) this.instances.get(key);
        } else if (this.suppliers.containsKey(key)) {
            return (I) this.suppliers.get(key).get();
        }

        throw new NoSuchElementException(key.getName());
    }

    @Override public Path configDir() {
        return this.configPath;
    }

    @Override public Path dataDir() {
        return this.dataDir;
    }

}
