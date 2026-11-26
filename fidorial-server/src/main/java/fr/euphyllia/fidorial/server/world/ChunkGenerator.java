package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.generation.NoiseChunkGenerator;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import org.jspecify.annotations.Nullable;

public interface ChunkGenerator {

    ChunkColumn generate(int chunkX, int chunkZ);

    ChunkGeneratorConfig describeForSave();

    DimensionTypeDefinition dimensionType();

    default boolean generatesStructures() {
        return true;
    }

    /**
     * {@return the noise generator producing the chunks, through any wrapper, or {@code null} when the chunks come from elsewhere}
     */
    default @Nullable NoiseChunkGenerator noise() {
        return null;
    }
}
