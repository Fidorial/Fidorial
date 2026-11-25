package fr.euphyllia.fidorial.server.world.generation.density;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

import java.util.Arrays;

/**
 * The caches of one thread evaluating the functions of a {@link DensityProgram}: the last value of every cache
 * point, and the samples taken on the coarse grids of the upscaled functions.
 *
 * <p>Nothing kept here changes a result; it only saves work. {@link #reset()} drops everything, which a caller
 * does between unrelated areas so that the grids do not grow without end. Not thread-safe.</p>
 */
public final class DensityContext {

    private long[] keys;
    private float[] values;
    private int[] stamps;
    private Long2FloatOpenHashMap[] grids;
    private int stamp = 1;

    DensityContext(final int cacheSlots, final int gridSlots) {
        this.keys = new long[cacheSlots];
        this.values = new float[cacheSlots];
        this.stamps = new int[cacheSlots];
        this.grids = new Long2FloatOpenHashMap[0];
        growGrids(gridSlots);
    }

    private void growGrids(final int size) {
        final int old = grids.length;
        grids = Arrays.copyOf(grids, size);
        for (int i = old; i < size; i++) {
            final Long2FloatOpenHashMap grid = new Long2FloatOpenHashMap();
            grid.defaultReturnValue(Float.NaN);
            grids[i] = grid;
        }
    }

    private void growCaches(final int size) {
        keys = Arrays.copyOf(keys, size);
        values = Arrays.copyOf(values, size);
        stamps = Arrays.copyOf(stamps, size);
    }

    /**
     * {@return a key for a block, unique within the heights world generation reaches}
     */
    static long blockKey(final int x, final int y, final int z) {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | ((long) y & 0xFFFL);
    }

    /**
     * {@return a key for a column}
     */
    static long columnKey(final int x, final int z) {
        return (long) x << 32 | ((long) z & 0xFFFFFFFFL);
    }

    /**
     * {@return the value of a function, reused when the last computation of this slot was for the same key}
     */
    float cached(final int slot, final long key, final Density input, final int x, final int y, final int z) {
        if (slot >= stamps.length) {
            // functions compiled after this context was made
            growCaches(slot + 16);
        }
        if (stamps[slot] == stamp && keys[slot] == key) {
            return values[slot];
        }
        final float value = input.compute(this, x, y, z);
        keys[slot] = key;
        values[slot] = value;
        stamps[slot] = stamp;
        return value;
    }

    /**
     * {@return the coarse grid samples of an upscaled function, keyed by {@link #blockKey}; missing samples read
     * as NaN}
     */
    Long2FloatOpenHashMap grid(final int slot) {
        if (slot >= grids.length) {
            growGrids(slot + 4);
        }
        return grids[slot];
    }

    /**
     * Forgets every cached value.
     */
    public void reset() {
        stamp++;
        if (stamp == 0) {
            // after a wrap around, old stamps could match again
            Arrays.fill(stamps, 0);
            stamp = 1;
        }
        for (final Long2FloatOpenHashMap grid : grids) {
            if (grid.size() > 4096) {
                grid.clear();
                grid.trim(4096);
            } else {
                grid.clear();
            }
        }
    }
}
