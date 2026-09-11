package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.generation.GeneratedChunk;
import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginGeneratedChunk implements GeneratedChunk {

    private static final Map<Key, BlockState> BLOCK_CACHE = new ConcurrentHashMap<>();

    private final ChunkColumn column;
    private final int minY;
    private final int height;

    public PluginGeneratedChunk(final int chunkX, final int chunkZ, final int minY, final int height, final Key defaultBiome) {
        this.column = new ChunkColumn(chunkX, chunkZ, minY, height, BlockState.of(BlockTypeKeys.AIR.key()), defaultBiome);
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
    public void setBlock(final int x, final int y, final int z, final Key block, final Map<String, String> properties) {
        checkLocal(x, z);
        checkY(y);
        final BlockState state = properties.isEmpty()
                ? BLOCK_CACHE.computeIfAbsent(block, BlockState::of)
                : BlockState.of(block, properties);
        column.setBlock(x, y, z, state);
    }

    @Override
    public Key blockAt(final int x, final int y, final int z) {
        checkLocal(x, z);
        checkY(y);
        return column.getBlock(x, y, z).name();
    }

    @Override
    public void setBiome(final int x, final int y, final int z, final Key biome) {
        checkLocal(x, z);
        checkY(y);
        final ChunkSection section = column.sections()[(y >> 4) - column.minSectionY()];
        section.setBiome(x >> 2, (y & 15) >> 2, z >> 2, biome);
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
