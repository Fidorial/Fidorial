package fr.fidorial.map;

import net.kyori.adventure.text.ComponentLike;

import java.util.function.Consumer;

/**
 * A {@value #SIZE}×{@value #SIZE} grid of map pixels, see {@link MapPalette}.
 *
 * <p>Column {@code 0} is on the left, row {@code 0} at the top, as the map is seen in hand or in an
 * item frame. Drawing outside the grid is silently clipped, so shapes and text can overflow the
 * edges. Canvases are safe to draw on from any thread; group related changes in a
 * {@link #batch(Consumer)} so viewers never see half of them.</p>
 *
 * @see MapView#canvas()
 * @see MapManager#createCanvas()
 * @since 0.1.0
 */
public interface MapCanvas {

    /**
     * The width and height of a canvas.
     */
    int SIZE = 128;

    /**
     * Returns a pixel.
     *
     * @param x the column
     * @param y the row
     * @return the pixel, {@link MapPalette#TRANSPARENT} outside the canvas
     * @since 0.1.0
     */
    byte pixel(int x, int y);

    /**
     * Sets a pixel. Does nothing outside the canvas.
     *
     * @param x     the column
     * @param y     the row
     * @param pixel the pixel
     * @since 0.1.0
     */
    void pixel(int x, int y, byte pixel);

    /**
     * Runs several changes at once: other threads, and viewers, see either none or all of them.
     *
     * @param edits the changes, made on this canvas
     * @since 0.1.0
     */
    void batch(Consumer<? super MapCanvas> edits);

    /**
     * {@return a copy of every pixel, row by row}
     *
     * @since 0.1.0
     */
    byte[] pixels();

    /**
     * Replaces every pixel.
     *
     * @param pixels {@value #SIZE}×{@value #SIZE} pixels, row by row
     * @since 0.1.0
     */
    void pixels(byte[] pixels);

    /**
     * Fills the whole canvas.
     *
     * @param pixel the pixel, {@link MapPalette#TRANSPARENT} to clear
     * @since 0.1.0
     */
    void fill(byte pixel);

    /**
     * Fills a rectangle.
     *
     * @param x      the left column
     * @param y      the top row
     * @param width  the width
     * @param height the height
     * @param pixel  the pixel
     * @since 0.1.0
     */
    void fill(int x, int y, int width, int height, byte pixel);

    /**
     * Draws the outline of a rectangle, one pixel thick.
     *
     * @param x      the left column
     * @param y      the top row
     * @param width  the width
     * @param height the height
     * @param pixel  the pixel
     * @since 0.1.0
     */
    void rectangle(int x, int y, int width, int height, byte pixel);

    /**
     * Draws a straight line, both ends included.
     *
     * @param x1    the column of the start
     * @param y1    the row of the start
     * @param x2    the column of the end
     * @param y2    the row of the end
     * @param pixel the pixel
     * @since 0.1.0
     */
    void line(int x1, int y1, int x2, int y2, byte pixel);

    /**
     * Draws an image. Transparent pixels of the image leave the canvas unchanged.
     *
     * @param image the image
     * @param x     the column of the left edge of the image
     * @param y     the row of its top edge
     * @since 0.1.0
     */
    void draw(MapImage image, int x, int y);

    /**
     * Copies every pixel of another canvas, transparent ones included.
     *
     * @param source the canvas to copy
     * @since 0.1.0
     */
    void copy(MapCanvas source);

    /**
     * Writes text in one color, with the {@linkplain MapManager#standardFont() standard font}.
     *
     * @param x     the column where lines start
     * @param y     the row of the top of the first line
     * @param text  the text; {@code \n} starts a new line
     * @param pixel the color of the text
     * @return the width of the widest line, in pixels
     * @since 0.1.0
     */
    int text(int x, int y, String text, byte pixel);

    /**
     * Writes text.
     *
     * <p>Each part of the text takes the map color closest to its color; parts without a color take
     * {@code pixel}. Bold parts are drawn one pixel wider. Other decorations are ignored.
     * Translatable parts show their fallback, or their key.</p>
     *
     * @param x      the column where lines start
     * @param y      the row of the top of the first line
     * @param text   the text; {@code \n} starts a new line
     * @param font   the font
     * @param pixel  the color of uncolored parts
     * @param shadow whether to draw a darker copy one pixel down and right first, like chat does
     * @return the width of the widest line, in pixels
     * @since 0.1.0
     */
    int text(int x, int y, ComponentLike text, MapFont font, byte pixel, boolean shadow);
}
