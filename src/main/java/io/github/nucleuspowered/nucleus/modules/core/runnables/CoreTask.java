/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.runnables;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.interfaces.TaskBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Core tasks. No module, must always run.
 */
@NonnullByDefault
public class CoreTask implements TaskBase, Reloadable {

    private boolean printSave = false;
    private final INucleusServiceCollection serviceCollection;

    @Inject
    public CoreTask(INucleusServiceCollection serviceCollection) {
        this.serviceCollection = serviceCollection;
    }


    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(5, ChronoUnit.MINUTES);
    }

    @Override
    public void accept(Task task) {
        Nucleus plugin = Nucleus.getNucleus();
        this.serviceCollection.storageManager().getUserService().clearCache();

        if (this.printSave) {
            plugin.getLogger().info(this.serviceCollection.messageProvider().getMessageString("core.savetask.starting"));
        }

        plugin.saveData();

        if (this.printSave) {
            plugin.getLogger().info(this.serviceCollection.messageProvider().getMessageString("core.savetask.complete"));
        }
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.printSave = serviceCollection.moduleDataProvider().getModuleConfig(CoreConfig.class).isPrintOnAutosave();
    }

}
