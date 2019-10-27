/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.commandelement;

import com.google.inject.Inject;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusRequirePermissionArgument;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.services.ICommandElementSupplier;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;

import java.util.Optional;

public class CommandElementSupplier implements ICommandElementSupplier {

    private final IPermissionService permissionService;

    @Inject
    public CommandElementSupplier(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override public CommandElement createOnlyOtherUserPermissionElement(String permission) {
        return GenericArguments.optional(
                new NucleusRequirePermissionArgument(
                        NucleusParameters.ONE_USER,
                        this.permissionService,
                        permission
                )
        );
    }

    @Override public CommandElement createOtherUserPermissionElement(boolean isPlayer, String permission) {
        return GenericArguments.optionalWeak(
                new NucleusRequirePermissionArgument(
                    isPlayer ? NucleusParameters.ONE_PLAYER : NucleusParameters.ONE_USER,
                    this.permissionService,
                    permission
                )
        );
    }

    @Override public CommandElement createOnlyOtherUserPermissionElement(boolean isPlayer, String permission) {
        return GenericArguments.optional(
                new NucleusRequirePermissionArgument(
                        isPlayer ? NucleusParameters.ONE_PLAYER : NucleusParameters.ONE_USER,
                        this.permissionService,
                        permission
                )
        );
    }

    @Override public NucleusRequirePermissionArgument createPermissionParameter(CommandElement wrapped, String permission) {
        return new NucleusRequirePermissionArgument(wrapped, this.permissionService, permission);
    }

    @Override public User getUserFromParametersElseSelf(ICommandContext<? extends CommandSource> context) throws CommandException {
        Optional<User> user = context.getOne(NucleusParameters.Keys.USER, User.class).filter(context::is);
        if (!user.isPresent()) {
            return context.getIfPlayer();
        }

        // If not self, we set no cooldown etc.
        context.setCooldown(0);
        context.setCost(0);
        context.setWarmup(0);
        return user.map(x -> x.getPlayer().isPresent() ? x.getPlayer().get() : x).get();
    }

}
