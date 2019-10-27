/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.api.EventContexts;
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
import io.github.nucleuspowered.nucleus.modules.spawn.SpawnPermissions;
import io.github.nucleuspowered.nucleus.modules.spawn.config.GlobalSpawnConfig;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfig;
import io.github.nucleuspowered.nucleus.modules.spawn.events.SendToSpawnEvent;
import io.github.nucleuspowered.nucleus.modules.spawn.helpers.SpawnHelper;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

@EssentialsEquivalent("spawn")
@NonnullByDefault
@Command(
        aliases = "spawn",
        basePermission = SpawnPermissions.BASE_SPAWN,
        commandDescriptionKey = "spawn",
        modifiers = {
                @CommandModifier(value = CommandModifiers.HAS_COOLDOWN, exemptPermission = SpawnPermissions.EXEMPT_COOLDOWN_SPAWN),
                @CommandModifier(value = CommandModifiers.HAS_WARMUP, exemptPermission = SpawnPermissions.EXEMPT_WARMUP_SPAWN),
                @CommandModifier(value = CommandModifiers.HAS_COST, exemptPermission = SpawnPermissions.EXEMPT_COST_SPAWN)
        }
)
public class SpawnCommand implements ICommandExecutor<Player>, Reloadable {

    private SpawnConfig sc = new SpawnConfig();

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        this.sc = serviceCollection.moduleDataProvider().getModuleConfig(SpawnConfig.class);
    }

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
            GenericArguments.flags().permissionFlag(
                    SpawnPermissions.SPAWN_FORCE, "f", "-force").buildWith(
                            serviceCollection.commandElementSupplier()
                                .createPermissionParameter(
                                        NucleusParameters.WORLD_PROPERTIES_ENABLED_ONLY,
                                        SpawnPermissions.SPAWN_OTHERWORLDS))
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        boolean force = context.hasAny("f");
        Player src = context.getIfPlayer();
        GlobalSpawnConfig gsc = this.sc.getGlobalSpawn();
        WorldProperties wp = context.getOne(NucleusParameters.Keys.WORLD, WorldProperties.class)
            .orElseGet(() -> gsc.isOnSpawnCommand() ?
                    gsc.getWorld().orElse(src.getWorld().getProperties()) : src.getWorld().getProperties());

        Optional<World> ow = Sponge.getServer().loadWorld(wp.getUniqueId());

        if (!ow.isPresent()) {
            return context.errorResult("command.spawn.noworld");
        } else if (!context.testPermission(SpawnPermissions.SPAWN_WORLDS + "." + ow.get().getName().toLowerCase())) {
            return context.errorResult("command.spawn.nopermsworld", ow.get().getName());
        }

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContexts.SPAWN_EVENT_TYPE, SendToSpawnEvent.Type.COMMAND);
            SendToSpawnEvent event =
                    new SendToSpawnEvent(
                            SpawnHelper.getSpawn(
                                    ow.get().getProperties(),
                                    src,
                                    context),
                            src,
                            frame.getCurrentCause());
            if (Sponge.getEventManager().post(event)) {
                if (event.getCancelReason().isPresent()) {
                    return context.errorResult("command.spawnother.self.failed.reason", event.getCancelReason().get());
                } else {
                    return context.errorResult("command.spawnother.self.failed.noreason");
                }
            }

            // If we don't have a rotation, then use the current rotation
            TeleportResult result = context
                    .getServiceCollection()
                    .teleportService()
                    .teleportPlayerSmart(
                            src,
                            event.getTransformTo(),
                            true,
                            !force && this.sc.isSafeTeleport(),
                            TeleportScanners.NO_SCAN
                    );

            if (result.isSuccessful()) {
                context.sendMessage("command.spawn.success", wp.getWorldName());
                return context.successResult();
            }

            if (result == TeleportResults.FAIL_NO_LOCATION) {
                return context.errorResult("command.spawn.fail", wp.getWorldName());
            }

            return context.errorResult("command.spawn.cancelled", wp.getWorldName());
        }
    }
}
