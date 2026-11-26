package fr.euphyllia.fidorial.server.world.generation.climate;

import fr.euphyllia.fidorial.server.world.generation.noise.SimplexNoise;
import fr.euphyllia.fidorial.server.world.generation.random.LegacyRandom;

/**
 * The temperature of a biome at a given block: frozen biomes have warmer patches, and the air gets colder with
 * the height above the snow line. Decides where water freezes, where snow falls and where icebergs melt a little.
 *
 * <p>The noises come from fixed seeds: the same block is always as cold, whatever the world. Immutable.</p>
 */
public final class BiomeClimate {

    private static final float SNOW_BELOW = 0.15F;
    private static final float ICEBERG_MELTS_ABOVE = 0.1F;
    private static final int SNOW_LINE_ABOVE_SEA = 17;

    private final SimplexNoise heightNoise;
    private final SimplexNoise patchWide;
    private final SimplexNoise patchMedium;
    private final SimplexNoise patchNarrow;
    private final SimplexNoise patchEdge;

    public BiomeClimate() {
        this.heightNoise = new SimplexNoise(new LegacyRandom(1234L), 0.0);
        final LegacyRandom patches = new LegacyRandom(3456L);
        this.patchWide = new SimplexNoise(patches, 0.0);
        this.patchMedium = new SimplexNoise(patches, 0.0);
        this.patchNarrow = new SimplexNoise(patches, 0.0);
        this.patchEdge = new SimplexNoise(new LegacyRandom(2345L), 0.0);
    }

    /**
     * {@return the noise of the frozen patches: three octaves, the widest weighing the most}
     */
    private float patches(final double x, final double z) {
        float value = 0.0F;
        value += 0.14285715F * patchWide.sample(x, z);
        value += 0.2857143F * patchMedium.sample(x * 0.5, z * 0.5);
        value += 0.5714286F * patchNarrow.sample(x * 0.25, z * 0.25);
        return value;
    }

    private float baseTemperature(final BiomeInfo biome, final int x, final int z) {
        if (!biome.frozen()) {
            return biome.temperature();
        }
        final double wide = patches(x * 0.05, z * 0.05) * 7.0F;
        final double edge = patchEdge.sample(x * 0.2, z * 0.2);
        if (wide + edge < 0.3 && patchEdge.sample(x * 0.09, z * 0.09) < 0.8) {
            return 0.2F;
        }
        return biome.temperature();
    }

    /**
     * {@return the temperature of a biome at a block}
     */
    public float temperature(final BiomeInfo biome, final int x, final int y, final int z, final int seaLevel) {
        final float base = baseTemperature(biome, x, z);
        final int snowLine = seaLevel + SNOW_LINE_ABOVE_SEA;
        if (y > snowLine) {
            final float variation = heightNoise.sample(x / 8.0F, z / 8.0F) * 8.0F;
            return base - (variation + y - snowLine) * 0.05F / 40.0F;
        }
        return base;
    }

    /**
     * {@return {@code true} where snow falls rather than rain}
     */
    public boolean coldEnoughToSnow(final BiomeInfo biome, final int x, final int y, final int z, final int seaLevel) {
        return temperature(biome, x, y, z, seaLevel) < SNOW_BELOW;
    }

    /**
     * {@return the noise that groups vegetation into patches: the same noise as the edge of the frozen patches,
     * read at any scale}
     */
    public double vegetationNoise(final double x, final double z) {
        return patchEdge.sample(x, z);
    }

    /**
     * {@return {@code true} where icebergs are lowered a little}
     */
    public boolean meltsIcebergs(final BiomeInfo biome, final int x, final int z, final int seaLevel) {
        return temperature(biome, x, seaLevel, z, seaLevel) > ICEBERG_MELTS_ABOVE;
    }
}
