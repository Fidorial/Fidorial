package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockProperties.Names;
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
@BlockTrait(properties = {Names.DELAY, Names.LOCKED})
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
        return get(type().range(Names.DELAY));
    }

    /**
     * {@return the same repeater with another delay}
     *
     * @param delay the delay, in redstone ticks
     * @throws IllegalArgumentException if the delay is out of range
     * @since 0.1.0
     */
    default Repeater withDelay(final int delay) {
        return (Repeater) with(type().range(Names.DELAY), delay);
    }

    /**
     * {@return the shortest delay of this repeater}
     *
     * @throws IllegalStateException if the block type declares no {@code delay} property
     * @since 0.1.0
     */
    default int getMinimumDelay() {
        return type().range(Names.DELAY).min();
    }

    /**
     * {@return the longest delay of this repeater}
     *
     * @throws IllegalStateException if the block type declares no {@code delay} property
     * @since 0.1.0
     */
    default int getMaximumDelay() {
        return type().range(Names.DELAY).max();
    }

    /**
     * {@return {@code true} if a powered repeater or comparator on its side holds it in its state}
     *
     * @since 0.1.0
     */
    default boolean isLocked() {
        return get(type().flag(Names.LOCKED));
    }

    /**
     * {@return the same repeater, locked or not}
     *
     * @param locked {@code true} to lock the repeater
     * @since 0.1.0
     */
    default Repeater withLocked(final boolean locked) {
        return (Repeater) with(type().flag(Names.LOCKED), locked);
    }
}
