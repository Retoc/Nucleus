/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.note.NotePermissions;
import io.github.nucleuspowered.nucleus.modules.note.data.NoteData;
import io.github.nucleuspowered.nucleus.modules.note.services.NoteHandler;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Instant;
import java.util.UUID;

@NonnullByDefault
@Command(
        aliases = {"note", "addnote"},
        basePermission = NotePermissions.NOTE_NOTIFY,
        commandDescriptionKey = "note",
        async = true
)
public class NoteCommand implements ICommandExecutor<CommandSource> {

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                NucleusParameters.MESSAGE
        };
    }

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        User user = context.requireOne(NucleusParameters.Keys.USER, User.class);
        String note = context.requireOne(NucleusParameters.Keys.MESSAGE, String.class);

        UUID noter = context.getUniqueId().orElse(Util.CONSOLE_FAKE_UUID);
        NoteData noteData = new NoteData(Instant.now(), noter, note);

        if (context.getServiceCollection().getServiceUnchecked(NoteHandler.class).addNote(user, noteData)) {
            MutableMessageChannel messageChannel =
                    context.getServiceCollection().permissionService().permissionMessageChannel(NotePermissions.NOTE_NOTIFY).asMutable();
            messageChannel.addMember(context.getCommandSource());
            IMessageProviderService messageProviderService = context.getServiceCollection().messageProvider();
            messageChannel.getMembers().forEach(messageReceiver ->
                    messageProviderService
                            .sendMessageTo(messageReceiver, "command.note.success", context.getName(), noteData.getNote(), user.getName())
            );

            return context.successResult();
        }

        context.sendMessage("command.warn.fail", user.getName());
        return context.successResult();
    }
}
