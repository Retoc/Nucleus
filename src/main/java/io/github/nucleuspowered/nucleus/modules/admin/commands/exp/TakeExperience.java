/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.exp;

import io.github.nucleuspowered.nucleus.argumentparsers.ExperienceLevelArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.admin.AdminPermissions;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@Command(aliases = "take",
        parentCommand = ExperienceCommand.class,
        basePermission = AdminPermissions.BASE_EXP_TAKE,
        commandDescriptionKey = "exp.take")
@NonnullByDefault
public class TakeExperience implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_ONE_PLAYER,
                GenericArguments.firstParsing(
                    GenericArguments.onlyOne(new ExperienceLevelArgument(Text.of(ExperienceCommand.levelKey))),
                    GenericArguments.onlyOne(new PositiveIntegerArgument(Text.of(ExperienceCommand.experienceKey)))
                )
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Player pl = context.getPlayerFromArgs();
        Optional<ICommandResult> r = ExperienceCommand.checkGameMode(context, pl);
        if (r.isPresent()) {
            return r.get();
        }

        // int currentExp = pl.get(Keys.TOTAL_EXPERIENCE).get();
        int toOffer = 0;
        if (context.hasAny(ExperienceCommand.levelKey)) {
            ExperienceHolderData data = pl.get(ExperienceHolderData.class).get();
            int currentLevel = data.level().get();
            int levelReduction = context.requireOne(ExperienceCommand.levelKey, int.class);

            // If this will take us down to below zero, we just let this continue to the return line. Else...
            if (currentLevel >= levelReduction) {
                int extra = data.experienceSinceLevel().get();
                data.set(data.level().set(currentLevel - levelReduction));
                data.set(data.experienceSinceLevel().set(Math.min(extra, data.getExperienceBetweenLevels().getMaxValue())));
                return ExperienceCommand.tellUserAboutExperience(context, pl, pl.offer(data).isSuccessful());
            }
        } else {
            toOffer = pl.get(Keys.TOTAL_EXPERIENCE).get() - context.requireOne(ExperienceCommand.experienceKey, int.class);
        }

        return ExperienceCommand.tellUserAboutExperience(context, pl, pl.offer(Keys.TOTAL_EXPERIENCE, Math.max(0, toOffer)).isSuccessful());
    }
}
