package fr.euphyllia.fidorial.server.world.fluid;

import com.google.common.base.Preconditions;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.registry.keys.FluidKeys;
import fr.fidorial.world.fluid.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * The fluid held by a block.
 *
 * @param type     the fluid, {@code null} when the block holds none
 * @param isSource {@code true} for a source
 * @param amount   how many eighths of the block the fluid fills, from {@code 1} to {@value #FULL},
 *                 {@code 0} for no fluid
 */
public record FluidStateImpl(@Nullable FluidType type, boolean isSource, int amount) implements FluidState {

    /**
     * How many eighths a source, or falling fluid, fills: the whole block.
     */
    public static final int FULL = 8;

    private static final FluidStateImpl EMPTY = new FluidStateImpl(null, false, 0);

    public FluidStateImpl {
        if (type == null) {
            Preconditions.checkArgument(!isSource && amount == 0, "A block holding no fluid has no source and no amount");
        } else {
            Preconditions.checkArgument(amount >= 1 && amount <= FULL, "A fluid fills from 1 to 8 eighths of a block, not %s", amount);
            Preconditions.checkArgument(!isSource || amount == FULL, "A source fills its whole block");
        }
    }

    /**
     * {@return the state of a block holding no fluid}
     */
    public static FluidStateImpl empty() {
        return EMPTY;
    }

    /**
     * {@return a source of a fluid}
     */
    public static FluidStateImpl source(final FluidType type) {
        return new FluidStateImpl(type, true, FULL);
    }

    /**
     * {@return flowing fluid}, filling from {@code 1} to {@value #FULL} eighths of its block, the
     * latter for falling fluid
     */
    public static FluidStateImpl flowing(final FluidType type, final int amount) {
        return new FluidStateImpl(type, false, amount);
    }

    /**
     * {@return fluid falling down a column}
     */
    public static FluidStateImpl falling(final FluidType type) {
        return flowing(type, FULL);
    }

    /**
     * {@return the fluid a fluid block holds}, from the {@code level} of the block: {@code 0} for a
     * source, {@code 1} to {@code 7} for flowing fluid, {@code 8} and above for falling fluid
     */
    public static FluidStateImpl ofBlockLevel(final FluidType type, final int level) {
        if (level <= 0) {
            return source(type);
        }
        return level >= FULL ? falling(type) : flowing(type, FULL - level);
    }

    @Override
    public TypedKey<Fluid> fluid() {
        return type == null ? FluidKeys.EMPTY : type.fluidKey();
    }

    @Override
    public boolean isEmpty() {
        return type == null;
    }

    @Override
    public boolean isFalling() {
        return !isSource && amount == FULL;
    }

    /**
     * {@return the level of the fluid block holding this fluid}: {@code 0} for a source, the eighths
     * it lost for flowing fluid, {@code 8} for falling fluid
     */
    public int blockLevel() {
        if (isSource) {
            return 0;
        }
        return FULL - amount + (isFalling() ? FULL : 0);
    }

    /**
     * {@return how high the fluid stands in its block, from {@code 0} to {@code 8/9}}, a fluid with
     * the same fluid above filling it whole
     */
    public float ownHeight() {
        return amount / 9.0f;
    }
}
