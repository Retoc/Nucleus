/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.services;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.api.service.NucleusPlayerMetadataService;
import io.github.nucleuspowered.nucleus.internal.annotations.APIService;
import io.github.nucleuspowered.nucleus.internal.interfaces.ServiceBase;
import io.github.nucleuspowered.nucleus.modules.core.CoreKeys;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IStorageManager;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@APIService(NucleusPlayerMetadataService.class)
@NonnullByDefault
public class PlayerMetadataService implements NucleusPlayerMetadataService, ServiceBase {

    private final IStorageManager storageManager;

    @Inject
    public PlayerMetadataService(INucleusServiceCollection serviceCollection) {
        this.storageManager = serviceCollection.storageManager();
    }

    @Override public Optional<Result> getUserData(UUID uuid) {
        return this.storageManager.getUserService().get(uuid).join().map(x -> new ResultImpl(uuid, x));
    }

    public static class ResultImpl implements Result {

        // private final User user;

        private final UUID uuid;
        @Nullable private final Instant login;
        @Nullable private final Instant logout;
        @Nullable private final String lastIP;

        private ResultImpl(UUID uuid, IUserDataObject udo) {
            // this.user = userService.getUser();

            this.uuid = uuid;
            this.login = udo.get(CoreKeys.LAST_LOGIN).orElse(null);
            this.logout = udo.get(CoreKeys.LAST_LOGOUT).orElse(null);
            this.lastIP = udo.get(CoreKeys.IP_ADDRESS).orElse(null);
        }

        @Override public Optional<Instant> getLastLogin() {
            return Optional.ofNullable(this.login);
        }

        @Override public Optional<Instant> getLastLogout() {
            return Optional.ofNullable(this.logout);
        }

        @Override public Optional<String> getLastIP() {
            return Optional.ofNullable(this.lastIP);
        }

        @Override public Optional<Tuple<WorldProperties, Vector3d>> getLastLocation() {
            Optional<Player> pl = Sponge.getServer().getPlayer(this.uuid);
            if (pl.isPresent()) {
                Location<World> l = pl.get().getLocation();
                return Optional.of(Tuple.of(
                    l.getExtent().getProperties(),
                    l.getPosition()
                ));
            }

            return Optional.empty();
        }
    }
}
