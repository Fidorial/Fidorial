package fr.euphyllia.fidorial.server.world.generation;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.generation.GeneratedChunk;
import fr.fidorial.world.generation.GenerationDescriptor;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

/**
 * The generator plugins get to create worlds shaped by noise. The server does not go through
 * {@link #generate(GeneratedChunk)}: it builds a {@link NoiseChunkGenerator} for the seed of each world.
 */
public final class NoiseWorldGenerator implements WorldGenerator {

    private final NoiseTerrainService service;
    private final Key settings;
    private final Key preset;
    private volatile @Nullable NoiseChunkGenerator forSave;

    NoiseWorldGenerator(final NoiseTerrainService service, final Key settings, final Key preset) {
        this.service = service;
        this.settings = settings;
        this.preset = preset;
    }

    /**
     * {@return a new generator for a world of its own}
     */
    public NoiseChunkGenerator forSeed(final long seed) {
        return service.newGenerator(settings, preset, seed);
    }

    /**
     * {@return the generator used with the seed of the save, built on first use}
     */
    public NoiseChunkGenerator forSave() {
        NoiseChunkGenerator known = forSave;
        if (known == null) {
            synchronized (this) {
                known = forSave;
                if (known == null) {
                    known = forSeed(service.saveSeed());
                    forSave = known;
                }
            }
        }
        return known;
    }

    /**
     * {@return a short description for the logs}
     */
    public String describe() {
        return "noise " + settings.asString() + " / " + preset.asString();
    }

    @Override
    public void generate(final GeneratedChunk chunk) {
        final ChunkColumn column = forSave().generate(chunk.chunkX(), chunk.chunkZ());
        final int minY = Math.max(chunk.minY(), column.minY());
        final int maxY = Math.min(chunk.minY() + chunk.height(), column.minY() + column.height());
        for (int y = minY; y < maxY; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    final BlockState state = column.getBlock(x, y, z);
                    if (!state.isAir()) {
                        chunk.setBlock(x, y, z, state.name(), state.properties());
                    }
                }
            }
        }
        for (int y = minY; y < maxY; y += 4) {
            for (int z = 0; z < 16; z += 4) {
                for (int x = 0; x < 16; x += 4) {
                    final TypedKey<BiomeDefinition> biome = column.getBiome(x, y, z);
                    if (biome != null) {
                        chunk.setBiome(x, y, z, biome);
                    }
                }
            }
        }
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return service.dimensionType(settings);
    }

    @Override
    public GenerationDescriptor describeForSave() {
        return GenerationDescriptor.noise(settings, preset);
    }
}
