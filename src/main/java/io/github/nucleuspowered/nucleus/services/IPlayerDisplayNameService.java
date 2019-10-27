/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.playername.PlayerDisplayNameService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

@ImplementedBy(PlayerDisplayNameService.class)
public interface IPlayerDisplayNameService {

    void provideDisplayNameResolver(DisplayNameResolver resolver);

    void provideDisplayNameQuery(DisplayNameQuery resolver);

    Optional<User> getUser(Text displayName);

    Text getDisplayName(UUID playerUUID);

    default Text getDisplayName(Player player) {
        return getDisplayName(player.getUniqueId());
    }

    default Text getDisplayName(User user) {
        return getDisplayName(user.getUniqueId());
    }

    Text getDisplayName(CommandSource source);

    Text getName(CommandSource user);

    Text addCommandToName(CommandSource p);

    Text addCommandToDisplayName(CommandSource p);

    @FunctionalInterface
    interface DisplayNameResolver {

        Optional<Text> resolve(UUID userUUID);

    }

    @FunctionalInterface
    interface DisplayNameQuery {

        Optional<User> resolve(Text name);

    }

}
