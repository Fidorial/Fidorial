package fr.fidorial.event.redstone;

/**
 * Fired when a powered piston is about to extend: its head comes out and pushes the blocks in
 * front of it.
 *
 * <p>Cancelling the event keeps the piston retracted, although it is powered.</p>
 *
 * @since 0.1.0
 */
public interface PistonExtendEvent extends PistonEvent {
}
