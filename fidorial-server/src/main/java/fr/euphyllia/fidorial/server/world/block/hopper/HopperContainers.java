package fr.euphyllia.fidorial.server.world.block.hopper;

import fr.euphyllia.fidorial.server.inventory.BlockEntityInventory;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BarrelBlock;
import fr.euphyllia.fidorial.server.world.block.ContainerBlocks;
import fr.euphyllia.fidorial.server.world.block.CrafterBlock;
import fr.euphyllia.fidorial.server.world.block.chest.ChestBlocks;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.type.Chest;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The containers a hopper moves items into and out of: a chest, the two halves of a large chest
 * seen as one, a barrel, another hopper, or a crafter; and how items go into them.
 *
 * <p>Like vanilla, items go into the slots in order: each one takes what it can, an empty slot a
 * whole stack, a slot holding the same item what it lacks to be full. A crafter spreads them over
 * its slots instead, as {@link CrafterSlots} tells.</p>
 */
final class HopperContainers {

    private HopperContainers() {
        throw new UnsupportedOperationException("HopperContainers cannot be instantiated.");
    }

    /**
     * {@return the container of the block at a position, empty for any other block, for a block
     * without its block entity, or when its chunk is not loaded}
     *
     * <p>A hopper looks at the same blocks on every tick: the container it looked at last is given
     * back while it still spans the same blocks, so that its slots are not read again from its block
     * entity unless they changed.</p>
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param cached   the container found there last time, {@code null} for none
     */
    static @Nullable Target at(final ServerWorld world, final BlockPosition position, final @Nullable Target cached) {
        final BlockData state = ContainerBlocks.stateAt(world, position);
        if (state == null || world.blockEntityData(position) == null) {
            return null;
        }
        final Kind kind = kind(state);
        if (kind == null) {
            return null;
        }
        final List<BlockPosition> blocks = kind == Kind.CHEST && state instanceof final Chest chest
                ? ChestBlocks.halves(world, position, chest)
                : List.of(position);
        if (cached != null && cached.kind == kind && cached.blocks.equals(blocks)
                && cached.container.size() == size(world, kind, blocks)) {
            return cached;
        }
        final Container container = switch (kind) {
            case CHEST -> ChestBlocks.contents(world, blocks);
            case BARREL -> BarrelBlock.inventory(world, position);
            case HOPPER -> HopperBlock.inventory(world, position);
            case CRAFTER -> new CrafterSlots(world, position);
        };
        return new Target(kind, blocks, container);
    }

    /**
     * {@return {@code true} if a block keeps items a hopper moves, whether or not it has its block
     * entity}
     */
    static boolean isContainer(final @Nullable BlockData state) {
        return state != null && kind(state) != null;
    }

    private static @Nullable Kind kind(final BlockData state) {
        if (state instanceof Chest && ChestBlocks.behaviour(state.key()) != null) {
            return Kind.CHEST;
        }
        if (state.key().equals(BarrelBlock.KEY)) {
            return Kind.BARREL;
        }
        if (state.key().equals(HopperBlock.KEY)) {
            return Kind.HOPPER;
        }
        if (state.key().equals(CrafterBlock.KEY)) {
            return Kind.CRAFTER;
        }
        return null;
    }

    /**
     * {@return how many slots a container shows today: the rows of a barrel follow the configuration
     * of its world}
     */
    private static int size(final ServerWorld world, final Kind kind, final List<BlockPosition> blocks) {
        return switch (kind) {
            case CHEST -> blocks.size() * 27;
            case BARREL -> world.configuration().blocks().barrel().rows() * 9;
            case HOPPER -> 5;
            case CRAFTER -> 9;
        };
    }

    /**
     * {@return the largest stack an item forms in a slot}
     */
    static int maxStackSize(final ItemStack stack) {
        return Math.max(1, ItemDefaults.maxStackSize(stack.id(), stack));
    }

    /**
     * {@return how many items of a stack a container would take, from {@code 0} to its count}
     */
    static int room(final Container container, final ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (container instanceof final CrafterSlots crafter) {
            return crafter.room(stack);
        }
        final int max = maxStackSize(stack);
        int room = 0;
        for (final ItemStack held : container.contents()) {
            if (held.isEmpty()) {
                room += max;
            } else if (held.isSimilar(stack)) {
                room += Math.max(0, maxStackSize(held) - held.count());
            }
            if (room >= stack.count()) {
                return stack.count();
            }
        }
        return room;
    }

    /**
     * Puts the items of a stack into a container, as one step against the other threads looking
     * into it.
     *
     * @return what did not fit, empty when every item did
     */
    static ItemStack insert(final Container container, final ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (container instanceof final CrafterSlots crafter) {
            return crafter.insert(stack);
        }
        final ItemStack[] left = {stack};
        BlockEntityInventory.atomically(List.of(container), () -> left[0] = insertNow(container, stack));
        return left[0];
    }

    private static ItemStack insertNow(final Container container, final ItemStack stack) {
        int left = stack.count();
        final ItemStack[] contents = container.contents();
        for (int slot = 0; slot < contents.length && left > 0; slot++) {
            final ItemStack held = contents[slot];
            if (held.isEmpty()) {
                final int put = Math.min(left, maxStackSize(stack));
                container.set(slot, stack.withCount(put));
                left -= put;
            } else if (held.isSimilar(stack)) {
                final int put = Math.min(left, maxStackSize(held) - held.count());
                if (put > 0) {
                    container.set(slot, held.withCount(held.count() + put));
                    left -= put;
                }
            }
        }
        return left == 0 ? ItemStack.EMPTY : stack.withCount(left);
    }

    /**
     * {@return {@code true} if every slot of a container holds a full stack}
     */
    static boolean isFull(final Container container) {
        for (final ItemStack held : container.contents()) {
            if (held.isEmpty() || held.count() < maxStackSize(held)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves part of the stack of a slot into another container, as one step against the other
     * threads looking into them: a player of a neighbouring region may be taking from the same
     * slots. Nothing moves when the slot no longer holds what the move was decided on.
     *
     * @param from   the container moved out of
     * @param slot   the slot of {@code from}
     * @param seen   what the slot held when the move was decided
     * @param moving what to move, part of {@code seen}
     * @param into   the container moved into
     * @return what could not be put into {@code into}, all of {@code moving} when nothing moved
     */
    static ItemStack move(final Container from, final int slot, final ItemStack seen, final ItemStack moving,
                          final Container into) {
        final AtomicReference<ItemStack> left = new AtomicReference<>(moving);
        BlockEntityInventory.atomically(List.of(from, into), () -> {
            if (!from.get(slot).equals(seen)) {
                return;
            }
            from.set(slot, without(seen, moving.count()));
            left.set(insert(into, moving));
            giveBack(from, slot, left.get());
        });
        return left.get();
    }

    /**
     * {@return a stack with {@code taken} items fewer, empty once none is left}
     */
    static ItemStack without(final ItemStack stack, final int taken) {
        return taken >= stack.count() ? ItemStack.EMPTY : stack.withCount(stack.count() - taken);
    }

    /**
     * Puts back into a slot what could not be moved out of it.
     */
    static void giveBack(final Container container, final int slot, final ItemStack left) {
        if (left.isEmpty()) {
            return;
        }
        final ItemStack held = container.get(slot);
        container.set(slot, held.isEmpty() ? left : held.withCount(held.count() + left.count()));
    }

    /**
     * What kind of block a container is.
     */
    enum Kind {
        CHEST,
        BARREL,
        HOPPER,
        CRAFTER
    }

    /**
     * A container a hopper found, and the blocks it spans.
     *
     * @param kind      what it is
     * @param blocks    the blocks it spans: the two halves of a large chest, the block otherwise
     * @param container its slots
     */
    record Target(Kind kind, List<BlockPosition> blocks, Container container) {
    }
}
