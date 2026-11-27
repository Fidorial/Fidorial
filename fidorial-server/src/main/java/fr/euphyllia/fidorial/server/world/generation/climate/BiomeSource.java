package fr.euphyllia.fidorial.server.world.generation.climate;

import fr.euphyllia.fidorial.server.world.generation.density.DensityContext;

/**
 * How a generator picks the biome of each cell of 4 by 4 by 4 blocks: from a parameter list of climates, or from
 * the islands of the End. Immutable, safe to share between threads; the samplers it hands out are not.
 */
public sealed interface BiomeSource permits MultiNoiseBiomes, EndBiomes {

    /**
     * {@return a sampler for the calling thread}
     */
    Sampler newSampler();

    /**
     * Picks the biomes of cells for one thread.
     */
    @FunctionalInterface
    interface Sampler {

        /**
         * {@return the number of the biome of a cell, in the biome table of the generator}
         *
         * @param context the caches of the calling thread
         * @param quartX  the cell, in quarters of a chunk
         * @param quartY  the same along y
         * @param quartZ  the same along z
         */
        char biomeAt(DensityContext context, int quartX, int quartY, int quartZ);
    }
}
