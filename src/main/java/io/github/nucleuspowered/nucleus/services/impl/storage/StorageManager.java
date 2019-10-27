/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.storage;

import com.google.gson.JsonObject;
import io.github.nucleuspowered.nucleus.guice.DataDirectory;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IStorageManager;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataaccess.IConfigurateBackedDataTranslator;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.GeneralDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IGeneralDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IWorldDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.UserDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.WorldDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.persistence.FlatFileStorageRepositoryFactory;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IUserQueryObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IWorldQueryObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.registry.IStorageRepositoryFactoryRegistryModule;
import io.github.nucleuspowered.nucleus.services.impl.storage.services.GeneralService;
import io.github.nucleuspowered.nucleus.services.impl.storage.services.UserService;
import io.github.nucleuspowered.nucleus.services.impl.storage.services.WorldService;
import io.github.nucleuspowered.storage.dataaccess.IDataTranslator;
import io.github.nucleuspowered.storage.persistence.IStorageRepository;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class StorageManager implements IStorageManager, Reloadable {

    private final FlatFileStorageRepositoryFactory flatFileStorageRepositoryFactory;

    @Inject
    StorageManager(@DataDirectory Provider<Path> dataDirectory, Logger logger) {
        this.flatFileStorageRepositoryFactory = new FlatFileStorageRepositoryFactory(dataDirectory, logger);
        new IStorageRepositoryFactoryRegistryModule(this.flatFileStorageRepositoryFactory);
    }

    @Nullable
    private IStorageRepository.Keyed<UUID, IUserQueryObject, JsonObject> userRepository;

    @Nullable
    private IStorageRepository.Keyed<UUID, IWorldQueryObject, JsonObject> worldRepository;

    @Nullable
    private IStorageRepository.Single<JsonObject> generalRepository;

    private final IConfigurateBackedDataTranslator<IUserDataObject> userDataAccess = UserDataObject::new;
    private final IConfigurateBackedDataTranslator<IWorldDataObject> worldDataAccess = WorldDataObject::new;
    private final IConfigurateBackedDataTranslator<IGeneralDataObject> generalDataAccess = GeneralDataObject::new;

    private final GeneralService generalService = new GeneralService(this);
    private final UserService userService = new UserService(this);
    private final WorldService worldService = new WorldService(this);

    @Override
    public GeneralService getGeneralService() {
        return this.generalService;
    }

    @Override
    public UserService getUserService() {
        return this.userService;
    }

    @Override
    public WorldService getWorldService() {
        return this.worldService;
    }

    @Override public IDataTranslator<IUserDataObject, JsonObject> getUserDataAccess() {
        return this.userDataAccess;
    }

    @Override public IDataTranslator<IWorldDataObject, JsonObject> getWorldDataAccess() {
        return this.worldDataAccess;
    }

    @Override public IDataTranslator<IGeneralDataObject, JsonObject> getGeneralDataAccess() {
        return this.generalDataAccess;
    }

    @Override
    public IStorageRepository.Keyed<UUID, IUserQueryObject, JsonObject> getUserRepository() {
        if (this.userRepository == null) {
            // fallback to flat file
            this.userRepository = this.flatFileStorageRepositoryFactory.userRepository();
        }
        return this.userRepository;
    }

    @Override
    public IStorageRepository.Keyed<UUID, IWorldQueryObject, JsonObject> getWorldRepository() {
        if (this.worldRepository== null) {
            // fallback to flat file
            this.worldRepository = this.flatFileStorageRepositoryFactory.worldRepository();
        }
        return this.worldRepository;
    }

    @Override
    public IStorageRepository.Single<JsonObject> getGeneralRepository() {
        if (this.generalRepository == null) {
            // fallback to flat file
            this.generalRepository = this.flatFileStorageRepositoryFactory.generalRepository();
        }
        return this.generalRepository;
    }

    @Override
    public CompletableFuture<Void> saveAndInvalidateAllCaches() {
        CompletableFuture<Void> a = this.generalService.ensureSaved().whenComplete((cv, t) -> this.generalService.clearCache());
        CompletableFuture<Void> b = this.userService.ensureSaved().whenComplete((cv, t) -> this.userService.clearCache());
        CompletableFuture<Void> c = this.worldService.ensureSaved().whenComplete((cv, t) -> this.worldService.clearCache());
        return CompletableFuture.allOf(a, b, c);
    }

    @Override public CompletableFuture<Void> saveAll() {
        CompletableFuture<Void> a = this.generalService.ensureSaved();
        CompletableFuture<Void> b = this.userService.ensureSaved();
        CompletableFuture<Void> c = this.worldService.ensureSaved();
        return CompletableFuture.allOf(a, b, c);
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        // TODO: Data registry
        if (this.generalRepository != null) {
            this.generalRepository.shutdown();
        }

        this.generalRepository = null; // TODO: config

        if (this.worldRepository != null) {
            this.worldRepository.shutdown();
        }

        this.worldRepository = null; // TODO: config

        if (this.userRepository != null) {
            this.userRepository.shutdown();
        }

        this.userRepository = null; // TODO: config
    }

}
