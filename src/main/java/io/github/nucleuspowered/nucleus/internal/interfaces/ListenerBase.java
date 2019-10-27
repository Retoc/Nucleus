/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.interfaces;

import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.internal.Constants;
import io.github.nucleuspowered.nucleus.internal.annotations.EntryPoint;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;

@EntryPoint
@Store(Constants.LISTENER)
public interface ListenerBase {

    @EntryPoint
    @Store(Constants.LISTENER)
    interface Conditional extends ListenerBase {

        boolean shouldEnable(INucleusServiceCollection serviceCollection);
    }

}
