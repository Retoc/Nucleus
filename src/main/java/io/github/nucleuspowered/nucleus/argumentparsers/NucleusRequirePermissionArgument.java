/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.argumentparsers.util.WrappedElement;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;

import javax.annotation.Nullable;

public class NucleusRequirePermissionArgument extends WrappedElement {

    private final String permission;
    private final IPermissionService permissionService;

    public NucleusRequirePermissionArgument(CommandElement wrapped, IPermissionService permissionService, String permission) {
        super(wrapped);
        this.permissionService = permissionService;
        this.permission = permission;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (!this.permissionService.hasPermission(source, this.permission)) {
            Text key = getKey();
            throw args.createError(t("You do not have permission to use the %s argument", key != null ? key : t("unknown")));
        }
        getWrappedElement().parse(source, args, context);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        if (!this.permissionService.hasPermission(src, this.permission)) {
            return ImmutableList.of();
        }
        return getWrappedElement().complete(src, args, context);
    }
}
