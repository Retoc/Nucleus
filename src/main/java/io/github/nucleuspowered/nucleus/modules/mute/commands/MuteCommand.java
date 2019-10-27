/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.mute.MutePermissions;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.services.MuteHandler;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@NonnullByDefault
@EssentialsEquivalent({"mute", "silence"})
@Command(aliases = { "mute", "unmute" }, basePermission = MutePermissions.BASE_MUTE, commandDescriptionKey = "mute")
public class MuteCommand implements ICommandExecutor<CommandSource>, Reloadable {

    private boolean requireUnmutePermission = false;
    private long maxMute = Long.MAX_VALUE;

    // seemuted

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
            NucleusParameters.ONE_USER,
            NucleusParameters.OPTIONAL_WEAK_DURATION,
            NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        MuteHandler handler = context.getServiceCollection().getServiceUnchecked(MuteHandler.class);
        // Get the user.
        User user = context.requireOne(NucleusParameters.Keys.USER, User.class);
                //args.<User>getOne(NucleusParameters.Keys.USER).get();

        Optional<Long> time = context.getOne(NucleusParameters.Keys.DURATION, Long.class);
        Optional<MuteData> omd = handler.getPlayerMuteData(user);
        Optional<String> reas = context.getOne(NucleusParameters.Keys.REASON, String.class);

        if (!context.isConsoleAndBypass() && context.testPermission(MutePermissions.MUTE_EXEMPT_TARGET)) {
            return context.errorResult("command.mute.exempt", user.getName());
        }

        // No time, no reason, but is muted, unmute
        if (omd.isPresent() && !time.isPresent() && !reas.isPresent()) {
            if (!this.requireUnmutePermission || context.testPermission(MutePermissions.MUTE_UNMUTE)) {
                // Unmute.
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.pushCause(context.getCommandSource());
                    handler.unmutePlayer(user, frame.getCurrentCause(), false);
                    context.sendMessage("command.unmute.success", user.getName(), context.getName());
                    return context.successResult();
                }
            }

            return context.errorResult("command.unmute.perm");
        }

        // Do we have a reason?
        String rs = reas.orElseGet(() -> context.getMessageString("command.mute.defaultreason"));
        UUID ua = Util.CONSOLE_FAKE_UUID;
        if (context.is(Player.class)) {
            ua = context.getIfPlayer().getUniqueId();
        }

        if (this.maxMute > 0 && time.orElse(Long.MAX_VALUE) > this.maxMute && !context.testPermission(MutePermissions.MUTE_EXEMPT_TARGET)) {
            return context.errorResult("command.mute.length.toolong", context.getTimeString(this.maxMute));
        }

        MuteData data;
        if (time.isPresent()) {
            if (!user.isOnline()) {
                data = new MuteData(ua, rs, Duration.ofSeconds(time.get()));
            } else {
                data = new MuteData(ua, rs, Instant.now().plus(time.get(), ChronoUnit.SECONDS));
            }
        } else {
            data = new MuteData(ua, rs);
        }

        CommandSource src = context.getCommandSource();
        if (handler.mutePlayer(user, data)) {
            // Success.
            MutableMessageChannel mc =
                    context.getServiceCollection().permissionService().permissionMessageChannel(MutePermissions.MUTE_NOTIFY).asMutable();
            mc.addMember(src);

            if (time.isPresent()) {
                timedMute(context, user, data, time.get(), mc);
            } else {
                permMute(context, user, data, mc);
            }

            return context.successResult();
        }

        return context.errorResult("command.mute.fail", user.getName());
    }

    private void timedMute(ICommandContext<? extends CommandSource> context, User user, MuteData data, long time, MessageChannel mc) {
        String ts = context.getTimeString(time);
        mc.send(context.getMessage("command.mute.success.time", user.getName(), context.getName(), ts));
        mc.send(context.getMessage("standard.reasoncoloured", data.getReason()));

        if (user.isOnline()) {
            context.sendMessageTo(user.getPlayer().get(), "mute.playernotify.time", ts);
            context.sendMessageTo(user.getPlayer().get(), "command.reason", data.getReason());
        }
    }

    private void permMute(ICommandContext<? extends CommandSource> context, User user, MuteData data, MessageChannel mc) {
        mc.send(context.getMessage("command.mute.success.norm", user.getName(), context.getName()));
        mc.send(context.getMessage("standard.reasoncoloured", data.getReason()));

        if (user.isOnline()) {
            context.sendMessageTo(user.getPlayer().get(), "mute.playernotify.standard");
            context.sendMessageTo(user.getPlayer().get(), "command.reason", data.getReason());
        }
    }

    @Override
    public void onReload(INucleusServiceCollection serviceCollection) {
        MuteConfigAdapter mca = serviceCollection.getServiceUnchecked(MuteConfigAdapter.class);
        this.requireUnmutePermission = mca.getNodeOrDefault().isRequireUnmutePermission();
        this.maxMute = mca.getNodeOrDefault().getMaximumMuteLength();
    }
}
