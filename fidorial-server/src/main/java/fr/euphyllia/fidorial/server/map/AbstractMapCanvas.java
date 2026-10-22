package fr.euphyllia.fidorial.server.map;

import com.google.common.base.Preconditions;
import fr.fidorial.map.MapCanvas;
import fr.fidorial.map.MapFont;
import fr.fidorial.map.MapImage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.flattener.ComponentFlattener;

import java.util.function.Consumer;

/**
 * The drawing operations of a canvas, built on reading and writing one pixel.
 */
public abstract class AbstractMapCanvas implements MapCanvas {

    @Override
    public void batch(final Consumer<? super MapCanvas> edits) {
        Preconditions.checkArgument(edits != null, "The edits of a map canvas batch must not be null");
        edits.accept(this);
    }

    @Override
    public byte[] pixels() {
        final byte[] pixels = new byte[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                pixels[y * SIZE + x] = pixel(x, y);
            }
        }
        return pixels;
    }

    @Override
    public void pixels(final byte[] pixels) {
        Preconditions.checkArgument(pixels != null && pixels.length == SIZE * SIZE,
                "A map canvas holds exactly 128 * 128 pixels");
        batch(canvas -> {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    canvas.pixel(x, y, pixels[y * SIZE + x]);
                }
            }
        });
    }

    @Override
    public void fill(final byte pixel) {
        fill(0, 0, SIZE, SIZE, pixel);
    }

    @Override
    public void fill(final int x, final int y, final int width, final int height, final byte pixel) {
        final int left = Math.max(0, x);
        final int top = Math.max(0, y);
        final int right = Math.min(SIZE, x + width);
        final int bottom = Math.min(SIZE, y + height);
        batch(canvas -> {
            for (int row = top; row < bottom; row++) {
                for (int column = left; column < right; column++) {
                    canvas.pixel(column, row, pixel);
                }
            }
        });
    }

    @Override
    public void rectangle(final int x, final int y, final int width, final int height, final byte pixel) {
        if (width < 1 || height < 1) {
            return;
        }
        batch(canvas -> {
            canvas.fill(x, y, width, 1, pixel);
            canvas.fill(x, y + height - 1, width, 1, pixel);
            canvas.fill(x, y, 1, height, pixel);
            canvas.fill(x + width - 1, y, 1, height, pixel);
        });
    }

    @Override
    public void line(final int x1, final int y1, final int x2, final int y2, final byte pixel) {
        batch(canvas -> {
            final int dx = Math.abs(x2 - x1);
            final int dy = -Math.abs(y2 - y1);
            final int sx = x1 < x2 ? 1 : -1;
            final int sy = y1 < y2 ? 1 : -1;
            int error = dx + dy;
            int x = x1;
            int y = y1;
            // a line longer than the canvas diagonal only clips; cap the steps for absurd inputs
            for (int steps = 0; steps <= 4 * SIZE + dx - dy; steps++) {
                canvas.pixel(x, y, pixel);
                if (x == x2 && y == y2) {
                    return;
                }
                final int doubled = 2 * error;
                if (doubled >= dy) {
                    error += dy;
                    x += sx;
                }
                if (doubled <= dx) {
                    error += dx;
                    y += sy;
                }
            }
        });
    }

    @Override
    public void draw(final MapImage image, final int x, final int y) {
        Preconditions.checkArgument(image != null, "The image to draw must not be null");
        final byte[] pixels = image.pixels();
        final int width = image.width();
        final int left = Math.max(0, -x);
        final int top = Math.max(0, -y);
        final int right = Math.min(width, SIZE - x);
        final int bottom = Math.min(image.height(), SIZE - y);
        batch(canvas -> {
            for (int row = top; row < bottom; row++) {
                for (int column = left; column < right; column++) {
                    final byte pixel = pixels[row * width + column];
                    if (!ServerMapPalette.transparent(pixel)) {
                        canvas.pixel(x + column, y + row, pixel);
                    }
                }
            }
        });
    }

    @Override
    public void copy(final MapCanvas source) {
        Preconditions.checkArgument(source != null, "The canvas to copy must not be null");
        pixels(source.pixels());
    }

    @Override
    public int text(final int x, final int y, final String text, final byte pixel) {
        return text(x, y, Component.text(text), StandardMapFont.INSTANCE, pixel, false);
    }

    @Override
    public int text(final int x, final int y, final ComponentLike text, final MapFont font, final byte pixel,
                    final boolean shadow) {
        Preconditions.checkArgument(text != null, "The text to write must not be null");
        Preconditions.checkArgument(font != null, "The font to write with must not be null");
        final MapTextLayout layout = new MapTextLayout(this, x, y, font, pixel, shadow);
        batch(canvas -> {
            layout.target(canvas);
            ComponentFlattener.basic().flatten(text.asComponent(), layout);
        });
        return layout.widest();
    }
}
