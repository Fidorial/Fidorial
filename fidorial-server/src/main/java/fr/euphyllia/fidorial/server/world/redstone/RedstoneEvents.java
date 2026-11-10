package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.events.SimpleEventBus;
import fr.euphyllia.fidorial.server.events.redstone.PistonExtendEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.PistonRetractEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneDustPowerEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneInputEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneOutputEventImpl;
import fr.euphyllia.fidorial.server.events.redstone.RedstoneUpdateEventImpl;
import fr.fidorial.event.Event;
import fr.fidorial.event.redstone.PistonExtendEvent;
import fr.fidorial.event.redstone.PistonRetractEvent;
import fr.fidorial.event.redstone.RedstoneDustPowerEvent;
import fr.fidorial.event.redstone.RedstoneInputEvent;
import fr.fidorial.event.redstone.RedstoneOutputEvent;
import fr.fidorial.event.redstone.RedstoneUpdateEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.List;

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

    /**
     * Fires a {@link PistonExtendEvent}.
     *
     * @param world     the world of the piston
     * @param position  where the base of the piston is
     * @param block     the state of the base, before it extends
     * @param direction the side the piston faces
     * @param sticky    {@code true} for a sticky piston
     * @param moved     where the blocks it is about to push are
     * @param broken    where the blocks it is about to break are
     * @return {@code true} if the piston may extend, {@code false} if a listener cancelled it
     */
    public static boolean pistonExtend(final World world, final BlockPosition position, final BlockData block, final BlockFace direction,
                                       final boolean sticky, final List<BlockPosition> moved, final List<BlockPosition> broken) {
        final SimpleEventBus bus = listened(PistonExtendEventImpl.class);
        return bus == null || !bus.post(new PistonExtendEventImpl(world, position, block, direction, sticky, moved, broken)).isCancelled();
    }

    /**
     * {@return {@code true} if a {@link PistonRetractEvent} would reach a listener}: working out
     * what a sticky piston is about to pull, ahead of pulling it, is only worth it then
     */
    public static boolean listensToPistonRetract() {
        return listened(PistonRetractEventImpl.class) != null;
    }

    /**
     * Fires a {@link PistonRetractEvent}.
     *
     * @param world     the world of the piston
     * @param position  where the base of the piston is
     * @param block     the state of the base, before it retracts
     * @param direction the side the piston faces
     * @param sticky    {@code true} for a sticky piston
     * @param moved     where the blocks it is about to pull are
     * @param broken    where the blocks it is about to break are
     * @return {@code true} if the piston may retract, {@code false} if a listener cancelled it
     */
    public static boolean pistonRetract(final World world, final BlockPosition position, final BlockData block, final BlockFace direction,
                                        final boolean sticky, final List<BlockPosition> moved, final List<BlockPosition> broken) {
        final SimpleEventBus bus = listened(PistonRetractEventImpl.class);
        return bus == null || !bus.post(new PistonRetractEventImpl(world, position, block, direction, sticky, moved, broken)).isCancelled();
    }
}
