package fr.fidorial.world;

import fr.fidorial.entity.Entity;
import fr.fidorial.scheduler.RegionizedScheduler;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.time.DayNightCycle;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface World extends Keyed, ForwardingAudience {

    int minY();

    int height();

    /**
     * {@return this world's dimension type}
     *
     * @since 0.1.0
     */
    DimensionTypeDefinition dimensionType();

    DayNightCycle dayNightCycle();

    /**
     * {@return the scheduler responsible for running tasks against positions in this world}
     *
     * @since 0.1.0
     */
    RegionizedScheduler scheduler();

    CompletableFuture<Chunk> getChunkAsync(int chunkX, int chunkZ);

    default CompletableFuture<Chunk> getChunkAsync(final ChunkPos pos) {
        return getChunkAsync(pos.x(), pos.z());
    }

    Optional<Chunk> getChunkIfLoaded(int chunkX, int chunkZ);

    default Chunk getChunkIfLoaded(final ChunkPos pos) {
        return getChunkIfLoaded(pos.x(), pos.z()).orElseThrow();
    }

    /**
     * Whether a chunk is force-loaded.
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return {@code true} if the chunk is force-loaded
     * @since 0.1.0
     */
    boolean isChunkForceLoaded(int chunkX, int chunkZ);

    /**
     * Whether a chunk is force-loaded.
     *
     * @param pos the chunk position
     * @return {@code true} if the chunk is force-loaded
     * @see #isChunkForceLoaded(int, int)
     * @since 0.1.0
     */
    default boolean isChunkForceLoaded(final ChunkPos pos) {
        return isChunkForceLoaded(pos.x(), pos.z());
    }

    /**
     * {@return an immutable snapshot of the force-loaded chunks of this world}
     *
     * @since 0.1.0
     */
    Set<ChunkPos> forceLoadedChunks();

    default boolean isChunkLoaded(final int chunkX, final int chunkZ) {
        return getChunkIfLoaded(chunkX, chunkZ).isPresent();
    }

    Optional<Key> blockKeyAt(BlockPos pos);

    int getBlockStateId(BlockPos pos);

    boolean setBlockStateId(BlockPos pos, int stateId);

    int blockLight(BlockPos pos);

    int skyLight(BlockPos pos);

    int lightLevel(BlockPos pos);

    Collection<? extends Entity> entities();

    Entity entity(UUID uuid);

    Entity entity(int entityId);

    /**
     * Sets the force-loaded state of a chunk.
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @param forced whether to force-load or unforce-load the chunk
     * @return {@code true} forces the chunk to remain loaded, {@code false} removes the loaded chunk
     * @since 0.1.0
     */
    boolean setChunkForceLoaded(int chunkX, int chunkZ, boolean forced);

    CompletableFuture<Boolean> unloadChunkAsync(int chunkX, int chunkZ);

    default CompletableFuture<Boolean> unloadChunkAsync(final ChunkPos pos) {
        return unloadChunkAsync(pos.x(), pos.z());
    }
}
