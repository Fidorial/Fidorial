package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.recipe.CraftingInput;
import fr.fidorial.recipe.CraftingMatch;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.MenuTypeKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The window of a crafter: its nine slots in 0 to 8, the player inventory, then in 45 what the
 * grid would make, shown but never taken.
 */
public final class CrafterMenu extends ContainerMenu {

    public static final int RESULT_SLOT = 45;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(CrafterMenu.class);
    private static final int TRIGGERED_PROPERTY = CrafterContainer.SIZE;

    private final ServerWorld world;
    private final BlockPosition position;
    private final CrafterContainer crafter;
    private @Nullable CraftingInput lastInput;
    private ItemStack preview = ItemStack.EMPTY;

    public CrafterMenu(final ServerPlayer player, final int windowId, final ServerWorld world,
                       final BlockPosition position, final CrafterContainer crafter) {
        super(player, windowId, position, BlockTypeKeys.CRAFTER.key());
        this.world = world;
        this.position = position;
        this.crafter = crafter;
    }

    public CrafterContainer crafter() {
        return crafter;
    }

    @Override
    public Container container() {
        return crafter;
    }

    /**
     * {@return what the grid would make, shown in the result slot}
     *
     * <p>Like vanilla's slot listener, it follows the grid whoever changed it, so that every player
     * looking at the crafter sees the same result.</p>
     */
    public ItemStack preview() {
        refreshPreview();
        return preview;
    }

    @Override
    protected List<Slot> createSlots() {
        final List<Slot> slots = new ArrayList<>();
        for (int i = 0; i < CrafterContainer.SIZE; i++) {
            slots.add(new CrafterSlot(crafter, i));
        }
        addPlayerInventory(slots);
        slots.add(new PreviewSlot());
        return slots;
    }

    @Override
    public TypedKey<MenuType> menuType() {
        return MenuTypeKeys.CRAFTER_3X3;
    }

    @Override
    public Component title() {
        return Component.translatable("container.crafter");
    }

    @Override
    protected int dataCount() {
        return CrafterContainer.SIZE + 1;
    }

    @Override
    protected int data(final int property) {
        if (property == TRIGGERED_PROPERTY) {
            return crafter.isTriggered() ? 1 : 0;
        }
        return crafter.isSlotDisabled(property) ? 1 : 0;
    }

    /**
     * Enables or disables one of the nine slots, as the client asks when its empty slot is clicked.
     *
     * @return {@code true} when the slot changed
     */
    public boolean setSlotState(final int slot, final boolean enabled) {
        if (!crafter.setSlotDisabled(slot, !enabled)) {
            return false;
        }
        persist();
        return true;
    }

    @Override
    public void onOpened() {
        player.connection().server().players().blockViewers().open(world.key(), position);
    }

    @Override
    protected void onClosed() {
        persist();
        player.connection().server().players().blockViewers().close(world.key(), position);
    }

    @Override
    protected void slotsChanged() {
        persist();
    }

    /**
     * Like vanilla: the crafter's slots go to the inventory, the inventory goes to the crafter
     * first, then between the main inventory and the hotbar.
     */
    @Override
    protected void quickMove(final int index) {
        final Slot source = slot(index);
        final ItemStack stack = source.get();
        if (stack.isEmpty() || !source.mayPickup()) {
            return;
        }
        ItemStack leftover;
        if (index < CrafterContainer.SIZE) {
            leftover = moveItemStackTo(stack, inventoryFrom(), inventoryTo(), true);
        } else {
            leftover = moveItemStackTo(stack, 0, CrafterContainer.SIZE, false);
            if (leftover.count() == stack.count()) {
                leftover = index < hotbarFrom()
                        ? moveItemStackTo(stack, hotbarFrom(), inventoryTo(), false)
                        : moveItemStackTo(stack, inventoryFrom(), hotbarFrom(), false);
            }
        }
        source.set(leftover);
    }

    private void refreshPreview() {
        final CraftingInput input = crafter.asInput();
        if (input.equals(lastInput)) {
            return;
        }
        lastInput = input;
        preview = player.connection().server().registries().recipes()
                .matchCrafting(input)
                .map(CraftingMatch::result)
                .orElse(ItemStack.EMPTY);
    }

    private void persist() {
        try {
            world.setBlockEntityData(position, crafter.save(world.blockEntityData(position)));
        } catch (final IOException exception) {
            LOGGER.error("Unable to save the crafter at {}", position, exception);
        }
    }

    /**
     * One of the nine slots: holds nothing while disabled.
     */
    private static final class CrafterSlot extends Slot {

        private final CrafterContainer crafter;
        private final int index;

        private CrafterSlot(final CrafterContainer crafter, final int index) {
            super(crafter, index);
            this.crafter = crafter;
            this.index = index;
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return !crafter.isSlotDisabled(index);
        }
    }

    /**
     * The result slot: shows what the grid would make, takes and gives nothing.
     */
    private final class PreviewSlot extends Slot {

        private PreviewSlot() {
            super(crafter, 0);
        }

        @Override
        public ItemStack get() {
            return preview();
        }

        @Override
        public void set(final ItemStack stack) {
            // The crafter crafts on a redstone pulse only.
        }

        @Override
        public boolean mayPlace(final ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup() {
            return false;
        }
    }
}
