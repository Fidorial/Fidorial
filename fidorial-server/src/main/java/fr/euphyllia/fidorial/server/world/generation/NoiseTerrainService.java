package fr.euphyllia.fidorial.server.world.generation;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.fidorial.registry.keys.DimensionTypeKeys;
import fr.fidorial.world.World;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.generation.NoiseTerrain;
import fr.fidorial.world.generation.NoiseTerrains;
import fr.fidorial.world.generation.WorldGenerator;
import net.kyori.adventure.key.Key;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

/**
 * Builds the noise generators of the server, and the terrains plugins query.
 *
 * <p>Each world gets a generator of its own, since a generator remembers which chunks it decorated. The terrains
 * built only to be queried are shared and a few are kept.</p>
 */
public final class NoiseTerrainService implements NoiseTerrains {

    /**
     * The noise settings and biome preset of the overworld of a new save.
     */
    public static final Key OVERWORLD = Key.key("overworld");

    private static final Key NETHER_SETTINGS = Key.key("nether");
    private static final Key END_SETTINGS = Key.key("end");
    private static final int KEPT_TERRAINS = 4;

    private final NoiseChunkGenerator.Environment environment;
    private final Function<Key, DimensionTypeDefinition> dimensionTypes;
    private final LongSupplier saveSeed;
    private final Set<Key> settings;
    private final Set<Key> presets;
    private final Map<Shared, NoiseTerrainView> shared = new LinkedHashMap<>(KEPT_TERRAINS, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(final Map.Entry<Shared, NoiseTerrainView> eldest) {
            return size() > KEPT_TERRAINS;
        }
    };

    private record Shared(Key settings, Key preset, long seed) {
    }

    /**
     * @param environment    what the generators read from the server
     * @param dimensionTypes the definition of a dimension type, as the registries hold it
     * @param saveSeed       the seed of the save, for generators given no seed of their own
     */
    public NoiseTerrainService(final NoiseChunkGenerator.Environment environment,
                               final Function<Key, DimensionTypeDefinition> dimensionTypes, final LongSupplier saveSeed) {
        this.environment = environment;
        this.dimensionTypes = dimensionTypes;
        this.saveSeed = saveSeed;
        this.settings = keys(environment.data(), WorldgenData.Section.NOISE_SETTINGS);
        this.presets = keys(environment.data(), WorldgenData.Section.BIOME_PARAMETERS);
    }

    private static Set<Key> keys(final WorldgenData data, final WorldgenData.Section section) {
        return data.ids(section).stream().map(Key::key).collect(Collectors.toUnmodifiableSet());
    }

    long saveSeed() {
        return saveSeed.getAsLong();
    }

    /**
     * {@return whether a generator can be built from these settings and this preset}
     */
    public boolean knows(final Key settingsKey, final Key presetKey) {
        return settings.contains(settingsKey) && presets.contains(presetKey);
    }

    private void check(final Key settingsKey, final Key presetKey) {
        if (!settings.contains(settingsKey)) {
            throw new IllegalArgumentException("Unknown noise settings " + settingsKey.asString());
        }
        if (!presets.contains(presetKey)) {
            throw new IllegalArgumentException("Unknown biome preset " + presetKey.asString());
        }
    }

    /**
     * Builds a generator for one world.
     *
     * @throws IllegalArgumentException if the settings or the preset are unknown
     */
    public NoiseChunkGenerator newGenerator(final Key settingsKey, final Key presetKey, final long seed) {
        check(settingsKey, presetKey);
        return new NoiseChunkGenerator(environment, dimensionType(settingsKey), settingsKey, presetKey, seed);
    }

    DimensionTypeDefinition dimensionType(final Key settingsKey) {
        if (settingsKey.equals(NETHER_SETTINGS)) {
            return dimensionTypes.apply(DimensionTypeKeys.THE_NETHER.key());
        }
        if (settingsKey.equals(END_SETTINGS)) {
            return dimensionTypes.apply(DimensionTypeKeys.THE_END.key());
        }
        return dimensionTypes.apply(DimensionTypeKeys.OVERWORLD.key());
    }

    @Override
    public Set<Key> settings() {
        return settings;
    }

    @Override
    public Set<Key> biomePresets() {
        return presets;
    }

    @Override
    public Optional<NoiseTerrain> of(final World world) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            return Optional.empty();
        }
        final NoiseChunkGenerator generator = serverWorld.generator.noise();
        return generator == null ? Optional.empty() : Optional.of(new NoiseTerrainView(generator));
    }

    @Override
    public NoiseTerrain create(final Key settingsKey, final Key presetKey, final long seed) {
        check(settingsKey, presetKey);
        final Shared key = new Shared(settingsKey, presetKey, seed);
        synchronized (shared) {
            final NoiseTerrainView known = shared.get(key);
            if (known != null) {
                return known;
            }
        }
        final NoiseTerrainView built = new NoiseTerrainView(newGenerator(settingsKey, presetKey, seed));
        synchronized (shared) {
            return shared.computeIfAbsent(key, _ -> built);
        }
    }

    @Override
    public WorldGenerator generator(final Key settingsKey, final Key presetKey) {
        check(settingsKey, presetKey);
        return new NoiseWorldGenerator(this, settingsKey, presetKey);
    }
}
