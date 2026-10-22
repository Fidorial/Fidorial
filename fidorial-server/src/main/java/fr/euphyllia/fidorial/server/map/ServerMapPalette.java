package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.fidorial.map.MapColor;
import fr.fidorial.map.MapPalette;
import fr.fidorial.map.MapShade;

/**
 * The colors of map pixels, and how to find the closest one to any color.
 */
public final class ServerMapPalette implements MapPalette {

    static final ServerMapPalette INSTANCE = new ServerMapPalette();

    /**
     * The first pixel that is not transparent.
     */
    static final int FIRST_OPAQUE = 4;

    /**
     * One past the last pixel the client can draw.
     */
    static final int END = MapColor.values().length * 4;

    private static final int[] RGB = new int[256];

    /**
     * Closest pixel of every color quantized to 6 bits per channel, {@code 0} while not computed.
     */
    private static final byte[] CLOSEST = new byte[1 << 18];

    static {
        for (int pixel = FIRST_OPAQUE; pixel < END; pixel++) {
            RGB[pixel] = shade(MapColor.values()[pixel >> 2].rgb(), MapShade.values()[pixel & 3]);
        }
    }

    private ServerMapPalette() {
    }

    /**
     * {@return a color drawn in a shade, as {@code 0xRRGGBB}}
     */
    static int shade(final int rgb, final MapShade shade) {
        final int multiplier = shade.multiplier();
        final int r = ((rgb >> 16) & 0xFF) * multiplier / 255;
        final int g = ((rgb >> 8) & 0xFF) * multiplier / 255;
        final int b = (rgb & 0xFF) * multiplier / 255;
        return r << 16 | g << 8 | b;
    }

    /**
     * Packs a color and a shade into a pixel.
     *
     * @param color the color
     * @param shade the shade
     * @return the pixel, {@link #TRANSPARENT} when the color is {@link MapColor#NONE}
     */
    @Override
    public byte pixel(final MapColor color, final MapShade shade) {
        return of(color, shade);
    }

    @Override
    public byte pixel(final MapColor color) {
        return of(color, MapShade.NORMAL);
    }

    static byte of(final MapColor color, final MapShade shade) {
        Preconditions.checkArgument(color != null, "The color of a map pixel must not be null");
        Preconditions.checkArgument(shade != null, "The shade of a map pixel must not be null");
        return color == MapColor.NONE ? MapPalette.TRANSPARENT : (byte) (color.id() << 2 | shade.ordinal());
    }

    /**
     * {@return whether a pixel is drawn transparent}
     *
     * @param pixel the pixel
     */
    @Override
    public boolean isTransparent(final byte pixel) {
        return transparent(pixel);
    }

    static boolean transparent(final byte pixel) {
        final int unsigned = pixel & 0xFF;
        return unsigned < FIRST_OPAQUE || unsigned >= END;
    }

    /**
     * {@return the base color of a pixel, {@link MapColor#NONE} when it is transparent}
     *
     * @param pixel the pixel
     */
    @Override
    public MapColor color(final byte pixel) {
        if (transparent(pixel)) {
            return MapColor.NONE;
        }
        return MapColor.values()[(pixel & 0xFF) >> 2];
    }

    /**
     * {@return the shade of a pixel}
     *
     * @param pixel the pixel
     */
    @Override
    public MapShade shade(final byte pixel) {
        return MapShade.values()[pixel & 3];
    }

    /**
     * Returns the color the client draws a pixel with.
     *
     * @param pixel the pixel
     * @return the color as {@code 0xAARRGGBB}: opaque, or {@code 0} when the pixel is transparent
     */
    @Override
    public int argb(final byte pixel) {
        return rgbOf(pixel);
    }

    static int rgbOf(final byte pixel) {
        return transparent(pixel) ? 0 : 0xFF000000 | RGB[pixel & 0xFF];
    }

    /**
     * Finds the pixel whose color is the closest to a color.
     *
     * <p>Distances are measured with a weighting that follows how the eye perceives red, green and
     * blue, on colors rounded to 6 bits per channel. The result is never transparent.</p>
     *
     * @param rgb a color as {@code 0xRRGGBB}; an alpha channel is ignored
     * @return the closest opaque pixel
     */
    @Override
    public byte closest(final int rgb) {
        return nearest(rgb);
    }

    static byte nearest(final int rgb) {
        final int key = (rgb >> 6 & 0x3F000) | (rgb >> 4 & 0xFC0) | (rgb >> 2 & 0x3F);
        final byte cached = CLOSEST[key];
        if (cached != 0) {
            return cached;
        }
        final int r = (key >> 12) * 255 / 63;
        final int g = (key >> 6 & 0x3F) * 255 / 63;
        final int b = (key & 0x3F) * 255 / 63;
        final byte found = search(r, g, b);
        CLOSEST[key] = found;
        return found;
    }

    /**
     * Finds the closest pixel to an exact color, without rounding it first.
     *
     * @param r red, {@code 0}-{@code 255}, may overflow when dithering
     * @param g green
     * @param b blue
     * @return the closest opaque pixel
     */
    static byte search(final int r, final int g, final int b) {
        int best = FIRST_OPAQUE;
        long bestDistance = Long.MAX_VALUE;
        for (int pixel = FIRST_OPAQUE; pixel < END; pixel++) {
            final long distance = distance(r, g, b, RGB[pixel]);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = pixel;
            }
        }
        return (byte) best;
    }

    /**
     * "Red mean" distance: cheap, and much closer to perception than a plain euclidean one.
     */
    private static long distance(final int r, final int g, final int b, final int rgb) {
        final int r2 = rgb >> 16 & 0xFF;
        final int g2 = rgb >> 8 & 0xFF;
        final int b2 = rgb & 0xFF;
        final long mean = (r + r2) / 2;
        final long dr = r - r2;
        final long dg = g - g2;
        final long db = b - b2;
        return ((512 + mean) * dr * dr >> 8) + 4 * dg * dg + ((767 - mean) * db * db >> 8);
    }
}
