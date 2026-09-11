package fr.euphyllia.fidorial.server.world.structure.worldgen;

import net.kyori.adventure.key.Key;

public interface TerrainView {

    long seed();

    int minY();

    int height();

    int worldSurface(int x, int z);

    int oceanFloor(int x, int z);

    Key biome(int x, int y, int z);

    default int height(final Heightmap heightmap, final int x, final int z) {
        return heightmap.ignoresFluids() ? oceanFloor(x, z) : worldSurface(x, z);
    }

    default int maxY() {
        return minY() + height() - 1;
    }
}
