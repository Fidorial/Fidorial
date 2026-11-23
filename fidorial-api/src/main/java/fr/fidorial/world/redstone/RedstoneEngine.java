package fr.fidorial.world.redstone;

import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.World;

/**
 * The redstone simulation, registered as a {@linkplain ServiceRegistry service}: how signals are
 * resolved and the order block updates spread in. The ticks blocks schedule belong to the world,
 * whatever the engine.
 *
 * @since 0.1.0
 */
public interface RedstoneEngine {

    /**
     * Gets the redstone of a world: its signals, and what changes them.
     *
     * @param world a loaded world
     * @return the redstone view of the world
     * @throws IllegalArgumentException if the world is not one the engine can drive
     * @since 0.1.0
     */
    RedstoneWorld world(World world);
}
