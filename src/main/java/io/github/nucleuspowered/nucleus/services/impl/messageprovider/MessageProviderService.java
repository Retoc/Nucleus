/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.messageprovider;

import io.github.nucleuspowered.nucleus.guice.ConfigDirectory;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.services.IMessageProviderService;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository.ConfigFileMessagesRepository;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository.IMessageRepository;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository.PropertiesMessageRepository;
import io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository.UTF8Control;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MessageProviderService implements IMessageProviderService, Reloadable {

    private static final String MESSAGES_BUNDLE = "assets.nucleus.messages";

    private static final String MESSAGES_BUNDLE_RESOURCE_LOC = "/assets/nucleus/messages.properties.{0}";

    private final INucleusServiceCollection serviceCollection;

    private Locale defaultLocale = Sponge.getServer().getConsole().getLocale();
    private boolean useMessagesFile;
    private boolean useClientLocalesWhenPossible;

    private final PropertiesMessageRepository defaultMessagesResource;
    private final ConfigFileMessagesRepository configFileMessagesRepository;

    private final Map<Locale, PropertiesMessageRepository> messagesMap = new HashMap<>();

    @Inject
    MessageProviderService(INucleusServiceCollection serviceCollection, @ConfigDirectory Path configPath) {
        this.serviceCollection = serviceCollection;
        serviceCollection.reloadableService().registerReloadable(this);
        this.defaultMessagesResource = new PropertiesMessageRepository(
                serviceCollection.textStyleService(),
                serviceCollection.playerDisplayNameService(),
                ResourceBundle.getBundle(MESSAGES_BUNDLE, Locale.ROOT, UTF8Control.INSTANCE));
        this.configFileMessagesRepository = new ConfigFileMessagesRepository(
                serviceCollection.textStyleService(),
                serviceCollection.playerDisplayNameService(),
                configPath.resolve("messages.conf"),
                () -> getPropertiesMessagesRepository(this.defaultLocale)
        );
    }

    @Override
    public boolean hasKey(String key) {
        return getMessagesRepository(this.defaultLocale).hasEntry(key);
    }

    @Override
    public Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    @Override public Text getMessageFor(Locale locale, String key) {
        return getMessagesRepository(locale).getText(key);
    }

    @Override
    public Text getMessageFor(Locale locale, String key, Text... args) {
        return getMessagesRepository(locale).getText(key, args);
    }

    @Override public Text getMessageFor(Locale locale, String key, Object... replacements) {
        return getMessagesRepository(locale).getText(key, replacements);
    }

    @Override public Text getMessageFor(Locale locale, String key, String... replacements) {
        return getMessagesRepository(locale).getText(key, replacements);
    }

    @Override public String getMessageString(Locale locale, String key, String... replacements) {
        return getMessagesRepository(locale).getString(key, replacements);
    }

    @Override public String getMessageString(Locale locale, String key, Object... replacements) {
        return  getMessagesRepository(locale).getString(key, replacements);
    }

    @Override public boolean reloadMessageFile() {
        if (this.useMessagesFile) {
            this.configFileMessagesRepository.invalidateIfNecessary();
            return true;
        }

        return false;
    }

    @Override public void onReload(INucleusServiceCollection serviceCollection) {
        CoreConfig coreConfig = serviceCollection.moduleDataProvider().getModuleConfig(CoreConfig.class);
        this.useMessagesFile = coreConfig.isCustommessages();
        this.useClientLocalesWhenPossible = coreConfig.isClientLocaleWhenPossible();
        this.defaultLocale = Locale.forLanguageTag(coreConfig.getServerLocale());
        reloadMessageFile();
    }

    @Override public IMessageRepository getMessagesRepository(Locale locale) {
        if (this.useMessagesFile) {
            return this.configFileMessagesRepository;
        }

        return getPropertiesMessagesRepository(locale);
    }

    @Override public ConfigFileMessagesRepository getConfigFileMessageRepository() {
        return this.configFileMessagesRepository;
    }

    @Override public String getTimeString(Locale locale, Duration duration) {
        return getTimeString(locale, duration.getSeconds());
    }

    @Override public String getTimeString(Locale locale, long time) {
        time = Math.abs(time);
        long sec = time % 60;
        long min = (time / 60) % 60;
        long hour = (time / 3600) % 24;
        long day = time / 86400;

        if (time == 0) {
            return getMessageString(locale, "standard.inamoment");
        }

        StringBuilder sb = new StringBuilder();
        if (day > 0) {
            sb.append(day).append(" ");
            if (day > 1) {
                sb.append(getMessageString(locale, "standard.days"));
            } else {
                sb.append(getMessageString(locale, "standard.day"));
            }
        }

        if (hour > 0) {
            appendComma(sb);
            sb.append(hour).append(" ");
            if (hour > 1) {
                sb.append(getMessageString(locale, "standard.hours"));
            } else {
                sb.append(getMessageString(locale, "standard.hour"));
            }
        }

        if (min > 0) {
            appendComma(sb);
            sb.append(min).append(" ");
            if (min > 1) {
                sb.append(getMessageString(locale, "standard.minutes"));
            } else {
                sb.append(getMessageString(locale, "standard.minute"));
            }
        }

        if (sec > 0) {
            appendComma(sb);
            sb.append(sec).append(" ");
            if (sec > 1) {
                sb.append(getMessageString(locale, "standard.seconds"));
            } else {
                sb.append(getMessageString(locale, "standard.second"));
            }
        }

        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return getMessageString(locale, "standard.unknown");
        }
    }

    private void appendComma(StringBuilder stringBuilder) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(", ");
        }
    }

    private PropertiesMessageRepository getPropertiesMessagesRepository(Locale locale) {
        final Locale toUse;
        if (this.useClientLocalesWhenPossible) {
            toUse = locale;
        } else {
            toUse = this.defaultLocale;
        }

        return this.messagesMap.computeIfAbsent(locale, key -> {
            if (getClass().getClassLoader().getResource(MessageFormat.format(MESSAGES_BUNDLE_RESOURCE_LOC, toUse.toLanguageTag())) != null) {
                // it exists
                return new PropertiesMessageRepository(
                        this.serviceCollection.textStyleService(),
                        this.serviceCollection.playerDisplayNameService(),
                        ResourceBundle.getBundle(MESSAGES_BUNDLE, locale, UTF8Control.INSTANCE));
            } else {
                return this.defaultMessagesResource;
            }
        });
    }

}
