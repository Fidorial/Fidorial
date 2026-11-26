package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * Something a decoration builds around a block: a tree, an ore vein, a flower, a lake.
 */
@FunctionalInterface
public interface Feature {

    /**
     * A feature that builds nothing.
     */
    Feature NOTHING = (region, random, x, y, z) -> false;

    /**
     * Builds the feature.
     *
     * @return {@code true} if anything was built
     */
    boolean place(DecorationRegion region, BitRandom random, int x, int y, int z);
}
