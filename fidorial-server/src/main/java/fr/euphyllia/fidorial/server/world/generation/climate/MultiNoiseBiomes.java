package fr.euphyllia.fidorial.server.world.generation.climate;

/**
 * Picks each biome from the climate at the cell: the parameter list of a biome preset gives the biome whose
 * climate lies closest.
 *
 * @param climate the climate functions and the parameter list
 */
public record MultiNoiseBiomes(ClimateSampler climate) implements BiomeSource {

    @Override
    public Sampler newSampler() {
        final ClimateTable.Cursor<Character> cursor = climate.table().cursor();
        return (context, quartX, quartY, quartZ) -> climate.biomeAt(context, cursor, quartX, quartY, quartZ);
    }
}
