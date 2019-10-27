/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.interfaces;

import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;

public interface Reloadable {

    void onReload(INucleusServiceCollection serviceCollection);

}
