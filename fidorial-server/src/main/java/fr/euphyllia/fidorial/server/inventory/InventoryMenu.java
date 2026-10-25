package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.item.enchantment.EnchantmentEngine;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Window 0, the player's own inventory, always there and never opened with {@code open_screen}:
 * the result in slot 0, the 2×2 grid in slots 1 to 4, the armor from the helmet (5) to the boots
 * (8), the main inventory (9 to 35), the hotbar (36 to 44) and the off hand (45).
 *
 * <p>Like vanilla, the grid stays filled while other windows are open; the client closing its
 * inventory gives it back.</p>
 */
public final class InventoryMenu extends AbstractCraftingMenu {

    /**
     * The window of the player inventory, always open on the client.
     */
    public static final int WINDOW_ID = 0;
    public static final int RESULT_SLOT = 0;
    public static final int GRID_FROM = 1;
    public static final int GRID_SIZE = 2;
    public static final int ARMOR_FROM = 5;
    public static final int ARMOR_TO = 9;
    public static final int OFF_HAND_SLOT = 45;

    /**
     * The armor behind window slots 5 to 8, helmet first.
     */
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public InventoryMenu(final ServerPlayer player) {
        super(player, WINDOW_ID, GRID_SIZE, GRID_SIZE);
    }

    @Override
    protected List<Slot> createSlots() {
        final PlayerInventory inventory = player.inventory();
        final List<Slot> slots = new ArrayList<>();
        slots.add(newResultSlot());
        addGridSlots(slots);
        for (final EquipmentSlot armor : ARMOR) {
            slots.add(new ArmorSlot(player, inventory, armor));
        }
        addPlayerInventory(slots);
        slots.add(new Slot(inventory, PlayerInventory.OFF_HAND));
        return slots;
    }

    @Override
    protected int resultSlot() {
        return RESULT_SLOT;
    }

    @Override
    protected boolean keepsGridOnClose() {
        return true;
    }

    /**
     * A creative-mode client sets a slot of its inventory outright. Like vanilla, any slot but the
     * result may be set, the crafting grid included, and a stack larger than the item allows is
     * refused.
     *
     * @param windowSlot the slot, in window numbering
     * @param stack      what the client put there
     * @return {@code true} when the slot was set
     */
    public boolean setCreativeSlot(final int windowSlot, final ItemStack stack) {
        if (windowSlot <= RESULT_SLOT || windowSlot >= slotCount()
                || (!stack.isEmpty() && stack.count() > maxStackSize(stack))) {
            return false;
        }
        slot(windowSlot).set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        refreshResult();
        return true;
    }

    /**
     * Like vanilla: the grid, the armor and the off hand go to the inventory, armor goes to its
     * slot when it is free, the rest moves between the main inventory and the hotbar.
     */
    @Override
    protected void quickMove(final int index) {
        final Slot source = slot(index);
        final ItemStack stack = source.get();
        if (stack.isEmpty()) {
            return;
        }
        if ((index >= GRID_FROM && index < ARMOR_TO) || index == OFF_HAND_SLOT) {
            source.set(moveItemStackTo(stack, inventoryFrom(), inventoryTo(), false));
            return;
        }
        final int armor = armorWindowSlot(ItemDefaults.equipmentSlot(stack.id()));
        if (armor >= 0 && slot(armor).get().isEmpty()) {
            source.set(moveItemStackTo(stack, armor, armor + 1, false));
            return;
        }
        source.set(index < hotbarFrom()
                ? moveItemStackTo(stack, hotbarFrom(), inventoryTo(), false)
                : moveItemStackTo(stack, inventoryFrom(), hotbarFrom(), false));
    }

    private static int armorWindowSlot(final EquipmentSlot equipment) {
        for (int i = 0; i < ARMOR.length; i++) {
            if (ARMOR[i] == equipment) {
                return ARMOR_FROM + i;
            }
        }
        return -1;
    }

    /**
     * Never sent with {@code open_screen}: the client always has its inventory.
     */
    @Override
    public TypedKey<MenuType> menuType() {
        throw new UnsupportedOperationException("The player inventory is never opened as a screen");
    }

    @Override
    public Component title() {
        return Component.translatable("container.inventory");
    }

    /**
     * An armor slot: one item worn in that slot, as its {@code equippable} component says. Like
     * vanilla, a player out of creative mode cannot take off a piece of armor whose enchantments forbid
     * it, as Curse of Binding does.
     */
    private static final class ArmorSlot extends Slot {

        private final ServerPlayer owner;
        private final EquipmentSlot equipment;

        private ArmorSlot(final ServerPlayer owner, final PlayerInventory inventory, final EquipmentSlot equipment) {
            super(inventory, PlayerInventory.slotOf(equipment));
            this.owner = owner;
            this.equipment = equipment;
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return ItemDefaults.equipmentSlot(stack.id()) == equipment;
        }

        @Override
        public boolean mayPickup() {
            final ItemStack worn = get();
            return worn.isEmpty()
                    || owner.gameMode() == GameMode.CREATIVE
                    || !EnchantmentEngine.preventsArmorChange(owner, worn, equipment);
        }

        @Override
        public int maxStackSize(final ItemStack stack) {
            return 1;
        }
    }
}
