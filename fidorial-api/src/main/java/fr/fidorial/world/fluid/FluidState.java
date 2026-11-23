package fr.fidorial.world.fluid;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.keys.FluidKeys;

/**
 * The fluid held by a block.
 *
 * @see FluidManager#fluidAt
 * @since 0.1.0
 */
public interface FluidState {

    /**
     * {@return the fluid}: {@link FluidKeys#WATER}, {@link FluidKeys#LAVA}, or {@link FluidKeys#EMPTY}
     * when the block holds none
     *
     * @since 0.1.0
     */
    TypedKey<Fluid> fluid();

    /**
     * {@return {@code true} when the block holds no fluid}
     *
     * @since 0.1.0
     */
    boolean isEmpty();

    /**
     * {@return {@code true} for a source}
     *
     * @since 0.1.0
     */
    boolean isSource();

    /**
     * {@return {@code true} for fluid falling down a column}: a full block that is not a source
     *
     * @since 0.1.0
     */
    boolean isFalling();

    /**
     * {@return how many eighths of the block the fluid fills}: {@code 8} for a source or falling
     * fluid, down to {@code 1} for flowing fluid, {@code 0} for no fluid
     *
     * @since 0.1.0
     */
    int amount();
}
