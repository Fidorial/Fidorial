package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.MenuTypeKeys;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The window of a crafting table: the result in slot 0, the 3×3 grid in slots 1 to 9, then the
 * player inventory.
 */
public final class CraftingMenu extends AbstractCraftingMenu {

    public static final int RESULT_SLOT = 0;
    public static final int GRID_FROM = 1;
    public static final int GRID_SIZE = 3;

    private static final int GRID_TO = GRID_FROM + GRID_SIZE * GRID_SIZE;

    public CraftingMenu(final ServerPlayer player, final int windowId, final BlockPosition position) {
        super(player, windowId, position, BlockTypeKeys.CRAFTING_TABLE.key(), GRID_SIZE, GRID_SIZE);
    }

    @Override
    protected List<Slot> createSlots() {
        final List<Slot> slots = new ArrayList<>();
        slots.add(newResultSlot());
        addGridSlots(slots);
        addPlayerInventory(slots);
        return slots;
    }

    @Override
    protected int resultSlot() {
        return RESULT_SLOT;
    }

    @Override
    protected boolean keepsGridOnClose() {
        return false;
    }

    /**
     * Like vanilla: the grid goes to the inventory, the inventory goes to the grid first, then
     * between the main inventory and the hotbar.
     */
    @Override
    protected void quickMove(final int index) {
        final Slot source = slot(index);
        final ItemStack stack = source.get();
        if (stack.isEmpty()) {
            return;
        }
        ItemStack leftover;
        if (index >= GRID_FROM && index < GRID_TO) {
            leftover = moveItemStackTo(stack, inventoryFrom(), inventoryTo(), false);
        } else {
            leftover = moveItemStackTo(stack, GRID_FROM, GRID_TO, false);
            if (leftover.count() == stack.count()) {
                leftover = index < hotbarFrom()
                        ? moveItemStackTo(stack, hotbarFrom(), inventoryTo(), false)
                        : moveItemStackTo(stack, inventoryFrom(), hotbarFrom(), false);
            }
        }
        source.set(leftover);
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return MenuTypeKeys.CRAFTING;
    }

    @Override
    public Component title() {
        return Component.translatable("container.crafting");
    }
}
