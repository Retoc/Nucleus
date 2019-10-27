/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.command;

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

@NonnullByDefault
@Command(
        aliases = { "add", "+" },
        basePermission = KitPermissions.BASE_KIT_COMMAND_ADD,
        commandDescriptionKey = "kit.command.add",
        async = true,
        parentCommand = KitCommandCommand.class
)
public class KitAddCommandCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(false),
                NucleusParameters.COMMAND
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Kit kitInfo = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);
        String c = context.requireOne(NucleusParameters.Keys.COMMAND, String.class)
                .replace(" {player} ", " {{player}} ");
        kitInfo.addCommand(c);
        context.getServiceCollection().getServiceUnchecked(KitHandler.class).saveKit(kitInfo);

        context.sendMessage("command.kit.command.add.command", c, kitInfo.getName());
        return context.successResult();
    }
}
