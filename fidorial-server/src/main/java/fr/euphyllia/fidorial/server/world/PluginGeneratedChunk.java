package fr.euphyllia.fidorial.server.world;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.generation.GeneratedChunk;

import java.util.Map;

public class PluginGeneratedChunk implements GeneratedChunk {


    private final ChunkColumn column;
    private final int minY;
    private final int height;

    public PluginGeneratedChunk(final int chunkX, final int chunkZ, final int minY, final int height, final TypedKey<BiomeDefinition> defaultBiome) {
        this.column = new ChunkColumn(chunkX, chunkZ, minY, height, BlockState.defaultOf(BlockTypeKeys.AIR), defaultBiome);
        this.minY = minY;
        this.height = height;
    }

    ChunkColumn column() {
        return column;
    }

    @Override
    public int chunkX() {
        return column.chunkX();
    }

    @Override
    public int chunkZ() {
        return column.chunkZ();
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final TypedKey<BlockType> block, final Map<String, String> properties) {
        checkLocal(x, z);
        checkY(y);
        final BlockState state = BlockState.parseLeniently(block, properties);
        Preconditions.checkArgument(state != null, "%s is not a registered block type", block.key().asString());
        column.setBlock(x, y, z, state);
    }

    @Override
    public void setBlock(final int x, final int y, final int z, final BlockData data) {
        checkLocal(x, z);
        checkY(y);
        column.setBlock(x, y, z, BlockState.of(data));
    }

    @Override
    public TypedKey<BlockType> blockAt(final int x, final int y, final int z) {
        checkLocal(x, z);
        checkY(y);
        return column.getBlock(x, y, z).name();
    }

    @Override
    public void setBiome(final int x, final int y, final int z, final TypedKey<BiomeDefinition> biome) {
        checkLocal(x, z);
        checkY(y);
        final ChunkSection section = column.sections()[(y >> 4) - column.minSectionY()];
        section.setBiome(x, y & 15, z, biome);
    }

    private void checkLocal(final int x, final int z) {
        if (x < 0 || x > 15 || z < 0 || z > 15) {
            throw new IllegalArgumentException("Local coordinates outside bounds:: x=" + x + ", z=" + z);
        }
    }

    private void checkY(final int y) {
        if (y < minY || y >= minY + height) {
            throw new IllegalArgumentException(
                    "y out of bounds: " + y + " (expected [" + minY + ", " + (minY + height) + "[)");
        }
    }
}
