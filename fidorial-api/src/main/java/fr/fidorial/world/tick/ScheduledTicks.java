package fr.fidorial.world.tick;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.keys.FluidKeys;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.TickPriority;

/**
 * The ticks scheduled in a world: a block or a fluid asks to be ticked again some game ticks later,
 * as a repeater does to switch or water to flow on.
 *
 * <p>On every game tick, the block ticks due run first, by {@linkplain TickPriority priority} then
 * in the order they were scheduled, then the fluid ticks due, then the block events. A tick due when
 * its block or fluid is no longer there is dropped. A block or a fluid has at most one tick of its
 * type pending at a position: scheduling another one while it waits does nothing.</p>
 *
 * <p>The ticks of the blocks of a chunk are saved with it, each with the number of game ticks it
 * is still due in, and scheduled again once the chunk is loaded: a world picks up where it was
 * stopped.</p>
 *
 * <p>Scheduling hands the tick over to the region owning the position when another one runs the
 * calling thread; the queries only answer for positions the calling thread owns.</p>
 *
 * @see World#ticks()
 * @since 0.1.0
 */
public interface ScheduledTicks {

    /**
     * Schedules a tick for a block.
     *
     * @param position where the block is
     * @param block    the block type to tick; the tick is dropped if another block stands there once due
     * @param delay    in how many game ticks it is due, {@code 0} or less for the next game tick
     * @param priority the order of the ticks due in the same game tick
     * @since 0.1.0
     */
    void scheduleBlockTick(BlockPosition position, TypedKey<BlockType> block, int delay, TickPriority priority);

    /**
     * Schedules a tick for a block, of {@link TickPriority#NORMAL} priority.
     *
     * @param position where the block is
     * @param block    the block type to tick
     * @param delay    in how many game ticks it is due
     * @since 0.1.0
     */
    void scheduleBlockTick(BlockPosition position, TypedKey<BlockType> block, int delay);

    /**
     * {@return {@code true} if a tick of a block type is pending at a position}, not counting one
     * taken to run in the current game tick
     *
     * @param position where the block is
     * @param block    the block type
     * @since 0.1.0
     */
    boolean hasBlockTick(BlockPosition position, TypedKey<BlockType> block);

    /**
     * {@return {@code true} if a tick of a block type is due in the current game tick and has not run
     * yet}: what a repeater looks at not to turn off while another one ahead still has to turn on
     *
     * @param position where the block is
     * @param block    the block type
     * @since 0.1.0
     */
    boolean willBlockTickThisTick(BlockPosition position, TypedKey<BlockType> block);

    /**
     * Schedules a tick for a fluid: it flows on, dries up or turns into a source then.
     *
     * @param position where the fluid is
     * @param fluid    the fluid to tick, {@link FluidKeys#WATER} or {@link FluidKeys#LAVA}; the tick is
     *                 dropped if the block holds another one once due
     * @param delay    in how many game ticks it is due, {@code 0} or less for the next game tick
     * @throws IllegalArgumentException if the fluid is neither water nor lava
     * @since 0.1.0
     */
    void scheduleFluidTick(BlockPosition position, TypedKey<Fluid> fluid, int delay);

    /**
     * {@return {@code true} if a tick of a fluid is pending at a position}
     *
     * @param position where the fluid is
     * @param fluid    the fluid
     * @since 0.1.0
     */
    boolean hasFluidTick(BlockPosition position, TypedKey<Fluid> fluid);
}
