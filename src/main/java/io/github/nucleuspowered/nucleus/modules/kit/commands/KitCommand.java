/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.KitRedeemException;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfig;
import io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitHandler;
import io.github.nucleuspowered.nucleus.services.IEconomyServiceProvider;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Allows a user to redeem a kit.
 */
@NonnullByDefault
@Command(
        aliases = { "kit" },
        basePermission = KitPermissions.BASE_KIT,
        commandDescriptionKey = "kit",
        modifiers = {
                // Cooldowns and cost are determined by the kit itself.
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = KitPermissions.EXEMPT_WARMUP_KIT)
        }
)
@EssentialsEquivalent(value = "kit, kits", isExact = false, notes = "'/kit' redeems, '/kits' lists.")
public class KitCommand implements ICommandExecutor<Player>, Reloadable {

    private boolean isDrop;
    private boolean mustGetAll;

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(true)
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        Kit kit = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);

        KitHandler kitService = context.getServiceCollection().getServiceUnchecked(KitHandler.class);
        Player player = context.getCommandSourceAsPlayerUnchecked();
        IEconomyServiceProvider econHelper = context.getServiceCollection().economyServiceProvider();
        double cost = econHelper.serviceExists() ? kit.getCost() : 0;
        if (context.testPermission(KitPermissions.KIT_EXEMPT_COST)) {
            // If exempt - no cost.
            cost = 0;
        }

        // If we have a cost for the kit, check we have funds.
        if (cost > 0 && !econHelper.hasBalance(player, cost)) {
            return context.errorResult("command.kit.notenough", kit.getName(), econHelper.getCurrencySymbol(cost));
        }

        try {
            NucleusKitService.RedeemResult redeemResult = kitService.redeemKit(kit, player, true, this.mustGetAll);
            if (!redeemResult.rejected().isEmpty()) {
                // If we drop them, tell the user
                if (this.isDrop) {
                    context.sendMessage("command.kit.itemsdropped");
                    redeemResult.rejected().forEach(x -> Util.dropItemOnFloorAtLocation(x, player.getLocation()));
                } else {
                    context.sendMessage("command.kit.fullinventory");
                }
            }

            if (kit.isDisplayMessageOnRedeem()) {
                context.sendMessage("command.kit.spawned", kit.getName());
            }

            // Charge, if necessary
            if (cost > 0 && econHelper.serviceExists()) {
                econHelper.withdrawFromPlayer(player, cost);
            }

            return context.successResult();
        } catch (KitRedeemException ex) {
            switch (ex.getReason()) {
                case ALREADY_REDEEMED:
                    return context.errorResult("command.kit.onetime.alreadyredeemed", kit.getName());
                case COOLDOWN_NOT_EXPIRED:
                    KitRedeemException.Cooldown kre = (KitRedeemException.Cooldown) ex;
                    return context.errorResult("command.kit.cooldown",
                            context.getServiceCollection().messageProvider().getTimeString(
                                    context.getCommandSource().getLocale(),
                                    kre.getTimeLeft().getSeconds()),
                            kit.getName());
                case PRE_EVENT_CANCELLED:
                    KitRedeemException.PreCancelled krepe = (KitRedeemException.PreCancelled) ex;
                    return krepe.getCancelMessage()
                            .map(x -> context.errorResultLiteral(Text.of(x)))
                            .orElseGet(() -> context.errorResult("command.kit.cancelledpre", kit.getName()));
                case NO_SPACE:
                    return context.errorResult("command.kit.fullinventorynosave", kit.getName());
                case UNKNOWN:
                default:
                    return context.errorResult("command.kit.fail", kit.getName());
            }
        }

    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        KitConfig kca = serviceCollection.moduleDataProvider().getModuleConfig(KitConfig.class);
        this.isDrop = kca.isDropKitIfFull();
        this.mustGetAll = kca.isMustGetAll();
    }
}
