/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveDoubleArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.warp.WarpPermissions;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfig;
import io.github.nucleuspowered.nucleus.modules.warp.services.WarpService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@Command(
        aliases = {"cost", "setcost"},
        basePermission = WarpPermissions.BASE_WARP_COST,
        commandDescriptionKey = "warp.cost",
        async = true,
        parentCommand = WarpCommand.class
)
public class SetCostCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private final String costKey = "cost";
    private double defaultCost = 0;

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(WarpService.class).warpElement(false),
                GenericArguments.onlyOne(new PositiveDoubleArgument(Text.of(this.costKey)))
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Warp warpData = context.requireOne(WarpService.WARP_KEY, Warp.class);
        double cost = context.requireOne(this.costKey, Double.class);
        if (cost < -1) {
            return context.errorResult("command.warp.costset.arg");
        }

        WarpService warpService = context.getServiceCollection().getServiceUnchecked(WarpService.class);
        if (cost == -1 && warpService.setWarpCost(warpData.getName(), -1)) {
            context.sendMessage("command.warp.costset.reset", warpData.getName(), String.valueOf(this.defaultCost));
            return context.successResult();
        } else if (warpService.setWarpCost(warpData.getName(), cost)) {
            context.sendMessage("command.warp.costset.success", warpData.getName(), cost);
            return context.successResult();
        }

        return context.errorResult("command.warp.costset.failed", warpData.getName());
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        this.defaultCost = serviceCollection.moduleDataProvider()
                .getModuleConfig(WarpConfig.class)
                .getDefaultWarpCost();
    }

}
