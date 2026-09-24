package fr.euphyllia.fidorial.server.world.light;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.world.ChunkPos;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

final class CachedLightAccess implements LightAccess {

    private static final int SIZE = 64; // power of two
    private static final int MASK = SIZE - 1;

    private final LightAccess delegate;

    private final long[] keys = new long[SIZE];
    private final @Nullable ChunkLightData[] data = new ChunkLightData[SIZE];
    private final @Nullable BlockColumnAccess[] columns = new BlockColumnAccess[SIZE];
    private final int[] topSection = new int[SIZE];
    private final boolean[] populated = new boolean[SIZE];
    private final boolean[] filled = new boolean[SIZE];

    CachedLightAccess(final LightAccess delegate) {
        this.delegate = delegate;
        Arrays.fill(keys, Long.MIN_VALUE);
    }

    private int slotFor(final int chunkX, final int chunkZ, final long key) {
        final int slot = (int) ((key * 0x9E3779B97F4A7C15L >>> 40)) & MASK;
        if (!filled[slot] || keys[slot] != key) {
            keys[slot] = key;
            final LightAccess.ColumnSnapshot snap = delegate.snapshotAt(chunkX, chunkZ);
            data[slot] = snap.lightData();
            columns[slot] = snap.column();
            topSection[slot] = snap.topNonEmptySectionY();
            populated[slot] = snap.populated();
            filled[slot] = true;
        }
        return slot;
    }

    @Override
    public int minY() {
        return delegate.minY();
    }

    @Override
    public int height() {
        return delegate.height();
    }

    @Override
    public @Nullable ChunkLightData lightAt(final int chunkX, final int chunkZ) {
        return data[slotFor(chunkX, chunkZ, ChunkPos.chunkKey(chunkX, chunkZ))];
    }

    @Override
    public @Nullable BlockColumnAccess columnAt(final int chunkX, final int chunkZ) {
        return columns[slotFor(chunkX, chunkZ, ChunkPos.chunkKey(chunkX, chunkZ))];
    }

    @Override
    public int topNonEmptySectionY(final int chunkX, final int chunkZ) {
        return topSection[slotFor(chunkX, chunkZ, ChunkPos.chunkKey(chunkX, chunkZ))];
    }

    @Override
    public boolean isLightPopulated(final int chunkX, final int chunkZ) {
        return populated[slotFor(chunkX, chunkZ, ChunkPos.chunkKey(chunkX, chunkZ))];
    }

    @Override
    public boolean sectionHasEmissiveBlocks(final int chunkX, final int sectionY, final int chunkZ) {
        return delegate.sectionHasEmissiveBlocks(chunkX, sectionY, chunkZ);
    }

    @Override
    public BlockState blockAt(final int x, final int y, final int z) {
        final BlockColumnAccess col = columnAt(x >> 4, z >> 4);
        return col != null ? col.blockAt(x & 15, y, z & 15) : delegate.blockAt(x, y, z);
    }
}
