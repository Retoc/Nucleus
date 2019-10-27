/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommandInterceptors;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.quickstart.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.messagetoken.Tokens;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.interceptors.AFKCommandInterceptor;
import io.github.nucleuspowered.nucleus.modules.afk.services.AFKHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.holders.DiscoveryModuleHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

@RegisterCommandInterceptors(AFKCommandInterceptor.class)
@ModuleData(id = AFKModule.ID, name = "AFK")
public class AFKModule extends ConfigurableModule<AFKConfig, AFKConfigAdapter> {

    public static final String ID = "afk";

    @Inject
    public AFKModule(Supplier<DiscoveryModuleHolder<?, ?>> moduleHolder, INucleusServiceCollection collection) {
        super(moduleHolder, collection);
    }

    @Override
    public AFKConfigAdapter createAdapter() {
        return new AFKConfigAdapter();
    }

    @Override protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.<String, Tokens.Translator>builder()
                .put("afk", new Tokens.TrueFalseVariableTranslator() {
                    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                    final Optional<Text> def = Optional.of(Text.of(TextColors.GRAY, "[AFK]"));

                    @Override protected Optional<Text> getDefault() {
                        return this.def;
                    }

                    @Override protected boolean condition(CommandSource source) {
                        return source instanceof Player && serviceCollection.getServiceUnchecked(AFKHandler.class).isAFK((Player) source);
                    }
                })
                .build();
    }

}
