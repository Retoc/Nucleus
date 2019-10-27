/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.listeners;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.internal.interfaces.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.core.CoreKeys;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.modules.core.events.NucleusOnLoginEvent;
import io.github.nucleuspowered.nucleus.modules.core.events.OnFirstLoginEvent;
import io.github.nucleuspowered.nucleus.modules.core.events.UserDataLoadedEvent;
import io.github.nucleuspowered.nucleus.modules.core.services.UniqueUserService;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.storage.dataobjects.modular.IUserDataObject;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class CoreListener implements Reloadable, ListenerBase {

    private final INucleusServiceCollection serviceCollection;
    @Nullable private NucleusTextTemplate getKickOnStopMessage = null;
    @Nullable private final URL url;
    private boolean warnOnWildcard = true;

    @Inject
    public CoreListener(INucleusServiceCollection serviceCollection) {
        this.serviceCollection = serviceCollection;
        URL u = null;
        try {
            u = new URL("https://ore.spongepowered.org/Nucleus/Nucleus/pages/The-Permissions-Wildcard-(And-Why-You-Shouldn't-Use-It)");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.url = u;
    }

    @Listener(order = Order.POST)
    public void onPlayerAuth(final ClientConnectionEvent.Auth event) {
        final UUID userId = event.getProfile().getUniqueId();
        if (userId == null) { // it could be, I guess?
            return;
        }

        // Create user data if required, and place into cache.
        // As this is already async, load on thread.
        IUserDataObject dataObject = this.serviceCollection.storageManager().getUserService().getOrNewOnThread(userId);

        // Fire the event, which will be async too, perhaps unsurprisingly.
        // The main use for this will be migrations.
        UserDataLoadedEvent eventToFire = new UserDataLoadedEvent(
                event.getCause().with(Nucleus.getNucleus()),
                dataObject,
                event.getProfile()
        );
        Sponge.getEventManager().post(eventToFire);
        if (eventToFire.shouldSave()) {
            this.serviceCollection.storageManager().getUserService().save(userId, dataObject);
        }
    }

    /* (non-Javadoc)
     * We do this last to avoid interfering with other modules.
     */
    @Listener(order = Order.LATE)
    public void onPlayerLoginLast(final ClientConnectionEvent.Login event, @Getter("getProfile") GameProfile profile,
        @Getter("getTargetUser") User user) {

        IUserDataObject udo = this.serviceCollection.storageManager().getUserService().getOrNewOnThread(user.getUniqueId());

        if (event.getFromTransform().equals(event.getToTransform())) {
            // Check this
            NucleusOnLoginEvent onLoginEvent =
                    CauseStackHelper.createFrameWithCausesWithReturn(cause ->
                            new NucleusOnLoginEvent(cause, user, udo, event.getFromTransform()), profile);

            Sponge.getEventManager().post(onLoginEvent);
            if (onLoginEvent.getTo().isPresent()) {
                event.setToTransform(onLoginEvent.getTo().get());
            }
        }

        this.serviceCollection.userCacheService().updateCacheForPlayer(user.getUniqueId(), udo);
    }

    /* (non-Javadoc)
     * We do this first to try to get the first play status as quick as possible.
     */
    @Listener(order = Order.FIRST)
    public void onPlayerJoinFirst(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        try {
            IUserDataObject qsu = this.serviceCollection.storageManager().getUserService().getOrNewOnThread(player.getUniqueId());
            qsu.set(CoreKeys.LAST_LOGIN, Instant.now());
            if (Nucleus.getNucleus().isServer()) {
                qsu.set(CoreKeys.IP_ADDRESS, player.getConnection().getAddress().getAddress().toString());
            }

            // We'll do this bit shortly - after the login events have resolved.
            final String name = player.getName();
            Task.builder().execute(() -> qsu.set(CoreKeys.LAST_KNOWN_NAME, name)).delayTicks(20L).submit(Nucleus.getNucleus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onPlayerJoinLast(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        // created before
        if (!this.serviceCollection.storageManager().getUserService().getOnThread(player.getUniqueId())
                .map(x -> x.get(CoreKeys.FIRST_JOIN)).isPresent()) {
            this.serviceCollection.getServiceUnchecked(UniqueUserService.class).resetUniqueUserCount();

            NucleusFirstJoinEvent firstJoinEvent = new OnFirstLoginEvent(
                event.getCause(), player, event.getOriginalChannel(), event.getChannel().orElse(null), event.getOriginalMessage(),
                    event.isMessageCancelled(), event.getFormatter());

            Sponge.getEventManager().post(firstJoinEvent);
            event.setChannel(firstJoinEvent.getChannel().get());
            event.setMessageCancelled(firstJoinEvent.isMessageCancelled());
            this.serviceCollection.storageManager().getUserService()
                    .getOrNew(player.getUniqueId())
                    .thenAccept(x -> x.set(CoreKeys.FIRST_JOIN, x.get(CoreKeys.LAST_LOGIN).orElseGet(Instant::now)));
        }

        // Warn about wildcard.
        if (!this.serviceCollection.permissionService().isOpOnly() && player.hasPermission("nucleus")) {
            IMessageProviderService provider = serviceCollection.messageProvider();
            Nucleus.getNucleus().getLogger().warn("The player " + player.getName() + " has got either the nucleus wildcard or the * wildcard "
                    + "permission. This may cause unintended side effects.");

            if (this.warnOnWildcard) {
                // warn
                List<Text> text = Lists.newArrayList();
                text.add(provider.getMessageFor(player, "core.permission.wildcard2"));
                text.add(provider.getMessageFor(player, "core.permission.wildcard3"));
                if (this.url != null) {
                    text.add(
                            provider.getMessageFor(player, "core.permission.wildcard4").toBuilder()
                                    .onClick(TextActions.openUrl(this.url)).build()
                    );
                }
                text.add(provider.getMessageFor(player, "core.permission.wildcard5"));
                Sponge.getServiceManager().provideUnchecked(PaginationService.class)
                        .builder()
                        .title(provider.getMessageFor(player, "core.permission.wildcard"))
                        .contents(text)
                        .padding(Text.of(TextColors.GOLD, "-"))
                        .sendTo(player);
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onPlayerQuit(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        // There is an issue in Sponge where the connection may not even exist, because they were disconnected before the connection was
        // completely established.
        //noinspection ConstantConditions
        if (player.getConnection() == null || player.getConnection().getAddress() == null) {
            return;
        }

        this.serviceCollection.storageManager().getUser(player.getUniqueId()).thenAccept(x -> x.ifPresent(y -> onPlayerQuit(player, y)));

    }

    private void onPlayerQuit(Player player, IUserDataObject udo) {
        final InetAddress address = player.getConnection().getAddress().getAddress();

        try {
            udo.set(CoreKeys.IP_ADDRESS, address.toString());
            this.serviceCollection.userCacheService().updateCacheForPlayer(player.getUniqueId(), udo);
            this.serviceCollection.storageManager().saveUser(player.getUniqueId(), udo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        CoreConfig c = this.serviceCollection.moduleDataProvider().getModuleConfig(CoreConfig.class);
        this.getKickOnStopMessage = c.isKickOnStop() ? c.getKickOnStopMessage() : null;
        this.warnOnWildcard = c.isCheckForWildcard();
    }

    @Listener
    public void onServerAboutToStop(final GameStoppingServerEvent event) {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            this.serviceCollection.storageManager().getUserOnThread(player.getUniqueId()).ifPresent(x -> onPlayerQuit(player, x));
        }

        if (this.getKickOnStopMessage != null) {
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                Text msg = this.getKickOnStopMessage.getForCommandSource(p);
                if (msg.isEmpty()) {
                    p.kick();
                } else {
                    p.kick(msg);
                }
            }
        }

    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        CommandSource requester = event.getCause().first(CommandSource.class).orElse(Sponge.getServer().getConsole());
        IMessageProviderService messageProviderService = this.serviceCollection.messageProvider();
        try {
            this.serviceCollection.reloadableService().fireReloadables(this.serviceCollection);
            requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ",
                    messageProviderService.getMessageFor(requester, "command.reload.one")));
            requester.sendMessage(Text.of(TextColors.YELLOW, "[Nucleus] ",
                    messageProviderService.getMessageFor(requester, "command.reload.two")));
        } catch (Exception e) {
            e.printStackTrace();
            requester.sendMessage(Text.of(TextColors.RED, "[Nucleus] ",
                    messageProviderService.getMessageFor(requester, "command.reload.errorone")));
        }
    }
}
