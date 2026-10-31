package fr.euphyllia.fidorial.server.world.storage;

import fr.euphyllia.fidorial.server.world.anvil.RegionCompression;
import fr.euphyllia.fidorial.server.world.anvil.RegionConstants;
import fr.euphyllia.fidorial.server.world.anvil.RegionFile;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.registry.DataFixersRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Function;

public final class EntityRegionStorage implements AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EntityRegionStorage.class);

    private final WorldPathsImpl paths;
    private final Function<Key, RegionCompression> compression;
    private final RegionFileCache regions = new RegionFileCache(RegionFileCache.DEFAULT_MAX_OPEN);

    public EntityRegionStorage(final WorldPathsImpl paths, final Function<Key, RegionCompression> compression) {
        this.paths = paths;
        this.compression = compression;
    }

    private <R> R withRegion(final Dimension dim, final int chunkX, final int chunkZ,
                             final RegionFileCache.RegionAction<R> action) throws IOException {
        final int rx = RegionConstants.chunkToRegion(chunkX);
        final int rz = RegionConstants.chunkToRegion(chunkZ);
        return regions.with(dim.id(), rx, rz, paths.entitiesDir(dim).resolve(RegionConstants.fileName(rx, rz)), action);
    }

    public boolean hasChunk(final Dimension dim, final int chunkX, final int chunkZ) throws IOException {
        return withRegion(dim, chunkX, chunkZ, rf -> rf.hasChunk(chunkX, chunkZ));
    }

    public @Nullable CompoundBinaryTag load(final Dimension dim, final int chunkX, final int chunkZ) throws IOException {
        final RegionFile.ChunkRead read = withRegion(dim, chunkX, chunkZ, rf -> {
            final RegionFile.ChunkRead found = rf.read(chunkX, chunkZ);
            if (found != null) {
                convertIfNeeded(rf, dim, chunkX, chunkZ, found);
            }
            return found;
        });
        if (read == null) return null;
        return DataFixersRegistry.update(DataFixerType.ENTITY, read.tag(), true);
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
                (rx, rz) -> regions.peek(dim.id(), rx, rz));
    }

    public void save(final Dimension dim, final int chunkX, final int chunkZ, final CompoundBinaryTag nbt) throws IOException {
        withRegion(dim, chunkX, chunkZ, rf -> {
            rf.writeChunk(chunkX, chunkZ, nbt, compression.apply(dim.id()));
            return null;
        });
    }

    @Override
    public void close() {
        regions.close();
    }
}
