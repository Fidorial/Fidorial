package fr.euphyllia.fidorial.server.schedulers.tick;

/**
 * The tick settings a newly started ticker begins with.
 *
 * @param rate   ticks per second, {@code 1} to {@code 10000}
 * @param frozen whether game logic is paused
 */
public record TickSettings(float rate, boolean frozen) {

    public static final float DEFAULT_RATE = 20.0F;

    public static final TickSettings DEFAULT = new TickSettings(DEFAULT_RATE, false);
}
