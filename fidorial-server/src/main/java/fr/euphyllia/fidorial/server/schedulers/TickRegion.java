package fr.euphyllia.fidorial.server.schedulers;

import ca.spottedleaf.common.time.TickData;
import ca.spottedleaf.common.time.TickTime;
import ca.spottedleaf.common.util.TimeUtil;
import ca.spottedleaf.concurrentutil.scheduler.SchedulableTick;
import fr.euphyllia.fidorial.server.schedulers.tick.TickState;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.ChunkPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * A group of {@linkplain RegionSection sections} of one world, ticked together by one thread at a
 * time. Its shape is managed by {@link WorldRegions}.
 */
public final class TickRegion extends SchedulableTick {

    static final int TRANSIENT = 0; // waiting to be merged into a ticking region, not scheduled
    static final int READY = 1;
    static final int TICKING = 2;
    static final int DEAD = 3;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(TickRegion.class);

    private static final RegionSection[] NO_SECTIONS = new RegionSection[0];
    private static final long[] NO_KEYS = new long[0];

    private static final long TICK_INTERVAL_NS = TimeUnit.MILLISECONDS.toNanos(50L);
    private static final long TPS_WINDOW_NS = TimeUnit.SECONDS.toNanos(5L);

    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();
    private static final boolean CPU_TIME_SUPPORTED = cpuTimeSupported();

    final WorldRegions world;

    // guarded by the world lock
    int state = TRANSIENT;
    final Long2ObjectOpenHashMap<RegionSection> sections = new Long2ObjectOpenHashMap<>();
    final ReferenceOpenHashSet<RegionSection> deadSections = new ReferenceOpenHashSet<>();
    /** Ticking regions this one merges into once they finish their tick. */
    final ReferenceOpenHashSet<TickRegion> mergeIntoLater = new ReferenceOpenHashSet<>();
    /** Regions merging into this one once it finishes its tick. */
    final ReferenceOpenHashSet<TickRegion> expectingMergeFrom = new ReferenceOpenHashSet<>();

    /** The sections, for the ticking thread and {@link #hasTasks()}. Replaced under the world lock. */
    private volatile RegionSection[] owned = NO_SECTIONS;
    /** The keys of {@link #owned}, for the region-wide handlers. Replaced along with it. */
    private volatile long[] ownedKeys = NO_KEYS;

    // confined to the thread ticking the region
    private final TickState tickState;
    int appliedTickChanges;
    boolean gameTick = true;
    final List<RegionSection> spentTaskHolds = new ArrayList<>();

    volatile @Nullable Thread tickingThread;

    private final Object tpsLock = new Object();
    private final TickData tickData = new TickData(TPS_WINDOW_NS);
    private long previousTickStart = TimeUtil.DEADLINE_NOT_SET;
    private long intermediateTimeNS;
    private long intermediateTimeCpuNS;

    TickRegion(final WorldRegions world, final TickState tickState, final int appliedTickChanges) {
        this.world = world;
        this.tickState = tickState;
        this.appliedTickChanges = appliedTickChanges;
    }

    public Key world() {
        return world.key;
    }

    /**
     * {@return whether the chunk belongs to this region} Exact on the thread ticking it.
     */
    public boolean owns(final ChunkPos chunk) {
        final RegionSection section = world.sectionOf(chunk);
        return section != null && section.region == this;
    }

    void addSection(final RegionSection section) {
        sections.put(section.key, section);
        section.region = this;
        if (!section.isAlive()) {
            deadSections.add(section);
        }
    }

    void removeSection(final RegionSection section) {
        sections.remove(section.key);
        deadSections.remove(section);
    }

    void refreshOwned() {
        final RegionSection[] current = sections.values().toArray(NO_SECTIONS);
        final long[] keys = new long[current.length];
        for (int i = 0; i < current.length; i++) {
            keys[i] = current[i].key;
        }
        ownedKeys = keys;
        owned = current;
    }

    void clear() {
        sections.clear();
        deadSections.clear();
        mergeIntoLater.clear();
        expectingMergeFrom.clear();
        owned = NO_SECTIONS;
        ownedKeys = NO_KEYS;
    }

    /**
     * {@return a copy of the tick state for a region split from this one} On the ticking thread.
     */
    TickState forkTickState() {
        return tickState.copy();
    }

    void initScheduledStart(final long nanos) {
        setScheduledStart(nanos);
    }

    /**
     * {@return this region as an API snapshot}
     */
    RegionSnapshot snapshot() {
        final RegionSection[] current = owned;
        final LongOpenHashSet keys = new LongOpenHashSet(current.length);
        for (final RegionSection section : current) {
            keys.add(section.key);
        }
        return new RegionSnapshot(id, world.key, keys, centerChunk(current));
    }

    private static ChunkPos centerChunk(final RegionSection[] sections) {
        if (sections.length == 0) {
            return new ChunkPos(0, 0);
        }
        final RegionSection[] sorted = sections.clone();
        Arrays.sort(sorted, Comparator.<RegionSection>comparingInt(s -> s.z).thenComparingInt(s -> s.x));
        final RegionSection middle = sorted[sorted.length >> 1];
        final int shift = ThreadedRegionRegionizer.sectionShift();
        final int half = ThreadedRegionRegionizer.sectionSize() >> 1;
        return new ChunkPos((middle.x << shift) + half, (middle.z << shift) + half);
    }

    @Override
    public boolean hasTasks() {
        for (final RegionSection section : owned) {
            if (section.hasImmediateTasks()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean runTasks(final BooleanSupplier canContinue) {
        if (!world.tryMarkTicking(this)) {
            return false;
        }
        final long start = System.nanoTime();
        final long startCpu = currentThreadCpuNanos();
        boolean keepScheduled = false;
        enter();
        try {
            for (final RegionSection section : owned) {
                if (!section.runTasks(canContinue)) {
                    break;
                }
            }
        } finally {
            exit();
            final long endCpu = currentThreadCpuNanos();
            intermediateTimeNS += System.nanoTime() - start;
            if (startCpu >= 0 && endCpu >= 0) {
                intermediateTimeCpuNS += Math.max(0L, endCpu - startCpu);
            }
            keepScheduled = world.markNotTicking(this);
        }
        return keepScheduled;
    }

    @Override
    public boolean runTick() {
        if (!world.tryMarkTicking(this)) {
            return false;
        }
        boolean keepScheduled = false;
        try {
            tick();
        } finally {
            keepScheduled = world.markNotTicking(this);
        }
        return keepScheduled;
    }

    private void tick() {
        final ThreadedRegionRegionizer regionizer = world.regionizer;
        final RegionTickProfiler[] profilers = regionizer.tickProfilers();
        for (final RegionTickProfiler profiler : profilers) {
            profiler.heartbeat();
        }

        final long scheduledStart = getScheduledStart();
        final long tickStart = System.nanoTime();
        final long tickStartCpu = currentThreadCpuNanos();

        enter();
        try {
            appliedTickChanges = regionizer.applyTickChanges(tickState, appliedTickChanges);
            gameTick = tickState.beginTick();

            final RegionSection[] current = owned;
            for (final RegionSection section : current) {
                section.runDueTasks();
            }
            final long[] keys = ownedKeys;
            for (final RegionWideTickHandler handler : regionizer.regionWideTickHandlers()) {
                try {
                    handler.tick(world.key, keys);
                } catch (final Throwable ex) {
                    LOGGER.error("Error in a region-wide tick handler of region {}", id, ex);
                }
            }
            final RegionTickHandler[] handlers = regionizer.tickHandlers();
            for (final RegionSection section : current) {
                for (final RegionTickHandler handler : handlers) {
                    try {
                        handler.tick(world.key, section.x, section.z, section.currentTick);
                    } catch (final Throwable ex) {
                        LOGGER.error("Error in a tick handler of {}", section, ex);
                    }
                }
            }
            for (final RegionSection section : current) {
                // taskHold is only written under the world lock, which this thread took to tick
                if (section.taskHold && !section.hasPendingTasks()) {
                    spentTaskHolds.add(section);
                }
            }
        } finally {
            exit();
            final long taskNanos = intermediateTimeNS;
            final long durationNanos = recordTick(scheduledStart, tickStart, tickStartCpu);
            for (final RegionTickProfiler profiler : profilers) {
                try {
                    profiler.reportRegionTick(this, durationNanos, taskNanos);
                } catch (final Throwable ex) {
                    LOGGER.error("Error in a tick profiler of region {}", id, ex);
                }
            }
            scheduleNextTick(scheduledStart);
        }
    }

    private void enter() {
        final Thread self = Thread.currentThread();
        tickingThread = self;
        if (self instanceof final RegionTickThread tickThread) {
            tickThread.currentRegion = this;
        }
    }

    private void exit() {
        if (Thread.currentThread() instanceof final RegionTickThread tickThread) {
            tickThread.currentRegion = null;
        }
        tickingThread = null;
    }

    private void scheduleNextTick(final long scheduledStart) {
        final long interval = tickState.nanosPerTick();
        final long target = scheduledStart + interval;
        final long floor = System.nanoTime() - interval;
        setScheduledStart(TimeUtil.getGreatestTime(target, floor));
    }

    private long recordTick(final long scheduledStart, final long tickStart, final long tickStartCpu) {
        final long tickEnd = System.nanoTime();
        final long tickEndCpu = currentThreadCpuNanos();
        final boolean cpuMeasured = CPU_TIME_SUPPORTED && tickStartCpu >= 0 && tickEndCpu >= 0;

        final TickTime time = new TickTime(
                previousTickStart,
                scheduledStart,
                tickStart,
                tickStartCpu,
                tickEnd,
                tickEndCpu,
                intermediateTimeNS,
                intermediateTimeCpuNS,
                cpuMeasured);

        previousTickStart = tickStart;
        intermediateTimeNS = 0L;
        intermediateTimeCpuNS = 0L;

        synchronized (tpsLock) {
            tickData.addDataFrom(time);
        }

        return tickEnd - tickStart;
    }

    ThreadedRegionRegionizer.@Nullable RegionTpsSnapshot tpsSnapshot() {
        final TickData.TickReportData report;
        synchronized (tpsLock) {
            report = tickData.generateTickReport(null, System.nanoTime(), TICK_INTERVAL_NS);
        }
        if (report == null || report.collectedTicks() < 2) {
            return null;
        }

        int queued = 0;
        int holds = 0;
        for (final RegionSection section : owned) {
            queued += section.queuedTasks();
            holds += section.holds();
        }
        return new ThreadedRegionRegionizer.RegionTpsSnapshot(
                snapshot(),
                Math.min(report.tpsData().segmentAll().average(), 20.0),
                report.timePerTickData().segmentAll().average() / 1_000_000.0,
                report.utilisation() * 100.0,
                Math.max(0, queued),
                holds);
    }

    private static boolean cpuTimeSupported() {
        try {
            if (!THREADS.isThreadCpuTimeSupported()) {
                return false;
            }
            if (!THREADS.isThreadCpuTimeEnabled()) {
                THREADS.setThreadCpuTimeEnabled(true);
            }
            return THREADS.isThreadCpuTimeEnabled();
        } catch (final UnsupportedOperationException | SecurityException e) {
            LOGGER.warn("Per-thread CPU time is unavailable, falling back to tick occupancy", e);
            return false;
        }
    }

    private static long currentThreadCpuNanos() {
        if (!CPU_TIME_SUPPORTED) {
            return -1L;
        }
        final long cpu = THREADS.getCurrentThreadCpuTime();
        return cpu < 0 ? -1L : cpu;
    }

    @Override
    public String toString() {
        return "TickRegion{id=" + id + ",world=" + world.key.asString() + ",sections=" + owned.length + "}";
    }
}
