package fr.fidorial.scheduler;

import net.kyori.adventure.key.Key;

/**
 * Work run on every tick of every region.
 *
 * <p>A region owns several sections: the handler is called once per section of the region, one
 * after the other, on the region thread. Each section counts its own ticks, and keeps counting when
 * it moves to another region through a merge or a split, so data stored per section never has to
 * move.</p>
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface RegionTickHandler {

    /**
     * Ticks one section of the region the calling thread is ticking.
     *
     * @param world       the key of the world of the section
     * @param sectionX    the X coordinate of the section
     * @param sectionZ    the Z coordinate of the section
     * @param currentTick the number of ticks of this section, this one included
     * @since 0.1.0
     */
    void tick(Key world, int sectionX, int sectionZ, long currentTick);
}
