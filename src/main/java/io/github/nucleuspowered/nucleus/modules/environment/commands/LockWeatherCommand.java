/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.environment.EnvironmentKeys;
import io.github.nucleuspowered.nucleus.modules.environment.EnvironmentPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.storage.dataobjects.keyed.IKeyedDataObject;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

@NonnullByDefault
@Command(
        aliases = {"lockweather", "killweather" },
        basePermission = EnvironmentPermissions.BASE_LOCKWEATHER,
        commandDescriptionKey = "lockweather",
        async = true
)
public class LockWeatherCommand implements ICommandExecutor<CommandSource> {

    private final String worldKey = "world";

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.world(Text.of(this.worldKey)))),
                NucleusParameters.OPTIONAL_ONE_TRUE_FALSE
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Optional<WorldProperties> world = context.getWorldPropertiesOrFromSelf(this.worldKey);
        if (!world.isPresent()) {
            return context.errorResult("command.specifyworld");
        }

        WorldProperties wp = world.get();
        try (IKeyedDataObject.Value<Boolean> vb = context.getServiceCollection().storageManager()
                .getWorldOnThread(wp.getUniqueId())
                .orElseThrow(() -> context.createException("command.noworld", wp.getWorldName()))
                .getAndSet(EnvironmentKeys.LOCKED_WEATHER)) {
            boolean current = vb.getValue().orElse(false);
            boolean toggle = context.getOne(NucleusParameters.Keys.BOOL, Boolean.class).orElse(!current);
            vb.setValue(toggle);
            if (toggle) {
                context.errorResult("command.lockweather.locked", wp.getWorldName());
            } else {
                context.errorResult("command.lockweather.unlocked", wp.getWorldName());
            }
        }

        return context.successResult();
    }
}
