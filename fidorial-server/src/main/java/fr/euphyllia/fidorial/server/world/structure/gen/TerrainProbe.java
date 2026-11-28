package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.world.ChunkGenerator;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.generation.NoiseChunkGenerator;
import fr.euphyllia.fidorial.server.world.structure.FutureCache;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainView;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.biome.BiomeDefinition;
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
            final ChunkSummary terrain = terrainSummary(chunkX, chunkZ);
            if (terrain != null) {
                return terrain;
            }
            final ChunkColumn column = base.generate(chunkX, chunkZ);
            final ChunkSummary summary = ChunkSummary.of(column);
            synchronized (protoChunks) {
                protoChunks.putIfAbsent(key, column);
            }
            return summary;
        });
    }


    /**
     * {@return the summary of the terrain the noise generator shapes for a chunk, before its features; {@code null}
     * when the chunks do not come from the noise generator}
     *
     * <p>A structure only needs the height of the ground and the biomes. Generating the chunk for them would also
     * decorate it, and a decoration needs the terrain of the 25 chunks around: probing one column would cost 25
     * terrains instead of one. The terrain is the one the generator keeps for the decorations, so the work is not
     * lost when the chunk is generated later.</p>
     */
    private @Nullable ChunkSummary terrainSummary(final int chunkX, final int chunkZ) {
        final NoiseChunkGenerator noise = base.noise();
        if (noise == null) {
            return null;
        }
        return ChunkSummary.of(noise.terrain(chunkX, chunkZ), noise.biomes(), minY, height);
    }

    public void offer(final int chunkX, final int chunkZ, final ChunkColumn column) {
        if (base.noise() != null) {
            // a noise chunk is summed up from its terrain, and only if a structure asks: the decorated column
            // would give other heights than the terrain summary does
            return;
        }
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
    public TypedKey<BiomeDefinition> biome(final int x, final int y, final int z) {
        return summary(x >> 4, z >> 4).biome(x & 15, y, z & 15);
    }
}
