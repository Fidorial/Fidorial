package fr.euphyllia.fidorial.server.world.block.blockentity;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The block entities ticked on every game tick, as vanilla ticks a hopper: each type given a
 * {@link Ticker} is ticked while its chunk is loaded and its region ticks, after the scheduled
 * ticks, the block events and the blocks pistons move, in the order the block entities appeared.
 *
 * <p>A block entity is tracked when its block is placed, and when its chunk is generated or read
 * from disk. It is dropped once its chunk is unloaded or its block replaced, on the next game tick
 * of its region; its ticker is then told to {@linkplain Ticker#forget forget} it.</p>
 *
 * <p>The block entities of one region are only ever touched by the thread ticking it.</p>
 */
public final class TickingBlockEntities implements RegionTickHandler {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(TickingBlockEntities.class);

    private final ServerWorldManager worlds;
    private final ThreadedRegionRegionizer regionizer;
    private final Map<TypedKey<BlockEntityType>, Ticker> tickers = new ConcurrentHashMap<>();
    private final Map<RegionKey, RegionEntries> regions = new ConcurrentHashMap<>();

    /**
     * @param worlds     the worlds of the server
     * @param regionizer who owns which chunk
     */
    public TickingBlockEntities(final ServerWorldManager worlds, final ThreadedRegionRegionizer regionizer) {
        Preconditions.checkArgument(worlds != null, "The ticking block entities need the worlds of the server");
        Preconditions.checkArgument(regionizer != null, "The ticking block entities need to know who owns which chunk");
        this.worlds = worlds;
        this.regionizer = regionizer;
    }

    /**
     * Ticks every block entity of a type on every game tick.
     *
     * @param type   the type of block entity
     * @param ticker what ticks them
     * @throws IllegalArgumentException if the type already has a ticker
     */
    public void register(final TypedKey<BlockEntityType> type, final Ticker ticker) {
        Preconditions.checkArgument(type != null, "Only a type of block entity can be ticked, not null");
        Preconditions.checkArgument(ticker != null, "The block entities of %s need something to tick them", type);
        Preconditions.checkArgument(tickers.putIfAbsent(type, ticker) == null,
                "The block entities of %s are already ticked", type);
    }

    /**
     * {@return {@code true} if the block entities of a type are ticked}
     */
    public boolean isTicked(final TypedKey<BlockEntityType> type) {
        return tickers.containsKey(type);
    }

    /**
     * Starts ticking a block entity just placed, on the thread of the region owning it.
     *
     * @param world    the world of the block entity
     * @param position where it is
     * @param type     its type
     */
    public void placed(final ServerWorld world, final BlockPosition position, final TypedKey<BlockEntityType> type) {
        if (!tickers.containsKey(type)) {
            return;
        }
        final ChunkPos chunk = position.chunk();
        if (regionizer.isOwnedByCurrentThread(world.key(), chunk)) {
            track(world, position, type);
        } else {
            regionizer.execute(world.key(), chunk, () -> track(world, position, type));
        }
    }

    /**
     * Starts ticking the block entities of a chunk just loaded, read from disk or generated, on the
     * thread of the region owning it.
     *
     * @param world  the world of the chunk
     * @param column the chunk
     */
    public void resume(final ServerWorld world, final ChunkColumn column) {
        if (column.blockEntityCount() == 0 || tickers.isEmpty()) {
            return;
        }
        final List<BlockPosition> positions = new ArrayList<>();
        final List<TypedKey<BlockEntityType>> types = new ArrayList<>();
        for (final BlockEntity blockEntity : column.blockEntities()) {
            if (tickers.containsKey(blockEntity.type())) {
                positions.add(Position.block((column.chunkX() << 4) + blockEntity.localX(), blockEntity.y(),
                        (column.chunkZ() << 4) + blockEntity.localZ()));
                types.add(blockEntity.type());
            }
        }
        if (positions.isEmpty()) {
            return;
        }
        regionizer.execute(world.key(), new ChunkPos(column.chunkX(), column.chunkZ()), () -> {
            for (int index = 0; index < positions.size(); index++) {
                track(world, positions.get(index), types.get(index));
            }
        });
    }

    private void track(final ServerWorld world, final BlockPosition position, final TypedKey<BlockEntityType> type) {
        if (isThere(world, position, type)) {
            regions.computeIfAbsent(RegionKey.of(world.key(), position.chunk()), _ -> new RegionEntries()).add(position, type);
        }
    }

    @Override
    public void tick(final Key world, final int sectionX, final int sectionZ, final long currentTick) {
        if (!ThreadedRegionRegionizer.isGameTick()) {
            return; // frozen: the block entities wait
        }
        final RegionKey key = new RegionKey(world, sectionX, sectionZ);
        final RegionEntries entries = regions.get(key);
        if (entries == null) {
            return;
        }
        final ServerWorld serverWorld = worlds.world(world);
        if (serverWorld == null) {
            regions.remove(key, entries);
            return;
        }
        entries.tick(serverWorld, currentTick);
        if (entries.isEmpty()) {
            regions.remove(key, entries);
        }
    }

    /**
     * {@return {@code true} if a block entity of the type is at the position, in a loaded chunk}
     */
    private static boolean isThere(final ServerWorld world, final BlockPosition position, final TypedKey<BlockEntityType> type) {
        final ChunkColumn column = world.loadedColumn(position.chunkX(), position.chunkZ());
        if (column == null) {
            return false;
        }
        final BlockEntity blockEntity = column.blockEntity(position.blockX() & 15, position.blockY(), position.blockZ() & 15);
        return blockEntity != null && blockEntity.type().equals(type);
    }

    /**
     * Ticks the block entities of one type.
     */
    @FunctionalInterface
    public interface Ticker {

        /**
         * Ticks a block entity, on the thread of the region owning it.
         *
         * @param world       the world of the block entity
         * @param position    where it is
         * @param currentTick the game tick of its region
         */
        void tick(ServerWorld world, BlockPosition position, long currentTick);

        /**
         * Told that a block entity is no longer ticked: its chunk was unloaded or its block replaced.
         *
         * @param world    the world of the block entity
         * @param position where it was
         */
        default void forget(final ServerWorld world, final BlockPosition position) {
        }
    }

    /**
     * One region of a world.
     */
    private record RegionKey(Key world, int sectionX, int sectionZ) {

        static RegionKey of(final Key world, final ChunkPos chunk) {
            return new RegionKey(world, ThreadedRegionRegionizer.chunkToSection(chunk.x()),
                    ThreadedRegionRegionizer.chunkToSection(chunk.z()));
        }
    }

    /**
     * The block entities ticked in one region, in the order they appeared.
     */
    private final class RegionEntries {

        private final Map<BlockPosition, TypedKey<BlockEntityType>> entries = new LinkedHashMap<>();

        void add(final BlockPosition position, final TypedKey<BlockEntityType> type) {
            entries.put(position, type);
        }

        boolean isEmpty() {
            return entries.isEmpty();
        }

        void tick(final ServerWorld world, final long currentTick) {
            final List<Map.Entry<BlockPosition, TypedKey<BlockEntityType>>> due = new ArrayList<>(entries.entrySet());
            for (final Map.Entry<BlockPosition, TypedKey<BlockEntityType>> entry : due) {
                final BlockPosition position = entry.getKey();
                final TypedKey<BlockEntityType> type = entry.getValue();
                final Ticker ticker = tickers.get(type);
                if (ticker == null || !isThere(world, position, type)) {
                    entries.remove(position, type);
                    if (ticker != null) {
                        ticker.forget(world, position);
                    }
                    continue;
                }
                try {
                    ticker.tick(world, position, currentTick);
                } catch (final RuntimeException exception) {
                    LOGGER.error("The block entity {} at {} in {} failed to tick", type.key().asString(), position,
                            world.key().asString(), exception);
                }
            }
        }
    }
}
