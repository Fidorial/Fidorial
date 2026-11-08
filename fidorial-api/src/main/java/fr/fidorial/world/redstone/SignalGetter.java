package fr.fidorial.world.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockGetter;

/**
 * Read access to the blocks of a world and to the redstone signals running through them.
 *
 * @see RedstoneEngine#world(World)
 * @since 0.1.0
 */
public interface SignalGetter extends BlockGetter {

    /**
     * {@return the world these signals run in}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * Gets the signal a block powers its neighbour with: the weak signal it emits, or, for a
     * conductor, the strongest strong signal it receives when that is higher.
     *
     * @param position  the block emitting the signal
     * @param direction the direction from the block reading the signal to {@code position}
     * @return the signal, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    int signal(BlockPosition position, BlockFace direction);

    /**
     * {@return {@code true} if {@link #signal(BlockPosition, BlockFace)} is above {@code 0}}
     *
     * @param position  the block emitting the signal
     * @param direction the direction from the block reading the signal to {@code position}
     * @since 0.1.0
     */
    default boolean hasSignal(final BlockPosition position, final BlockFace direction) {
        return signal(position, direction) > 0;
    }

    /**
     * Gets the strong signal a block emits towards one of its neighbours, the one that powers a
     * conductor through.
     *
     * @param position  the block emitting the signal
     * @param direction the direction from the block reading the signal to {@code position}
     * @return the signal, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    int directSignal(BlockPosition position, BlockFace direction);

    /**
     * {@return the strongest strong signal a block receives from its six neighbours}
     *
     * @param position the block receiving the signals
     * @since 0.1.0
     */
    int directSignalTo(BlockPosition position);

    /**
     * {@return the strongest signal a block receives from its six neighbours, from {@code 0} to {@code 15}}
     *
     * @param position the block receiving the signals
     * @since 0.1.0
     */
    int bestNeighborSignal(BlockPosition position);

    /**
     * {@return {@code true} when at least one of the six neighbours of a block powers it, what a door,
     * a lamp or a piston listens to}
     *
     * @param position the block receiving the signals
     * @since 0.1.0
     */
    boolean hasNeighborSignal(BlockPosition position);

    /**
     * Gets the signal a block feeds into the side of a repeater or a comparator.
     *
     * @param position    the block on the side of the repeater or comparator
     * @param direction   the direction from the repeater or comparator to {@code position}
     * @param diodesOnly  {@code true} to only accept repeaters and comparators, what locks a repeater
     * @return the signal, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    int controlInputSignal(BlockPosition position, BlockFace direction, boolean diodesOnly);

    /**
     * {@return {@code true} if a comparator can read a level out of the block, such as how full a
     * container is}
     *
     * @param position the block to read
     * @see RedstoneBehaviour#hasAnalogOutput(BlockData)
     * @since 0.1.0
     */
    boolean hasAnalogOutput(BlockPosition position);

    /**
     * Gets the level a comparator reads out of a block.
     *
     * @param position  the block to read
     * @param direction the direction from the comparator to {@code position}
     * @return the level, from {@code 0} to {@code 15}, {@code 0} for a block with no analog output
     * @since 0.1.0
     */
    int analogOutput(BlockPosition position, BlockFace direction);

    /**
     * {@return {@code true} if the block conducts power: a full, opaque block such as stone, but not
     * glass, glowstone or a block of redstone}
     *
     * @param position the block
     * @since 0.1.0
     */
    boolean isConductor(BlockPosition position);

    /**
     * {@return {@code true} if a face of the block can hold a block attached to it}
     *
     * @param position the block
     * @param face     the face, as seen from the block itself: {@link BlockFace#UP} for its top
     * @param support  how much of the face must be covered
     * @since 0.1.0
     */
    boolean isFaceSturdy(BlockPosition position, BlockFace face, SupportType support);
}
