package io.github.nucleuspowered.nucleus.modules.admin.commands.gamemode;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.modules.admin.AdminPermissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@EssentialsEquivalent({"gma", "adventure"})
@Command(
        aliases = {"gma", "adventure"},
        parentCommand = GamemodeCommand.class,
        basePermission = {AdminPermissions.BASE_GAMEMODE, AdminPermissions.GAMEMODE_MODES_ADVENTURE},
        commandDescriptionKey = "gma",
        modifiers =
                {
                        @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = AdminPermissions.EXEMPT_WARMUP_GAMEMODE,
                                useFrom = GamemodeCommand.class),
                        @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = AdminPermissions.EXEMPT_COOLDOWN_GAMEMODE,
                                useFrom = GamemodeCommand.class),
                        @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = AdminPermissions.EXEMPT_COST_GAMEMODE,
                                useFrom = GamemodeCommand.class)
                }
)
public class AdventureGamemodeCommand extends GamemodeBase<Player> {

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> src) throws CommandException {
        return baseCommand(src, src.getIfPlayer(), GameModes.ADVENTURE);
    }
}
