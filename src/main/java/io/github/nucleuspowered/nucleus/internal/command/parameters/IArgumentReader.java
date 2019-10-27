/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.parameters;

import java.util.Optional;

public interface IArgumentReader {

    String next();

    Optional<String> nextIfPresent();

    String toEnd();

    String all();

}
