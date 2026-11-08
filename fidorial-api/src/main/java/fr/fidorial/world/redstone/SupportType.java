package fr.fidorial.world.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;

/**
 * How much of a face a block must cover to hold another block against it.
 *
 * @see SignalGetter#isFaceSturdy(BlockPosition, BlockFace, SupportType)
 * @since 0.1.0
 */
public enum SupportType {
    /**
     * The whole face: what a lever, a button or a wall torch hangs on, and what redstone dust lies on.
     */
    FULL,
    /**
     * The center of the face: what a standing torch stands on.
     */
    CENTER,
    /**
     * The rim of the face: what a repeater or a comparator stands on.
     */
    RIGID
}
