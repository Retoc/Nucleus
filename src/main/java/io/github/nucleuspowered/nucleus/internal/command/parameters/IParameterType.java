/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.parameters;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

public interface IParameterType<R> {

    @Nullable
    R parse(IArgumentReader reader, ICommandContext<?> context);

    List<String> complete(IArgumentReader reader, ICommandContext<?> context);

}
