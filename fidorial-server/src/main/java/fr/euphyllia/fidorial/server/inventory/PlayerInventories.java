package fr.euphyllia.fidorial.server.inventory;

import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;

/**
 * Puts items into a player inventory the way picked up items go, vanilla's order: the selected
 * hotbar slot and the off hand first when they hold the same item, then similar stacks of the
 * hotbar and main inventory, then their empty slots, the hotbar first.
 */
public final class PlayerInventories {

    private PlayerInventories() {
    }

    /**
     * {@return how many of {@code stack} the inventory still has room for, at most its count}
     *
     * @param inventory the inventory
     * @param stack     the stack to fit
     */
    public static int roomFor(final PlayerInventory inventory, final ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        final int max = maxStackSize(stack);
        // The off hand only takes more of what it already holds, never a new item.
        final ItemStack offHand = inventory.get(PlayerInventory.OFF_HAND);
        int room = offHand.isEmpty() ? 0 : roomIn(offHand, stack, max);
        for (int slot = PlayerInventory.HOTBAR_START; slot < PlayerInventory.STORAGE_SIZE && room < stack.count(); slot++) {
            room += roomIn(inventory.get(slot), stack, max);
        }
        return Math.min(room, stack.count());
    }

    /**
     * Puts as much of {@code stack} as fits into the inventory.
     *
     * @param inventory    the inventory
     * @param selectedSlot the selected hotbar slot, filled first when it holds the same item
     * @param stack        the stack to add
     * @return what did not fit, {@link ItemStack#EMPTY} when everything did
     */
    public static ItemStack add(final PlayerInventory inventory, final int selectedSlot, final ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final int max = maxStackSize(stack);
        int left = stack.count();
        left = fillSimilar(inventory, selectedSlot, stack, max, left);
        left = fillSimilar(inventory, PlayerInventory.OFF_HAND, stack, max, left);
        for (int slot = PlayerInventory.HOTBAR_START; slot < PlayerInventory.STORAGE_SIZE && left > 0; slot++) {
            left = fillSimilar(inventory, slot, stack, max, left);
        }
        for (int slot = PlayerInventory.HOTBAR_START; slot < PlayerInventory.STORAGE_SIZE && left > 0; slot++) {
            if (inventory.get(slot).isEmpty()) {
                final int moved = Math.min(left, max);
                inventory.set(slot, stack.withCount(moved));
                left -= moved;
            }
        }
        return left <= 0 ? ItemStack.EMPTY : stack.withCount(left);
    }

    private static int fillSimilar(final PlayerInventory inventory, final int slot, final ItemStack stack,
                                   final int max, final int left) {
        if (left <= 0 || slot < 0 || slot >= inventory.size()) {
            return left;
        }
        final ItemStack inSlot = inventory.get(slot);
        if (inSlot.isEmpty() || !inSlot.isSimilar(stack) || inSlot.count() >= max) {
            return left;
        }
        final int moved = Math.min(left, max - inSlot.count());
        inventory.set(slot, inSlot.withCount(inSlot.count() + moved));
        return left - moved;
    }

    private static int roomIn(final ItemStack inSlot, final ItemStack stack, final int max) {
        if (inSlot.isEmpty()) {
            return max;
        }
        return inSlot.isSimilar(stack) ? Math.max(0, max - inSlot.count()) : 0;
    }

    private static int maxStackSize(final ItemStack stack) {
        return Math.max(1, ItemDefaults.maxStackSize(stack.id(), stack));
    }
}
