package fr.euphyllia.fidorial.server.world.structure.worldgen;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;

public interface TerrainView {

    long seed();

    int minY();

    int height();

    int worldSurface(int x, int z);

    int oceanFloor(int x, int z);

    TypedKey<BiomeDefinition> biome(int x, int y, int z);

    default int height(final Heightmap heightmap, final int x, final int z) {
        return heightmap.ignoresFluids() ? oceanFloor(x, z) : worldSurface(x, z);
    }

    default int maxY() {
        return minY() + height() - 1;
    }
}
