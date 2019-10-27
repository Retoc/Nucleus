/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.storage.services;

import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IGeneralDataObject;
import io.github.nucleuspowered.storage.services.IStorageService;

import java.util.Optional;

public interface IGeneralDataService extends IStorageService.Single<IGeneralDataObject> {

    Optional<IGeneralDataObject> getCached();
}
