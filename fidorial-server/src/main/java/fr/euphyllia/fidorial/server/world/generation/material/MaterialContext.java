package fr.euphyllia.fidorial.server.world.generation.material;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeTable;
import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.euphyllia.fidorial.server.world.generation.density.DensityContext;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseField;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import fr.euphyllia.fidorial.server.world.generation.terrain.NoiseBiomeGrid;
import org.jspecify.annotations.Nullable;

/**
 * Where the material rules are evaluated: the column, the block, and what the rules know about them: how deep
 * the soil goes, how much stone lies above and below, where the water stands, which biome the block is in.
 *
 * <p>The terrain builder moves the context from column to column and from block to block. Each move bumps a
 * counter, which tells the conditions whether the value they remember still holds. One context per thread.</p>
 */
public final class MaterialContext {

    private static final int BUILD_BELOW_SURFACE = 8;

    private final MaterialSystem system;
    private DensityContext densities;
    private @Nullable NoiseBiomeGrid biomes;

    long xzStamp;
    long yStamp;
    int x;
    int z;
    int y;
    int gradientX;
    int gradientZ;
    int surfaceDepth;
    int stoneAbove;
    int stoneBelow;
    int waterHeight;

    private long secondaryStamp = -1;
    private double surfaceSecondary;
    private long minSurfaceStamp = -1;
    private int minSurfaceLevel;
    private long biomeStamp = -1;
    private @Nullable BiomeInfo biome;

    MaterialContext(final MaterialSystem system) {
        this.system = system;
    }

    /**
     * Points the context at a new chunk.
     */
    public void begin(final DensityContext densities, final NoiseBiomeGrid biomes) {
        this.densities = densities;
        this.biomes = biomes;
        xzStamp++;
        yStamp++;
    }

    /**
     * Moves to a column.
     *
     * @param gradientX how much higher the terrain is on the next column along x than on the previous one
     * @param gradientZ the same along z
     */
    public void column(final int blockX, final int blockZ, final int gradientX, final int gradientZ) {
        xzStamp++;
        yStamp++;
        this.x = blockX;
        this.z = blockZ;
        this.gradientX = gradientX;
        this.gradientZ = gradientZ;
        this.surfaceDepth = system.surfaceDepth(blockX, blockZ);
    }

    /**
     * Moves to a block of the column.
     *
     * @param stoneAbove  how many solid blocks are above, this one included, up to the first gap
     * @param stoneBelow  how many solid blocks are below, this one included, down to the first gap
     * @param waterHeight the height of the first fluid above, plus one; {@link Integer#MIN_VALUE} for none
     */
    public void block(final int stoneAbove, final int stoneBelow, final int waterHeight, final int blockY) {
        yStamp++;
        this.y = blockY;
        this.stoneAbove = stoneAbove;
        this.stoneBelow = stoneBelow;
        this.waterHeight = waterHeight;
    }

    MaterialSystem system() {
        return system;
    }

    DensityContext densities() {
        return densities;
    }

    double surfaceSecondary() {
        if (secondaryStamp != xzStamp) {
            secondaryStamp = xzStamp;
            surfaceSecondary = system.surfaceSecondary(x, z);
        }
        return surfaceSecondary;
    }

    /**
     * {@return the lowest height the surface rules start at in this column}: the surface level of the chunk,
     * plus the soil depth, minus a margin
     */
    public int minSurfaceLevel() {
        if (minSurfaceStamp != xzStamp) {
            minSurfaceStamp = xzStamp;
            final float level = system.chunkSurfaceLevel().compute(densities, x, 0, z);
            minSurfaceLevel = (int) Math.floor(level) + surfaceDepth - BUILD_BELOW_SURFACE;
        }
        return minSurfaceLevel;
    }

    BiomeInfo biome() {
        if (biomeStamp != yStamp || biome == null) {
            biomeStamp = yStamp;
            biome = system.biomes().get(biomes.biome(x, y, z));
        }
        return biome;
    }

    /**
     * The world-wide parts of the material rules: the soil noises, the clay bands of the badlands, the seeds.
     *
     * @param seaLevel          the sea level
     * @param chunkSurfaceLevel the surface level of the noise router
     * @param surfaceNoise      how deep the soil goes
     * @param secondaryNoise    a second soil noise some rules use
     * @param bandOffsetNoise   how much the clay bands bend
     * @param random            the positional factory of the world
     * @param biomes            the biomes of the generator
     * @param clayBands         the layers of the badlands, bottom first
     */
    public record MaterialSystem(int seaLevel, Density chunkSurfaceLevel, NoiseField surfaceNoise, NoiseField secondaryNoise,
                                 NoiseField bandOffsetNoise, PositionalRandom random, BiomeTable biomes, BlockState[] clayBands) {

        int surfaceDepth(final int x, final int z) {
            final double noise = surfaceNoise.sample(x, 0.0, z);
            return (int) (noise * 2.75 + 3.0 + random.at(x, 0, z).nextDouble() * 0.25);
        }

        double surfaceSecondary(final int x, final int z) {
            return secondaryNoise.sample(x, 0.0, z);
        }

        BlockState band(final int x, final int y, final int z) {
            final int offset = Math.round(bandOffsetNoise.sample(x, 0.0, z) * 4.0F);
            return clayBands[(y + offset + clayBands.length) % clayBands.length];
        }

        /**
         * {@return a new context reading this system}
         */
        public MaterialContext newContext() {
            return new MaterialContext(this);
        }
    }
}
