/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfig;
import io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

@Command(
        aliases = { "view" },
        basePermission = KitPermissions.BASE_KIT_VIEW,
        commandDescriptionKey = "kit.view",
        parentCommand = KitCommand.class
)
@NonnullByDefault
public class KitViewCommand implements ICommandExecutor<Player>, Reloadable {

    private boolean processTokens = false;

    @Override public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(true)
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        final Kit kitInfo = context.requireOne(KitParameter.KIT_PARAMETER_KEY, Kit.class);
        final KitHandler service = context.getServiceCollection().getServiceUnchecked(KitHandler.class);
        final Player src = context.getIfPlayer();

        Inventory inventory = Util.getKitInventoryBuilder()
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(context.getMessage("command.kit.view.title", kitInfo.getName())))
                .build(Nucleus.getNucleus());

        List<ItemStack> lis = kitInfo.getStacks().stream().filter(x -> !x.getType().equals(ItemTypes.NONE)).map(ItemStackSnapshot::createStack)
                .collect(Collectors.toList());
        if (this.processTokens) {
            service.processTokensInItemStacks(src, lis);
        }

        lis.forEach(inventory::offer);
        return src.openInventory(inventory)
            .map(x -> {
                service.addViewer(x);
                return context.successResult();
            })
            .orElseGet(() -> context.errorResult("command.kit.view.cantopen", kitInfo.getName()));
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.processTokens = serviceCollection.moduleDataProvider().getModuleConfig(KitConfig.class).isProcessTokens();
    }
}
