package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import net.kyori.adventure.key.Key;

public final class FlatChunkGenerator implements ChunkGenerator {

    private final BlockState floor;
    private final Key biome;
    private final int floorThickness;
    private final DimensionTypeDefinition dimensionType;

    public FlatChunkGenerator(final DimensionTypeDefinition dimensionType, final BlockState floor, final Key biome, final int floorThickness) {
        this.dimensionType = dimensionType;
        this.floor = floor;
        this.biome = biome;
        this.floorThickness = floorThickness;
    }

    public static FlatChunkGenerator cobblestone(final DimensionTypeDefinition dimensionType) {
        return new FlatChunkGenerator(dimensionType, BlockState.of(Key.key("cobblestone")), Key.key("plains"), 16);
    }

    @Override
    public ChunkColumn generate(final int chunkX, final int chunkZ) {
        final ChunkColumn chunk = new ChunkColumn(chunkX, chunkZ, dimensionType.minY(), dimensionType.height(), BlockState.of(BlockTypeKeys.AIR.key()), biome);
        for (int y = dimensionType.minY(); y < dimensionType.minY() + floorThickness; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    chunk.setBlock(x, y, z, floor);
                }
            }
        }
        return chunk;
    }

    @Override
    public ChunkGeneratorConfig describeForSave() {
        return new ChunkGeneratorConfig.Flat(floor, floorThickness, biome);
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }
}
