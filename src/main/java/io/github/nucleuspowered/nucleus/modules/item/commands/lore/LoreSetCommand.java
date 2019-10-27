/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.lore;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.modules.item.ItemPermissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.entity.living.player.Player;

@Command(
        aliases = { "set" },
        basePermission = ItemPermissions.BASE_LORE_SET,
        commandDescriptionKey = "lore.set",
        parentCommand = LoreCommand.class,
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = ItemPermissions.EXEMPT_COOLDOWN_LORE_SET),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = ItemPermissions.EXEMPT_WARMUP_LORE_SET),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = ItemPermissions.EXEMPT_COST_LORE_SET)
        }
)
public class LoreSetCommand extends LoreSetBaseCommand {

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        return setLore(context, context.requireOne(NucleusParameters.Keys.LORE, String.class), true);
    }
}
