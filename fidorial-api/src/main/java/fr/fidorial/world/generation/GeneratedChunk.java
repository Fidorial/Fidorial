package fr.fidorial.world.generation;

import fr.fidorial.world.block.BlockData;
import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Map;

/**
 * The chunk a {@link WorldGenerator} fills.
 */
public interface GeneratedChunk {

    int chunkX();

    int chunkZ();

    int minY();

    int height();

    /**
     * Places the default state of a block.
     */
    default void setBlock(final int x, final int y, final int z, final Key block) {
        setBlock(x, y, z, block, Map.of());
    }

    /**
     * Places a block state. Properties that are not given keep their default value.
     *
     * @param properties property name to value, e.g. {@code half=upper}
     */
    void setBlock(int x, int y, int z, Key block, Map<String, String> properties);

    /**
     * Places a block state.
     */
    default void setBlock(final int x, final int y, final int z, final BlockData data) {
        final Map<String, String> properties = new HashMap<>(data.propertyMap());
        setBlock(x, y, z, data.key(), properties);
    }

    /**
     * @return the block currently stored at the position
     */
    Key blockAt(int x, int y, int z);

    void setBiome(int x, int y, int z, Key biome);
}
