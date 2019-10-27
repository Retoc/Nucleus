/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.permission;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import io.github.nucleuspowered.nucleus.services.IReloadableService;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import io.github.nucleuspowered.nucleus.util.PrettyPrinter;
import io.github.nucleuspowered.nucleus.util.ThrownFunction;
import org.slf4j.event.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NucleusPermissionService implements IPermissionService, Reloadable {

    private final IMessageProviderService messageProviderService;
    private boolean init = false;
    private boolean useRole = false;
    private boolean isOpOnly = true;
    private boolean consoleOverride = false;
    private final Set<ContextCalculator<Subject>> contextCalculators = new HashSet<>();
    private final Set<String> failedChecks = new HashSet<>();
    private final Map<String, IPermissionService.Metadata> metadataMap = new HashMap<>();
    private final Map<String, IPermissionService.Metadata> prefixMetadataMap = new HashMap<>();

    @Inject NucleusPermissionService(
            IMessageProviderService messageProviderService,
            IReloadableService service) {
        this.messageProviderService = messageProviderService;
        service.registerReloadable(this);
    }

    @Override public boolean isOpOnly() {
        return this.isOpOnly;
    }

    @Override public void registerContextCalculator(ContextCalculator<Subject> calculator) {
        this.contextCalculators.add(calculator);
    }

    @Override public void checkServiceChange(ProviderRegistration<PermissionService> service) {

    }

    @Override public boolean hasPermission(Subject permissionSubject, String permission) {
        return hasPermission(permissionSubject, permission, this.useRole);
    }

    @Override public boolean hasPermissionWithConsoleOverride(Subject subject, String permission, boolean permissionIfConsoleAndOverridden) {
        if (this.consoleOverride && subject instanceof ConsoleSource) {
            return permissionIfConsoleAndOverridden;
        }

        return hasPermission(subject, permission);
    }

    @Override public boolean isConsoleOverride(Subject subject) {
        return this.consoleOverride && subject instanceof ConsoleSource;
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {

    }

    @Override public void registerDescriptions() {
        Preconditions.checkState(!this.init);
        this.init = true;
        PermissionService ps = Sponge.getServiceManager().provide(PermissionService.class).orElse(null);
        boolean isPresent = ps != null;

        for (Map.Entry<String, IPermissionService.Metadata> entry : this.metadataMap.entrySet()) {
            SuggestedLevel level = entry.getValue().getSuggestedLevel();
            if (isPresent && level.getRole() != null) {
                ps.newDescriptionBuilder(Nucleus.getNucleus())
                        .assign(level.getRole(), true)
                        .description(Text.of(entry.getValue().getDescription(this.messageProviderService)))
                        .id(entry.getKey()).register();
            }
        }
    }

    @Override public void register(String permission, PermissionMetadata metadata, String moduleid) {
        NucleusPermissionService.Metadata m = new NucleusPermissionService.Metadata(permission, metadata, moduleid);
        if (metadata.isPrefix()) {
            this.prefixMetadataMap.put(permission.toLowerCase(), m);
        } else {
            this.metadataMap.put(permission.toLowerCase(), m);
        }
    }

    @Override public Optional<Double> getDoubleOptionFromSubject(Subject player, String... options) {
        return getTypedObjectFromSubject(Double::parseDouble, player, options);
    }

    @Override public Optional<Long> getPositiveLongOptionFromSubject(Subject player, String... options) {
        return getTypedObjectFromSubject(Long::parseUnsignedLong, player, options);
    }

    @Override public Optional<Integer> getPositiveIntOptionFromSubject(Subject player, String... options) {
        return getTypedObjectFromSubject(Integer::parseUnsignedInt, player, options);
    }

    @Override public Optional<Integer> getIntOptionFromSubject(Subject player, String... options) {
        return getTypedObjectFromSubject(Integer::parseInt, player, options);
    }

    private <T> Optional<T> getTypedObjectFromSubject(ThrownFunction<String, T, Exception> conversion, Subject player, String... options) {
        try {
            Optional<String> optional = getOptionFromSubject(player, options);
            if (optional.isPresent()) {
                return Optional.ofNullable(conversion.apply(optional.get()));
            }
        } catch (Exception e) {
            // ignored
        }

        return Optional.empty();
    }

    @Override public Optional<String> getOptionFromSubject(Subject player, String... options) {
        for (String option : options) {
            String o = option.toLowerCase();

            // Option for context.
            Optional<String> os = player.getOption(player.getActiveContexts(), o);
            if (os.isPresent()) {
                return os.map(r -> r.isEmpty() ? null : r);
            }

            // General option
            os = player.getOption(o);
            if (os.isPresent()) {
                return os.map(r -> r.isEmpty() ? null : r);
            }
        }

        return Optional.empty();
    }

    @Override public PermissionMessageChannel permissionMessageChannel(String permission) {
        return new PermissionMessageChannel(this, permission);
    }

    @Override public List<IPermissionService.Metadata> getAllMetadata() {
        return ImmutableList.copyOf(this.metadataMap.values());
    }

    private boolean hasPermission(Subject subject, String permission, boolean checkRole) {
        if (checkRole && permission.startsWith("nucleus.")) {
            Tristate tristate = subject.getPermissionValue(subject.getActiveContexts(), permission);
            if (tristate == Tristate.UNDEFINED) {
                @Nullable IPermissionService.Metadata result = this.metadataMap.get(permission);
                if (result != null) { // check the "parent" perm
                    String perm = result.getSuggestedLevel().getPermission();
                    if (perm == null) {
                        return false;
                    } else {
                        return subject.hasPermission(perm);
                    }
                }

                for (Map.Entry<String, IPermissionService.Metadata> entry : this.prefixMetadataMap.entrySet()) {
                    if (permission.startsWith(entry.getKey())) {
                        String perm = entry.getValue().getSuggestedLevel().getPermission();
                        if (perm == null) {
                            return false;
                        } else {
                            return subject.hasPermission(perm);
                        }
                    }
                }

                // if we get here, no registered permissions were found
                // therefore, warn
                if (this.failedChecks.add(permission)) {
                    PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Nucleus Permission Not Registered").centre().hr();
                    printer.add("Nucleus has not registered a permission properly. This is an error in Nucleus - please report to the Nucleus "
                            + "github.");
                    printer.hr();
                    printer.add("Permission: %s", permission);
                    printer.log(Nucleus.getNucleus().getLogger(), Level.WARN);
                }

                // guarantees that the subject default is selected.
                return subject.hasPermission(permission);
            }

            return tristate.asBoolean();
        }

        return subject.hasPermission(permission);
    }

    @Override
    public Optional<IPermissionService.Metadata> getMetadataFor(String permission) {
        this.metadataMap.get(permission);
        return Optional.empty();
    }

    public static class Metadata implements IPermissionService.Metadata {

        private final String description;
        private final String permission;
        private final SuggestedLevel suggestedLevel;
        private final boolean isPrefix;
        private final String[] replacements;
        private final String moduleid;

        Metadata(String permission, PermissionMetadata metadata, String moduleid) {
            this(
                    metadata.descriptionKey(),
                    metadata.replacements(),
                    permission,
                    metadata.level(),
                    metadata.isPrefix(),
                    moduleid
            );
        }

        Metadata(String description,
                String[] replacements,
                String permission,
                SuggestedLevel suggestedLevel,
                boolean isPrefix,
                String moduleid) {
            this.description = description;
            this.replacements = replacements;
            this.permission = permission.toLowerCase();
            this.suggestedLevel = suggestedLevel;
            this.isPrefix = isPrefix;
            this.moduleid = moduleid;
        }

        @Override public boolean isPrefix() {
            return this.isPrefix;
        }

        @Override public SuggestedLevel getSuggestedLevel() {
            return this.suggestedLevel;
        }

        @Override public String getDescription(IMessageProviderService service) {
            return service.getMessageString(this.description, this.replacements);
        }

        @Override public String getPermission() {
            return this.permission;
        }

        @Override public String getModuleId() {
            return this.moduleid;
        }

    }

}
