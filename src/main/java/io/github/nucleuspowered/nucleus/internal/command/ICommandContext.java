/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.storage.WorldProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface ICommandContext<C extends CommandSource> {

    Cause getCause();

    String getCommandKey();

    C getCommandSource() throws CommandException;

    C getCommandSourceUnchecked();

    Optional<UUID> getUniqueId();

    /**
     * Gets the current cooldown for the command
     *
     * @return
     */
    int getCooldown();

    void setCooldown(int cooldown);

    double getCost();

    void setCost(double cost);

    int getWarmup();

    /**
     * Sets the warmup in seconds.
     *
     * @param warmup The warmup in seconds
     */
    void setWarmup(int warmup);

    Player getPlayerFromArgs(String key, String errorKey) throws NoSuchElementException, CommandException;

    default Player getPlayerFromArgs() throws NoSuchElementException, CommandException {
        return getPlayerFromArgs(NucleusParameters.Keys.PLAYER, "command.playeronly");
    }

    Player getCommandSourceAsPlayerUnchecked();

    default User getUserFromArgs() throws NoSuchElementException, CommandException {
        return getUserFromArgs(NucleusParameters.Keys.USER, "command.playeronly");
    }

    User getUserFromArgs(String key, String errorKey) throws NoSuchElementException, CommandException;

    boolean hasAny(String name);

    <T> Optional<T> getOne(String name, Class<T> clazz);

    <T> Optional<T> getOne(String name, TypeToken<T> clazz);

    <T> Collection<T> getAll(String name, Class<T> clazz);

    <T> Collection<T> getAll(String name, TypeToken<T> clazz);

    <T> T requireOne(String name, Class<T> clazz);

    <T> T requireOne(String name, TypeToken<T> clazz);

    INucleusServiceCollection getServiceCollection();

    ICommandResult successResult();

    ICommandResult failResult();

    ICommandResult errorResultLiteral(Text message);

    ICommandResult errorResult(String key, Object... args);

    ICommandResult getResultFromBoolean(boolean success);

    CommandException createException(Throwable ex, String key, Object... args);

    CommandException createException(String key, Object... args);

    default Player getIfPlayer() throws CommandException {
        return getIfPlayer("command.playeronly");
    }

    Player getIfPlayer(String errorKey) throws CommandException;

    Collection<CommandModifier> modifiers();

    Collection<Consumer<C>> failActions();

    boolean testPermission(String permission);

    boolean testPermissionFor(Subject subject, String permission);

    String getMessageString(String key, Object... replacements);

    String getMessageStringFor(CommandSource to, String key, Object... replacements);

    Text getMessage(String key, Object... replacements);

    Text getMessageFor(CommandSource to, String key, Object... replacements);

    default String getTimeString(Duration duration) {
        return getTimeString(duration.getSeconds());
    }

    String getTimeString(long seconds);

    /**
     * Sends a message to the command invoker.
     *
     * @param key The translation key
     * @param replacements The replacements
     */
    void sendMessage(String key, Object... replacements);

    void sendMessageText(Text message);

    void sendMessageTo(MessageReceiver to, String key, Object... replacements);

    default boolean is(Player other) {
        return is(other.getCommandSource().get());
    }

    boolean is(CommandSource other);

    boolean is(Class<?> other);

    boolean is(User x);

    boolean isUser();

    boolean isConsoleAndBypass();

    Optional<WorldProperties> getWorldPropertiesOrFromSelf(String worldKey);

    String getName();

    Text getDisplayName();

    Text getDisplayName(UUID uuid);

    default String getTimeToNowString(Instant endTime) {
        return getTimeString(Duration.between(Instant.now(), endTime).abs());
    }

    interface Mutable<C extends CommandSource> extends ICommandContext<C> {

        <T> void put(String name, Class<T> clazz, T obj);

        <T> void putAll(String name, Class<T> clazz, Collection<? extends T> obj);

        void removeModifier(CommandModifiers modifier);

        void addFailAction(Consumer<C> action);

    }

}
