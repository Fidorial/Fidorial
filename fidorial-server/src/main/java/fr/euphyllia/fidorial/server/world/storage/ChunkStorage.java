package fr.euphyllia.fidorial.server.world.storage;

import ca.spottedleaf.converter.types.MapType;
import fr.euphyllia.fidorial.server.world.anvil.RegionCompression;
import fr.euphyllia.fidorial.server.world.anvil.RegionConstants;
import fr.euphyllia.fidorial.server.world.anvil.RegionFile;
import fr.euphyllia.fidorial.server.world.chunk.AnvilChunkSerializer;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.registry.DataFixersRegistry;
import fr.euphyllia.fidorial.server.world.storage.datafixers.util.nbt.NbtMapType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ChunkStorage implements AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ChunkStorage.class);

    private final WorldPaths paths;
    private final AnvilChunkSerializer serializer;
    private final BlockState defaultBlock;
    private final Key defaultBiome;
    private final Function<Key, RegionCompression> compression;

    private final Map<RegionKey, RegionFile> regionCache = new ConcurrentHashMap<>();

    public ChunkStorage(
            final WorldPaths paths,
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

    private record RegionKey(Key dimension, int regionX, int regionZ) {
    }

    private RegionFile region(final Dimension dim, final int chunkX, final int chunkZ) {
        final int rx = RegionConstants.chunkToRegion(chunkX);
        final int rz = RegionConstants.chunkToRegion(chunkZ);
        final RegionKey key = new RegionKey(dim.id(), rx, rz);
        return regionCache.computeIfAbsent(key, k -> {
            final Path file = paths.regionDir(dim).resolve(RegionConstants.fileName(rx, rz));
            try {
                return new RegionFile(file);
            } catch (final IOException e) {
                throw new RuntimeException("Unable to open the region file: " + file, e);
            }
        });
    }

    public @Nullable ChunkColumn load(final Dimension dim, final int chunkX, final int chunkZ, final int minY, final int height) throws IOException {
        final RegionFile rf = region(dim, chunkX, chunkZ);
        final RegionFile.ChunkRead read = rf.read(chunkX, chunkZ);
        if (read == null) return null;
        CompoundBinaryTag nbt = read.tag();
        convertIfNeeded(rf, dim, chunkX, chunkZ, read);

        final int sourceVersion = nbt.getInt("DataVersion");
        final int latest = DataFixersRegistry.latestDataFixerVersion();
        if (sourceVersion < latest) {
            final MapType fixed = DataFixersRegistry.update(
                    DataFixerType.CHUNK, NbtMapType.of(nbt), sourceVersion);
            nbt = ((NbtMapType) fixed).toCompound().putInt("DataVersion", latest);
        }

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
                (rx, rz) -> regionCache.get(new RegionKey(dim.id(), rx, rz)));
    }

    public void save(final Dimension dim, final ChunkColumn chunk) throws IOException {
        chunk.setLastUpdate(System.currentTimeMillis() / 50L);
        final CompoundBinaryTag nbt = serializer.toNbt(chunk);
        final RegionFile rf = region(dim, chunk.chunkX(), chunk.chunkZ());
        rf.writeChunk(chunk.chunkX(), chunk.chunkZ(), nbt, compression.apply(dim.id()));
    }

    @Override
    public void close() {
        for (final RegionFile rf : regionCache.values()) {
            try {
                rf.close();
            } catch (final IOException ignored) {
            }
        }
        regionCache.clear();
    }
}
