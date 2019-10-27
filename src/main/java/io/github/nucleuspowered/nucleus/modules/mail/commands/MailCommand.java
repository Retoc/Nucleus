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
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.mail.MailPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.modules.mail.services.MailHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@EssentialsEquivalent({"mail", "email"})
@NonnullByDefault
@Command(
        aliases = { "mail", "email" },
        basePermission = MailPermissions.BASE_MAIL,
        commandDescriptionKey = "mail",
        async = true
)
public class MailCommand implements ICommandExecutor<Player> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.optional(
                        GenericArguments.allOf(
                                new MailFilterArgument(Text.of(MailReadBase.FILTERS), serviceCollection.getServiceUnchecked(MailHandler.class))
                        )
                )
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        return MailReadBase.INSTANCE.executeCommand(
                context,
                context.getIfPlayer(),
                context.getAll(MailReadBase.FILTERS, NucleusMailService.MailFilter.class));
    }
}
