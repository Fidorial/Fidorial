package fr.fidorial.inventory;

import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * A fixed number of item slots, indexed from {@code 0}.
 *
 * <p>Empty slots hold {@link ItemStack#EMPTY}, never {@code null}.</p>
 *
 * @since 0.1.0
 */
public interface Container {

    /**
     * {@return the number of slots exposed by this container}
     *
     * @since 0.1.0
     */
    int size();

    /**
     * Gets the contents of a slot.
     *
     * @param slot the slot index, from {@code 0} to {@code size() - 1}
     * @return the stack in the slot, {@link ItemStack#EMPTY} when it is empty
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @since 0.1.0
     */
    ItemStack get(int slot);

    /**
     * Replaces the contents of a slot.
     *
     * @param slot  the slot index, from {@code 0} to {@code size() - 1}
     * @param stack the new stack; {@code null} is normalized to {@link ItemStack#EMPTY}
     * @throws IndexOutOfBoundsException if the slot is out of bounds
     * @since 0.1.0
     */
    void set(int slot, @Nullable ItemStack stack);

    /**
     * Empties every slot.
     *
     * @since 0.1.0
     */
    void clear();

    /**
     * {@return {@code true} if every slot is empty}
     *
     * @since 0.1.0
     */
    boolean isEmpty();

    /**
     * {@return the index of the first empty slot, or {@code -1} when the container is full}
     *
     * @since 0.1.0
     */
    default int firstEmpty() {
        for (int slot = 0; slot < size(); slot++) {
            if (get(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * {@return a copy of every slot, in index order}
     *
     * @since 0.1.0
     */
    default ItemStack[] contents() {
        final ItemStack[] contents = new ItemStack[size()];
        for (int slot = 0; slot < contents.length; slot++) {
            contents[slot] = get(slot);
        }
        return contents;
    }

    /**
     * Replaces every slot at once.
     *
     * <p>The container is cleared first; stacks beyond {@link #size()} are ignored and {@code null}
     * entries leave their slot empty.</p>
     *
     * @param contents the new contents, in index order
     * @since 0.1.0
     */
    default void setContents(final @Nullable ItemStack[] contents) {
        clear();
        final int limit = Math.min(contents.length, size());
        for (int slot = 0; slot < limit; slot++) {
            set(slot, contents[slot]);
        }
    }
}
