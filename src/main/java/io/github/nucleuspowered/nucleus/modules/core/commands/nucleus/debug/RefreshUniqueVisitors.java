package io.github.nucleuspowered.nucleus.modules.core.commands.nucleus.debug;

import io.github.nucleuspowered.nucleus.internal.command.ICommandContext;
import io.github.nucleuspowered.nucleus.internal.command.ICommandExecutor;
import io.github.nucleuspowered.nucleus.internal.command.ICommandResult;
import io.github.nucleuspowered.nucleus.internal.command.annotation.Command;
import io.github.nucleuspowered.nucleus.modules.core.CorePermissions;
import io.github.nucleuspowered.nucleus.modules.core.commands.nucleus.DebugCommand;
import io.github.nucleuspowered.nucleus.modules.core.services.UniqueUserService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Command(
        aliases = "refreshuniquevisitors",
        basePermission = CorePermissions.BASE_DEBUG_REFRESHUNIQUEVISITORS,
        commandDescriptionKey = "nucleus.debug.refreshuniquevisitors",
        parentCommand = DebugCommand.class
)
public class RefreshUniqueVisitors implements ICommandExecutor<CommandSource> {

    @Override public ICommandResult execute(ICommandContext<? extends CommandSource> context) throws CommandException {
        UniqueUserService uus = context.getServiceCollection().getServiceUnchecked(UniqueUserService.class);
        context.sendMessage("command.nucleus.debug.refreshuniquevisitors.started", uus.getUniqueUserCount());

        Optional<UUID> optionalUUID = context.getUniqueId();
        Supplier<CommandSource> scs;
        if (optionalUUID.isPresent()) {
            final UUID uuid = optionalUUID.get();
            scs = () -> Sponge.getServer().getPlayer(uuid).map(x -> (CommandSource) x).orElseGet(Sponge.getServer()::getConsole);
        } else {
            scs = Sponge.getServer()::getConsole;
        }

        uus.resetUniqueUserCount(l -> context.sendMessageTo(scs.get(), "command.nucleus.debug.refreshuniquevisitors.done", l));
        return context.successResult();
    }
}
