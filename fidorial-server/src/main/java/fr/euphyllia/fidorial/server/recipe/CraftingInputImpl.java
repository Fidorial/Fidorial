package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingInput;

import java.util.List;

/**
 * The server's {@link CraftingInput}: an immutable snapshot of a grid, comparable by value so that
 * an unchanged grid is not matched again.
 *
 * @param width  the number of columns
 * @param height the number of rows
 * @param items  {@code width * height} stacks, row-major
 */
public record CraftingInputImpl(int width, int height, List<ItemStack> items) implements CraftingInput {

    public CraftingInputImpl {
        Preconditions.checkArgument(width >= 1 && height >= 1,
                "A crafting grid must be at least 1x1, got %sx%s", width, height);
        Preconditions.checkArgument(items != null, "The items of a crafting grid must not be null");
        Preconditions.checkArgument(items.size() == width * height,
                "A %sx%s crafting grid holds %s stacks, got %s", width, height, width * height, items.size());
        items = List.copyOf(items);
    }

    @Override
    public ItemStack get(final int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack get(final int x, final int y) {
        return items.get(y * width + x);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public int nonEmptyCount() {
        int count = 0;
        for (final ItemStack stack : items) {
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isEmpty() {
        return nonEmptyCount() == 0;
    }
}
