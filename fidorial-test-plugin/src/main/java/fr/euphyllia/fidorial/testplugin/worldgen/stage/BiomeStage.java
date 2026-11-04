package fr.euphyllia.fidorial.testplugin.worldgen.stage;

import fr.euphyllia.fidorial.testplugin.worldgen.ChunkScratch;
import fr.euphyllia.fidorial.testplugin.worldgen.climate.BiomeTable;
import fr.euphyllia.fidorial.testplugin.worldgen.climate.ClimatePoint;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;

public final class BiomeStage {

    public void assign(final ChunkScratch scratch) {
        final int minY = scratch.minY();
        final int cellCountY = scratch.cellCountY();

        for (int cellZ = 0; cellZ < 4; cellZ++) {
            for (int cellX = 0; cellX < 4; cellX++) {
                final ClimatePoint point = scratch.climate(cellX, cellZ);
                final TypedKey<BiomeDefinition> surface = BiomeTable.surface(point);
                final int surfaceY = averageSurface(scratch, cellX, cellZ);

                for (int cellY = 0; cellY < cellCountY; cellY++) {
                    final int y = minY + (cellY << 2) + 2;
                    final TypedKey<BiomeDefinition> cave = BiomeTable.cave(point, y, surfaceY);
                    scratch.setBiomeCell(cellX, cellY, cellZ, cave != null ? cave : surface);
                }
            }
        }
    }

    private int averageSurface(final ChunkScratch scratch, final int cellX, final int cellZ) {
        long total = 0;
        int count = 0;
        for (int dz = 0; dz < 4; dz++) {
            for (int dx = 0; dx < 4; dx++) {
                final int y = scratch.surfaceY((cellX << 2) + dx, (cellZ << 2) + dz);
                if (y >= scratch.minY()) {
                    total += y;
                    count++;
                }
            }
        }
        return count == 0 ? scratch.minY() : (int) (total / count);
    }
}
