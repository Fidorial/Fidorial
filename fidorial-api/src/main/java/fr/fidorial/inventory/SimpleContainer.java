package fr.fidorial.inventory;

import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * A {@link Container} backed by an array, the base of the built-in inventories.
 *
 * <p>Instances are not thread-safe; touch them from the region thread owning their holder.</p>
 *
 * @since 0.1.0
 */
public abstract class SimpleContainer implements Container {

    private final ItemStack[] slots;

    /**
     * Creates an empty container.
     *
     * @param size the number of slots
     * @throws IllegalArgumentException if {@code size} is negative
     * @since 0.1.0
     */
    protected SimpleContainer(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("A container cannot have a negative size: " + size);
        }
        this.slots = new ItemStack[size];
        Arrays.fill(slots, ItemStack.EMPTY);
    }

    private void checkSlot(final int slot) {
        if (slot < 0 || slot >= slots.length) {
            throw new IndexOutOfBoundsException("Invalid slot: " + slot);
        }
    }

    @Override
    public final int size() {
        return slots.length;
    }

    @Override
    public ItemStack get(final int slot) {
        checkSlot(slot);
        return slots[slot];
    }

    @Override
    public void set(final int slot, final @Nullable ItemStack stack) {
        checkSlot(slot);
        slots[slot] = stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public void clear() {
        Arrays.fill(slots, ItemStack.EMPTY);
    }

    @Override
    public boolean isEmpty() {
        for (final ItemStack stack : slots) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack[] contents() {
        return Arrays.copyOf(slots, slots.length);
    }
}
