/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.services;

import io.github.nucleuspowered.nucleus.api.service.NucleusBackService;
import io.github.nucleuspowered.nucleus.internal.annotations.APIService;
import io.github.nucleuspowered.nucleus.internal.interfaces.ServiceBase;
import io.github.nucleuspowered.nucleus.modules.back.listeners.OfflineLocation;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@APIService(NucleusBackService.class)
public class BackHandler implements NucleusBackService, ServiceBase {

    private final Map<UUID, OfflineLocation> lastLocations = new HashMap<>();
    private final Set<UUID> shouldNotLog = new HashSet<>();

    @Override
    public Optional<Transform<World>> getLastLocation(User user) {
        OfflineLocation location = this.lastLocations.get(user.getUniqueId());
        if (location == null) {
            return Optional.empty();
        }
        return location.getLastLocation();
    }

    @Override
    public void setLastLocation(User user, Transform<World> location) {
        this.lastLocations.put(user.getUniqueId(), new OfflineLocation(location));
    }

    @Override
    public void removeLastLocation(User user) {
        this.lastLocations.remove(user.getUniqueId());
    }

    @Override
    public boolean isLoggingLastLocation(User user) {
        return !this.shouldNotLog.contains(user.getUniqueId());
    }

    @Override
    public void setLoggingLastLocation(User user, boolean log) {
        if (log) {
            this.shouldNotLog.add(user.getUniqueId());
        } else {
            this.shouldNotLog.remove(user.getUniqueId());
        }
    }
}
