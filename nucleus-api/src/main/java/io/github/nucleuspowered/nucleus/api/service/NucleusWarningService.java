/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Warning;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Warnings in Nucleus are no longer available. This service allows you to
 * get the legacy data for a user.
 */
public interface NucleusWarningService {

    /**
     * Gets all warnings (active and expired) for a {@link User}
     *
     * @param user The {@link User} to get the warnings for.
     * @return The {@link Warning}s.
     */
    CompletableFuture<List<Warning>> getWarnings(User user);
}
