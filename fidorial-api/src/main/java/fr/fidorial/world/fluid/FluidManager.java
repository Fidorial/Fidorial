package fr.fidorial.world.fluid;

import net.kyori.adventure.key.Key;

/**
 * The fluid simulation, registered as a {@linkplain fr.fidorial.service.ServiceRegistry service}.
 *
 * <p>Coordinates are absolute block coordinates in the world identified by its key.</p>
 *
 * @since 0.1.0
 */
public interface FluidManager {

    /**
     * {@return the fluid held by a block, {@link FluidState#empty()} if the world is not loaded}
     *
     * @param world the world key
     * @param x     the block X coordinate
     * @param y     the block Y coordinate
     * @param z     the block Z coordinate
     * @since 0.1.0
     */
    FluidState fluidAt(Key world, int x, int y, int z);

    /**
     * Places a fluid source and lets it spread.
     *
     * @param world the world key
     * @param x     the block X coordinate
     * @param y     the block Y coordinate
     * @param z     the block Z coordinate
     * @param type  the fluid
     * @return {@code true} if the source was placed
     * @since 0.1.0
     */
    boolean placeSource(Key world, int x, int y, int z, FluidType type);

    /**
     * Removes the fluid of a block and lets the surroundings drain.
     *
     * @param world the world key
     * @param x     the block X coordinate
     * @param y     the block Y coordinate
     * @param z     the block Z coordinate
     * @return {@code true} if the block held a fluid
     * @since 0.1.0
     */
    boolean removeFluid(Key world, int x, int y, int z);

    /**
     * Schedules the next spreading step of the fluid at a block, if any.
     *
     * @param world the world key
     * @param x     the block X coordinate
     * @param y     the block Y coordinate
     * @param z     the block Z coordinate
     * @since 0.1.0
     */
    void scheduleUpdate(Key world, int x, int y, int z);

    /**
     * Tells the simulation a block changed, so the fluids of it and its six neighbours update.
     *
     * @param world the world key
     * @param x     the block X coordinate
     * @param y     the block Y coordinate
     * @param z     the block Z coordinate
     * @since 0.1.0
     */
    void notifyBlockChanged(Key world, int x, int y, int z);
}
