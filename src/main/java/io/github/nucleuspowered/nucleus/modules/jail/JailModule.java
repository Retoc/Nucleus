/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.quickstart.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.messagetoken.Tokens;
import io.github.nucleuspowered.nucleus.modules.jail.commands.CheckJailCommand;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.services.JailHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

@ModuleData(id = JailModule.ID, name = "Jail")
public class JailModule extends ConfigurableModule<JailConfig, JailConfigAdapter> {

    public static final String ID = "jail";

    @Inject
    public JailModule(Supplier<DiscoveryModuleHolder<?, ?>> moduleHolder, INucleusServiceCollection collection) {
        super(moduleHolder, collection);
    }

    @Override
    public JailConfigAdapter createAdapter() {
        return new JailConfigAdapter();
    }

    @Override
    protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.<String, Tokens.Translator>builder()
                .put("jailed", new Tokens.TrueFalseVariableTranslator() {
                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                    final Optional<Text> def = Optional.of(Text.of(TextColors.GRAY, "[Jailed]"));

                    @Override protected Optional<Text> getDefault() {
                        return this.def;
                    }

                    @Override protected boolean condition(CommandSource commandSource) {
                        return commandSource instanceof Player &&
                                serviceCollection.getServiceUnchecked(JailHandler.class).isPlayerJailed((Player) commandSource);
                    }
                })
                .put("jail", (source, variableString, variables) -> {
                    if (source instanceof Player) {
                        return serviceCollection.getServiceUnchecked(JailHandler.class).getPlayerJailData((Player) source).map(x -> Text.of(x.getJailName()));
                    }

                    return Optional.empty();
                })
                .build();
    }
}
