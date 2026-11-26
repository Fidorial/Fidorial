package fr.euphyllia.fidorial.server.world.generation.feature;

import fr.euphyllia.fidorial.server.world.generation.NoiseChunkGenerator;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeTable;
import fr.euphyllia.fidorial.server.world.generation.data.HeightContext;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Everything the decorations of one generator share: the block states, the survival rules, the biomes with the
 * features each one lists, and the library of parsed features.
 *
 * <p>Immutable once built, apart from the caches it holds, which are thread-safe.</p>
 */
public final class DecorationContext {

    private static final Key REGION_RANDOM = Key.key("worldgen_region_random");

    private final NoiseChunkGenerator generator;
    private final StateFlags states;
    private final Survival survival;
    private final List<Set<String>> featuresByBiome;
    private final PositionalRandom regionRandoms;
    private final FeatureLibrary library;

    public DecorationContext(final NoiseChunkGenerator generator) {
        this.generator = generator;
        this.states = new StateFlags(generator.palette(), generator.environment().blocks());
        this.survival = new Survival(states);
        this.featuresByBiome = new ArrayList<>();
        for (final BiomeInfo biome : generator.biomes().all()) {
            final Set<String> features = new HashSet<>();
            for (final List<String> step : biome.features()) {
                for (final String feature : step) {
                    features.addAll(FeatureOrder.expand(generator.environment().data(), feature));
                }
            }
            featuresByBiome.add(Set.copyOf(features));
        }
        this.regionRandoms = generator.worldRandom().named(REGION_RANDOM).splitPositional();
        this.library = new FeatureLibrary(this);
    }

    public NoiseChunkGenerator generator() {
        return generator;
    }

    public StateFlags states() {
        return states;
    }

    public Survival survival() {
        return survival;
    }

    public BlockLookup blocks() {
        return generator.environment().blocks();
    }

    public WorldgenData data() {
        return generator.environment().data();
    }

    public HeightContext heights() {
        return generator.heights();
    }

    public BiomeTable biomes() {
        return generator.biomes();
    }

    public PositionalRandom regionRandoms() {
        return regionRandoms;
    }

    public FeatureLibrary library() {
        return library;
    }

    /**
     * {@return {@code true} if a biome lists a placed feature, in any step}
     */
    public boolean biomeHasFeature(final BiomeInfo biome, final String placedFeature) {
        return featuresByBiome.get(biome.index()).contains(placedFeature);
    }

    /**
     * {@return the noise of the vegetation patches, the same in every world}
     */
    public double vegetationNoise(final double x, final double z) {
        return generator.biomeClimate().vegetationNoise(x, z);
    }
}
