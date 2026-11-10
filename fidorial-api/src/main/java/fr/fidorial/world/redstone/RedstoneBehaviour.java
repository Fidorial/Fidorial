package fr.fidorial.world.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

/**
 * The redstone side of a block type: whether it emits a signal, what a comparator reads out of it,
 * and how it reacts when its surroundings change.
 *
 * @since 0.1.0
 */
public interface RedstoneBehaviour {

    /**
     * {@return {@code true} if the state emits a signal, which also makes redstone dust connect to it}
     *
     * @param state the state of the block
     * @since 0.1.0
     */
    default boolean isSignalSource(final BlockData state) {
        return false;
    }

    /**
     * Gets the weak signal the block emits towards one of its neighbours.
     *
     * @param world     the world of the block
     * @param position  where the block is
     * @param state     the state of the block
     * @param direction the direction from the block reading the signal to this block
     * @return the signal, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    default int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return 0;
    }

    /**
     * Gets the strong signal the block emits towards one of its neighbours: a conductor receiving it
     * powers its own neighbours with it.
     *
     * @param world     the world of the block
     * @param position  where the block is
     * @param state     the state of the block
     * @param direction the direction from the block reading the signal to this block
     * @return the signal, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    default int directSignal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return 0;
    }

    /**
     * Tells whether redstone dust next to the block connects to it.
     *
     * @param world     the world of the block
     * @param position  where the block is
     * @param state     the state of the block
     * @param direction the direction from the dust to this block, or {@code null} when the dust is
     *                  one block above or below, where only dust connects
     * @return {@code true} to connect; by default, when the block is a signal source and the dust is
     *         beside it
     * @since 0.1.0
     */
    default boolean connectsToWire(final SignalGetter world, final BlockPosition position, final BlockData state,
                                   final @Nullable BlockFace direction) {
        return direction != null && isSignalSource(state);
    }

    /**
     * {@return {@code true} if a comparator can read a level out of the state}
     *
     * @param state the state of the block
     * @since 0.1.0
     */
    default boolean hasAnalogOutput(final BlockData state) {
        return false;
    }

    /**
     * Gets the level a comparator reads out of the block, such as how full a container is.
     *
     * @param world     the world of the block
     * @param position  where the block is
     * @param state     the state of the block
     * @param direction the direction from the comparator to this block
     * @return the level, from {@code 0} to {@code 15}
     * @since 0.1.0
     */
    default int analogOutput(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return 0;
    }

    /**
     * Called when the state at {@code position} became {@code state}: the block was just placed, or
     * only one of its properties changed, as when a repeater turns on. The block is in place.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the new state
     * @param previous the state it replaced, of another block type when the block was just placed
     * @since 0.1.0
     */
    default void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
    }

    /**
     * Called once the block was replaced by another block type, to tell the blocks it powered: it is
     * no longer in the world. {@link BlockBehaviour#onRemove} ran before, while it still was. Like
     * vanilla, a replacement made with {@link RedstoneWorld.Update#SHAPES} or
     * {@link RedstoneWorld.Update#QUIET} does not call it.
     *
     * @param world    the world of the block
     * @param position where the block was
     * @param state    the state that was removed
     * @since 0.1.0
     */
    default void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
    }

    /**
     * Called when one of the six neighbours of the block changed: what a lamp, a door or redstone
     * dust react to.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state of the block
     * @param source   the block type that caused the update
     * @since 0.1.0
     */
    default void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final TypedKey<BlockType> source) {
    }

    /**
     * Called when the block next to this one on one side changed, to let this one follow its shape:
     * redstone dust reconnects, an observer fires, a lever whose support disappeared breaks.
     *
     * @param world     the world of the block
     * @param position  where the block is
     * @param state     the state of the block
     * @param direction the side of this block the changed neighbour is on
     * @param neighbor  the new state of that neighbour
     * @return the state the block should take: {@code state} to keep it, any kind of air to break it
     * @since 0.1.0
     */
    default BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                  final BlockFace direction, final BlockData neighbor) {
        return state;
    }

    /**
     * Called when the block was placed, changed or removed, to update the shape of blocks that are
     * not its direct neighbours, such as redstone dust one block lower or higher beside it.
     *
     * @param world    the world of the block
     * @param position where the block is, or was
     * @param state    the state of the block, the removed one when it was removed
     * @since 0.1.0
     */
    default void updateIndirectNeighborShapes(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
    }

    /**
     * Called when a tick {@linkplain RedstoneWorld#scheduleTick scheduled} for the block is due: a
     * repeater switches, a button pops back out.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state of the block
     * @since 0.1.0
     */
    default void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
    }

    /**
     * Called when a block event {@linkplain RedstoneWorld#blockEvent queued} for the block is due:
     * a piston extends or retracts.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state of the block
     * @param type     what the event is, as given when it was queued
     * @param data     the value going with it, as given when it was queued
     * @return {@code true} if the block acted on the event, which is then sent to the players seeing
     *         the block; {@code false}, the default, if it ignored it
     * @since 0.1.0
     */
    default boolean triggerEvent(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final int type, final int data) {
        return false;
    }
}
