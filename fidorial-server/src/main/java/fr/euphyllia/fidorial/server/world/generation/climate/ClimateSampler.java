package fr.euphyllia.fidorial.server.world.generation.climate;

import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.euphyllia.fidorial.server.world.generation.density.DensityContext;

/**
 * Picks the biome of a cell of 4 by 4 by 4 blocks: the six climate functions of the noise router are read at the
 * lowest corner of the cell, and the biome table picks the biome nearest to that climate.
 *
 * <p>Immutable, safe to share; the cursor and the context come from the calling thread.</p>
 *
 * @param temperature     the temperature function
 * @param humidity        the vegetation function
 * @param continentalness the continents function
 * @param erosion         the erosion function
 * @param depth           the depth function
 * @param weirdness       the ridges function
 * @param table           the climate table of the biome source
 */
public record ClimateSampler(Density temperature, Density humidity, Density continentalness, Density erosion,
                             Density depth, Density weirdness, ClimateTable<Character> table) {

    /**
     * {@return the number of the biome of a cell}
     */
    public char biomeAt(final DensityContext context, final ClimateTable.Cursor<Character> cursor,
                        final int quartX, final int quartY, final int quartZ) {
        final int x = quartX << 2;
        final int y = quartY << 2;
        final int z = quartZ << 2;
        return cursor.find(
                ClimateTable.quantize(temperature.compute(context, x, y, z)),
                ClimateTable.quantize(humidity.compute(context, x, y, z)),
                ClimateTable.quantize(continentalness.compute(context, x, y, z)),
                ClimateTable.quantize(erosion.compute(context, x, y, z)),
                ClimateTable.quantize(depth.compute(context, x, y, z)),
                ClimateTable.quantize(weirdness.compute(context, x, y, z)));
    }
}
