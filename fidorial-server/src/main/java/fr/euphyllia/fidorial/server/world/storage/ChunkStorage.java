package fr.euphyllia.fidorial.server.world.storage;

import fr.euphyllia.fidorial.server.world.anvil.RegionCompression;
import fr.euphyllia.fidorial.server.world.anvil.RegionConstants;
import fr.euphyllia.fidorial.server.world.anvil.RegionFile;
import fr.euphyllia.fidorial.server.world.chunk.AnvilChunkSerializer;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.registry.DataFixersRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Function;

public final class ChunkStorage implements AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ChunkStorage.class);

    private final WorldPathsImpl paths;
    private final AnvilChunkSerializer serializer;
    private final BlockState defaultBlock;
    private final Key defaultBiome;
    private final Function<Key, RegionCompression> compression;

    private final RegionFileCache regions = new RegionFileCache(RegionFileCache.DEFAULT_MAX_OPEN);

    public ChunkStorage(
            final WorldPathsImpl paths,
            final AnvilChunkSerializer serializer,
            final BlockState defaultBlock,
            final Key defaultBiome,
            final Function<Key, RegionCompression> compression
    ) {
        this.paths = paths;
        this.serializer = serializer;
        this.defaultBlock = defaultBlock;
        this.defaultBiome = defaultBiome;
        this.compression = compression;
    }

    private <R> R withRegion(final Dimension dim, final int chunkX, final int chunkZ,
                             final RegionFileCache.RegionAction<R> action) throws IOException {
        final int rx = RegionConstants.chunkToRegion(chunkX);
        final int rz = RegionConstants.chunkToRegion(chunkZ);
        return regions.with(dim.id(), rx, rz, paths.regionDir(dim).resolve(RegionConstants.fileName(rx, rz)), action);
    }

    public @Nullable ChunkColumn load(final Dimension dim, final int chunkX, final int chunkZ, final int minY, final int height) throws IOException {
        final RegionFile.ChunkRead read = withRegion(dim, chunkX, chunkZ, rf -> {
            final RegionFile.ChunkRead found = rf.read(chunkX, chunkZ);
            if (found != null) {
                convertIfNeeded(rf, dim, chunkX, chunkZ, found);
            }
            return found;
        });
        if (read == null) return null;
        final CompoundBinaryTag nbt = DataFixersRegistry.update(DataFixerType.CHUNK, read.tag(), true);
        return serializer.fromNbt(nbt, minY, height, defaultBlock, defaultBiome);
    }

    /**
     * Rewrites a chunk stored with another compression than the configured one (after
     * {@code storage.region-compression} changed). The original NBT is written back as is; a failure does not prevent
     * the chunk from loading.
     */
    private void convertIfNeeded(final RegionFile rf, final Dimension dim, final int chunkX, final int chunkZ,
                                 final RegionFile.ChunkRead read) {
        final RegionCompression target = compression.apply(dim.id());
        if (read.compression() == target) return;
        try {
            rf.recompress(chunkX, chunkZ, target);
        } catch (final IOException e) {
            LOGGER.warn("Could not convert chunk {},{} of {} from {} to {}: {}", chunkX, chunkZ, dim.id().asString(),
                    read.compression().configName(), target.configName(), e.getMessage());
        }
    }

    /**
     * Rewrites in {@code target} every chunk of {@code dim} stored with another compression. Must be called while the
     * world is not loaded, so that no other region file of the dimension is opened during the conversion.
     */
    RegionRecompressor.Result convertAll(final Dimension dim, final RegionCompression target) throws IOException {
        return RegionRecompressor.convertDirectory(paths.regionDir(dim), target,
                (rx, rz) -> regions.peek(dim.id(), rx, rz));
    }

    public void save(final Dimension dim, final ChunkColumn chunk) throws IOException {
        chunk.setLastUpdate(System.currentTimeMillis() / 50L);
        final CompoundBinaryTag nbt = serializer.toNbt(chunk);
        withRegion(dim, chunk.chunkX(), chunk.chunkZ(), rf -> {
            rf.writeChunk(chunk.chunkX(), chunk.chunkZ(), nbt, compression.apply(dim.id()));
            return null;
        });
    }

    @Override
    public void close() {
        regions.close();
    }
}
