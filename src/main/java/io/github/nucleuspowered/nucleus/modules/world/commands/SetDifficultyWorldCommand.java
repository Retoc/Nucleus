/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedCatalogTypeArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.world.WorldPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.WorldProperties;

@NonnullByDefault
@Command(
        aliases = {"setdifficulty", "difficulty"},
        basePermission = WorldPermissions.BASE_WORLD_SETDIFFICULTY,
        commandDescriptionKey = "world.setdifficulty",
        parentCommand = WorldCommand.class
)
public class SetDifficultyWorldCommand implements ICommandExecutor<CommandSource> {

    private final String difficulty = "difficulty";

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.onlyOne(new ImprovedCatalogTypeArgument(Text.of(this.difficulty), CatalogTypes.DIFFICULTY)),
                NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ALL
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Difficulty difficultyInput = context.requireOne(this.difficulty, Difficulty.class);
        WorldProperties worldProperties = context.getWorldPropertiesOrFromSelf(NucleusParameters.Keys.WORLD)
                        .orElseThrow(() -> context.createException("command.world.player"));

        worldProperties.setDifficulty(difficultyInput);
        context.sendMessage("command.world.setdifficulty.success",
                worldProperties.getWorldName(),
                Util.getTranslatableIfPresent(difficultyInput));

        return context.successResult();
    }
}
