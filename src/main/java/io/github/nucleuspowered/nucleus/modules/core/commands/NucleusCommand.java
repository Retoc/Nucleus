/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import static io.github.nucleuspowered.nucleus.PluginInfo.GIT_HASH;
import static io.github.nucleuspowered.nucleus.PluginInfo.NAME;
import static io.github.nucleuspowered.nucleus.PluginInfo.VERSION;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.core.CorePermissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.quickstart.ModuleHolder;

import java.util.Set;

import javax.annotation.Nullable;

@Command(
        aliases = "nucleus",
        basePermission = CorePermissions.BASE_NUCLEUS,
        commandDescriptionKey = "nucleus",
        prefixAliasesWithN = false
)
@NonnullByDefault
public class NucleusCommand implements ICommandExecutor<CommandSource> {

    private final Text version = Text.of(TextColors.GREEN, NAME + " version " + VERSION + " (built from commit " + GIT_HASH + ")");
    @Nullable private Text modules = null;

    @Override
    public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        if (this.modules == null) {
            Text.Builder tb = Text.builder("Modules: ").color(TextColors.GREEN);

            boolean addComma = false;
            Set<String> enabled = Nucleus.getNucleus().getModuleHolder().getModules(ModuleHolder.ModuleStatusTristate.ENABLE);
            for (String module : Nucleus.getNucleus().getModuleHolder().getModules(ModuleHolder.ModuleStatusTristate.ALL)) {
                if (addComma) {
                    tb.append(Text.of(TextColors.GREEN, ", "));
                }

                tb.append(Text.of(enabled.contains(module) ? TextColors.GREEN : TextColors.RED, module));
                addComma = true;
            }

            this.modules = tb.append(Text.of(TextColors.GREEN, ".")).build();
        }

        context.getCommandSource().sendMessage(this.version);
        context.getCommandSource().sendMessage(this.modules);
        return context.successResult();
    }
}
