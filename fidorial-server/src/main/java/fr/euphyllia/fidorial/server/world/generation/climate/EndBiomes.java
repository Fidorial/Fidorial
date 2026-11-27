package fr.euphyllia.fidorial.server.world.generation.climate;

import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.euphyllia.fidorial.server.world.generation.density.DensityContext;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BiomeKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;

import java.util.List;

/**
 * The biomes of the End, picked chunk by chunk: one biome over the main island, then, outside, from the land the
 * islands give at the middle of the chunk, highlands, midlands, barrens and small islands from the most land to
 * the least. All the cells of a chunk share its biome, at any height.
 *
 * @param islands      the land function, the erosion of the noise settings
 * @param mainIsland   the number of the biome around the origin
 * @param highlands    the number of the biome of the hearts of the islands
 * @param midlands     the number of the biome of their slopes
 * @param barrens      the number of the biome of their shores
 * @param smallIslands the number of the biome over the void
 */
public record EndBiomes(Density islands, char mainIsland, char highlands, char midlands, char barrens,
                        char smallIslands) implements BiomeSource {

    /**
     * The biome preset standing for these biomes, beside the parameter lists.
     */
    public static final Key PRESET = Key.key("the_end");

    /**
     * Every biome these may give, in the order the features of a step are numbered from.
     */
    public static final List<TypedKey<BiomeDefinition>> BIOMES = List.of(BiomeKeys.THE_END, BiomeKeys.END_HIGHLANDS,
            BiomeKeys.END_MIDLANDS, BiomeKeys.SMALL_END_ISLANDS, BiomeKeys.END_BARRENS);

    // around the origin everything is the main island, out to this many chunks, squared
    private static final long MAIN_ISLAND_CHUNKS_SQ = 4096L;
    private static final double HIGHLANDS_ABOVE = 0.25;
    private static final double MIDLANDS_FROM = -0.0625;
    private static final double SMALL_ISLANDS_BELOW = -0.21875;
    private static final int HALF_CHUNK = 8;

    /**
     * {@return the biomes of the End, numbered as in a table holding {@link #BIOMES}}
     *
     * @param islands the land function
     * @param table   the biomes of the generator
     */
    public static EndBiomes of(final Density islands, final BiomeTable table) {
        return new EndBiomes(islands, number(table, BiomeKeys.THE_END), number(table, BiomeKeys.END_HIGHLANDS),
                number(table, BiomeKeys.END_MIDLANDS), number(table, BiomeKeys.END_BARRENS),
                number(table, BiomeKeys.SMALL_END_ISLANDS));
    }

    private static char number(final BiomeTable table, final TypedKey<BiomeDefinition> biome) {
        final BiomeInfo info = table.find(biome.key().asString());
        if (info == null) {
            throw new IllegalArgumentException("The biome table does not hold " + biome.key().asString());
        }
        return (char) info.index();
    }

    @Override
    public Sampler newSampler() {
        return this::biomeAt;
    }

    private char biomeAt(final DensityContext context, final int quartX, final int quartY, final int quartZ) {
        final int chunkX = quartX >> 2;
        final int chunkZ = quartZ >> 2;
        if ((long) chunkX * chunkX + (long) chunkZ * chunkZ <= MAIN_ISLAND_CHUNKS_SQ) {
            return mainIsland;
        }
        final double land = islands.compute(context, (chunkX * 2 + 1) * HALF_CHUNK, quartY << 2, (chunkZ * 2 + 1) * HALF_CHUNK);
        if (land > HIGHLANDS_ABOVE) {
            return highlands;
        }
        if (land >= MIDLANDS_FROM) {
            return midlands;
        }
        return land < SMALL_ISLANDS_BELOW ? smallIslands : barrens;
    }
}
