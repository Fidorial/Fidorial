package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BiomeKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.generation.GenerationDescriptor;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public final class PluginBackedChunkGenerator implements ChunkGenerator {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PluginBackedChunkGenerator.class);

    private final WorldGenerator generator;
    private final ChunkGenerator fallback;
    private final DimensionTypeDefinition dimensionType;

    public PluginBackedChunkGenerator(final WorldGenerator generator, final ChunkGenerator fallback) {
        this.generator = generator;
        this.fallback = fallback;
        this.dimensionType = generator.dimensionType();
    }

    @Override
    public ChunkColumn generate(final int chunkX, final int chunkZ) {
        final PluginGeneratedChunk chunk = new PluginGeneratedChunk(chunkX, chunkZ, dimensionType.minY(), dimensionType.height(), BiomeKeys.PLAINS);
        try {
            generator.generate(chunk);
            return chunk.column();
        } catch (final Exception e) {
            LOGGER.error(
                    "The World Generator {} failed for the chunk ({}, {}), returning to the fallback generator",
                    generator.getClass().getName(),
                    chunkX,
                    chunkZ,
                    e);
            return fallback.generate(chunkX, chunkZ);
        }
    }

    @Override
    public ChunkGeneratorConfig describeForSave() {
        return switch (generator.describeForSave()) {
            case GenerationDescriptor.Flat(final TypedKey<BlockType> floorBlock, final int floorThickness, final TypedKey<BiomeDefinition> biome) ->
                    new ChunkGeneratorConfig.Flat(
                            BlockState.withoutProperties(floorBlock), floorThickness, biome);
            case GenerationDescriptor.Noise(final Key settings, final Key biomeSourcePreset) ->
                    new ChunkGeneratorConfig.Noise(settings, biomeSourcePreset);
            case GenerationDescriptor.Unknown _ -> fallback.describeForSave();
        };
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }

    @Override
    public boolean generatesStructures() {
        return generator.generatesStructures();
    }
}
