package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RedstoneBlockTicks implements RegionTickHandler {

    /**
     * Vanilla's limit of block ticks run in one game tick.
     */
    private static final int MAX_TICKS_PER_TICK = 65536;

    private final FidorialRedstoneEngine engine;
    private final ServerWorldManager worlds;
    private final ThreadedRegionRegionizer regionizer;
    private final Map<RegionKey, RegionTicks> regions = new ConcurrentHashMap<>();

    RedstoneBlockTicks(final FidorialRedstoneEngine engine, final ServerWorldManager worlds, final ThreadedRegionRegionizer regionizer) {
        this.engine = engine;
        this.worlds = worlds;
        this.regionizer = regionizer;
    }

    @Override
    public void tick(final Key world, final int sectionX, final int sectionZ, final long currentTick) {
        if (!ThreadedRegionRegionizer.isGameTick()) {
            return; // frozen: block ticks wait, and so does their clock
        }
        final RegionKey key = new RegionKey(world, sectionX, sectionZ);
        final RegionTicks ticks = regions.get(key);
        if (ticks == null) {
            return;
        }
        final ServerWorld serverWorld = worlds.world(world);
        if (serverWorld == null) {
            ticks.clear();
            regions.remove(key, ticks);
            return;
        }
        ticks.run(serverWorld);
        if (ticks.isEmpty()) {
            ticks.releaseTicket();
            regions.remove(key, ticks);
        }
    }

    void schedule(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block,
                  final int delay, final TickPriority priority) {
        final ChunkPos chunk = position.chunk();
        if (!regionizer.isOwnedByCurrentThread(world.key(), chunk)) {
            regionizer.execute(world.key(), chunk, () -> schedule(world, position, block, delay, priority));
            return;
        }
        regions.computeIfAbsent(RegionKey.of(world.key(), chunk), RegionTicks::new)
                .schedule(position, block, delay, priority, chunk);
    }

    boolean hasScheduledTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        final RegionTicks ticks = ownedTicks(world, position);
        return ticks != null && ticks.pending.contains(new TickKey(position, block));
    }

    boolean willTickThisTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        final RegionTicks ticks = ownedTicks(world, position);
        return ticks != null && ticks.collected.contains(new TickKey(position, block));
    }

    private @Nullable RegionTicks ownedTicks(final ServerWorld world, final BlockPosition position) {
        final ChunkPos chunk = position.chunk();
        if (!regionizer.isOwnedByCurrentThread(world.key(), chunk)) {
            return null;
        }
        return regions.get(RegionKey.of(world.key(), chunk));
    }

    private record TickKey(BlockPosition position, TypedKey<BlockType> block) {
    }

    private record ScheduledTick(BlockPosition position, TypedKey<BlockType> block, long trigger,
                                 TickPriority priority, long order) {

        static final Comparator<ScheduledTick> DRAIN_ORDER = Comparator
                .comparingLong(ScheduledTick::trigger)
                .thenComparing(ScheduledTick::priority)
                .thenComparingLong(ScheduledTick::order);

        TickKey key() {
            return new TickKey(position, block);
        }
    }

    /**
     * The ticks of one region, only ever touched by the thread ticking it.
     */
    private final class RegionTicks {

        private final RegionKey key;
        private final PriorityQueue<ScheduledTick> queue = new PriorityQueue<>(ScheduledTick.DRAIN_ORDER);
        private final Set<TickKey> pending = new HashSet<>();
        private final Set<TickKey> collected = new HashSet<>();
        private long time;
        private long order;
        private @Nullable ChunkPos ticket;

        RegionTicks(final RegionKey key) {
            this.key = key;
        }

        void schedule(final BlockPosition position, final TypedKey<BlockType> block, final int delay,
                      final TickPriority priority, final ChunkPos chunk) {
            if (!pending.add(new TickKey(position, block))) {
                return;
            }
            // like vanilla, due the given number of game ticks after the last run, the current game
            // time; one due right away while ticks run waits for the next run
            final long trigger = time + Math.max(0, delay);
            queue.add(new ScheduledTick(position, block, trigger, priority, order++));
            if (ticket == null) {
                ticket = chunk;
                regionizer.addTicket(key.dimension(), chunk);
            }
        }

        void run(final ServerWorld world) {
            time++;
            final List<ScheduledTick> due = new ArrayList<>();
            ScheduledTick next;
            while (due.size() < MAX_TICKS_PER_TICK && (next = queue.peek()) != null && next.trigger() <= time) {
                queue.poll();
                final TickKey tickKey = next.key();
                pending.remove(tickKey);
                collected.add(tickKey);
                due.add(next);
            }
            if (due.isEmpty()) {
                return;
            }
            try {
                for (final ScheduledTick tick : due) {
                    collected.remove(tick.key());
                    engine.runTick(world, tick.position(), tick.block());
                }
            } finally {
                collected.clear();
            }
        }

        boolean isEmpty() {
            return queue.isEmpty();
        }

        void clear() {
            queue.clear();
            pending.clear();
            collected.clear();
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
