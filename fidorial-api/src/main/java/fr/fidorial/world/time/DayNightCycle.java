package fr.fidorial.world.time;

import net.kyori.adventure.key.Key;

/**
 * The clock of a world: its age, its time of day and how fast time flows.
 *
 * <p>Time is counted in ticks; a full day lasts {@value #DAY_LENGTH} ticks, the day starting at
 * sunrise ({@code 0}). Times of day are the time modulo {@value #DAY_LENGTH}.</p>
 *
 * @see fr.fidorial.world.World#dayNightCycle()
 * @since 0.1.0
 */
public interface DayNightCycle {

    /**
     * The length of a full day, in ticks.
     *
     * @since 0.1.0
     */
    int DAY_LENGTH = 24_000;

    /**
     * The time of day the sun is fully risen, and the day starts.
     *
     * @since 0.1.0
     */
    int SUNRISE_END = 0;

    /**
     * The time of day of noon.
     *
     * @since 0.1.0
     */
    int NOON = 6_000;

    /**
     * The time of day the sun starts setting.
     *
     * @since 0.1.0
     */
    int SUNSET = 12_000;

    /**
     * The time of day night falls.
     *
     * @since 0.1.0
     */
    int NIGHT_START = 13_000;

    /**
     * The time of day of midnight.
     *
     * @since 0.1.0
     */
    int MIDNIGHT = 18_000;

    /**
     * The time of day the sun starts rising.
     *
     * @since 0.1.0
     */
    int SUNRISE = 23_000;

    /**
     * The first time of day players may sleep, inclusive.
     *
     * @since 0.1.0
     */
    int SLEEP_START = 12_542;

    /**
     * The last time of day players may sleep, inclusive.
     *
     * @since 0.1.0
     */
    int SLEEP_END = 23_459;

    /**
     * The number of moon phases, one per day.
     *
     * @since 0.1.0
     */
    int MOON_PHASES = 8;

    /**
     * {@return the key of the {@code minecraft:world_clock} driving this cycle}
     *
     * @since 0.1.0
     */
    Key clock();

    /**
     * {@return the number of ticks this world has existed, whatever the cycle does}
     *
     * @since 0.1.0
     */
    long worldAge();

    /**
     * {@return the total time, in ticks; it only advances while the cycle runs}
     *
     * @since 0.1.0
     */
    long time();

    /**
     * Sets the total time and resynchronizes the clients.
     *
     * @param time the new total time, in ticks
     * @since 0.1.0
     */
    void setTime(long time);

    /**
     * Moves the time forward, or backward with a negative value.
     *
     * @param ticks the number of ticks to add
     * @since 0.1.0
     */
    default void addTime(final long ticks) {
        setTime(time() + ticks);
    }

    /**
     * {@return the fraction of tick accumulated when the {@linkplain #rate() rate} is not a whole number}
     *
     * @since 0.1.0
     */
    float fractionalTime();

    /**
     * {@return the number of ticks the time advances by per server tick, {@code 1} by default}
     *
     * @since 0.1.0
     */
    float rate();

    /**
     * Sets how fast time flows.
     *
     * @param rate the number of ticks per server tick; negative values are treated as {@code 0}
     * @since 0.1.0
     */
    void setRate(float rate);

    /**
     * {@return {@code true} while the time advances on its own}
     *
     * @since 0.1.0
     */
    boolean doDaylightCycle();

    /**
     * Freezes or resumes the time.
     *
     * @param enabled {@code false} to freeze the time
     * @since 0.1.0
     */
    void setDoDaylightCycle(boolean enabled);

    /**
     * {@return the time of day, from {@code 0} to {@code DAY_LENGTH - 1}}
     *
     * @since 0.1.0
     */
    default int timeOfDay() {
        return (int) Math.floorMod(time(), (long) DAY_LENGTH);
    }

    /**
     * {@return the number of full days elapsed}
     *
     * @since 0.1.0
     */
    default long day() {
        return Math.floorDiv(time(), (long) DAY_LENGTH);
    }

    /**
     * {@return the moon phase, from {@code 0} (full moon) to {@code MOON_PHASES - 1}}
     *
     * @since 0.1.0
     */
    default int moonPhase() {
        return (int) Math.floorMod(day(), (long) MOON_PHASES);
    }

    /**
     * {@return the part of the day the current time falls in}
     *
     * @since 0.1.0
     */
    default DayPhase phase() {
        return DayPhase.at(timeOfDay());
    }

    /**
     * {@return {@code true} during {@link DayPhase#DAY}}
     *
     * @since 0.1.0
     */
    default boolean isDay() {
        return phase() == DayPhase.DAY;
    }

    /**
     * {@return {@code true} during {@link DayPhase#NIGHT}}
     *
     * @since 0.1.0
     */
    default boolean isNight() {
        return phase() == DayPhase.NIGHT;
    }

    /**
     * {@return {@code true} when the time of day allows players to sleep}
     *
     * @since 0.1.0
     */
    default boolean canSleep() {
        final int tick = timeOfDay();
        return tick >= SLEEP_START && tick <= SLEEP_END;
    }

    /**
     * {@return the number of ticks until the given time of day comes next, from {@code 1} to {@code DAY_LENGTH}}
     *
     * @param targetTimeOfDay the time of day to wait for
     * @since 0.1.0
     */
    default long ticksUntil(final int targetTimeOfDay) {
        final int target = Math.floorMod(targetTimeOfDay, DAY_LENGTH);
        final int delta = target - timeOfDay();
        return delta > 0 ? delta : delta + DAY_LENGTH;
    }

    /**
     * {@return the angle of the sun in the sky, in degrees}
     *
     * @since 0.1.0
     */
    default double skyAngleDegrees() {
        final double tick = timeOfDay();
        final double turns = (tick - NOON) / (double) DAY_LENGTH;
        final double modOne = turns - Math.floor(turns);
        final double quarters = (tick - NOON) / (double) NOON;
        final double modFour = quarters - 4.0d * Math.floor(quarters / 4.0d);
        return ((1.0d - Math.cos(Math.PI * modOne)) + modFour) * 60.0d;
    }

    /**
     * {@return the angle of the sun in the sky, as a fraction of a full turn}
     *
     * @since 0.1.0
     */
    default double celestialAngle() {
        return skyAngleDegrees() / 360.0d;
    }
}
