/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.command;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
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
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

@NonnullByDefault
@Command(
        aliases = { "remove", "del", "-" },
        basePermission = KitPermissions.BASE_KIT_COMMAND_REMOVE,
        commandDescriptionKey = "kit.command.remove",
        async = true,
        parentCommand = KitCommandCommand.class
)
public class KitRemoveCommandCommand implements ICommandExecutor<CommandSource> {

    private final String index = "index";

    @Override public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(false),
                GenericArguments.firstParsing(new PositiveIntegerArgument(Text.of(this.index)), NucleusParameters.COMMAND)
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Kit kitInfo = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);
        List<String> commands = kitInfo.getCommands();

        String cmd;
        if (context.hasAny(this.index)) {
            int idx = context.requireOne(this.index, Integer.class);
            if (idx == 0) {
                return context.errorResult("command.kit.command.remove.onebased");
            }

            if (idx > commands.size()) {
                return context.errorResult("command.kit.command.remove.overidx", commands.size(), kitInfo.getName());
            }

            cmd = commands.remove(idx - 1);
        } else {
            cmd = context.requireOne(NucleusParameters.Keys.COMMAND, String.class).replace(" {player} ", " {{player}} ");
            if (!commands.remove(cmd)) {
                return context.errorResult("command.kit.command.remove.noexist", cmd, kitInfo.getName());
            }
        }

        kitInfo.setCommands(commands);
        context.getServiceCollection().getServiceUnchecked(KitHandler.class).saveKit(kitInfo);
        context.sendMessage("command.kit.command.remove.success", cmd, kitInfo.getName());
        return context.successResult();
    }
}
