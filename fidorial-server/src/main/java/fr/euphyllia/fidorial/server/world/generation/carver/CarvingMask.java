package fr.euphyllia.fidorial.server.world.generation.carver;

import java.util.BitSet;

/**
 * The blocks of a chunk the carvers hollow out, one bit per block between two heights.
 */
public final class CarvingMask {

    private final int minY;
    private final int maxY;
    private final BitSet bits;

    /**
     * @param minY the lowest height a carver may reach, excluded by the carvers themselves
     * @param maxY the highest height a carver may reach
     */
    public CarvingMask(final int minY, final int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        this.bits = new BitSet(256 * Math.max(0, maxY - minY + 1));
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    private int index(final int x, final int y, final int z) {
        return (y - minY) << 8 | z << 4 | x;
    }

    /**
     * Marks a block as carved.
     *
     * @param x inside the chunk, from 0 to 15
     * @param z inside the chunk, from 0 to 15
     */
    public void carve(final int x, final int y, final int z) {
        if (y >= minY && y <= maxY) {
            bits.set(index(x, y, z));
        }
    }

    public boolean isCarved(final int x, final int y, final int z) {
        return y >= minY && y <= maxY && bits.get(index(x, y, z));
    }

    public boolean isEmpty() {
        return bits.isEmpty();
    }
}
