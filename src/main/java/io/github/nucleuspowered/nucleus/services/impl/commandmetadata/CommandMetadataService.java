/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.commandmetadata;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.guice.ConfigDirectory;
import io.github.nucleuspowered.nucleus.internal.TypeTokens;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandInterceptor;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandControl;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandMetadata;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.services.ICommandMetadataService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IReloadableService;
import io.github.nucleuspowered.nucleus.util.PrettyPrinter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandSource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@SuppressWarnings("UnstableApiUsage")
@Singleton
public class CommandMetadataService implements ICommandMetadataService, Reloadable {

    private static final String ALIASES = "aliases";
    private static final String ENABLED = "enabled";
    private static final TypeToken<Map<String, Boolean>> MAP_TYPE_TOKEN = new TypeToken<Map<String, Boolean>>() {};

    private final Logger logger;
    private final Map<String, String> commandremap = new HashMap<>();
    private final Path commandsFile;
    private boolean shouldReload = true;
    private final Map<String, CommandMetadata> commandMetadataMap = new HashMap<>();
    private final Map<CommandControl, List<String>> controlToAliases = new HashMap<>();
    private final Map<Class<? extends ICommandExecutor<? extends CommandSource>>, CommandControl> controlToExecutorClass = new HashMap<>();

    private CommentedConfigurationNode commandsConfConfigNode;
    private boolean registrationComplete = false;
    private List<ICommandInterceptor> interceptors = new ArrayList<>();
    private List<String> registeredAliases = new ArrayList<>();

    @Inject
    public CommandMetadataService(@ConfigDirectory Path configDirectory, IReloadableService reloadableService, Logger logger) {
        reloadableService.registerReloadable(this);
        this.commandsFile = configDirectory.resolve("commands.conf");
        this.logger = logger;
    }

    private String getKey(Command command) {
        return getKey(new StringBuilder(), command).toString().toLowerCase();
    }

    private StringBuilder getKey(
            StringBuilder stringBuilder,
            Command command) {
        if (command.parentCommand() != ICommandExecutor.class) {
            getKey(stringBuilder, command.parentCommand().getAnnotation(Command.class)).append(".");
        }

        return stringBuilder.append(command.aliases()[0]);
    }

    @Override
    public void registerCommand(
            String id,
            String name,
            Command command,
            Class<? extends ICommandExecutor<?>> associatedContext
    ) {
        Preconditions.checkState(!this.registrationComplete, "Registration has completed.");
        String key = getKey(command);
        this.commandMetadataMap.put(key, new CommandMetadata(
                id,
                name,
                command,
                associatedContext,
                key,
                associatedContext.getAnnotation(EssentialsEquivalent.class)
        ));
    }

    /**
     * This is where the magic happens with registering commands. We need to:
     *
     * <ol>
     *     <li>Update command.conf</li>
     *     <li>Sift through and get the aliases to register.</li>
     *     <li>Register "root" aliases</li>
     *     <li>Then subcommands... obviously.</li>
     * </ol>
     */
    @Override
    public void completeRegistrationPhase(final INucleusServiceCollection serviceCollection) {
        Preconditions.checkState(!this.registrationComplete, "Registration has completed.");
        this.registrationComplete = true;
        load();

        Map<Class<? extends ICommandExecutor<?>>, String> metadataStringMap = this.commandMetadataMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> entry.getValue().getExecutor(),
                        Map.Entry::getKey
                ));

        Map<CommandMetadata, CommandControl> commands = new HashMap<>();
        // this.commandMetadataMap.values().forEach(metadata -> execToMeta.put(metadata.getExecutor(), metadata));

        Map<Class<? extends ICommandExecutor>, Map<String, CommandMetadata>> toRegister = new HashMap<>();
        toRegister.put(ICommandExecutor.class, new HashMap<>());

        // We need aliases out
        for (CommandMetadata metadata : this.commandMetadataMap.values()) {
            if (!getValue(ENABLED, TypeTokens.BOOLEAN, () -> true, metadata.getCommandKey())) {
                // then continue, we're not loading it.
                continue;
            }

            String prefix = metadata.isRoot() ? "" : metadataStringMap.get(metadata.getCommandAnnotation().parentCommand()).replace(".", " ");
            Map<String, Boolean> map = getAliasMap(metadata.getCommandKey());
            List<String> expected = new ArrayList<>();
            for (String alias : metadata.getRootAliases()) {
                map.putIfAbsent(alias, true);
                expected.add(alias);
            }

            for (String alias : metadata.getAtLevelAliases()) {
                String aa = prefix + " " + alias;
                map.putIfAbsent(aa, true);
                expected.add(aa);
            }

            // Set it back
            map.keySet().removeIf(s -> !expected.contains(s));

            // Add the aliases to the commands config.
            ConfigurationNode node = this.commandsConfConfigNode.getNode(metadata.getCommandKey());
            node.getNode(ENABLED).setValue(false);
            node.getNode(ALIASES).setValue(map);

            map.entrySet().stream().filter(Map.Entry::getValue).forEach(x -> {
                if (x.getKey().contains(" ")) {
                    // metadata
                    toRegister.get(ICommandExecutor.class).put(x.getKey(), metadata);
                } else {
                    toRegister.computeIfAbsent(
                            metadata.getCommandAnnotation().parentCommand(),
                            n -> new HashMap<>())
                        .putIfAbsent(x.getKey().substring(x.getKey().lastIndexOf(" ")), metadata);
                }
            });
        }

        // Now for mappings
        Set<String> toRemove = new HashSet<>();
        for (Map.Entry<String, String> entry : this.commandremap.entrySet()) {
            CommentedConfigurationNode node = this.commandsConfConfigNode.getNode(entry.getKey()).getNode("enabled");
            if (node.isVirtual()) {
                node.setValue(true).setComment(serviceCollection.messageProvider().getMessageString("config.enabled"));
            } else if (!node.getBoolean(true)) {
                // remove from mapping
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(this.commandremap::remove);
        save();

        // use aliases to register commands.
        register(toRegister, commands, this.controlToExecutorClass, ICommandExecutor.class, null, serviceCollection);

        // Okay, now we've created our commands, time to update command conf with the modifiers.
        refreshCommandConfig();
        save();
    }

    private <T extends ICommandExecutor> void register(
            Map<Class<? extends ICommandExecutor>, Map<String, CommandMetadata>> toStart,
            Map<CommandMetadata, CommandControl> commands,
            Map<Class<? extends ICommandExecutor<?>>, CommandControl> created,
            Class<T> keyToCheck,
            @Nullable CommandControl parentControl,
            INucleusServiceCollection collection) {

        for (Map.Entry<String, CommandMetadata> entry : toStart.get(keyToCheck).entrySet()) {
            CommandControl control = commands.computeIfAbsent(entry.getValue(), mm -> construct(mm, collection));
            if (created.put(entry.getValue().getExecutor(), control) == null) {
                // then we're creating the child.
                register(toStart, commands, created, entry.getValue().getExecutor(), control, collection);
            }

            if (parentControl == null) {
                this.controlToAliases.computeIfAbsent(control, c -> new ArrayList<>()).add(entry.getKey());
            } else {
                parentControl.attach(entry.getKey(), control);
            }

        }

        // Now we register all root commands as necessary.
        if (parentControl == null) {
            for (Map.Entry<CommandControl, List<String>> aliases : this.controlToAliases.entrySet()) {
                Sponge.getCommandManager().register(collection.pluginContainer(), aliases.getKey(), aliases.getValue())
                    .ifPresent(x -> this.registeredAliases.addAll(x.getAllAliases()));
            }
        }
    }

    private void refreshCommandConfig() {
        this.controlToAliases.keySet().forEach(control -> {
            if (!control.isModifierKeyRedirected()) { // if redirected, another command will deal with this.
                ConfigurationNode node = this.commandsConfConfigNode.getNode(control.getCommandKey());
                for (CommandModifier modifier : control.getCommandModifiers()) {
                    modifier.value().setupConfig(control.getCommandModifiersConfig(), node);
                }
            }
        });

        save();
    }

    private CommandControl construct(CommandMetadata metadata, INucleusServiceCollection serviceCollection) {
        ICommandExecutor<?> executor = serviceCollection.injector().getInstance(metadata.getExecutor());
        return new CommandControl(
                executor,
                metadata,
                serviceCollection
        );
    }

    @Override public void addMapping(String newCommand, String remapped) {
        if (this.commandremap.containsKey(newCommand.toLowerCase())) {
            throw new IllegalArgumentException("command already in use");
        }

        this.commandremap.put(newCommand.toLowerCase(), remapped);
    }

    @Override public void activate() {
        for (Map.Entry<String, String> entry : this.commandremap.entrySet()) {
            if (!Sponge.getCommandManager().get(entry.getKey()).isPresent()) {
                Sponge.getCommandManager().get(entry.getValue()).ifPresent(x ->
                        Sponge.getCommandManager().register(Nucleus.getNucleus(), x.getCallable(), entry.getKey()));
            }
        }
    }

    @Override public void deactivate() {
        for (Map.Entry<String, String> entry : this.commandremap.entrySet()) {
            Optional<? extends CommandMapping> mappingOptional = Sponge.getCommandManager().get(entry.getKey());
            if (mappingOptional.isPresent() &&
                    Sponge.getCommandManager().getOwner(mappingOptional.get()).map(x -> x.getId().equals(PluginInfo.ID)).orElse(false)) {
                Sponge.getCommandManager().removeMapping(mappingOptional.get());
            }
        }
    }

    @Override public Map<String, Boolean> getAliasMap(String command) {
        return getValue(ALIASES, MAP_TYPE_TOKEN, HashMap::new, command);
    }

    @Override public boolean isNucleusCommand(String command) {
        return this.registrationComplete && this.registeredAliases.contains(command.toLowerCase());
    }

    @Override public Optional<CommandControl> getControl(Class<? extends ICommandExecutor<? extends CommandSource>> executorClass) {
        return Optional.ofNullable(this.controlToExecutorClass.get(executorClass));
    }

    @Override public Collection<CommandControl> getCommands() {
        return this.controlToAliases.keySet();
    }

    @Override public void registerInterceptor(ICommandInterceptor impl) {
        this.interceptors.add(impl);
    }

    @Override public Collection<ICommandInterceptor> interceptors() {
        return ImmutableList.copyOf(this.interceptors);
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        // reload the file.
        this.shouldReload = true;
    }

    private <T> T getValue(String node, TypeToken<T> resultType, Supplier<T> empty, String command) {
        if (this.shouldReload || this.commandsConfConfigNode == null) {
            try {
                load();
            } catch (Exception ex) {
                // something bad happened.
                new PrettyPrinter()
                        .add("[Nucleus] Could not load commands.conf")
                        .hr()
                        .add("We could not read your commands.conf file and so things like cooldowns and warmups are not available.")
                        .add("")
                        .add("The error is below. Check that your config file is not malformed.")
                        .hr()
                        .add("Stack trace:")
                        .add(ex)
                        .log(this.logger, Level.ERROR);
                if (this.commandsConfConfigNode == null) {
                    throw new RuntimeException(ex);
                }
            }
        }

        ConfigurationNode cn = this.commandsConfConfigNode.getNode(command).getNode(node);
        if (cn.isVirtual()) {
            return empty.get();
        }

        try {
            return cn.getValue(resultType, empty.get());
        } catch (ObjectMappingException ex) {
            ex.printStackTrace();
            return empty.get();
        }
    }

    private void load() {
        try {
            this.commandsConfConfigNode = HoconConfigurationLoader
                    .builder()
                    .setPath(this.commandsFile)
                    .build()
                    .load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void save() {
        try {
            HoconConfigurationLoader
                    .builder()
                    .setPath(this.commandsFile)
                    .build()
                    .save(this.commandsConfConfigNode);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
