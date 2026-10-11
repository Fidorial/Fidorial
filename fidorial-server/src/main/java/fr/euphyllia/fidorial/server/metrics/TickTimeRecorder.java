package fr.euphyllia.fidorial.server.metrics;

import fr.euphyllia.fidorial.server.schedulers.RegionTickProfiler;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;


final class TickTimeRecorder implements RegionTickProfiler {

    private static final long SLOW_TICK_NANOS = TimeUnit.MILLISECONDS.toNanos(50L);

    private final AtomicReference<Window> window = new AtomicReference<>(new Window());

    @Override
    public void heartbeat() {

    }

    @Override
    public void reportRegionTick(final double durationMillis) {
        record((long) (durationMillis * 1_000_000.0D));
    }

    @Override
    public void reportRegionTick(final Key world, final int sectionX, final int sectionZ, final long tickNanos, final long taskNanos) {
        record(tickNanos);
    }

    private void record(final long tickNanos) {
        if (tickNanos < 0L) {
            return;
        }
        final Window current = window.get();
        current.ticks.increment();
        current.totalNanos.add(tickNanos);
        if (tickNanos > SLOW_TICK_NANOS) {
            current.slowTicks.increment();
        }
    }

    @Nullable Double averageMspt() {
        final Window current = window.get();
        final long ticks = current.ticks.sum();
        if (ticks == 0L) {
            return null;
        }
        final double mspt = current.totalNanos.sum() / (double) ticks / 1_000_000.0D;
        return Math.round(mspt * 100.0D) / 100.0D;
    }

    @Nullable Double slowTickPercent() {
        final Window current = window.get();
        final long ticks = current.ticks.sum();
        if (ticks == 0L) {
            return null;
        }
        final double percent = current.slowTicks.sum() * 100.0D / ticks;
        return Math.round(percent * 100.0D) / 100.0D;
    }

    void reset() {
        window.set(new Window());
    }

    private static final class Window {
        final LongAdder ticks = new LongAdder();
        final LongAdder totalNanos = new LongAdder();
        final LongAdder slowTicks = new LongAdder();
    }
}
