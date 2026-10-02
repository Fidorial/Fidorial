package fr.fidorial.world.block.data;

import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperty;

/**
 * A block state carrying the {@code level} property: the fill level of fluids, cauldrons and similar blocks.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
public interface Levelled extends BlockData {

    /**
     * {@return the current level, from {@code 0} to {@link #getMaximumLevel()}}
     *
     * @since 0.1.0
     */
    default int getLevel() {
        return Integer.parseInt(get("level"));
    }

    /**
     * {@return the same block with another {@code level} value}
     *
     * @param level the level
     * @since 0.1.0
     */
    default Levelled withLevel(final int level) {
        return (Levelled) with("level", String.valueOf(level));
    }

    /**
     * {@return the highest level of this block}
     *
     * @throws IllegalStateException if the block type declares no {@code level} property
     * @since 0.1.0
     */
    default int getMaximumLevel() {
        final BlockProperty property = type().property("level");
        if (property == null) {
            throw new IllegalStateException("Cannot determine maximum level");
        }
        return Integer.parseInt(property.values().getLast());
    }
}
