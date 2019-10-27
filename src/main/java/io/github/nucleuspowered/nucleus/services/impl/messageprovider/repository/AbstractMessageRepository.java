/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.services.impl.messageprovider.repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.services.IPlayerDisplayNameService;
import io.github.nucleuspowered.nucleus.services.ITextStyleService;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.translation.Translatable;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

abstract class AbstractMessageRepository implements IMessageRepository {

    private final static Pattern STRING_REPLACER = Pattern.compile("\\{+[^0-9]+}+");
    private final static Pattern STRING_LOCALISER = Pattern.compile("loc:([a-z\\-.]+)");

    final Map<String, String> cachedStringMessages = new HashMap<>();
    final Map<String, TextTemplate> cachedMessages = new HashMap<>();
    private final IPlayerDisplayNameService playerDisplayNameService;
    private final ITextStyleService textStyleService;

    AbstractMessageRepository(
            ITextStyleService textStyleService,
            IPlayerDisplayNameService playerDisplayNameService) {
        this.textStyleService = textStyleService;
        this.playerDisplayNameService = playerDisplayNameService;
    }

    abstract String getEntry(String key);

    private String getStringEntry(String key) {
        return STRING_REPLACER.matcher(
                getEntry(key).replaceAll("'", "''")
        ).replaceAll("'$0'");
    }

    private TextTemplate getTextTemplate(String key) {
        return this.cachedMessages.computeIfAbsent(key, k -> templateCreator(getEntry(k)));
    }

    @Override
    public Text getText(String key) {
        return this.cachedMessages.computeIfAbsent(key, this::getTextTemplate).toText();
    }

    @Override
    public Text getText(String key, Object[] args) {
        return getTextMessageWithTextFormat(key,
                Arrays.stream(args).map(x -> {
                    if (x instanceof User) {
                        return this.playerDisplayNameService.getDisplayName(((User) x).getUniqueId());
                    } else if (x instanceof TextRepresentable) {
                        return (TextRepresentable) x;
                    } else if (x instanceof Translatable) {
                        return Text.of(x);
                    } else if (x instanceof String) {
                        String s = (String) x;
                        Matcher matcher = STRING_LOCALISER.matcher(s);
                        if (matcher.matches()) {
                             return getText(matcher.group(1));
                        }

                        return Text.of(x);
                    } else {
                        return Text.of(x.toString());
                    }
                }).collect(Collectors.toList()));
    }

    @Override
    public String getString(String key) {
        return this.cachedStringMessages.computeIfAbsent(key, this::getStringEntry);
    }

    @Override
    public String getString(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }

    private Text getTextMessageWithTextFormat(String key, List<? extends TextRepresentable> textList) {
        TextTemplate template = getTextTemplate(key);
        if (textList.isEmpty()) {
            return template.toText();
        }

        Map<String, TextRepresentable> objs = Maps.newHashMap();
        for (int i = 0; i < textList.size(); i++) {
            objs.put(String.valueOf(i), textList.get(i));
        }

        return template.apply(objs).build();
    }

    final TextTemplate templateCreator(String string) {
        // regex!
        Matcher mat = Pattern.compile("\\{([\\d]+)}").matcher(string);
        List<Integer> map = Lists.newArrayList();

        while (mat.find()) {
            map.add(Integer.parseInt(mat.group(1)));
        }

        String[] s = string.split("\\{([\\d]+)}");

        List<Object> objects = Lists.newArrayList();
        Text t = this.textStyleService.oldLegacy(s[0]);
        TextParsingUtils.StyleTuple tuple = TextParsingUtils.getLastColourAndStyle(t, null);
        objects.add(t);
        int count = 1;
        for (Integer x : map) {
            objects.add(TextTemplate.arg(x.toString()).optional().color(tuple.colour).style(tuple.style).build());
            if (s.length > count) {
                t = Text.of(tuple.colour, tuple.style, this.textStyleService.oldLegacy(s[count]));
                tuple = TextParsingUtils.getLastColourAndStyle(t, null);
                objects.add(t);
            }

            count++;
        }

        return TextTemplate.of(objects.toArray(new Object[0]));
    }

}
