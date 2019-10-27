/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.services.IPlayerDisplayNameService;
import io.github.nucleuspowered.nucleus.services.ITextStyleService;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class ConfigFileMessagesRepository extends AbstractMessageRepository implements IMessageRepository {

    private static final Pattern KEYS = Pattern.compile("\\{(\\d+)}");

    private final Path file;
    private final Supplier<PropertiesMessageRepository> messageRepositorySupplier;
    private CommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();

    public ConfigFileMessagesRepository(
            ITextStyleService textStyleService,
            IPlayerDisplayNameService playerDisplayNameService,
            Path file,
            Supplier<PropertiesMessageRepository> messageRepositorySupplier) {
        super(textStyleService, playerDisplayNameService);
        this.file = file;
        this.messageRepositorySupplier = messageRepositorySupplier;
    }

    @Override
    public void invalidateIfNecessary() {
        this.cachedMessages.clear();
        this.cachedStringMessages.clear();
    }

    @Override public boolean hasEntry(String key) {
        return false;
    }

    @Override String getEntry(String key) {
        return null;
    }

    protected CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = SimpleCommentedConfigurationNode.root();
        PropertiesMessageRepository repository = this.messageRepositorySupplier.get();

        repository.getKeys()
                .forEach(x ->
                        ccn.getNode((Object[])x.split("\\.")).setValue(repository.getEntry(x)));

        return ccn;
    }

    protected HoconConfigurationLoader getLoader(Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }

    public Optional<String> getKey(@Nonnull String key) {
        Preconditions.checkNotNull(key);
        Object[] obj = key.split("\\.");
        return Optional.ofNullable(this.node.getNode(obj).getString());
    }

    public List<String> walkThroughForMismatched() {
        Matcher keyMatcher = KEYS.matcher("");
        final List<String> keysToFix = Lists.newArrayList();
        PropertiesMessageRepository propertiesMessageRepository = this.messageRepositorySupplier.get();
        propertiesMessageRepository.getKeys().forEach(x -> {
            String resKey = propertiesMessageRepository.getEntry(x);
            Optional<String> msgKey = getKey(x);
            if (msgKey.isPresent() && getTokens(resKey, keyMatcher) != getTokens(msgKey.get(), keyMatcher)) {
                keysToFix.add(x);
            }
        });

        return keysToFix;
    }

    public void fixMismatched(List<String> toFix) {
        Preconditions.checkNotNull(toFix);
        final PropertiesMessageRepository propertiesMessageRepository = this.messageRepositorySupplier.get();
        toFix.forEach(x -> {
            String resKey = propertiesMessageRepository.getEntry(x);
            Optional<String> msgKey = getKey(x);

            Object[] nodeKey = x.split("\\.");
            CommentedConfigurationNode cn = this.node.getNode(nodeKey).setValue(resKey);
            msgKey.ifPresent(cn::setComment);
        });

        save();
    }

    private int getTokens(String message, Matcher matcher) {
        int result = -1;

        matcher.reset(message);
        while (matcher.find()) {
            result = Math.max(result, Integer.parseInt(matcher.group(1)));
        }

        return result;
    }

    private void load(boolean mismatch) {
        try {
            this.node = getLoader(this.file).load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try {
            getLoader(this.file).save(this.node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
