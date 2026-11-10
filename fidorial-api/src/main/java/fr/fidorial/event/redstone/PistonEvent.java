package fr.fidorial.event.redstone;

import fr.fidorial.event.Cancellable;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;

import java.util.List;

/**
 * An event about a piston about to move: {@link #position()} is its base, and {@link #block()} the
 * state of that base before it moves.
 *
 * <p>It is fired once the piston worked out what it moves, and before anything changed in the
 * world. Cancelling it leaves the piston and the blocks around it as they are; the piston tries
 * again the next time one of its neighbours changes.</p>
 *
 * @see PistonExtendEvent
 * @see PistonRetractEvent
 * @since 0.1.0
 */
public interface PistonEvent extends RedstoneEvent, Cancellable {

    /**
     * {@return the side the piston faces, the one its head comes out of}
     *
     * @since 0.1.0
     */
    BlockFace direction();

    /**
     * {@return {@code true} for a sticky piston}
     *
     * @since 0.1.0
     */
    boolean sticky();

    /**
     * {@return the side the blocks move towards}: {@link #direction()} when the piston extends, its
     * opposite when it retracts
     *
     * @since 0.1.0
     */
    BlockFace movement();

    /**
     * {@return where the blocks the piston is about to move are, before they move; each ends up one
     * block further along {@link #movement()}}
     *
     * <p>The head of the piston is not one of them. The list cannot be modified, and is empty when
     * the piston moves nothing but its head.</p>
     *
     * @since 0.1.0
     */
    List<BlockPosition> movedBlocks();

    /**
     * {@return where the blocks the piston is about to break are: those in its way that do not
     * withstand being pushed, such as a torch or a flower}
     *
     * <p>The list cannot be modified.</p>
     *
     * @since 0.1.0
     */
    List<BlockPosition> brokenBlocks();
}
