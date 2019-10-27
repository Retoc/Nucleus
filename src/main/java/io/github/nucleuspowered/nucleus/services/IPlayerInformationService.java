/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.playerinformation.PlayerInformationService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Optional;

@ImplementedBy(PlayerInformationService.class)
public interface IPlayerInformationService {

    void registerProvider(Provider provider);

    Collection<Provider> getProviders();

    @FunctionalInterface
    interface Provider {

        Optional<Text> get(User user, CommandSource source, INucleusServiceCollection serviceCollection);

    }
}
