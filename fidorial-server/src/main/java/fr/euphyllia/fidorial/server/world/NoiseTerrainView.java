package fr.euphyllia.fidorial.server.world.generation;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.generation.NoiseTerrain;
import net.kyori.adventure.key.Key;

/**
 * What plugins see of a noise generator.
 */
final class NoiseTerrainView implements NoiseTerrain {

    private final NoiseChunkGenerator generator;

    NoiseTerrainView(final NoiseChunkGenerator generator) {
        this.generator = generator;
    }

    private static boolean ground(final BlockState state) {
        return !state.isAir() && FluidType.byBlockKey(state.name()) == null;
    }

    @Override
    public long seed() {
        return generator.seed();
    }

    @Override
    public Key settings() {
        return generator.settingsKey();
    }

    @Override
    public Key biomePreset() {
        return generator.presetKey();
    }

    @Override
    public int minY() {
        return generator.settings().minY();
    }

    @Override
    public int height() {
        return generator.settings().height();
    }

    @Override
    public int seaLevel() {
        return generator.settings().seaLevel();
    }

    @Override
    public TypedKey<BiomeDefinition> biomeAt(final int x, final int y, final int z) {
        return generator.biomeAt(x, y, z).key();
    }

    @Override
    public int surfaceHeight(final int x, final int z) {
        return generator.terrain(x >> 4, z >> 4).top(x & 15, z & 15, NoiseTerrainView::ground);
    }

    @Override
    public double density(final Key function, final int x, final int y, final int z) {
        final String id = function.asString();
        if (!generator.environment().data().ids(WorldgenData.Section.DENSITY_FUNCTION).contains(id)) {
            throw new IllegalArgumentException("Unknown density function " + id);
        }
        final Density compiled = generator.program().named(id);
        return generator.sample(compiled, x, y, z);
    }

    @Override
    public String toString() {
        return "NoiseTerrain[" + generator.settingsKey().asString() + ", seed=" + generator.seed() + "]";
    }
}
