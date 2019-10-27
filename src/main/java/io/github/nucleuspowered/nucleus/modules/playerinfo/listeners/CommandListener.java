/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.PlayerInfoConfig;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;

import javax.inject.Inject;

public class CommandListener implements ListenerBase.Conditional {

    private final IMessageProviderService messageProviderService;
    private boolean messageShown = false;

    @Inject
    public CommandListener(INucleusServiceCollection serviceCollection) {
        this.messageProviderService = serviceCollection.messageProvider();
    }

    @Listener
    public void onCommandPreProcess(SendCommandEvent event, @Root ConsoleSource source, @Getter("getCommand") String command) {
        if (command.equalsIgnoreCase("list")) {
            event.setCommand("minecraft:list");
            if (!this.messageShown) {
                this.messageShown = true;
                Sponge.getScheduler().createSyncExecutor(Nucleus.getNucleus()).submit(() ->
                        this.messageProviderService.sendMessageTo(source, "list.listener.multicraftcompat"));
            }
        }
    }

    @Override public boolean shouldEnable(INucleusServiceCollection serviceCollection) {
        return serviceCollection.moduleDataProvider().getModuleConfig(PlayerInfoConfig.class).getList().isPanelCompatibility();
    }

}
