/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.modules.environment.EnvironmentPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

@NonnullByDefault
@EssentialsEquivalent(value = {"time"}, isExact = false, notes = "This just displays the time. Use '/time set' to set the time.")
@Command(
        aliases = {"time"},
        basePermission = EnvironmentPermissions.BASE_TIME,
        commandDescriptionKey = "time",
        modifiers = {
            @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = EnvironmentPermissions.EXEMPT_COOLDOWN_TIME),
            @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission =  EnvironmentPermissions.EXEMPT_WARMUP_TIME),
            @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = EnvironmentPermissions.EXEMPT_COST_TIME)
        }
)
public class TimeCommand implements ICommandExecutor<CommandSource> {

    private final String world = "world";

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] { GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.world(Text.of(this.world)))) };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) {
        WorldProperties pr = context.getWorldPropertiesOrFromSelf(this.world).orElseGet(
                () -> Sponge.getServer().getDefaultWorld().get()
        );

        context.sendMessage("command.time", pr.getWorldName(), String.valueOf(Util.getTimeFromTicks(pr.getWorldTime())));
        return context.successResult();
    }
}
