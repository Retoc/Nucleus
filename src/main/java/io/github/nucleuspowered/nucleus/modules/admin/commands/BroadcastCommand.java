/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.internal.command.annotation.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.admin.AdminPermissions;
import io.github.nucleuspowered.nucleus.modules.admin.config.AdminConfig;
import io.github.nucleuspowered.nucleus.modules.admin.config.BroadcastConfig;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.texttemplatefactory.NucleusTextTemplateMessageSender;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Command(aliases = {"broadcast", "bcast", "bc"}, basePermission = AdminPermissions.BASE_BROADCAST, commandDescriptionKey = "broadcast")
@EssentialsEquivalent({"broadcast", "bcast"})
@NonnullByDefault
public class BroadcastCommand implements ICommandExecutor<CommandSource>, Reloadable {
    private BroadcastConfig bc = new BroadcastConfig();

    @Override
    public CommandElement[] parameters(INucleusServiceCollection serviceCollection) {
        return new CommandElement[] {
                NucleusParameters.MESSAGE
        };
    }

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        String m = context.requireOne(NucleusParameters.Keys.MESSAGE, TypeTokens.STRING_TOKEN);

        Text p = this.bc.getPrefix().getForCommandSource(context.getCommandSource());
        Text s = this.bc.getSuffix().getForCommandSource(context.getCommandSource());

        NucleusTextTemplate textTemplate =
                context.getServiceCollection().textTemplateFactory()
                        .createFromAmpersandString(m, p, s);

        new NucleusTextTemplateMessageSender(
                context.getServiceCollection().textTemplateFactory(),
                textTemplate,
                context.getServiceCollection().messageTokenService(),
                context.getCommandSource()
        ).send(context.getCause());
        return context.successResult();
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        this.bc = serviceCollection
                .moduleDataProvider()
                .getModuleConfig(AdminConfig.class)
                .getBroadcastMessage();
    }
}
