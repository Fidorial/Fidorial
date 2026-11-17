package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;

public sealed interface ChunkGeneratorConfig {

    record Flat(BlockState floor, int floorThickness, TypedKey<BiomeDefinition> biome) implements ChunkGeneratorConfig {
    }

    record Debug() implements ChunkGeneratorConfig {
    }

    record Noise(Key settings, Key biomeSourcePreset) implements ChunkGeneratorConfig {
    }

    /**
     * The islands of the End, shaped by the seed of the world alone: there is nothing to configure.
     */
    record TheEnd() implements ChunkGeneratorConfig {
    }
}
