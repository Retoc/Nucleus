/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message;

import io.github.nucleuspowered.nucleus.services.impl.userprefs.NucleusKeysProvider;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.PreferenceKeyImpl;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.PreferenceKeyImpl.BooleanKey;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.UserPrefKeys;

public class MessageUserPrefKeys implements UserPrefKeys {

    public static final PreferenceKeyImpl<Boolean> SOCIAL_SPY = new BooleanKey(
            MessagePermissions.BASE_SOCIALSPY,
            true,
            ((serviceCollection, user) -> serviceCollection.permissionService().hasPermission(user, MessagePermissions.BASE_SOCIALSPY)
                    && !serviceCollection.permissionService().hasPermission(user, MessagePermissions.SOCIALSPY_FORCE)),
            "userpref.socialspy"
    );

    public static final PreferenceKeyImpl<Boolean> RECEIVING_MESSAGES = new BooleanKey(
            NucleusKeysProvider.MESSAGE_TOGGLE_KEY,
            true,
            MessagePermissions.MSGTOGGLE_BYPASS,
            "userpref.messagetoggle"
    );

}
