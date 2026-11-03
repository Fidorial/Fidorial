package fr.euphyllia.fidorial.testplugin.terrain;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.dimension.types.VanillaDimensionTypes;
import fr.fidorial.world.generation.GeneratedChunk;
import fr.fidorial.world.generation.GenerationDescriptor;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;

public final class HillsGenerator implements WorldGenerator {

    private static final TypedKey<BlockType> BEDROCK = TypedKey.withRegistry(RegistryKey.BLOCK, "bedrock");
    private static final TypedKey<BlockType> STONE = TypedKey.withRegistry(RegistryKey.BLOCK, "stone");
    private static final TypedKey<BlockType> DIRT = TypedKey.withRegistry(RegistryKey.BLOCK, "dirt");
    private static final TypedKey<BlockType> GRASS_BLOCK = TypedKey.withRegistry(RegistryKey.BLOCK, "grass_block");
    private static final TypedKey<BlockType> SAND = TypedKey.withRegistry(RegistryKey.BLOCK, "sand");
    private static final TypedKey<BlockType> WATER = TypedKey.withRegistry(RegistryKey.BLOCK, "water");

    private static final TypedKey<BiomeDefinition> BIOME_PLAINS = TestBiomes.VOLCANIC_PLAINS.key();
    private static final TypedKey<BiomeDefinition> BIOME_BEACH = TestBiomes.CRYSTAL_SHORE.key();
    private static final TypedKey<BiomeDefinition> BIOME_RIVER = TestBiomes.TOXIC_RIVER.key();
    private static final TypedKey<BiomeDefinition> BIOME_PEAKS = TestBiomes.FROZEN_PEAKS.key();

    /**
     * Hauteur de base du terrain.
     */
    private final int baseHeight;
    /**
     * Amplitude maximale des collines au-dessus/en-dessous de la base.
     */
    private final int amplitude;
    /**
     * Niveau de la mer : tout creux sous ce niveau est rempli d'eau.
     */
    private final int seaLevel;

    /**
     * Au-dessus de cette altitude la surface bascule sur les pics gelees.
     */
    private final int highlandLevel;

    private final PerlinNoise heightNoise;
    private final PerlinNoise detailNoise;

    private final DimensionTypeDefinition dimensionType;

    public HillsGenerator(final long seed, final int baseHeight, final int amplitude, final int seaLevel) {
        this(seed, baseHeight, amplitude, seaLevel, VanillaDimensionTypes.OVERWORLD);
    }

    public HillsGenerator(final long seed, final int baseHeight, final int amplitude, final int seaLevel,
                          final DimensionTypeDefinition dimensionType) {
        this.baseHeight = baseHeight;
        this.amplitude = amplitude;
        this.seaLevel = seaLevel;
        this.highlandLevel = baseHeight + amplitude / 2;
        this.heightNoise = new PerlinNoise(seed);
        this.detailNoise = new PerlinNoise(seed * 31 + 7);
        this.dimensionType = dimensionType;
    }

    @Override
    public void generate(final GeneratedChunk chunk) {
        final int minY = chunk.minY();
        final int baseX = chunk.chunkX() << 4;
        final int baseZ = chunk.chunkZ() << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int worldX = baseX + x;
                final int worldZ = baseZ + z;
                final int surface = surfaceHeight(worldX, worldZ);
                final boolean underWater = surface < seaLevel;

                // socle
                chunk.setBlock(x, minY, z, BEDROCK);

                // pierre jusqu'a 4 blocs sous la surface
                for (int y = minY + 1; y <= surface - 4; y++) {
                    chunk.setBlock(x, y, z, STONE);
                }

                // couche superficielle : sable pres de l'eau, sinon terre + herbe
                if (underWater || surface <= seaLevel + 1) {
                    for (int y = Math.max(minY + 1, surface - 3); y <= surface; y++) {
                        chunk.setBlock(x, y, z, SAND);
                    }
                } else {
                    for (int y = Math.max(minY + 1, surface - 3); y < surface; y++) {
                        chunk.setBlock(x, y, z, DIRT);
                    }
                    chunk.setBlock(x, surface, z, GRASS_BLOCK);
                }

                // eau jusqu'au niveau de la mer
                for (int y = surface + 1; y <= seaLevel; y++) {
                    chunk.setBlock(x, y, z, WATER);
                }

                // biome : resolution 4x4x4, donc un seul appel par cellule
                // (coin de cellule uniquement, pour eviter 16 ecritures redondantes)
                if ((x & 3) == 0 && (z & 3) == 0) {
                    final TypedKey<BiomeDefinition> biome = underWater ? BIOME_RIVER
                            : surface <= seaLevel + 2 ? BIOME_BEACH
                            : surface >= highlandLevel ? BIOME_PEAKS
                            : BIOME_PLAINS;
                    for (int y = minY; y < minY + chunk.height(); y += 4) {
                        chunk.setBiome(x, y, z, biome);
                    }
                }
            }
        }
    }

    /**
     * Hauteur de surface pour une colonne monde donnee.
     */
    private int surfaceHeight(final int worldX, final int worldZ) {
        final double hills = heightNoise.fbm(worldX * 0.004, worldZ * 0.004, 4);
        final double detail = detailNoise.fbm(worldX * 0.02, worldZ * 0.02, 2);
        return baseHeight + (int) Math.round(hills * amplitude + detail * 4);
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }

    @Override
    public GenerationDescriptor describeForSave() {
        return GenerationDescriptor.noise(dimensionType.key(), Key.key("fidorial", "hills"));
    }
}
