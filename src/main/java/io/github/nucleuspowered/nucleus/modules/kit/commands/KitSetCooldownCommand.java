/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;

@NonnullByDefault
@Command(
        aliases = { "setcooldown", "setinterval" },
        basePermission = KitPermissions.BASE_KIT_SETCOOLDOWN,
        commandDescriptionKey = "kit.setcooldown",
        parentCommand = KitCommand.class,
        async = true
)
public class KitSetCooldownCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(false),
                NucleusParameters.DURATION
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Kit kitInfo = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);
        long seconds = context.requireOne(NucleusParameters.Keys.DURATION, Long.class);

        kitInfo.setCooldown(Duration.ofSeconds(seconds));
        context.getServiceCollection().getServiceUnchecked(KitHandler.class).saveKit(kitInfo);
        context.sendMessage("command.kit.setcooldown.success", kitInfo.getName(), context.getTimeString(seconds));
        return context.successResult();
    }
}
