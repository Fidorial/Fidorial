package fr.fidorial.recipe;

import fr.fidorial.item.ItemStack;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

/**
 * Creates the recipe objects of this package, implemented by the server.
 *
 * <p>Plugins do not call it: they go through the static methods of the types it creates, such as
 * {@link ShapedRecipe#builder(Key)} or {@link Ingredient#of(Key...)}.</p>
 *
 * @since 0.1.0
 */
@ApiStatus.Internal
public interface RecipeFactory {

    /**
     * @return the factory the server provides
     * @since 0.1.0
     */
    static RecipeFactory factory() {
        final class Holder {
            static final RecipeFactory INSTANCE = ServiceLoader.load(RecipeFactory.class, RecipeFactory.class.getClassLoader())
                    .findFirst()
                    .orElseThrow();
        }
        return Holder.INSTANCE;
    }

    /**
     * @see ShapedRecipe#builder(Key)
     */
    ShapedRecipe.Builder shaped(Key key);

    /**
     * @see ShapelessRecipe#builder(Key)
     */
    ShapelessRecipe.Builder shapeless(Key key);

    /**
     * @see TransmuteRecipe#builder(Key)
     */
    TransmuteRecipe.Builder transmute(Key key);

    /**
     * @see Ingredient#of(Collection)
     */
    Ingredient items(Collection<Key> items);

    /**
     * @see Ingredient#tag(Key)
     */
    Ingredient tag(Key tag);

    /**
     * @see Ingredient#exact(ItemStack)
     */
    Ingredient exact(ItemStack stack);

    /**
     * @see Ingredient#matching(Predicate)
     */
    Ingredient matching(Predicate<ItemStack> predicate);

    /**
     * @see CraftingInput#of(int, int, List)
     */
    CraftingInput input(int width, int height, List<ItemStack> items);

    /**
     * @see CraftingMatch#of(CraftingRecipe, ItemStack, int[])
     */
    CraftingMatch match(CraftingRecipe recipe, ItemStack result, int[] consumed);
}
