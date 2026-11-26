package fr.fidorial.world.generation;

import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldManager;
import fr.fidorial.world.WorldSpec;
import net.kyori.adventure.key.Key;

import java.util.Optional;
import java.util.Set;

/**
 * Builds and finds the {@linkplain NoiseTerrain noise terrains} the server generates its worlds with.
 *
 * <p>The server provides it as a {@linkplain ServiceRegistry service}. The overworld is generated from the
 * {@code minecraft:overworld} settings and biome preset unless a plugin replaces its {@link WorldGenerator}.</p>
 *
 * @since 0.1.0
 */
public interface NoiseTerrains {

    /**
     * {@return the keys of the noise settings a terrain can be built from}
     *
     * @since 0.1.0
     */
    Set<Key> settings();

    /**
     * {@return the keys of the biome presets a terrain can be built from}
     *
     * @since 0.1.0
     */
    Set<Key> biomePresets();

    /**
     * {@return the terrain a world is generated from, empty for a world shaped by any other generator}
     *
     * @param world the world
     * @since 0.1.0
     */
    Optional<NoiseTerrain> of(World world);

    /**
     * Builds a terrain, to query it without generating any world. Terrains built twice from the same arguments are
     * shared.
     *
     * @param settings    the noise settings, one of {@link #settings()}
     * @param biomePreset the biome preset, one of {@link #biomePresets()}
     * @param seed        the seed
     * @return the terrain
     * @throws IllegalArgumentException if the settings or the preset are unknown
     * @since 0.1.0
     */
    NoiseTerrain create(Key settings, Key biomePreset, long seed);

    /**
     * Gives a generator shaping the worlds it is given to from noise settings, to be passed to
     * {@link WorldManager#create(WorldSpec)}: each world is then generated from its own seed. Registered as the
     * default {@link WorldGenerator} service instead, it generates from the seed of the save.
     *
     * @param settings    the noise settings, one of {@link #settings()}
     * @param biomePreset the biome preset, one of {@link #biomePresets()}
     * @return the generator
     * @throws IllegalArgumentException if the settings or the preset are unknown
     * @since 0.1.0
     */
    WorldGenerator generator(Key settings, Key biomePreset);
}
