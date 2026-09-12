package fr.euphyllia.fidorial.server.item;

import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.ItemKeys;
import net.kyori.adventure.key.Key;

import java.util.Set;

public final class Hoes {

    private static final Set<Key> KEYS = Set.of(
            ItemKeys.WOODEN_HOE.key(),
            ItemKeys.STONE_HOE.key(),
            ItemKeys.COPPER_HOE.key(),
            ItemKeys.IRON_HOE.key(),
            ItemKeys.GOLDEN_HOE.key(),
            ItemKeys.DIAMOND_HOE.key(),
            ItemKeys.NETHERITE_HOE.key());

    private Hoes() {
        throw new UnsupportedOperationException("Hoes cannot be instantiated.");
    }

    /**
     * @param item an item identifier
     * @return {@code true} when that item is a hoe
     */
    public static boolean is(final Key item) {
        return KEYS.contains(item);
    }

    /**
     * @param stack a stack, possibly empty
     * @return {@code true} when the stack holds a hoe
     */
    public static boolean is(final ItemStack stack) {
        return !stack.isEmpty() && is(stack.id());
    }
}
