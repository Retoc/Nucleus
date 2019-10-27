/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.messagetoken;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.services.IMessageTokenService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.IPermissionService;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class NucleusTokenServiceImpl implements IMessageTokenService {

    private static final Pattern suffixPattern = Pattern.compile(":([sp]+)$", Pattern.CASE_INSENSITIVE);
    private final Tokens tokens;
    private final Map<String, TokenParser> tokenStore = Maps.newHashMap();
    private final Map<String, Tuple<TokenParser, String>> primaryTokenStore = Maps.newHashMap();
    private final Set<Tuple<String, String>> registered = Sets.newHashSet();
    private final List<Function<String, String>> replacements = Lists.newArrayList();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") private final Optional<Text> EMPTY = Optional.empty();
    private final IPermissionService permissionService;

    @Inject
    public NucleusTokenServiceImpl(INucleusServiceCollection serviceCollection) {
        this.permissionService = serviceCollection.permissionService();
        this.tokens = new Tokens(serviceCollection);
        try {
            register(serviceCollection.pluginContainer(), tokens);
            tokens.getTokenNames().forEach(x ->
            {
                try {
                    register(serviceCollection.pluginContainer(), this::getTextFromToken);
                } catch (PluginAlreadyRegisteredException e) {
                    e.printStackTrace();
                }
                registerPrimaryToken(
                        x.toLowerCase(),
                        serviceCollection.pluginContainer(),
                        x.toLowerCase());
            });
        } catch (PluginAlreadyRegisteredException e) {
            e.printStackTrace();
        }
    }

    @Override public void register(PluginContainer pluginContainer, TokenParser textFunction) throws PluginAlreadyRegisteredException {
        Preconditions.checkNotNull(pluginContainer);
        Preconditions.checkNotNull(textFunction);

        if (this.tokenStore.containsKey(pluginContainer.getId())) {
            throw new PluginAlreadyRegisteredException(pluginContainer);
        }

        this.tokenStore.put(pluginContainer.getId(), textFunction);
    }

    @Override public boolean unregister(PluginContainer pluginContainer) {
        Preconditions.checkNotNull(pluginContainer, "pluginContainer");
        Preconditions.checkState(!pluginContainer.getId().equalsIgnoreCase(PluginInfo.ID), "Cannot remove Nucleus tokens");
        TokenParser parser = this.tokenStore.remove(pluginContainer.getId());
        if (parser != null) {
            this.primaryTokenStore.entrySet().removeIf(x -> x.getValue().getFirst().equals(parser));
            return true;
        }

        return false;
    }

    @Override
    public boolean registerTokenFormat(String tokenStart, String tokenEnd, String replacement) {
        String s = tokenStart.trim();
        String e = tokenEnd.trim();
        Preconditions.checkArgument(!(s.contains("{{") || e.contains("}}")));
        if (this.registered.stream().anyMatch(x -> x.getFirst().equalsIgnoreCase(s) || x.getSecond().equalsIgnoreCase(e))) {
            return false;
        }

        // Create replacement regex.
        String replacementRegex = Pattern.quote(tokenStart.trim()) + "([^\\s{}]+)" + Pattern.quote(tokenEnd.trim());
        this.replacements.add(st -> st.replaceAll(replacementRegex, "{{" + replacement + "}}"));
        this.registered.add(Tuple.of(s, e));
        return true;
    }

    @Override public boolean registerPrimaryToken(String primaryIdentifier, PluginContainer registeringPlugin, String identiferToMapTo) {
        Preconditions.checkArgument(!primaryIdentifier.matches("^.*[\\s|{}:].*$"), "Token cannot contain spaces or \":|{}\"");
        if (this.tokenStore.containsKey(registeringPlugin.getId()) && !this.primaryTokenStore.containsKey(primaryIdentifier.toLowerCase())) {
            // Register!
            this.primaryTokenStore.put(primaryIdentifier.toLowerCase(), Tuple.of(this.tokenStore.get(registeringPlugin.getId()),
                    identiferToMapTo.toLowerCase()));
            return true;
        }

        return false;
    }

    @Override public Optional<TokenParser> getTokenParser(String plugin) {
        Preconditions.checkNotNull(plugin, "pluginContainer");
        return Optional.ofNullable(this.tokenStore.get(plugin.toLowerCase()));
    }

    @Override public Optional<Tuple<TokenParser, String>> getPrimaryTokenParserAndIdentifier(String primaryToken) {
        return Optional.ofNullable(this.primaryTokenStore.get(primaryToken.toLowerCase()));
    }

    @Override public List<String> getPrimaryTokens() {
        return ImmutableList.copyOf(this.primaryTokenStore.keySet());
    }

    @Override public Optional<Text> parseToken(String token, CommandSource source, @Nullable Map<String, Object> variables) {
        return getTextFromToken(token, source, variables);
    }

    @Override
    public String performReplacements(String string) {
        for (Function<String, String> replacementFunction : this.replacements) {
            string = replacementFunction.apply(string);
        }

        return string;
    }

    private Optional<Text> getTextFromToken(String token, CommandSource source, Map<String, Object> variables) {
        token = token.toLowerCase().trim().replace("{{", "").replace("}}", "");
        Matcher m = suffixPattern.matcher(token);
        boolean addSpace = false;
        boolean prependSpace = false;
        if (m.find(0)) {
            String match = m.group(1).toLowerCase();
            addSpace = match.contains("s");
            prependSpace = match.contains("p");

            token = token.replaceAll(":[sp]+$", "");
        }

        try {
            Optional<Text> toReturn;
            if (token.startsWith("pl:") || token.startsWith("p:")) {
                // Plugin identifiers are of the form pl:<pluginid>:<identifier>
                String[] tokSplit = token.split(":", 3);
                if (tokSplit.length < 3) {
                    return this.EMPTY;
                }

                toReturn = applyToken(tokSplit[1], tokSplit[2], source, variables);
            } else if (token.startsWith("o:")) { // Option identifier.
                toReturn = getTextFromOption(source, token.substring(2));
            } else {
                // Standard.
                toReturn = applyPrimaryToken(token, source, variables);
            }

            if (addSpace) {
                toReturn = toReturn.map(x -> x.isEmpty() ? x : Text.join(x, Util.SPACE));
            }

            if (prependSpace) {
                toReturn = toReturn.map(x -> x.isEmpty() ? x : Text.join(Util.SPACE, x));
            }

            return toReturn;
        } catch (Exception e) {
            e.printStackTrace();
            return this.EMPTY;
        }
    }

    private Optional<Text> getTextFromOption(CommandSource cs, String option) {
        if (cs instanceof Player) {
            return this.permissionService.getOptionFromSubject(cs, option).map(TextSerializers.FORMATTING_CODE::deserialize);
        }

        return Optional.empty();
    }

    @Override public Tokens getNucleusTokenParser() {
        return this.tokens;
    }
}
