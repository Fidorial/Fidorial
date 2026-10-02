package fr.fidorial.world;

import fr.fidorial.entity.Entity;
import fr.fidorial.gamerule.WorldGameRules;
import fr.fidorial.scheduler.RegionizedScheduler;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.time.DayNightCycle;
import fr.fidorial.world.weather.WeatherManager;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A loaded world, identified by its {@linkplain #key() key}.
 *
 * <p>As a {@link ForwardingAudience}, a world forwards anything sent to it to the players it
 * currently holds. Block reads may be issued from any thread; block writes are queued on the
 * region thread owning the position and complete asynchronously.</p>
 *
 * @see fr.fidorial.Server#worlds()
 * @since 0.1.0
 */
public interface World extends Keyed, ForwardingAudience {

    /**
     * {@return the lowest block Y coordinate of this world, inclusive}
     *
     * @since 0.1.0
     */
    int minY();

    /**
     * {@return the number of block layers of this world, starting at {@link #minY()}}
     *
     * @since 0.1.0
     */
    int height();

    /**
     * {@return this world's dimension type}
     *
     * @since 0.1.0
     */
    DimensionTypeDefinition dimensionType();

    /**
     * {@return the clock driving the time of day of this world}
     *
     * @since 0.1.0
     */
    DayNightCycle dayNightCycle();

    /**
     * The game rules in effect in this world: the base values held by the overworld, except for the
     * rules this world overrides.
     *
     * @return the game rules of this world
     * @since 0.1.0
     */
    WorldGameRules gameRules();

    /**
     * The weather of this world. Every world has its own weather; it only changes on its own in
     * worlds whose dimension type has a skylight, while the {@code advance_weather} game rule allows it.
     *
     * @return the weather of this world
     * @since 0.1.0
     */
    WeatherManager weather();

    /**
     * {@return the scheduler responsible for running tasks against positions in this world}
     *
     * @since 0.1.0
     */
    RegionizedScheduler scheduler();

    /**
     * Gets a chunk, loading or generating it when needed.
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return a future completing with the chunk, or exceptionally if it could not be read
     * @since 0.1.0
     */
    CompletableFuture<Chunk> chunkAsync(int chunkX, int chunkZ);

    /**
     * Gets a chunk, loading or generating it when needed.
     *
     * @param pos the chunk position
     * @return a future completing with the chunk, or exceptionally if it could not be read
     * @since 0.1.0
     */
    default CompletableFuture<Chunk> chunkAsync(final ChunkPos pos) {
        return chunkAsync(pos.x(), pos.z());
    }

    /**
     * Gets a chunk only if it is already loaded, without triggering any load.
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return the chunk, or empty if it is not loaded
     * @since 0.1.0
     */
    Optional<Chunk> chunkIfLoaded(int chunkX, int chunkZ);

    /**
     * Gets a chunk only if it is already loaded, without triggering any load.
     *
     * @param pos the chunk position
     * @return the chunk, or empty if it is not loaded
     * @since 0.1.0
     */
    default Optional<Chunk> chunkIfLoaded(final ChunkPos pos) {
        return chunkIfLoaded(pos.x(), pos.z());
    }

    /**
     * Checks whether a chunk is currently loaded.
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return {@code true} if the chunk is loaded
     * @since 0.1.0
     */
    default boolean isChunkLoaded(final int chunkX, final int chunkZ) {
        return chunkIfLoaded(chunkX, chunkZ).isPresent();
    }

    /**
     * Unloads a chunk, saving it first.
     *
     * <p>The request is refused when the chunk is not loaded, is force-loaded, or is still viewed
     * by a player.</p>
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return a future completing with {@code true} once the chunk is unloaded, or {@code false} if
     * the request was refused
     * @since 0.1.0
     */
    CompletableFuture<Boolean> unloadChunkAsync(int chunkX, int chunkZ);

    /**
     * Unloads a chunk, saving it first.
     *
     * @param pos the chunk position
     * @return a future completing with {@code true} once the chunk is unloaded, or {@code false} if
     * the request was refused
     * @see #unloadChunkAsync(int, int)
     * @since 0.1.0
     */
    default CompletableFuture<Boolean> unloadChunkAsync(final ChunkPos pos) {
        return unloadChunkAsync(pos.x(), pos.z());
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
     * Sets the force-loaded state of a chunk. A force-loaded chunk is loaded right away and stays
     * loaded, across restarts, until it is released.
     *
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @param forced {@code true} to force-load the chunk, {@code false} to release it
     * @return {@code true} if the force-loaded state changed
     * @since 0.1.0
     */
    boolean setChunkForceLoaded(int chunkX, int chunkZ, boolean forced);

    /**
     * {@return an immutable snapshot of the force-loaded chunks of this world}
     *
     * @since 0.1.0
     */
    Set<ChunkPos> forceLoadedChunks();

    /**
     * Gets the type of the block at a position.
     *
     * @param pos the block position
     * @return the block key, or empty for any kind of air
     * @since 0.1.0
     */
    Optional<Key> blockKeyAt(BlockPos pos);

    /**
     * Gets the block state at a position, loading the chunk when needed.
     *
     * @param pos the block position
     * @return the network identifier of the block state
     * @throws java.io.UncheckedIOException if the chunk could not be read
     * @see fr.fidorial.world.block.BlockRegistry#fromNetworkId(int)
     * @since 0.1.0
     */
    int blockStateId(BlockPos pos);

    /**
     * Replaces the block state at a position, on the region thread owning it.
     *
     * @param pos     the block position
     * @param stateId the network identifier of the new block state
     * @return a future completing with {@code true} if the block changed, {@code false} if it
     * already held that state or the write could not be scheduled
     * @since 0.1.0
     */
    CompletableFuture<Boolean> setBlockStateId(BlockPos pos, int stateId);

    /**
     * {@return the block light level at a position, between {@code 0} and {@code 15}}
     *
     * @param pos the block position
     * @since 0.1.0
     */
    int blockLight(BlockPos pos);

    /**
     * {@return the sky light level at a position, between {@code 0} and {@code 15}}
     *
     * @param pos the block position
     * @since 0.1.0
     */
    int skyLight(BlockPos pos);

    /**
     * {@return the effective light level at a position, the highest of block and sky light}
     *
     * @param pos the block position
     * @since 0.1.0
     */
    int lightLevel(BlockPos pos);

    /**
     * {@return the entities currently in this world, players included}
     *
     * @since 0.1.0
     */
    Collection<? extends Entity> entities();

    /**
     * Looks up an entity of this world by identity.
     *
     * @param uuid the entity identity
     * @return the entity, or empty if this world holds no entity with that identity
     * @since 0.1.0
     */
    Optional<? extends Entity> entity(UUID uuid);

    /**
     * Looks up an entity of this world by network identifier.
     *
     * @param entityId the network identifier, as returned by {@link Entity#entityId()}
     * @return the entity, or empty if this world holds no entity with that identifier
     * @since 0.1.0
     */
    Optional<? extends Entity> entity(int entityId);
}
