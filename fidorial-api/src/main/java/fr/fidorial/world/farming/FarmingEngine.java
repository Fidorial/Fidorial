package fr.fidorial.world.farming;

import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.World;
import net.kyori.adventure.key.Key;

/**
 * The farming simulation, registered as a {@linkplain ServiceRegistry service}: which blocks get a
 * random tick, how a block change reaches the plants around it, and what hoes and fertilizers do.
 *
 * <p>The server only ever talks to the engine in effect: it calls {@link #tick} on every game tick
 * of every region, and reports what happens in a world to the {@link FarmingWorld} the engine
 * hands out. An engine wrapping the built-in one passes on what it does not handle itself.</p>
 *
 * @since 0.1.0
 */
public interface FarmingEngine {

    /**
     * Gets the farming view of a world: what its plants read, and what changes them.
     *
     * @param world a loaded world
     * @return the farming view of the world
     * @throws IllegalArgumentException if the world is not one the engine can drive
     * @since 0.1.0
     */
    FarmingWorld world(World world);

    /**
     * Runs one game tick of farming on a region: where random ticks are given to its blocks.
     *
     * <p>Called by the server on the thread of the region, once per tick of the region, as a
     * {@link RegionTickHandler} is. It is not called while the game is frozen.</p>
     *
     * @param world       the key of the world of the region
     * @param sectionX    the X coordinate of the region, in regions
     * @param sectionZ    the Z coordinate of the region, in regions
     * @param currentTick the number of ticks the region ran
     * @since 0.1.0
     */
    void tick(Key world, int sectionX, int sectionZ, long currentTick);
}
