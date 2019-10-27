/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.gson.JsonObject;
import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.storage.StorageManager;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IGeneralDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IWorldDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IUserQueryObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IWorldQueryObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.services.IGeneralDataService;
import io.github.nucleuspowered.storage.dataaccess.IDataTranslator;
import io.github.nucleuspowered.storage.persistence.IStorageRepository;
import io.github.nucleuspowered.storage.services.IStorageService;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ImplementedBy(StorageManager.class)
public interface IStorageManager {

    IGeneralDataService getGeneralService();

    IStorageService.Keyed<UUID, IUserQueryObject, IUserDataObject> getUserService();

    IStorageService.Keyed<UUID, IWorldQueryObject, IWorldDataObject> getWorldService();

    IDataTranslator<IUserDataObject, JsonObject> getUserDataAccess();

    IDataTranslator<IWorldDataObject, JsonObject> getWorldDataAccess();

    IDataTranslator<IGeneralDataObject, JsonObject> getGeneralDataAccess();

    IStorageRepository.Keyed<UUID, IUserQueryObject, JsonObject> getUserRepository();

    IStorageRepository.Keyed<UUID, IWorldQueryObject, JsonObject> getWorldRepository();

    IStorageRepository.Single<JsonObject> getGeneralRepository();

    CompletableFuture<Void> saveAndInvalidateAllCaches();

    default CompletableFuture<IUserDataObject> getOrCreateUser(UUID uuid) {
        return getUserService().getOrNew(uuid);
    }

    default IUserDataObject getOrCreateUserOnThread(UUID uuid) {
        return getUserService().getOrNewOnThread(uuid);
    }

    default CompletableFuture<Optional<IUserDataObject>> getUser(UUID uuid) {
        return getUserService().get(uuid);
    }

    default Optional<IUserDataObject> getUserOnThread(UUID uuid) {
        return getUserService().getOnThread(uuid);
    }

    default CompletableFuture<Void> saveUser(UUID uuid, IUserDataObject object) {
        return getUserService().save(uuid, object);
    }

    default Optional<IWorldDataObject> getWorldOnThread(UUID uuid) {
        return getWorldService().getOnThread(uuid);
    }

    default IWorldDataObject getOrCreateWorldOnThread(UUID uuid) {
        return getWorldService().getOrNewOnThread(uuid);
    }

    default IGeneralDataObject getGeneral() {
        IGeneralDataService gs = getGeneralService();
        return gs.getCached().orElseGet(() -> gs.getOrNew().join());
    }

    CompletableFuture<Void> saveAll();
}
