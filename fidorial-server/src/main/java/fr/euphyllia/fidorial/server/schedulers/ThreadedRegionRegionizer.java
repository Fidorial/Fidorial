package fr.euphyllia.fidorial.server.schedulers;

import ca.spottedleaf.concurrentutil.list.COWArrayList;
import ca.spottedleaf.concurrentutil.numa.OSNuma;
import ca.spottedleaf.concurrentutil.scheduler.StealingScheduledThreadPool;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.euphyllia.fidorial.server.schedulers.tick.TickChange;
import fr.euphyllia.fidorial.server.schedulers.tick.TickSettings;
import fr.euphyllia.fidorial.server.schedulers.tick.TickState;
import fr.fidorial.event.Event;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.scheduler.RegionTps;
import fr.fidorial.scheduler.RegionizedScheduler;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ticks the worlds on a pool of threads, cut into regions that merge and split as they come close
 * and drift apart again: see {@link WorldRegions}.
 *
 * <p>A section is occupied by the chunks loaded in it and by the tickets taken on it
 * ({@link #addTicket}); tasks sent to a place with no section get one for as long as they wait.</p>
 */
public final class ThreadedRegionRegionizer implements RegionizedScheduler {

    /**
     * Default section shift: sections of {@code 1 << 5} = 32×32 chunks.
     */
    public static final int DEFAULT_SECTION_SHIFT = 5;

    /**
     * Largest accepted section shift: sections of {@code 1 << 10} = 1024×1024 chunks.
     */
    public static final int MAX_SECTION_SHIFT = 10;

    /**
     * Default margin of empty sections around the occupied ones.
     */
    public static final int DEFAULT_BUFFER_RADIUS = 1;

    /**
     * Largest accepted margin.
     */
    public static final int MAX_BUFFER_RADIUS = 8;

    /**
     * Sections are {@code 1 << sectionShift} chunks wide. Set once from {@code threading.region-section-shift}
     * when the regionizer is built, before any region thread or ticket exists.
     */
    private static volatile int sectionShift = DEFAULT_SECTION_SHIFT;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ThreadedRegionRegionizer.class);

    private static final long STEAL_THRESHOLD_NS = TimeUnit.MILLISECONDS.toNanos(5L);

    private static final long TASK_TIME_SLICE_NS = TimeUnit.MILLISECONDS.toNanos(15L);

    private final StealingScheduledThreadPool scheduler;
    private final int bufferRadius;
    private final @Nullable SimpleEventBus events;
    private final Map<Key, WorldRegions> worlds = new ConcurrentHashMap<>();
    private final COWArrayList<RegionTickHandler> tickHandlers = new COWArrayList<>(RegionTickHandler.class);
    private final COWArrayList<RegionWideTickHandler> regionWideTickHandlers = new COWArrayList<>(RegionWideTickHandler.class);
    private final COWArrayList<RegionTickProfiler> tickProfilers = new COWArrayList<>(RegionTickProfiler.class);

    private volatile boolean shutdown;

    private final Object tickChangeLock = new Object();
    private volatile TickSettings tickDefaults = TickSettings.DEFAULT; // written under tickChangeLock
    private final List<TickChange> tickChanges = new ArrayList<>(); // guarded by tickChangeLock
    private volatile int tickChangeCount;

    @SuppressWarnings("StaticAssignmentInConstructor") // set once from the config, before any region exists
    public ThreadedRegionRegionizer(final int workerThreads, final int sectionShift, final int bufferRadius,
                                    final @Nullable SimpleEventBus events) {
        if (sectionShift < 0 || sectionShift > MAX_SECTION_SHIFT) {
            throw new IllegalArgumentException("Region section shift must be between 0 and " + MAX_SECTION_SHIFT
                    + ", got " + sectionShift);
        }
        if (bufferRadius < 0 || bufferRadius > MAX_BUFFER_RADIUS) {
            throw new IllegalArgumentException("Region buffer radius must be between 0 and " + MAX_BUFFER_RADIUS
                    + ", got " + bufferRadius);
        }
        ThreadedRegionRegionizer.sectionShift = sectionShift;
        this.bufferRadius = bufferRadius;
        this.events = events;

        final AtomicInteger id = new AtomicInteger();
        this.scheduler = new StealingScheduledThreadPool(
                r -> new RegionTickThread(r, "fidorial-region-worker-" + id.incrementAndGet()),
                OSNuma.getNativeInstance());
        this.scheduler.setThreadAllocation(
                allocateThreads(workerThreads), STEAL_THRESHOLD_NS, TASK_TIME_SLICE_NS);

        final int size = sectionSize();
        LOGGER.info("Region pool started with {} workers, sections of {}x{} chunks (section shift {}), {}",
                workerThreads, size, size, sectionShift,
                bufferRadius == 0 ? "regions never merged" : "regions merged within " + bufferRadius + " section(s)");
    }

    /**
     * {@return the section shift: a section is {@code 1 << sectionShift()} chunks wide}
     */
    public static int sectionShift() {
        return sectionShift;
    }

    /**
     * {@return the width of a section, in chunks}
     */
    public static int sectionSize() {
        return 1 << sectionShift;
    }

    /**
     * {@return the section coordinate of a chunk coordinate}
     *
     * @param chunkCoord a chunk X or Z coordinate
     */
    public static int chunkToSection(final int chunkCoord) {
        return chunkCoord >> sectionShift;
    }

    /**
     * {@return the margin of sections owned around the occupied ones; {@code 0} when regions never merge}
     */
    public int bufferRadius() {
        return bufferRadius;
    }

    private static Int2IntMap allocateThreads(final int workerThreads) {
        final int threads = Math.max(1, workerThreads);
        final int nodes = Math.max(1, OSNuma.getNativeInstance().getTotalNumaNodes());
        final Int2IntMap allocation = new Int2IntOpenHashMap(nodes);
        for (int i = 0; i < threads; i++) {
            allocation.mergeInt(i % nodes, 1, Integer::sum);
        }
        return allocation;
    }

    private WorldRegions world(final Key world) {
        return worlds.computeIfAbsent(world, key -> new WorldRegions(key, this, bufferRadius));
    }

    @Override
    public boolean execute(final Key worldName, final ChunkPos pos, final Runnable task) {
        return executeDelayed(worldName, pos, task, 0L);
    }

    @Override
    public boolean executeDelayed(final Key worldName, final ChunkPos pos, final Runnable task, final long delayTicks) {
        if (shutdown) {
            return false;
        }
        world(worldName).offer(chunkToSection(pos.x()), chunkToSection(pos.z()), task, Math.max(0L, delayTicks));
        return true;
    }

    @Override
    public boolean isOwnedByCurrentThread(final Key worldName, final ChunkPos pos) {
        return RegionTickThread.owns(worldName, pos);
    }

    @Override
    public List<RegionTpsSnapshot> tpsSnapshots() {
        final List<RegionTpsSnapshot> out = new ArrayList<>();
        for (final WorldRegions world : worlds.values()) {
            for (final TickRegion region : world.regions) {
                final RegionTpsSnapshot snapshot = region.tpsSnapshot();
                if (snapshot != null) {
                    out.add(snapshot);
                }
            }
        }
        out.sort(Comparator.comparingDouble(RegionTpsSnapshot::tps));
        return out;
    }

    /**
     * Occupies the section of a chunk until {@link #removeTicket} is called as many times.
     */
    public void addTicket(final Key worldName, final ChunkPos pos) {
        if (shutdown) {
            return;
        }
        world(worldName).addHold(chunkToSection(pos.x()), chunkToSection(pos.z()));
    }

    public void removeTicket(final Key worldName, final ChunkPos pos) {
        final WorldRegions world = worlds.get(worldName);
        if (world != null) {
            world.removeHold(chunkToSection(pos.x()), chunkToSection(pos.z()));
        }
    }

    public void moveTicket(final Key worldName, final ChunkPos from, final ChunkPos to) {
        if (chunkToSection(from.x()) == chunkToSection(to.x()) && chunkToSection(from.z()) == chunkToSection(to.z())) {
            return;
        }
        addTicket(worldName, to);
        removeTicket(worldName, from);
    }

    /**
     * {@return whether the current thread is ticking a region whose tick runs game logic}
     * <p>
     * {@code true} outside region threads.
     */
    public static boolean isGameTick() {
        if (!(Thread.currentThread() instanceof final RegionTickThread self)) {
            return true;
        }
        final TickRegion region = self.currentRegion;
        return region == null || region.gameTick;
    }

    public TickSettings tickDefaults() {
        return tickDefaults;
    }

    /**
     * Applies a tick change to every region, each on its own thread at its next tick, and to the
     * regions started after it. Changes are serialised so every region sees them in the same order.
     */
    public void changeTicking(final TickChange change) {
        synchronized (tickChangeLock) {
            tickDefaults = change.applyTo(tickDefaults);
            tickChanges.add(change);
            tickChangeCount = tickChanges.size();
        }
    }

    /**
     * {@return the number of changes applied once {@code state} caught up with the changes made since
     * {@code applied}}
     */
    int applyTickChanges(final TickState state, final int applied) {
        if (applied == tickChangeCount) {
            return applied;
        }
        synchronized (tickChangeLock) {
            for (int i = applied; i < tickChanges.size(); i++) {
                state.apply(tickChanges.get(i));
            }
            return tickChanges.size();
        }
    }

    TickRegion newRegion(final WorldRegions world) {
        synchronized (tickChangeLock) {
            return new TickRegion(world, new TickState(tickDefaults), tickChanges.size());
        }
    }

    public void registerTickHandler(final RegionTickHandler handler) {
        tickHandlers.add(handler);
    }

    RegionTickHandler[] tickHandlers() {
        return tickHandlers.getArray();
    }

    /**
     * Registers work run once per region tick over all its sections, before the section handlers.
     */
    public void registerRegionWideTickHandler(final RegionWideTickHandler handler) {
        regionWideTickHandlers.add(handler);
    }

    RegionWideTickHandler[] regionWideTickHandlers() {
        return regionWideTickHandlers.getArray();
    }

    void schedule(final TickRegion region) {
        if (shutdown) {
            return;
        }
        region.initScheduledStart(System.nanoTime());
        scheduler.schedule(region);
    }

    void cancel(final TickRegion region) {
        scheduler.cancel(region);
    }

    void notifyTasks(final TickRegion region) {
        scheduler.notifyTasks(region);
    }

    boolean listens(final Class<? extends Event> type) {
        final SimpleEventBus bus = events;
        return bus != null && bus.hasListeners(type);
    }

    void post(final List<Event> posted) {
        final SimpleEventBus bus = events;
        if (bus == null) {
            return;
        }
        for (final Event event : posted) {
            try {
                bus.post(event);
            } catch (final RuntimeException ex) {
                LOGGER.error("Failed to post {}", event, ex);
            }
        }
    }

    public @Nullable String describeOwningThread(final Key worldName, final ChunkPos pos) {
        final WorldRegions world = worlds.get(worldName);
        final RegionSection section = world == null ? null : world.sectionOf(pos);
        if (section == null) {
            return null;
        }
        final TickRegion region = section.region;
        final Thread owner = region.tickingThread;
        return owner == null
                ? "region " + region + " is idle (no thread currently ticking it)"
                : "[thread=" + owner.getName() + ",class=" + owner.getClass().getName() + ",region=" + region + "]";
    }

    public void addTickProfiler(final RegionTickProfiler profiler) {
        tickProfilers.add(profiler);
    }

    public void removeTickProfiler(final RegionTickProfiler profiler) {
        tickProfilers.remove(profiler);
    }

    RegionTickProfiler[] tickProfilers() {
        return tickProfilers.getArray();
    }

    public void shutdown() {
        shutdown = true;
        scheduler.halt();
        if (!scheduler.join(TimeUnit.SECONDS.toMillis(5L))) {
            LOGGER.warn("Region workers did not stop within 5s");
        }
        worlds.clear();
        LOGGER.info("Region workers stopped");
    }

    /**
     * A region as {@code /tps} and the API see it.
     */
    public record RegionTpsSnapshot(
            RegionSnapshot region,
            double tps,
            double msptAvg,
            double cpuPercent,
            int queuedTasks,
            int tickets)
            implements RegionTps {

        @Override
        public long id() {
            return region.id();
        }

        @Override
        public Key world() {
            return region.world();
        }

        @Override
        public int sectionCount() {
            return region.sectionCount();
        }

        @Override
        public boolean contains(final ChunkPos chunk) {
            return region.contains(chunk);
        }

        @Override
        public ChunkPos centerChunk() {
            return region.centerChunk();
        }
    }
}
