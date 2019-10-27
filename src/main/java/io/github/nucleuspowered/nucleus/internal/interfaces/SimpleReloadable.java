/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.interfaces;

import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;

@Deprecated
@FunctionalInterface
public interface SimpleReloadable extends Reloadable {

    @Override default void onReload(INucleusServiceCollection serviceCollection) {
        try {
            onReload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void onReload() throws Exception;
}
