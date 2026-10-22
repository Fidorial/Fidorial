package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapManager;
import fr.fidorial.map.MapPalette;

import java.util.function.Consumer;

/**
 * A canvas that belongs to no map, see {@link MapManager#createCanvas()}.
 */
public final class ArrayMapCanvas extends AbstractMapCanvas {

    private final byte[] pixels = new byte[SIZE * SIZE];

    @Override
    public synchronized byte pixel(final int x, final int y) {
        return x >= 0 && y >= 0 && x < SIZE && y < SIZE ? pixels[y * SIZE + x] : MapPalette.TRANSPARENT;
    }

    @Override
    public synchronized void pixel(final int x, final int y, final byte pixel) {
        if (x >= 0 && y >= 0 && x < SIZE && y < SIZE) {
            pixels[y * SIZE + x] = pixel;
        }
    }

    @Override
    public synchronized void batch(final Consumer<? super MapCanvas> edits) {
        Preconditions.checkArgument(edits != null, "The edits of a map canvas batch must not be null");
        edits.accept(this);
    }

    @Override
    public synchronized byte[] pixels() {
        return pixels.clone();
    }

    @Override
    public synchronized void pixels(final byte[] pixels) {
        Preconditions.checkArgument(pixels != null && pixels.length == SIZE * SIZE,
                "A map canvas holds exactly 128 * 128 pixels");
        System.arraycopy(pixels, 0, this.pixels, 0, this.pixels.length);
    }

    @Override
    public String toString() {
        return "MapCanvas[standalone]";
    }
}
