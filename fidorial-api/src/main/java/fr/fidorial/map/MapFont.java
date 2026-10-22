package fr.fidorial.map;

import org.jspecify.annotations.Nullable;

/**
 * A bitmap font to {@linkplain MapCanvas#text write} on maps with.
 *
 * <p>Each character is a {@link Glyph}: a small grid of set and unset pixels. Text is laid out on
 * lines {@link #lineHeight()} pixels apart; on each line, glyph row {@code 0} sits {@link #ascent()}
 * pixels below the top of the line, which leaves room for the accents of capitals.</p>
 *
 * @see MapManager#standardFont()
 * @see MapManager#fontBuilder(int, int)
 * @since 0.1.0
 */
public interface MapFont {

    /**
     * Returns the glyph of a character.
     *
     * @param codePoint the character
     * @return its glyph, or {@code null} if the font does not have it
     * @since 0.1.0
     */
    @Nullable Glyph glyph(int codePoint);

    /**
     * {@return the glyph drawn for characters the font does not have}
     *
     * @since 0.1.0
     */
    Glyph fallback();

    /**
     * {@return how many pixels apart two lines are}
     *
     * @since 0.1.0
     */
    int lineHeight();

    /**
     * {@return how many pixels below the top of a line glyph row {@code 0} is}
     *
     * @since 0.1.0
     */
    int ascent();

    /**
     * {@return how many empty columns separate two glyphs}
     *
     * @since 0.1.0
     */
    int letterSpacing();

    /**
     * Measures one line of text.
     *
     * @param text the text, without line breaks
     * @return its width in pixels
     * @since 0.1.0
     */
    int width(String text);

    /**
     * The pixels of one character.
     *
     * @param width the width in pixels, {@code 1}-{@code 32}
     * @param top   the line row the first row of the glyph goes on, relative to glyph row {@code 0} of
     *              the line; negative for accents above capitals
     * @param rows  one entry per row, top to bottom; bit {@code n} set means column {@code n} is drawn
     * @since 0.1.0
     */
    record Glyph(int width, int top, int[] rows) {
    }

    /**
     * Collects the glyphs of a font.
     *
     * @see MapManager#fontBuilder(int, int)
     * @since 0.1.0
     */
    interface Builder {

        /**
         * Adds or replaces a glyph.
         *
         * @param codePoint the character
         * @param glyph     its pixels
         * @return this builder
         * @since 0.1.0
         */
        Builder glyph(int codePoint, Glyph glyph);

        /**
         * Adds or replaces a glyph drawn as text, one string per row, {@code #} for drawn pixels.
         *
         * @param codePoint the character
         * @param top       the line row of the first row, see {@link Glyph}
         * @param rows      the rows; the longest gives the width
         * @return this builder
         * @since 0.1.0
         */
        Builder glyph(int codePoint, int top, String... rows);

        /**
         * Copies every glyph of another font that this builder does not have yet, for the
         * characters {@code 0}-{@code 0xFFFF}.
         *
         * @param font the font
         * @return this builder
         * @since 0.1.0
         */
        Builder fallbackTo(MapFont font);

        /**
         * Sets how many empty columns separate two glyphs, {@code 1} by default.
         *
         * @param letterSpacing the spacing, at least {@code 0}
         * @return this builder
         * @since 0.1.0
         */
        Builder letterSpacing(int letterSpacing);

        /**
         * Sets the glyph drawn for missing characters, a hollow box by default.
         *
         * @param fallback the glyph
         * @return this builder
         * @since 0.1.0
         */
        Builder fallback(Glyph fallback);

        /**
         * {@return the font}
         *
         * @since 0.1.0
         */
        MapFont build();
    }
}
