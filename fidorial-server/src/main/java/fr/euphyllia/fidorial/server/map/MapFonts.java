package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.fidorial.map.MapFont;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Glyph lookups and the fonts plugins build.
 */
final class MapFonts {

    /**
     * A hollow box, drawn for characters a font does not have.
     */
    static final MapFont.Glyph BOX = new MapFont.Glyph(4, 0, new int[]{15, 9, 9, 9, 9, 9, 15});

    private MapFonts() {
    }

    /**
     * {@return the glyph drawn for a character: its own, or the fallback of the font}
     */
    static MapFont.Glyph glyph(final MapFont font, final int codePoint) {
        final MapFont.Glyph glyph = font.glyph(codePoint);
        return glyph != null ? glyph : font.fallback();
    }

    /**
     * {@return whether a pixel of a glyph is drawn}
     */
    static boolean isSet(final MapFont.Glyph glyph, final int column, final int row) {
        final int[] rows = glyph.rows();
        return row >= 0 && row < rows.length && column >= 0 && column < glyph.width() && (rows[row] >> column & 1) != 0;
    }

    /**
     * Measures one line of text.
     */
    static int width(final MapFont font, final String text) {
        Preconditions.checkArgument(text != null, "The text to measure must not be null");
        int width = 0;
        int count = 0;
        for (int i = 0; i < text.length(); ) {
            final int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            width += glyph(font, codePoint).width();
            count++;
        }
        return count == 0 ? 0 : width + (count - 1) * font.letterSpacing();
    }

    /**
     * Draws a glyph from text, one string per row, {@code #} for drawn pixels.
     */
    static MapFont.Glyph parse(final int top, final String... rows) {
        Preconditions.checkArgument(rows != null && rows.length > 0, "A glyph needs at least one row");
        int width = 1;
        final int[] bits = new int[rows.length];
        for (int y = 0; y < rows.length; y++) {
            width = Math.max(width, rows[y].length());
            for (int x = 0; x < rows[y].length(); x++) {
                if (rows[y].charAt(x) == '#') {
                    bits[y] |= 1 << x;
                }
            }
        }
        return new MapFont.Glyph(width, top, bits);
    }

    /**
     * Collects the glyphs of a font, see {@link fr.fidorial.map.MapManager#fontBuilder(int, int)}.
     */
    static final class Builder implements MapFont.Builder {

        private final int lineHeight;
        private final int ascent;
        private final Map<Integer, MapFont.Glyph> glyphs = new HashMap<>();
        private int letterSpacing = 1;
        private MapFont.Glyph fallback = BOX;

        Builder(final int lineHeight, final int ascent) {
            Preconditions.checkArgument(lineHeight >= 1, "The line height of a font must be at least 1");
            Preconditions.checkArgument(ascent >= 0, "The ascent of a font must not be negative");
            this.lineHeight = lineHeight;
            this.ascent = ascent;
        }

        @Override
        public MapFont.Builder glyph(final int codePoint, final MapFont.Glyph glyph) {
            Preconditions.checkArgument(glyph != null, "A glyph must not be null");
            glyphs.put(codePoint, glyph);
            return this;
        }

        @Override
        public MapFont.Builder glyph(final int codePoint, final int top, final String... rows) {
            return glyph(codePoint, parse(top, rows));
        }

        @Override
        public MapFont.Builder fallbackTo(final MapFont font) {
            Preconditions.checkArgument(font != null, "The font to copy must not be null");
            for (int codePoint = 0; codePoint <= 0xFFFF; codePoint++) {
                final MapFont.Glyph glyph = font.glyph(codePoint);
                if (glyph != null) {
                    glyphs.putIfAbsent(codePoint, glyph);
                }
            }
            return this;
        }

        @Override
        public MapFont.Builder letterSpacing(final int letterSpacing) {
            Preconditions.checkArgument(letterSpacing >= 0, "The letter spacing of a font must not be negative");
            this.letterSpacing = letterSpacing;
            return this;
        }

        @Override
        public MapFont.Builder fallback(final MapFont.Glyph fallback) {
            Preconditions.checkArgument(fallback != null, "The fallback glyph must not be null");
            this.fallback = fallback;
            return this;
        }

        @Override
        public MapFont build() {
            return new Bitmap(lineHeight, ascent, letterSpacing, fallback, Map.copyOf(glyphs));
        }
    }

    /**
     * A font made with a {@link Builder}.
     */
    private record Bitmap(int lineHeight, int ascent, int letterSpacing, MapFont.Glyph fallback,
                          Map<Integer, MapFont.Glyph> glyphs) implements MapFont {

        @Override
        public MapFont.@Nullable Glyph glyph(final int codePoint) {
            return glyphs.get(codePoint);
        }

        @Override
        public int width(final String text) {
            return MapFonts.width(this, text);
        }
    }
}
