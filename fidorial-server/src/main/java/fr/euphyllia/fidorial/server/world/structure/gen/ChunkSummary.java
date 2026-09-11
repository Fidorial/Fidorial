package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ChunkSummary {

    private static final Map<Key, Short> BIOME_IDS = new ConcurrentHashMap<>();
    private static final List<Key> BIOMES = new CopyOnWriteArrayList<>();
    private static final Key PLAINS = Key.key("plains");

    private final int minY;
    private final short[] surface = new short[256];
    private final short[] floor = new short[256];
    private final short[] biomes;
    private final int cellsY;

    private ChunkSummary(final int minY, final int height) {
        this.minY = minY;
        this.cellsY = Math.max(1, height >> 2);
        this.biomes = new short[16 * cellsY];
    }

    public static ChunkSummary of(final ChunkColumn column) {
        final ChunkSummary summary = new ChunkSummary(column.minY(), column.height());
        final ChunkSection[] sections = column.sections();
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int surface = column.minY();
                int floor = column.minY();
                boolean surfaceFound = false;
                scan:
                for (int i = sections.length - 1; i >= 0; i--) {
                    final ChunkSection section = sections[i];
                    if (section == null || section.isEmpty()) {
                        continue;
                    }
                    final int baseY = section.sectionY() << 4;
                    for (int ly = 15; ly >= 0; ly--) {
                        final BlockState state = section.getBlock(x, ly, z);
                        final byte kind = TerrainClassifier.classify(state);
                        if (kind == TerrainClassifier.IGNORED) {
                            continue;
                        }
                        if (!surfaceFound) {
                            surface = baseY + ly + 1;
                            surfaceFound = true;
                        }
                        if (kind == TerrainClassifier.GROUND) {
                            floor = baseY + ly + 1;
                            break scan;
                        }
                    }
                }
                summary.surface[z << 4 | x] = (short) surface;
                summary.floor[z << 4 | x] = (short) floor;
            }
        }
        for (int cellY = 0; cellY < summary.cellsY; cellY++) {
            final int y = column.minY() + (cellY << 2);
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                for (int cellX = 0; cellX < 4; cellX++) {
                    final Key biome = column.getBiome(cellX << 2, y, cellZ << 2);
                    summary.biomes[(cellY << 4) | (cellZ << 2) | cellX] = biomeId(biome == null ? PLAINS : biome);
                }
            }
        }
        return summary;
    }

    private static short biomeId(final Key biome) {
        final Short existing = BIOME_IDS.get(biome);
        if (existing != null) {
            return existing;
        }
        synchronized (BIOMES) {
            return BIOME_IDS.computeIfAbsent(biome, key -> {
                BIOMES.add(key);
                return (short) (BIOMES.size() - 1);
            });
        }
    }

    public int worldSurface(final int localX, final int localZ) {
        return surface[(localZ & 15) << 4 | (localX & 15)];
    }

    public int oceanFloor(final int localX, final int localZ) {
        return floor[(localZ & 15) << 4 | (localX & 15)];
    }

    public Key biome(final int localX, final int y, final int localZ) {
        final int cellY = Math.clamp((y - minY) >> 2, 0, cellsY - 1);
        return BIOMES.get(biomes[(cellY << 4) | (((localZ & 15) >> 2) << 2) | ((localX & 15) >> 2)]);
    }
}
