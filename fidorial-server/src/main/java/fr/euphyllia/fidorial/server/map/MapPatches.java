package fr.euphyllia.fidorial.server.map;

import fr.fidorial.map.MapCanvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Turns the difference between two canvases into as few {@link MapPatch patches} as reasonable.
 */
final class MapPatches {

    private static final int SIZE = MapCanvas.SIZE;
    /**
     * Bands of changed rows closer than this are sent as one patch.
     */
    private static final int MERGE_GAP = 3;
    /**
     * Beyond this many bands, one rectangle covering them all is sent instead.
     */
    private static final int MAX_PATCHES = 8;

    private MapPatches() {
    }

    /**
     * Finds the rectangles to send to bring {@code sent} to {@code wanted}: bands of changed rows,
     * each as wide as its changes.
     */
    static List<MapPatch> diff(final byte[] sent, final byte[] wanted) {
        final int[] left = new int[SIZE];
        final int[] right = new int[SIZE];
        int first = -1;
        int last = -1;
        for (int y = 0; y < SIZE; y++) {
            left[y] = -1;
            final int row = y * SIZE;
            if (Arrays.mismatch(sent, row, row + SIZE, wanted, row, row + SIZE) < 0) {
                continue;
            }
            int l = 0;
            while (sent[row + l] == wanted[row + l]) {
                l++;
            }
            int r = SIZE - 1;
            while (sent[row + r] == wanted[row + r]) {
                r--;
            }
            left[y] = l;
            right[y] = r;
            if (first < 0) {
                first = y;
            }
            last = y;
        }
        if (first < 0) {
            return List.of();
        }
        final List<int[]> bands = new ArrayList<>();
        int[] band = null;
        for (int y = first; y <= last; y++) {
            if (left[y] < 0) {
                continue;
            }
            if (band != null && y - band[3] <= MERGE_GAP) {
                band[3] = y;
                band[0] = Math.min(band[0], left[y]);
                band[1] = Math.max(band[1], right[y]);
            } else {
                band = new int[]{left[y], right[y], y, y};
                bands.add(band);
            }
        }
        if (bands.size() > MAX_PATCHES) {
            int minX = SIZE;
            int maxX = 0;
            for (final int[] b : bands) {
                minX = Math.min(minX, b[0]);
                maxX = Math.max(maxX, b[1]);
            }
            bands.clear();
            bands.add(new int[]{minX, maxX, first, last});
        }
        final List<MapPatch> patches = new ArrayList<>(bands.size());
        for (final int[] b : bands) {
            final int width = b[1] - b[0] + 1;
            final int height = b[3] - b[2] + 1;
            final byte[] colors = new byte[width * height];
            for (int y = 0; y < height; y++) {
                System.arraycopy(wanted, (b[2] + y) * SIZE + b[0], colors, y * width, width);
            }
            patches.add(new MapPatch(b[0], b[2], width, height, colors));
        }
        return patches;
    }
}
