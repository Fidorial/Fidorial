package fr.fidorial.world.generation;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;

/**
 * The land a world grows from its noise settings and its seed: the shape of the ground and of the seas, the biomes,
 * the caves carved through them, then the ores, plants and trees laid over it.
 *
 * <p>Every query is answered by the terrain alone, without loading the chunks of a world, and always gives the same
 * answer for the same seed. All methods are thread-safe.</p>
 *
 * @see NoiseTerrains
 * @since 0.1.0
 */
public interface NoiseTerrain {

    /**
     * {@return the seed every noise of this terrain derives from}
     *
     * @since 0.1.0
     */
    long seed();

    /**
     * {@return the noise settings shaping this terrain, such as {@code minecraft:overworld} or {@code minecraft:amplified}}
     *
     * @since 0.1.0
     */
    Key settings();

    /**
     * {@return the biome table choosing the biomes from the climate, such as {@code minecraft:overworld}}
     *
     * @since 0.1.0
     */
    Key biomePreset();

    /**
     * {@return the lowest block height of the terrain}
     *
     * @since 0.1.0
     */
    int minY();

    /**
     * {@return the number of block layers of the terrain, from {@link #minY()}}
     *
     * @since 0.1.0
     */
    int height();

    /**
     * {@return the height under which the empty spaces of the ground are flooded}
     *
     * @since 0.1.0
     */
    int seaLevel();

    /**
     * {@return the biome at a block}
     *
     * @param x the block x
     * @param y the block y
     * @param z the block z
     * @since 0.1.0
     */
    TypedKey<BiomeDefinition> biomeAt(int x, int y, int z);

    /**
     * Gives the height of the ground of a column, once shaped and carved but before anything grows on it: the height
     * just above its highest block that is neither air nor a liquid.
     *
     * @param x the block x
     * @param z the block z
     * @return the height of the first block above the ground, {@link #minY()} for a column without ground
     * @since 0.1.0
     */
    int surfaceHeight(int x, int z);

    /**
     * Evaluates one of the density functions of the world generation data at a block.
     *
     * @param function the key of the function, such as {@code minecraft:overworld/continents}
     * @param x        the block x
     * @param y        the block y
     * @param z        the block z
     * @return the value of the function
     * @throws IllegalArgumentException if no function has that key
     * @since 0.1.0
     */
    double density(Key function, int x, int y, int z);
}
