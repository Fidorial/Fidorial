package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.player.PlayerCraftItemEventImpl;
import fr.euphyllia.fidorial.server.events.player.PlayerPrepareCraftEventImpl;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.recipe.CraftingRemainders;
import fr.fidorial.event.player.PlayerCraftItemEvent;
import fr.fidorial.event.player.PlayerPrepareCraftEvent;
import fr.fidorial.inventory.Container;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A window with a crafting grid and a result slot: the player inventory and the crafting table.
 *
 * <p>The result is recomputed after every click. Taking it fires {@link PlayerCraftItemEvent},
 * then takes from each grid slot what the {@linkplain CraftingMatch match} says, which may be more
 * than one item per slot for plugin recipes.</p>
 */
public abstract class AbstractCraftingMenu extends ContainerMenu {

    /**
     * Upper bound of crafts per shift-click, against a recipe that would take nothing.
     */
    private static final int MAX_SHIFT_CRAFTS = 9 * 64;

    protected final CraftingContainer grid;
    private @Nullable CraftingInput lastInput;
    private @Nullable CraftingMatch currentMatch;
    private ItemStack currentResult = ItemStack.EMPTY;

    protected AbstractCraftingMenu(final ServerPlayer player, final int windowId, final int gridWidth,
                                   final int gridHeight) {
        super(player, windowId);
        this.grid = new CraftingContainer(gridWidth, gridHeight);
    }

    protected AbstractCraftingMenu(final ServerPlayer player, final int windowId, final BlockPosition blockPosition,
                                   final Key blockType, final int gridWidth, final int gridHeight) {
        super(player, windowId, blockPosition, blockType);
        this.grid = new CraftingContainer(gridWidth, gridHeight);
    }

    /**
     * The window slot of the result.
     */
    protected abstract int resultSlot();

    /**
     * Whether what the player inventory cannot take back on close stays in the grid rather than
     * being dropped at the player's feet. The inventory grid persists with the player, a crafting table grid does not.
     */
    protected abstract boolean keepsGridOnClose();

    protected final ResultSlot newResultSlot() {
        return new ResultSlot();
    }

    protected final void addGridSlots(final List<Slot> slots) {
        for (int i = 0; i < grid.size(); i++) {
            slots.add(new Slot(grid, i));
        }
    }

    /**
     * {@return what the result slot shows}
     */
    public final ItemStack result() {
        return currentResult;
    }

    /**
     * {@return the grid}
     */
    public final CraftingContainer grid() {
        return grid;
    }

    /**
     * {@return the grid, what the window shows above the player inventory}
     */
    @Override
    public final Container container() {
        return grid;
    }

    @Override
    protected final void slotsChanged() {
        refreshResult();
    }

    /**
     * Looks the grid up in the recipe registry, unless it did not change since the last time.
     */
    public final void refreshResult() {
        final CraftingInput input = grid.asInput();
        if (input.equals(lastInput)) {
            return;
        }
        lastInput = input;
        final CraftingMatch match = player.connection().server().registries().recipes()
                .matchCrafting(input)
                .orElse(null);
        final PlayerPrepareCraftEvent event =
                player.connection().server().events().post(new PlayerPrepareCraftEventImpl(player, input, match));
        currentMatch = match;
        currentResult = match == null ? ItemStack.EMPTY : event.result();
    }

    @Override
    protected final boolean handleClick(final int slot, final int button, final int mode) {
        if (slot != resultSlot()) {
            return false;
        }
        switch (mode) {
            case ServerboundContainerClickPacket.MODE_PICKUP -> takeResultToCursor();
            case ServerboundContainerClickPacket.MODE_QUICK_MOVE -> craftAllIntoInventory();
            case ServerboundContainerClickPacket.MODE_SWAP -> craftIntoHotbar(button);
            case ServerboundContainerClickPacket.MODE_THROW -> {
                // Would throw the result on the ground: without item entities, craft nothing.
            }
            default -> {
                // Cloning, dragging and double clicks treat the result like any other slot.
                return false;
            }
        }
        return true;
    }

    private void takeResultToCursor() {
        final ItemStack result = currentResult;
        if (result.isEmpty()) {
            return;
        }
        final ItemStack cursor = carried();
        if (cursor.isEmpty()) {
            final ItemStack crafted = craftOnce(false);
            if (!crafted.isEmpty()) {
                setCarried(crafted);
            }
            return;
        }
        if (cursor.isSimilar(result) && cursor.count() + result.count() <= maxStackSize(cursor)) {
            final ItemStack crafted = craftOnce(false);
            if (!crafted.isEmpty()) {
                setCarried(cursor.withCount(cursor.count() + crafted.count()));
            }
        }
    }

    private void craftAllIntoInventory() {
        final ItemStack first = currentResult;
        for (int crafts = 0; crafts < MAX_SHIFT_CRAFTS; crafts++) {
            final ItemStack result = currentResult;
            if (result.isEmpty() || !result.id().equals(first.id())
                    || !canFit(result, inventoryFrom(), inventoryTo())) {
                return;
            }
            final ItemStack crafted = craftOnce(true);
            if (crafted.isEmpty()) {
                return;
            }
            final ItemStack leftover = moveItemStackTo(crafted, inventoryFrom(), inventoryTo(), true);
            if (!leftover.isEmpty()) {
                // canFit said it would fit: only a listener filling the inventory mid-craft gets here.
                if (carried().isEmpty()) {
                    setCarried(leftover);
                } else {
                    dropForPlayer(leftover);
                }
                return;
            }
        }
    }

    private void craftIntoHotbar(final int button) {
        final int target = swapTarget(button);
        final PlayerInventory inventory = player.inventory();
        if (target < 0 || currentResult.isEmpty() || !inventory.get(target).isEmpty()) {
            return;
        }
        final ItemStack crafted = craftOnce(false);
        if (!crafted.isEmpty()) {
            inventory.set(target, crafted);
        }
    }

    /**
     * Crafts the current result once: fires the event, then takes the ingredients.
     *
     * @return what the craft gave, {@link ItemStack#EMPTY} when nothing was crafted
     */
    private ItemStack craftOnce(final boolean shiftClick) {
        refreshResult();
        final CraftingMatch match = currentMatch;
        final ItemStack result = currentResult;
        if (match == null || result.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final PlayerCraftItemEvent event = player.connection().server().events()
                .post(new PlayerCraftItemEventImpl(player, match, result, shiftClick));
        if (event.isCancelled()) {
            return ItemStack.EMPTY;
        }
        consume(match);
        refreshResult();
        return result;
    }

    private void consume(final CraftingMatch match) {
        for (int slot = 0; slot < grid.size(); slot++) {
            final int wanted = match.consumed(slot);
            if (wanted <= 0) {
                continue;
            }
            final ItemStack stack = grid.get(slot);
            final int taken = Math.min(wanted, stack.count());
            grid.set(slot, stack.count() > taken ? stack.withCount(stack.count() - taken) : ItemStack.EMPTY);

            final ItemStack remainder = CraftingRemainders.of(stack.withCount(taken));
            if (!remainder.isEmpty()) {
                placeRemainder(slot, remainder);
            }
        }
    }

    /**
     * Leaves a remainder in its grid slot when it can, gives it to the player otherwise.
     */
    private void placeRemainder(final int slot, final ItemStack remainder) {
        ItemStack left = remainder;
        final ItemStack inSlot = grid.get(slot);
        if (inSlot.isEmpty()) {
            final int placed = Math.min(left.count(), maxStackSize(left));
            grid.set(slot, left.withCount(placed));
            left = left.count() > placed ? left.withCount(left.count() - placed) : ItemStack.EMPTY;
        } else if (inSlot.isSimilar(left)) {
            final int placed = Math.min(left.count(), maxStackSize(inSlot) - inSlot.count());
            if (placed > 0) {
                grid.set(slot, inSlot.withCount(inSlot.count() + placed));
                left = left.count() > placed ? left.withCount(left.count() - placed) : ItemStack.EMPTY;
            }
        }
        if (!left.isEmpty()) {
            left = giveToPlayer(left);
        }
        if (!left.isEmpty()) {
            dropForPlayer(left);
        }
    }

    /**
     * Whether the whole of {@code stack} fits in the slots {@code from} (inclusive) to {@code to}
     * (exclusive).
     */
    private boolean canFit(final ItemStack stack, final int from, final int to) {
        int room = 0;
        for (int index = from; index < to && room < stack.count(); index++) {
            final Slot slot = slot(index);
            if (!slot.mayPlace(stack)) {
                continue;
            }
            final ItemStack inSlot = slot.get();
            if (inSlot.isEmpty()) {
                room += slot.maxStackSize(stack);
            } else if (inSlot.isSimilar(stack)) {
                room += Math.max(0, slot.maxStackSize(inSlot) - inSlot.count());
            }
        }
        return room >= stack.count();
    }

    /**
     * Gives the grid back to the player, as vanilla does when the window closes.
     */
    @Override
    protected void onClosed() {
        for (int slot = 0; slot < grid.size(); slot++) {
            final ItemStack stack = grid.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            final ItemStack leftover = giveToPlayer(stack);
            grid.set(slot, keepsGridOnClose() ? leftover : ItemStack.EMPTY);
            if (!leftover.isEmpty() && !keepsGridOnClose()) {
                dropForPlayer(leftover);
            }
        }
        refreshResult();
    }

    /**
     * The result slot: shows what the grid makes, takes nothing.
     */
    protected final class ResultSlot extends Slot {

        private ResultSlot() {
            super(grid, 0);
        }

        @Override
        public ItemStack get() {
            return currentResult;
        }

        @Override
        public void set(final ItemStack stack) {
            // Only crafting empties the result slot.
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeForPickupAll() {
            return false;
        }
    }
}
