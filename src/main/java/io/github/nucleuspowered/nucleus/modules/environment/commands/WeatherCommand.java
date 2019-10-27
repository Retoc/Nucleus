/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.WeatherArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.environment.EnvironmentKeys;
import io.github.nucleuspowered.nucleus.modules.environment.EnvironmentPermissions;
import io.github.nucleuspowered.nucleus.modules.environment.config.EnvironmentConfig;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.weather.Weather;

import java.util.Optional;

@NonnullByDefault
@EssentialsEquivalent({"thunder", "sun", "weather", "sky", "storm", "rain"})
@Command(
        aliases = {"weather"},
        basePermission = EnvironmentPermissions.BASE_WEATHER,
        commandDescriptionKey = "weather",
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = EnvironmentPermissions.EXEMPT_COOLDOWN_WEATHER),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission =  EnvironmentPermissions.EXEMPT_WARMUP_WEATHER),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = EnvironmentPermissions.EXEMPT_COST_WEATHER)
        }
)
public class WeatherCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private final String weather = "weather";

    private long max = Long.MAX_VALUE;

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        this.max = serviceCollection.moduleDataProvider().getModuleConfig(EnvironmentConfig.class).getMaximumWeatherTimespan();
    }

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[]{
                NucleusParameters.OPTIONAL_WEAK_WORLD_PROPERTIES_ENABLED_ONLY,
                GenericArguments.onlyOne(new WeatherArgument(Text.of(this.weather))), // More flexible with the arguments we can use.
                NucleusParameters.OPTIONAL_DURATION
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        // We can predict the weather on multiple worlds now!
        WorldProperties wp = context.getWorldPropertiesOrFromSelf(NucleusParameters.Keys.WORLD)
                .orElseGet(
                        () -> Sponge.getServer().getDefaultWorld().get()
                );
        World w = Sponge.getServer().getWorld(wp.getUniqueId())
            .orElseThrow(() -> context.createException("args.worldproperties.notloaded", wp.getWorldName()));

        // Get whether we locked the weather.
        if (context.getServiceCollection().storageManager().getWorldOnThread(w.getUniqueId())
                .map(x -> x.get(EnvironmentKeys.LOCKED_WEATHER).orElse(false)).orElse(false)) {
            // Tell the user to unlock first.
            return context.errorResult("command.weather.locked", w.getName());
        }

        // Houston, we have a world! Now, what was the forecast?
        Weather we = context.requireOne(this.weather, Weather.class);

        // Have we gotten an accurate forecast? Do we know how long this weather spell will go on for?
        Optional<Long> oi = context.getOne(NucleusParameters.Keys.DURATION, Long.class);

        // Even weather masters have their limits. Sometimes.
        if (this.max > 0 && oi.orElse(Long.MAX_VALUE) > this.max && !context.testPermission(EnvironmentPermissions.WEATHER_EXEMPT_LENGTH)) {
            return context.errorResult("command.weather.toolong", context.getTimeString(this.max));
        }

        if (oi.isPresent()) {
            // YES! I should get a job at the weather service and show them how it's done!
            Task.builder().execute(() -> w.setWeather(we, oi.get() * 20L)).submit(Nucleus.getNucleus());
            context.sendMessage("command.weather.time", we.getName(), w.getName(), context.getTimeString(oi.get()));
        } else {
            // No, probably because I've already gotten a job at the weather service...
            Task.builder().execute(() -> w.setWeather(we)).submit(Nucleus.getNucleus());
            context.sendMessage("command.weather.set", we.getName(), w.getName());
        }

        // The weather control device has been activated!
        return context.successResult();
    }


}
