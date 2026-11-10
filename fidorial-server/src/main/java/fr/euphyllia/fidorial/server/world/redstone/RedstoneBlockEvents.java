package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The block events of the engine: queued by a block while it is updated, and run later in the same
 * game tick, once the scheduled ticks have run.
 * Pistons move on them.
 *
 * <p>An event queued while events run still runs on that game tick, which is what lets a piston
 * powered by the block another one just pushed start moving at once.</p>
 */
final class RedstoneBlockEvents implements RegionTickHandler {

    /**
     * How many block events one region runs in one game tick, the rest waiting for the next one: a
     * guard against events queuing one another without end, which vanilla does not have.
     */
    private static final int MAX_EVENTS_PER_TICK = 65536;

    private final FidorialRedstoneEngine engine;
    private final ServerWorldManager worlds;
    private final ThreadedRegionRegionizer regionizer;
    private final Map<RegionKey, RegionEvents> regions = new ConcurrentHashMap<>();

    RedstoneBlockEvents(final FidorialRedstoneEngine engine, final ServerWorldManager worlds, final ThreadedRegionRegionizer regionizer) {
        this.engine = engine;
        this.worlds = worlds;
        this.regionizer = regionizer;
    }

    @Override
    public void tick(final Key world, final int sectionX, final int sectionZ, final long currentTick) {
        if (!ThreadedRegionRegionizer.isGameTick()) {
            return; // frozen: block events wait
        }
        final RegionKey key = new RegionKey(world, sectionX, sectionZ);
        final RegionEvents events = regions.get(key);
        if (events == null) {
            return;
        }
        final ServerWorld serverWorld = worlds.world(world);
        if (serverWorld == null) {
            events.clear();
            regions.remove(key, events);
            return;
        }
        events.run(serverWorld);
        if (events.isEmpty()) {
            events.releaseTicket();
            regions.remove(key, events);
        }
    }

    void add(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block, final int type, final int data) {
        final ChunkPos chunk = position.chunk();
        if (!regionizer.isOwnedByCurrentThread(world.key(), chunk)) {
            // nothing to run in a chunk that is not loaded
            if (world.loadedColumn(chunk.x(), chunk.z()) != null) {
                regionizer.execute(world.key(), chunk, () -> add(world, position, block, type, data));
            }
            return;
        }
        regions.computeIfAbsent(RegionKey.of(world.key(), chunk), RegionEvents::new)
                .add(new BlockEvent(position, block, type, data), chunk);
    }

    private record BlockEvent(BlockPosition position, TypedKey<BlockType> block, int type, int data) {
    }

    /**
     * The block events of one region, only ever touched by the thread ticking it.
     */
    private final class RegionEvents {

        private final RegionKey key;
        /**
         * In the order they were queued, each once: an event equal to one that waits is not queued again.
         */
        private final LinkedHashSet<BlockEvent> queue = new LinkedHashSet<>();
        private @Nullable ChunkPos ticket;

        RegionEvents(final RegionKey key) {
            this.key = key;
        }

        void add(final BlockEvent event, final ChunkPos chunk) {
            if (queue.add(event) && ticket == null) {
                // keeps the region ticking until the event ran
                ticket = chunk;
                regionizer.addTicket(key.dimension(), chunk);
            }
        }

        void run(final ServerWorld world) {
            for (int ran = 0; ran < MAX_EVENTS_PER_TICK && !queue.isEmpty(); ran++) {
                final Iterator<BlockEvent> first = queue.iterator();
                final BlockEvent event = first.next();
                first.remove();
                engine.runBlockEvent(world, event.position(), event.block(), event.type(), event.data());
            }
        }

        boolean isEmpty() {
            return queue.isEmpty();
        }

        void clear() {
            queue.clear();
            releaseTicket();
        }

        void releaseTicket() {
            final ChunkPos held = ticket;
            if (held != null) {
                ticket = null;
                regionizer.removeTicket(key.dimension(), held);
            }
        }
    }
}
