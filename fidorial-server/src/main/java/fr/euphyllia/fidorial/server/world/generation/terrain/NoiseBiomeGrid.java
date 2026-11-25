package fr.euphyllia.fidorial.server.world.generation.terrain;

import fr.euphyllia.fidorial.server.world.generation.biome.JitteredBiomeGrid;

import java.util.BitSet;

/**
 * The biomes of a chunk and of a margin of one cell around it, one per cell of 4 by 4 by 4 blocks, and the biome
 * of each block worked out from them: a block takes the biome of the nearest of the eight cell corners around it,
 * each corner moved by a random offset. The margin is what lets the blocks at the edges of the chunk see past it.
 *
 * <p>Immutable once filled, safe to share between threads.</p>
 */
public final class NoiseBiomeGrid {

    private static final int SIDE = 6;

    private final int minQuartX;
    private final int minQuartY;
    private final int minQuartZ;
    private final int sizeY;
    private final int minY;
    private final int maxY;
    private final char[] cells;
    private final JitteredBiomeGrid zoom;
    private final boolean uniform;

    /**
     * The biome of a cell.
     */
    @FunctionalInterface
    public interface CellBiome {
        char biomeAt(int quartX, int quartY, int quartZ);
    }

    /**
     * Fills the grid of a chunk.
     *
     * @param zoom the jitter of the world
     * @param minY the lowest block of the chunk
     * @param height how many blocks the chunk is high
     */
    public NoiseBiomeGrid(final int chunkX, final int chunkZ, final int minY, final int height, final JitteredBiomeGrid zoom,
                          final CellBiome source) {
        this.minQuartX = (chunkX << 2) - 1;
        this.minQuartY = (minY >> 2) - 1;
        this.minQuartZ = (chunkZ << 2) - 1;
        this.sizeY = (height >> 2) + 2;
        this.minY = minY;
        this.maxY = minY + height - 1;
        this.zoom = zoom;
        this.cells = new char[SIDE * SIDE * sizeY];
        boolean same = true;
        for (int x = 0; x < SIDE; x++) {
            for (int z = 0; z < SIDE; z++) {
                for (int y = 0; y < sizeY; y++) {
                    final char biome = source.biomeAt(minQuartX + x, minQuartY + y, minQuartZ + z);
                    cells[index(x, y, z)] = biome;
                    same &= biome == cells[0];
                }
            }
        }
        this.uniform = same;
    }

    private int index(final int x, final int y, final int z) {
        return (x * SIDE + z) * sizeY + y;
    }

    /**
     * {@return the biome of a cell; heights past the grid read the nearest cell}
     */
    public char cell(final int quartX, final int quartY, final int quartZ) {
        final int y = Math.clamp(quartY - minQuartY, 0, sizeY - 1);
        return cells[index(quartX - minQuartX, y, quartZ - minQuartZ)];
    }

    /**
     * {@return the biome of the cell holding a block, without the jitter}
     */
    public char cellOfBlock(final int blockX, final int blockY, final int blockZ) {
        return cell(blockX >> 2, Math.clamp(blockY, minY, maxY) >> 2, blockZ >> 2);
    }

    /**
     * {@return the biome of a block of the chunk}
     *
     * @param blockX an absolute coordinate inside the chunk
     * @param blockZ the same along z
     */
    public char biome(final int blockX, final int blockY, final int blockZ) {
        if (uniform) {
            return cells[0];
        }
        final int y = Math.clamp(blockY, minY, maxY);
        final int corner = zoom.nearestCorner(blockX, y, blockZ);
        final int cellX = JitteredBiomeGrid.cellOf(blockX) + ((corner & JitteredBiomeGrid.FAR_X) != 0 ? 1 : 0);
        final int cellY = JitteredBiomeGrid.cellOf(y) + ((corner & JitteredBiomeGrid.FAR_Y) != 0 ? 1 : 0);
        final int cellZ = JitteredBiomeGrid.cellOf(blockZ) + ((corner & JitteredBiomeGrid.FAR_Z) != 0 ? 1 : 0);
        return cell(cellX, cellY, cellZ);
    }

    /**
     * {@return whether the cells a section of the chunk may draw its blocks from all hold one biome}
     *
     * @param section the index of the section from the bottom of the chunk
     */
    public boolean sectionIsUniform(final int section) {
        if (uniform) {
            return true;
        }
        final int from = section * 4;
        final char first = cells[index(1, from + 1, 1)];
        for (int x = 0; x < SIDE; x++) {
            for (int z = 0; z < SIDE; z++) {
                for (int y = from; y < Math.min(from + 6, sizeY); y++) {
                    if (cells[index(x, y, z)] != first) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@return the biome of the cell at the centre of a section, the one it holds when uniform}
     */
    public char sectionBiome(final int section) {
        return cells[index(1, section * 4 + 1, 1)];
    }

    /**
     * {@return every biome numbered in the grid, each once}
     */
    public char[] distinct() {
        return distinctOf(cells);
    }

    /**
     * Marks the biomes of the chunk itself, the cells of the margin left out: the biomes its sections are saved
     * with.
     */
    public void collectOwn(final BitSet out) {
        if (uniform) {
            out.set(cells[0]);
            return;
        }
        for (int x = 1; x < SIDE - 1; x++) {
            for (int z = 1; z < SIDE - 1; z++) {
                for (int y = 1; y < sizeY - 1; y++) {
                    out.set(cells[index(x, y, z)]);
                }
            }
        }
    }

    private static char[] distinctOf(final char[] cells) {
        final BitSet seen = new BitSet();
        for (final char cell : cells) {
            seen.set(cell);
        }
        final char[] result = new char[seen.cardinality()];
        int i = 0;
        for (int bit = seen.nextSetBit(0); bit >= 0; bit = seen.nextSetBit(bit + 1)) {
            result[i++] = (char) bit;
        }
        return result;
    }
}
