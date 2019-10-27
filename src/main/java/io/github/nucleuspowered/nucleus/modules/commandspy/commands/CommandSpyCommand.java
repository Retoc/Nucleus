/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandspy.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.commandspy.CommandSpyPermissions;
import io.github.nucleuspowered.nucleus.modules.commandspy.CommandSpyUserPrefKeys;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IUserPreferenceService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.UUID;

@Command(
        aliases = "commandspy",
        basePermission = CommandSpyPermissions.BASE_COMMANDSPY,
        commandDescriptionKey = "commandspy"
)
@NonnullByDefault
public class CommandSpyCommand implements ICommandExecutor<Player> {

    @Override public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_ONE_TRUE_FALSE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        IUserPreferenceService userPreferenceService = context.getServiceCollection().userPreferenceService();
        UUID uuid = context.getUniqueId().orElseThrow(() -> new CommandException(Text.of("No UUID was found")));
        boolean to =
                context.getOne(NucleusParameters.Keys.BOOL, Boolean.class)
                    .orElseGet(() -> !userPreferenceService.getUnwrapped(
                            uuid,
                            CommandSpyUserPrefKeys.COMMAND_SPY));
        userPreferenceService.set(uuid, CommandSpyUserPrefKeys.COMMAND_SPY, to);
        // "loc:" indicates to the engine that the text in the key is localisable
        context.sendMessage("command.commandspy.success", to ? "loc:standard.enabled" : "loc:standard.disabled");

        return context.successResult();
    }
}
