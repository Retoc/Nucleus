/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.modules.core.CoreModule;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfig;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.services.VanishService;
import io.github.nucleuspowered.nucleus.quickstart.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.messagetoken.Tokens;
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

@ModuleData(id = "vanish", name = "Vanish", dependencies = CoreModule.ID)
public class VanishModule extends ConfigurableModule<VanishConfig, VanishConfigAdapter> {

    @Inject
    public VanishModule(Supplier<DiscoveryModuleHolder<?, ?>> moduleHolder, INucleusServiceCollection collection) {
        super(moduleHolder, collection);
    }

    @Override
    public VanishConfigAdapter createAdapter() {
        return new VanishConfigAdapter();
    }

    @Override protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.<String, Tokens.Translator>builder()
                .put("vanished", new Tokens.TrueFalseVariableTranslator() {
                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") final Optional<Text> def =
                            Optional.of(Text.of(TextColors.GRAY, "[Vanished]"));

                    @Override protected Optional<Text> getDefault() {
                        return this.def;
                    }

                    @Override protected boolean condition(CommandSource commandSource) {
                        return commandSource instanceof Player &&
                                serviceCollection.getServiceUnchecked(VanishService.class)
                                        .isVanished((Player) commandSource);
                    }
                }).build();
    }

}
