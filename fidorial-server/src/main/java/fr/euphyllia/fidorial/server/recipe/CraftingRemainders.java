package fr.euphyllia.fidorial.server.recipe;

import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.ItemKeys;

import java.util.Map;

/**
 * What an item leaves behind in the grid once crafted with, like the bucket of a milk bucket in a
 * cake.
 */
public final class CraftingRemainders {

    private static final Map<TypedKey<Item>, TypedKey<Item>> REMAINDERS = Map.of(
            ItemKeys.WATER_BUCKET, ItemKeys.BUCKET,
            ItemKeys.LAVA_BUCKET, ItemKeys.BUCKET,
            ItemKeys.MILK_BUCKET, ItemKeys.BUCKET,
            ItemKeys.POWDER_SNOW_BUCKET, ItemKeys.BUCKET,
            ItemKeys.HONEY_BOTTLE, ItemKeys.GLASS_BOTTLE,
            ItemKeys.DRAGON_BREATH, ItemKeys.GLASS_BOTTLE);

    private CraftingRemainders() {
    }

    /**
     * @param consumed the stack the craft takes, its count being how many items it takes
     * @return what those items leave behind, {@link ItemStack#EMPTY} when nothing
     */
    public static ItemStack of(final ItemStack consumed) {
        if (consumed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final TypedKey<Item> remainder = REMAINDERS.get(consumed.id());
        return remainder == null ? ItemStack.EMPTY : ItemStack.of(remainder, consumed.count());
    }
}
