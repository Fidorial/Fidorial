package fr.euphyllia.fidorial.server.schedulers.tick;

import java.util.concurrent.TimeUnit;

/**
 * The tick settings of one ticker. Confined to that ticker's thread: no synchronisation.
 */
public final class TickState {

    private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1L);

    private float rate;
    private boolean frozen;
    private int stepTicks;
    private long accumulatedNanos;

    public TickState(final TickSettings settings) {
        this.rate = settings.rate();
        this.frozen = settings.frozen();
    }

    /**
     * {@return a copy of this state, step in progress included, for a ticker taking over part of this one}
     */
    public TickState copy() {
        final TickState copy = new TickState(new TickSettings(rate, frozen));
        copy.stepTicks = stepTicks;
        copy.accumulatedNanos = accumulatedNanos;
        return copy;
    }

    public long nanosPerTick() {
        return (long) (NANOS_PER_SECOND / rate);
    }

    /**
     * {@return whether the tick starting now runs game logic}
     *
     * <p>While frozen, spends one step tick if any are left.</p>
     */
    public boolean beginTick() {
        if (!frozen) {
            return true;
        }
        if (stepTicks > 0) {
            stepTicks--;
            return true;
        }
        return false;
    }

    /**
     * {@return how many game ticks a ticker on a fixed schedule runs now}
     *
     * @param elapsedNanos time since that ticker's previous run
     */
    public int dueTicks(final long elapsedNanos) {
        accumulatedNanos += elapsedNanos;
        final long perTick = nanosPerTick();
        int due = (int) (accumulatedNanos / perTick);
        accumulatedNanos -= due * perTick;
        if (frozen) {
            due = Math.min(due, stepTicks);
            stepTicks -= due;
        }
        return due;
    }

    public void apply(final TickChange change) {
        switch (change) {
            case TickChange.SetFrozen(final boolean freeze) -> {
                frozen = freeze;
                if (freeze) {
                    stepTicks = 0;
                }
            }
            case TickChange.SetRate(final float newRate) -> rate = newRate;
            case TickChange.Step(final int ticks) -> stepTicks = ticks;
            case TickChange.StopStep() -> stepTicks = 0;
        }
    }
}
