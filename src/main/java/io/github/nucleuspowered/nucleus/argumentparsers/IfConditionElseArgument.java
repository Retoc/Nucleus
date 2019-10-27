/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.services.IPermissionService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;

import java.util.List;

import javax.annotation.Nullable;

public class IfConditionElseArgument extends CommandElement {

    private final Condition predicate;
    private final CommandElement trueElement;
    private final CommandElement falseElement;
    private final IPermissionService permissionService;

    public static IfConditionElseArgument permission(
            IPermissionService permissionService,
            String permission,
            CommandElement ifSo,
            CommandElement ifNot) {
        return new IfConditionElseArgument(permissionService, ifSo, ifNot, (p, s, c) -> p.hasPermission(s, permission));
    }

    public IfConditionElseArgument(
            IPermissionService permissionService,
            CommandElement trueElement,
            CommandElement falseElement,
            Condition predicate) {
        super(trueElement.getKey());
        this.permissionService = permissionService;
        this.trueElement = trueElement;
        this.falseElement = falseElement;
        this.predicate = predicate;
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) {
        return null;
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (this.predicate.test(this.permissionService, source, context)) {
            this.trueElement.parse(source, args, context);
        } else {
            this.falseElement.parse(source, args, context);
        }
    }

    @Override public List<String> complete(CommandSource source, CommandArgs args, CommandContext context) {
        if (this.predicate.test(this.permissionService, source, context)) {
            return this.trueElement.complete(source, args, context);
        } else {
            return this.falseElement.complete(source, args, context);
        }
    }

    @FunctionalInterface
    public interface Condition {

        boolean test(IPermissionService permissionService, CommandSource commandSource, CommandContext context);

    }
}
