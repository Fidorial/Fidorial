package fr.euphyllia.fidorial.server.schedulers;

import net.kyori.adventure.key.Key;

/**
 * Callback surface used by the scheduler to feed an external profiler.
 *
 * <p>Kept free of any profiler-specific type so the scheduler package stays independent of spark.
 * The only implementations today are {@link fr.euphyllia.fidorial.server.spark.SparkTickBridge} and {@link fr.euphyllia.fidorial.server.debug.DebugSampleBroadcaster}.</p>
 *
 * <p>Every method is called from region worker threads and from the day/night ticker, so
 * implementations must be thread safe and must not block.</p>
 */
public interface RegionTickProfiler {

    /**
     * Implementation used while no profiler is attached.
     */
    RegionTickProfiler NO_OP = new RegionTickProfiler() {

        @Override
        public void heartbeat() {
        }

        @Override
        public void reportRegionTick(final double durationMillis) {
        }
    };

    /**
     * Signals that a tick is being processed somewhere on the server.
     *
     * <p>Called far more often than 20 times per second — once per region tick plus once per
     * day/night tick — so the implementation is responsible for collapsing those calls down to a
     * single logical server tick per 50 ms window.</p>
     */
    void heartbeat();

    /**
     * Reports how long a single region took to tick.
     *
     * @param durationMillis the wall-clock duration of the region tick, in milliseconds
     */
    void reportRegionTick(double durationMillis);

    /**
     * Reports a region tick with its identity and the task time spent since the previous tick.
     * Called on the region thread right after the tick.
     */
    default void reportRegionTick(final Key world, final int sectionX, final int sectionZ, final long tickNanos, final long taskNanos) {
        reportRegionTick(tickNanos / 1_000_000.0D);
    }
}
