package fr.euphyllia.fidorial.server.world.generation.end;

import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.ChunkGenerator;
import fr.euphyllia.fidorial.server.world.ChunkGeneratorConfig;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.generation.biome.JitteredBiomeGrid;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BiomeKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.dimension.DimensionTypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the End of a world from its seed: the islands, in end stone, and their biomes.
 */
@NeedsToBeRevisited("Only the bare land is made. Nothing is placed on it: no obsidian pillars, no plants, no structures of its own.")
public final class EndChunkGenerator implements ChunkGenerator {

    // around the origin everything is one biome, out to this many chunks, squared
    private static final long MAIN_ISLAND_CHUNKS_SQ = 4096L;
    private static final double HIGHLANDS_ABOVE = 0.25;
    private static final double MIDLANDS_FROM = -0.0625;
    private static final double OPEN_VOID_BELOW = -0.21875;
    private static final int CHUNK_CENTRE = 8;
    private static final int CHUNK_WIDTH = 16;
    // a block takes its biome from grid points that may lie in the chunks next to its own
    private static final int NEIGHBOURHOOD = 3;

    private final DimensionTypeDefinition dimensionType;
    private final EndTerrain terrain;
    private final JitteredBiomeGrid biomeGrid;
    private final BlockState air;
    private final BlockState endStone;

    /**
     * @param dimensionType the dimension type of the world the chunks are made for
     * @param seed          the seed of the world; the same seed always gives the same End
     */
    public EndChunkGenerator(final DimensionTypeDefinition dimensionType, final long seed) {
        this.dimensionType = dimensionType;
        this.terrain = new EndTerrain(seed);
        this.biomeGrid = new JitteredBiomeGrid(seed);
        this.air = BlockState.withoutProperties(BlockTypeKeys.AIR);
        this.endStone = BlockState.withoutProperties(BlockTypeKeys.END_STONE);
    }

    private static int neighbour(final int offsetX, final int offsetZ) {
        return (offsetX + 1) * NEIGHBOURHOOD + offsetZ + 1;
    }

    @Override
    public ChunkColumn generate(final int chunkX, final int chunkZ) {
        final List<TypedKey<BiomeDefinition>> around = new ArrayList<>(NEIGHBOURHOOD * NEIGHBOURHOOD);
        for (int offsetX = -1; offsetX <= 1; offsetX++) {
            for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                around.add(biomeOf(chunkX + offsetX, chunkZ + offsetZ));
            }
        }
        final TypedKey<BiomeDefinition> own = around.get(neighbour(0, 0));
        final ChunkColumn chunk = new ChunkColumn(chunkX, chunkZ, dimensionType.minY(), dimensionType.height(), air, own);
        terrain.forEachLandBlock(chunkX, chunkZ, (x, y, z) -> chunk.setBlock(x, y, z, endStone));
        if (around.stream().anyMatch(biome -> !biome.equals(own))) {
            blendBiomes(chunk, chunkX, chunkZ, around);
        }
        return chunk;
    }

    /**
     * Gives every block of a chunk the biome of the nearest point of the jittered grid, where the chunk meets
     * chunks of other biomes. The borders then wander instead of following the chunk edges.
     *
     * @param around the biomes of the chunk and of its eight neighbours, see {@link #neighbour(int, int)}
     */
    private void blendBiomes(final ChunkColumn chunk, final int chunkX, final int chunkZ, final List<TypedKey<BiomeDefinition>> around) {
        final int minY = dimensionType.minY();
        final int maxY = minY + dimensionType.height();
        for (int z = 0; z < CHUNK_WIDTH; z++) {
            final int blockZ = (chunkZ << 4) + z;
            final int cellZ = JitteredBiomeGrid.cellOf(blockZ);
            // a grid point belongs to the chunk holding the block it stands on
            final int nearZ = (cellZ >> 2) - chunkZ;
            final int farZ = ((cellZ + 1) >> 2) - chunkZ;
            for (int x = 0; x < CHUNK_WIDTH; x++) {
                final int blockX = (chunkX << 4) + x;
                final int cellX = JitteredBiomeGrid.cellOf(blockX);
                final int nearX = (cellX >> 2) - chunkX;
                final int farX = ((cellX + 1) >> 2) - chunkX;
                final TypedKey<BiomeDefinition> nearNear = around.get(neighbour(nearX, nearZ));
                final TypedKey<BiomeDefinition> nearFar = around.get(neighbour(nearX, farZ));
                final TypedKey<BiomeDefinition> farNear = around.get(neighbour(farX, nearZ));
                final TypedKey<BiomeDefinition> farFar = around.get(neighbour(farX, farZ));
                final boolean uniform = nearNear.equals(nearFar) && nearNear.equals(farNear) && nearNear.equals(farFar);
                for (int y = minY; y < maxY; y++) {
                    final TypedKey<BiomeDefinition> biome;
                    if (uniform) {
                        biome = nearNear;
                    } else {
                        // the biomes do not change with the height here, so only the horizontal side matters
                        final int corner = biomeGrid.nearestCorner(blockX, y, blockZ);
                        final boolean towardsFarX = (corner & JitteredBiomeGrid.FAR_X) != 0;
                        final boolean towardsFarZ = (corner & JitteredBiomeGrid.FAR_Z) != 0;
                        if (towardsFarX) {
                            biome = towardsFarZ ? farFar : farNear;
                        } else {
                            biome = towardsFarZ ? nearFar : nearNear;
                        }
                    }
                    chunk.setBiome(x, y, z, biome);
                }
            }
        }
    }

    /**
     * {@return the biome a chunk stands for, picked from how much land there is at its centre; the blocks near its
     * edges may take the biome of a neighbour, see {@link #blendBiomes}}
     */
    private TypedKey<BiomeDefinition> biomeOf(final int chunkX, final int chunkZ) {
        if ((long) chunkX * chunkX + (long) chunkZ * chunkZ <= MAIN_ISLAND_CHUNKS_SQ) {
            return BiomeKeys.THE_END;
        }
        final double land = terrain.landValue((chunkX * 2 + 1) * CHUNK_CENTRE, (chunkZ * 2 + 1) * CHUNK_CENTRE);
        if (land > HIGHLANDS_ABOVE) {
            return BiomeKeys.END_HIGHLANDS;
        }
        if (land >= MIDLANDS_FROM) {
            return BiomeKeys.END_MIDLANDS;
        }
        if (land < OPEN_VOID_BELOW) {
            return BiomeKeys.SMALL_END_ISLANDS;
        }
        return BiomeKeys.END_BARRENS;
    }

    @Override
    public ChunkGeneratorConfig describeForSave() {
        return new ChunkGeneratorConfig.TheEnd();
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }
}
