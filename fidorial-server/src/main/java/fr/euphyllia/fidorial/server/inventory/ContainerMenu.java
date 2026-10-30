package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundContainerSetContentPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundContainerSetDataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundContainerSetSlotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play.ServerboundContainerClickPacket;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.Container;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MenuType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ContainerMenu {

    private static final int MAIN_FIRST = PlayerInventory.MAIN_START;
    private static final int MAIN_COUNT = PlayerInventory.STORAGE_SIZE - PlayerInventory.MAIN_START;
    private static final int HOTBAR_FIRST = PlayerInventory.HOTBAR_START;
    private static final int HOTBAR_COUNT = PlayerInventory.HOTBAR_SIZE;
    private static final int SWAP_OFFHAND_BUTTON = 40;

    /**
     * How far beyond their block interaction range a player keeps a block's window, as in vanilla.
     */
    private static final double STILL_VALID_REACH_BUFFER = 4.0;

    private static final int DRAG_START = 0;
    private static final int DRAG_ADD_SLOT = 1;
    private static final int DRAG_END = 2;
    private static final int DRAG_SPLIT = 0;
    private static final int DRAG_ONE_EACH = 1;
    private static final int DRAG_CLONE = 2;

    protected final ServerPlayer player;
    private final int windowId;
    private final @Nullable BlockPosition blockPosition;
    private final @Nullable Key blockType;

    private final Set<Integer> dragSlots = new LinkedHashSet<>();
    private @Nullable List<Slot> slots;
    private int inventoryFrom = -1;
    private int inventoryTo = -1;
    private ItemStack carried = ItemStack.EMPTY;
    private final List<ItemStack> remoteSlots = new ArrayList<>();
    private int[] remoteData = new int[0];
    private int stateId;
    private int dragType = -1;
    private boolean dragging;

    /**
     * A window that belongs to no block, such as the player inventory.
     */
    protected ContainerMenu(final ServerPlayer player, final int windowId) {
        this.player = player;
        this.windowId = windowId;
        this.blockPosition = null;
        this.blockType = null;
    }

    /**
     * A window opened on a block, in the player's world, like vanilla's
     * {@code ContainerLevelAccess}.
     *
     * @param blockPosition where the block is
     * @param blockType     the block the window needs there to stay open
     */
    protected ContainerMenu(final ServerPlayer player, final int windowId, final BlockPosition blockPosition,
                            final Key blockType) {
        this.player = player;
        this.windowId = windowId;
        this.blockPosition = blockPosition;
        this.blockType = blockType;
    }

    /**
     * The largest stack {@code stack} may form, wherever it sits.
     */
    protected static int maxStackSize(final ItemStack stack) {
        return Math.max(1, ItemDefaults.maxStackSize(stack.id(), stack));
    }

    private static ItemStack remainderOrEmpty(final ItemStack source, final int count) {
        return count <= 0 ? ItemStack.EMPTY : source.withCount(count);
    }

    public final int windowId() {
        return windowId;
    }

    public final int stateId() {
        return stateId;
    }

    public final ItemStack carried() {
        return carried;
    }

    /**
     * {@return the kind of window the client draws, from {@code MenuTypeKeys}}
     */
    public abstract TypedKey<MenuType> menuType();

    public abstract Component title();

    /**
     * {@return what the window shows above the player inventory, such as a crafting grid or the
     * player's ender chest}
     */
    public abstract Container container();

    /**
     * {@return the block this window was opened on, in the player's world; {@code null} when it
     * belongs to no block}
     */
    public final @Nullable BlockPosition blockPosition() {
        return blockPosition;
    }

    /**
     * Whether the player may keep the window open, checked every tick and before each click, as
     * vanilla's {@code stillValid}: the window closes as soon as it answers {@code false}. A window
     * opened on a block is valid while the block is still there and the player within reach of it.
     */
    public boolean stillValid() {
        if (blockPosition == null || blockType == null) {
            return true;
        }
        return player.world().blockKeyAt(blockPosition).map(blockType::equals).orElse(false)
                && player.isWithinBlockInteractionRange(blockPosition, STILL_VALID_REACH_BUFFER);
    }

    /**
     * Called once the window is shown to the client.
     */
    public void onOpened() {
    }

    /**
     * {@return how many properties ({@code container_set_data}) the window has, such as the
     * disabled slots of a crafter}
     */
    protected int dataCount() {
        return 0;
    }

    /**
     * {@return the current value of a property, from {@code 0} to {@link #dataCount()} excluded}
     */
    protected int data(final int property) {
        return 0;
    }

    /**
     * Lists the slots of the window, in protocol order. Called once, the first time a slot is
     * needed, so that subclasses may rely on their own fields.
     */
    protected abstract List<Slot> createSlots();

    /**
     * Called once the window is closed and the cursor emptied.
     */
    protected void onClosed() {
    }

    /**
     * Vanilla's {@code removed}: the window is closed, the cursor goes back into the inventory, then
     * {@link #onClosed()} lets the window give back or save what it holds.
     */
    public final void removed() {
        returnCarried();
        if (!carried.isEmpty()) {
            // No room left in the inventory: like vanilla, the cursor falls at the player's feet.
            dropForPlayer(carried);
            carried = ItemStack.EMPTY;
        }
        onClosed();
    }

    /**
     * Called after every click, once the slots have settled.
     */
    protected void slotsChanged() {
    }

    /**
     * Gives subclasses the first look at a click, for slots that do not behave like storage, such
     * as a crafting result.
     *
     * @return {@code true} when the click was handled
     */
    protected boolean handleClick(final int slot, final int button, final int mode) {
        return false;
    }

    /**
     * Where a shift-click on {@code slot} sends its stack. The default moves the stack between the
     * container and the player inventory.
     */
    protected void quickMove(final int slot) {
        final Slot source = slot(slot);
        final ItemStack stack = source.get();
        if (stack.isEmpty() || !source.mayPickup()) {
            return;
        }
        final ItemStack leftover = isInventorySlot(slot)
                ? moveItemStackTo(stack, 0, inventoryFrom, false)
                : moveItemStackTo(stack, inventoryFrom, inventoryTo, true);
        source.set(leftover);
    }

    protected final List<Slot> slots() {
        List<Slot> current = slots;
        if (current == null) {
            current = Collections.unmodifiableList(new ArrayList<>(createSlots()));
            slots = current;
        }
        return current;
    }

    protected final Slot slot(final int index) {
        return slots().get(index);
    }

    public final int slotCount() {
        return slots().size();
    }

    /**
     * Appends the 27 main slots then the 9 hotbar slots of the player inventory, the way every
     * window but the inventory itself ends.
     */
    protected final void addPlayerInventory(final List<Slot> slots) {
        final PlayerInventory inventory = player.inventory();
        inventoryFrom = slots.size();
        for (int i = 0; i < MAIN_COUNT; i++) {
            slots.add(new Slot(inventory, MAIN_FIRST + i));
        }
        for (int i = 0; i < HOTBAR_COUNT; i++) {
            slots.add(new Slot(inventory, HOTBAR_FIRST + i));
        }
        inventoryTo = slots.size();
    }

    /**
     * The first window slot of the player inventory added by {@link #addPlayerInventory(List)}.
     */
    protected final int inventoryFrom() {
        slots();
        return inventoryFrom;
    }

    /**
     * One past the last window slot of the player inventory, the hotbar being last.
     */
    protected final int inventoryTo() {
        slots();
        return inventoryTo;
    }

    /**
     * The first window slot of the hotbar.
     */
    protected final int hotbarFrom() {
        return inventoryTo() - HOTBAR_COUNT;
    }

    protected final boolean isInventorySlot(final int windowSlot) {
        return windowSlot >= inventoryFrom() && windowSlot < inventoryTo();
    }

    private boolean isValidSlot(final int windowSlot) {
        return windowSlot >= 0 && windowSlot < slotCount();
    }

    protected final void setCarried(final ItemStack stack) {
        carried = stack;
    }

    public final void click(final ServerboundContainerClickPacket packet) {
        final int slot = packet.slot();
        final int button = packet.button();

        if (packet.mode() != ServerboundContainerClickPacket.MODE_QUICK_CRAFT) {
            resetDrag();
        }

        if (!isValidSlot(slot) || !handleClick(slot, button, packet.mode())) {
            switch (packet.mode()) {
                case ServerboundContainerClickPacket.MODE_PICKUP -> pickup(slot, button);
                case ServerboundContainerClickPacket.MODE_QUICK_MOVE -> {
                    if (isValidSlot(slot)) {
                        quickMove(slot);
                    }
                }
                case ServerboundContainerClickPacket.MODE_SWAP -> swap(slot, button);
                case ServerboundContainerClickPacket.MODE_CLONE -> clone(slot);
                case ServerboundContainerClickPacket.MODE_THROW -> throwOut(slot, button);
                case ServerboundContainerClickPacket.MODE_QUICK_CRAFT -> quickCraft(slot, button);
                case ServerboundContainerClickPacket.MODE_PICKUP_ALL -> pickupAll(slot, button);
                default -> {
                    // Unknown mode: touch nothing, the resync will set the client straight.
                }
            }
        }
        slotsChanged();
    }

    private void pickup(final int index, final int button) {
        if (index == ServerboundContainerClickPacket.SLOT_OUTSIDE) {
            dropCarried(button == 0);
            return;
        }
        if (!isValidSlot(index)) {
            return;
        }
        final Slot slot = slot(index);
        final ItemStack inSlot = slot.get();
        final boolean leftClick = button == 0;

        if (carried.isEmpty()) {
            if (inSlot.isEmpty() || !slot.mayPickup()) {
                return;
            }
            final int taken = leftClick ? inSlot.count() : (inSlot.count() + 1) / 2;
            carried = inSlot.withCount(taken);
            slot.set(remainderOrEmpty(inSlot, inSlot.count() - taken));
            return;
        }

        if (inSlot.isEmpty()) {
            if (!slot.mayPlace(carried)) {
                return;
            }
            final int moved = Math.min(leftClick ? carried.count() : 1, slot.maxStackSize(carried));
            slot.set(carried.withCount(moved));
            carried = remainderOrEmpty(carried, carried.count() - moved);
            return;
        }

        if (inSlot.isSimilar(carried)) {
            if (!slot.mayPlace(carried)) {
                return;
            }
            final int room = slot.maxStackSize(inSlot) - inSlot.count();
            if (room <= 0) {
                return;
            }
            final int moved = Math.min(room, leftClick ? carried.count() : 1);
            slot.set(inSlot.withCount(inSlot.count() + moved));
            carried = remainderOrEmpty(carried, carried.count() - moved);
            return;
        }

        if (slot.mayPlace(carried) && slot.mayPickup() && carried.count() <= slot.maxStackSize(carried)) {
            slot.set(carried);
            carried = inSlot;
        }
    }

    /**
     * The inventory slot a swap click targets: a hotbar slot for the number keys, the off hand
     * for the swap key.
     *
     * @return the inventory slot, {@code -1} for an unknown button
     */
    protected static int swapTarget(final int button) {
        if (button >= 0 && button < HOTBAR_COUNT) {
            return HOTBAR_FIRST + button;
        }
        return button == SWAP_OFFHAND_BUTTON ? PlayerInventory.OFF_HAND : -1;
    }

    private void swap(final int index, final int button) {
        final int target = swapTarget(button);
        if (!isValidSlot(index) || target < 0) {
            return;
        }
        final Slot slot = slot(index);
        final PlayerInventory inventory = player.inventory();
        final ItemStack inSlot = slot.get();
        final ItemStack inTarget = inventory.get(target);
        if (!inSlot.isEmpty() && !slot.mayPickup()) {
            return;
        }
        if (!inTarget.isEmpty() && (!slot.mayPlace(inTarget) || inTarget.count() > slot.maxStackSize(inTarget))) {
            return;
        }
        slot.set(inTarget);
        inventory.set(target, inSlot);
    }

    private void clone(final int index) {
        if (!isValidSlot(index) || player.gameMode() != GameMode.CREATIVE) {
            return;
        }
        if (!carried.isEmpty()) {
            return;
        }
        final ItemStack inSlot = slot(index).get();
        if (inSlot.isEmpty()) {
            return;
        }
        carried = inSlot.withCount(maxStackSize(inSlot));
    }

    /**
     * The drop key over a slot: one item, or the whole stack with {@code button} 1, thrown out where
     * the player looks. Only with an empty cursor, as in vanilla.
     */
    private void throwOut(final int index, final int button) {
        if (!carried.isEmpty() || !isValidSlot(index)) {
            return;
        }
        final Slot slot = slot(index);
        final ItemStack inSlot = slot.get();
        if (inSlot.isEmpty() || !slot.mayPickup()) {
            return;
        }
        final int count = button == 1 ? inSlot.count() : 1;
        slot.set(remainderOrEmpty(inSlot, inSlot.count() - count));
        dropForPlayer(inSlot.withCount(count));
    }

    /**
     * A drag across slots, as vanilla reads it: the button packs a header (start, add a slot, end)
     * and a type (split evenly, one each, or a full stack each for a creative player).
     */
    private void quickCraft(final int index, final int button) {
        final int header = button & 3;
        final int type = button >> 2 & 3;
        switch (header) {
            case DRAG_START -> {
                final boolean validType = type == DRAG_SPLIT || type == DRAG_ONE_EACH
                        || (type == DRAG_CLONE && player.gameMode() == GameMode.CREATIVE);
                if (carried.isEmpty() || !validType) {
                    resetDrag();
                    return;
                }
                dragging = true;
                dragType = type;
                dragSlots.clear();
            }
            case DRAG_ADD_SLOT -> {
                if (!dragging || type != dragType || !isValidSlot(index)) {
                    return;
                }
                final Slot slot = slot(index);
                final ItemStack inSlot = slot.get();
                if (slot.mayPlace(carried) && (inSlot.isEmpty() || inSlot.isSimilar(carried))
                        && (dragType == DRAG_CLONE || carried.count() > dragSlots.size())) {
                    dragSlots.add(index);
                }
            }
            case DRAG_END -> {
                if (dragging && type == dragType) {
                    applyDrag();
                }
                resetDrag();
            }
            default -> resetDrag();
        }
    }

    private void applyDrag() {
        if (carried.isEmpty() || dragSlots.isEmpty()) {
            return;
        }
        if (dragSlots.size() == 1) {
            // Like vanilla, a drag over one slot is a plain click on it.
            final int only = dragSlots.iterator().next();
            if (dragType != DRAG_CLONE) {
                pickup(only, dragType);
            }
            return;
        }
        final int perSlot = switch (dragType) {
            case DRAG_SPLIT -> carried.count() / dragSlots.size();
            case DRAG_ONE_EACH -> 1;
            default -> maxStackSize(carried);
        };
        int remaining = carried.count();
        for (final int index : dragSlots) {
            final Slot slot = slot(index);
            final ItemStack inSlot = slot.get();
            final int current = inSlot.isEmpty() ? 0 : inSlot.count();
            final int count = Math.min(perSlot + current, slot.maxStackSize(carried));
            slot.set(carried.withCount(count));
            remaining -= count - current;
        }
        carried = remainderOrEmpty(carried, remaining);
    }

    private void resetDrag() {
        dragging = false;
        dragType = -1;
        dragSlots.clear();
    }

    /**
     * A double click: like vanilla, gathers onto the cursor, unless the slot clicked holds
     * something it could give, first the partial stacks then the full ones, from the first slot
     * for the left button and from the last one for the right button.
     */
    private void pickupAll(final int index, final int button) {
        if (carried.isEmpty() || !isValidSlot(index)) {
            return;
        }
        final Slot clicked = slot(index);
        if (!clicked.get().isEmpty() && clicked.mayPickup()) {
            return;
        }
        final int max = maxStackSize(carried);
        final boolean fromLast = button != 0;
        for (int pass = 0; pass < 2 && carried.count() < max; pass++) {
            for (int step = 0; step < slotCount() && carried.count() < max; step++) {
                final Slot slot = slot(fromLast ? slotCount() - 1 - step : step);
                final ItemStack inSlot = slot.get();
                if (inSlot.isEmpty() || !inSlot.isSimilar(carried) || !slot.canTakeForPickupAll()) {
                    continue;
                }
                if (pass == 0 && inSlot.count() >= maxStackSize(inSlot)) {
                    continue;
                }
                final int moved = Math.min(max - carried.count(), inSlot.count());
                carried = carried.withCount(carried.count() + moved);
                slot.set(remainderOrEmpty(inSlot, inSlot.count() - moved));
            }
        }
    }

    /**
     * A click outside the window: the whole cursor with the left button, one item with the right one.
     */
    private void dropCarried(final boolean wholeStack) {
        if (carried.isEmpty()) {
            return;
        }
        final int count = wholeStack ? carried.count() : 1;
        dropForPlayer(carried.withCount(count));
        carried = remainderOrEmpty(carried, carried.count() - count);
    }

    /**
     * Empties the cursor and gives back what it held, for a player who loses it at once, such as
     * on death.
     *
     * @return what the cursor held, {@link ItemStack#EMPTY} when nothing
     */
    public final ItemStack takeCarried() {
        final ItemStack taken = carried;
        carried = ItemStack.EMPTY;
        return taken;
    }

    /**
     * Throws {@code stack} out of the player's hands, where they look.
     */
    protected final void dropForPlayer(final ItemStack stack) {
        if (!stack.isEmpty()) {
            player.connection().server().entities().dropFromPlayer(player, stack);
        }
    }

    /**
     * Moves as much of {@code stack} as fits into the slots {@code from} (inclusive) to {@code to}
     * (exclusive): first onto similar stacks, then into empty slots, last to first when
     * {@code reverse}.
     *
     * @return what did not fit, {@link ItemStack#EMPTY} when everything did
     */
    protected final ItemStack moveItemStackTo(final ItemStack stack, final int from, final int to,
                                              final boolean reverse) {
        ItemStack remaining = stack;
        for (int pass = 0; pass < 2 && !remaining.isEmpty(); pass++) {
            for (int step = 0; step < to - from && !remaining.isEmpty(); step++) {
                final Slot slot = slot(reverse ? to - 1 - step : from + step);
                final ItemStack inSlot = slot.get();
                if (!slot.mayPlace(remaining)) {
                    continue;
                }
                if (pass == 0) {
                    if (inSlot.isEmpty() || !inSlot.isSimilar(remaining)) {
                        continue;
                    }
                    final int room = slot.maxStackSize(inSlot) - inSlot.count();
                    if (room <= 0) {
                        continue;
                    }
                    final int moved = Math.min(room, remaining.count());
                    slot.set(inSlot.withCount(inSlot.count() + moved));
                    remaining = remainderOrEmpty(remaining, remaining.count() - moved);
                } else if (inSlot.isEmpty()) {
                    final int moved = Math.min(slot.maxStackSize(remaining), remaining.count());
                    slot.set(remaining.withCount(moved));
                    remaining = remainderOrEmpty(remaining, remaining.count() - moved);
                }
            }
        }
        return remaining;
    }

    /**
     * Vanilla's {@code sendAllDataToRemote}: sends the whole window, its contents then every
     * property, and remembers it as what the client knows.
     */
    public final void sendAllDataToRemote() {
        stateId++;
        final List<ItemStack> contents = new ArrayList<>(slotCount());
        for (final Slot slot : slots()) {
            contents.add(slot.get());
        }
        remoteSlots.clear();
        remoteSlots.addAll(contents);
        send(new ClientboundContainerSetContentPacket(windowId, stateId, contents, carried, registries()));

        remoteData = new int[dataCount()];
        for (int property = 0; property < remoteData.length; property++) {
            remoteData[property] = data(property);
            send(new ClientboundContainerSetDataPacket(windowId, property, remoteData[property]));
        }
    }

    /**
     * Vanilla's {@code broadcastChanges}, called every tick: one packet per slot and property that
     * changed since the client last heard of them, whoever changed them. This is how players
     * looking at the same crafter see each other's moves.
     *
     * <p>Sends nothing until the window has been {@linkplain #sendAllDataToRemote() sent} once.</p>
     */
    public final void broadcastChanges() {
        if (remoteSlots.size() == slotCount()) {
            for (int index = 0; index < remoteSlots.size(); index++) {
                final ItemStack current = slot(index).get();
                if (!current.equals(remoteSlots.get(index))) {
                    remoteSlots.set(index, current);
                    send(new ClientboundContainerSetSlotPacket(windowId, ++stateId, index, current, registries()));
                }
            }
        }
        if (remoteData.length == dataCount()) {
            for (int property = 0; property < remoteData.length; property++) {
                final int current = data(property);
                if (current != remoteData[property]) {
                    remoteData[property] = current;
                    send(new ClientboundContainerSetDataPacket(windowId, property, current));
                }
            }
        }
    }

    private void send(final ClientboundPacket packet) {
        player.connection().send(packet);
    }

    private ServerRegistries registries() {
        return player.connection().server().registries().serverRegistries();
    }

    /**
     * Vanilla's {@code transferState}: once {@code closed} is closed, this window takes over what
     * the client was last told of the slots both show (the player inventory), so that nothing has
     * to be sent again.
     */
    public final void transferState(final ContainerMenu closed) {
        if (remoteSlots.size() != slotCount() || closed.remoteSlots.size() != closed.slotCount()) {
            return;
        }
        for (int index = 0; index < slotCount(); index++) {
            final Slot slot = slot(index);
            for (int other = 0; other < closed.slotCount(); other++) {
                final Slot candidate = closed.slot(other);
                if (candidate.container == slot.container && candidate.index == slot.index) {
                    remoteSlots.set(index, closed.remoteSlots.get(other));
                    break;
                }
            }
        }
    }

    /**
     * Puts the cursor back into the player inventory.
     */
    public final void returnCarried() {
        if (carried.isEmpty()) {
            return;
        }
        carried = moveItemStackTo(carried, inventoryFrom(), inventoryTo(), false);
    }

    /**
     * Puts {@code stack} into the player inventory, the way vanilla gives back the contents of a
     * crafting grid.
     *
     * @return what did not fit
     */
    protected final ItemStack giveToPlayer(final ItemStack stack) {
        return moveItemStackTo(stack, inventoryFrom(), inventoryTo(), false);
    }

    /**
     * One slot of a window, backed by one slot of a container.
     */
    protected static class Slot {

        private final Container container;
        private final int index;

        protected Slot(final Container container, final int index) {
            this.container = container;
            this.index = index;
        }

        public ItemStack get() {
            return container.get(index);
        }

        public void set(final ItemStack stack) {
            container.set(index, stack);
        }

        /**
         * Whether a player may put {@code stack} here.
         */
        public boolean mayPlace(final ItemStack stack) {
            return true;
        }

        /**
         * Whether a player may take what is here.
         */
        public boolean mayPickup() {
            return true;
        }

        /**
         * Whether a double click elsewhere may gather what is here.
         */
        public boolean canTakeForPickupAll() {
            return mayPickup();
        }

        /**
         * The largest stack of {@code stack} this slot holds.
         */
        public int maxStackSize(final ItemStack stack) {
            return ContainerMenu.maxStackSize(stack);
        }
    }
}
