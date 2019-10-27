/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.parameters;

import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.modules.kit.KitPermissions;
import io.github.nucleuspowered.nucleus.modules.kit.services.KitHandler;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class KitParameter extends CommandElement {

    public final static String KIT_PARAMETER_KEY = "kit";
    private final KitHandler kitHandler;
    private final IPermissionService permissionService;
    private final IMessageProviderService messageProviderService;
    private boolean permissionCheck;

    public KitParameter(KitHandler handler,
            IMessageProviderService messageProviderService,
            IPermissionService permissionService,
            boolean permissionCheck) {
        super(Text.of(KIT_PARAMETER_KEY));
        this.kitHandler = handler;
        this.messageProviderService = messageProviderService;
        this.permissionCheck = permissionCheck;
        this.permissionService = permissionService;
    }

    @Nullable
    @Override
    protected Object parseValue(@NonNull CommandSource source, CommandArgs args) throws ArgumentParseException {
        String kitName = args.next();
        if (kitName.isEmpty()) {
            throw args.createError(this.messageProviderService.getMessageFor(source, "args.kit.noname"));
        }

        Kit kit = this.kitHandler.getKit(kitName)
                .orElseThrow(() -> args.createError(this.messageProviderService.getMessageFor(source,"args.kit.noexist")));

        if (!checkPermission(source, kit)) {
            throw args.createError(this.messageProviderService.getMessageFor(source,"args.kit.noperms"));
        }

        return kit;
    }

    @Override
    @NonNull
    public List<String> complete(@NonNull CommandSource src, CommandArgs args, @NonNull CommandContext context) {
        try {
            final boolean showhidden = this.permissionService.hasPermission(src, KitPermissions.KIT_SHOWHIDDEN);
            String name = args.peek().toLowerCase();
            return this.kitHandler.getKitNames().stream()
                    .filter(s -> s.toLowerCase().startsWith(name))
                    .limit(20)
                    .map(x -> this.kitHandler.getKit(x).get())
                    .filter(x -> checkPermission(src, x))
                    .filter(x -> this.permissionCheck && (showhidden || !x.isHiddenFromList()))
                    .map(x -> x.getName().toLowerCase())
                    .collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return ImmutableList.of();
        }
    }

    private boolean checkPermission(CommandSource src, Kit kit) {
        if (!this.permissionCheck) {
            return true;
        }

        // No permissions, no entry!
        return this.permissionService.hasPermission(src, KitPermissions.getKitPermission(kit.getName()));
    }

}
