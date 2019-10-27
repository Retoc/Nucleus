/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import static io.github.nucleuspowered.nucleus.modules.kit.parameters.KitParameter.KIT_PARAMETER_KEY;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
@Command(
        aliases = { "edit", "ed" },
        basePermission = KitPermissions.BASE_KIT_EDIT,
        commandDescriptionKey = "kit.edit",
        parentCommand = KitCommand.class
)
public class KitEditCommand implements ICommandExecutor<Player> {

    @Override public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                serviceCollection.getServiceUnchecked(KitHandler.class).createKitElement(false)
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        final KitHandler service = context.getServiceCollection().getServiceUnchecked(KitHandler.class);
        final Kit kitInfo = context.requireOne(KIT_PARAMETER_KEY, Kit.class);

        if (service.isOpen(kitInfo.getName())) {
            return context.errorResult("command.kit.edit.current", kitInfo.getName());
        }

        Inventory inventory = Util.getKitInventoryBuilder()
            .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(context.getMessage("command.kit.edit.title", kitInfo.getName())))
            .build(context.getServiceCollection().pluginContainer());

        kitInfo.getStacks().stream().filter(x -> !x.getType().equals(ItemTypes.NONE)).forEach(x -> inventory.offer(x.createStack()));
        Optional<Container> openedInventory = context.getIfPlayer().openInventory(inventory);

        if (openedInventory.isPresent()) {
            service.addKitInventoryToListener(Tuple.of(kitInfo, inventory), openedInventory.get());
            return context.successResult();
        }

        return context.errorResult("command.kit.edit.cantopen", kitInfo.getName());
    }
}
