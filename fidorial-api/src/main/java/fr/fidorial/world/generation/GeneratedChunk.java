package fr.fidorial.world.generation;

import fr.fidorial.world.block.BlockData;
import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Map;

/**
 * The chunk a {@link WorldGenerator} fills.
 *
 * <p>X and Z coordinates are local to the chunk ({@code 0} to {@code 15}); Y coordinates are
 * absolute. Out-of-range coordinates throw {@link IllegalArgumentException}.</p>
 *
 * @since 0.1.0
 */
public interface GeneratedChunk {

    /**
     * {@return the chunk X coordinate}
     *
     * @since 0.1.0
     */
    int chunkX();

    /**
     * {@return the chunk Z coordinate}
     *
     * @since 0.1.0
     */
    int chunkZ();

    /**
     * {@return the lowest block Y coordinate, inclusive}
     *
     * @since 0.1.0
     */
    int minY();

    /**
     * {@return the number of block layers, starting at {@link #minY()}}
     *
     * @since 0.1.0
     */
    int height();

    /**
     * Places the default state of a block.
     *
     * @param x     the X coordinate inside the chunk, {@code 0} to {@code 15}
     * @param y     the absolute Y coordinate
     * @param z     the Z coordinate inside the chunk, {@code 0} to {@code 15}
     * @param block the block key
     * @since 0.1.0
     */
    default void setBlock(final int x, final int y, final int z, final Key block) {
        setBlock(x, y, z, block, Map.of());
    }

    /**
     * Places a block state. Properties that are not given keep their default value.
     *
     * @param x          the X coordinate inside the chunk, {@code 0} to {@code 15}
     * @param y          the absolute Y coordinate
     * @param z          the Z coordinate inside the chunk, {@code 0} to {@code 15}
     * @param block      the block key
     * @param properties property name to value, e.g. {@code half=upper}
     * @since 0.1.0
     */
    void setBlock(int x, int y, int z, Key block, Map<String, String> properties);

    /**
     * Places a block state.
     *
     * @param x    the X coordinate inside the chunk, {@code 0} to {@code 15}
     * @param y    the absolute Y coordinate
     * @param z    the Z coordinate inside the chunk, {@code 0} to {@code 15}
     * @param data the block state
     * @since 0.1.0
     */
    default void setBlock(final int x, final int y, final int z, final BlockData data) {
        final Map<String, String> properties = new HashMap<>(data.propertyMap());
        setBlock(x, y, z, data.key(), properties);
    }

    /**
     * Reads back a block placed so far.
     *
     * @param x the X coordinate inside the chunk, {@code 0} to {@code 15}
     * @param y the absolute Y coordinate
     * @param z the Z coordinate inside the chunk, {@code 0} to {@code 15}
     * @return the key of the block currently stored at the position
     * @since 0.1.0
     */
    Key blockAt(int x, int y, int z);

    /**
     * Sets the biome of the 4x4x4 cell holding a block.
     *
     * @param x     the X coordinate inside the chunk, {@code 0} to {@code 15}
     * @param y     the absolute Y coordinate
     * @param z     the Z coordinate inside the chunk, {@code 0} to {@code 15}
     * @param biome the biome key, registered in the {@link fr.fidorial.world.biome.BiomeRegistry}
     * @since 0.1.0
     */
    void setBiome(int x, int y, int z, Key biome);
}
