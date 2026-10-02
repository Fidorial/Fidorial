package fr.fidorial.world.block;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * One state of a {@link BlockType}: the block plus a value for each of its properties.
 *
 * <p>States are immutable and shared; {@link #with(String, String)} and the {@code with...} methods
 * of the traits in {@link fr.fidorial.world.block.data} return another state.</p>
 *
 * @since 0.1.0
 */
public interface BlockData {

    /**
     * The key of plain air.
     *
     * @since 0.1.0
     */
    Key AIR = Key.key("air");
    /**
     * The key of cave air.
     *
     * @since 0.1.0
     */
    Key CAVE_AIR = Key.key("cave_air");
    /**
     * The key of void air, below the world.
     *
     * @since 0.1.0
     */
    Key VOID_AIR = Key.key("void_air");

    /**
     * {@return the block type of this state}
     *
     * @since 0.1.0
     */
    BlockType type();

    /**
     * {@return the key of the block type of this state}
     *
     * @since 0.1.0
     */
    default Key key() {
        return type().key();
    }

    /**
     * {@return the network identifier of this state}
     *
     * @since 0.1.0
     */
    int networkId();

    /**
     * Gets the value of a property.
     *
     * @param property the property name
     * @return the value, or {@code null} if the block has no such property
     * @since 0.1.0
     */
    @Nullable String get(String property);

    /**
     * {@return the state of the same block with one property changed}
     *
     * @param property the property name
     * @param value    the new value
     * @throws IllegalArgumentException if the property or value is unknown to this block
     * @since 0.1.0
     */
    BlockData with(String property, String value);

    /**
     * {@return every property value of this state, in declaration order}
     *
     * @since 0.1.0
     */
    Map<String, String> propertyMap();

    /**
     * {@return {@code true} for any kind of air}
     *
     * @since 0.1.0
     */
    default boolean isAir() {
        final Key name = key();
        return name.equals(AIR) || name.equals(CAVE_AIR) || name.equals(VOID_AIR);
    }

    /**
     * {@return this state in command syntax, for instance {@code minecraft:oak_stairs[facing=north]}}
     *
     * @since 0.1.0
     */
    default String asString() {
        if (type().properties().isEmpty()) {
            return key().asString();
        }
        final StringBuilder builder = new StringBuilder(key().asString()).append('[');
        boolean first = true;
        for (final Map.Entry<String, String> entry : propertyMap().entrySet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        return builder.append(']').toString();
    }
}
