package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperty;

/**
 * A block state carrying the {@code age} property: the growth stage of crops and similar blocks.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Ageable extends BlockData {

    /**
     * {@return the current growth stage, from {@code 0} to {@link #getMaximumAge()}}
     *
     * @since 0.1.0
     */
    default int getAge() {
        return Integer.parseInt(get("age"));
    }

    /**
     * {@return the same block with another {@code age} value}
     *
     * @param age the growth stage
     * @since 0.1.0
     */
    default Ageable withAge(final int age) {
        return (Ageable) with("age", String.valueOf(age));
    }

    /**
     * {@return the last growth stage of this block}
     *
     * @throws IllegalStateException if the block type declares no {@code age} property
     * @since 0.1.0
     */
    default int getMaximumAge() {
        final BlockProperty property = type().property("age");
        if (property == null) {
            throw new IllegalStateException("Cannot determine maximum age");
        }
        return Integer.parseInt(property.values().getLast());
    }
}
