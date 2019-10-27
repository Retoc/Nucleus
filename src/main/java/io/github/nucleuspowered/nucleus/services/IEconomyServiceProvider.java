/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.services.impl.economy.EconomyServiceProvider;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.EconomyService;

import java.util.Optional;

@ImplementedBy(EconomyServiceProvider.class)
public interface IEconomyServiceProvider {

    boolean serviceExists();

    String getCurrencySymbol(double cost);

    boolean hasBalance(Player src, double balance);

    boolean withdrawFromPlayer(Player src, double cost);

    boolean withdrawFromPlayer(Player src, double cost, boolean message);

    boolean depositInPlayer(User src, double cost);

    boolean depositInPlayer(User src, double cost, boolean message);
}
