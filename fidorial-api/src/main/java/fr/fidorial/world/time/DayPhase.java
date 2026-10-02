package fr.fidorial.world.time;

/**
 * The four parts of a day, by time of day.
 *
 * @since 0.1.0
 */
public enum DayPhase {

    /**
     * From sunrise to sunset.
     */
    DAY(0, 12_000),
    /**
     * From sunset to nightfall.
     */
    DUSK(12_000, 13_000),
    /**
     * From nightfall to sunrise.
     */
    NIGHT(13_000, 23_000),
    /**
     * From sunrise until the sun is fully up.
     */
    DAWN(23_000, 24_000);

    private final int start;
    private final int end;

    DayPhase(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * {@return the phase a time of day falls in}
     *
     * @param timeOfDay the time of day, any value being wrapped into a single day
     * @since 0.1.0
     */
    public static DayPhase at(final int timeOfDay) {
        final int tick = Math.floorMod(timeOfDay, DayNightCycle.DAY_LENGTH);
        if (tick < DUSK.start) {
            return DAY;
        }
        if (tick < NIGHT.start) {
            return DUSK;
        }
        if (tick < DAWN.start) {
            return NIGHT;
        }
        return DAWN;
    }

    /**
     * {@return the first time of day of this phase, inclusive}
     *
     * @since 0.1.0
     */
    public int start() {
        return start;
    }

    /**
     * {@return the last time of day of this phase, exclusive}
     *
     * @since 0.1.0
     */
    public int end() {
        return end;
    }

    /**
     * {@return the length of this phase, in ticks}
     *
     * @since 0.1.0
     */
    public int lengthTicks() {
        return end - start;
    }

    /**
     * {@return {@code true} for {@link #DAY}}
     *
     * @since 0.1.0
     */
    public boolean daylight() {
        return this == DAY;
    }
}
