package fr.fidorial.entity.ai;

import java.util.EnumSet;
import java.util.Set;

/**
 * One behaviour a mob may pursue, scheduled by its {@link Goals}.
 *
 * <p>Several goals run at once as long as they do not need the same {@linkplain #flags() controls}:
 * a mob may stroll while it looks at a player. Each tick, a goal that
 * {@linkplain #canStart() can start} takes the controls it needs from the running goals of lower
 * priority; a running goal stops once it no longer {@linkplain #shouldContinue() should continue}
 * or loses one of its controls.</p>
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
     * {@return the controls of the mob this goal needs while it runs}
     *
     * <p>Two goals sharing a control never run together. By default a goal needs
     * {@linkplain Flag#MOVE moving}, {@linkplain Flag#LOOK looking} and {@linkplain Flag#JUMP
     * jumping}, so that it runs alone.</p>
     *
     * @since 0.1.0
     */
    default Set<Flag> flags() {
        return Flag.EXCLUSIVE;
    }

    /**
     * {@return {@code true} if a goal of better priority may take a control from this goal while it
     * runs}
     *
     * @since 0.1.0
     */
    default boolean isInterruptible() {
        return true;
    }

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

    /**
     * A control of a mob that a running {@link Goal} holds.
     *
     * @since 0.1.0
     */
    enum Flag {
        /**
         * Where the mob walks.
         *
         * @since 0.1.0
         */
        MOVE,
        /**
         * Where the mob looks.
         *
         * @since 0.1.0
         */
        LOOK,
        /**
         * Whether the mob jumps.
         *
         * @since 0.1.0
         */
        JUMP,
        /**
         * Whom the mob targets.
         *
         * @since 0.1.0
         */
        TARGET;

        /**
         * The controls of a goal that runs alone: moving, looking and jumping.
         *
         * @since 0.1.0
         */
        public static final Set<Flag> EXCLUSIVE = Set.copyOf(EnumSet.of(MOVE, LOOK, JUMP));
    }
}
