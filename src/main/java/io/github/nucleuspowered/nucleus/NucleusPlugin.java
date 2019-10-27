/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import static io.github.nucleuspowered.nucleus.PluginInfo.DESCRIPTION;
import static io.github.nucleuspowered.nucleus.PluginInfo.ID;
import static io.github.nucleuspowered.nucleus.PluginInfo.NAME;
import static io.github.nucleuspowered.nucleus.PluginInfo.SPONGE_API_VERSION;
import static io.github.nucleuspowered.nucleus.PluginInfo.VERSION;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Injector;
import com.typesafe.config.ConfigException;
import io.github.nucleuspowered.nucleus.api.NucleusAPITokens;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import io.github.nucleuspowered.nucleus.api.service.NucleusSafeTeleportService;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserPreferenceService;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarmupManagerService;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.dataservices.KitDataService;
import io.github.nucleuspowered.nucleus.dataservices.NameBanService;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProviders;
import io.github.nucleuspowered.nucleus.guice.NucleusInjectorModule;
import io.github.nucleuspowered.nucleus.internal.CatalogTypeFinalStaticProcessor;
import io.github.nucleuspowered.nucleus.internal.PreloadTasks;
import io.github.nucleuspowered.nucleus.internal.client.ClientMessageReciever;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.core.CoreModule;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.config.WarmupConfig;
import io.github.nucleuspowered.nucleus.modules.core.services.UUIDChangeService;
import io.github.nucleuspowered.nucleus.modules.core.services.UniqueUserService;
import io.github.nucleuspowered.nucleus.quickstart.ModuleRegistrationProxyService;
import io.github.nucleuspowered.nucleus.quickstart.NucleusConfigAdapter;
import io.github.nucleuspowered.nucleus.quickstart.NucleusLoggerProxy;
import io.github.nucleuspowered.nucleus.quickstart.QuickStartModuleConstructor;
import io.github.nucleuspowered.nucleus.quickstart.event.BaseModuleEvent;
import io.github.nucleuspowered.nucleus.quickstart.module.StandardModule;
import io.github.nucleuspowered.nucleus.services.IConfigurateHelper;
import io.github.nucleuspowered.nucleus.services.IModuleDataProvider;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import io.github.nucleuspowered.nucleus.services.IStorageManager;
import io.github.nucleuspowered.nucleus.services.impl.commandmetadata.CommandMetadataService;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.legacy.ConfigMessageProvider;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.legacy.MessageProvider;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.legacy.ResourceMessageProvider;
import io.github.nucleuspowered.nucleus.services.impl.moduledata.ModuleDataProvider;
import io.github.nucleuspowered.nucleus.services.impl.usercache.UserCacheService;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleDiscoveryException;
import uk.co.drnaylor.quickstart.exceptions.QuickStartModuleLoaderException;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;
import uk.co.drnaylor.quickstart.holders.discoverystrategies.Strategy;
import uk.co.drnaylor.quickstart.loaders.ModuleEnablerBuilder;
import uk.co.drnaylor.quickstart.loaders.PhasedModuleEnabler;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

@Plugin(id = ID, name = NAME, version = VERSION, description = DESCRIPTION, dependencies = @Dependency(id = "spongeapi", version = PluginInfo.SPONGE_API_VERSION))
public class NucleusPlugin extends Nucleus {

    private static final String divider = "+------------------------------------------------------------+";
    private static final int length = divider.length() - 2;

    private final INucleusServiceCollection serviceCollection;
    private final Logger logger;

    private Instant gameStartedTime = null;
    private boolean hasStarted = false;
    private Throwable isErrored = null;
    private CommandsConfig commandsConfig;
    private UserCacheService userCacheService;
    private NameBanService nameBanService;
    private KitDataService kitDataService;
    private TextParsingUtils textParsingUtils;

    private final List<Text> startupMessages = Lists.newArrayList();

    private MessageProvider messageProvider = new ResourceMessageProvider(ResourceMessageProvider.messagesBundle);

    private DiscoveryModuleHolder<StandardModule, StandardModule> moduleContainer;

    private final Path configDir;
    private final Supplier<Path> dataDir;
    @Nullable private Path dataFileLocation = null;
    private Path currentDataDir;
    private boolean isServer = false;
    private WarmupConfig warmupConfig;
    @Nullable private String versionFail;

    private boolean isDebugMode = false;
    private boolean savesandloads = false;

    private static boolean versionCheck(MessageProvider provider) throws IllegalStateException {
        Pattern matching = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)");
        Optional<String> v = Sponge.getPlatform().getContainer(Platform.Component.API).getVersion();

        if (v.isPresent()) {
            Matcher version = matching.matcher(SPONGE_API_VERSION);
            if (!version.find()) {
                return false; // can't compare.
            }

            int maj = Integer.parseInt(version.group("major"));
            int min = Integer.parseInt(version.group("minor"));
            @SuppressWarnings("ConstantConditions") boolean notRequiringSnapshot = !SPONGE_API_VERSION.contains("SNAPSHOT");

            Matcher m = matching.matcher(v.get());
            if (m.find()) {
                int major = Integer.parseInt(m.group("major"));
                if (major != maj) {
                    // not current API
                    throw new IllegalStateException(provider.getMessageWithFormat("startup.nostart.spongeversion.major",
                            PluginInfo.NAME, v.get(), SPONGE_API_VERSION,
                            Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName()));
                }

                int minor = Integer.parseInt(m.group("minor"));
                boolean serverIsSnapshot = v.get().contains("SNAPSHOT");

                //noinspection ConstantConditions
                if (serverIsSnapshot && notRequiringSnapshot) {
                    // If we are a snapshot, and the target version is NOT a snapshot, decrement our version number.
                    minor = minor - 1;
                }

                if (minor < min) {
                    // not right minor version
                    throw new IllegalStateException(provider.getMessageWithFormat("startup.nostart.spongeversion.minor",
                            Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName(), NAME, SPONGE_API_VERSION));
                }
            }

            return true;
        } else {
            // no idea.
            return false;
        }
    }

    // We inject this into the constructor so we can build the config path ourselves.
    @Inject
    public NucleusPlugin(
            @ConfigDir(sharedRoot = true) Path configDir,
            Logger logger,
            Injector injector) {
        Nucleus.setNucleus(this);
        this.logger = logger;
        this.configDir = configDir.resolve(PluginInfo.ID);
        Supplier<Path> sp;
        try {
            Path path = Sponge.getGame().getSavesDirectory();
            sp = () -> path;
            this.isServer = true;
        } catch (NullPointerException e) {
            sp = () -> Sponge.getGame().getSavesDirectory();
        }

        this.dataDir = sp;
        IModuleDataProvider moduleDataProvider = new ModuleDataProvider(() -> this.moduleContainer);
        Injector baseInjector = injector.createChildInjector(new NucleusInjectorModule(sp, configDir, moduleDataProvider));
        this.serviceCollection = baseInjector.getInstance(INucleusServiceCollection.class);
    }

    @Listener(order = Order.FIRST)
    public void onPreInit(GamePreInitializationEvent preInitializationEvent) {
        // Setup object mapper.
        MessageReceiver s;
        if (Sponge.getGame().isServerAvailable()) {
            s = Sponge.getServer().getConsole();
        } else {
            s = new ClientMessageReciever();
        }

        // From the config, get the `core.language` entry, if it exists.
        HoconConfigurationLoader.Builder builder = HoconConfigurationLoader.builder().setPath(Paths.get(this.configDir.toString(), "main.conf"));
        try {
            CommentedConfigurationNode node = builder.build().load();
            String language = node.getNode("core", "language").getString("default");
            if (!language.equalsIgnoreCase("default")) {
                this.messageProvider.setLocale(language);
            }

            String location = node.getNode("core", "data-file-location").getString("default");
            if (!location.equalsIgnoreCase("default")) {
                this.dataFileLocation = Paths.get(location);
            }
        } catch (IOException e) {
            // don't worry about it
        }

        if (System.getProperty("nucleusnocheck") == null) {
            try {
                if (!versionCheck(this.messageProvider)) {
                    s.sendMessage(this.messageProvider.getTextMessageWithFormat("startup.nostart.nodetect", NAME, SPONGE_API_VERSION));
                }
            } catch (IllegalStateException e) {
                s.sendMessage(this.messageProvider.getTextMessageWithFormat("startup.nostart.compat", PluginInfo.NAME,
                        Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName(),
                        Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("unknown")));
                s.sendMessage(this.messageProvider.getTextMessageWithFormat("startup.nostart.compat2", e.getMessage()));
                s.sendMessage(this.messageProvider.getTextMessageWithFormat("startup.nostart.compat3", NAME));
                this.versionFail = e.getMessage();
                disable();
                return;
            }
        }

        s.sendMessage(this.messageProvider.getTextMessageWithFormat("startup.welcome", PluginInfo.NAME,
                PluginInfo.VERSION, Sponge.getPlatform().getContainer(Platform.Component.API).getVersion().orElse("unknown")));

        this.logger.info(this.messageProvider.getMessageWithFormat("startup.preinit", PluginInfo.NAME));
        Game game = Sponge.getGame();
        NucleusAPITokens.onPreInit(this);

        // Startup tasks, for the migrations I need to do.
        PreloadTasks.getPreloadTasks().forEach(x -> x.accept(this));

        // Get the mandatory config files.
        try {
            Files.createDirectories(this.configDir);
            if (this.isServer) {
                Files.createDirectories(this.dataDir.get());
            }
            this.commandsConfig = new CommandsConfig(Paths.get(this.configDir.toString(), "commands.conf"));

            DataProviders d = new DataProviders(this);
            this.kitDataService = new KitDataService(d.getKitsDataProvider());
            this.nameBanService = new NameBanService(d.getNameBanDataProvider());
            this.userCacheService = new UserCacheService(this.serviceCollection);
            this.textParsingUtils = new TextParsingUtils();
            // registerReloadable(this.textParsingUtils);

            if (this.isServer) {
                allChange();
            }
        } catch (Exception e) {
            this.isErrored = e;
            disable();
            e.printStackTrace();
            return;
        }

        PreloadTasks.getPreloadTasks2().forEach(x -> x.accept(this));

        game.getServiceManager().setProvider(this, NucleusModuleService.class, new ModuleRegistrationProxyService(this.serviceCollection, this.moduleContainer));
        game.getServiceManager().setProvider(this, NucleusWarmupManagerService.class, this.serviceCollection.warmupService());
        game.getServiceManager().setProvider(this, NucleusUserPreferenceService.class, this.serviceCollection.userPreferenceService());
        game.getServiceManager().setProvider(this, NucleusMessageTokenService.class, this.serviceCollection.messageTokenService());
        game.getServiceManager().setProvider(this, NucleusSafeTeleportService.class, this.serviceCollection.teleportService());

        try {
            final String he = this.messageProvider.getMessageWithFormat("config.main-header", PluginInfo.VERSION);
            Optional<Asset> optionalAsset = Sponge.getAssetManager().getAsset(Nucleus.getNucleus(), "classes.json");
            DiscoveryModuleHolder.Builder<StandardModule, StandardModule> db =
                    DiscoveryModuleHolder.builder(StandardModule.class, StandardModule.class);
            if (optionalAsset.isPresent()) {
                Map<String, Map<String, List<String>>> m = new Gson().fromJson(
                        optionalAsset.get().readString(),
                        new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType()
                );

                Set<Class<?>> sc = Sets.newHashSet();
                for (String classString : m.keySet()) {
                    sc.add(Class.forName(classString));
                }

                db.setStrategy((string, classloader) -> sc)
                        .setConstructor(new QuickStartModuleConstructor(m, this.serviceCollection));
            } else {
                db.setConstructor(new QuickStartModuleConstructor(null, this.serviceCollection))
                        .setStrategy(Strategy.DEFAULT);
            }

            PhasedModuleEnabler<StandardModule, StandardModule> enabler =
                    new ModuleEnablerBuilder<>(StandardModule.class, StandardModule.class)
                            .createPreEnablePhase("preenable", holder -> Sponge.getEventManager().post(new BaseModuleEvent.AboutToEnable(this)))
                            .createEnablePhase("reg", (module, holder) -> module.loadRegistries())
                            .createEnablePhase("services", (module, holder) -> module.loadServices())
                            .createEnablePhase("pre-tasks", (module, holder) -> module.performPreTasks(this.serviceCollection))
                            .createEnablePhase("command-interceptors", (module, holder) -> module.registerCommandInterceptors())
                            .createPreEnablePhase("enable", holder -> Sponge.getEventManager().post(new BaseModuleEvent.PreEnable(this)))
                            .createEnablePhase("command-discovery", (module, holder) -> module.loadCommands())
                            .createEnablePhase("aliased-commands", (module, holder) -> module.prepareAliasedCommands())
                            .createPreEnablePhase("command-registration",
                                holder -> this.serviceCollection.commandMetadataService().completeRegistrationPhase(this.serviceCollection))
                            .createEnablePhase("events", (module, holder) -> module.loadEvents())
                            .createEnablePhase("runnables", (module, holder) -> module.loadRunnables())
                            .createEnablePhase("prefKeys", (module, holder) -> module.loadUserPrefKeys())
                            .createEnablePhase("infoproviders", (module, holder) -> module.loadInfoProviders())
                            .createEnablePhase("enableTasks", (module, holder) -> module.performEnableTasks(this.serviceCollection))
                            .createPreEnablePhase("postenable", holder -> Sponge.getEventManager().post(new BaseModuleEvent.Enabled(this)))
                            .createEnablePhase("tokens", (module, holder) -> module.loadTokens())
                            .createEnablePhase("config", (module, holder) -> module.configTasks())
                            .createEnablePhase("interceptors", (module, holder) -> module.registerCommandInterceptors())
                            .createEnablePhase("postTasks", (module, holder) -> module.performPostTasks(this.serviceCollection))
                            .build();

            IConfigurateHelper configurateHelper = this.serviceCollection.configurateHelper();
            this.moduleContainer = db
                    .setConfigurationLoader(builder.setDefaultOptions(configurateHelper.setOptions(builder.getDefaultOptions()).setHeader(he)).build())
                    .setPackageToScan(getClass().getPackage().getName() + ".modules")
                    .setLoggerProxy(new NucleusLoggerProxy(this.logger))
                    .setConfigurationOptionsTransformer(x -> configurateHelper.setOptions(x).setHeader(he))
                    .setAllowDisable(false)
                    .setModuleEnabler(enabler)
                    .setRequireModuleDataAnnotation(true)
                    .setNoMergeIfPresent(true)
                    .setModuleConfigurationHeader(m -> {
                            StringBuilder ssb = new StringBuilder().append(divider).append("\n");
                            String name = m.getClass().getAnnotation(ModuleData.class).name();
                            int nameLength = name.length() + 2;
                            int dashes = (length - nameLength) / 2;
                            ssb.append("|");
                            for (int i = 0; i < dashes; i++) {
                                ssb.append(" ");
                            }

                            ssb.append(" ").append(name).append(" ");
                            for (int i = 0; i < dashes; i++) {
                                ssb.append(" ");
                            }

                            if (length > dashes * 2 + nameLength) {
                                ssb.append(" ");
                            }

                            return ssb.append("|").append("\n").append(divider).toString();
                    })
                    .setModuleConfigSectionName("-modules")
                    .setModuleConfigSectionDescription(this.messageProvider.getMessageWithFormat("config.module-desc"))
                    .setModuleDescriptionHandler(m -> this.messageProvider.getMessageWithFormat("config.module." +
                            m.getAnnotation(ModuleData.class).id().toLowerCase() + ".desc"))
                    .build();

            this.moduleContainer.startDiscover();
        } catch (Exception e) {
            this.isErrored = e;
            disable();
            e.printStackTrace();
        }
    }

    @Listener(order = Order.FIRST)
    public void onInit(GameInitializationEvent event) {
        if (this.isErrored != null) {
            return;
        }

        this.logger.info(this.messageProvider.getMessageWithFormat("startup.init", PluginInfo.NAME));

        try {
            CatalogTypeFinalStaticProcessor.setEventContexts();
        } catch (Exception e) {
            this.isErrored = e;
            disable();
            e.printStackTrace();
        }
    }

    @Listener(order = Order.POST)
    public void onInitLate(GameInitializationEvent event) {
        if (this.isErrored != null) {
            return;
        }

        this.logger.info(this.messageProvider.getMessageWithFormat("startup.postinit", PluginInfo.NAME));

        // Load up the general data files now, mods should have registered items by now.
        try {
            // Reloadable so that we can update the serialisers.
            this.moduleContainer.reloadSystemConfig();
        } catch (Exception e) {
            this.isErrored = e;
            disable();
            e.printStackTrace();
            return;
        }

        try {
            Sponge.getEventManager().post(new BaseModuleEvent.AboutToConstructEvent(this));
            this.logger.info(this.messageProvider.getMessageWithFormat("startup.moduleloading", PluginInfo.NAME));
            this.moduleContainer.loadModules(true);

            CoreConfig coreConfig = this.moduleContainer.getConfigAdapterForModule(CoreModule.ID, CoreConfigAdapter.class).getNodeOrDefault();

            if (coreConfig.isErrorOnStartup()) {
                throw new IllegalStateException("In main.conf, core.simulate-error-on-startup is set to TRUE. Remove this config entry to allow Nucleus to start. Simulating error and disabling Nucleus.");
            }

            this.isDebugMode = coreConfig.isDebugmode();
            this.savesandloads = coreConfig.isPrintSaveLoad();
        } catch (Throwable construction) {
            this.logger.info(this.messageProvider.getMessageWithFormat("startup.modulenotloaded", PluginInfo.NAME));
            construction.printStackTrace();
            disable();
            this.isErrored = construction;
            return;
        }

        logMessageDefault();
        this.logger.info(this.messageProvider.getMessageWithFormat("startup.moduleloaded", PluginInfo.NAME));
        this.serviceCollection.permissionService().registerDescriptions();
        Sponge.getEventManager().post(new BaseModuleEvent.Complete(this));

        this.logger.info(this.messageProvider.getMessageWithFormat("startup.completeinit", PluginInfo.NAME));
    }

    @Listener(order = Order.EARLY)
    public void onGameStartingEarly(GameStartingServerEvent event) {
        if (!this.isServer) {
            try {
                this.logger.info(this.messageProvider.getMessageWithFormat("startup.loaddata", PluginInfo.NAME));
                allChange();
            } catch (Exception e) {
                this.isErrored = e;
                disable();
                e.printStackTrace();
            }
        }
    }

    private void allChange() throws Exception {
        this.serviceCollection.storageManager().saveAndInvalidateAllCaches();
        reload();
        resetDataPath(true);

        this.kitDataService.changeFile();
        this.nameBanService.changeFile();
        // this.userCacheService.changeFile();

        this.userCacheService.load();
        this.nameBanService.load();
        this.kitDataService.loadInternal();
    }

    @Listener
    public void onGameStarting(GameStartingServerEvent event) {
        if (this.isErrored == null) {
            this.logger.info(this.messageProvider.getMessageWithFormat("startup.gamestart", PluginInfo.NAME));

            // Load up the general data files now, mods should have registered items by now.
            try {
                this.kitDataService.loadInternal();
            } catch (Exception e) {
                this.isErrored = e;
                disable();
                e.printStackTrace();
                return;
            }

            // Start the user cache walk if required, the user storage service is loaded at this point.
            Task.builder().async().execute(() -> this.userCacheService.startFilewalkIfNeeded()).submit(this);
            this.logger.info(this.messageProvider.getMessageWithFormat("startup.started", PluginInfo.NAME));
        }
    }

    @Listener(order = Order.PRE)
    public void onGameStarted(GameStartedServerEvent event) {
        if (this.isErrored == null) {
            try {
                this.serviceCollection.getServiceUnchecked(UniqueUserService.class).resetUniqueUserCount();
                this.serviceCollection.getServiceUnchecked(UUIDChangeService.class).setStateAndReload(this.serviceCollection);
                this.serviceCollection.getServiceUnchecked(CommandMetadataService.class).activate();

                // Save any additions.
                this.moduleContainer.refreshSystemConfig();
                fireReloadables();
            } catch (Throwable e) {
                this.isErrored = e;
                disable();
                errorOnStartup();
                return;
            }

            this.hasStarted = true;
            Sponge.getScheduler().createSyncExecutor(this).submit(() -> this.gameStartedTime = Instant.now());

            if (this.serviceCollection.getServiceUnchecked(CoreConfigAdapter.class).getNodeOrDefault().isWarningOnStartup()) {
                // What about perms and econ?
                List<Text> lt = Lists.newArrayList();
                if (this.serviceCollection.permissionService().isOpOnly()) {
                    addTri(lt);
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.line"));
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.nopermplugin"));
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.nopermplugin2"));
                }

                if (!Sponge.getServiceManager().isRegistered(EconomyService.class)) {
                    if (lt.isEmpty()) {
                        addTri(lt);
                    }

                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.line"));
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.noeconplugin"));
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.noeconplugin2"));
                }

                if (!lt.isEmpty()) {
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.line"));
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.seesuggested"));
                }

                if (!this.startupMessages.isEmpty()) {
                    if (lt.isEmpty()) {
                        addTri(lt);
                    }

                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.line"));
                    lt.addAll(this.startupMessages);
                    this.startupMessages.clear();
                }

                if (!lt.isEmpty()) {
                    lt.add(this.messageProvider.getTextMessageWithFormat("standard.line"));
                    ConsoleSource c = Sponge.getServer().getConsole();
                    lt.forEach(c::sendMessage);
                }
            }
        }
    }

    @Listener
    public void onServerStop(GameStoppedServerEvent event) {
        if (this.hasStarted && this.isErrored == null) {
            this.gameStartedTime = null;
            this.logger.info(this.messageProvider.getMessageWithFormat("startup.stopped", PluginInfo.NAME));
            saveData();
            this.serviceCollection.getServiceUnchecked(CommandMetadataService.class).deactivate();
        }
    }

    @Override
    public void saveData() {
        IStorageManager ism = this.serviceCollection.storageManager();
        ism.getUserService().ensureSaved();
        ism.getWorldService().ensureSaved();

        if (Sponge.getGame().getState().ordinal() > GameState.SERVER_ABOUT_TO_START.ordinal()) {
            try {
                ism.getGeneralService().ensureSaved();
                this.nameBanService.save();
                this.userCacheService.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public Path getConfigDirPath() {
        return this.configDir;
    }

    private Path resetDataPath(boolean tryCreate) throws IOException {
        Path path;
        boolean custom = false;
        if (this.dataFileLocation == null) {
            path = this.dataDir.get();
        } else {
            custom = true;
            if (this.dataFileLocation.isAbsolute()) {
                path = this.dataFileLocation;
            } else {
                path = this.dataDir.get().resolve(this.dataFileLocation);
            }

            if (!Files.isDirectory(path)) {
                // warning
                this.logger.error(this.serviceCollection.messageProvider().getMessageString("nucleus.custompath.error",
                        path.toAbsolutePath().toString(),
                        this.dataDir.get().toAbsolutePath().toString()));
                custom = false;
                path = this.dataDir.get();
            }
        }

        this.currentDataDir = path.resolve("nucleus");
        if (tryCreate) {
            if (custom) {
                this.logger.info(this.serviceCollection.messageProvider().getMessageString("nucleus.custompath.info",
                        this.currentDataDir.toAbsolutePath().toString()));
            }
            Files.createDirectories(this.currentDataDir);
        }
        return this.currentDataDir;
    }

    @Override
    @Nonnull
    public Path getDataPath() {
        if (this.currentDataDir == null) {
            try {
                return resetDataPath(true);
            } catch (IOException e) {
                e.printStackTrace();
                this.logger.error(this.serviceCollection.messageProvider().getMessageString("nucleus.couldntcreate"));
                try {
                    return resetDataPath(false);
                } catch (IOException ignored) { }
            }
        }

        return this.currentDataDir;
    }

    @Override
    public synchronized boolean reload() {
        try {
            this.moduleContainer.reloadSystemConfig();
            reloadMessages();
            this.commandsConfig.load();
            this.warmupConfig = null;

            CoreConfig coreConfig = this.serviceCollection.getServiceUnchecked(CoreConfigAdapter.class).getNodeOrDefault();
            this.isDebugMode = coreConfig.isDebugmode();
            this.savesandloads = coreConfig.isPrintSaveLoad();

            fireReloadables();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void fireReloadables() {
        this.serviceCollection.reloadableService().fireReloadables(this.serviceCollection);
    }

    @Override
    public boolean reloadMessages() {
        boolean r = true;
        CoreConfig config = this.serviceCollection.getServiceUnchecked(CoreConfigAdapter.class).getNodeOrDefault();
        // Get the language
        String language = config.getServerLocale();
        if (language == null) {
            language = "default";
        }

        if (config.isCustommessages()) {
            try {
                this.messageProvider =
                        new ConfigMessageProvider(this.configDir.resolve("messages.conf"), ResourceMessageProvider.messagesBundle, language);
                Sponge.getServer().getConsole().sendMessage(this.messageProvider.getTextMessageWithFormat("language.set", "messages.conf"));
                return true;
            } catch (Throwable exception) {
                r = false;
                // On error, fallback.
                // Blegh, relocations
                if (exception instanceof IOException && exception.getCause().getClass().getName().contains(ConfigException.class.getSimpleName())) {
                    MessageReceiver s;
                    if (Sponge.getGame().isServerAvailable()) {
                        s = Sponge.getServer().getConsole();
                    } else {
                        s = new ClientMessageReciever();
                    }

                    exception = exception.getCause();
                    s.sendMessage(Text.of(TextColors.RED, "It appears that there is an error in your messages file! The error is: "));
                    s.sendMessage(Text.of(TextColors.RED, exception.getMessage()));
                    s.sendMessage(Text.of(TextColors.RED, "Please correct this - then run ", TextColors.YELLOW, "/nucleus reload"));
                    s.sendMessage(Text.of(TextColors.RED, "Ignoring messages.conf for now."));
                    if (this.isDebugMode) {
                        exception.printStackTrace();
                    }
                } else {
                    this.logger.warn("Could not load custom messages file. Falling back.");
                    exception.printStackTrace();
                }
            }
        }

        this.messageProvider = new ResourceMessageProvider(ResourceMessageProvider.messagesBundle, language);
        if (this.hasStarted) {
            logMessageDefault();
        }
        return r;
    }

    private void logMessageDefault() {
        this.logger.info(this.messageProvider.getMessageWithFormat("language.set", this.messageProvider.getLocale().toLanguageTag()));
    }

    @Override public WarmupConfig getWarmupConfig() {
        if (this.warmupConfig == null) {
            this.warmupConfig = getConfigValue(CoreModule.ID, CoreConfigAdapter.class, CoreConfig::getWarmupConfig).orElseGet(WarmupConfig::new);
        }

        return this.warmupConfig;
    }

    @Override
    public DiscoveryModuleHolder<StandardModule, StandardModule> getModuleHolder() {
        return this.moduleContainer;
    }

    @Override
    public <R extends NucleusConfigAdapter<?>> Optional<R> getConfigAdapter(String id, Class<R> configAdapterClass) {
        try {
            return Optional.of(getModuleHolder().getConfigAdapterForModule(id, configAdapterClass));
        } catch (NoModuleException | IncorrectAdapterTypeException e) {
            return Optional.empty();
        }
    }

    @Override public Optional<Instant> getGameStartedTime() {
        return Optional.ofNullable(this.gameStartedTime);
    }

    @Override public KitDataService getKitDataService() {
        return this.kitDataService;
    }

    @Override public NameBanService getNameBanService() { return this.nameBanService; }

    @Override
    public CommandsConfig getCommandsConfig() {
        return this.commandsConfig;
    }

    public TextParsingUtils getTextParsingUtils() {
        return this.textParsingUtils;
    }

    @Override public void registerReloadable(Reloadable reloadable) {
        this.serviceCollection.reloadableService().registerReloadable(reloadable);
    }

    @Override
    public IPermissionService getPermissionResolver() {
        return this.serviceCollection.permissionService();
    }

    @Override
    public boolean isServer() {
        return this.isServer;
    }

    @Override public void addStartupMessage(Text message) {
        this.startupMessages.add(message);
    }

    @Override public boolean isPrintingSavesAndLoads() {
        return this.savesandloads;
    }

    private void disable() {
        // Disable everything, just in case. Thanks to pie-flavor: https://forums.spongepowered.org/t/disable-plugin-disable-itself/15831/8
        Sponge.getEventManager().unregisterPluginListeners(this);
        Sponge.getCommandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
        Sponge.getScheduler().getScheduledTasks(this).forEach(Task::cancel);
        this.serviceCollection.getService(CommandMetadataService.class).ifPresent(CommandMetadataService::deactivate);

        // Re-register this to warn people about the error.
        Sponge.getEventManager().registerListener(this, GameStartedServerEvent.class, e -> errorOnStartup());
    }

    private void errorOnStartup() {
        try {
            Sponge.getServer().setHasWhitelist(this.isServer);
        } catch (Throwable e) {
            //ignored
        }

        if (this.versionFail != null) {
            Sponge.getServer().getConsole().sendMessages(getIncorrectVersion());
        } else {
            Sponge.getServer().getConsole().sendMessages(getErrorMessage());
        }
    }

    private List<Text> getIncorrectVersion() {
        List<Text> messages = Lists.newArrayList();
        messages.add(Text.of(TextColors.RED, "------------------------------"));
        messages.add(Text.of(TextColors.RED, "-   NUCLEUS FAILED TO LOAD   -"));
        messages.add(Text.of(TextColors.RED, "------------------------------"));
        addX(messages, 7);
        messages.add(Text.of(TextColors.RED, "------------------------------"));
        messages.add(Text.of(TextColors.RED, "-  INCORRECT SPONGE VERSION  -"));
        messages.add(Text.of(TextColors.RED, "------------------------------"));
        messages.add(Text.EMPTY);
        messages.add(Text.of(TextColors.RED, "You are a mismatched version of Sponge on your server - this version of Nucleus will not run upon it."));
        messages.add(Text.of(TextColors.RED, "Nucleus has not started. Update Sponge to the latest version and try again."));
        if (this.isServer) {
            messages.add(Text.of(TextColors.RED,
                    "The server has been automatically whitelisted - this is to protect your server and players if you rely on some of Nucleus' functionality (such as fly states, etc.)"));
        }
        messages.add(Text.of(TextColors.RED, "------------------------------"));
        messages.add(Text.of(TextColors.YELLOW, "Reason: "));
        messages.add(Text.of(TextColors.YELLOW, this.versionFail));
        messages.add(Text.of(TextColors.RED, "------------------------------"));
        messages.add(Text.of(TextColors.YELLOW, "Current Sponge Implementation: ",
                Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName(), ", version ",
                Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("unknown"), "."));
        return messages;
    }

    private void addTri(List<Text> messages) {
        messages.add(Text.of(TextColors.YELLOW, "        /\\"));
        messages.add(Text.of(TextColors.YELLOW, "       /  \\"));
        messages.add(Text.of(TextColors.YELLOW, "      / || \\"));
        messages.add(Text.of(TextColors.YELLOW, "     /  ||  \\"));
        messages.add(Text.of(TextColors.YELLOW, "    /   ||   \\"));
        messages.add(Text.of(TextColors.YELLOW, "   /    ||    \\"));
        messages.add(Text.of(TextColors.YELLOW, "  /            \\"));
        messages.add(Text.of(TextColors.YELLOW, " /      **      \\"));
        messages.add(Text.of(TextColors.YELLOW, "------------------"));
    }

    @Override
    public void addX(List<Text> messages, int spacing) {
        Text space = Text.of(String.join("", Collections.nCopies(spacing, " ")));
        messages.add(Text.of(space, TextColors.RED, "\\              /"));
        messages.add(Text.of(space, TextColors.RED, " \\            /"));
        messages.add(Text.of(space, TextColors.RED, "  \\          /"));
        messages.add(Text.of(space, TextColors.RED, "   \\        /"));
        messages.add(Text.of(space, TextColors.RED, "    \\      /"));
        messages.add(Text.of(space, TextColors.RED, "     \\    /"));
        messages.add(Text.of(space, TextColors.RED, "      \\  /"));
        messages.add(Text.of(space, TextColors.RED, "       \\/"));
        messages.add(Text.of(space, TextColors.RED, "       /\\"));
        messages.add(Text.of(space, TextColors.RED, "      /  \\"));
        messages.add(Text.of(space, TextColors.RED, "     /    \\"));
        messages.add(Text.of(space, TextColors.RED, "    /      \\"));
        messages.add(Text.of(space, TextColors.RED, "   /        \\"));
        messages.add(Text.of(space, TextColors.RED, "  /          \\"));
        messages.add(Text.of(space, TextColors.RED, " /            \\"));
        messages.add(Text.of(space, TextColors.RED, "/              \\"));
    }

    private List<Text> getErrorMessage() {
        List<Text> messages = Lists.newArrayList();
        messages.add(Text.of(TextColors.RED, "----------------------------"));
        messages.add(Text.of(TextColors.RED, "-  NUCLEUS FAILED TO LOAD  -"));
        messages.add(Text.of(TextColors.RED, "----------------------------"));
        addX(messages, 5);
        messages.add(Text.of(TextColors.RED, "----------------------------"));

        messages.add(Text.EMPTY);
        messages.add(Text.of(TextColors.RED, "Nucleus encountered an error during server start up and did not enable successfully. No commands, listeners or tasks are registered."));
        if (this.isServer) {
            messages.add(Text.of(TextColors.RED,
                    "The server has been automatically whitelisted - this is to protect your server and players if you rely on some of Nucleus' functionality (such as fly states, etc.)"));
        }
        messages.add(Text.of(TextColors.RED, "The error that Nucleus encountered will be reproduced below for your convenience."));

        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        if (this.isErrored == null) {
            messages.add(Text.of(TextColors.YELLOW, "No exception was saved."));
        } else {
            Throwable exception = this.isErrored;
            if (exception.getCause() != null &&
                    (exception instanceof QuickStartModuleLoaderException || exception instanceof QuickStartModuleDiscoveryException)) {
                exception = exception.getCause();
            }

            // Blegh, relocations
            if (exception instanceof IOException && exception.getCause().getClass().getName().contains(ConfigException.class.getSimpleName())) {
                exception = exception.getCause();
                messages.add(Text.of(TextColors.RED, "It appears that there is an error in your configuration file! The error is: "));
                messages.add(Text.of(TextColors.RED, exception.getMessage()));
                messages.add(Text.of(TextColors.RED, "Please correct this and restart your server."));
                messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
                messages.add(Text.of(TextColors.YELLOW, "(The error that was thrown is shown below)"));
            }

            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                exception.printStackTrace(pw);
                pw.flush();
                String[] stackTrace = sw.toString().split("(\r)?\n");
                for (String s : stackTrace) {
                    messages.add(Text.of(TextColors.YELLOW, s));
                }
            } catch (IOException e) {
                exception.printStackTrace();
            }
        }

        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        messages.add(Text.of(TextColors.RED, "If this error persists, check your configuration files and ensure that you have the latest version of Nucleus which matches the current version of the Sponge API."));
        messages.add(Text.of(TextColors.RED, "If you do, please report this error to the Nucleus team at https://github.com/NucleusPowered/Nucleus/issues"));
        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        messages.add(Text.of(TextColors.YELLOW, "Server Information"));
        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        messages.add(Text.of(TextColors.YELLOW, "Nucleus version: " + PluginInfo.VERSION + ", (Git: " + PluginInfo.GIT_HASH + ")"));

        Platform platform = Sponge.getPlatform();
        messages.add(Text.of(TextColors.YELLOW, "Minecraft version: " + platform.getMinecraftVersion().getName()));
        messages.add(Text.of(TextColors.YELLOW, String.format("Sponge Version: %s %s", platform.getContainer(Platform.Component.IMPLEMENTATION).getName(),
                platform.getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("unknown"))));
        messages.add(Text.of(TextColors.YELLOW, String.format("Sponge API Version: %s %s", platform.getContainer(Platform.Component.API).getName(),
                platform.getContainer(Platform.Component.API).getVersion().orElse("unknown"))));

        messages.add(Text.EMPTY);
        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        messages.add(Text.of(TextColors.YELLOW, "Installed Plugins"));
        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        Sponge.getPluginManager().getPlugins().forEach(x -> messages.add(Text.of(TextColors.YELLOW, x.getName() + " (" + x.getId() + ") version " + x.getVersion().orElse("unknown"))));

        messages.add(Text.EMPTY);
        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        messages.add(Text.of(TextColors.YELLOW, "- END NUCLEUS ERROR REPORT -"));
        messages.add(Text.of(TextColors.YELLOW, "----------------------------"));
        return messages;
    }
}
