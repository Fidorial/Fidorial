package fr.euphyllia.fidorial.server.network.serialization.util.nbt;

import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.Nullable;

import java.util.function.IntFunction;
import java.util.regex.Pattern;

public final class NbtTextDecorator {

    private static final int NESTING_LIMIT = 64;
    private static final int ARRAY_PRINT_LIMIT = 128;
    private static final Pattern SIMPLE_KEY = Pattern.compile("[A-Za-z0-9._+-]+");

    private final Palette palette;
    private int nesting = 0;

    private NbtTextDecorator(final Palette palette) {
        this.palette = palette;
    }

    public static Component render(final BinaryTag tag, final boolean plain) {
        final NbtTextDecorator renderer = new NbtTextDecorator(plain ? Palette.NONE : Palette.COLORED);
        return renderer.write(tag);
    }

    private Component write(final BinaryTag tag) {
        return switch (tag) {
            case final StringBinaryTag t -> quoted(t.value(), palette.stringColor());
            case final ByteBinaryTag t -> number(String.valueOf(t.value())).append(typeMarker("b"));
            case final ShortBinaryTag t -> number(String.valueOf(t.value())).append(typeMarker("s"));
            case final IntBinaryTag t -> number(String.valueOf(t.value()));
            case final LongBinaryTag t -> number(String.valueOf(t.value())).append(typeMarker("L"));
            case final FloatBinaryTag t -> number(String.valueOf(t.value())).append(typeMarker("f"));
            case final DoubleBinaryTag t -> number(String.valueOf(t.value())).append(typeMarker("d"));
            case final ByteArrayBinaryTag t -> numericArray("B", t.value().length, i -> String.valueOf(t.value()[i]), "b");
            case final IntArrayBinaryTag t -> numericArray("I", t.value().length, i -> String.valueOf(t.value()[i]), null);
            case final LongArrayBinaryTag t -> numericArray("L", t.value().length, i -> String.valueOf(t.value()[i]), "L");
            case final ListBinaryTag t -> list(t);
            case final CompoundBinaryTag t -> compound(t);
            default -> plain(String.valueOf(tag));
        };
    }

    private Component list(final ListBinaryTag list) {
        if (list.isEmpty()) {
            return punctuation("[").append(punctuation("]"));
        }
        if (nesting >= NESTING_LIMIT) {
            return punctuation("[").append(folded()).append(punctuation("]"));
        }

        nesting++;
        Component joined = null;
        for (final BinaryTag element : list) {
            final Component rendered = write(element);
            joined = joined == null ? rendered : joined.append(punctuation(",")).append(plain(" ")).append(rendered);
        }
        nesting--;

        return punctuation("[").append(joined).append(punctuation("]"));
    }

    private Component compound(final CompoundBinaryTag compound) {
        if (compound.keySet().isEmpty()) {
            return punctuation("{").append(punctuation("}"));
        }
        if (nesting >= NESTING_LIMIT) {
            return punctuation("{").append(folded()).append(punctuation("}"));
        }

        nesting++;
        Component joined = null;
        for (final String key : compound.keySet()) {
            final Component entry = fieldName(key).append(punctuation(":")).append(plain(" ")).append(write(compound.get(key)));
            joined = joined == null ? entry : joined.append(punctuation(",")).append(plain(" ")).append(entry);
        }
        nesting--;

        return punctuation("{").append(joined).append(punctuation("}"));
    }

    private Component numericArray(final String typeMarker, final int length, final IntFunction<String> elementText, final @Nullable String elementSuffix) {
        final int limit = Math.min(length, ARRAY_PRINT_LIMIT);
        Component body = typeMarker(typeMarker).append(punctuation(";"));
        for (int i = 0; i < limit; i++) {
            body = body.append(plain(" ")).append(number(elementText.apply(i)));
            if (elementSuffix != null) {
                body = body.append(typeMarker(elementSuffix));
            }
            if (i != length - 1) {
                body = body.append(punctuation(","));
            }
        }
        if (length > ARRAY_PRINT_LIMIT) {
            body = body.append(plain(" ")).append(folded());
        }
        return punctuation("[").append(body).append(punctuation("]"));
    }

    private Component fieldName(final String key) {
        if (SIMPLE_KEY.matcher(key).matches()) {
            return colorize(key, palette.keyColor());
        }
        return quoted(key, palette.keyColor());
    }

    private Component quoted(final String raw, final @Nullable TextColor color) {
        final StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);
            if (c == '"' || c == '\\') {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return punctuation("\"").append(colorize(escaped.toString(), color)).append(plain("\""));
    }

    private Component number(final String text) {
        return colorize(text, palette.numberColor());
    }

    private Component typeMarker(final String text) {
        return colorize(text, palette.typeMarkerColor());
    }

    private Component punctuation(final String text) {
        return colorize(text, palette.punctuationColor());
    }

    private Component folded() {
        return colorize("...", palette.foldedColor());
    }

    private Component plain(final String text) {
        return Component.text(text);
    }

    private Component colorize(final String text, final @Nullable TextColor color) {
        final Component base = Component.text(text);
        return color == null ? base : base.color(color);
    }

    private record Palette(@Nullable TextColor keyColor, @Nullable TextColor stringColor, @Nullable TextColor numberColor, @Nullable TextColor typeMarkerColor, @Nullable TextColor punctuationColor, @Nullable TextColor foldedColor) {
        static final Palette NONE = new Palette(null, null, null, null, null, null);
        static final Palette COLORED = new Palette(NamedTextColor.AQUA, NamedTextColor.GREEN, NamedTextColor.GOLD, NamedTextColor.RED, NamedTextColor.WHITE, NamedTextColor.GRAY);
    }
}
