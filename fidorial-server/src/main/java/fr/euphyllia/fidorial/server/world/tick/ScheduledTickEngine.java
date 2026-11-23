package fr.euphyllia.fidorial.server.world.tick;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The ticks blocks and fluids schedule, in every world, kept by region.
 *
 * <p>On every game tick of a region, its block ticks due run, then its fluid ticks due. The ticks of
 * a chunk are saved with it and scheduled again once it is loaded: {@link #pendingIn} gives them to
 * the threads saving chunks, {@link #resume} takes back those a chunk was read with.</p>
 */
public final class ScheduledTickEngine implements RegionTickHandler {

    /**
     * The most ticks of one kind run in one game tick of a region.
     */
    private static final int MAX_TICKS_PER_TICK = 65536;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ScheduledTickEngine.class);

    private final ServerWorldManager worlds;
    private final ThreadedRegionRegionizer regionizer;
    private final Map<RegionKey, RegionTicks> regions = new ConcurrentHashMap<>();
    private volatile Runner<TypedKey<BlockType>> blockRunner = (_, _, _) -> {};
    private volatile Runner<FluidType> fluidRunner = (_, _, _) -> {};

    public ScheduledTickEngine(final ServerWorldManager worlds, final ThreadedRegionRegionizer regionizer) {
        this.worlds = worlds;
        this.regionizer = regionizer;
    }

    /**
     * Sets what a due block tick runs: the behaviours of the block.
     *
     * @param runner called on the thread owning the block
     */
    public void setBlockRunner(final Runner<TypedKey<BlockType>> runner) {
        this.blockRunner = runner;
    }

    /**
     * Sets what a due fluid tick runs: the simulation of the fluid.
     *
     * @param runner called on the thread owning the block
     */
    public void setFluidRunner(final Runner<FluidType> runner) {
        this.fluidRunner = runner;
    }

    @Override
    public void tick(final Key world, final int sectionX, final int sectionZ, final long currentTick) {
        if (!ThreadedRegionRegionizer.isGameTick()) {
            return; // frozen: ticks wait, and so does their clock
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

    /**
     * Schedules a block tick, on the thread owning the block.
     */
    public void scheduleBlockTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block,
                                  final int delay, final TickPriority priority) {
        schedule(world, position, delay, ticks -> ticks.scheduleBlock(position, block, delay, priority));
    }

    /**
     * Schedules a fluid tick, on the thread owning the block.
     */
    public void scheduleFluidTick(final ServerWorld world, final BlockPosition position, final FluidType fluid, final int delay) {
        schedule(world, position, delay, ticks -> ticks.scheduleFluid(position, fluid, delay));
    }

    private void schedule(final ServerWorld world, final BlockPosition position, final int delay, final Scheduling scheduling) {
        final ChunkPos chunk = position.chunk();
        if (!regionizer.isOwnedByCurrentThread(world.key(), chunk)) {
            regionizer.execute(world.key(), chunk, () -> schedule(world, position, delay, scheduling));
            return;
        }
        if (scheduling.schedule(regions.computeIfAbsent(RegionKey.of(world.key(), chunk), RegionTicks::new))) {
            // like any change to a chunk, it is to be saved
            world.markDirty(chunk.x(), chunk.z());
        }
    }

    public boolean hasBlockTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        final RegionTicks ticks = owned(world, position);
        return ticks != null && ticks.blocks.has(position, block);
    }

    public boolean willBlockTickThisTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        final RegionTicks ticks = owned(world, position);
        return ticks != null && ticks.blocks.willRunThisTick(position, block);
    }

    public boolean hasFluidTick(final ServerWorld world, final BlockPosition position, final FluidType fluid) {
        final RegionTicks ticks = owned(world, position);
        return ticks != null && ticks.fluids.has(position, fluid);
    }

    private @Nullable RegionTicks owned(final ServerWorld world, final BlockPosition position) {
        final ChunkPos chunk = position.chunk();
        if (!regionizer.isOwnedByCurrentThread(world.key(), chunk)) {
            return null;
        }
        return regions.get(RegionKey.of(world.key(), chunk));
    }

    /**
     * {@return the ticks pending for the blocks and fluids of a chunk}, to save with it. Safe to
     * call from any thread.
     *
     * @param world  the world of the chunk
     * @param chunkX the chunk coordinate on the x axis
     * @param chunkZ the chunk coordinate on the z axis
     */
    public ChunkTicks pendingIn(final ServerWorld world, final int chunkX, final int chunkZ) {
        final ChunkPos chunk = new ChunkPos(chunkX, chunkZ);
        final RegionTicks ticks = regions.get(RegionKey.of(world.key(), chunk));
        if (ticks == null) {
            return ChunkTicks.NONE;
        }
        final long now = ticks.time.get();
        return new ChunkTicks(ticks.blocks.pendingIn(chunk.key(), now), ticks.fluids.pendingIn(chunk.key(), now));
    }

    /**
     * Schedules again, on the thread of its region, the ticks a chunk was read with: they are due as
     * many game ticks after it was loaded as they were still due in when it was saved.
     *
     * @param world  the world of the chunk
     * @param column the chunk just read from disk
     */
    public void resume(final ServerWorld world, final ChunkColumn column) {
        if (column.pendingTicks().isEmpty()) {
            return;
        }
        regionizer.execute(world.key(), new ChunkPos(column.chunkX(), column.chunkZ()), () -> {
            final ChunkTicks read = column.takePendingTicks();
            for (final PendingTick<TypedKey<BlockType>> tick : read.blocks()) {
                scheduleBlockTick(world, tick.position(), tick.type(), tick.delay(), tick.priority());
            }
            for (final PendingTick<FluidType> tick : read.fluids()) {
                scheduleFluidTick(world, tick.position(), tick.type(), tick.delay());
            }
        });
    }

    /**
     * Runs a due tick.
     *
     * @param <T> a block type key, or a fluid
     */
    @FunctionalInterface
    public interface Runner<T> {
        void run(ServerWorld world, BlockPosition position, T type);
    }

    @FunctionalInterface
    private interface Scheduling {
        boolean schedule(RegionTicks ticks);
    }

    private record RegionKey(Key dimension, int sectionX, int sectionZ) {

        static RegionKey of(final Key dimension, final ChunkPos chunk) {
            return new RegionKey(dimension, ThreadedRegionRegionizer.chunkToSection(chunk.x()),
                    ThreadedRegionRegionizer.chunkToSection(chunk.z()));
        }
    }

    /**
     * The ticks of one region, only ever touched by the thread ticking it, but for what saving
     * chunks reads.
     */
    private final class RegionTicks {

        private final RegionKey key;
        private final TickQueue<TypedKey<BlockType>> blocks = new TickQueue<>();
        private final TickQueue<FluidType> fluids = new TickQueue<>();
        /**
         * The game time of the region, read as well by the threads saving chunks.
         */
        private final AtomicLong time = new AtomicLong();
        private long order;
        private @Nullable ChunkPos ticket;

        RegionTicks(final RegionKey key) {
            this.key = key;
        }

        boolean scheduleBlock(final BlockPosition position, final TypedKey<BlockType> block, final int delay,
                              final TickPriority priority) {
            return added(blocks.schedule(position, block, trigger(delay), priority, order++), position);
        }

        boolean scheduleFluid(final BlockPosition position, final FluidType fluid, final int delay) {
            return added(fluids.schedule(position, fluid, trigger(delay), TickPriority.NORMAL, order++), position);
        }

        /**
         * {@return the game time a tick is due at}: as many game ticks after the last run, the
         * current game time; one due right away while ticks run waits for the next run
         */
        private long trigger(final int delay) {
            return time.get() + Math.max(0, delay);
        }

        private boolean added(final boolean added, final BlockPosition position) {
            if (added && ticket == null) {
                final ChunkPos chunk = position.chunk();
                ticket = chunk;
                regionizer.addTicket(key.dimension(), chunk);
            }
            return added;
        }

        void run(final ServerWorld world) {
            final long now = time.incrementAndGet();
            run(world, blocks, now, blockRunner);
            run(world, fluids, now, fluidRunner);
        }

        private <T> void run(final ServerWorld world, final TickQueue<T> queue, final long now, final Runner<T> runner) {
            final List<TickQueue.Scheduled<T>> due = queue.takeDue(now, MAX_TICKS_PER_TICK);
            if (due.isEmpty()) {
                return;
            }
            try {
                for (final TickQueue.Scheduled<T> tick : due) {
                    queue.running(tick);
                    try {
                        runner.run(world, tick.position(), tick.type());
                    } catch (final RuntimeException exception) {
                        LOGGER.error("The tick of {} failed at {} in {}", tick.type(), tick.position(), world.key(), exception);
                    }
                }
            } finally {
                queue.ranAll();
            }
        }

        boolean isEmpty() {
            return blocks.isEmpty() && fluids.isEmpty();
        }

        void clear() {
            blocks.clear();
            fluids.clear();
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
