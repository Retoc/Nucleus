/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kick.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.kick.KickPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@EssentialsEquivalent("kick")
@NonnullByDefault
@Command(aliases = "kick", basePermission = KickPermissions.BASE_KICK, commandDescriptionKey = "kick")
public class KickCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.ONE_PLAYER,
                NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Player pl = context.requireOne(NucleusParameters.Keys.PLAYER, Player.class);
        String r = context.getOne(NucleusParameters.Keys.REASON, String.class)
                .orElseGet(() -> context.getMessageString("command.kick.defaultreason"));

        if (context.isConsoleAndBypass() || context.testPermissionFor(pl, KickPermissions.KICK_EXEMPT_TARGET)) {
            return context.errorResult("command.kick.exempt", pl.getName());
        }

        pl.kick(TextSerializers.FORMATTING_CODE.deserialize(r));

        MessageChannel messageChannel = context.getServiceCollection().permissionService().permissionMessageChannel(KickPermissions.KICK_NOTIFY);
        messageChannel.send(context.getCommandSource(), context.getMessage("command.kick.message", pl.getName(), context.getName()));
        messageChannel.send(context.getCommandSource(), context.getMessage("command.reason", r));
        return context.successResult();
    }
}
