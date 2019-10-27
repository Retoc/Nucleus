/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.nucleus;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.core.CorePermissions;
import io.github.nucleuspowered.nucleus.modules.core.commands.NucleusCommand;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.services.impl.storage.queryobjects.IUserQueryObject;
import io.github.nucleuspowered.storage.services.IStorageService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@NonnullByDefault
@Command(
        aliases = "resetuser",
        basePermission = CorePermissions.BASE_NUCLEUS_RESETUSER,
        commandDescriptionKey = "nucleus.resetuser",
        parentCommand = NucleusCommand.class,
        async = true
)
public class ResetUserCommand implements ICommandExecutor<CommandSource> {

    private final String userKey = "user";
    private final String uuidKey = "UUID";

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
            GenericArguments.flags().flag("a", "-all").buildWith(
                GenericArguments.firstParsing(
                    GenericArguments.user(Text.of(this.userKey)),
                        new UUIDArgument<>(Text.of(this.uuidKey), u -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(u))
                ))
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        final User user = context.getOne(this.userKey, User.class).orElseGet(() -> context.requireOne(this.uuidKey, User.class));
        final boolean deleteall = context.hasAny("a");

        List<Text> messages = new ArrayList<>();

        IMessageProviderService messageProvider = context.getServiceCollection().messageProvider();
        CommandSource source = context.getCommandSource();

        messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning"));
        messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning2", user.getName()));
        messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning3"));
        messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning4"));
        messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning5"));
        messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning6"));
        if (deleteall) {
            messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning8"));
        } else {
            messages.add(messageProvider.getMessageFor(source, "command.nucleus.reset.warning7"));
        }

        messages.add(Text.builder().append(messageProvider.getMessageFor(source, "command.nucleus.reset.reset")).style(TextStyles.UNDERLINE)
                .onClick(TextActions.executeCallback(new Delete(user, deleteall, context.getServiceCollection()))).build());

        source.sendMessages(messages);
        return context.successResult();
    }

    private static class Delete implements Consumer<CommandSource> {

        private final User user;
        private final boolean all;
        private final INucleusServiceCollection serviceCollection;

        Delete(User user, boolean all, INucleusServiceCollection serviceCollection) {
            this.user = user;
            this.all = all;
            this.serviceCollection = serviceCollection;
        }

        @Override
        public void accept(final CommandSource source) {
            IMessageProviderService messageProvider = this.serviceCollection.messageProvider();
            if (this.user.isOnline()) {
                Player player = this.user.getPlayer().get();
                Text kickReason = messageProvider.getMessageFor(player, "command.kick.defaultreason");
                player.kick(kickReason);

                // Let Sponge do what it needs to close the user off.
                Task.builder().execute(() -> this.accept(source)).delayTicks(1).submit(Nucleus.getNucleus());
                return;
            }

            source.sendMessage(messageProvider.getMessageFor(source, "command.nucleus.reset.starting", this.user.getName()));

            // Ban temporarily.
            final BanService bss = Sponge.getServiceManager().provideUnchecked(BanService.class);
            final boolean isBanned = bss.getBanFor(this.user.getProfile()).isPresent();
            bss.addBan(Ban.builder().type(BanTypes.PROFILE).expirationDate(Instant.now().plus(30, ChronoUnit.SECONDS)).profile(this.user.getProfile())
                    .build());

            // Unload the player in a second, just to let events fire.
            Sponge.getScheduler().createAsyncExecutor(Nucleus.getNucleus()).schedule(() -> {
                IStorageService.Keyed<UUID, IUserQueryObject, IUserDataObject> userStorageService =
                        this.serviceCollection.storageManager().getUserService();

                // Get the file to delete.
                try {
                    // Remove them from the cache immediately.
                    userStorageService.clearCache();
                    userStorageService.delete(this.user.getUniqueId());
                    if (this.all) {
                        String uuid = this.user.getUniqueId() + ".dat";
                        if (Sponge.getServiceManager().provideUnchecked(UserStorageService.class).delete(this.user)) {
                            // Sponge Data
                            Files.deleteIfExists(Sponge.getGame().getSavesDirectory().resolve("data/sponge").resolve(uuid));
                            source.sendMessage(messageProvider.getMessageFor(source, "command.nucleus.reset.completeall", this.user.getName()));
                        } else {
                            source.sendMessage(messageProvider.getMessageFor(source, "command.nucleus.reset.completenonm", this.user.getName()));
                        }
                    } else {
                        source.sendMessage(messageProvider.getMessageFor(source, "command.nucleus.reset.complete", this.user.getName()));
                    }

                    source.sendMessage(messageProvider.getMessageFor(source, "command.nucleus.reset.restartadvised", this.user.getName()));
                } catch (Exception e) {
                    source.sendMessage(messageProvider.getMessageFor(source, "command.nucleus.reset.failed", this.user.getName()));
                } finally {
                    if (!isBanned) {
                        bss.getBanFor(this.user.getProfile()).ifPresent(bss::removeBan);
                    }
                }
            } , 1, TimeUnit.SECONDS);
        }
    }
}
