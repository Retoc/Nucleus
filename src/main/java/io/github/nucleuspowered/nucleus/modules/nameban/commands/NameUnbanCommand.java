/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nameban.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.RegexArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.nameban.NameBanPermissions;
import io.github.nucleuspowered.nucleus.modules.nameban.services.NameBanHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = {"nameunban", "namepardon"},
        basePermission = NameBanPermissions.BASE_NAMEUNBAN,
        commandDescriptionKey = "nameunban",
        async = true)
public class NameUnbanCommand implements ICommandExecutor<CommandSource> {

    private final String nameKey = "name";

    @Override public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
            new RegexArgument(Text.of(this.nameKey), Util.usernameRegexPattern, "command.nameban.notvalid")
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        String name = context.requireOne(this.nameKey, String.class).toLowerCase();

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(context.getCommandSource());
            if (context.getServiceCollection().getServiceUnchecked(NameBanHandler.class).removeName(name, frame.getCurrentCause())) {
                context.sendMessage("command.nameban.pardon.success", name);
                return context.successResult();
            }
        }

        return context.errorResult("command.nameban.pardon.failed", name);
    }
}
