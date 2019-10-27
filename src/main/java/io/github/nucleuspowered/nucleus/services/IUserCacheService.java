/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.impl.usercache.UserCacheService;

import java.util.List;
import java.util.UUID;

@ImplementedBy(UserCacheService.class)
public interface IUserCacheService {

    void load();

    void save();

    List<UUID> getForIp(String ip);

    List<UUID> getJailed();

    List<UUID> getJailedIn(String name);

    List<UUID> getMuted();

    void updateCacheForOnlinePlayers();

    void updateCacheForPlayer(UUID uuid, IUserDataObject u);

    void updateCacheForPlayer(UUID uuid);

    void startFilewalkIfNeeded();

    boolean isCorrectVersion();

    boolean fileWalk();
}
