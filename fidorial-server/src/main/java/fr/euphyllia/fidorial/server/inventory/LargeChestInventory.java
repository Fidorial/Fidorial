package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.fidorial.inventory.ChestInventory;
import fr.fidorial.inventory.DoubleChestInventory;
import fr.fidorial.item.ItemStack;
import org.jspecify.annotations.Nullable;


/**
 * A large chest: the slots of its {@linkplain #first() first} half, then those of its
 * {@linkplain #second() second}, each read from and written to the chest holding it.
 *
 * @param first  the half on the left of a player facing the front, {@code type=right}
 * @param second the half on the right of a player facing the front, {@code type=left}
 */
public record LargeChestInventory(ChestInventory first, ChestInventory second) implements DoubleChestInventory {

    public LargeChestInventory {
        Preconditions.checkArgument(first != null, "A large chest needs its first half, the one on the left of its front");
        Preconditions.checkArgument(second != null, "A large chest needs its second half, the one on the right of its front");
        Preconditions.checkArgument(first.world() == second.world(),
                "The two halves of a large chest stand in one world, not in %s and %s",
                first.world().key(), second.world().key());
        Preconditions.checkArgument(!first.position().equals(second.position()),
                "The two halves of a large chest cannot both stand at %s", first.position());
    }

    @Override
    public int size() {
        return SIZE;
    }

    private ChestInventory half(final int slot) {
        if (slot < 0 || slot >= SIZE) {
            throw new IndexOutOfBoundsException("Invalid slot: " + slot);
        }
        return slot < ChestInventory.SIZE ? first : second;
    }

    @Override
    public ItemStack get(final int slot) {
        return half(slot).get(slot % ChestInventory.SIZE);
    }

    @Override
    public void set(final int slot, final @Nullable ItemStack stack) {
        half(slot).set(slot % ChestInventory.SIZE, stack);
    }

    @Override
    public void clear() {
        first.clear();
        second.clear();
    }

    @Override
    public boolean isEmpty() {
        return first.isEmpty() && second.isEmpty();
    }

    @Override
    public ItemStack[] contents() {
        final ItemStack[] contents = new ItemStack[SIZE];
        System.arraycopy(first.contents(), 0, contents, 0, ChestInventory.SIZE);
        System.arraycopy(second.contents(), 0, contents, ChestInventory.SIZE, ChestInventory.SIZE);
        return contents;
    }
}
