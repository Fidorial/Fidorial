package fr.euphyllia.fidorial.server.world.farming;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.euphyllia.fidorial.server.events.farming.HarvestEventImpl;
import fr.euphyllia.fidorial.server.events.farming.PlantFertilizeEventImpl;
import fr.euphyllia.fidorial.server.events.farming.PlantGrowEventImpl;
import fr.euphyllia.fidorial.server.events.farming.SoilMoistureEventImpl;
import fr.euphyllia.fidorial.server.events.farming.SoilRevertEventImpl;
import fr.euphyllia.fidorial.server.events.farming.SoilTillEventImpl;
import fr.euphyllia.fidorial.server.events.farming.SoilTrampleEventImpl;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.Event;
import fr.fidorial.event.farming.HarvestEvent;
import fr.fidorial.event.farming.PlantFertilizeEvent;
import fr.fidorial.event.farming.PlantGrowEvent;
import fr.fidorial.event.farming.SoilMoistureEvent;
import fr.fidorial.event.farming.SoilRevertEvent;
import fr.fidorial.event.farming.SoilTillEvent;
import fr.fidorial.event.farming.SoilTrampleEvent;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Posts the {@linkplain fr.fidorial.event.farming farming events} for the engine and the built-in
 * farming blocks.
 *
 * <p>Random ticks reach a great many plants, so an event is only built when a listener would
 * receive it: with no plugin listening, each call costs one lookup in the bus.</p>
 */
public final class FarmingEvents {

    private FarmingEvents() {
        throw new UnsupportedOperationException("FarmingEvents cannot be instantiated.");
    }

    private static @Nullable SimpleEventBus listened(final Class<? extends Event> type) {
        if (!FidorialServer.isInitialized()) {
            return null;
        }
        return FidorialServer.getInstance().events() instanceof final SimpleEventBus bus && bus.hasListeners(type) ? bus : null;
    }

    /**
     * Fires a {@link PlantGrowEvent}.
     *
     * @param world      the world of the plant
     * @param position   where the plant is
     * @param block      the state of the plant, before it grows
     * @param target     where the grown state is about to be written
     * @param grown      the state the plant chose
     * @param fertilized {@code true} if a fertilizer causes the growth
     * @return the state to write: {@code grown} unless a listener changed it, {@code null} if a
     *         listener cancelled the growth
     */
    public static @Nullable BlockData grow(final World world, final BlockPosition position, final BlockData block,
                                           final BlockPosition target, final BlockData grown, final boolean fertilized) {
        final SimpleEventBus bus = listened(PlantGrowEventImpl.class);
        if (bus == null) {
            return grown;
        }
        final PlantGrowEventImpl event = bus.post(new PlantGrowEventImpl(world, position, block, target, grown, fertilized));
        return event.isCancelled() ? null : event.grown();
    }

    /**
     * Fires a {@link PlantFertilizeEvent}.
     *
     * @param world      the world of the block
     * @param position   where the block is
     * @param block      the state of the block, before it is fed
     * @param player     the player using the fertilizer, {@code null} for none
     * @param fertilizer the stack the fertilizer is taken from
     * @return {@code true} if the fertilizer may be used, {@code false} if a listener cancelled it
     */
    public static boolean fertilize(final World world, final BlockPosition position, final BlockData block,
                                    final @Nullable Player player, final ItemStack fertilizer) {
        final SimpleEventBus bus = listened(PlantFertilizeEventImpl.class);
        return bus == null || !bus.post(new PlantFertilizeEventImpl(world, position, block, player, fertilizer)).isCancelled();
    }

    /**
     * Fires a {@link SoilTillEvent}.
     *
     * @param world    the world of the soil
     * @param position where the soil is
     * @param block    the state of the soil, before it is tilled
     * @param player   the player tilling
     * @param tool     the hoe
     * @param tilled   the state the soil is about to turn into
     * @return the state to write: {@code tilled} unless a listener changed it, {@code null} if a
     *         listener cancelled the tilling
     */
    public static @Nullable BlockData till(final World world, final BlockPosition position, final BlockData block,
                                           final Player player, final ItemStack tool, final BlockData tilled) {
        final SimpleEventBus bus = listened(SoilTillEventImpl.class);
        if (bus == null) {
            return tilled;
        }
        final SoilTillEventImpl event = bus.post(new SoilTillEventImpl(world, position, block, player, tool, tilled));
        return event.isCancelled() ? null : event.tilled();
    }

    /**
     * Fires a {@link SoilMoistureEvent}.
     *
     * @param world     the world of the soil
     * @param position  where the soil is
     * @param block     the state of the soil, with the moisture it has
     * @param moistened the state of the soil, with the moisture it is about to have
     * @return {@code true} if the moisture may change, {@code false} if a listener cancelled it
     */
    public static boolean moisture(final World world, final BlockPosition position, final BlockData block,
                                   final BlockData moistened) {
        final SimpleEventBus bus = listened(SoilMoistureEventImpl.class);
        return bus == null || !bus.post(new SoilMoistureEventImpl(world, position, block, moistened)).isCancelled();
    }

    /**
     * Fires a {@link SoilRevertEvent}.
     *
     * @param world    the world of the soil
     * @param position where the soil is
     * @param block    the state of the soil, still tilled
     * @param reverted the state the soil is about to turn into
     * @return the state to write: {@code reverted} unless a listener changed it, {@code null} if a
     *         listener cancelled the event
     */
    public static @Nullable BlockData revert(final World world, final BlockPosition position, final BlockData block,
                                             final BlockData reverted) {
        final SimpleEventBus bus = listened(SoilRevertEventImpl.class);
        if (bus == null) {
            return reverted;
        }
        final SoilRevertEventImpl event = bus.post(new SoilRevertEventImpl(world, position, block, reverted));
        return event.isCancelled() ? null : event.reverted();
    }

    /**
     * Fires a {@link SoilTrampleEvent}.
     *
     * @param world        the world of the soil
     * @param position     where the soil is
     * @param block        the state of the soil, still tilled
     * @param reverted     the state the soil is about to turn into
     * @param entity       the entity that landed on the soil
     * @param fallDistance how far it fell, in blocks
     * @return the state to write: {@code reverted} unless a listener changed it, {@code null} if a
     *         listener cancelled the event
     */
    public static @Nullable BlockData trample(final World world, final BlockPosition position, final BlockData block,
                                              final BlockData reverted, final Entity entity, final double fallDistance) {
        final SimpleEventBus bus = listened(SoilTrampleEventImpl.class);
        if (bus == null) {
            return reverted;
        }
        final SoilTrampleEventImpl event =
                bus.post(new SoilTrampleEventImpl(world, position, block, reverted, entity, fallDistance));
        return event.isCancelled() ? null : event.reverted();
    }

    /**
     * Fires a {@link HarvestEvent}.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param block    the state giving the harvest
     * @param player   the player harvesting, {@code null} when the block breaks on its own
     * @param tool     what the block is harvested with
     * @param drops    what the behaviour of the block gives
     * @return the stacks to drop: {@code drops} unless a listener changed them, empty if a listener
     *         cancelled the event
     */
    public static List<ItemStack> harvest(final World world, final BlockPosition position, final BlockData block,
                                          final @Nullable Player player, final ItemStack tool, final List<ItemStack> drops) {
        final SimpleEventBus bus = listened(HarvestEventImpl.class);
        if (bus == null) {
            return drops;
        }
        final HarvestEventImpl event = bus.post(new HarvestEventImpl(world, position, block, player, tool, drops));
        return event.isCancelled() ? List.of() : List.copyOf(event.drops());
    }
}
