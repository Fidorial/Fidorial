package fr.fidorial.world.fluid;

import org.jspecify.annotations.Nullable;

/**
 * The fluid held by a block.
 *
 * @param type    the fluid, or {@code null} when the block holds none
 * @param level   {@code 0} for a source, higher values flowing further from it
 * @param falling {@code true} for fluid falling down a column
 * @since 0.1.0
 */
public record FluidState(@Nullable FluidType type, int level, boolean falling) {

    private static final FluidState EMPTY = new FluidState(null, 0, false);

    /**
     * {@return the state of a block holding no fluid}
     *
     * @since 0.1.0
     */
    public static FluidState empty() {
        return EMPTY;
    }

    /**
     * {@return a source block of a fluid}
     *
     * @param type the fluid
     * @since 0.1.0
     */
    public static FluidState source(final FluidType type) {
        return new FluidState(type, 0, false);
    }

    /**
     * {@return a flowing block of a fluid}
     *
     * @param type  the fluid
     * @param level the level, {@code 1} next to the source
     * @since 0.1.0
     */
    public static FluidState flowing(final FluidType type, final int level) {
        return new FluidState(type, level, false);
    }

    /**
     * {@return a falling block of a fluid}
     *
     * @param type the fluid
     * @since 0.1.0
     */
    public static FluidState fallingFluid(final FluidType type) {
        return new FluidState(type, 0, true);
    }

    /**
     * {@return {@code true} when the block holds no fluid}
     *
     * @since 0.1.0
     */
    public boolean isEmpty() {
        return type == null;
    }

    /**
     * {@return {@code true} for a source block}
     *
     * @since 0.1.0
     */
    public boolean isSource() {
        return type != null && level == 0 && !falling;
    }

    /**
     * {@return the level used to spread, {@code 0} for sources and falling fluid}
     *
     * @since 0.1.0
     */
    public int effectiveLevel() {
        return (falling || level <= 0) ? 0 : level;
    }
}
