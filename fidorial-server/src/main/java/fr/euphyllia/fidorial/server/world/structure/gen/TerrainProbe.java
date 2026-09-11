package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.world.ChunkGenerator;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.structure.FutureCache;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainView;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TerrainProbe implements TerrainView {

    private static final int SUMMARIES = 8192;
    private static final int PROTO_CHUNKS = 256;

    private final ChunkGenerator base;
    private final long seed;
    private final int minY;
    private final int height;
    private final FutureCache<Long, ChunkSummary> summaries = new FutureCache<>(SUMMARIES);
    private final LinkedHashMap<Long, ChunkColumn> protoChunks = new LinkedHashMap<>(PROTO_CHUNKS, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<Long, ChunkColumn> eldest) {
            return size() > PROTO_CHUNKS;
        }
    };

    public TerrainProbe(final ChunkGenerator base, final long seed) {
        this.base = base;
        this.seed = seed;
        this.minY = base.dimensionType().minY();
        this.height = base.dimensionType().height();
    }

    public ChunkSummary summary(final int chunkX, final int chunkZ) {
        return summaries.get(ChunkPos.chunkKey(chunkX, chunkZ), key -> {
            final ChunkColumn column = base.generate(chunkX, chunkZ);
            final ChunkSummary summary = ChunkSummary.of(column);
            synchronized (protoChunks) {
                protoChunks.putIfAbsent(key, column);
            }
            return summary;
        });
    }


    public void offer(final int chunkX, final int chunkZ, final ChunkColumn column) {
        summaries.get(ChunkPos.chunkKey(chunkX, chunkZ), key -> ChunkSummary.of(column));
    }

    public @Nullable ChunkColumn takeProtoChunk(final int chunkX, final int chunkZ) {
        synchronized (protoChunks) {
            return protoChunks.remove(ChunkPos.chunkKey(chunkX, chunkZ));
        }
    }

    public void clear() {
        summaries.clear();
        synchronized (protoChunks) {
            protoChunks.clear();
        }
    }

    @Override
    public long seed() {
        return seed;
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int worldSurface(final int x, final int z) {
        return summary(x >> 4, z >> 4).worldSurface(x & 15, z & 15);
    }

    @Override
    public int oceanFloor(final int x, final int z) {
        return summary(x >> 4, z >> 4).oceanFloor(x & 15, z & 15);
    }

    @Override
    public Key biome(final int x, final int y, final int z) {
        return summary(x >> 4, z >> 4).biome(x & 15, y, z & 15);
    }
}
