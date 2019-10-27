/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.modules.warp.WarpPermissions;
import io.github.nucleuspowered.nucleus.modules.warp.event.CreateWarpEvent;
import io.github.nucleuspowered.nucleus.modules.warp.services.WarpService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.regex.Pattern;

@EssentialsEquivalent({"setwarp", "createwarp"})
@NonnullByDefault
@Command(
        aliases = {"set", "#setwarp", "#warpset"},
        basePermission = WarpPermissions.BASE_WARP_SET,
        commandDescriptionKey = "warp.set",
        modifiers = {
                @CommandModifier(
                        value = CommandModifiers.HAS_WARMUP,
                        exemptPermission = WarpPermissions.EXEMPT_WARMUP_WARP_SET
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COOLDOWN,
                        exemptPermission = WarpPermissions.EXEMPT_COOLDOWN_WARP_SET
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COST,
                        exemptPermission = WarpPermissions.EXEMPT_COST_WARP_SET
                )
        }
)
public class SetWarpCommand implements ICommandExecutor<Player> {

//    private final WarpService qs = getServiceUnchecked(WarpService.class);
    private final Pattern warpRegex = Pattern.compile("^[A-Za-z][A-Za-z0-9]{0,25}$");

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments.string(Text.of(WarpService.WARP_KEY)))
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        String warp = context.requireOne(WarpService.WARP_KEY, String.class);

        // Needs to match the name...
        if (!this.warpRegex.matcher(warp).matches()) {
            return context.errorResult("command.warps.invalidname");
        }

        WarpService warpService = context.getServiceCollection().getServiceUnchecked(WarpService.class);

        // Get the service, does the warp exist?
        if (warpService.getWarp(warp).isPresent()) {
            // You have to delete to set the same name
            return context.errorResult("command.warps.nooverwrite");
        }

        Player src = context.getIfPlayer();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(src);
            CreateWarpEvent event = new CreateWarpEvent(frame.getCurrentCause(), warp, src.getLocation());
            if (Sponge.getEventManager().post(event)) {
                return event.getCancelMessage()
                        .map(context::errorResultLiteral)
                        .orElseGet(() -> context.errorResult("nucleus.eventcancelled")
                );
            }

            // OK! Set it.
            if (warpService.setWarp(warp, src.getLocation(), src.getRotation())) {
                // Worked. Tell them.
                context.sendMessage("command.warps.set", warp);
                return context.successResult();
            }

            // Didn't work. Tell them.
            return context.errorResult("command.warps.seterror");
        }
    }
}
