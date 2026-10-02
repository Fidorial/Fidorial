package fr.fidorial.entity.ai;

/**
 * One behaviour a mob may pursue, scheduled by its {@link Goals}.
 *
 * <p>A single goal runs at a time. Each tick, a goal of better priority than the running one that
 * {@linkplain #canStart() can start} preempts it; the running goal stops once it no longer
 * {@linkplain #shouldContinue() should continue}.</p>
 *
 * @since 0.1.0
 */
public interface Goal {

    /**
     * {@return the priority of this goal; the lower the value, the more important the goal}
     *
     * @since 0.1.0
     */
    int priority();

    /**
     * {@return {@code true} if the goal may start now}
     *
     * @since 0.1.0
     */
    boolean canStart();

    /**
     * {@return {@code true} if the goal, once running, should keep running}
     *
     * @since 0.1.0
     */
    boolean shouldContinue();

    /**
     * Called when the goal starts running.
     *
     * @since 0.1.0
     */
    default void start() {
    }

    /**
     * Called when the goal stops running, because it ended or was preempted.
     *
     * @since 0.1.0
     */
    default void stop() {
    }

    /**
     * Called every tick while the goal runs.
     *
     * @since 0.1.0
     */
    default void tick() {
    }
}
