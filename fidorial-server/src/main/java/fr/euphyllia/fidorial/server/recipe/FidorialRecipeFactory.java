package fr.euphyllia.fidorial.server.recipe;

import com.google.common.base.Preconditions;
import fr.fidorial.item.ItemStack;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.recipe.CraftingRecipe;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.RecipeFactory;
import fr.fidorial.recipe.ShapedRecipe;
import fr.fidorial.recipe.ShapelessRecipe;
import fr.fidorial.recipe.TransmuteRecipe;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Backs the static factories of {@code fr.fidorial.recipe}, found through {@link java.util.ServiceLoader}.
 */
public final class FidorialRecipeFactory implements RecipeFactory {

    @Override
    public ShapedRecipe.Builder shaped(final Key key) {
        return new ShapedRecipeImpl.BuilderImpl(key);
    }

    @Override
    public ShapelessRecipe.Builder shapeless(final Key key) {
        return new ShapelessRecipeImpl.BuilderImpl(key);
    }

    @Override
    public TransmuteRecipe.Builder transmute(final Key key) {
        return new TransmuteRecipeImpl.BuilderImpl(key);
    }

    @Override
    public Ingredient items(final Collection<Key> items) {
        Preconditions.checkArgument(items != null, "The items passed to Ingredient.of() must not be null");
        return IngredientImpl.items(new LinkedHashSet<>(items));
    }

    @Override
    public Ingredient tag(final Key tag) {
        return IngredientImpl.tag(tag);
    }

    @Override
    public Ingredient exact(final ItemStack stack) {
        return IngredientImpl.exact(stack);
    }

    @Override
    public Ingredient matching(final Predicate<ItemStack> predicate) {
        return IngredientImpl.matching(predicate);
    }

    @Override
    public CraftingInput input(final int width, final int height, final List<ItemStack> items) {
        return new CraftingInputImpl(width, height, items);
    }

    @Override
    public CraftingMatch match(final CraftingRecipe recipe, final ItemStack result, final int[] consumed) {
        return new CraftingMatchImpl(recipe, result, consumed);
    }
}
