package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.world.dimension.DimensionTypeDefinition;

public interface ChunkGenerator {

    ChunkColumn generate(int chunkX, int chunkZ);

    ChunkGeneratorConfig describeForSave();

    DimensionTypeDefinition dimensionType();

    default boolean generatesStructures() {
        return true;
    }
}
