/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat.commands;

import io.github.nucleuspowered.nucleus.api.EventContexts;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatPermissions;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatUserPrefKeys;
import io.github.nucleuspowered.nucleus.modules.staffchat.services.StaffChatService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@NonnullByDefault
@Command(
        aliases = {"staffchat", "sc", "a"},
        basePermission = StaffChatPermissions.BASE_STAFFCHAT,
        commandDescriptionKey = "staffchat"
)
public class StaffChatCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_MESSAGE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        Optional<String> toSend = context.getOne(NucleusParameters.Keys.MESSAGE, String.class);
        if (toSend.isPresent()) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContexts.SHOULD_FORMAT_CHANNEL, StaffChatMessageChannel.getInstance().formatMessages());
                if (context.is(Player.class)) {
                    Player pl = context.getIfPlayer();
                    frame.pushCause(pl);
                    frame.addContext(EventContextKeys.PLAYER_SIMULATED, pl.getProfile());

                    MessageChannel mc = pl.getMessageChannel();
                    pl.setMessageChannel(StaffChatMessageChannel.getInstance());
                    pl.simulateChat(
                            context.getServiceCollection()
                                    .textStyleService()
                                    .addUrls(toSend.get()), Sponge.getCauseStackManager().getCurrentCause());
                    pl.setMessageChannel(mc);

                    // If you send a message, you're viewing it again.
                    context.getServiceCollection()
                            .userPreferenceService()
                            .setPreferenceFor(pl, StaffChatUserPrefKeys.VIEW_STAFF_CHAT, true);
                } else {
                    StaffChatMessageChannel.getInstance()
                            .send(context.getCommandSource(),
                            context.getServiceCollection().textStyleService().addUrls(toSend.get()), ChatTypes.CHAT);
                }

                return context.successResult();
            }
        }

        if (!(context.is(Player.class))) {
            return context.errorResult("command.staffchat.consoletoggle");
        }

        Player player = context.getIfPlayer();

        StaffChatService service = context.getServiceCollection().getServiceUnchecked(StaffChatService.class);
        boolean result = service.isToggledChat(player);
        service.toggle(player, !result);

        context.sendMessage("command.staffchat." + (result ? "on" : "off"));
        return context.successResult();
    }

}
