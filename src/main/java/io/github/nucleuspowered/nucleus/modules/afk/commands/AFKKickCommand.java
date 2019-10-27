/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.commands;

import io.github.nucleuspowered.nucleus.internal.TypeTokens;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.afk.AFKPermissions;
import io.github.nucleuspowered.nucleus.modules.afk.services.AFKHandler;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.Optional;

@Command(aliases = {"afkkick", "kickafk"}, basePermission = AFKPermissions.BASE_AFKKICK, commandDescriptionKey = "afkkick")
public class AFKKickCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Optional<Text> reason = context
                .getOne(NucleusParameters.Keys.REASON, TypeTokens.STRING)
                .map(TextSerializers.FORMATTING_CODE::deserialize);

        Collection<Player> playersToKick = context.getServiceCollection().getServiceUnchecked(AFKHandler.class).getAfk(x ->
                !context.testPermissionFor(x, AFKPermissions.AFK_EXEMPT_KICK));
        if (playersToKick.isEmpty()) {
            return context.errorResult("command.afkkick.nokick");
        }

        IMessageProviderService messageProviderService = context.getServiceCollection().messageProvider();
        int number = playersToKick.size();
        playersToKick.forEach(x -> x.kick(reason.orElseGet(() -> messageProviderService.getMessageFor(x.getLocale(), "afk.kickreason"))));

        context.sendMessage("command.afkkick.success", number);
        return context.successResult();
    }
}
