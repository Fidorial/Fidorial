package fr.euphyllia.fidorial.server.world.structure.gen;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeTable;
import fr.euphyllia.fidorial.server.world.generation.terrain.GenChunk;
import fr.euphyllia.fidorial.server.world.generation.terrain.NoiseBiomeGrid;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BiomeKeys;
import fr.fidorial.world.biome.BiomeDefinition;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ChunkSummary {

    private static final Map<TypedKey<BiomeDefinition>, Short> BIOME_IDS = new ConcurrentHashMap<>();
    private static final List<TypedKey<BiomeDefinition>> BIOMES = new CopyOnWriteArrayList<>();
    private static final Object BIOMES_LOCK = new Object();
    private static final TypedKey<BiomeDefinition> PLAINS = BiomeKeys.PLAINS;

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
                    final TypedKey<BiomeDefinition> biome = column.getBiome(cellX << 2, y, cellZ << 2);
                    summary.biomes[(cellY << 4) | (cellZ << 2) | cellX] = biomeId(biome == null ? PLAINS : biome);
                }
            }
        }
        return summary;
    }

    /**
     * {@return the summary of the terrain of a chunk as the noise shaped it, before any feature}: what the
     * structures read to stand on the ground, as the {@code _WG} heightmaps of the game do. The blocks and biomes
     * read the same way as {@link #of(ChunkColumn)} on the column this terrain becomes, its features left out.
     *
     * @param biomes the biomes the terrain is numbered with
     * @param minY   the lowest block of the dimension
     * @param height how many blocks the dimension is high
     */
    public static ChunkSummary of(final GenChunk chunk, final BiomeTable biomes, final int minY, final int height) {
        final ChunkSummary summary = new ChunkSummary(minY, height);
        final int top = Math.min(minY + height, chunk.minY() + chunk.height()) - 1;
        final int bottom = Math.max(minY, chunk.minY());
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int surface = minY;
                int floor = minY;
                boolean surfaceFound = false;
                int y = top;
                while (y >= bottom) {
                    final int section = (y - chunk.minY()) >> 4;
                    // the generator numbers air first: a section of air alone holds nothing to find
                    if (chunk.sectionData(section) == null && chunk.uniformId(section) == 0) {
                        y = chunk.minY() + (section << 4) - 1;
                        continue;
                    }
                    final byte kind = TerrainClassifier.classify(chunk.get(x, y, z));
                    if (kind != TerrainClassifier.IGNORED) {
                        if (!surfaceFound) {
                            surface = y + 1;
                            surfaceFound = true;
                        }
                        if (kind == TerrainClassifier.GROUND) {
                            floor = y + 1;
                            break;
                        }
                    }
                    y--;
                }
                summary.surface[z << 4 | x] = (short) surface;
                summary.floor[z << 4 | x] = (short) floor;
            }
        }
        final NoiseBiomeGrid grid = chunk.biomes();
        // the biome of the sections the column gets outside the terrain
        final TypedKey<BiomeDefinition> fallback = biomes.get(grid.sectionBiome(0)).key();
        final int baseX = chunk.chunkX() << 4;
        final int baseZ = chunk.chunkZ() << 4;
        for (int cellY = 0; cellY < summary.cellsY; cellY++) {
            final int y = minY + (cellY << 2);
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                for (int cellX = 0; cellX < 4; cellX++) {
                    final TypedKey<BiomeDefinition> biome = chunk.contains(y)
                            ? biomes.get(grid.biome(baseX + (cellX << 2), y, baseZ + (cellZ << 2))).key()
                            : fallback;
                    summary.biomes[(cellY << 4) | (cellZ << 2) | cellX] = biomeId(biome);
                }
            }
        }
        return summary;
    }

    private static short biomeId(final TypedKey<BiomeDefinition> biome) {
        final Short existing = BIOME_IDS.get(biome);
        if (existing != null) {
            return existing;
        }
        synchronized (BIOMES_LOCK) {
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

    public TypedKey<BiomeDefinition> biome(final int localX, final int y, final int localZ) {
        final int cellY = Math.clamp((y - minY) >> 2, 0, cellsY - 1);
        return BIOMES.get(biomes[(cellY << 4) | (((localZ & 15) >> 2) << 2) | ((localX & 15) >> 2)]);
    }
}
