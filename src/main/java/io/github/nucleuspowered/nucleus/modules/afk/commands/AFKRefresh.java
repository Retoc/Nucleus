/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.afk.AFKPermissions;
import io.github.nucleuspowered.nucleus.modules.afk.services.AFKHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Command(aliases = "afkrefresh", commandDescriptionKey = "afkrefresh", basePermission = AFKPermissions.BASE_AFKREFRESH)
@NonnullByDefault
public class AFKRefresh implements ICommandExecutor<CommandSource> {

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) {
        context.getServiceCollection().getServiceUnchecked(AFKHandler.class).invalidateAfkCache();
        context.sendMessage("command.afkrefresh.complete");
        return context.successResult();
    }
}
