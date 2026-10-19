package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.fidorial.map.MapCanvas;

/**
 * A rectangle of map pixels to paint on a client's copy of a map.
 *
 * @param x      the left column, {@code 0}-{@code 127}
 * @param y      the top row, {@code 0}-{@code 127}
 * @param width  the width in pixels, at least {@code 1}
 * @param height the height in pixels, at least {@code 1}
 * @param colors the pixels, row by row, {@code width * height} of them
 */
public record MapPatch(int x, int y, int width, int height, byte[] colors) {

    public MapPatch {
        Preconditions.checkArgument(x >= 0 && y >= 0, "A map patch must start inside the canvas");
        Preconditions.checkArgument(width >= 1 && height >= 1, "A map patch must not be empty");
        Preconditions.checkArgument(x + width <= MapCanvas.SIZE && y + height <= MapCanvas.SIZE,
                "A map patch must fit in the canvas");
        Preconditions.checkArgument(colors != null && colors.length == width * height,
                "A map patch needs exactly width * height colors");
    }

    /**
     * {@return a patch covering the whole canvas}
     *
     * @param colors {@code 128 * 128} pixels, row by row
     */
    static MapPatch full(final byte[] colors) {
        return new MapPatch(0, 0, MapCanvas.SIZE, MapCanvas.SIZE, colors);
    }
}
