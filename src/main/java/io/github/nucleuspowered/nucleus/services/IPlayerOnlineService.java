/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.playeronline.PlayerOnlineService;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;

import java.time.Instant;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@ImplementedBy(PlayerOnlineService.class)
public interface IPlayerOnlineService {

    boolean isOnline(CommandSource src, User player);

    default Optional<Instant> lastSeen(CommandSource src, User player) {
        return player.get(Keys.LAST_DATE_PLAYED);
    }

    void set(@Nullable BiPredicate<CommandSource, User> isOnline, @Nullable BiFunction<CommandSource, User, Optional<Instant>> lastSeen);

    void reset();

}
