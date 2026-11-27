package fr.euphyllia.fidorial.server.world.block.hopper;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.inventory.BlockViewerTracker;
import fr.euphyllia.fidorial.server.inventory.CrafterContainer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * The nine slots of a crafter, as a hopper sees them: the grid its viewers share while its window
 * is open, the one its block entity keeps otherwise, saved back after every change.
 *
 * <p>Like vanilla, a hopper fills a crafter one item at a time, spreading them over its enabled
 * slots: an item goes into the first empty one, then, once none is left, onto the smallest stack of
 * the same item, the first one of them. A disabled slot takes nothing.</p>
 */
final class CrafterSlots implements Container {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(CrafterSlots.class);

    private final ServerWorld world;
    private final BlockPosition position;
    private @Nullable CompoundBinaryTag readFrom;
    private CrafterContainer read = new CrafterContainer();

    CrafterSlots(final ServerWorld world, final BlockPosition position) {
        this.world = world;
        this.position = position;
    }

    @Override
    public int size() {
        return CrafterContainer.SIZE;
    }

    @Override
    public ItemStack get(final int slot) {
        return crafter().get(slot);
    }

    @Override
    public void set(final int slot, final @Nullable ItemStack stack) {
        final CrafterContainer crafter = crafter();
        crafter.set(slot, stack);
        save(crafter);
    }

    @Override
    public void clear() {
        final CrafterContainer crafter = crafter();
        if (!crafter.isEmpty()) {
            crafter.clear();
            save(crafter);
        }
    }

    @Override
    public boolean isEmpty() {
        return crafter().isEmpty();
    }

    @Override
    public ItemStack[] contents() {
        return crafter().contents();
    }

    /**
     * {@return how many items of a stack the crafter would take, from {@code 0} to its count}
     */
    int room(final ItemStack stack) {
        final CrafterContainer crafter = crafter();
        final ItemStack[] slots = crafter.contents();
        int placed = 0;
        while (placed < stack.count()) {
            final int slot = slotFor(crafter, slots, stack);
            if (slot < 0) {
                break;
            }
            slots[slot] = slots[slot].isEmpty() ? stack.withCount(1) : slots[slot].plus(1);
            placed++;
        }
        return placed;
    }

    /**
     * Puts the items of a stack into the crafter, one at a time.
     *
     * @return what did not fit, empty when every item did
     */
    ItemStack insert(final ItemStack stack) {
        final CrafterContainer crafter = crafter();
        final ItemStack[] slots = crafter.contents();
        int placed = 0;
        while (placed < stack.count()) {
            final int slot = slotFor(crafter, slots, stack);
            if (slot < 0) {
                break;
            }
            slots[slot] = slots[slot].isEmpty() ? stack.withCount(1) : slots[slot].plus(1);
            crafter.set(slot, slots[slot]);
            placed++;
        }
        if (placed > 0) {
            save(crafter);
        }
        return placed == stack.count() ? ItemStack.EMPTY : stack.withCount(stack.count() - placed);
    }

    /**
     * {@return the slot the next item of a stack goes to, {@code -1} when none takes it}
     *
     * @param slots what the slots of the crafter hold, the items already placed included
     */
    private static int slotFor(final CrafterContainer crafter, final ItemStack[] slots, final ItemStack stack) {
        for (int slot = 0; slot < slots.length; slot++) {
            if (crafter.isSlotDisabled(slot)) {
                continue;
            }
            final ItemStack held = slots[slot];
            if (held.isEmpty()) {
                return slot;
            }
            if (held.isSimilar(stack) && held.count() < HopperContainers.maxStackSize(held)
                    && !smallerAfter(crafter, slots, slot, held)) {
                return slot;
            }
        }
        return -1;
    }

    /**
     * {@return {@code true} if an enabled slot after {@code slot} is empty, or holds fewer of the
     * same item than it}
     */
    private static boolean smallerAfter(final CrafterContainer crafter, final ItemStack[] slots, final int slot,
                                        final ItemStack held) {
        for (int other = slot + 1; other < slots.length; other++) {
            if (crafter.isSlotDisabled(other)) {
                continue;
            }
            final ItemStack stack = slots[other];
            if (stack.isEmpty() || (stack.count() < held.count() && stack.isSimilar(held))) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@return the grid of the crafter: the one its viewers share while its window is open, the one
     * its block entity keeps otherwise}
     */
    private CrafterContainer crafter() {
        final BlockViewerTracker viewers = FidorialServer.getInstance().players().blockViewers();
        if (viewers.count(world.key(), position) > 0) {
            return viewers.shared(world.key(), position, () -> load(world.blockEntityData(position)));
        }
        final CompoundBinaryTag data = world.blockEntityData(position);
        if (data != readFrom) {
            read = load(data);
            readFrom = data;
        }
        return read;
    }

    private CrafterContainer load(final @Nullable CompoundBinaryTag data) {
        try {
            return CrafterContainer.load(data);
        } catch (final IOException exception) {
            LOGGER.error("Unable to read the crafter at {} {} {}, the hoppers see it empty",
                    position.blockX(), position.blockY(), position.blockZ(), exception);
            return new CrafterContainer();
        }
    }

    private void save(final CrafterContainer crafter) {
        final CompoundBinaryTag data = world.blockEntityData(position);
        if (data == null) {
            return;
        }
        try {
            final CompoundBinaryTag saved = crafter.save(data);
            if (world.setBlockEntityData(position, saved) && crafter == read) {
                readFrom = saved;
            }
        } catch (final IOException exception) {
            LOGGER.error("Unable to save the crafter at {} {} {}", position.blockX(), position.blockY(), position.blockZ(),
                    exception);
        }
    }
}
