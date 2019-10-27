/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.cooldown.CooldownService;
import org.spongepowered.api.util.Identifiable;

import java.time.Duration;
import java.util.Optional;

/**
 * A service that contains information about cooldowns.
 */
@ImplementedBy(CooldownService.class)
public interface ICooldownService {

    /**
     * Gets whether the target {@link Identifiable} has a cooldown associated
     * with the supplied key.
     *
     * @param key The key for the cooldown
     * @param identifiable The {@link Identifiable}
     * @return true if so
     */
    boolean hasCooldown(String key, Identifiable identifiable);

    /**
     * Gets the time left on a cooldown, if any.
     *
     * @param key The key for the cooldown
     * @param identifiable The {@link Identifiable}
     * @return The {@link Duration} of the cooldown, if any.
     */
    Optional<Duration> getCooldown(String key, Identifiable identifiable);

    /**
     * Sets a cooldown.
     *
     * @param key The key for the cooldown
     * @param identifiable The {@link Identifiable} that the cooldown is against.
     * @param cooldownLength The length of the cooldown.
     */
    void setCooldown(String key, Identifiable identifiable, Duration cooldownLength);

    /**
     * Clears a cooldown.
     *
     * @param key The cooldown key
     * @param identifiable The {@link Identifiable} to clear the key for.
     */
    void clearCooldown(String key, Identifiable identifiable);

}
