/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.Collection;
import java.util.List;

public class HelpOpMessageChannel implements NucleusChatChannel.HelpOp {

    private final IPermissionService permissionService;

    public HelpOpMessageChannel(INucleusServiceCollection serviceCollection) {
        this.permissionService = serviceCollection.permissionService();
    }

    @Override
    public Collection<MessageReceiver> getMembers() {
        List<MessageReceiver> members = Lists.newArrayList(Sponge.getServer().getConsole());
        Sponge.getServer().getOnlinePlayers().stream()
                .filter(x -> this.permissionService.hasPermission(x, MessagePermissions.HELPOP_RECEIVE)).forEach(members::add);
        return members;
    }
}
