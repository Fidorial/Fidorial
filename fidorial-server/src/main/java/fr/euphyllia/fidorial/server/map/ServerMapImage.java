package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapImage;
import fr.fidorial.map.MapPalette;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * A picture converted to map pixels; immutable.
 */
final class ServerMapImage implements MapImage {

    private final int width;
    private final int height;
    private final byte[] pixels;

    private ServerMapImage(final int width, final int height, final byte[] pixels) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    /**
     * Wraps pixels that are already map pixels.
     *
     * @param width  the width, at least {@code 1}
     * @param height the height, at least {@code 1}
     * @param pixels {@code width * height} map pixels, row by row; copied
     * @return the image
     */
    static MapImage of(final int width, final int height, final byte[] pixels) {
        Preconditions.checkArgument(width >= 1 && height >= 1, "A map image must not be empty");
        Preconditions.checkArgument(pixels != null && pixels.length == width * height,
                "A map image needs exactly width * height pixels");
        return new ServerMapImage(width, height, pixels.clone());
    }

    /**
     * Converts colors to map pixels.
     *
     * <p>Pixels with an alpha below {@code 128} become {@linkplain MapPixels#TRANSPARENT transparent},
     * the others take the closest map color.</p>
     *
     * @param width     the width, at least {@code 1}
     * @param height    the height, at least {@code 1}
     * @param argb      {@code width * height} colors as {@code 0xAARRGGBB}, row by row
     * @param dithering how to approximate colors
     * @return the image
     */
    static MapImage fromArgb(final int width, final int height, final int[] argb, final Dithering dithering) {
        Preconditions.checkArgument(width >= 1 && height >= 1, "A map image must not be empty");
        Preconditions.checkArgument(argb != null && argb.length == width * height,
                "A map image needs exactly width * height colors");
        Preconditions.checkArgument(dithering != null, "The dithering of a map image must not be null");
        final byte[] pixels = new byte[width * height];
        if (dithering == Dithering.NONE) {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = argb[i] >>> 24 < 128 ? MapPalette.TRANSPARENT : ServerMapPalette.nearest(argb[i]);
            }
        } else {
            diffuse(width, height, argb, pixels);
        }
        return new ServerMapImage(width, height, pixels);
    }

    /**
     * Converts a picture to map pixels, at its own size.
     *
     * @param image     the picture
     * @param dithering how to approximate colors
     * @return the image
     */
    static MapImage from(final BufferedImage image, final Dithering dithering) {
        Preconditions.checkArgument(image != null, "The image to convert must not be null");
        final int width = image.getWidth();
        final int height = image.getHeight();
        return fromArgb(width, height, image.getRGB(0, 0, width, height, null, 0, width), dithering);
    }

    /**
     * Scales a picture, then converts it to map pixels.
     *
     * <p>To fill a wall of {@code n×m} maps, scale to {@code 128n×128m} and cut the result into
     * {@linkplain #tile(int, int) tiles}.</p>
     *
     * @param image     the picture
     * @param width     the width to scale to, at least {@code 1}
     * @param height    the height to scale to, at least {@code 1}
     * @param dithering how to approximate colors
     * @return the image
     */
    static MapImage from(final BufferedImage image, final int width, final int height, final Dithering dithering) {
        Preconditions.checkArgument(image != null, "The image to convert must not be null");
        Preconditions.checkArgument(width >= 1 && height >= 1, "A map image must not be empty");
        if (image.getWidth() == width && image.getHeight() == height) {
            return from(image, dithering);
        }
        final BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return from(scaled, dithering);
    }

    /**
     * Floyd–Steinberg: the error of each pixel goes 7/16 right, 3/16 down left, 5/16 down and 1/16
     * down right. Transparent pixels neither receive nor spread error.
     */
    private static void diffuse(final int width, final int height, final int[] argb, final byte[] pixels) {
        // error carried by the current and the next row, three channels per pixel, with a margin
        int[] current = new int[(width + 2) * 3];
        int[] next = new int[(width + 2) * 3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int color = argb[y * width + x];
                if (color >>> 24 < 128) {
                    pixels[y * width + x] = MapPalette.TRANSPARENT;
                    continue;
                }
                final int e = (x + 1) * 3;
                final int r = clamp((color >> 16 & 0xFF) + current[e] / 16);
                final int g = clamp((color >> 8 & 0xFF) + current[e + 1] / 16);
                final int b = clamp((color & 0xFF) + current[e + 2] / 16);
                final byte pixel = ServerMapPalette.search(r, g, b);
                pixels[y * width + x] = pixel;
                final int got = ServerMapPalette.rgbOf(pixel);
                spread(current, next, e, r - (got >> 16 & 0xFF), 0);
                spread(current, next, e, g - (got >> 8 & 0xFF), 1);
                spread(current, next, e, b - (got & 0xFF), 2);
            }
            final int[] swap = current;
            current = next;
            next = swap;
            Arrays.fill(next, 0);
        }
    }

    private static void spread(final int[] current, final int[] next, final int at, final int error, final int channel) {
        current[at + 3 + channel] += error * 7;
        next[at - 3 + channel] += error * 3;
        next[at + channel] += error * 5;
        next[at + 3 + channel] += error;
    }

    private static int clamp(final int value) {
        return value < 0 ? 0 : Math.min(value, 255);
    }

    /**
     * {@return the width in pixels}
     *
     */
    @Override
    public int width() {
        return width;
    }

    /**
     * {@return the height in pixels}
     *
     */
    @Override
    public int height() {
        return height;
    }

    /**
     * Returns one pixel.
     *
     * @param x the column, {@code 0} to {@code width - 1}
     * @param y the row, {@code 0} to {@code height - 1}
     * @return the map pixel
     */
    @Override
    public byte pixel(final int x, final int y) {
        Preconditions.checkElementIndex(x, width, "x");
        Preconditions.checkElementIndex(y, height, "y");
        return pixels[y * width + x];
    }

    /**
     * {@return a copy of the pixels, row by row}
     *
     */
    @Override
    public byte[] pixels() {
        return pixels.clone();
    }

    /**
     * {@return how many maps wide the image is, counting a partly filled last column}
     *
     */
    @Override
    public int columns() {
        return (width + MapCanvas.SIZE - 1) / MapCanvas.SIZE;
    }

    /**
     * {@return how many maps high the image is, counting a partly filled last row}
     *
     */
    @Override
    public int rows() {
        return (height + MapCanvas.SIZE - 1) / MapCanvas.SIZE;
    }

    /**
     * Cuts the map-sized piece at a column and row of maps.
     *
     * <p>Pieces on the right and bottom edges are padded with transparent pixels when the image is
     * not a multiple of {@value MapCanvas#SIZE} pixels.</p>
     *
     * @param column the column of maps, {@code 0} to {@code columns() - 1}, left to right
     * @param row    the row of maps, {@code 0} to {@code rows() - 1}, top to bottom
     * @return a {@value MapCanvas#SIZE}×{@value MapCanvas#SIZE} image
     */
    @Override
    public MapImage tile(final int column, final int row) {
        Preconditions.checkElementIndex(column, columns(), "column");
        Preconditions.checkElementIndex(row, rows(), "row");
        final int size = MapCanvas.SIZE;
        final byte[] tile = new byte[size * size];
        final int left = column * size;
        final int top = row * size;
        final int copy = Math.min(size, width - left);
        for (int y = 0; y < size && top + y < height; y++) {
            System.arraycopy(pixels, (top + y) * width + left, tile, y * size, copy);
        }
        return new ServerMapImage(size, size, tile);
    }

    /**
     * Converts the image back to colors, for previews.
     *
     * @return a picture of the same size, with transparent pixels left transparent
     */
    @Override
    public BufferedImage toBufferedImage() {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final int[] argb = new int[pixels.length];
        for (int i = 0; i < argb.length; i++) {
            argb[i] = ServerMapPalette.rgbOf(pixels[i]);
        }
        image.setRGB(0, 0, width, height, argb, 0, width);
        return image;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof final ServerMapImage image && width == image.width && height == image.height
                && Arrays.equals(pixels, image.pixels);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * width + height) + Arrays.hashCode(pixels);
    }

    @Override
    public String toString() {
        return "MapImage[" + width + "x" + height + "]";
    }
}
