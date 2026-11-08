package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneDustPowerEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneInputEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneOutputEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneUpdateEventImpl;
import fr.fidorial.event.Event;
import fr.fidorial.event.redstone.RedstoneDustPowerEvent;
import fr.fidorial.event.redstone.RedstoneInputEvent;
import fr.fidorial.event.redstone.RedstoneOutputEvent;
import fr.fidorial.event.redstone.RedstoneUpdateEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

/**
 * Posts the {@linkplain fr.fidorial.event.redstone redstone events} for the engine and the vanilla
 * redstone blocks.
 *
 * <p>A busy circuit fires far more of these than anything else posts events, so an event is only
 * built when a listener would receive it: with no plugin listening, each call costs one lookup in
 * the bus.</p>
 */
public final class RedstoneEvents {

    private RedstoneEvents() {
        throw new UnsupportedOperationException("RedstoneEvents cannot be instantiated.");
    }

    private static @Nullable SimpleEventBus listened(final Class<? extends Event> type) {
        if (!FidorialServer.isInitialized()) {
            return null;
        }
        return FidorialServer.getInstance().events() instanceof final SimpleEventBus bus && bus.hasListeners(type) ? bus : null;
    }

    /**
     * Fires a {@link RedstoneDustPowerEvent}.
     *
     * @param world    the world of the dust
     * @param position where the dust is
     * @param dust     the state of the dust
     * @param oldPower the power the dust carries
     * @param newPower the power the signal gives it
     * @return the power the dust should carry: {@code newPower} unless a listener changed it
     */
    public static int dustPower(final World world, final BlockPosition position, final BlockData dust,
                                final int oldPower, final int newPower) {
        if (oldPower == newPower) {
            return newPower;
        }
        final SimpleEventBus bus = listened(RedstoneDustPowerEventImpl.class);
        if (bus == null) {
            return newPower;
        }
        return bus.post(new RedstoneDustPowerEventImpl(world, position, dust, oldPower, newPower)).resultingPower();
    }

    /**
     * Fires a {@link RedstoneInputEvent}.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param block    the state of the block, before it reacts
     * @param powered  {@code true} if the block is now powered
     * @return {@code true} if the block may react, {@code false} if a listener cancelled it
     */
    public static boolean input(final World world, final BlockPosition position, final BlockData block, final boolean powered) {
        final SimpleEventBus bus = listened(RedstoneInputEventImpl.class);
        return bus == null || !bus.post(new RedstoneInputEventImpl(world, position, block, powered)).isCancelled();
    }

    /**
     * Fires a {@link RedstoneOutputEvent}.
     *
     * @param world     the world of the component
     * @param position  where the component is
     * @param block     the state of the component, before it switches
     * @param oldSignal the signal it emits
     * @param newSignal the signal it is about to emit
     * @return {@code true} if the component may switch, {@code false} if a listener cancelled it
     */
    public static boolean output(final World world, final BlockPosition position, final BlockData block,
                                 final int oldSignal, final int newSignal) {
        final SimpleEventBus bus = listened(RedstoneOutputEventImpl.class);
        return bus == null || !bus.post(new RedstoneOutputEventImpl(world, position, block, oldSignal, newSignal)).isCancelled();
    }

    /**
     * Fires a {@link RedstoneOutputEvent} for a component that only emits {@code 15} or {@code 0}.
     *
     * @param world    the world of the component
     * @param position where the component is
     * @param block    the state of the component, before it switches
     * @param on       {@code true} if it is turning on, {@code false} if it is turning off
     * @return {@code true} if the component may switch, {@code false} if a listener cancelled it
     */
    public static boolean output(final World world, final BlockPosition position, final BlockData block, final boolean on) {
        return output(world, position, block, on ? 0 : 15, on ? 15 : 0);
    }

    /**
     * Fires a {@link RedstoneUpdateEvent}.
     *
     * @param world    the world of the block
     * @param position where the block told of the change is
     * @param block    the state of that block
     * @param source   the block type that caused the update
     * @return {@code true} if the block may react, {@code false} if a listener cancelled it
     */
    public static boolean update(final World world, final BlockPosition position, final BlockData block,
                                 final TypedKey<BlockType> source) {
        final SimpleEventBus bus = listened(RedstoneUpdateEventImpl.class);
        return bus == null || !bus.post(new RedstoneUpdateEventImpl(world, position, block, source)).isCancelled();
    }
}
