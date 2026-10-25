/**
 * Crafting recipes: the vanilla ones the server ships with, and the ones plugins add.
 *
 * <p>Recipes live in the {@linkplain GameRegistries#recipes() recipe registry}.
 * A plugin builds a {@link ShapedRecipe}, a
 * {@link ShapelessRecipe} or a {@link TransmuteRecipe}, or
 * implements {@link CraftingRecipe} itself for logic none of those cover.</p>
 *
 * <p>Unlike vanilla, where every grid slot takes exactly one item, an
 * {@link Ingredient} can ask for several of the same item in one slot:</p>
 *
 * <pre>{@code
 * server.registries().recipes().register(
 *         ShapedRecipe.builder(Key.key("myplugin", "compressed_pickaxe"))
 *                 .pattern("DDD", " S ", " S ")
 *                 .define('D', Ingredient.of(ItemKeys.DIAMOND_BLOCK.key()).withCount(16))
 *                 .define('S', Ingredient.of(ItemKeys.STICK.key()))
 *                 .result(ItemStack.of(ItemKeys.DIAMOND_PICKAXE.key()))
 *                 .build(),
 *         plugin);
 * }</pre>
 *
 * @since 0.1.0
 */
package fr.fidorial.recipe;

import fr.fidorial.registry.GameRegistries;
