/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.quickstart.module;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.api.service.NucleusUserPreferenceService;
import io.github.nucleuspowered.nucleus.internal.Constants;
import io.github.nucleuspowered.nucleus.internal.annotations.APIService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommandInterceptors;
import io.github.nucleuspowered.nucleus.internal.annotations.RequireExistenceOf;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresPlatform;
import io.github.nucleuspowered.nucleus.internal.annotations.ReregisterService;
import io.github.nucleuspowered.nucleus.internal.annotations.ServerOnly;
import io.github.nucleuspowered.nucleus.internal.annotations.SkipOnError;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandInterceptor;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.control.CommandMetadata;
import io.github.nucleuspowered.nucleus.internal.interfaces.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.interfaces.ServiceBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.TaskBase;
import io.github.nucleuspowered.nucleus.internal.registry.NucleusRegistryModule;
import io.github.nucleuspowered.nucleus.services.ICommandMetadataService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IUserPreferenceService;
import io.github.nucleuspowered.nucleus.services.impl.messagetoken.Tokens;
import io.github.nucleuspowered.nucleus.services.impl.playerinformation.NucleusProvider;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.PreferenceKeyImpl;
import io.github.nucleuspowered.nucleus.services.impl.userprefs.UserPrefKeys;
import org.slf4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.Subject;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.exceptions.MissingDependencyException;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.inject.Inject;

@Store(isRoot = true)
public abstract class StandardModule implements Module {

    private final String moduleId;
    private final String moduleName;
    protected final INucleusServiceCollection serviceCollection;
    private final Supplier<DiscoveryModuleHolder<?, ?>> moduleHolderSupplier;
    private final Logger logger;
    private String packageName;
    @Nullable private Map<String, List<String>> objectTypesToClassListMap;
    // private final String message = NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("config.enabled");

    @Inject
    public StandardModule(Supplier<DiscoveryModuleHolder<?, ?>> moduleHolder, INucleusServiceCollection collection) {
        ModuleData md = this.getClass().getAnnotation(ModuleData.class);
        this.moduleId = md.id();
        this.moduleName = md.name();
        this.serviceCollection = collection;
        this.moduleHolderSupplier = moduleHolder;
        this.logger = collection.logger();
    }

    protected final INucleusServiceCollection getServiceCollection() {
        return this.serviceCollection;
    }

    public void init(Map<String, List<String>> m) {
        this.objectTypesToClassListMap = m;
    }

    @Override
    public final void checkExternalDependencies() throws MissingDependencyException {
        if (this.getClass().isAnnotationPresent(ServerOnly.class) && !Nucleus.getNucleus().isServer()) {
            throw new MissingDependencyException("This module is server only and will not be loaded.");
        }
    }

    protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.of();
    }

    /**
     * Non-configurable module, no configuration to register.
     *
     * @return {@link Optional#empty()}
     */
    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.empty();
    }

    public final void loadServices() throws Exception {
        Set<Class<? extends ServiceBase>> servicesToLoad;
        if (this.objectTypesToClassListMap != null) {
            servicesToLoad = getClassesFromList(Constants.SERVICE);
        } else {
            servicesToLoad = getStreamForModule(ServiceBase.class).collect(Collectors.toSet());
        }

        for (Class<? extends ServiceBase> serviceClass : servicesToLoad) {
            registerService(serviceClass);
        }
    }

    private <T extends ServiceBase> void registerService(Class<T> serviceClass) throws Exception {
        T serviceImpl = getInstance(serviceClass);
        if (serviceImpl == null) {
            String error = "ERROR: Cannot instantiate " + serviceClass.getName();
            Nucleus.getNucleus().getLogger().error(error);
            throw new IllegalStateException(error);
        }

        APIService apiService = serviceClass.getAnnotation(APIService.class);
        if (apiService != null) {
            Class<?> apiInterface = apiService.value();
            if (apiInterface.isInstance(serviceImpl)) {
                // OK
                register((Class) apiInterface, serviceClass, serviceImpl);
            } else {
                String error = "ERROR: " + apiInterface.getName() + " does not inherit from " + serviceClass.getName();
                Nucleus.getNucleus().getLogger().error(error);
                throw new IllegalStateException(error);
            }
        } else {
            ReregisterService reregisterService = serviceClass.getAnnotation(ReregisterService.class);
            if (reregisterService != null) {
                Class<?> apiInterface = reregisterService.value();
                if (apiInterface.isInstance(serviceImpl)) {
                    // OK
                    register((Class) apiInterface, serviceClass, serviceImpl, true);
                } else {
                    String error = "ERROR: " + apiInterface.getName() + " does not inherit from " + serviceClass.getName();
                    this.logger.error(error);
                    throw new IllegalStateException(error);
                }
            } else {
                register(serviceClass, serviceImpl);
            }
        }

        if (serviceImpl instanceof Reloadable) {
            Reloadable reloadable = (Reloadable) serviceImpl;
            this.serviceCollection.reloadableService().registerReloadable(reloadable);
            reloadable.onReload(this.serviceCollection);
        }

        if (serviceImpl instanceof ContextCalculator) {
            try {
                // boolean matches(Context context, T calculable);
                serviceImpl.getClass().getMethod("matches", Context.class, Subject.class);

                // register it
                //noinspection unchecked
                this.serviceCollection.permissionService().registerContextCalculator((ContextCalculator<Subject>) serviceImpl);
            } catch (NoSuchMethodException e) {
                // ignored
            }
        }
    }

    public void registerCommandInterceptors() throws Exception {
        RegisterCommandInterceptors annotation = getClass().getAnnotation(RegisterCommandInterceptors.class);
        if (annotation != null) {
            // for each annotation, attempt to register the service.
            for (Class<? extends ICommandInterceptor> service : annotation.value()) {

                // create the impl
                ICommandInterceptor impl;
                try {
                    impl = service.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    String error = "ERROR: Cannot instantiate ICommandInterceptor " + service.getName();
                    this.serviceCollection.logger().error(error);
                    throw new IllegalStateException(error, e);
                }

                if (impl instanceof Reloadable) {
                    Reloadable reloadable = (Reloadable) impl;
                    this.serviceCollection.reloadableService().registerReloadable(reloadable);
                    reloadable.onReload(this.serviceCollection);
                }

                // hahahaha, no
                this.serviceCollection.commandMetadataService().registerInterceptor(impl);
            }
        }
    }

    public final void setPackageName() {
        this.packageName = this.getClass().getPackage().getName() + ".";
    }

    public void setPermissionPredicates() {}

    @SuppressWarnings("unchecked")
    public final void loadCommands() {

        HashMap<CommandMetadata, Class<? extends ICommandExecutor<?>>> commandExecutorHashMap = new HashMap<>();
        Set<Class<? extends ICommandExecutor<?>>> cmds;
        if (this.objectTypesToClassListMap != null) {
            cmds = getClassesFromList(Constants.COMMAND);
        } else {
            cmds = performFilter(getStreamForModule(ICommandExecutor.class)
                    .map(x -> (Class<? extends ICommandExecutor<?>>) x))
                    .collect(Collectors.toSet());
        }

        ICommandMetadataService metadataService = this.serviceCollection.commandMetadataService();
        for (Class<? extends ICommandExecutor<?>> command : cmds) {
            Command rc = command.getAnnotation(Command.class);
            if (rc != null) {
                // then we should add it.
                metadataService.registerCommand(
                    this.moduleId,
                    this.moduleName,
                    rc,
                    command
                );
            }
        }

        // Construction happens later in a pre step in QSML
    }

    public final void prepareAliasedCommands() {
        this.serviceCollection.commandMetadataService().addMapping(remapCommand());
    }

    private Stream<Class<? extends ICommandExecutor<?>>> performFilter(Stream<Class<? extends ICommandExecutor<?>>> stream) {
        return stream.filter(x -> x.isAnnotationPresent(Command.class));
    }

    public final void loadEvents() {
        Set<Class<? extends ListenerBase>> listenersToLoad;
        if (this.objectTypesToClassListMap != null) {
            listenersToLoad = getClassesFromList(Constants.LISTENER);
        } else {
            listenersToLoad = getStreamForModule(ListenerBase.class).collect(Collectors.toSet());
        }

        listenersToLoad.stream().map(x -> this.getInstance(x, true)).filter(Objects::nonNull).forEach(c -> {
            if (c instanceof ListenerBase.Conditional) {
                // Add reloadable to load in the listener dynamically if required.
                Reloadable tae = serviceCollection -> {
                    Sponge.getEventManager().unregisterListeners(c);
                    if (c instanceof Reloadable) {
                        ((Reloadable) c).onReload(serviceCollection);
                    }

                    if (((ListenerBase.Conditional) c).shouldEnable(serviceCollection)) {
                        Sponge.getEventManager().registerListeners(serviceCollection.pluginContainer(), c);
                    }
                };

                this.serviceCollection.reloadableService().registerReloadable(tae);
                try {
                    tae.onReload(this.serviceCollection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (c instanceof Reloadable) {
                this.serviceCollection.reloadableService().registerReloadable((Reloadable) c);
                Sponge.getEventManager().registerListeners(this.serviceCollection.pluginContainer(), c);
            } else {
                Sponge.getEventManager().registerListeners(this.serviceCollection.pluginContainer(), c);
            }
        });
    }

    public final void loadRunnables() {
        Set<Class<? extends TaskBase>> tasksToLoad;
        if (this.objectTypesToClassListMap != null) {
            tasksToLoad = getClassesFromList(Constants.RUNNABLE);
        } else {
            tasksToLoad = getStreamForModule(TaskBase.class).collect(Collectors.toSet());
        }

        tasksToLoad.stream().map(this::getInstance).filter(Objects::nonNull).forEach(c -> {
            Task.Builder tb = Sponge.getScheduler().createTaskBuilder().interval(c.interval().toMillis(), TimeUnit.MILLISECONDS);
            if (Nucleus.getNucleus().isServer()) {
                tb.execute(c);
            } else {
                tb.execute(t -> {
                    if (Sponge.getGame().isServerAvailable()) {
                        c.accept(t);
                    }
                });
            }

            if (c.isAsync()) {
                tb.async();
            }

            tb.submit(this.serviceCollection.pluginContainer());

            if (c instanceof Reloadable) {
                this.serviceCollection.reloadableService().registerReloadable((Reloadable) c);
                try {
                    ((Reloadable) c).onReload(this.serviceCollection);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public final void loadTokens() {
        Map<String, Tokens.Translator> map = tokensToRegister();
        if (!map.isEmpty()) {
            map.forEach((k, t) -> {
                try {
                    if (!this.serviceCollection.messageTokenService().getNucleusTokenParser().register(k, t, true)) {
                        Nucleus.getNucleus().getLogger().warn("Could not register primary token identifier " + k);
                    }
                } catch (IllegalArgumentException e) {
                    Nucleus.getNucleus().getLogger().warn("Could not register nucleus token identifier " + k);
                }
            });
        }
    }

    public final void loadRegistries() {
        Set<Class<? extends NucleusRegistryModule>> registries;
        if (this.objectTypesToClassListMap != null) {
            registries = getClassesFromList(Constants.REGISTRY);
        } else {
            registries = getStreamForModule(NucleusRegistryModule.class).collect(Collectors.toSet());
        }

        for (Class<? extends NucleusRegistryModule> r : registries) {
            NucleusRegistryModule instance = getInstance(r);
            try {
                instance.init();
            } catch (Exception e) {
                Nucleus.getNucleus().getLogger().error("Could not register registry " + r.getName(), e);
            }
        }
    }

    public final void loadInfoProviders() {
        Set<Class<? extends NucleusProvider>> registries;
        if (this.objectTypesToClassListMap != null) {
            registries = getClassesFromList(Constants.PLAYER_INFO);
        } else {
            registries = getStreamForModule(NucleusProvider.class).collect(Collectors.toSet());
        }

        for (Class<? extends NucleusProvider> r : registries) {
            NucleusProvider instance = getInstance(r);
            this.serviceCollection.playerInformationService().registerProvider(instance);
        }
    }

    public final void loadUserPrefKeys() {
        Set<Class<? extends UserPrefKeys>> keyClasses;
        if (this.objectTypesToClassListMap != null) {
            keyClasses = getClassesFromList(Constants.PREF_KEYS);
        } else {
            keyClasses = getStreamForModule(UserPrefKeys.class).collect(Collectors.toSet());
        }

        if (!keyClasses.isEmpty()) {
            // Get the User Preference Service
            IUserPreferenceService ups = this.serviceCollection.userPreferenceService();
            for (Class<? extends UserPrefKeys> r : keyClasses) {
                // These will contain static fields.
                Arrays.stream(r.getFields())
                        .filter(x -> Modifier.isStatic(x.getModifiers()) && NucleusUserPreferenceService.PreferenceKey.class.isAssignableFrom(x.getType()))
                        .forEach(x -> {
                            try {
                                PreferenceKeyImpl<?> key = (PreferenceKeyImpl<?>) x.get(null);
                                ups.register(key);
                            } catch (IllegalAccessException e) {
                                Nucleus.getNucleus().getLogger().error("Could not register " + x.getName() + " in the User Preference Service", e);
                            }
                        });
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Stream<Class<? extends T>> getStreamForModule(Class<T> assignableClass) {
        return Nucleus.getNucleus().getModuleHolder().getLoadedClasses().stream()
                .filter(assignableClass::isAssignableFrom)
                .filter(x -> x.getPackage().getName().startsWith(this.packageName))
                .filter(x -> !Modifier.isAbstract(x.getModifiers()) && !Modifier.isInterface(x.getModifiers()))
                .filter(this::checkPlatform)
                .map(x -> (Class<? extends T>)x);
    }

    public void performPreTasks(INucleusServiceCollection serviceCollection) throws Exception { }

    public void performEnableTasks(INucleusServiceCollection serviceCollection) throws Exception { }

    public void performPostTasks(INucleusServiceCollection serviceCollection) { }

    public void configTasks() {

    }

    protected ImmutableMap<String, String> remapCommand() {
        return ImmutableMap.of();
    }

    private <T> T getInstance(Class<T> clazz) {
        return getInstance(clazz, false);
    }

    private <T> T getInstance(Class<T> clazz, boolean checkMethods) {
        try {
            RequireExistenceOf[] v = clazz.getAnnotationsByType(RequireExistenceOf.class);
            if (v.length > 0) {
                try {
                    for (RequireExistenceOf r : v) {
                        String toFind = r.value();
                        String[] a;
                        if (toFind.contains("#")) {
                            a = toFind.split("#", 2);
                        } else {
                            a = new String[]{toFind};
                        }

                        // Check the class.
                        Class<?> c = Class.forName(a[0]);
                        if (a.length == 2) {
                            // Check the method
                            Method[] methods = c.getDeclaredMethods();
                            boolean methodFound = false;
                            for (Method m : methods) {
                                if (m.getName().equals(a[1])) {
                                    methodFound = true;
                                    break;
                                }
                            }

                            if (!methodFound) {
                                if (r.showError()) {
                                    throw new RuntimeException();
                                }

                                return null;
                            }
                        }
                    }
                } catch (ClassNotFoundException | RuntimeException | NoClassDefFoundError e) {
                    this.serviceCollection.logger().warn(this.serviceCollection.messageProvider().getMessageString("startup.injectablenotloaded", clazz.getName()));
                    return null;
                }
            }

            if (checkMethods) {
                // This checks all the methods to ensure the classes in question exist.
                clazz.getDeclaredMethods();
            }

            return construct(clazz);

        // I can't believe I have to do this...
        } catch (Exception | NoClassDefFoundError e) {
            if (clazz.isAnnotationPresent(SkipOnError.class)) {
                this.serviceCollection.logger()
                        .warn(this.serviceCollection.messageProvider().getMessageString("startup.injectablenotloaded", clazz.getName()));
                return null;
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private <T extends Class<?>> Optional<T> checkPlatformOpt(T clazz) {
        if (checkPlatform(clazz)) {
            return Optional.of(clazz);
        }

        return Optional.empty();
    }

    private <T extends Class<?>> boolean checkPlatform(T clazz) {
        if (clazz.isAnnotationPresent(RequiresPlatform.class)) {
            String platformId = Sponge.getPlatform().getContainer(Platform.Component.GAME).getId();
            boolean loadable = Arrays.stream(clazz.getAnnotation(RequiresPlatform.class).value()).anyMatch(platformId::equalsIgnoreCase);
            if (!loadable) {
                this.serviceCollection.logger().warn("Not loading /" + clazz.getSimpleName() + ": platform " + platformId + " is not supported.");
                return false;
            }
        }

        return true;
    }

    protected final <I, S extends I> void register(Class<S> impl) {
        register(impl, this.serviceCollection.injector().getInstance(impl));
    }

    protected final <I, S extends I> void register(Class<I> api, Class<S> impl) {
        S object = this.serviceCollection.injector().getInstance(impl);
        Sponge.getServiceManager().setProvider(this.serviceCollection.pluginContainer(), api, object);
        register(api, object);
        register(impl, object);
    }

    protected final <I, S extends I> void register(Class<? super S> impl, S object) {
        this.serviceCollection.registerService(impl, object, false);
    }

    protected final <I, S extends I> void register(Class<I> internalApi, Class<S> impl, S object, boolean remap) {
        register(impl, object);
        this.serviceCollection.registerService(internalApi, object, remap);
    }

    protected final <I, S extends I> void register(Class<I> api, Class<S> impl, S object) {
        Sponge.getServiceManager().setProvider(this.serviceCollection.pluginContainer(), api, object);
        this.serviceCollection.registerService(api, object, false);
        register(impl, object);
    }

    private <T> Set<Class<? extends T>> getClassesFromList(String key) {
        List<String> list = this.objectTypesToClassListMap.get(key);
        if (list == null) {
            return new HashSet<>();
        }

        Set<Class<? extends T>> classes = new HashSet<>();
        for (String s : list) {
            try {
                checkPlatformOpt((Class<? extends T>) Class.forName(s)).ifPresent(classes::add);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return classes;
    }

    private <T> T construct(Class<T> cls) throws Exception {
        try {
            Constructor<T> c = cls.getDeclaredConstructor(INucleusServiceCollection.class);
            c.setAccessible(true);
            return c.newInstance(this.serviceCollection);
        } catch (NoSuchMethodException e) {
            // nope, do we have parameterless?
            try {
                Constructor<T> c = cls.getDeclaredConstructor();
                c.setAccessible(true);
                return c.newInstance();
            } catch (NoSuchMethodException ex) {
                // nope
                return this.serviceCollection.injector().getInstance(cls);
            }
        }
    }

}
