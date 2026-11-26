package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.euphyllia.fidorial.server.world.generation.terrain.GenChunk;
import net.kyori.adventure.nbt.CompoundBinaryTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A chunk between its terrain and its final form: the decorations of the chunks around it write into it, then it
 * is handed over once all nine of them are done.
 *
 * <p>Keeps the column heights of every {@link Heightmap}, the extra data of the block entities placed by the
 * decorations, and the fluids that must start flowing. Guarded by {@link #lock()}: a decoration holds the locks
 * of the nine chunks it may touch.</p>
 */
public final class DecorationChunk {

    private static final int COLUMNS = 256;
    private static final Heightmap[] HEIGHTMAPS = Heightmap.values();

    private final GenChunk blocks;
    private final StateFlags states;
    private final int[] heights;
    private final Map<Integer, CompoundBinaryTag> blockEntities = new HashMap<>();
    private final List<int[]> fluidTicks = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private boolean handedOver;
    private boolean missesDecorations;

    /**
     * @param blocks the terrain, owned by this chunk from now on
     */
    public DecorationChunk(final GenChunk blocks, final StateFlags states) {
        this.blocks = blocks;
        this.states = states;
        this.heights = new int[HEIGHTMAPS.length * COLUMNS];
        computeHeights();
    }

    private void computeHeights() {
        final int kinds = HEIGHTMAPS.length;
        final int minY = blocks.minY();
        final int top = minY + blocks.height() - 1;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int column = z << 4 | x;
                int missing = (1 << kinds) - 1;
                for (int kind = 0; kind < kinds; kind++) {
                    heights[kind * COLUMNS + column] = minY;
                }
                for (int y = top; y >= minY && missing != 0; y--) {
                    final char id = blocks.id(x, y, z);
                    if (states.isAir(id)) {
                        continue;
                    }
                    for (int kind = 0; kind < kinds; kind++) {
                        if ((missing & (1 << kind)) != 0 && states.counts(HEIGHTMAPS[kind], id)) {
                            heights[kind * COLUMNS + column] = y + 1;
                            missing &= ~(1 << kind);
                        }
                    }
                }
            }
        }
    }

    public GenChunk blocks() {
        return blocks;
    }

    public ReentrantLock lock() {
        return lock;
    }

    /**
     * {@return the first free height of a column for a kind}
     */
    public int height(final Heightmap kind, final int localX, final int localZ) {
        return heights[kind.ordinal() * COLUMNS + (localZ << 4 | localX)];
    }

    /**
     * Sets a block and follows it in the column heights.
     */
    public void set(final int localX, final int y, final int localZ, final char id) {
        blocks.setId(localX, y, localZ, id);
        final int column = localZ << 4 | localX;
        final int minY = blocks.minY();
        for (final Heightmap kind : HEIGHTMAPS) {
            if (kind.generationOnly()) {
                continue;
            }
            final int slot = kind.ordinal() * COLUMNS + column;
            final int free = heights[slot];
            if (y <= free - 2) {
                continue;
            }
            if (states.counts(kind, id)) {
                if (y >= free) {
                    heights[slot] = y + 1;
                }
            } else if (y == free - 1) {
                int found = minY;
                for (int below = y - 1; below >= minY; below--) {
                    if (states.counts(kind, blocks.id(localX, below, localZ))) {
                        found = below + 1;
                        break;
                    }
                }
                heights[slot] = found;
            }
        }
        if (!blockEntities.isEmpty()) {
            blockEntities.remove(BlockEntity.positionKey(localX, y, localZ));
        }
    }

    /**
     * Sets a block without following it in the column heights, as ore veins do.
     */
    public void setRaw(final int localX, final int y, final int localZ, final char id) {
        blocks.setId(localX, y, localZ, id);
        if (!blockEntities.isEmpty()) {
            blockEntities.remove(BlockEntity.positionKey(localX, y, localZ));
        }
    }

    /**
     * Keeps the extra data of a block entity, read when the chunk becomes final.
     */
    public void putBlockEntity(final int localX, final int y, final int localZ, final CompoundBinaryTag data) {
        blockEntities.put(BlockEntity.positionKey(localX, y, localZ), data);
    }

    public Map<Integer, CompoundBinaryTag> blockEntities() {
        return blockEntities;
    }

    /**
     * Remembers a fluid that must start flowing once the chunk is loaded.
     *
     * @param lava {@code true} for lava, {@code false} for water
     */
    public void scheduleFluid(final int x, final int y, final int z, final boolean lava) {
        fluidTicks.add(new int[] {x, y, z, lava ? 1 : 0});
    }

    public List<int[]> fluidTicks() {
        return fluidTicks;
    }

    boolean handedOver() {
        return handedOver;
    }

    void markHandedOver() {
        this.handedOver = true;
    }

    /**
     * {@return {@code true} if a decoration that should have written into this chunk ran before it existed}
     */
    boolean missesDecorations() {
        return missesDecorations;
    }

    void markMissesDecorations() {
        this.missesDecorations = true;
    }
}
