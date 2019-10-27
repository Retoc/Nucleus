/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish;

import io.github.nucleuspowered.nucleus.services.impl.userprefs.NucleusKeysProvider;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.PreferenceKeyImpl;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.UserPrefKeys;

public class VanishUserPrefKeys implements UserPrefKeys {

    // String key, @Nullable Boolean def, String permission, String descriptionKey
    public static final PreferenceKeyImpl<Boolean> VANISH_ON_LOGIN = new PreferenceKeyImpl.BooleanKey(
            NucleusKeysProvider.VANISH_ON_LOGIN_KEY,
            false,
            VanishPermissions.VANISH_ONLOGIN,
            "userpref.vanishonlogin"
    );
}
