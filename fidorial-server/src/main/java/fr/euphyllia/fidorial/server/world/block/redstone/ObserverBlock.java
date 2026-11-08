package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import org.jspecify.annotations.Nullable;

/**
 * The observer, a port of vanilla's {@code ObserverBlock}: when the block its face looks at changes
 * shape or state, it sends a two game tick pulse out of its back.
 */
public final class ObserverBlock implements BlockBehaviour, RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.OBSERVER;

    public static final ObserverBlock INSTANCE = new ObserverBlock();

    private static final int PULSE_TICKS = 2;

    private ObserverBlock() {
    }

    private static boolean isPowered(final BlockData state) {
        return state instanceof final Powerable observer && observer.isPowered();
    }

    private static @Nullable BlockFace facing(final BlockData state) {
        return state instanceof final Directional observer ? observer.getFacing() : null;
    }

    /**
     * Tells the block behind the observer, and the neighbours of that block but the observer, that
     * its output changed.
     */
    private static void updateNeighborsInFront(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        final BlockFace facing = facing(state);
        if (facing == null) {
            return;
        }
        final BlockPosition behind = position.relative(facing.opposite());
        world.neighborChanged(behind, KEY);
        world.updateNeighborsExcept(behind, KEY, facing);
    }

    @Override
    public BlockType type() {
        return Redstone.type(KEY);
    }

    /**
     * The face looks where the player looks, so the back, the output, faces the
     * player.
     */
    @Override
    public BlockData placementState(final BlockPlaceContext context) {
        return ((Directional) Redstone.defaultState(KEY)).withFacing(context.lookingDirection());
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(KEY);
    }

    @Override
    public boolean isSignalSource(final BlockData state) {
        return true;
    }

    /**
     * Powered, it powers the block behind it, which reads it in the direction the
     * observer faces.
     */
    @Override
    public int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return isPowered(state) && facing(state) == direction ? 15 : 0;
    }

    @Override
    public int directSignal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return signal(world, position, state, direction);
    }

    /**
     * dust only connects to the back of an observer.
     */
    @Override
    public boolean connectsToWire(final SignalGetter world, final BlockPosition position, final BlockData state,
                                  final @Nullable BlockFace direction) {
        return direction != null && facing(state) == direction;
    }

    /**
     * A change in front of the observer starts a pulse, unless one is under way.
     */
    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (facing(state) == direction && !isPowered(state) && !world.hasScheduledTick(position, KEY)) {
            world.scheduleTick(position, KEY, PULSE_TICKS);
        }
        return state;
    }

    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final Powerable observer)) {
            return;
        }
        if (!RedstoneEvents.output(world.world(), position, state, !observer.isPowered())) {
            if (observer.isPowered()) {
                // a plugin holds it on: it tries to end its pulse again later
                world.scheduleTick(position, KEY, PULSE_TICKS);
            }
            return;
        }
        if (observer.isPowered()) {
            world.setBlock(position, observer.withPowered(false), RedstoneWorld.Update.SHAPES);
        } else {
            world.setBlock(position, observer.withPowered(true), RedstoneWorld.Update.SHAPES);
            world.scheduleTick(position, KEY, PULSE_TICKS);
        }
        updateNeighborsInFront(world, position, state);
    }

    /**
     * An observer placed powered, with no pulse to end, is switched off without
     * telling anyone but the block behind it.
     */
    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        if (state.key().equals(previous.key()) || !(state instanceof final Powerable observer)) {
            return;
        }
        if (observer.isPowered() && !world.hasScheduledTick(position, KEY)) {
            final BlockData unpowered = observer.withPowered(false);
            world.setBlock(position, unpowered, RedstoneWorld.Update.QUIET);
            updateNeighborsInFront(world, position, unpowered);
        }
    }

    @Override
    public void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (isPowered(state) && world.hasScheduledTick(position, KEY)) {
            updateNeighborsInFront(world, position, state);
        }
    }
}
