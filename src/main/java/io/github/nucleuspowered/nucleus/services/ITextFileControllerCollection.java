/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.io.TextFileController;
import io.github.nucleuspowered.nucleus.services.impl.textfilecontroller.TextFileControllerCollection;

import java.util.Optional;

@ImplementedBy(TextFileControllerCollection.class)
public interface ITextFileControllerCollection {

    Optional<TextFileController> get(String key);

    void register(String key, TextFileController controller);

    void remove(String key);

}
