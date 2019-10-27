/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserPreferenceService;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.NucleusKeysProvider;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.PreferenceKeyImpl;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.UserPreferenceService;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@ImplementedBy(UserPreferenceService.class)
public interface IUserPreferenceService extends NucleusUserPreferenceService {

    CommandElement getElement();

    void postInit();

    void register(PreferenceKeyImpl<?> key);

    <T> void set(UUID uuid, PreferenceKey<T> key, @Nullable T value);

    <T> void set(UUID uuid, PreferenceKeyImpl<T> key, @Nullable T value);

    Map<PreferenceKey<?>, Object> get(User user);

    <T> Optional<T> get(UUID uuid, PreferenceKey<T> key);

    <T> T getUnwrapped(UUID uuid, PreferenceKey<T> key);

    @Override NucleusKeysProvider keys();

}
