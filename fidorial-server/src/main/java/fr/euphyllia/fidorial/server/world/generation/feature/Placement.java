package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * One step of the placement of a feature: turns a position into none, one or several positions, moved,
 * repeated or filtered.
 */
@FunctionalInterface
public interface Placement {

    /**
     * Where the results go, as packed positions.
     */
    @FunctionalInterface
    interface Sink {
        void accept(int x, int y, int z);
    }

    void apply(PlacementRun run, BitRandom random, int x, int y, int z, Sink out);
}
