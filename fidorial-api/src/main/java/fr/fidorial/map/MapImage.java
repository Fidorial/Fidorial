package fr.fidorial.map;

import java.awt.image.BufferedImage;

/**
 * A picture already converted to map pixels, ready to be {@linkplain MapCanvas#draw drawn}.
 *
 * <p>Converting is the slow part, so convert once and draw as often as needed. A picture larger
 * than a map can be cut into {@linkplain #tile(int, int) tiles}, one per map of a wall of item
 * frames. Images are immutable.</p>
 *
 * @see MapManager#image(BufferedImage, Dithering)
 * @since 0.1.0
 */
public interface MapImage {

    /**
     * {@return the width in pixels}
     *
     * @since 0.1.0
     */
    int width();

    /**
     * {@return the height in pixels}
     *
     * @since 0.1.0
     */
    int height();

    /**
     * Returns one pixel.
     *
     * @param x the column, {@code 0} to {@code width - 1}
     * @param y the row, {@code 0} to {@code height - 1}
     * @return the map pixel
     * @since 0.1.0
     */
    byte pixel(int x, int y);

    /**
     * {@return a copy of the pixels, row by row}
     *
     * @since 0.1.0
     */
    byte[] pixels();

    /**
     * {@return how many maps wide the image is, counting a partly filled last column}
     *
     * @since 0.1.0
     */
    int columns();

    /**
     * {@return how many maps high the image is, counting a partly filled last row}
     *
     * @since 0.1.0
     */
    int rows();

    /**
     * Cuts the map-sized piece at a column and row of maps.
     *
     * <p>Pieces on the right and bottom edges are padded with transparent pixels when the image is
     * not a multiple of {@value MapCanvas#SIZE} pixels.</p>
     *
     * @param column the column of maps, {@code 0} to {@code columns() - 1}, left to right
     * @param row    the row of maps, {@code 0} to {@code rows() - 1}, top to bottom
     * @return a {@value MapCanvas#SIZE}×{@value MapCanvas#SIZE} image
     * @since 0.1.0
     */
    MapImage tile(int column, int row);

    /**
     * Converts the image back to colors, for previews.
     *
     * @return a picture of the same size, with transparent pixels left transparent
     * @since 0.1.0
     */
    BufferedImage toBufferedImage();

    /**
     * How colors that maps cannot show are approximated.
     *
     * @since 0.1.0
     */
    enum Dithering {

        /**
         * Each pixel takes the closest map color. Sharp, but gradients turn into bands.
         */
        NONE,

        /**
         * The difference between each pixel and the color it got is spread onto its neighbours.
         * Gradients and photos look much better, flat pixel art slightly noisier.
         */
        ERROR_DIFFUSION
    }
}
