package fr.euphyllia.fidorial.server.world.generation.terrain;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.block.StatePalette;

import java.util.Arrays;

/**
 * A chunk while it is being generated: one {@code char} per block, numbered by the {@link StatePalette} of the
 * generator, and the noise biomes around it.
 *
 * <p>A section that holds a single state keeps no array. Coordinates are local along x and z, absolute along y.
 * Not thread-safe while it is being written; read-only once handed to the decoration step.</p>
 */
public final class GenChunk {

    private static final int SECTION_VOLUME = 4096;

    private final int chunkX;
    private final int chunkZ;
    private final int minY;
    private final int height;
    private final StatePalette palette;
    private final char[][] sections;
    private final char[] uniform;
    private final NoiseBiomeGrid biomes;

    /**
     * @param fill the state every block starts as
     */
    public GenChunk(final int chunkX, final int chunkZ, final int minY, final int height, final StatePalette palette,
                    final BlockState fill, final NoiseBiomeGrid biomes) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.minY = minY;
        this.height = height;
        this.palette = palette;
        this.sections = new char[height >> 4][];
        this.uniform = new char[height >> 4];
        Arrays.fill(uniform, palette.id(fill));
        this.biomes = biomes;
    }

    private GenChunk(final GenChunk source) {
        this.chunkX = source.chunkX;
        this.chunkZ = source.chunkZ;
        this.minY = source.minY;
        this.height = source.height;
        this.palette = source.palette;
        this.sections = new char[source.sections.length][];
        for (int i = 0; i < sections.length; i++) {
            sections[i] = source.sections[i] == null ? null : source.sections[i].clone();
        }
        this.uniform = source.uniform.clone();
        this.biomes = source.biomes;
    }

    /**
     * {@return a copy whose blocks can be changed without touching this chunk; the biomes are shared}
     */
    public GenChunk copy() {
        return new GenChunk(this);
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public int minY() {
        return minY;
    }

    public int height() {
        return height;
    }

    public int sectionCount() {
        return sections.length;
    }

    public StatePalette palette() {
        return palette;
    }

    public NoiseBiomeGrid biomes() {
        return biomes;
    }

    private static int index(final int x, final int y, final int z) {
        return (y & 15) << 8 | z << 4 | x;
    }

    /**
     * {@return whether a height is inside the chunk}
     */
    public boolean contains(final int y) {
        return y >= minY && y < minY + height;
    }

    /**
     * {@return the number of a block, or the air number outside the chunk}
     */
    public char id(final int x, final int y, final int z) {
        if (!contains(y)) {
            // the generator numbers air first
            return 0;
        }
        final int section = (y - minY) >> 4;
        final char[] blocks = sections[section];
        return blocks == null ? uniform[section] : blocks[index(x, y, z)];
    }

    public BlockState get(final int x, final int y, final int z) {
        return palette.state(id(x, y, z));
    }

    public void setId(final int x, final int y, final int z, final char id) {
        if (!contains(y)) {
            return;
        }
        final int section = (y - minY) >> 4;
        char[] blocks = sections[section];
        if (blocks == null) {
            if (uniform[section] == id) {
                return;
            }
            blocks = new char[SECTION_VOLUME];
            Arrays.fill(blocks, uniform[section]);
            sections[section] = blocks;
        }
        blocks[index(x, y, z)] = id;
    }

    public void set(final int x, final int y, final int z, final BlockState state) {
        setId(x, y, z, palette.id(state));
    }

    /**
     * {@return the section array of a section index, {@code null} when it holds a single state}
     */
    public char[] sectionData(final int section) {
        return sections[section];
    }

    /**
     * {@return the single state of a section that keeps no array}
     */
    public char uniformId(final int section) {
        return uniform[section];
    }

    /**
     * {@return the height of the top block of a column matching a test, plus one; {@code minY} for none}
     */
    public int top(final int x, final int z, final java.util.function.Predicate<BlockState> occupies) {
        for (int section = sections.length - 1; section >= 0; section--) {
            final char[] blocks = sections[section];
            final int base = minY + (section << 4);
            if (blocks == null) {
                if (occupies.test(palette.state(uniform[section]))) {
                    return base + 16;
                }
                continue;
            }
            for (int ly = 15; ly >= 0; ly--) {
                if (occupies.test(palette.state(blocks[ly << 8 | z << 4 | x]))) {
                    return base + ly + 1;
                }
            }
        }
        return minY;
    }
}
