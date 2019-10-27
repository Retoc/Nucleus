/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.LocationData;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.jail.JailParameters;
import io.github.nucleuspowered.nucleus.modules.jail.JailPermissions;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.services.JailHandler;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.inject.Inject;

@NonnullByDefault
@Command(
        aliases = {"jail", "unjail", "togglejail"},
        basePermission = JailPermissions.BASE_JAIL,
        commandDescriptionKey = "jail"
)
@EssentialsEquivalent({"togglejail", "tjail", "unjail", "jail"})
public class JailCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private final JailHandler handler;

    private boolean requireUnjailPermission = false;

    @Inject
    public JailCommand(INucleusServiceCollection serviceCollection) {
        this.handler = serviceCollection.getServiceUnchecked(JailHandler.class);
    }

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                JailParameters.OPTIONAL_JAIL,
                NucleusParameters.OPTIONAL_WEAK_DURATION,
                NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        // Get the subject.
        User pl = context.requireOne(NucleusParameters.Keys.USER, User.class);
        if (!pl.isOnline() && !context.testPermission(JailPermissions.JAIL_OFFLINE)) {
            return context.errorResult("command.jail.offline.noperms");
        }

        if (this.handler.isPlayerJailed(pl)) {
            if (!this.requireUnjailPermission || context.testPermission(JailPermissions.JAIL_UNJAIL)) {
                return onUnjail(context, pl);
            }

            return context.errorResult("command.jail.unjail.perm");
        } else {
            if (!context.isConsoleAndBypass() && context.testPermission(JailPermissions.JAIL_EXEMPT_TARGET)) {
                return context.errorResult("command.jail.exempt", pl.getName());
            }

            return onJail(context, pl);
        }
    }

    private ICommandResult onUnjail(ICommandContext<? extends CommandSource> context, User user) throws CommandException {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(context.getCommandSource());
            if (this.handler.unjailPlayer(user)) {
                context.sendMessage("command.jail.unjail.success", user.getName());
                return context.successResult();
            } else {
                return context.errorResult("command.jail.unjail.fail", user.getName());
            }
        }
    }

    private ICommandResult onJail(ICommandContext<? extends CommandSource> context, User user) throws CommandException {
        Optional<LocationData> owl = context.getOne(JailParameters.JAIL_KEY, LocationData.class);
        if (!owl.isPresent()) {
            return context.errorResult("command.jail.jail.nojail");
        }

        // This might not be there.
        Optional<Long> duration = context.getOne(NucleusParameters.Keys.DURATION, Long.class);
        String reason = context.getOne(NucleusParameters.Keys.REASON, String.class)
                .orElseGet(() -> context.getMessageString("command.jail.reason"));
        JailData jd;
        Text message;
        Text messageTo;

        CommandSource src = context.getCommandSource();
        if (duration.isPresent()) {
            if (user.isOnline()) {
                jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, user.getPlayer().get().getLocation(),
                        Instant.now().plusSeconds(duration.get()));
            } else {
                jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, null, Duration.of(duration.get(), ChronoUnit.SECONDS));
            }

            IMessageProviderService messageProviderService = context.getServiceCollection().messageProvider();
            message = context.getMessage("command.checkjail.jailedfor", user.getName(), jd.getJailName(),
                    src.getName(), messageProviderService.getTimeString(src.getLocale(), duration.get()));
            messageTo = context.getMessage("command.jail.jailedfor", owl.get().getName(), src.getName(),
                    messageProviderService.getTimeString(src.getLocale(), duration.get()));
        } else {
            jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, user.getPlayer().map(Locatable::getLocation).orElse(null));
            message = context.getMessage("command.checkjail.jailedperm", user.getName(), owl.get().getName(), src.getName());
            messageTo = context.getMessage("command.jail.jailedperm", owl.get().getName(), src.getName());
        }

        if (this.handler.jailPlayer(user, jd)) {
            MutableMessageChannel mc = new PermissionMessageChannel(context.getServiceCollection().permissionService(),
                    JailPermissions.JAIL_NOTIFY).asMutable();
            mc.addMember(src);
            mc.send(message);
            mc.send(context.getMessage("standard.reasoncoloured", reason));

            user.getPlayer().ifPresent(x -> {
                x.sendMessage(messageTo);
                x.sendMessage(context.getMessage("standard.reasoncoloured", reason));
            });

            return context.successResult();
        }

        return context.errorResult("command.jail.error");
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        this.requireUnjailPermission = serviceCollection.moduleDataProvider().getModuleConfig(JailConfig.class).isRequireUnjailPermission();
    }
}
