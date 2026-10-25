package fr.euphyllia.fidorial.server.recipe;

import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.ItemKeys;
import net.kyori.adventure.key.Key;

import java.util.Map;

/**
 * What an item leaves behind in the grid once crafted with, like the bucket of a milk bucket in a
 * cake.
 */
public final class CraftingRemainders {

    private static final Map<Key, Key> REMAINDERS = Map.of(
            ItemKeys.WATER_BUCKET.key(), ItemKeys.BUCKET.key(),
            ItemKeys.LAVA_BUCKET.key(), ItemKeys.BUCKET.key(),
            ItemKeys.MILK_BUCKET.key(), ItemKeys.BUCKET.key(),
            ItemKeys.POWDER_SNOW_BUCKET.key(), ItemKeys.BUCKET.key(),
            ItemKeys.HONEY_BOTTLE.key(), ItemKeys.GLASS_BOTTLE.key(),
            ItemKeys.DRAGON_BREATH.key(), ItemKeys.GLASS_BOTTLE.key());

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
        final Key remainder = REMAINDERS.get(consumed.id());
        return remainder == null ? ItemStack.EMPTY : ItemStack.of(remainder, consumed.count());
    }
}
