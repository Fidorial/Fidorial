package fr.fidorial.event.redstone;

/**
 * Fired when a piston that is no longer powered is about to retract: its head comes back in, and a
 * sticky piston pulls the block in front of its head along.
 *
 * <p>{@link #movedBlocks()} holds what a sticky piston pulls. It is empty for a normal piston, and
 * for a sticky one that lets go of its block: one retracting while that block is still being
 * pushed, as a short pulse makes it do.</p>
 *
 * <p>Cancelling the event keeps the piston extended, although it is not powered.</p>
 *
 * @since 0.1.0
 */
public interface PistonRetractEvent extends PistonEvent {
}
