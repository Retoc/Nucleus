/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.storage.persistence;

import com.google.gson.JsonObject;
import io.github.nucleuspowered.nucleus.guice.DataDirectory;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IUserQueryObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IWorldQueryObject;
import io.github.nucleuspowered.storage.exceptions.DataQueryException;
import io.github.nucleuspowered.storage.persistence.IStorageRepository;
import io.github.nucleuspowered.storage.persistence.IStorageRepositoryFactory;
import io.github.nucleuspowered.storage.queryobjects.IQueryObject;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public final class FlatFileStorageRepositoryFactory implements IStorageRepositoryFactory<JsonObject> {

    private static final String wd = "worlddata";
    private static final String ud = "userdata";
    private final Provider<Path> dataPath;
    private final Logger logger;

    @Inject public FlatFileStorageRepositoryFactory(@DataDirectory Provider<Path> path, Logger logger) {
        this.dataPath = path;
        this.logger = logger;
    }

    @Override
    public IStorageRepository.Keyed<UUID, IUserQueryObject, JsonObject> userRepository() {
        return repository(ud);
    }

    @Override
    public IStorageRepository.Keyed<UUID, IWorldQueryObject, JsonObject> worldRepository() {
        return repository(wd);
    }

    private <R extends IQueryObject<UUID, R>> IStorageRepository.Keyed<UUID, R, JsonObject> repository(final String p) {
        return new FlatFileStorageRepository.UUIDKeyed<>(this.logger, query -> {
            if (query.keys().size() == 1) {
                Collection<UUID> uuids = query.keys();
                String uuid = uuids.iterator().next().toString();
                return this.dataPath.get().resolve(p).resolve(uuid.substring(0, 2)).resolve(uuid  + ".json");
            }

            throw new DataQueryException("There must only a key", query);
        },
        uuid -> this.dataPath.get().resolve(p).resolve(uuid.toString().substring(0, 2)).resolve(uuid.toString() + ".json"),
        () -> this.dataPath.get().resolve(p));
    }

    @Override
    public IStorageRepository.Single<JsonObject> generalRepository() {
        return new FlatFileStorageRepository.Single(this.logger, () -> this.dataPath.get().resolve("general.json"));
    }

    @Override public String getId() {
        return "nucleus:flatfile";
    }

    @Override public String getName() {
        return "Flat File";
    }
}
