/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.textfilecontroller;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.io.TextFileController;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.ITextFileControllerCollection;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class TextFileControllerCollection implements ITextFileControllerCollection, Reloadable {

    private final Map<String, TextFileController> textFileControllers = Maps.newHashMap();

    public TextFileControllerCollection(INucleusServiceCollection serviceCollection) {
        serviceCollection.reloadableService().registerReloadable(this);
    }

    @Override public Optional<TextFileController> get(String key) {
        return Optional.ofNullable(this.textFileControllers.get(key));
    }

    @Override public void register(String key, TextFileController controller) {
        this.textFileControllers.put(key, controller);
    }

    @Override public void remove(String key) {
        this.textFileControllers.remove(key);
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        for (TextFileController textFileController : this.textFileControllers.values()) {
            try {
                textFileController.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
