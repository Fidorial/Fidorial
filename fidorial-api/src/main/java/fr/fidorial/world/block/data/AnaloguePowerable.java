package fr.fidorial.world.block.data;

import com.google.common.base.Preconditions;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockTrait;

/**
 * A block state carrying the {@code power} property: the strength of the redstone signal the block
 * carries or emits, like redstone dust, a weighted pressure plate or a daylight detector. Blocks
 * that are only on or off are {@link Powerable} instead.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(properties = "power")
public interface AnaloguePowerable extends BlockData {

    /**
     * {@return the signal strength, from {@code 0} to {@link #getMaximumPower()}}
     *
     * @since 0.1.0
     */
    default int getPower() {
        final String power = get("power");
        Preconditions.checkState(power != null, "%s has no power property", key());
        return Integer.parseInt(power);
    }

    /**
     * {@return the same block with another {@code power} value}
     *
     * @param power the signal strength
     * @throws IllegalArgumentException if the block does not accept that strength
     * @since 0.1.0
     */
    default AnaloguePowerable withPower(final int power) {
        return (AnaloguePowerable) with("power", String.valueOf(power));
    }

    /**
     * {@return the highest signal strength of this block}
     *
     * @throws IllegalStateException if the block type declares no {@code power} property
     * @since 0.1.0
     */
    default int getMaximumPower() {
        final BlockProperty property = type().property("power");
        if (property == null) {
            throw new IllegalStateException("Cannot determine maximum power");
        }
        return Integer.parseInt(property.values().getLast());
    }
}
