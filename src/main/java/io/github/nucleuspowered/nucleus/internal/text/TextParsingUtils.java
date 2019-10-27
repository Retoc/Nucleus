/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.interfaces.SimpleReloadable;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.services.INucleusServiceCollection;
import io.github.nucleuspowered.nucleus.util.Tuples;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextElement;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class TextParsingUtils {

    public static Text joinTextsWithColoursFlowing(Text... texts) {
        List<Text> result = Lists.newArrayList();
        Text last = null;
        for (Text n : texts) {
            if (last != null) {
                StyleTuple st = getLastColourAndStyle(last, null);
                result.add(Text.of(st.colour, st.style, n));
            } else {
                result.add(n);
            }

            last = n;
        }

        return Text.join(result);
    }

    public static StyleTuple getLastColourAndStyle(TextRepresentable text, @Nullable StyleTuple current) {
        return getLastColourAndStyle(text, current, TextColors.NONE, TextStyles.NONE);
    }

    public static StyleTuple getLastColourAndStyle(TextRepresentable text,
            @Nullable StyleTuple current,
            TextColor defaultColour,
            TextStyle defaultStyle) {
        List<Text> texts = flatten(text.toText());
        if (texts.isEmpty()) {
            return current == null ? new StyleTuple(defaultColour, defaultStyle) : current;
        }

        TextColor tc = TextColors.NONE;
        TextStyle ts =  texts.get(texts.size() - 1).getStyle();

        for (int i = texts.size() - 1; i > -1; i--) {
            // If we have both a Text Colour and a Text Style, then break out.
            tc = texts.get(i).getColor();
            if (tc != TextColors.NONE) {
                break;
            }
        }

        if (tc == TextColors.NONE) {
            tc = defaultColour;
        }

        if (current == null) {
            return new StyleTuple(tc, ts);
        }

        return new StyleTuple(tc != TextColors.NONE ? tc : current.colour, ts);
    }

    private static List<Text> flatten(Text text) {
        List<Text> texts = Lists.newArrayList(text);
        if (!text.getChildren().isEmpty()) {
            text.getChildren().forEach(x -> texts.addAll(flatten(x)));
        }

        return texts;
    }

    @NonnullByDefault
    public static final class StyleTuple {
        public final TextColor colour;
        public final TextStyle style;

        public StyleTuple(TextColor colour, TextStyle style) {
            this.colour = colour;
            this.style = style;
        }

        public void applyTo(Consumer<StyleTuple> consumer) {
            consumer.accept(this);
        }

        public Text getTextOf() {
            Text.Builder tb = Text.builder();
            if (this.colour != TextColors.NONE) {
                tb.color(this.colour);
            }

            tb.style(this.style);
            return tb.toText();
        }
    }
}
