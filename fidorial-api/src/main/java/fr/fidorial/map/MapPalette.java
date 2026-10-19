package fr.fidorial.map;

/**
 * Map pixels and their colors.
 *
 * <p>A map pixel is one byte: a {@link MapColor} id times four, plus a {@link MapShade}. Pixels
 * {@code 0}-{@code 3} are transparent; the client draws the 244 pixels {@code 4}-{@code 247} each in a
 * different color.</p>
 *
 * @see MapManager#palette()
 * @since 0.1.0
 */
public interface MapPalette {

    /**
     * The transparent pixel. A canvas starts filled with it.
     */
    byte TRANSPARENT = 0;

    /**
     * Packs a color and a shade into a pixel.
     *
     * @param color the color
     * @param shade the shade
     * @return the pixel, {@link #TRANSPARENT} when the color is {@link MapColor#NONE}
     * @since 0.1.0
     */
    byte pixel(MapColor color, MapShade shade);

    /**
     * Packs a color in its {@linkplain MapShade#NORMAL normal} shade into a pixel.
     *
     * @param color the color
     * @return the pixel, {@link #TRANSPARENT} when the color is {@link MapColor#NONE}
     * @since 0.1.0
     */
    byte pixel(MapColor color);

    /**
     * {@return the base color of a pixel, {@link MapColor#NONE} when it is transparent}
     *
     * @param pixel the pixel
     * @since 0.1.0
     */
    MapColor color(byte pixel);

    /**
     * {@return the shade of a pixel}
     *
     * @param pixel the pixel
     * @since 0.1.0
     */
    MapShade shade(byte pixel);

    /**
     * {@return whether the client draws a pixel transparent}
     *
     * @param pixel the pixel
     * @since 0.1.0
     */
    boolean isTransparent(byte pixel);

    /**
     * Returns the color the client draws a pixel with.
     *
     * @param pixel the pixel
     * @return the color as {@code 0xAARRGGBB}: opaque, or {@code 0} when the pixel is transparent
     * @since 0.1.0
     */
    int argb(byte pixel);

    /**
     * Finds the pixel whose color is the closest to a color, as the eye perceives it.
     *
     * @param rgb a color as {@code 0xRRGGBB}; an alpha channel is ignored
     * @return the closest opaque pixel
     * @since 0.1.0
     */
    byte closest(int rgb);
}
