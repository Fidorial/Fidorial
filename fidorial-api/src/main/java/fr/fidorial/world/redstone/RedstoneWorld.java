package fr.fidorial.world.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;

/**
 * A world as the redstone engine drives it: the signals of {@link SignalGetter}, plus changing
 * blocks, telling their neighbours, and scheduling ticks.
 *
 * <p>Neighbour updates run in the order vanilla runs them, one chain at a time: an update caused
 * while another one is running is queued and runs right after it, rather than nested inside it.
 * Updates reaching a block of another region are handed over to the thread of that region.</p>
 *
 * @see RedstoneEngine#world(World)
 * @since 0.1.0
 */
public interface RedstoneWorld extends SignalGetter {

    /**
     * Replaces a block, and lets the redstone around it react as {@code update} says. Players see
     * the change and the light follows in every case.
     *
     * <p>Like any block write, it must be called on the thread of the region owning
     * {@code position}.</p>
     *
     * @param position where the block is
     * @param data     the new state
     * @param update   how far the change spreads
     * @return {@code false} if the block already held that state, or its chunk is not loaded
     * @throws IllegalStateException if the calling thread does not own {@code position}
     * @since 0.1.0
     */
    boolean setBlock(BlockPosition position, BlockData data, Update update);

    /**
     * Removes a block that can no longer stand, such as redstone dust whose support was removed,
     * telling its neighbours. A waterlogged block leaves its water behind.
     *
     * @param position where the block is
     * @return {@code false} if there was nothing to remove, or its chunk is not loaded
     * @throws IllegalStateException if the calling thread does not own {@code position}
     * @since 0.1.0
     */
    boolean destroyBlock(BlockPosition position);

    /**
     * Tells the six neighbours of a block that something changed there, in vanilla order: west,
     * east, down, up, north, south.
     *
     * @param position the block that changed
     * @param source   the block type that caused the update
     * @since 0.1.0
     */
    void updateNeighbors(BlockPosition position, TypedKey<BlockType> source);

    /**
     * Tells the neighbours of a block that something changed there, except the one on one side.
     *
     * @param position the block that changed
     * @param source   the block type that caused the update
     * @param skipped  the side not to tell, usually where the change comes from
     * @since 0.1.0
     */
    void updateNeighborsExcept(BlockPosition position, TypedKey<BlockType> source, BlockFace skipped);

    /**
     * Tells one block that one of its neighbours changed.
     *
     * @param position the block to tell
     * @param source   the block type that caused the update
     * @see RedstoneBehaviour#neighborChanged(RedstoneWorld, BlockPosition, BlockData, TypedKey)
     * @since 0.1.0
     */
    void neighborChanged(BlockPosition position, TypedKey<BlockType> source);

    /**
     * Tells one block that the block next to it on one side changed shape, so that it can follow:
     * redstone dust reconnecting, an observer firing.
     *
     * @param position  the block to tell
     * @param direction the side of {@code position} the changed block is on
     * @see RedstoneBehaviour#updateShape(RedstoneWorld, BlockPosition, BlockData, BlockFace, BlockData)
     * @since 0.1.0
     */
    void neighborShapeChanged(BlockPosition position, BlockFace direction);

    /**
     * Tells the comparators reading a block, next to it or behind a conductor, that its
     * {@linkplain SignalGetter#analogOutput(BlockPosition, BlockFace) analog output} changed. Call it
     * when what a comparator reads changes without the block state changing, such as the
     * contents of a container.
     *
     * @param position the block whose analog output changed
     * @since 0.1.0
     */
    void analogOutputChanged(BlockPosition position);

    /**
     * Schedules a call to {@link RedstoneBehaviour#tick(RedstoneWorld, BlockPosition, BlockData)},
     * once {@code delay} game ticks have elapsed. The tick only runs if the block is still of type
     * {@code block} by then.
     *
     * <p>A block holds at most one pending tick per block type: scheduling another one before the
     * first ran does nothing.</p>
     *
     * @param position the block to tick
     * @param block    the block type the tick is for
     * @param delay    the number of game ticks to wait, {@code 1} for the next tick
     * @param priority the order among the ticks due on the same game tick
     * @since 0.1.0
     */
    void scheduleTick(BlockPosition position, TypedKey<BlockType> block, int delay, TickPriority priority);

    /**
     * Schedules a tick at {@link TickPriority#NORMAL} priority.
     *
     * @param position the block to tick
     * @param block    the block type the tick is for
     * @param delay    the number of game ticks to wait, {@code 1} for the next tick
     * @see #scheduleTick(BlockPosition, TypedKey, int, TickPriority)
     * @since 0.1.0
     */
    default void scheduleTick(final BlockPosition position, final TypedKey<BlockType> block, final int delay) {
        scheduleTick(position, block, delay, TickPriority.NORMAL);
    }

    /**
     * {@return {@code true} if a tick is pending for a block, not counting the ticks running this game tick}
     *
     * <p>Only reliable on the thread of the region owning {@code position}.</p>
     *
     * @param position the block
     * @param block    the block type the tick is for
     * @since 0.1.0
     */
    boolean hasScheduledTick(BlockPosition position, TypedKey<BlockType> block);

    /**
     * {@return {@code true} if a tick of a block is due on the current game tick and has not run yet}
     *
     * <p>Only reliable on the thread of the region owning {@code position}.</p>
     *
     * @param position the block
     * @param block    the block type the tick is for
     * @since 0.1.0
     */
    boolean willTickThisTick(BlockPosition position, TypedKey<BlockType> block);

    /**
     * Tells the engine a block was replaced by something else than {@link #setBlock}, so that the
     * redstone around it reacts as if {@code setBlock} had been called with {@link Update#NEIGHBORS}.
     *
     * <p>The server calls it for every block placed, broken or blown up; a plugin only needs it when
     * it writes blocks by other means.</p>
     *
     * @param position where the block is
     * @param previous the state that was replaced
     * @param current  the state now in place
     * @since 0.1.0
     */
    void blockChanged(BlockPosition position, BlockData previous, BlockData current);

    /**
     * How far a block change made through {@link #setBlock(BlockPosition, BlockData, Update)}
     * spreads. Whatever it is, the {@link RedstoneBehaviour#onPlace} of the new state runs, as in
     * vanilla.
     *
     * @since 0.1.0
     */
    enum Update {
        /**
         * The neighbours are told the block changed, and their shapes follow: what placing or
         * breaking a block does.
         */
        NEIGHBORS,
        /**
         * Only the shapes of the neighbours follow, which is enough for redstone dust or an
         * observer next to it: what a repeater or a lamp changing state does.
         */
        SHAPES,
        /**
         * Neither the neighbours nor their shapes react.
         */
        QUIET
    }
}
