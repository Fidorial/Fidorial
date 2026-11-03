package fr.fidorial.world.block;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.fluid.FluidType;
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
    default TypedKey<BlockType> key() {
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
        final TypedKey<BlockType> name = key();
        return name.equals(BlockTypeKeys.AIR) || name.equals(BlockTypeKeys.CAVE_AIR) || name.equals(BlockTypeKeys.VOID_AIR);
    }

    /**
     * {@return {@code true} for a block of fluid: water or lava, but not a waterlogged block}
     *
     * @since 0.1.0
     */
    default boolean isFluid() {
        return FluidType.byBlockKey(key()) != null;
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
