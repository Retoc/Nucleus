/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.userprefs;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserPreferenceService;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.storage.dataobjects.keyed.DataKeyImpl;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

public class PreferenceKeyImpl<T> extends DataKeyImpl<T, IUserDataObject> implements NucleusUserPreferenceService.PreferenceKey<T> {

    private final String key;
    @Nullable private final T def;
    private final Class<T> clazz;
    private final BiPredicate<INucleusServiceCollection, User> canAccess;
    private final String descriptionKey;

    PreferenceKeyImpl(
            String key,
            @Nullable T def,
            Class<T> clazz,
            String permission,
            String descriptionKey) {
        this(key,
                def,
                clazz,
                (serviceCollection, user) -> serviceCollection.permissionService().hasPermission(user, permission),
                descriptionKey);
    }

    PreferenceKeyImpl(
            String key,
            @Nullable T def,
            Class<T> clazz,
            BiPredicate<INucleusServiceCollection, User> canAccess,
            String descriptionKey) {
        super(new String[] { "user-prefs", key }, TypeToken.of(clazz), IUserDataObject.class, def);
        this.key = key;
        this.def = def;
        this.clazz = clazz;
        this.canAccess = canAccess;
        this.descriptionKey = descriptionKey;
    }

    public String getID() {
        return this.key;
    }

    public Optional<T> getDefaultValue() {
        return Optional.ofNullable(this.def);
    }

    public Class<T> getValueClass() {
        return this.clazz;
    }

    public boolean canAccess(INucleusServiceCollection serviceCollection, User user) {
        return this.canAccess.test(serviceCollection, user);
    }

    public String getDescription(IMessageProviderService messageProviderService) {
        return messageProviderService.getMessageString(this.descriptionKey);
    }

    public String getDescriptionKey() {
        return this.descriptionKey;
    }

    public static class BooleanKey extends PreferenceKeyImpl<Boolean> {

        public BooleanKey(String key, @Nullable Boolean def, String permission, String descriptionKey) {
            super(key, def, Boolean.class, permission, descriptionKey);
        }

        public BooleanKey(String key, @Nullable Boolean def, BiPredicate<INucleusServiceCollection, User> canAccess, String descriptionKey) {
            super(key, def, Boolean.class, canAccess, descriptionKey);
        }
    }
}
