package fr.fidorial.world.block;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;

import java.util.List;

/**
 * One state of a {@link BlockType}: the block plus a value for each of its properties.
 *
 * <p>States are immutable and shared; {@link #with(BlockProperty, Object)} and the {@code with...} methods
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

    /// {@return the ordinal of this state within its block type}
    ///
    /// @see BlockType#stateAt(int)
    /// @since 0.1.0
    int ordinal();

    /// {@return the value of a property in this state}
    ///
    /// @param property the property, declared by this block
    /// @param <T>      the type of the values
    /// @throws IllegalArgumentException if this block does not declare the property
    /// @since 0.1.0
    <T> T get(BlockProperty<T> property);

    /// {@return `true` if this block declares the property}
    ///
    /// @param property the property
    /// @since 0.1.0
    boolean has(BlockProperty<?> property);

    /// {@return the value of a property in this state, or `fallback` if this block does not declare it}
    ///
    /// @param property the property
    /// @param fallback the value to return when the property is missing
    /// @param <T>      the type of the values
    /// @since 0.1.0
    default <T> T getOrDefault(final BlockProperty<T> property, final T fallback) {
        return has(property) ? get(property) : fallback;
    }

    /// {@return the state of the same block with one property changed}
    ///
    /// @param property the property, declared by this block
    /// @param value    one of its values
    /// @param <T>      the type of the values
    /// @throws IllegalArgumentException if this block does not declare the property or the value
    /// @since 0.1.0
    <T> BlockData with(BlockProperty<T> property, T value);

    /// {@return every property value of this state, in declaration order}
    ///
    /// @since 0.1.0
    List<PropertyValue<?>> values();

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
        final TypedKey<BlockType> name = key();
        return name.equals(BlockTypeKeys.WATER) || name.equals(BlockTypeKeys.LAVA);
    }

    /**
     * {@return this state in command syntax, for instance {@code minecraft:oak_stairs[facing=north]}}
     *
     * @since 0.1.0
     */
    default String asString() {
        final String id = key().key().asString();
        if (type().properties().isEmpty()) {
            return id;
        }
        final StringBuilder builder = new StringBuilder(id).append('[');
        boolean first = true;
        for (final PropertyValue<?> value : values()) {
            if (!first) {
                builder.append(',');
            }
            builder.append(value.property().name()).append('=').append(value.valueName());
            first = false;
        }
        return builder.append(']').toString();
    }
}
