package fr.fidorial.world.fluid;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.keys.FluidKeys;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.World;

/**
 * The fluid simulation, registered as a {@linkplain ServiceRegistry service}.
 *
 * <p>Changes are made on the thread of the region owning the block.</p>
 *
 * @since 0.1.0
 */
public interface FluidManager {

    /**
     * {@return the fluid held by a block}: the fluid of a fluid block, the water of a waterlogged
     * block or of a block always under water; an {@linkplain FluidState#isEmpty() empty} state for
     * any other block or one that is not loaded
     *
     * @param world    the world
     * @param position the block
     * @since 0.1.0
     */
    FluidState fluidAt(World world, BlockPosition position);

    /**
     * Places a source of fluid, which then spreads: the block it replaces breaks and drops what it
     * would drop, unless it is air or the source fills a waterloggable block.
     *
     * @param world    the world
     * @param position the block
     * @param fluid    the fluid, {@link FluidKeys#WATER} or {@link FluidKeys#LAVA}
     * @return {@code true} if the source was placed
     * @throws IllegalArgumentException if the fluid is neither water nor lava
     * @since 0.1.0
     */
    boolean placeSource(World world, BlockPosition position, TypedKey<Fluid> fluid);

    /**
     * Takes the fluid out of a block, the way a bucket does: a fluid block turns into air, a
     * waterlogged block dries. The fluid around then drains.
     *
     * @param world    the world
     * @param position the block
     * @return {@code true} if the block held a fluid that could be taken
     * @since 0.1.0
     */
    boolean removeFluid(World world, BlockPosition position);

    /**
     * Schedules a tick for the fluid of a block, due as soon as that fluid ticks, if the block holds
     * one.
     *
     * @param world    the world
     * @param position the block
     * @since 0.1.0
     */
    void scheduleUpdate(World world, BlockPosition position);
}
