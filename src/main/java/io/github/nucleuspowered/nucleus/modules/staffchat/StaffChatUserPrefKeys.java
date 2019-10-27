/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat;

import io.github.nucleuspowered.nucleus.services.impl.userprefs.NucleusKeysProvider;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.PreferenceKeyImpl;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.UserPrefKeys;

public class StaffChatUserPrefKeys implements UserPrefKeys {

    // String key, @Nullable Boolean def, String permission, String descriptionKey
    public static final PreferenceKeyImpl<Boolean> VIEW_STAFF_CHAT = new PreferenceKeyImpl.BooleanKey(
            NucleusKeysProvider.VIEW_STAFF_CHAT_KEY,
            true,
            StaffChatPermissions.BASE_STAFFCHAT,
            "userpref.viewstaffchat"
    );

}
