/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands.nucleus;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Key;
import io.github.nucleuspowered.nucleus.guice.DataDirectory;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.CommandModifier;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandControl;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandMetadata;
import io.github.nucleuspowered.nucleus.internal.command.requirements.CommandModifiers;
import io.github.nucleuspowered.nucleus.modules.core.CorePermissions;
import io.github.nucleuspowered.nucleus.modules.core.commands.NucleusCommand;
import io.github.nucleuspowered.nucleus.modules.core.docgen.CommandDoc;
import io.github.nucleuspowered.nucleus.modules.core.docgen.EssentialsDoc;
import io.github.nucleuspowered.nucleus.modules.core.docgen.PermissionDoc;
import io.github.nucleuspowered.nucleus.modules.core.docgen.TokenDoc;
import io.github.nucleuspowered.nucleus.services.ICommandMetadataService;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.yaml.snakeyaml.DumperOptions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Intended as a local command.
 */
@SuppressWarnings("UnstableApiUsage")
@Command(
        aliases = "docgen",
        basePermission = CorePermissions.BASE_DOCGEN,
        commandDescriptionKey = "docgen",
        parentCommand = NucleusCommand.class
)
@NonnullByDefault
public class DocGenCommand implements ICommandExecutor<CommandSource> {

    private final TypeToken<List<CommandDoc>> ttlcd = new TypeToken<List<CommandDoc>>() {};
    private final TypeToken<List<PermissionDoc>> ttlpd = new TypeToken<List<PermissionDoc>>() {};
    private final TypeToken<List<TokenDoc>> ttltd = new TypeToken<List<TokenDoc>>() {};
    private final TypeToken<List<EssentialsDoc>> tted = new TypeToken<List<EssentialsDoc>>() {};

    private final IPermissionService permissionService;
    private final IMessageProviderService messageProviderService;

    @Inject
    public DocGenCommand(INucleusServiceCollection serviceCollection) {
        this.permissionService = serviceCollection.permissionService();
        this.messageProviderService = serviceCollection.messageProvider();
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        context.sendMessage("command.nucleus.docgen.start");

        INucleusServiceCollection serviceCollection = context.getServiceCollection();
        ICommandMetadataService commandMetadataService = serviceCollection.commandMetadataService();
        IPermissionService permissionService = serviceCollection.permissionService();

        Path dataPath = serviceCollection.injector().getInstance(Key.get(Path.class, DataDirectory.class));

        Collection<CommandControl> commands = commandMetadataService.getCommands();

        final List<EssentialsDoc> essentialsDocs = new ArrayList<>();
        List<CommandDoc> lcd = getAndSort(
                commands,
                (CommandControl first, CommandControl second) -> {
                    int m = first.getMetadata().getModuleid().compareToIgnoreCase(second.getMetadata().getModuleid());
                    if (m == 0) {
                        return first.getCommandKey().compareToIgnoreCase(second.getCommandKey());
                    }

                    return m;
                },
                control -> {
                    CommandMetadata metadata = control.getMetadata();
                    CommandDoc commandDoc = new CommandDoc();
                    String cmdPath = metadata.getCommandKey().replaceAll("\\.", " ");
                    commandDoc.setCommandName(cmdPath);

                    if (metadata.isRoot()) {
                        commandDoc.setAliases(String.join(", ", commandDoc.getAliases()));
                    } else {
                        String key = metadata.getCommandKey().replaceAll("\\.[a-z]+$", "").replaceAll("\\.", " ");
                        commandDoc.setAliases(key + String.join(", " + key, commandDoc.getAliases()));
                        if (!metadata.getRootAliases().isEmpty()) {
                            commandDoc.setRootAliases(String.join(", ", commandDoc.getRootAliases()));
                        }
                    }

                    Set<PermissionDoc> permissionDocs = new HashSet<>();
                    Command annotation = metadata.getCommandAnnotation();
                    for (CommandModifier modifier : annotation.modifiers()) {
                        if (modifier.value() == CommandModifiers.HAS_COOLDOWN) {
                            commandDoc.setCooldown(true);
                            getPermissionDoc(modifier.exemptPermission()).ifPresent(permissionDocs::add);
                        } else if (modifier.value() == CommandModifiers.HAS_COST) {
                            commandDoc.setCost(true);
                            getPermissionDoc(modifier.exemptPermission()).ifPresent(permissionDocs::add);
                        } else if (modifier.value() == CommandModifiers.HAS_WARMUP) {
                            commandDoc.setWarmup(true);
                            getPermissionDoc(modifier.exemptPermission()).ifPresent(permissionDocs::add);
                        }
                    }

                    EssentialsEquivalent essentialsEquivalent = metadata.getEssentialsEquivalent();
                    if (essentialsEquivalent != null) {
                        List<String> eqiv = Arrays.asList(essentialsEquivalent.value());
                        commandDoc.setEssentialsEquivalents(eqiv);
                        commandDoc.setEssNotes(essentialsEquivalent.notes());
                        commandDoc.setExactEssEquiv(essentialsEquivalent.isExact());
                        essentialsDocs.add(
                                new EssentialsDoc()
                                        .setExact(essentialsEquivalent.isExact())
                                        .setNotes(essentialsEquivalent.notes())
                                        .setEssentialsCommands(eqiv)
                                        .setNucleusEquiv(metadata.getRootAliases())
                        );
                    }

                    String[] base = metadata.getCommandAnnotation().basePermission();
                    if (base.length > 0) {
                        commandDoc.setPermissionbase(base[0]);
                        for (String permission : base) {
                            getPermissionDoc(permission).ifPresent(permissionDocs::add);
                        }
                    }

                    commandDoc.setOneLineDescription(control.getShortDescription(Sponge.getServer().getConsole()).toString());
                    commandDoc.setExtendedDescription(control.getHelp(Sponge.getServer().getConsole()).toString());
                    commandDoc.setUsageString(control.getUsage(Sponge.getServer().getConsole()).toString());
                    commandDoc.setPermissions(new ArrayList<>(permissionDocs));
                    commandDoc.setSimpleUsage(control.getUsageText(Sponge.getServer().getConsole(), cmdPath).toPlain());

                    return commandDoc;
                });

        List<PermissionDoc> permdocs = permissionService.getAllMetadata()
                .stream()
                .map(this::getFor)
                .sorted(Comparator.comparing(PermissionDoc::getPermission))
                .collect(Collectors.toList());

        List<TokenDoc> tokenDocs = serviceCollection.messageTokenService()
                .getNucleusTokenParser()
                .getTokenNames()
                .stream()
                .filter(x -> this.messageProviderService.hasKey("nucleus.token." + x.toLowerCase()))
                .map(x -> new TokenDoc().setName(x).setDescription(this.messageProviderService.getMessageString("nucleus.token." + x.toLowerCase())))
                .collect(Collectors.toList());

        // Now do all the saving
        try {
            // Config files
            Map<String, Class<?>> configs = serviceCollection
                    .moduleDataProvider()
                    .getModuleToConfigType();
            ConfigurationNode configNode = SimpleConfigurationNode.root();
            for (Map.Entry<String, Class<?>> entry : configs.entrySet()) {
                try {
                    configNode.getNode(entry.getKey())
                            .setValue(createConfigString(context, entry.getValue().newInstance()));
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            // Generate command file.
            YAMLConfigurationLoader configurationLoader = YAMLConfigurationLoader.builder()
                    .setPath(dataPath.resolve("commands.yml"))
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();
            ConfigurationNode commandConfigurationNode = SimpleConfigurationNode.root().setValue(this.ttlcd, lcd);
            configurationLoader.save(commandConfigurationNode);

            YAMLConfigurationLoader permissionsConfigurationLoader = YAMLConfigurationLoader.builder()
                    .setPath(dataPath.resolve("permissions.yml"))
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();
            ConfigurationNode permissionConfiguationNode = SimpleConfigurationNode.root().setValue(this.ttlpd, permdocs);
            permissionsConfigurationLoader.save(permissionConfiguationNode);

            YAMLConfigurationLoader essentialsConfigurationLoader = YAMLConfigurationLoader.builder()
                    .setPath(dataPath.resolve("essentials.yml"))
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();
            ConfigurationNode essentialsConfigurationNode = SimpleConfigurationNode.root().setValue(this.tted, essentialsDocs);
            essentialsConfigurationLoader.save(essentialsConfigurationNode);

            YAMLConfigurationLoader configurationConfigurationLoader = YAMLConfigurationLoader.builder()
                    .setPath(dataPath.resolve("conf.yml"))
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();
            configurationConfigurationLoader.save(configNode);

            YAMLConfigurationLoader tokensConfigurationLoader = YAMLConfigurationLoader.builder()
                    .setPath(dataPath.resolve("tokens.yml"))
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK).build();
            ConfigurationNode tokensConfigNode = SimpleConfigurationNode.root().setValue(this.ttltd, tokenDocs);
            tokensConfigurationLoader.save(tokensConfigNode);

        } catch (ObjectMappingException | IOException e) {
            throw new CommandException(Text.of("Could not save docs"), e);
        }

        context.sendMessage("command.nucleus.docgen.complete");
        return context.successResult();
    }

    private <T, R> List<R> getAndSort(
            Collection<T> list,
            Comparator<T> comparator,
            Function<T, R> mapper) {
        return list.stream().sorted(comparator).map(mapper).collect(Collectors.toList());
    }

    private Optional<PermissionDoc> getPermissionDoc(String permission) {
        return this.permissionService.getMetadataFor(permission).map(this::getFor);
    }

    private PermissionDoc getFor(IPermissionService.Metadata metadata) {
        return new PermissionDoc()
                .setDefaultLevel(metadata.getSuggestedLevel().getRole())
                .setDescription(metadata.getDescription(this.messageProviderService))
                .setModule(metadata.getModuleId());
    }

    private String createConfigString(ICommandContext<? extends CommandSource> context, Object obj) throws IOException {
        try (StringWriter sw = new StringWriter(); BufferedWriter writer = new BufferedWriter(sw)) {
            HoconConfigurationLoader hcl = HoconConfigurationLoader.builder()
                    .setDefaultOptions(context.getServiceCollection().configurateHelper().setOptions(ConfigurationOptions.defaults()))
                    .setSink(() -> writer)
                    .build();
            CommentedConfigurationNode cn = hcl.createEmptyNode(context.getServiceCollection().configurateHelper().setOptions(hcl.getDefaultOptions()));
                applyToNode(obj.getClass(), obj, cn);
            hcl.save(cn);
            return sw.toString();
        }
    }

    private <T> void applyToNode(Class<T> c, Object object, ConfigurationNode node) {
        try {
            node.setValue(TypeToken.of(c), c.cast(object));
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

}
