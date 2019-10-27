/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services;

import com.google.inject.ImplementedBy;
import io.github.nucleuspowered.nucleus.argumentparsers.NoModifiersArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusRequirePermissionArgument;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.services.impl.permission.NucleusPermissionService;
import io.github.nucleuspowered.nucleus.services.impl.permission.PermissionMetadata;
import io.github.nucleuspowered.nucleus.services.impl.permission.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;

import java.util.List;
import java.util.Optional;

@ImplementedBy(NucleusPermissionService.class)
public interface IPermissionService {

    boolean isOpOnly();

    void registerContextCalculator(ContextCalculator<Subject> calculator);

    void checkServiceChange(ProviderRegistration<PermissionService> service);

    boolean hasPermission(Subject subject, String permission);

    boolean hasPermissionWithConsoleOverride(Subject subject, String permission, boolean permissionIfConsoleAndOverridden);

    boolean isConsoleOverride(Subject subject);

    void registerDescriptions();

    void register(String permission, PermissionMetadata metadata, String moduleid);

    default CommandElement createOtherUserPermissionElement(String permission) {
        return GenericArguments.optionalWeak(new NucleusRequirePermissionArgument(
                new NoModifiersArgument<>(
                        NucleusParameters.ONE_USER,
                        NoModifiersArgument.USER_NOT_CALLER_PREDICATE
                ),
                this,
                permission));
    }

    default NucleusRequirePermissionArgument createPermissionParameter(CommandElement wrapped, String permission) {
        return new NucleusRequirePermissionArgument(wrapped, this, permission);
    }

    Optional<Double> getDoubleOptionFromSubject(Subject player, String... options);

    Optional<Long> getPositiveLongOptionFromSubject(Subject player, String... options);

    Optional<Integer> getPositiveIntOptionFromSubject(Subject player, String... options);

    Optional<Integer> getIntOptionFromSubject(Subject player, String... options);

    Optional<String> getOptionFromSubject(Subject player, String... options);

    PermissionMessageChannel permissionMessageChannel(String permission);

    List<Metadata> getAllMetadata();

    Optional<Metadata> getMetadataFor(String permission);

    interface Metadata {

        boolean isPrefix();

        SuggestedLevel getSuggestedLevel();

        String getDescription(IMessageProviderService service);

        String getPermission();

        String getModuleId();
    }

}
