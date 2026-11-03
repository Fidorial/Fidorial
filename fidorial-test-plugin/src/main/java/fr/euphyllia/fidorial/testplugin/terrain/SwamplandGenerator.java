package fr.euphyllia.fidorial.testplugin.terrain;

import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.generation.GeneratedChunk;
import fr.fidorial.world.generation.GenerationDescriptor;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;

public final class SwamplandGenerator implements WorldGenerator {

    private static final TypedKey<BlockType> BEDROCK = TypedKey.withRegistry(RegistryKey.BLOCK, "bedrock");
    private static final TypedKey<BlockType> STONE = TypedKey.withRegistry(RegistryKey.BLOCK, "stone");
    private static final TypedKey<BlockType> DEEPSLATE = TypedKey.withRegistry(RegistryKey.BLOCK, "deepslate");
    private static final TypedKey<BlockType> COBBLESTONE = TypedKey.withRegistry(RegistryKey.BLOCK, "cobblestone");
    private static final TypedKey<BlockType> GRAVEL = TypedKey.withRegistry(RegistryKey.BLOCK, "gravel");
    private static final TypedKey<BlockType> COARSE_DIRT = TypedKey.withRegistry(RegistryKey.BLOCK, "coarse_dirt");
    private static final TypedKey<BlockType> ASH = TypedKey.withRegistry(RegistryKey.BLOCK, "gray_concrete_powder");
    private static final TypedKey<BlockType> SCORCHED_ROCK = TypedKey.withRegistry(RegistryKey.BLOCK, "blackstone");
    private static final TypedKey<BlockType> CRACKED_STONE_BRICKS = TypedKey.withRegistry(RegistryKey.BLOCK, "cracked_stone_bricks");
    private static final TypedKey<BlockType> CRACKED_DEEPSLATE_BRICKS = TypedKey.withRegistry(RegistryKey.BLOCK, "cracked_deepslate_bricks");
    private static final TypedKey<BlockType> WATER = TypedKey.withRegistry(RegistryKey.BLOCK, "water");
    private static final TypedKey<BlockType> LAVA = TypedKey.withRegistry(RegistryKey.BLOCK, "lava");
    private static final TypedKey<BlockType> MAGMA_BLOCK = TypedKey.withRegistry(RegistryKey.BLOCK, "magma_block");
    private static final TypedKey<BlockType> BASALT = TypedKey.withRegistry(RegistryKey.BLOCK, "basalt");
    private static final TypedKey<BlockType> DEAD_BUSH = TypedKey.withRegistry(RegistryKey.BLOCK, "dead_bush");
    private static final TypedKey<BlockType> WITHER_ROSE = TypedKey.withRegistry(RegistryKey.BLOCK, "wither_rose");
    private static final TypedKey<BlockType> IRON_BLOCK = TypedKey.withRegistry(RegistryKey.BLOCK, "iron_block");
    private static final TypedKey<BlockType> CHAIN = TypedKey.withRegistry(RegistryKey.BLOCK, "chain");
    private static final TypedKey<BlockType> SEA_LANTERN = TypedKey.withRegistry(RegistryKey.BLOCK, "sea_lantern");
    private static final TypedKey<BlockType> MUSHROOM_STEM = TypedKey.withRegistry(RegistryKey.BLOCK, "mushroom_stem");
    private static final TypedKey<BlockType> RED_MUSHROOM_BLOCK = TypedKey.withRegistry(RegistryKey.BLOCK, "red_mushroom_block");
    private static final TypedKey<BlockType> BROWN_MUSHROOM_BLOCK = TypedKey.withRegistry(RegistryKey.BLOCK, "brown_mushroom_block");
    private static final TypedKey<BlockType> RED_MUSHROOM = TypedKey.withRegistry(RegistryKey.BLOCK, "red_mushroom");
    private static final TypedKey<BlockType> BROWN_MUSHROOM = TypedKey.withRegistry(RegistryKey.BLOCK, "brown_mushroom");

    private static final TypedKey<BiomeDefinition> BIOME_CRATER = TestBiomes.TOXIC_CRATERS.key();
    private static final TypedKey<BiomeDefinition> BIOME_MOLTEN = TestBiomes.MOLTEN_CRATERS.key();
    private static final TypedKey<BiomeDefinition> BIOME_RUBBLE_FLATS = TestBiomes.RUBBLE_FLATS.key();
    private static final TypedKey<BiomeDefinition> BIOME_WASTELAND = TestBiomes.ASHEN_WASTELAND.key();
    private static final TypedKey<BiomeDefinition> BIOME_RIDGES = TestBiomes.IRRADIATED_RIDGES.key();

    private final int baseHeight;
    private final int reliefAmplitude;
    private final int seaLevel;
    private final int ridgeHeight;
    private final int craterDepth;

    private static final double RIDGE_THRESHOLD = 0.74;
    private static final double CRATER_THRESHOLD = 0.6;
    private static final double POOL_THRESHOLD = 0.80;
    private static final double POOL_THRESHOLD_FOOT_BONUS = 0.14;
    private static final double POOL_FOOT_RANGE_FRACTION = 0.22;
    private static final double LAVA_THRESHOLD = 0.52;
    private static final double FOOTHILL_FRACTION = 0.22;
    private final int ashLine;
    private static final double ASH_BLEND_HALF_RANGE_FRACTION = 0.12;
    private static final int STONE_BLEND_MIN_DEPTH = 2;
    private static final int STONE_BLEND_MAX_DEPTH = 6;

    private final PerlinNoise terrainNoise;
    private final PerlinNoise ridgeNoise;
    private final PerlinNoise craterNoise;
    private final PerlinNoise detailNoise;
    private final PerlinNoise hazardNoise;
    private final PerlinNoise poolNoise;
    private final PerlinNoise ashBlendNoise;
    private final PerlinNoise blendNoise;

    private final DimensionTypeDefinition dimensionType;

    public SwamplandGenerator(final long seed, final int baseHeight, final int reliefAmplitude,
                              final int seaLevel, final int ridgeHeight) {
        this(seed, baseHeight, reliefAmplitude, seaLevel, ridgeHeight, TestDimensionTypes.IRRADIATED_WASTELAND);
    }

    public SwamplandGenerator(final long seed, final int baseHeight, final int reliefAmplitude,
                              final int seaLevel, final int ridgeHeight,
                              final DimensionTypeDefinition dimensionType) {
        this.baseHeight = baseHeight;
        this.reliefAmplitude = reliefAmplitude;
        this.seaLevel = seaLevel;
        this.ridgeHeight = ridgeHeight;
        this.craterDepth = Math.max(4, ridgeHeight / 2);
        this.ashLine = baseHeight + (int) (ridgeHeight * 0.65);
        this.terrainNoise = new PerlinNoise(seed);
        this.ridgeNoise = new PerlinNoise(seed * 31 + 7);
        this.craterNoise = new PerlinNoise(seed * 53 + 101);
        this.detailNoise = new PerlinNoise(seed * 131 + 17);
        this.hazardNoise = new PerlinNoise(seed * 271 + 43);
        this.poolNoise = new PerlinNoise(seed * 401 + 67);
        this.ashBlendNoise = new PerlinNoise(seed * 601 + 89);
        this.blendNoise = new PerlinNoise(seed * 811 + 131);
        this.dimensionType = dimensionType;
    }

    @Override
    public void generate(final GeneratedChunk chunk) {
        final int minY = chunk.minY();
        final int maxY = minY + chunk.height() - 1;
        final int baseX = chunk.chunkX() << 4;
        final int baseZ = chunk.chunkZ() << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int worldX = baseX + x;
                final int worldZ = baseZ + z;

                final double ridge = ridgeField(worldX, worldZ);
                final boolean isRidge = ridge > RIDGE_THRESHOLD;
                final double crater = isRidge ? 0.0 : craterField(worldX, worldZ);
                int surface = surfaceHeight(worldX, worldZ, ridge, crater, maxY);

                boolean ridgePool = false;
                int poolRim = surface;
                if (isRidge) {
                    final int elevation = surface - baseHeight;
                    final double footRange = ridgeHeight * POOL_FOOT_RANGE_FRACTION;
                    final double linear = Math.clamp(1.0 - elevation / footRange, 0.0, 1.0);
                    final double footFactor = linear * linear * linear;
                    final double effectiveThreshold = POOL_THRESHOLD - footFactor * POOL_THRESHOLD_FOOT_BONUS;
                    final double pool = poolField(worldX, worldZ);
                    if (pool > effectiveThreshold) {
                        final double intensity = (pool - effectiveThreshold) / (1.0 - effectiveThreshold);
                        final int poolDepth = Math.max(
                                3, (int) Math.round(Math.pow(intensity, 1.3) * Math.min(10, ridgeHeight / 2)));
                        poolRim = surface;
                        surface -= poolDepth;
                        ridgePool = true;
                    }
                }

                final boolean underLiquid = ridgePool || surface < seaLevel;
                final boolean isLava = underLiquid && hazardField(worldX, worldZ) > LAVA_THRESHOLD;

                final boolean ashCapped;
                if (isRidge && !underLiquid) {
                    final double halfRange = ridgeHeight * ASH_BLEND_HALF_RANGE_FRACTION;
                    final double ashT = Math.clamp(
                            (surface - (ashLine - halfRange)) / (2.0 * halfRange), 0.0, 1.0);
                    if (ashT <= 0.0) {
                        ashCapped = false;
                    } else if (ashT >= 1.0) {
                        ashCapped = true;
                    } else {
                        final double patch = (ashBlendNoise.fbm(worldX * 0.04, worldZ * 0.04, 2) + 1.0) / 2.0;
                        ashCapped = patch < ashT;
                    }
                } else {
                    ashCapped = false;
                }

                chunk.setBlock(x, minY, z, BEDROCK);

                final int deepslateTop = Math.min(surface - 4, minY + (chunk.height() / 3));
                for (int y = minY + 1; y <= deepslateTop; y++) {
                    chunk.setBlock(x, y, z, DEEPSLATE);
                }
                for (int y = Math.max(minY + 1, deepslateTop + 1); y <= surface - 4; y++) {
                    chunk.setBlock(x, y, z, STONE);
                }

                if (underLiquid && isLava) {
                    placeLavaBed(chunk, x, z, worldX, worldZ, minY, surface);
                } else if (underLiquid) {
                    placeSludgeBed(chunk, x, z, minY, surface);
                } else if (isRidge) {
                    placeRidgeSurface(chunk, x, z, worldX, worldZ, minY, surface, ashCapped);
                } else if (surface <= seaLevel + 1) {
                    final int depth = blendDepth(worldX, worldZ);
                    for (int y = Math.max(minY + 1, surface - depth); y <= surface; y++) {
                        chunk.setBlock(x, y, z, ASH);
                    }
                } else {
                    final TypedKey<BlockType> topBlock = pickWastelandTop(worldX, worldZ);
                    final int depth = blendDepth(worldX, worldZ);
                    for (int y = Math.max(minY + 1, surface - depth); y < surface; y++) {
                        chunk.setBlock(x, y, z, COARSE_DIRT);
                    }
                    chunk.setBlock(x, surface, z, topBlock);
                }

                if (underLiquid) {
                    final TypedKey<BlockType> liquid = isLava ? LAVA : WATER;
                    final int ceiling = ridgePool ? poolRim : seaLevel;
                    for (int y = surface + 1; y <= ceiling; y++) {
                        chunk.setBlock(x, y, z, liquid);
                    }
                }

                if (!isRidge && !underLiquid && surface + 1 <= maxY) {
                    final double deco = hash01(worldX * 7 + 3, worldZ * 7 + 3);
                    if (deco < 0.06) {
                        chunk.setBlock(x, surface + 1, z, DEAD_BUSH);
                    } else if (deco < 0.075) {
                        chunk.setBlock(x, surface + 1, z, WITHER_ROSE);
                    } else if (deco < 0.085) {
                        final TypedKey<BlockType> scrap = hash01(worldX * 13 + 9, worldZ * 13 + 9) < 0.5 ? IRON_BLOCK : CHAIN;
                        chunk.setBlock(x, surface + 1, z, scrap);
                    } else if (deco < 0.105) {
                        final TypedKey<BlockType> mushroom =
                                hash01(worldX * 17 + 5, worldZ * 17 + 5) < 0.5 ? RED_MUSHROOM : BROWN_MUSHROOM;
                        chunk.setBlock(x, surface + 1, z, mushroom);
                    } else if (deco < 0.107) {
                        placeGiantMushroom(
                                chunk, x, z, worldX, worldZ, surface, maxY,
                                hash01(worldX * 19 + 23, worldZ * 19 + 23) < 0.5);
                    }
                }

                if (!underLiquid && surface - 1 >= minY + 1
                        && hash01(worldX * 191 + 29, worldZ * 191 + 29) < 0.009) {
                    chunk.setBlock(x, surface - 1, z, SEA_LANTERN);
                }

                if ((x & 3) == 0 && (z & 3) == 0) {
                    final TypedKey<BiomeDefinition> biome = isRidge && !underLiquid ? BIOME_RIDGES
                            : underLiquid && isLava ? BIOME_MOLTEN
                            : underLiquid ? BIOME_CRATER
                            : surface <= seaLevel + 2 ? BIOME_RUBBLE_FLATS
                            : BIOME_WASTELAND;
                    for (int y = minY; y < minY + chunk.height(); y += 4) {
                        chunk.setBiome(x, y, z, biome);
                    }
                }
            }
        }
    }

    private void placeLavaBed(final GeneratedChunk chunk, final int x, final int z,
                              final int worldX, final int worldZ, final int minY, final int surface) {
        for (int y = Math.max(minY + 1, surface - 3); y < surface; y++) {
            chunk.setBlock(x, y, z, BASALT);
        }
        final boolean molten = hash01(worldX * 251 + 41, worldZ * 251 + 41) < 0.3;
        chunk.setBlock(x, surface, z, molten ? MAGMA_BLOCK : BASALT);
    }

    private void placeSludgeBed(final GeneratedChunk chunk, final int x, final int z,
                                final int minY, final int surface) {
        for (int y = Math.max(minY + 1, surface - 3); y <= surface; y++) {
            chunk.setBlock(x, y, z, GRAVEL);
        }
    }

    private void placeRidgeSurface(final GeneratedChunk chunk, final int x, final int z,
                                   final int worldX, final int worldZ, final int minY,
                                   final int surface, final boolean ashCapped) {
        final int elevation = surface - baseHeight;
        final int depth = blendDepth(worldX, worldZ);

        if (ashCapped) {
            for (int y = Math.max(minY + 1, surface - depth); y < surface; y++) {
                chunk.setBlock(x, y, z, SCORCHED_ROCK);
            }
            chunk.setBlock(x, surface, z, ASH);
            return;
        }

        if (elevation < ridgeHeight * FOOTHILL_FRACTION) {
            for (int y = Math.max(minY + 1, surface - depth); y < surface; y++) {
                chunk.setBlock(x, y, z, COARSE_DIRT);
            }
            chunk.setBlock(x, surface, z, GRAVEL);
            return;
        }

        for (int y = Math.max(minY + 1, surface - depth); y < surface; y++) {
            chunk.setBlock(x, y, z, SCORCHED_ROCK);
        }
        chunk.setBlock(x, surface, z, pickScreeTop(worldX, worldZ));
    }

    private void placeGiantMushroom(
            final GeneratedChunk chunk, final int x, final int z, final int worldX, final int worldZ,
            final int surface, final int maxY, final boolean red) {

        final int height = 3 + (int) (hash01(worldX * 337 + 11, worldZ * 337 + 11) * 3.0);
        final int top = surface + height;
        if (top + 1 > maxY) {
            return;
        }

        for (int y = surface + 1; y < top; y++) {
            chunk.setBlock(x, y, z, MUSHROOM_STEM);
        }

        final TypedKey<BlockType> cap = red ? RED_MUSHROOM_BLOCK : BROWN_MUSHROOM_BLOCK;
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (Math.abs(dx) == 2 && Math.abs(dz) == 2) {
                    continue;
                }
                final int cx = x + dx;
                final int cz = z + dz;
                if (cx < 0 || cx > 15 || cz < 0 || cz > 15) {
                    continue;
                }
                chunk.setBlock(cx, top, cz, cap);
            }
        }
    }

    private double ridgeField(final int worldX, final int worldZ) {
        final double n = ridgeNoise.fbm(worldX * 0.0015, worldZ * 0.0015, 5);
        return 1.0 - Math.abs(n);
    }

    private double craterField(final int worldX, final int worldZ) {
        final double n = craterNoise.fbm(worldX * 0.004, worldZ * 0.004, 4);
        return 1.0 - Math.abs(n);
    }

    private double hazardField(final int worldX, final int worldZ) {
        return (hazardNoise.fbm(worldX * 0.0016, worldZ * 0.0016, 3) + 1.0) / 2.0;
    }

    private double poolField(final int worldX, final int worldZ) {
        return (poolNoise.fbm(worldX * 0.01, worldZ * 0.01, 3) + 1.0) / 2.0;
    }

    private int blendDepth(final int worldX, final int worldZ) {
        final double n = (blendNoise.fbm(worldX * 0.09, worldZ * 0.09, 2) + 1.0) / 2.0;
        return STONE_BLEND_MIN_DEPTH
                + (int) Math.round(n * (STONE_BLEND_MAX_DEPTH - STONE_BLEND_MIN_DEPTH));
    }

    private int surfaceHeight(final int worldX, final int worldZ, final double ridge,
                              final double crater, final int maxY) {
        final double relief = terrainNoise.fbm(worldX * 0.01, worldZ * 0.01, 3);
        int surface = baseHeight + (int) Math.round(relief * reliefAmplitude);

        if (ridge > RIDGE_THRESHOLD) {
            final double intensity = (ridge - RIDGE_THRESHOLD) / (1.0 - RIDGE_THRESHOLD);
            surface += (int) Math.round(Math.pow(intensity, 1.5) * ridgeHeight);
        } else if (crater > CRATER_THRESHOLD) {
            final double intensity = (crater - CRATER_THRESHOLD) / (1.0 - CRATER_THRESHOLD);
            surface -= (int) Math.round(Math.pow(intensity, 1.2) * craterDepth);
        }

        return Math.min(surface, maxY - 2);
    }

    private TypedKey<BlockType> pickWastelandTop(final int worldX, final int worldZ) {
        final double n = (detailNoise.fbm(worldX * 0.05, worldZ * 0.05, 3) + 1.0) / 2.0;
        if (n < 0.35) {
            return ASH;
        } else if (n < 0.65) {
            return COARSE_DIRT;
        } else if (n < 0.85) {
            return CRACKED_STONE_BRICKS;
        }
        return GRAVEL;
    }

    private TypedKey<BlockType> pickScreeTop(final int worldX, final int worldZ) {
        final double h = hash01(worldX * 11 + 5, worldZ * 11 + 5);
        if (h < 0.65) {
            return SCORCHED_ROCK;
        } else if (h < 0.85) {
            return CRACKED_DEEPSLATE_BRICKS;
        }
        return COBBLESTONE;
    }

    private static double hash01(final int x, final int z) {
        long h = x * 374_761_393L + z * 668_265_263L;
        h = (h ^ (h >>> 13)) * 1_274_126_177L;
        h ^= (h >>> 16);
        return (h & 0x7FFFFFFFL) / (double) Integer.MAX_VALUE;
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }

    @Override
    public GenerationDescriptor describeForSave() {
        return GenerationDescriptor.noise(dimensionType.key(), Key.key("fidorial", "wasteland"));
    }
}
