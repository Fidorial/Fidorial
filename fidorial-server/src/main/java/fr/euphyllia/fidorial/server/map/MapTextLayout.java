package fr.euphyllia.fidorial.server.map;

import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapFont;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Lays text out glyph by glyph while a component is flattened.
 */
final class MapTextLayout implements FlattenerListener {

    private MapCanvas canvas;
    private final int left;
    private final MapFont font;
    private final Deque<Byte> colors = new ArrayDeque<>();
    private final Deque<Boolean> bolds = new ArrayDeque<>();
    private final boolean shadow;
    private int x;
    private int y;
    private int widest;
    private boolean lineStarted;

    MapTextLayout(final MapCanvas canvas, final int x, final int y, final MapFont font, final byte pixel,
                  final boolean shadow) {
        this.canvas = canvas;
        this.left = x;
        this.x = x;
        this.y = y;
        this.font = font;
        this.shadow = shadow;
        colors.push(pixel);
        bolds.push(false);
    }

    /**
     * Sets the canvas the glyphs go to, which is the batch view of the canvas being written on.
     */
    void target(final MapCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void pushStyle(final Style style) {
        final TextColor color = style.color();
        colors.push(color == null ? colors.peek() : ServerMapPalette.nearest(color.value()));
        final TextDecoration.State bold = style.decoration(TextDecoration.BOLD);
        bolds.push(bold == TextDecoration.State.NOT_SET ? bolds.peek() : bold == TextDecoration.State.TRUE);
    }

    @Override
    public void popStyle(final Style style) {
        if (colors.size() > 1) {
            colors.pop();
            bolds.pop();
        }
    }

    @Override
    public void component(final String text) {
        final byte pixel = colors.peek();
        final boolean bold = bolds.peek();
        final byte shade = ServerMapPalette.nearest(ServerMapPalette.rgbOf(pixel) >> 2 & 0x3F3F3F);
        for (int i = 0; i < text.length(); ) {
            final int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (codePoint == '\n') {
                x = left;
                y += font.lineHeight();
                lineStarted = false;
                continue;
            }
            final MapFont.Glyph glyph = MapFonts.glyph(font, codePoint);
            if (lineStarted) {
                x += font.letterSpacing();
            }
            if (shadow) {
                paint(glyph, x + 1, y + 1, shade, bold);
            }
            paint(glyph, x, y, pixel, bold);
            x += glyph.width() + (bold ? 1 : 0);
            lineStarted = true;
            widest = Math.max(widest, x - left);
        }
    }

    private void paint(final MapFont.Glyph glyph, final int x, final int y, final byte pixel, final boolean bold) {
        final int top = y + font.ascent() + glyph.top();
        for (int row = 0; row < glyph.rows().length; row++) {
            for (int column = 0; column < glyph.width(); column++) {
                if (MapFonts.isSet(glyph, column, row)) {
                    canvas.pixel(x + column, top + row, pixel);
                    if (bold) {
                        canvas.pixel(x + column + 1, top + row, pixel);
                    }
                }
            }
        }
    }

    int widest() {
        return widest;
    }
}
