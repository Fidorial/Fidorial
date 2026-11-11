package fr.fidorial.world.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.world.block.BlockData;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * What a hoe does to a block type: what the block turns into, whether it needs room above, and
 * what falls out of it.
 *
 * <p>Only {@link #tilled()} has to be given, so that a lambda is enough:</p>
 *
 * <pre>{@code
 * farming.tillable(PEAT, () -> farmland, plugin);
 * }</pre>
 *
 * @see FarmingRegistry#tillable
 * @since 0.1.0
 */
@FunctionalInterface
public interface Tillage {

    /**
     * {@return the state the tilled block turns into}
     *
     * @since 0.1.0
     */
    BlockData tilled();

    /**
     * {@return {@code true} if the block can only be tilled with air right above it}
     *
     * @since 0.1.0
     */
    default boolean needsRoom() {
        return true;
    }

    /**
     * Works out what falls out of the block when it is tilled, such as the roots of rooted dirt.
     *
     * @param random the source of randomness to roll the drops with
     * @return the stacks to drop from the clicked face, empty for nothing
     * @since 0.1.0
     */
    default List<ItemStack> drops(final RandomGenerator random) {
        return List.of();
    }
}
