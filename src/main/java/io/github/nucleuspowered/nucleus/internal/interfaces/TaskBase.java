/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.interfaces;

import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.internal.Constants;
import io.github.nucleuspowered.nucleus.internal.annotations.EntryPoint;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.util.function.Consumer;

@EntryPoint
@NonnullByDefault
@Store(Constants.RUNNABLE)
public interface TaskBase extends Consumer<Task> {

    boolean isAsync();

    Duration interval();

}
