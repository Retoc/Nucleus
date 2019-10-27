/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.api.service.NucleusSafeTeleportService;
import io.github.nucleuspowered.nucleus.api.teleport.TeleportResult;
import io.github.nucleuspowered.nucleus.api.teleport.TeleportScanner;
import io.github.nucleuspowered.nucleus.services.impl.teleport.SafeTeleportService;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;

@ImplementedBy(SafeTeleportService.class)
public interface INucleusTeleportService extends NucleusSafeTeleportService {

    boolean setLocation(Player player, Location<World> location);

    TeleportResult teleportPlayerSmart(Player player,
            Transform<World> transform,
            boolean centreBlock,
            boolean safe,
            TeleportScanner scanner);

    TeleportResult teleportPlayerSmart(Player player,
            Location<World> location,
            boolean centreBlock,
            boolean safe,
            TeleportScanner scanner);

    TeleportResult teleportPlayerSmart(Player player,
            Location<World> location,
            Vector3d rotation,
            boolean centreBlock,
            boolean safe,
            TeleportScanner scanner);

    TeleportHelperFilter getAppropriateFilter(Player src, boolean safeTeleport);

    BorderDisableSession temporarilyDisableBorder(boolean reset, World world);

    interface BorderDisableSession extends AutoCloseable {

        @Override default void close() { }
    }
}
