/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.config.CommandModifiersConfig;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandControl;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.storage.util.ThrownSupplier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public abstract class CommandContextImpl<P extends CommandSource> implements ICommandContext.Mutable<P> {

    private final INucleusServiceCollection serviceCollection;
    private final String commandkey;
    private double cost = 0;
    private int cooldown = 0;
    private int warmup = 0;
    private final Cause cause;
    final CommandContext context;
    private final ThrownSupplier<P, CommandException> source;
    private final Set<CommandModifier> modifiers;
    private final ArrayList<Consumer<P>> failActions = new ArrayList<>();

    CommandContextImpl(Cause cause,
            CommandContext context,
            INucleusServiceCollection serviceCollection,
            ThrownSupplier<P, CommandException> source,
            P sourceDirect,
            CommandControl control,
            Collection<CommandModifier> modifiers) {
        this.cause = cause;
        this.commandkey = control.getCommandKey();
        this.context = context;
        this.source = source;
        this.serviceCollection = serviceCollection;
        this.cost = control.getCost(sourceDirect);
        this.cooldown = control.getCooldown(sourceDirect);
        this.warmup = control.getWarmup(sourceDirect);
        this.modifiers = new HashSet<>(modifiers);
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public String getCommandKey() {
        return this.commandkey;
    }

    @Override
    public P getCommandSource() throws CommandException {
        return this.source.get();
    }

    @Override
    public <T> Optional<T> getOne(String name, Class<T> clazz) {
        return this.context.getOne(name);
    }

    @Override
    public boolean hasAny(String name) {
        return this.context.hasAny(name);
    }

    @Override
    public <T> Collection<T> getAll(String name, Class<T> clazz) {
        return this.context.getAll(name);
    }

    @Override public <T> Optional<T> getOne(String name, TypeToken<T> clazz) {
        return this.context.getOne(name);
    }

    @Override public <T> Collection<T> getAll(String name, TypeToken<T> clazz) {
        return this.context.getAll(name);
    }

    @Override public <T> T requireOne(String name, TypeToken<T> clazz) {
        return this.context.requireOne(name);
    }

    @Override
    public <T> T requireOne(String name, Class<T> clazz) {
        return this.context.requireOne(name);
    }

    @Override
    public Player getPlayerFromArgs(String key, String errorKey) throws NoSuchElementException {
        return getOne(key, Player.class).orElseGet(() -> {
            try {
                return getIfPlayer(errorKey);
            } catch (CommandException e) {
                throw new NoSuchElementException();
            }
        });
    }

    @Override
    public P getCommandSourceUnchecked() {
        try {
            return this.source.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Player getCommandSourceAsPlayerUnchecked() {
        try {
            return (Player) this.source.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User getUserFromArgs(String key, String errorKey) throws NoSuchElementException {
        return getOne(key, User.class).orElseGet(() -> {
            try {
                return getIfPlayer(errorKey);
            } catch (CommandException e) {
                throw new NoSuchElementException();
            }
        });
    }

    @Override
    public int getCooldown() {
        return this.cooldown;
    }

    @Override
    public void setCooldown(int cooldown) {
        this.cooldown = Math.max(cooldown, 0);
    }

    @Override
    public double getCost() {
        return this.cost;
    }

    @Override
    public void setCost(double cost) {
        this.cost = Math.max(cost, 0);
    }

    @Override
    public <T> void put(String name, Class<T> clazz, T obj) {
        this.context.putArg(name, obj);
    }

    @Override
    public <T> void putAll(String name, Class<T> clazz, Collection<? extends T> obj) {
        for (T o : obj) {
            this.context.putArg(name, o);
        }
    }

    @Override
    public ICommandResult successResult() {
        return CommandResultImpl.SUCCESS;
    }

    @Override
    public ICommandResult failResult() {
        return CommandResultImpl.FAILURE;
    }

    @Override public ICommandResult getResultFromBoolean(boolean success) {
        if (success) {
            return successResult();
        }
        return failResult();
    }

    public ICommandResult errorResultLiteral(Text message) {
        return new CommandResultImpl.Literal(message);
    }

    @Override
    public ICommandResult errorResult(String key, Object... args) {
        return new CommandResultImpl(this.serviceCollection.messageProvider(), key, args);
    }

    @Override
    public CommandException createException(Throwable th, String key, Object... args) {
        Optional<? extends CommandSource> c = this.source.asOptional();
        CommandSource source;
        if (c.isPresent()) {
            source = c.get();
        } else {
            source = Sponge.getServer().getConsole();
        }

        return new CommandException(
                this.serviceCollection.messageProvider().getMessageFor(source, key, args),
                th
        );
    }

    @Override
    public CommandException createException(String key, Object... args) {
        Optional<? extends CommandSource> c = this.source.asOptional();
        CommandSource source;
        if (c.isPresent()) {
            source = c.get();
        } else {
            source = Sponge.getServer().getConsole();
        }

        return new CommandException(
                this.serviceCollection.messageProvider().getMessageFor(source, key, args)
        );
    }

    @Override
    public INucleusServiceCollection getServiceCollection() {
        return this.serviceCollection;
    }

    @Override public Collection<CommandModifier> modifiers() {
        return ImmutableList.copyOf(this.modifiers);
    }

    @Override
    public void removeModifier(CommandModifiers modifier) {
        this.modifiers.removeIf(x -> x.value() == modifier);
    }

    @Override public Collection<Consumer<P>> failActions() {
        return ImmutableList.copyOf(this.failActions);
    }

    @Override public void addFailAction(Consumer<P> action) {
        this.failActions.add(action);
    }

    @Override public int getWarmup() {
        return this.warmup;
    }

    @Override public void setWarmup(int warmup) {
        this.warmup = warmup;
    }

    @Override public boolean testPermission(String permission) {
        return testPermissionFor(getCommandSourceUnchecked(), permission);
    }

    @Override public boolean testPermissionFor(Subject subject, String permission) {
        return this.serviceCollection.permissionService().hasPermission(subject, permission);
    }

    @Override public String getMessageString(String key, Object... replacements) {
        return getMessageStringFor(getCommandSourceUnchecked(), key, replacements);
    }

    @Override public String getMessageStringFor(CommandSource to, String key, Object... replacements) {
        return this.serviceCollection.messageProvider().getMessageString(to.getLocale(), key, replacements);
    }

    @Override public Text getMessageFor(CommandSource to, String key, Object... replacements) {
        return this.serviceCollection.messageProvider().getMessageFor(getCommandSourceUnchecked(), key, replacements);
    }

    @Override public Text getMessage(String key, Object... replacements) {
        return getMessageFor(getCommandSourceUnchecked(), key, replacements);
    }

    @Override public String getTimeString(long seconds) {
        return this.serviceCollection.messageProvider().getTimeString(getCommandSourceUnchecked().getLocale(), seconds);
    }

    @Override public void sendMessage(String key, Object... replacements) {
        sendMessageTo(getCommandSourceUnchecked(), key, replacements);
    }

    @Override public void sendMessageText(Text message) {
        getCommandSourceUnchecked().sendMessage(message);
    }

    @Override public void sendMessageTo(MessageReceiver source, String key, Object... replacements) {
        this.serviceCollection.messageProvider().sendMessageTo(source, key, replacements);
    }

    @Override public boolean is(CommandSource other) {
        return this.source.getUnchecked().equals(other);
    }

    @Override public boolean is(Class<?> other) {
        return other.isInstance(this.source.getUnchecked());
    }

    @Override public boolean isConsoleAndBypass() {
        return false;
    }

    @Override public boolean is(User x) {
        return false;
    }

    @Override public Optional<WorldProperties> getWorldPropertiesOrFromSelf(String worldKey) {
        Optional<WorldProperties> optionalWorldProperties = this.context.getOne(worldKey);
        if (!optionalWorldProperties.isPresent()) {
            CommandSource source = getCommandSourceUnchecked();
            if (source instanceof Locatable) {
                return Optional.of(((Locatable) source).getWorld().getProperties());
            }
        }

        return Optional.empty();
    }

    @Override public Text getDisplayName() {
        return this.getServiceCollection().playerDisplayNameService().getDisplayName(getCommandSourceUnchecked());
    }

    @Override public Text getDisplayName(UUID uuid) {
        return this.getServiceCollection().playerDisplayNameService().getDisplayName(uuid);
    }

    @Override public String getName() {
        return this.getCommandSourceUnchecked().getName();
    }

    public static class Any extends CommandContextImpl<CommandSource> {

        public Any(Cause cause,
                CommandContext context,
                INucleusServiceCollection serviceCollection,
                CommandSource target,
                CommandControl control,
                CommandModifiersConfig modifiersConfig,
                Collection<CommandModifier> modifiers) throws CommandException {
            super(cause, context, serviceCollection, () -> target, target, control, modifiers);
        }

        @Override public Optional<UUID> getUniqueId() {
            return Optional.empty();
        }

        @Override
        public Player getIfPlayer(String errorKey) throws CommandException {
            if (getCommandSource() instanceof Player) {
                return (Player) getCommandSource();
            }

            throw new CommandException(
                    this.getServiceCollection().messageProvider().getMessageFor(getCommandSource(), errorKey)
            );
        }

        @Override
        public boolean is(User x) {
            try {
                CommandSource source = getCommandSource();
                if (source instanceof Player) {
                    return ((Player) source).getUniqueId().equals(x.getUniqueId());
                }

                return false;
            } catch (CommandException e) {
                return false;
            }
        }

        @Override public boolean isUser() {
            return getCommandSourceUnchecked() instanceof User;
        }
    }


    public static class Console extends CommandContextImpl<ConsoleSource> {

        private final boolean isBypass;

        public Console(Cause cause,
                CommandContext context,
                INucleusServiceCollection serviceCollection,
                ConsoleSource target,
                CommandControl control,
                CommandModifiersConfig modifiersConfig,
                Collection<CommandModifier> modifiers,
                boolean isBypass) throws CommandException {
            super(cause, context, serviceCollection, () -> target, target, control, modifiers);
            this.isBypass = isBypass;
        }

        @Override public Optional<UUID> getUniqueId() {
            return Optional.empty();
        }

        @Override
        public Player getIfPlayer(String errorKey) throws CommandException {
            throw new CommandException(
                    this.getServiceCollection().messageProvider().getMessageFor(getCommandSource(), errorKey)
            );
        }

        @Override public boolean is(Class<?> other) {
            return ConsoleSource.class.isAssignableFrom(other);
        }

        @Override public boolean isUser() {
            return false;
        }

        @Override public boolean isConsoleAndBypass() {
            return this.isBypass;
        }

        @Override public Optional<WorldProperties> getWorldPropertiesOrFromSelf(String worldKey) {
            return this.context.getOne(worldKey);
        }
    }

    public static class PlayerSource extends CommandContextImpl<Player> {

        private final UUID uuid;

        public PlayerSource(Cause cause,
                CommandContext context,
                INucleusServiceCollection serviceCollection,
                ThrownSupplier<Player, CommandException> source,
                Player player,
                CommandControl control,
                CommandModifiersConfig modifiersConfig,
                Collection<CommandModifier> modifiers) throws CommandException {
            super(cause, context, serviceCollection, source, player, control, modifiers);
            this.uuid = source.asOptional().map(Identifiable::getUniqueId).get();
        }

        @Override
        public boolean is(Class<?> other) {
            return Player.class.isAssignableFrom(other);
        }

        @Override
        public boolean is(User x) {
            return x.getUniqueId().equals(this.uuid);
        }

        @Override public boolean isUser() {
            return true;
        }

        @Override
        public Optional<WorldProperties> getWorldPropertiesOrFromSelf(String worldKey) {
            Optional<WorldProperties> worldProperties = this.context.getOne(worldKey);
            if (!worldProperties.isPresent()) {
                return Optional.of(this.getCommandSourceAsPlayerUnchecked().getWorld().getProperties());
            }

            return Optional.empty();
        }

        @Override
        public Optional<UUID> getUniqueId() {
            return Optional.of(this.uuid);
        }

        @Override
        public Player getIfPlayer(String errorKey) throws CommandException {
            return getCommandSource();
        }
    }

}
