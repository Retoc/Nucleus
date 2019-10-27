/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.storage.services;

import io.github.nucleuspowered.nucleus.services.IStorageManager;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IWorldDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IWorldQueryObject;
import io.github.nucleuspowered.storage.services.AbstractKeyedService;

public class WorldService extends AbstractKeyedService<IWorldQueryObject, IWorldDataObject> {

    public <O> WorldService(IStorageManager repository) {
        super(repository::getWorldDataAccess, repository::getWorldRepository);
    }

}
