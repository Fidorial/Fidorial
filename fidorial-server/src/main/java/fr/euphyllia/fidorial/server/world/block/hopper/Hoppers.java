package fr.euphyllia.fidorial.server.world.block.hopper;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.WorldConfiguration;
import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.euphyllia.fidorial.server.events.block.HopperMoveItemEventImpl;
import fr.euphyllia.fidorial.server.events.block.HopperPickupItemEventImpl;
import fr.euphyllia.fidorial.server.inventory.BlockHopperInventory;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.ContainerBlocks;
import fr.euphyllia.fidorial.server.world.block.blockentity.TickingBlockEntities;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.event.Event;
import fr.fidorial.event.block.HopperMoveItemEvent;
import fr.fidorial.event.block.HopperPickupItemEvent;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.data.type.Hopper;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ticks the hoppers, on every game tick of their region, as vanilla does.
 *
 * <p>A hopper counts its cooldown down on every tick. Once it is over, an enabled hopper pushes
 * items out of its first slot holding something the container it faces takes, then pulls items
 * from the first slot of the container above it holding something it takes; after either one, it
 * waits again the cooldown the configuration of its world gives, and moves as many items at once
 * as that configuration gives. When nothing could move, it tries again on the next tick.</p>
 *
 * <p>An enabled hopper with no container above it also picks up, on every tick, without waiting
 * for its cooldown nor starting it, the items lying in its bowl, and those lying in the space above
 * it unless a full block covers it.
 * An empty hopper fed by another one waits its cooldown before passing the items on, like vanilla,
 * so that items travel down a line of hoppers at the pace of the cooldown.</p>
 *
 * <p>The cooldown of each hopper is kept here, not in its block entity: a hopper read from disk is
 * ready to move items.</p>
 */
public final class Hoppers implements TickingBlockEntities.Ticker {

    public static final Hoppers INSTANCE = new Hoppers();

    /**
     * Where an item lying above a hopper starts to be picked up: the top of its bowl, 11 pixels
     * high, as vanilla.
     */
    private static final double PICKUP_FROM = 11.0 / 16.0;

    /**
     * Where an item lying above a hopper stops being picked up: the top of the block above it.
     */
    private static final double PICKUP_TO = 2.0;

    /**
     * Where an item lying in a hopper covered by a full block stops being picked up: the top of the
     * hopper itself.
     */
    private static final double BOWL_TOP = 1.0;

    /**
     * The half width and the height of an item entity.
     */
    private static final double ITEM_HALF_WIDTH = 0.125;
    private static final double ITEM_HEIGHT = 0.25;

    private final Map<At, State> states = new ConcurrentHashMap<>();

    private Hoppers() {
    }

    /**
     * {@return the game ticks left before the hopper at a position moves items again, {@code 0}
     * when it is ready or has never ticked}
     */
    public int cooldown(final ServerWorld world, final BlockPosition position) {
        final State state = states.get(new At(world.key(), position));
        return state == null || state.world != world ? 0 : Math.max(0, state.cooldown);
    }

    /**
     * Makes the hopper at a position wait before moving items again.
     *
     * @param ticks the game ticks to wait, {@code 0} to move items on its next tick
     */
    public void setCooldown(final ServerWorld world, final BlockPosition position, final int ticks) {
        Preconditions.checkArgument(ticks >= 0, "The hopper at %s cannot wait %s game ticks, a cooldown is never negative",
                position, ticks);
        state(world, position).cooldown = ticks;
    }

    /**
     * Forgets the cooldown of the hopper at a position, removed or unloaded.
     */
    @Override
    public void forget(final ServerWorld world, final BlockPosition position) {
        states.remove(new At(world.key(), position));
    }

    @Override
    public void tick(final ServerWorld world, final BlockPosition position, final long currentTick) {
        final State state = state(world, position);
        final int waited = state.cooldown;
        state.cooldown = Math.max(0, waited - 1);
        state.tickedAt = currentTick;
        if (!(ContainerBlocks.stateAt(world, position) instanceof final Hopper hopper)
                || !hopper.key().equals(HopperBlock.KEY) || !hopper.isEnabled()) {
            return;
        }
        final BlockHopperInventory inventory = state.inventory(world, position);
        final WorldConfiguration.Hopper config = world.configuration().blocks().hopper();
        if (state.cooldown == 0) {
            boolean moved = false;
            if (!inventory.isEmpty()) {
                moved = push(world, position, hopper.getFacing(), inventory, state, config, currentTick);
            }
            if (!HopperContainers.isFull(inventory)) {
                moved |= pull(position, inventory, state, config, world);
            }
            if (moved) {
                state.cooldown = Math.max(state.cooldown, config.transferCooldown());
            }
        }
        pickUp(world, position, inventory);
    }

    /**
     * Pushes items into the container the hopper faces.
     *
     * @return {@code true} if items moved, or a listener refused it
     */
    private boolean push(final ServerWorld world, final BlockPosition position, final BlockFace facing,
                         final BlockHopperInventory inventory, final State state, final WorldConfiguration.Hopper config,
                         final long currentTick) {
        final BlockPosition target = position.relative(facing);
        final HopperContainers.Target destination = HopperContainers.at(world, target, state.pushTarget);
        state.pushTarget = destination;
        if (destination == null) {
            return false;
        }
        final Container into = destination.container();
        final ItemStack[] contents = inventory.contents();
        for (int slot = 0; slot < contents.length; slot++) {
            final ItemStack stack = contents[slot];
            if (stack.isEmpty()) {
                continue;
            }
            final int fits = HopperContainers.room(into, stack.withCount(Math.min(stack.count(), config.itemsPerTransfer())));
            if (fits <= 0) {
                continue;
            }
            final ItemStack moving = stack.withCount(fits);
            if (refused(inventory, true, position, inventory, target, into, moving)) {
                return true;
            }
            final boolean fedEmptyHopper = destination.kind() == HopperContainers.Kind.HOPPER && into.isEmpty();
            inventory.set(slot, HopperContainers.without(stack, fits));
            final ItemStack left = HopperContainers.insert(into, moving);
            HopperContainers.giveBack(inventory, slot, left);
            if (left.count() < moving.count()) {
                if (fedEmptyHopper) {
                    fed(world, target, currentTick, config.transferCooldown());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Pulls items from the container above the hopper.
     *
     * @return {@code true} if items moved, or a listener refused it
     */
    private boolean pull(final BlockPosition position, final BlockHopperInventory inventory, final State state,
                         final WorldConfiguration.Hopper config, final ServerWorld world) {
        final BlockPosition above = position.relative(BlockFace.UP);
        final HopperContainers.Target source = HopperContainers.at(world, above, state.pullSource);
        state.pullSource = source;
        if (source == null) {
            return false;
        }
        final Container from = source.container();
        final ItemStack[] contents = from.contents();
        for (int slot = 0; slot < contents.length; slot++) {
            final ItemStack stack = contents[slot];
            if (stack.isEmpty()) {
                continue;
            }
            final int fits = HopperContainers.room(inventory, stack.withCount(Math.min(stack.count(), config.itemsPerTransfer())));
            if (fits <= 0) {
                continue;
            }
            final ItemStack moving = stack.withCount(fits);
            if (refused(inventory, false, above, from, position, inventory, moving)) {
                return true;
            }
            from.set(slot, HopperContainers.without(stack, fits));
            final ItemStack left = HopperContainers.insert(inventory, moving);
            HopperContainers.giveBack(from, slot, left);
            if (left.count() < moving.count()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Picks up the items lying in the bowl of the hopper, and those lying above it unless a full
     * block covers it, when no container is above it.
     */
    private void pickUp(final ServerWorld world, final BlockPosition position, final BlockHopperInventory inventory) {
        final BlockPosition above = position.relative(BlockFace.UP);
        final ChunkColumn column = world.loadedColumn(above.chunkX(), above.chunkZ());
        if (column == null || HopperContainers.isContainer(ContainerBlocks.stateAt(world, above))) {
            return;
        }
        final BlockState cover = column.getBlock(above.blockX() & 15, above.blockY(), above.blockZ() & 15);
        final boolean covered = BlockCollision.shape(cover).isFullBlock()
                && !cover.isTagged(BlockTypeTagKeys.DOES_NOT_BLOCK_HOPPERS);
        final List<ServerItemEntity> lying = itemsAbove(world, position, covered ? BOWL_TOP : PICKUP_TO);
        for (final ServerItemEntity item : lying) {
            if (HopperContainers.isFull(inventory)) {
                return;
            }
            final ItemStack stack = item.item();
            if (item.isRemoved() || stack.isEmpty()) {
                continue;
            }
            final int fits = HopperContainers.room(inventory, stack);
            if (fits <= 0) {
                continue;
            }
            final ItemStack taken = stack.withCount(fits);
            if (refused(inventory, item, taken)) {
                continue;
            }
            final ItemStack left = HopperContainers.insert(inventory, taken);
            final int moved = fits - left.count();
            if (moved > 0) {
                item.setItem(HopperContainers.without(stack, moved));
            }
        }
    }

    /**
     * {@return the items, owned by the region ticking the hopper, whose box reaches into the space
     * from the top of its bowl to a height}
     *
     * @param top how high above the bottom of the hopper the space goes
     */
    private static List<ServerItemEntity> itemsAbove(final ServerWorld world, final BlockPosition position, final double top) {
        final double minX = position.blockX();
        final double minY = position.blockY() + PICKUP_FROM;
        final double minZ = position.blockZ();
        final double maxX = minX + 1.0;
        final double maxY = position.blockY() + top;
        final double maxZ = minZ + 1.0;
        final int localX = position.blockX() & 15;
        final int localZ = position.blockZ() & 15;
        // an item over the edge of the hopper may lie in the next chunk
        final int radius = localX == 0 || localX == 15 || localZ == 0 || localZ == 15 ? 1 : 0;
        final List<ServerItemEntity> found = new ArrayList<>();
        world.entityManager().forEachInChunkRange(position.chunkX(), position.chunkZ(), radius, entity -> {
            if (!(entity instanceof final ServerItemEntity item) || item.isRemoved()) {
                return;
            }
            final Location at = item.location();
            if (at.x() + ITEM_HALF_WIDTH > minX && at.x() - ITEM_HALF_WIDTH < maxX
                    && at.y() + ITEM_HEIGHT > minY && at.y() < maxY
                    && at.z() + ITEM_HALF_WIDTH > minZ && at.z() - ITEM_HALF_WIDTH < maxZ) {
                found.add(item);
            }
        });
        found.removeIf(item -> !item.isOwnedByCurrentThread());
        return found;
    }

    /**
     * An empty hopper just fed by another one waits the cooldown before passing the items on, one
     * tick less when it already ticked during this game tick, unless it already waits longer.
     */
    private void fed(final ServerWorld world, final BlockPosition position, final long currentTick, final int cooldown) {
        final State state = state(world, position);
        if (state.cooldown > cooldown) {
            return;
        }
        final boolean ticked = state.tickedAt == currentTick
                && world.scheduler().isOwnedByCurrentThread(world.key(), position.chunk());
        state.cooldown = ticked ? cooldown - 1 : cooldown;
    }

    private State state(final ServerWorld world, final BlockPosition position) {
        final At at = new At(world.key(), position);
        final State existing = states.get(at);
        if (existing != null && existing.world == world) {
            return existing;
        }
        final State created = new State(world);
        // a world loaded again replaces the hoppers of the one it was
        if (existing == null ? states.putIfAbsent(at, created) == null : states.replace(at, existing, created)) {
            return created;
        }
        return state(world, position);
    }

    /**
     * {@return {@code true} if a listener refused the move}
     */
    private static boolean refused(final BlockHopperInventory hopper, final boolean push,
                                   final BlockPosition sourcePosition, final Container source,
                                   final BlockPosition destinationPosition, final Container destination,
                                   final ItemStack item) {
        final SimpleEventBus bus = listened(HopperMoveItemEvent.class);
        return bus != null && bus.post(new HopperMoveItemEventImpl(hopper, push, sourcePosition, source,
                destinationPosition, destination, item)).isCancelled();
    }

    /**
     * {@return {@code true} if a listener refused the pickup}
     */
    private static boolean refused(final BlockHopperInventory hopper, final ServerItemEntity item, final ItemStack taken) {
        final SimpleEventBus bus = listened(HopperPickupItemEvent.class);
        return bus != null && bus.post(new HopperPickupItemEventImpl(hopper, item, taken)).isCancelled();
    }

    /**
     * {@return the event bus when a listener would receive an event of a type, {@code null} otherwise:
     * hoppers move items far too often to build events nobody listens to}
     */
    private static @Nullable SimpleEventBus listened(final Class<? extends Event> type) {
        if (!FidorialServer.isInitialized()) {
            return null;
        }
        return FidorialServer.getInstance().events() instanceof final SimpleEventBus bus && bus.hasListeners(type) ? bus : null;
    }

    /**
     * A hopper of a world.
     */
    private record At(Key world, BlockPosition position) {
    }

    /**
     * What a hopper keeps between its ticks: its cooldown, when it last ticked, and the containers it
     * looked at, so that their slots are not read again from their block entities unless they
     * changed.
     */
    private static final class State {

        private final ServerWorld world;
        private volatile int cooldown;
        private volatile long tickedAt = Long.MIN_VALUE;
        private @Nullable BlockHopperInventory inventory;
        private HopperContainers.@Nullable Target pushTarget;
        private HopperContainers.@Nullable Target pullSource;

        State(final ServerWorld world) {
            this.world = world;
        }

        BlockHopperInventory inventory(final ServerWorld world, final BlockPosition position) {
            BlockHopperInventory current = inventory;
            if (current == null) {
                current = HopperBlock.inventory(world, position);
                inventory = current;
            }
            return current;
        }
    }
}
