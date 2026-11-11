package fr.fidorial.world.farming;

import fr.fidorial.entity.Entity;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;

import java.util.List;

/**
 * The farming side of a block type: how it grows, what it needs to stand, how it answers to
 * fertilizer, and what it gives when harvested.
 *
 * <p>Every method has a default doing nothing, so that a behaviour only says what its block does: a
 * plant overrides growth and survival, soil overrides what happens when it is walked on.</p>
 *
 * <p>Every method runs on the thread of the region owning the block.</p>
 *
 * @see FarmingRegistry#attach
 * @since 0.1.0
 */
public interface FarmingBehaviour {

    /**
     * {@return {@code true} if the state may be picked for a random tick}
     *
     * <p>A state that has nothing left to do, such as a ripe crop, answers {@code false} so that the
     * engine does not call {@link #randomTick} for nothing.</p>
     *
     * @param state the state of the block
     * @since 0.1.0
     */
    default boolean ticksRandomly(final BlockData state) {
        return false;
    }

    /**
     * Called when the block is picked for a random tick: a few blocks of every chunk section are
     * picked at random on each game tick, as many as the {@code random_tick_speed} game rule says.
     * This is where a plant grows and where soil dries out.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state of the block
     * @since 0.1.0
     */
    default void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
    }

    /**
     * Called when a tick {@linkplain FarmingWorld#scheduleTick scheduled} for the block is due.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state of the block
     * @since 0.1.0
     */
    default void tick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
    }

    /**
     * Tells whether the block can stand where it is: a crop on its soil, a cane next to water.
     *
     * <p>It is asked before a player plants the block, in which case the block is not in the world
     * yet, and every time one of its six neighbours changes. A block that cannot stand any more is
     * {@linkplain FarmingWorld#destroy destroyed}, and drops its {@linkplain #drops harvest}.</p>
     *
     * @param world    the world of the block
     * @param position where the block is, or would be
     * @param state    the state of the block
     * @return {@code true} to stay, or to be planted
     * @since 0.1.0
     */
    default boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return true;
    }

    /**
     * Called when the block next to this one on one side changed, once it is known that this one
     * {@linkplain #canSurvive can still stand}, to let it follow: a stem whose fruit was picked
     * lets go of it, soil covered by a solid block turns back to dirt.
     *
     * @param world     the world of the block
     * @param position  where the block is
     * @param state     the state of the block
     * @param direction the side of this block the changed neighbour is on
     * @param neighbor  the new state of that neighbour
     * @return the state the block should take: {@code state} to keep it
     * @since 0.1.0
     */
    default BlockData updateShape(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                  final BlockFace direction, final BlockData neighbor) {
        return state;
    }

    /**
     * {@return {@code true} if a fertilizer can be used on the block as it is}
     *
     * <p>When it can, the fertilizer is used up, whether or not {@link #fertilize} then changes
     * anything.</p>
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state of the block
     * @since 0.1.0
     */
    default boolean canFertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return false;
    }

    /**
     * Applies a fertilizer to the block: a crop gains a few growth stages at once.
     *
     * <p>Only called when {@link #canFertilize} agreed.</p>
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state of the block
     * @since 0.1.0
     */
    default void fertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
    }

    /**
     * Works out what the block gives when it is broken: by a player who is not in creative mode, or
     * because it could {@linkplain #canSurvive no longer stand}.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param state    the state being broken
     * @param tool     what the block is broken with, {@link ItemStack#EMPTY} for a bare hand or when
     *                 nobody breaks it
     * @return the stacks to drop, empty for nothing
     * @since 0.1.0
     */
    default List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                  final ItemStack tool) {
        return List.of();
    }

    /**
     * Called when an entity lands on top of the block after a fall: where soil gets trampled.
     *
     * @param world        the world of the block
     * @param position     where the block is
     * @param state        the state of the block
     * @param entity       the entity that landed
     * @param fallDistance how far it fell, in blocks
     * @since 0.1.0
     */
    default void entityLanded(final FarmingWorld world, final BlockPosition position, final BlockData state,
                              final Entity entity, final double fallDistance) {
    }
}
