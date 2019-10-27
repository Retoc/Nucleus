/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import static io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter.KIT_PARAMETER_KEY;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfig;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Sets kit to be automatically redeemed on login.
 */
@NonnullByDefault
@Command(
        aliases = { "autoredeem" },
        basePermission = KitPermissions.BASE_KIT_AUTOREDEEM,
        commandDescriptionKey = "kit.autoredeem",
        parentCommand = KitCommand.class,
        async = true
)
public class KitAutoRedeemCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private boolean autoRedeemEnabled = false;

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(true),
                NucleusParameters.ONE_TRUE_FALSE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Kit kitInfo = context.requireOne(KIT_PARAMETER_KEY, Kit.class);
        boolean b = context.requireOne(NucleusParameters.Keys.BOOL, Boolean.class);

        // This Kit is a reference back to the version in list, so we don't need
        // to update it explicitly
        kitInfo.setAutoRedeem(b);
        context.getServiceCollection().getServiceUnchecked(KitHandler.class).saveKit(kitInfo);
        context.sendMessage(b ? "command.kit.autoredeem.on" : "command.kit.autoredeem.off", kitInfo.getName());
        if (!this.autoRedeemEnabled) {
            context.sendMessage("command.kit.autoredeem.disabled");
        }

        return context.successResult();
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.autoRedeemEnabled = serviceCollection.moduleDataProvider().getModuleConfig(KitConfig.class).isEnableAutoredeem();
    }
}
