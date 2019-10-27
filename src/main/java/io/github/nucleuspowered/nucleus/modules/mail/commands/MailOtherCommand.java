/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.commands;

import io.github.nucleuspowered.nucleus.api.service.NucleusMailService;
import io.github.nucleuspowered.nucleus.argumentparsers.MailFilterArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.mail.MailPermissions;
import io.github.nucleuspowered.nucleus.modules.mail.services.MailHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = { "other", "o" },
        basePermission = MailPermissions.BASE_MAIL_OTHER,
        commandDescriptionKey = "mail.other",
        async = true,
        parentCommand = MailCommand.class
)
public class MailOtherCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                GenericArguments.optional(GenericArguments.allOf(new MailFilterArgument(Text.of(MailReadBase.FILTERS), serviceCollection.getServiceUnchecked(MailHandler.class))))
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        return MailReadBase.INSTANCE.executeCommand(
                context,
                context.requireOne(NucleusParameters.Keys.USER, User.class),
                context.getAll(MailReadBase.FILTERS, NucleusMailService.MailFilter.class));
    }
}
