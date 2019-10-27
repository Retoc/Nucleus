/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.admin.AdminPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory.NucleusTextTemplateMessageSender;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Command(aliases = {"tellplain", "plaintell", "ptell"},
        basePermission = AdminPermissions.BASE_TELLPLAIN,
        commandDescriptionKey = "tellplain")
@NonnullByDefault
public class TellPlainCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.MANY_PLAYER_OR_CONSOLE,
                NucleusParameters.MESSAGE
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) {
        try {
            new NucleusTextTemplateMessageSender(
                    context.getServiceCollection().textTemplateFactory(),
                    context.getServiceCollection().textTemplateFactory().createFromString(
                        context.requireOne(NucleusParameters.Keys.MESSAGE, String.class)),
                    context.getServiceCollection().messageTokenService(),
                    context.getCommandSource())
                    .send(context.getAll(NucleusParameters.Keys.PLAYER_OR_CONSOLE, CommandSource.class), context.getCause());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return context.errorResult("command.tellplain.failed");
        }
        return context.successResult();
    }
}
