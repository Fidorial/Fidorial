package fr.euphyllia.fidorial.server.world.storage;

import ca.spottedleaf.converter.types.MapType;
import fr.euphyllia.fidorial.server.world.anvil.RegionCompression;
import fr.euphyllia.fidorial.server.world.anvil.RegionConstants;
import fr.euphyllia.fidorial.server.world.anvil.RegionFile;
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

public final class EntityRegionStorage implements AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EntityRegionStorage.class);

    private final WorldPaths paths;
    private final Function<Key, RegionCompression> compression;
    private final Map<RegionKey, RegionFile> regionCache = new ConcurrentHashMap<>();

    public EntityRegionStorage(final WorldPaths paths, final Function<Key, RegionCompression> compression) {
        this.paths = paths;
        this.compression = compression;
    }

    private record RegionKey(Key dimension, int regionX, int regionZ) {
    }

    private RegionFile region(final Dimension dim, final int chunkX, final int chunkZ) {
        final int rx = RegionConstants.chunkToRegion(chunkX);
        final int rz = RegionConstants.chunkToRegion(chunkZ);
        final RegionKey key = new RegionKey(dim.id(), rx, rz);
        return regionCache.computeIfAbsent(key, _ -> {
            final Path file = paths.entitiesDir(dim).resolve(RegionConstants.fileName(rx, rz));
            try {
                return new RegionFile(file);
            } catch (final IOException e) {
                throw new RuntimeException("Unable to open the entities file: " + file, e);
            }
        });
    }

    public boolean hasChunk(final Dimension dim, final int chunkX, final int chunkZ) {
        return region(dim, chunkX, chunkZ).hasChunk(chunkX, chunkZ);
    }

    public @Nullable CompoundBinaryTag load(final Dimension dim, final int chunkX, final int chunkZ) throws IOException {
        final RegionFile rf = region(dim, chunkX, chunkZ);
        final RegionFile.ChunkRead read = rf.read(chunkX, chunkZ);
        if (read == null) {
            return null;
        }
        CompoundBinaryTag nbt = read.tag();
        convertIfNeeded(rf, dim, chunkX, chunkZ, read);

        final int sourceVersion = nbt.getInt("DataVersion");
        final int latest = DataFixersRegistry.latestDataFixerVersion();
        if (sourceVersion < latest) {
            final MapType fixed = DataFixersRegistry.update(
                    DataFixerType.ENTITY, NbtMapType.of(nbt), sourceVersion);
            nbt = ((NbtMapType) fixed).toCompound().putInt("DataVersion", latest);
        }

        return nbt;
    }

    /**
     * Rewrites entities stored with another compression than the configured one; see {@link ChunkStorage}.
     */
    private void convertIfNeeded(final RegionFile rf, final Dimension dim, final int chunkX, final int chunkZ,
                                 final RegionFile.ChunkRead read) {
        final RegionCompression target = compression.apply(dim.id());
        if (read.compression() == target) {
            return;
        }
        try {
            rf.recompress(chunkX, chunkZ, target);
        } catch (final IOException e) {
            LOGGER.warn("Could not convert the entities of chunk {},{} of {} from {} to {}: {}", chunkX, chunkZ,
                    dim.id().asString(), read.compression().configName(), target.configName(), e.getMessage());
        }
    }

    /**
     * Rewrites in {@code target} the entities of {@code dim} stored with another compression; see
     * {@link ChunkStorage#convertAll}.
     */
    RegionRecompressor.Result convertAll(final Dimension dim, final RegionCompression target) throws IOException {
        return RegionRecompressor.convertDirectory(paths.entitiesDir(dim), target,
                (rx, rz) -> regionCache.get(new RegionKey(dim.id(), rx, rz)));
    }

    public void save(final Dimension dim, final int chunkX, final int chunkZ, final CompoundBinaryTag nbt) throws IOException {
        region(dim, chunkX, chunkZ).writeChunk(chunkX, chunkZ, nbt, compression.apply(dim.id()));
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
