/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.api.teleport.TeleportResult;
import io.github.nucleuspowered.nucleus.api.teleport.TeleportResults;
import io.github.nucleuspowered.nucleus.api.teleport.TeleportScanners;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.warp.WarpPermissions;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfig;
import io.github.nucleuspowered.nucleus.modules.warp.event.UseWarpEvent;
import io.github.nucleuspowered.nucleus.modules.warp.services.WarpService;
import io.github.nucleuspowered.nucleus.services.IEconomyServiceProvider;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.INucleusTeleportService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.teleport.TeleportHelperFilter;

import java.util.Optional;

@NonnullByDefault
@EssentialsEquivalent(value = {"warp", "warps"}, isExact = false, notes = "Use '/warp' for warping, '/warps' to list warps.")
@Command(
        aliases = {"warp"},
        basePermission = WarpPermissions.BASE_WARP,
        commandDescriptionKey = "warp",
        modifiers = {
                @CommandModifier(
                        value = CommandModifiers.HAS_WARMUP,
                        exemptPermission = WarpPermissions.EXEMPT_WARMUP_WARP
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COOLDOWN,
                        exemptPermission = WarpPermissions.EXEMPT_COOLDOWN_WARP
                )
        }
)
public class WarpCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private boolean isSafeTeleport = true;
    private double defaultCost = 0;

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        WarpConfig wc = serviceCollection.moduleDataProvider().getModuleConfig(WarpConfig.class);
        this.defaultCost = wc.getDefaultWarpCost();
        this.isSafeTeleport = wc.isSafeTeleport();
    }

    // flag,
    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments
                        .optionalWeak(GenericArguments.flags()
                                .flag("y", "a", "-accept")
                                .flag("f", "-force")
                                .setAnchorFlags(false)
                                .buildWith(GenericArguments.none()))),
                serviceCollection.commandElementSupplier()
                        .createPermissionParameter(
                                NucleusParameters.OPTIONAL_ONE_PLAYER,
                                WarpPermissions.OTHERS_WARP),

                GenericArguments.onlyOne(serviceCollection.getServiceUnchecked(WarpService.class)
                        .warpElement(true))
        };
    }

    @Override public Optional<ICommandResult> preExecute(ICommandContext.Mutable<? extends CommandSource> context) throws CommandException {
        Player target = context.getPlayerFromArgs();
        IEconomyServiceProvider economyServiceProvider = context.getServiceCollection().economyServiceProvider();
        if (!context.is(target)) {
            // Don't cooldown
            context.removeModifier(CommandModifiers.HAS_COOLDOWN);
            return Optional.empty();
        }

        if (!economyServiceProvider.serviceExists() ||
                context.testPermission(WarpPermissions.EXEMPT_COST_WARP) ||
                context.hasAny("y")) {
            return Optional.empty();
        }

        Warp wd = context.requireOne(WarpService.WARP_KEY, Warp.class);
        Optional<Double> i = wd.getCost();
        double cost = i.orElse(this.defaultCost);

        if (cost <= 0) {
            return Optional.empty();
        }

        String costWithUnit = economyServiceProvider.getCurrencySymbol(cost);
        if (economyServiceProvider.hasBalance(target, cost)) {
            String command = String.format("/warp -y %s", wd.getName());
            context.sendMessage("command.warp.cost.details", wd.getName(), costWithUnit);
            context.sendMessageText(
                    context.getMessage("command.warp.cost.clickaccept").toBuilder()
                            .onClick(TextActions.runCommand(command)).onHover(
                                    TextActions.showText(context.getMessage("command.warp.cost.clickhover", command)))
                            .append(context.getMessage("command.warp.cost.alt")).build());
        } else {
            context.sendMessage("command.warp.cost.nomoney", wd.getName(), costWithUnit);
        }

        return Optional.of(context.failResult());
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Player player = context.getPlayerFromArgs();
        boolean isOther = !context.is(player);

        // Permission checks are done by the parser.
        Warp wd = context.requireOne(WarpService.WARP_KEY, Warp.class);

        // Load the world in question
        if (!wd.getTransform().isPresent()) {
            Sponge.getServer().loadWorld(wd.getWorldProperties().get().getUniqueId())
                .orElseThrow(() -> context.createException("command.warp.worldnotloaded"));
        }

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(context.getCommandSource());
            UseWarpEvent event = new UseWarpEvent(frame.getCurrentCause(), player, wd);
            if (Sponge.getEventManager().post(event)) {
                return event.getCancelMessage().map(context::errorResultLiteral)
                        .orElseGet(() -> context.errorResult("nucleus.eventcancelled"));
            }

            Optional<Double> i = wd.getCost();
            double cost = i.orElse(this.defaultCost);

            boolean charge = false;
            IEconomyServiceProvider economyServiceProvider = context.getServiceCollection().economyServiceProvider();
            if (!isOther && economyServiceProvider.serviceExists() && cost > 0 &&
                    !context.testPermission(WarpPermissions.EXEMPT_COST_WARP)) {
                if (economyServiceProvider.withdrawFromPlayer(player, cost, false)) {
                    charge = true; // only true for a warp by the current subject.
                } else {
                    return context.errorResult("command.warp.cost.nomoney", wd.getName(),
                            economyServiceProvider.getCurrencySymbol(cost));
                }
            }

            // We have a warp data, warp them.
            if (isOther) {
                context.sendMessage("command.warps.namedstart",
                        context.getDisplayName(player.getUniqueId()),
                        wd.getName());
            } else {
                context.sendMessage("command.warps.start", wd.getName());
            }

            // Warp them.
            boolean isSafe = !context.hasAny("f") && this.isSafeTeleport;

            INucleusTeleportService safeLocationService = context.getServiceCollection().teleportService();
            TeleportHelperFilter filter = safeLocationService.getAppropriateFilter(player, isSafe);

            TeleportResult result = safeLocationService.teleportPlayer(
                    player,
                    wd.getLocation().get(),
                    wd.getRotation(),
                    false,
                    TeleportScanners.NO_SCAN,
                    filter
            );

            if (!result.isSuccessful()) {
                if (charge) {
                    economyServiceProvider.depositInPlayer(player, cost, false);
                }

                // Don't add the cooldown if enabled.
                return context.errorResult(result == TeleportResults.FAIL_NO_LOCATION ? "command.warps.nosafe" :
                        "command.warps.cancelled");
            }

            if (isOther) {
                context.sendMessageTo(player, "command.warps.warped", wd.getName());
            } else if (charge) {
                context.sendMessage("command.warp.cost.charged", economyServiceProvider.getCurrencySymbol(cost));
            }

            return context.successResult();
        }
    }
}
