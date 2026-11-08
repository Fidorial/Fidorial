package fr.fidorial.event.redstone;

import fr.fidorial.event.Cancellable;

/**
 * Fired when a block that listens to redstone is about to react to being powered, or to no longer
 * being powered: a lamp lighting up or going out, a door, a trapdoor or a fence gate opening or
 * closing, a note block playing, a copper bulb switching.
 *
 * <p>It is fired when the block reacts, which is not always when the signal changes: a lamp goes out
 * four game ticks after it lost its signal, and the event is fired then.</p>
 *
 * <p>Cancelling the event leaves the block as it is: the door stays shut, the lamp stays off. As the
 * block did not take the signal into account, the next update reaching it fires the event again.</p>
 *
 * @since 0.1.0
 */
public interface RedstoneInputEvent extends RedstoneEvent, Cancellable {

    /**
     * {@return {@code true} if the block is now powered, {@code false} if it lost its signal}
     *
     * @since 0.1.0
     */
    boolean powered();
}
