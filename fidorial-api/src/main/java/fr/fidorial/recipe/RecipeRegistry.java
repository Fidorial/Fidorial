package fr.fidorial.recipe;

import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Optional;

/**
 * Where recipes are declared: the vanilla ones the server loads at startup, and the ones plugins
 * add.
 *
 * <p>When a grid matches several recipes, the ones registered by plugins win over the vanilla ones,
 * in the order they were registered. To replace a vanilla recipe outright,
 * {@linkplain #unregister(Key) unregister} it first.</p>
 *
 * <p>Implementations are thread-safe.</p>
 *
 * @since 0.1.0
 */
public interface RecipeRegistry {

    /**
     * Declares a recipe.
     *
     * @param recipe the recipe
     * @param owner  the plugin declaring it, so that it can be {@linkplain #unregisterAll dropped}
     *               with the others when the plugin is disabled
     * @throws IllegalArgumentException if a recipe is already registered under {@link Recipe#key()}
     * @since 0.1.0
     */
    void register(Recipe recipe, Object owner);

    /**
     * Drops a recipe, a vanilla one included.
     *
     * @param key the identifier of the recipe
     * @return {@code true} if a recipe was registered under that key
     * @since 0.1.0
     */
    boolean unregister(Key key);

    /**
     * Drops every recipe a plugin declared.
     *
     * @param owner the plugin to clean up after
     * @since 0.1.0
     */
    void unregisterAll(Object owner);

    /**
     * @param key the identifier of the recipe
     * @return the recipe, empty when none is registered under that key
     * @since 0.1.0
     */
    Optional<Recipe> recipe(Key key);

    /**
     * @return every registered recipe, plugin ones first, in matching order
     * @since 0.1.0
     */
    Collection<Recipe> recipes();

    /**
     * @param key the identifier of the recipe
     * @return {@code true} when that recipe comes from the vanilla data rather than a plugin
     * @since 0.1.0
     */
    boolean isVanilla(Key key);

    /**
     * Finds the recipe a crafting grid makes, the way a crafting table does.
     *
     * @param input the grid
     * @return the first matching recipe, with what it gives and takes; empty when none matches
     * @since 0.1.0
     */
    Optional<CraftingMatch> matchCrafting(CraftingInput input);

    /**
     * @return the item tags the server resolves {@linkplain Ingredient#tag(Key) tag ingredients} with
     * @since 0.1.0
     */
    ItemTagLookup tags();
}
