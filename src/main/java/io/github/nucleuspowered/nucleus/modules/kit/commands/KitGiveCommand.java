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
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfig;
import io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Gives a kit to a subject.
 */
@Command(
        aliases = { "give" },
        basePermission = KitPermissions.BASE_KIT_GIVE,
        commandDescriptionKey = "kit.give",
        parentCommand = KitCommand.class
)
@NonnullByDefault
public class KitGiveCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private boolean mustGetAll;
    private boolean isDrop;

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.flags().permissionFlag(KitPermissions.KIT_GIVE_OVERRIDE, "i", "-ignore")
                    .buildWith(GenericArguments.seq(
                        NucleusParameters.ONE_PLAYER,
                        serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(false)
                ))
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        KitHandler service = context.getServiceCollection().getServiceUnchecked(KitHandler.class);
        Kit kit = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);
        Player player = context.requireOne(NucleusParameters.Keys.PLAYER, Player.class);
        boolean skip = context.hasAny("i");
        if (context.is(player)) {
            return context.errorResult("command.kit.give.self");
        }

        Text playerName = context.getDisplayName(player.getUniqueId());
        Text kitName = Text.of(kit.getName());
        try {
            NucleusKitService.RedeemResult redeemResult = service.redeemKit(kit, player, !skip, this.mustGetAll);
            if (!redeemResult.rejected().isEmpty()) {
                // If we drop them, tell the user
                if (this.isDrop) {
                    context.sendMessage("command.kit.give.itemsdropped", playerName);
                    redeemResult.rejected().forEach(x -> Util.dropItemOnFloorAtLocation(x, player.getLocation()));
                } else {
                    context.sendMessage("command.kit.give.fullinventory", playerName);
                }
            }

            context.sendMessage("command.kit.give.spawned", playerName, kitName);
            if (kit.isDisplayMessageOnRedeem()) {
                context.sendMessage("command.kit.spawned", kit.getName());
            }

            return context.successResult();
        } catch (KitRedeemException ex) {
            switch (ex.getReason()) {
                case ALREADY_REDEEMED:
                    return context.errorResult("command.kit.give.onetime.alreadyredeemed", kitName, playerName);
                case COOLDOWN_NOT_EXPIRED:
                    KitRedeemException.Cooldown kre = (KitRedeemException.Cooldown) ex;
                    return context.errorResult("command.kit.give.cooldown",
                            playerName,
                            context.getTimeString(kre.getTimeLeft()),
                            kitName);
                case PRE_EVENT_CANCELLED:
                    KitRedeemException.PreCancelled krepe = (KitRedeemException.PreCancelled) ex;
                    return krepe.getCancelMessage()
                            .map(context::errorResultLiteral)
                            .orElseGet(() -> context.errorResult("command.kit.cancelledpre", kit.getName()));
                case NO_SPACE:
                    return context.errorResult("command.kit.give.fullinventorynosave", playerName);
                case UNKNOWN:
                default:
                    return context.errorResult("command.kit.give.fail", playerName, kitName);
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
