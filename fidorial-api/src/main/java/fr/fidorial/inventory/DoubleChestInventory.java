package fr.fidorial.inventory;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;

/**
 * A large chest: two chests standing side by side, facing the same way, seen as one container of
 * {@value #SIZE} slots. Slots {@code 0} to {@code 26} are those of the {@linkplain #first() first}
 * half, the next ones those of the {@linkplain #second() second}.
 *
 * <p>It holds nothing of its own: reading and writing a slot reads and writes the chest holding
 * it, as {@link ChestInventory} tells.</p>
 *
 * @see ChestInventory#doubleChest()
 * @since 0.1.0
 */
public interface DoubleChestInventory extends Container {

    /**
     * The number of slots of a large chest.
     *
     * @since 0.1.0
     */
    int SIZE = ChestInventory.SIZE * 2;

    /**
     * {@return the world of the large chest}
     *
     * @since 0.1.0
     */
    default World world() {
        return first().world();
    }

    /**
     * {@return the half holding slots {@code 0} to {@code 26}, the top three rows of the window: the
     * one on the left of a player facing the front of the large chest, whose state is
     * {@code type=right}}
     *
     * @since 0.1.0
     */
    ChestInventory first();

    /**
     * {@return the half holding slots {@code 27} to {@code 53}, the bottom three rows of the window:
     * the one on the right of a player facing the front of the large chest, whose state is
     * {@code type=left}}
     *
     * @since 0.1.0
     */
    ChestInventory second();

    /**
     * {@return {@code true} if one of the two halves stands at {@code position}}
     *
     * @param position a block position, in the world of the large chest
     * @since 0.1.0
     */
    default boolean contains(final BlockPosition position) {
        return first().position().equals(position) || second().position().equals(position);
    }
}
