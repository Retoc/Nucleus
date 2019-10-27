/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.annotation.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.teleport.TeleportPermissions;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfig;
import io.github.nucleuspowered.nucleus.modules.teleport.events.RequestEvent;
import io.github.nucleuspowered.nucleus.modules.teleport.services.PlayerTeleporterService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

@NonnullByDefault
@NotifyIfAFK(NucleusParameters.Keys.PLAYER)
@Command(
        aliases = {"tpa", "teleportask", "call", "tpask"},
        basePermission = TeleportPermissions.BASE_TPA,
        commandDescriptionKey = "tpa",
        modifiers =
        {
                @CommandModifier(
                        value = CommandModifiers.HAS_WARMUP,
                        exemptPermission = TeleportPermissions.EXEMPT_WARMUP_TPA,
                        onExecute = false
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COOLDOWN,
                        exemptPermission = TeleportPermissions.EXEMPT_COOLDOWN_TPA,
                        onCompletion = false
                ),
                @CommandModifier(
                        value = CommandModifiers.HAS_COST,
                        exemptPermission = TeleportPermissions.EXEMPT_COST_TPA
                )
        }
)
@EssentialsEquivalent({"tpa", "call", "tpask"})
public class TeleportAskCommand implements ICommandExecutor<Player>, Reloadable {

    private boolean isCooldownOnAsk = false;

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                GenericArguments.flags().permissionFlag(TeleportPermissions.TELEPORT_ASK_FORCE, "f").buildWith(NucleusParameters.ONE_PLAYER)
        };
    }

    @Override public Optional<ICommandResult> preExecute(ICommandContext.Mutable<? extends Player> context) throws CommandException {
        boolean canTeleport = context.getServiceCollection().getServiceUnchecked(PlayerTeleporterService.class)
                .canTeleportTo(
                        context.getIfPlayer(),
                        context.requireOne(NucleusParameters.Keys.PLAYER, Player.class)
                );
        if (canTeleport) {
            return Optional.empty();
        }

        return Optional.of(context.failResult());
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends Player> context) throws CommandException {
        Player target = context.requireOne(NucleusParameters.Keys.PLAYER, Player.class);
        if (context.is(target)) {
            return context.errorResult("command.teleport.self");
        }

        RequestEvent.CauseToPlayer event = new RequestEvent.CauseToPlayer(context.getCause(), target);
        if (Sponge.getEventManager().post(event)) {
            if (event.getCancelMessage().isPresent()) {
                return context.errorResultLiteral(event.getCancelMessage().get());
            } else {
                return context.errorResult("command.tpa.eventfailed");
            }
        }

        Consumer<Player> cooldownSetter = player -> {};
        if (this.isCooldownOnAsk) {
            setCooldown(context);
        } else {
            cooldownSetter = player -> setCooldown(context);
        }

        context.getServiceCollection().getServiceUnchecked(PlayerTeleporterService.class).requestTeleport(
                context.getIfPlayer(),
                target,
                context.getCost(),
                context.getWarmup(),
                context.getIfPlayer(),
                target,
                !context.getOne("f", boolean.class).orElse(false),
                false,
                false,
                cooldownSetter,
                "command.tpa.question"
        );

        return context.successResult();
    }

    private void setCooldown(ICommandContext<? extends Player> context) {
        try {
            context.getServiceCollection()
                    .cooldownService()
                    .setCooldown(
                            context.getCommandKey(),
                            context.getIfPlayer(),
                            Duration.ofSeconds(context.getCooldown())
                    );
        } catch (CommandException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.isCooldownOnAsk = serviceCollection.moduleDataProvider()
                .getModuleConfig(TeleportConfig.class)
                .isCooldownOnAsk();
    }
}
