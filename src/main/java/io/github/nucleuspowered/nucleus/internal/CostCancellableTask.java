/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.interfaces.CancellableTask;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

public abstract class CostCancellableTask implements CancellableTask {

    private final INucleusServiceCollection serviceCollection;
    private final CommandSource target;
    private final double cost;
    private boolean hasRun = false;

    public CostCancellableTask(INucleusServiceCollection serviceCollection, CommandSource target, double cost) {
        this.serviceCollection = serviceCollection;
        this.target = target;
        this.cost = cost;
    }

    @Override
    public void onCancel() {
        if (!this.hasRun) {
            this.hasRun = true;
            if (this.target instanceof Player && this.cost > 0) {
                Task.builder()
                        .execute(task -> this.serviceCollection.economyServiceProvider().depositInPlayer((Player) target, cost))
                        .submit(this.serviceCollection.pluginContainer());
            }
        }
    }
}
