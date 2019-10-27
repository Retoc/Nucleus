/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.freezeplayer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.quickstart.module.StandardModule;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.messagetoken.Tokens;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.commands.FreezePlayerCommand;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.services.FreezePlayerService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

@ModuleData(id = "freeze-subject", name = "Freeze Player")
public class FreezePlayerModule extends StandardModule {

    @Inject
    public FreezePlayerModule(Supplier<DiscoveryModuleHolder<?, ?>> moduleHolder,
            INucleusServiceCollection collection) {
        super(moduleHolder, collection);
    }

    @Override protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.<String, Tokens.Translator>builder()
                .put("frozen", new Tokens.TrueFalseVariableTranslator() {
                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                    final Optional<Text> def = Optional.of(Text.of(TextColors.GRAY, "[Frozen]"));

                    @Override protected Optional<Text> getDefault() {
                        return this.def;
                    }

                    @Override protected boolean condition(CommandSource commandSource) {
                        return commandSource instanceof Player &&
                                serviceCollection.getServiceUnchecked(FreezePlayerService.class).getFromUUID(((Player) commandSource).getUniqueId());
                    }
                })
                .build();
    }
}
