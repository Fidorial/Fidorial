package fr.fidorial.event.redstone;

import fr.fidorial.event.Cancellable;

/**
 * Fired when a redstone component is about to change the signal it emits: a lever pulled, a button
 * pressed or popping back out, a torch turning on or off, a repeater or a comparator switching, an
 * observer pulsing.
 *
 * <p>The signals are those the component emits towards the blocks it powers: {@code 15} or {@code 0}
 * for most components, the level it outputs for a comparator.</p>
 *
 * <p>Cancelling the event keeps the component as it is, and the blocks around it are not told
 * anything changed. A lever stays where it was. A button is not pressed, or, popping back out, stays
 * pressed and tries again once its delay has elapsed once more; an observer ending its pulse tries
 * again two game ticks later. A repeater, a comparator or a torch tries again when one of its
 * neighbours next changes.</p>
 *
 * @since 0.1.0
 */
public interface RedstoneOutputEvent extends RedstoneEvent, Cancellable {

    /**
     * {@return the signal the component emits, from {@code 0} to {@code 15}}
     *
     * @since 0.1.0
     */
    int oldSignal();

    /**
     * {@return the signal the component is about to emit, from {@code 0} to {@code 15}}
     *
     * @since 0.1.0
     */
    int newSignal();

    /**
     * {@return {@code true} if the component is turning on: it emits no signal and is about to emit one}
     *
     * @since 0.1.0
     */
    default boolean turningOn() {
        return oldSignal() == 0 && newSignal() > 0;
    }

    /**
     * {@return {@code true} if the component is turning off: it emits a signal and is about to stop}
     *
     * @since 0.1.0
     */
    default boolean turningOff() {
        return oldSignal() > 0 && newSignal() == 0;
    }
}