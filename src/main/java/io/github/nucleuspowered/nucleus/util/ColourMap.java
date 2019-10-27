/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Map;

public class ColourMap {

    private ColourMap() {}

    private final static Map<Character, TextColor> colourMap = Maps.newHashMap();
    private final static Map<Character, TextStyle> styleMap = Maps.newHashMap();
    private final static Map<String, TextStyle> styleMapFull = Maps.newHashMap();
    private final static Map<Character, String> styleKeyMap = Maps.newHashMap();

    private final static String OBFUSICATED = "OBFUSICATED";
    private final static String BOLD = "BOLD";
    private final static String STRIKETHROUGH = "STRIKETHROUGH";
    private final static String UNDERLINE = "UNDERLINE";
    private final static String ITALIC = "ITALIC";
    private final static String RESET = "RESET";

    static {
        colourMap.put('0', TextColors.BLACK);
        colourMap.put('1', TextColors.DARK_BLUE);
        colourMap.put('2', TextColors.DARK_GREEN);
        colourMap.put('3', TextColors.DARK_AQUA);
        colourMap.put('4', TextColors.DARK_RED);
        colourMap.put('5', TextColors.DARK_PURPLE);
        colourMap.put('6', TextColors.GOLD);
        colourMap.put('7', TextColors.GRAY);
        colourMap.put('8', TextColors.DARK_GRAY);
        colourMap.put('9', TextColors.BLUE);
        colourMap.put('a', TextColors.GREEN);
        colourMap.put('b', TextColors.AQUA);
        colourMap.put('c', TextColors.RED);
        colourMap.put('d', TextColors.LIGHT_PURPLE);
        colourMap.put('e', TextColors.YELLOW);
        colourMap.put('f', TextColors.WHITE);

        styleMapFull.put("OBFUSCATED", TextStyles.OBFUSCATED);
        styleMapFull.put("MAGIC", TextStyles.OBFUSCATED);
        styleMapFull.put("BOLD", TextStyles.BOLD);
        styleMapFull.put("STRIKETHROUGH", TextStyles.STRIKETHROUGH);
        styleMapFull.put("UNDERLINE", TextStyles.UNDERLINE);
        styleMapFull.put("ITALIC", TextStyles.ITALIC);
        styleMapFull.put("RESET", TextStyles.RESET);

        styleMap.put('k', TextStyles.OBFUSCATED);
        styleMap.put('l', TextStyles.BOLD);
        styleMap.put('m', TextStyles.STRIKETHROUGH);
        styleMap.put('n', TextStyles.UNDERLINE);
        styleMap.put('o', TextStyles.ITALIC);
        styleMap.put('r', TextStyles.RESET);

        styleKeyMap.put('k', OBFUSICATED);
        styleKeyMap.put('l', BOLD);
        styleKeyMap.put('m', STRIKETHROUGH);
        styleKeyMap.put('n', UNDERLINE);
        styleKeyMap.put('o', ITALIC);
        styleKeyMap.put('r', RESET);

    }

    public static ImmutableMap<Character, TextColor> getColours() {
        return ImmutableMap.copyOf(colourMap);
    }

    public static ImmutableMap<Character, TextStyle> getStyles() {
        return ImmutableMap.copyOf(styleMap);
    }

    public static ImmutableMap<Character, String> getStyleKeys() {
        return ImmutableMap.copyOf(styleKeyMap);
    }
}
