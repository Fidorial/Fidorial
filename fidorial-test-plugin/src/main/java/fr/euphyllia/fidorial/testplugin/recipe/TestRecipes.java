package fr.euphyllia.fidorial.testplugin.recipe;

import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.recipe.CraftingCategory;
import fr.fidorial.recipe.Ingredient;
import fr.fidorial.recipe.RecipeRegistry;
import fr.fidorial.recipe.ShapedRecipe;
import fr.fidorial.recipe.ShapelessRecipe;
import fr.fidorial.registry.keys.ItemKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

/**
 * Recipes taking several items from one grid slot, which vanilla cannot do.
 */
public final class TestRecipes {

    /**
     * Sixteen diamond blocks in each top slot, a stick in each handle slot, at a crafting table.
     */
    public static final Key COMPRESSED_PICKAXE = Key.key("fidorialtest", "compressed_pickaxe");

    /**
     * Thirty-two gold ingots and a paper, anywhere in the grid, even the inventory one.
     */
    public static final Key GOLD_PACK = Key.key("fidorialtest", "gold_pack");

    private TestRecipes() {
    }

    public static void register(final RecipeRegistry recipes, final Object owner, final ComponentLogger logger) {
        recipes.register(ShapedRecipe.builder(COMPRESSED_PICKAXE)
                .pattern("DDD", " S ", " S ")
                .define('D', Ingredient.of(ItemKeys.DIAMOND_BLOCK.key()).withCount(16))
                .define('S', Ingredient.of(ItemKeys.STICK.key()))
                .result(ItemStack.builder(ItemKeys.NETHERITE_PICKAXE.key())
                        .set(DataComponentTypes.CUSTOM_NAME,
                                Component.text("Compressed pickaxe", NamedTextColor.AQUA))
                        .build())
                .category(CraftingCategory.EQUIPMENT)
                .build(), owner);

        recipes.register(ShapelessRecipe.builder(GOLD_PACK)
                .ingredient(Ingredient.of(ItemKeys.GOLD_INGOT.key()).withCount(32))
                .ingredient(Ingredient.of(ItemKeys.PAPER.key()))
                .result(ItemStack.of(ItemKeys.GOLD_BLOCK.key(), 3))
                .build(), owner);

        logger.info("[TestPlugin] Recipes {} (x16 per slot) and {} (x32 in one slot) registered",
                COMPRESSED_PICKAXE.asString(), GOLD_PACK.asString());
    }

    public static void unregisterAll(final RecipeRegistry recipes, final Object owner) {
        recipes.unregisterAll(owner);
    }
}
