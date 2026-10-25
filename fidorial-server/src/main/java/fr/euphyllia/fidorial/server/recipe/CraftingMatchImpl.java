package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.recipe.CraftingRecipe;

import java.util.Arrays;

/**
 * The server's {@link CraftingMatch}.
 */
public final class CraftingMatchImpl implements CraftingMatch {

    private final CraftingRecipe recipe;
    private final ItemStack result;
    private final int[] consumed;

    public CraftingMatchImpl(final CraftingRecipe recipe, final ItemStack result, final int[] consumed) {
        Preconditions.checkArgument(recipe != null, "The recipe of a crafting match must not be null");
        Preconditions.checkArgument(result != null && !result.isEmpty(), "The result of a crafting match must not be empty");
        Preconditions.checkArgument(consumed != null, "The consumed counts of a crafting match must not be null");
        for (final int count : consumed) {
            Preconditions.checkArgument(count >= 0, "A crafting match cannot take a negative count, got %s", count);
        }
        this.recipe = recipe;
        this.result = result;
        this.consumed = consumed.clone();
    }

    @Override
    public CraftingRecipe recipe() {
        return recipe;
    }

    @Override
    public ItemStack result() {
        return result;
    }

    @Override
    public int consumed(final int slot) {
        return slot >= 0 && slot < consumed.length ? consumed[slot] : 0;
    }

    @Override
    public CraftingMatch withResult(final ItemStack newResult) {
        return new CraftingMatchImpl(recipe, newResult, consumed);
    }

    @Override
    public String toString() {
        return "CraftingMatch{" + recipe.key().asString() + " -> " + result + ", consumed=" + Arrays.toString(consumed) + "}";
    }
}
