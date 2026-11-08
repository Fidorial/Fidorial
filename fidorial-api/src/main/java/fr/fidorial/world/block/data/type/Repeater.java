package fr.fidorial.world.block.data.type;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Powerable;

/**
 * The block state of a repeater: its facing, its delay, whether it is locked by a repeater or a
 * comparator on its side, and whether it is on. {@link #getFacing()} points towards its input; it
 * powers the block on the other side.
 *
 * @since 0.1.0
 */
@BlockTrait(properties = {"delay", "locked"})
public interface Repeater extends Directional, Powerable {

    @Override
    default Repeater withFacing(final BlockFace facing) {
        return (Repeater) Directional.super.withFacing(facing);
    }

    @Override
    default Repeater withPowered(final boolean powered) {
        return (Repeater) Powerable.super.withPowered(powered);
    }

    /**
     * {@return the delay, in redstone ticks of two game ticks each, from {@link #getMinimumDelay()} to
     * {@link #getMaximumDelay()}}
     *
     * @since 0.1.0
     */
    default int getDelay() {
        final String delay = get("delay");
        Preconditions.checkState(delay != null, "%s has no delay property", key());
        return Integer.parseInt(delay);
    }

    /**
     * {@return the same repeater with another delay}
     *
     * @param delay the delay, in redstone ticks
     * @throws IllegalArgumentException if the delay is out of range
     * @since 0.1.0
     */
    default Repeater withDelay(final int delay) {
        return (Repeater) with("delay", String.valueOf(delay));
    }

    /**
     * {@return the shortest delay of this repeater}
     *
     * @throws IllegalStateException if the block type declares no {@code delay} property
     * @since 0.1.0
     */
    default int getMinimumDelay() {
        final BlockProperty property = type().property("delay");
        if (property == null) {
            throw new IllegalStateException("Cannot determine minimum delay");
        }
        return Integer.parseInt(property.values().getFirst());
    }

    /**
     * {@return the longest delay of this repeater}
     *
     * @throws IllegalStateException if the block type declares no {@code delay} property
     * @since 0.1.0
     */
    default int getMaximumDelay() {
        final BlockProperty property = type().property("delay");
        if (property == null) {
            throw new IllegalStateException("Cannot determine maximum delay");
        }
        return Integer.parseInt(property.values().getLast());
    }

    /**
     * {@return {@code true} if a powered repeater or comparator on its side holds it in its state}
     *
     * @since 0.1.0
     */
    default boolean isLocked() {
        return Boolean.parseBoolean(get("locked"));
    }

    /**
     * {@return the same repeater, locked or not}
     *
     * @param locked {@code true} to lock the repeater
     * @since 0.1.0
     */
    default Repeater withLocked(final boolean locked) {
        return (Repeater) with("locked", String.valueOf(locked));
    }
}
