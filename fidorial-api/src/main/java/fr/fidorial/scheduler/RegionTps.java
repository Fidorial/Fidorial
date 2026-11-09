package fr.fidorial.scheduler;

/**
 * Performance and state information for a scheduler region.
 *
 * <p>A region is a group of sections processed by a dedicated scheduler thread. This interface
 * exposes metrics such as TPS, MSPT, CPU usage, queued tasks and ticket count, measured over the
 * last few seconds of the region. A region born from a merge keeps the measures of the region it
 * was merged into; the regions born from a split start measuring from scratch.</p>
 *
 * @see RegionizedScheduler#tpsSnapshots()
 * @since 0.1.0
 */
public interface RegionTps extends Region {

    /**
     * Returns the current ticks per second (TPS) of this region.
     *
     * <p>The ideal value is {@code 20.0}.</p>
     *
     * @return the current TPS
     * @since 0.1.0
     */
    double tps();

    /**
     * Returns the average milliseconds per tick (MSPT) for this region.
     *
     * <p>Lower values indicate better performance. A value around
     * {@code 50.0 ms} corresponds to the limit for maintaining 20 TPS.</p>
     *
     * @return the average MSPT
     * @since 0.1.0
     */
    double msptAvg();

    /**
     * Returns the CPU time consumed by the region thread over the measured
     * window, relative to the wall-clock time elapsed during that same window.
     *
     * <p>A value of {@code 100.0} means the region fully saturates a single CPU
     * core, spending the entire measurement window executing. Values are not
     * capped and may exceed {@code 100.0} if the region falls behind and
     * accumulates additional work.</p>
     *
     * @return the CPU usage of the region, expressed as a percentage of one CPU
     * core
     * @since 0.1.0
     */
    double cpuPercent();

    /**
     * Returns the number of tasks currently queued for this region.
     *
     * @return the queued task count
     * @since 0.1.0
     */
    int queuedTasks();

    /**
     * Returns the number of loaded chunks and tickets keeping the sections of this region occupied.
     *
     * @return the loaded chunks and tickets of the region
     * @since 0.1.0
     */
    int tickets();
}
