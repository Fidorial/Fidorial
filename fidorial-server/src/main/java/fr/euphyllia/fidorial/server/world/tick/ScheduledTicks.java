package fr.euphyllia.fidorial.server.world.tick;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The ticks scheduled on the blocks, or the fluids, of one world: vanilla's {@code LevelTicks}.
 *
 * <ul>
 *   <li>Ticks are kept per chunk, saved with it ({@code block_ticks} or {@code fluid_ticks}, with the
 *   delay left) and scheduled again when it loads.</li>
 *   <li>They only run in {@linkplain ServerWorld#isBlockTicking block ticking} chunks; elsewhere they
 *   wait, and run once the chunk ticks again.</li>
 *   <li>A region runs the ticks due in all its chunks together, ordered as vanilla orders them: by
 *   due time, then priority, then scheduling order.</li>
 * </ul>
 *
 * <p>Time is counted per section of the regionizer, by the region ticking it, so that it follows the
 * section through merges and splits; the delays saved and the order of the ticks only compare times
 * relatively, so sections need not agree.</p>
 *
 * @param <T> what is ticked: a block type, a fluid type
 */
public final class ScheduledTicks<T> {

    /**
     * Vanilla's limit of ticks run in one game tick.
     */
    public static final int MAX_TICKS_PER_TICK = 65536;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ScheduledTicks.class);

    private final ServerWorld world;
    private final Function<T, String> idOf;
    private final Function<String, @Nullable T> fromId;
    private final ConcurrentHashMap<Long, SectionTicks> sections = new ConcurrentHashMap<>();
    private final AtomicLong subTickOrder = new AtomicLong();

    /**
     * @param idOf   the id saved for a type, such as {@code minecraft:repeater}
     * @param fromId the type of a saved id, {@code null} if it is unknown
     */
    public ScheduledTicks(final ServerWorld world, final Function<T, String> idOf, final Function<String, @Nullable T> fromId) {
        this.world = world;
        this.idOf = idOf;
        this.fromId = fromId;
    }

    private static long sectionKeyOf(final int chunkX, final int chunkZ) {
        return ChunkPos.chunkKey(ThreadedRegionRegionizer.chunkToSection(chunkX), ThreadedRegionRegionizer.chunkToSection(chunkZ));
    }

    /**
     * Schedules a tick, unless one is pending for the same block and type or the chunk is not loaded.
     *
     * @return {@code true} if the tick was scheduled
     */
    public boolean schedule(final BlockPosition position, final T type, final int delay, final TickPriority priority) {
        final int chunkX = position.chunkX();
        final int chunkZ = position.chunkZ();
        if (world.loadedColumn(chunkX, chunkZ) == null) {
            return false; // like vanilla, nothing is scheduled where nothing is loaded
        }
        if (!add(chunkX, chunkZ, position, type, Math.max(0, delay), priority)) {
            return false;
        }
        world.markDirty(chunkX, chunkZ);
        return true;
    }

    private boolean add(final int chunkX, final int chunkZ, final BlockPosition position, final T type, final int delay,
                        final TickPriority priority) {
        final boolean[] added = new boolean[1];
        // under the lock of the section in the map, so that it cannot be dropped meanwhile
        sections.compute(sectionKeyOf(chunkX, chunkZ), (_, existing) -> {
            final SectionTicks section = existing != null ? existing : new SectionTicks();
            final ChunkTicks<T> chunk = section.chunk(chunkX, chunkZ);
            added[0] = chunk.add(new Scheduled<>(position, type, section.time + delay, priority, subTickOrder.getAndIncrement()));
            return section;
        });
        return added[0];
    }

    public boolean hasScheduledTick(final BlockPosition position, final T type) {
        final ChunkTicks<T> chunk = chunk(position.chunkX(), position.chunkZ());
        return chunk != null && chunk.isPending(new TickKey<>(position, type));
    }

    public boolean willTickThisTick(final BlockPosition position, final T type) {
        final ChunkTicks<T> chunk = chunk(position.chunkX(), position.chunkZ());
        return chunk != null && chunk.isCollected(new TickKey<>(position, type));
    }

    private @Nullable ChunkTicks<T> chunk(final int chunkX, final int chunkZ) {
        final SectionTicks section = sections.get(sectionKeyOf(chunkX, chunkZ));
        return section == null ? null : section.chunk(ChunkPos.chunkKey(chunkX, chunkZ));
    }

    /**
     * Counts one more tick in the sections of a region and runs the ticks due in its block ticking
     * chunks. On the thread ticking the region, while it runs game logic.
     *
     * @param sectionKeys the {@linkplain ChunkPos#chunkKey(int, int) keys} of the section coordinates
     * @param runner      runs one tick
     */
    public void tickRegion(final long[] sectionKeys, final BiConsumer<BlockPosition, T> runner) {
        if (!ThreadedRegionRegionizer.isGameTick()) {
            return; // frozen: the ticks wait, and so does their clock
        }
        List<Due<T>> due = null;
        for (final long sectionKey : sectionKeys) {
            final SectionTicks section = sections.get(sectionKey);
            if (section == null) {
                continue;
            }
            final long time = ++section.time;
            for (final ChunkTicks<T> chunk : section.<T>chunks()) {
                if (!world.isBlockTicking(chunk.chunkX, chunk.chunkZ)) {
                    continue; // waits until the chunk ticks
                }
                if (due == null) {
                    due = new ArrayList<>();
                }
                if (chunk.collectDue(time, due)) {
                    world.markDirty(chunk.chunkX, chunk.chunkZ); // what is saved no longer holds these ticks
                }
            }
        }
        if (due == null || due.isEmpty()) {
            return;
        }
        due.sort(Due.comparator());
        // past the limit, the latest ticks go back to wait for the next game tick
        for (int i = MAX_TICKS_PER_TICK; i < due.size(); i++) {
            final Due<T> late = due.get(i);
            late.chunk.putBack(late.tick);
        }
        final int count = Math.min(due.size(), MAX_TICKS_PER_TICK);
        try {
            for (int i = 0; i < count; i++) {
                final Due<T> next = due.get(i);
                next.chunk.uncollect(next.tick);
                try {
                    runner.accept(next.tick.position(), next.tick.type());
                } catch (final RuntimeException ex) {
                    LOGGER.error("Failed to run the tick of {} at {} in {}", next.tick.type(), next.tick.position(), world.key(), ex);
                }
            }
        } finally {
            for (int i = 0; i < count; i++) {
                due.get(i).chunk.clearCollected();
            }
        }
    }

    /**
     * {@return the ticks pending in a chunk, as vanilla saves them}
     */
    public ListBinaryTag save(final int chunkX, final int chunkZ) {
        final SectionTicks section = sections.get(sectionKeyOf(chunkX, chunkZ));
        final ChunkTicks<T> chunk = section == null ? null : section.chunk(ChunkPos.chunkKey(chunkX, chunkZ));
        if (chunk == null) {
            return ListBinaryTag.empty();
        }
        final long time = section.time;
        final ListBinaryTag.Builder<BinaryTag> list = ListBinaryTag.builder();
        for (final Scheduled<T> tick : chunk.snapshot()) {
            list.add(CompoundBinaryTag.builder()
                    .putString("i", idOf.apply(tick.type()))
                    .putInt("x", tick.position().blockX())
                    .putInt("y", tick.position().blockY())
                    .putInt("z", tick.position().blockZ())
                    .putInt("t", (int) Math.max(0L, Math.min(Integer.MAX_VALUE, tick.trigger() - time)))
                    .putInt("p", tick.priority().ordinal() - TickPriority.NORMAL.ordinal())
                    .build());
        }
        return list.build();
    }

    /**
     * Schedules again the ticks saved with a chunk, as it loads.
     */
    public void load(final int chunkX, final int chunkZ, final ListBinaryTag saved) {
        final TickPriority[] priorities = TickPriority.values();
        for (final BinaryTag entry : saved) {
            if (!(entry instanceof final CompoundBinaryTag tick)) {
                continue;
            }
            final T type = fromId.apply(tick.getString("i"));
            if (type == null) {
                continue;
            }
            final BlockPosition position = Position.block(tick.getInt("x"), tick.getInt("y"), tick.getInt("z"));
            if (position.chunkX() != chunkX || position.chunkZ() != chunkZ) {
                continue;
            }
            final int priority = Math.clamp(tick.getInt("p") + TickPriority.NORMAL.ordinal(), 0, priorities.length - 1);
            add(chunkX, chunkZ, position, type, Math.max(0, tick.getInt("t")), priorities[priority]);
        }
    }

    /**
     * Drops the ticks of a chunk being unloaded, once saved.
     */
    public void forget(final int chunkX, final int chunkZ) {
        sections.computeIfPresent(sectionKeyOf(chunkX, chunkZ), (_, section) -> {
            section.remove(ChunkPos.chunkKey(chunkX, chunkZ));
            return section.isEmpty() ? null : section;
        });
    }

    /**
     * The ticks of the chunks of one section, and the time of the section.
     */
    private static final class SectionTicks {

        /**
         * Game ticks of the section; only written by the thread ticking it.
         */
        volatile long time;
        private final ConcurrentHashMap<Long, ChunkTicks<?>> chunks = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        <T> ChunkTicks<T> chunk(final int chunkX, final int chunkZ) {
            return (ChunkTicks<T>) chunks.computeIfAbsent(ChunkPos.chunkKey(chunkX, chunkZ), _ -> new ChunkTicks<>(chunkX, chunkZ));
        }

        @SuppressWarnings("unchecked")
        <T> @Nullable ChunkTicks<T> chunk(final long chunkKey) {
            return (ChunkTicks<T>) chunks.get(chunkKey);
        }

        @SuppressWarnings("unchecked")
        <T> Iterable<ChunkTicks<T>> chunks() {
            return (Iterable<ChunkTicks<T>>) (Iterable<?>) chunks.values();
        }

        void remove(final long chunkKey) {
            chunks.remove(chunkKey);
        }

        boolean isEmpty() {
            return chunks.isEmpty();
        }
    }

    /**
     * The ticks of one chunk, vanilla's {@code LevelChunkTicks}. Its monitor guards it: the thread
     * ticking its region runs them, while a saving thread may read them.
     */
    private static final class ChunkTicks<T> {

        final int chunkX;
        final int chunkZ;
        private final PriorityQueue<Scheduled<T>> queue = new PriorityQueue<>(Scheduled.comparator());
        private final Set<TickKey<T>> pending = new HashSet<>();
        private final Set<TickKey<T>> collected = new HashSet<>();

        ChunkTicks(final int chunkX, final int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }

        synchronized boolean add(final Scheduled<T> tick) {
            if (!pending.add(tick.key())) {
                return false;
            }
            queue.add(tick);
            return true;
        }

        synchronized boolean isPending(final TickKey<T> key) {
            return pending.contains(key);
        }

        synchronized boolean isCollected(final TickKey<T> key) {
            return collected.contains(key);
        }

        /**
         * {@return whether a tick was due}
         */
        synchronized boolean collectDue(final long time, final List<Due<T>> into) {
            boolean any = false;
            Scheduled<T> next;
            while ((next = queue.peek()) != null && next.trigger() <= time) {
                queue.poll();
                pending.remove(next.key());
                collected.add(next.key());
                into.add(new Due<>(this, next, next.trigger() - time));
                any = true;
            }
            return any;
        }

        synchronized void putBack(final Scheduled<T> tick) {
            collected.remove(tick.key());
            if (pending.add(tick.key())) {
                queue.add(tick);
            }
        }

        synchronized void uncollect(final Scheduled<T> tick) {
            collected.remove(tick.key());
        }

        synchronized void clearCollected() {
            collected.clear();
        }

        synchronized List<Scheduled<T>> snapshot() {
            return new ArrayList<>(queue);
        }
    }

    private record TickKey<T>(BlockPosition position, T type) {
    }

    /**
     * @param trigger the time of its section the tick is due at
     * @param order   the order it was scheduled in, among the ticks of the world
     */
    private record Scheduled<T>(BlockPosition position, T type, long trigger, TickPriority priority, long order) {

        static <T> Comparator<Scheduled<T>> comparator() {
            return Comparator.<Scheduled<T>>comparingLong(Scheduled::trigger)
                    .thenComparing(Scheduled::priority)
                    .thenComparingLong(Scheduled::order);
        }

        TickKey<T> key() {
            return new TickKey<>(position, type);
        }
    }

    /**
     * A tick due this game tick.
     *
     * @param lateness how many game ticks ago it was due, negated: what orders ticks of sections
     *                 whose times differ
     */
    private record Due<T>(ChunkTicks<T> chunk, Scheduled<T> tick, long lateness) {

        static <T> Comparator<Due<T>> comparator() {
            return Comparator.<Due<T>>comparingLong(Due::lateness)
                    .thenComparing(due -> due.tick().priority())
                    .thenComparingLong(due -> due.tick().order());
        }
    }
}
