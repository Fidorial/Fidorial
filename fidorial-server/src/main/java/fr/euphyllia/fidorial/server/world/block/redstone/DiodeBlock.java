package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.redstone.Faces;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.euphyllia.fidorial.server.world.redstone.ServerRedstoneWorld;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
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
import fr.fidorial.world.redstone.SupportType;
import fr.fidorial.world.redstone.TickPriority;
import org.jspecify.annotations.Nullable;

/**
 * What repeaters and comparators share, a port of vanilla's {@code DiodeBlock}: they read the
 * block their facing points at, switch after a delay, and power the block on the other side.
 */
abstract class DiodeBlock implements BlockBehaviour, RedstoneBehaviour {

    protected final TypedKey<BlockType> key;

    DiodeBlock(final TypedKey<BlockType> key) {
        this.key = key;
    }

    protected static BlockFace facing(final BlockData state) {
        return ((Directional) state).getFacing();
    }

    protected static boolean isPowered(final BlockData state) {
        return ((Powerable) state).isPowered();
    }

    protected static boolean canSurviveOn(final @Nullable BlockData below) {
        return BlockSupport.isFaceSturdy(below, BlockFace.UP, SupportType.RIGID);
    }

    @Override
    public final TypedKey<BlockType> key() {
        return key;
    }

    @Override
    public final BlockType type() {
        return Redstone.type(key);
    }

    /**
     * {@return the number of game ticks between a change of input and the output following it}
     */
    protected abstract int delay(BlockData state);

    /**
     * {@return the signal the diode outputs while it is on}
     */
    protected int outputSignal(final SignalGetter world, final BlockPosition position, final BlockData state) {
        return 15;
    }

    /**
     * {@return {@code true} if a repeater or comparator on the side holds the diode in its state}
     */
    protected boolean isLocked(final SignalGetter world, final BlockPosition position, final BlockData state) {
        return false;
    }

    /**
     * {@return {@code true} if only repeaters and comparators count as side inputs}
     */
    protected boolean sideInputDiodesOnly() {
        return false;
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        if (!canSurviveOn(context.relative(BlockFace.DOWN))) {
            return null;
        }
        return ((Directional) Redstone.defaultState(key)).withFacing(context.horizontalFacing().opposite());
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(key);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(key);
    }

    @Override
    public boolean isSignalSource(final BlockData state) {
        return true;
    }

    @Override
    public int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return facing(state) == direction && isPowered(state) ? outputSignal(world, position, state) : 0;
    }

    @Override
    public int directSignal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return signal(world, position, state, direction);
    }

    protected boolean shouldTurnOn(final SignalGetter world, final BlockPosition position, final BlockData state) {
        return inputSignal(world, position, state) > 0;
    }

    protected int inputSignal(final SignalGetter world, final BlockPosition position, final BlockData state) {
        final BlockFace facing = facing(state);
        final BlockPosition input = position.relative(facing);
        final int signal = world.signal(input, facing);
        return signal >= 15 ? signal : Math.max(signal, ServerRedstoneWorld.wirePower(world.blockAt(input)));
    }

    protected int alternateSignal(final SignalGetter world, final BlockPosition position, final BlockData state) {
        final BlockFace facing = facing(state);
        final BlockFace clockWise = Faces.clockWise(facing);
        final BlockFace counterClockWise = Faces.counterClockWise(facing);
        return Math.max(
                world.controlInputSignal(position.relative(clockWise), clockWise, sideInputDiodesOnly()),
                world.controlInputSignal(position.relative(counterClockWise), counterClockWise, sideInputDiodesOnly()));
    }

    /**
     * {@return vanilla's {@code shouldPrioritize}: the diode in front does not face back at this one}
     */
    protected boolean shouldPrioritize(final SignalGetter world, final BlockPosition position, final BlockData state) {
        final BlockFace front = facing(state).opposite();
        final BlockData inFront = world.blockAt(position.relative(front));
        return ServerRedstoneWorld.isDiode(inFront) && inFront != null && facing(inFront) != front;
    }

    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (isLocked(world, position, state)) {
            return;
        }
        final boolean on = isPowered(state);
        final boolean shouldTurnOn = shouldTurnOn(world, position, state);
        if ((on && shouldTurnOn) || !RedstoneEvents.output(world.world(), position, state, !on)) {
            return;
        }
        if (on) {
            world.setBlock(position, ((Powerable) state).withPowered(false), RedstoneWorld.Update.SHAPES);
        } else {
            world.setBlock(position, ((Powerable) state).withPowered(true), RedstoneWorld.Update.SHAPES);
            if (!shouldTurnOn) {
                world.scheduleTick(position, key, delay(state), TickPriority.VERY_HIGH);
            }
        }
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (canSurviveOn(world.blockAt(position.relative(BlockFace.DOWN)))) {
            checkTickOnNeighbor(world, position, state);
            return;
        }
        // Gone without the particles of a block broken
        world.setBlock(position, Redstone.air(), RedstoneWorld.Update.NEIGHBORS);
        for (final BlockFace face : Faces.ALL) {
            world.updateNeighbors(position.relative(face), key);
        }
    }

    protected void checkTickOnNeighbor(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (isLocked(world, position, state)) {
            return;
        }
        final boolean on = isPowered(state);
        if (on != shouldTurnOn(world, position, state) && !world.willTickThisTick(position, key)) {
            final TickPriority priority;
            if (shouldPrioritize(world, position, state)) {
                priority = TickPriority.EXTREMELY_HIGH;
            } else if (on) {
                priority = TickPriority.VERY_HIGH;
            } else {
                priority = TickPriority.HIGH;
            }
            world.scheduleTick(position, key, delay(state), priority);
        }
    }

    /**
     * The block in front learns of every change. A new diode that should be on is
     * switched on a game tick later, what vanilla's {@code setPlacedBy} does for the diodes players
     * place.
     */
    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        updateNeighborsInFront(world, position, state);
        if (!previous.key().equals(key) && shouldTurnOn(world, position, state)) {
            world.scheduleTick(position, key, 1);
        }
    }

    @Override
    public void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        updateNeighborsInFront(world, position, state);
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        return direction == BlockFace.DOWN && !canSurviveOn(neighbor) ? Redstone.air() : state;
    }

    protected final void updateNeighborsInFront(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        final BlockFace facing = facing(state);
        final BlockPosition front = position.relative(facing.opposite());
        world.neighborChanged(front, key);
        world.updateNeighborsExcept(front, key, facing);
    }
}
