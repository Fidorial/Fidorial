package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.redstone.Faces;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.euphyllia.fidorial.server.world.redstone.ServerRedstoneWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.redstone.RedstoneDustPowerEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.type.RedstoneWire;
import fr.fidorial.world.block.data.type.RedstoneWire.Connection;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import fr.fidorial.world.redstone.SupportType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Redstone dust, a port of vanilla's {@code RedstoneWireBlock} and of its default
 * {@code RedstoneWireEvaluator}: the dust takes the strongest signal of the blocks around it, or the
 * power of the dust next to it minus one, and tells the blocks around it when that changes.
 *
 * <p>The update order is vanilla's, down to the order of the hash set the evaluator walks, so that
 * the circuits that depend on it behave the same.</p>
 */
public final class RedstoneWireBlock implements BlockBehaviour, RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.REDSTONE_WIRE;

    public static final RedstoneWireBlock INSTANCE = new RedstoneWireBlock();

    /**
     * Vanilla's {@code shouldSignal}, per thread since regions tick in parallel: while dust reads
     * the signal of the blocks around it, dust emits nothing, so that it never powers itself back
     * through them.
     */
    private static final ThreadLocal<int[]> SILENCED = ThreadLocal.withInitial(() -> new int[1]);

    private RedstoneWireBlock() {
    }

    private static boolean shouldSignal() {
        return SILENCED.get()[0] == 0;
    }

    private static boolean isWire(final @Nullable BlockData state) {
        return state != null && state.key().equals(KEY);
    }

    private static boolean isCross(final RedstoneWire wire) {
        for (final BlockFace side : Faces.HORIZONTAL) {
            if (!wire.getConnection(side).isConnected()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isDot(final RedstoneWire wire) {
        for (final BlockFace side : Faces.HORIZONTAL) {
            if (wire.getConnection(side).isConnected()) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@return vanilla's {@code canSurviveOn}: dust lies on a full face, or on a hopper}
     */
    private static boolean canSurviveOn(final @Nullable BlockData below) {
        return below != null && (BlockSupport.isFaceSturdy(below, BlockFace.UP, SupportType.FULL) || below.key().equals(BlockTypeKeys.HOPPER));
    }

    private static boolean canConnectUp(final SignalGetter world, final BlockPosition position) {
        return !world.isConductor(position.relative(BlockFace.UP));
    }

    private static Connection connectingSide(final SignalGetter world, final BlockPosition position, final BlockFace side,
                                             final boolean canConnectUp) {
        final BlockPosition relative = position.relative(side);
        final BlockData relativeState = world.blockAt(relative);
        if (relativeState == null) {
            return Connection.NONE;
        }
        if (canConnectUp) {
            final boolean placeableAbove = PoweredTrapdoor.isTrapdoor(relativeState) || canSurviveOn(relativeState);
            if (placeableAbove && connectsTo(world, relative.relative(BlockFace.UP), null)) {
                return BlockSupport.isFaceSturdy(relativeState, side.opposite(), SupportType.FULL) ? Connection.UP : Connection.SIDE;
            }
        }
        if (connectsTo(world, relative, relativeState, side)) {
            return Connection.SIDE;
        }
        return BlockSupport.isConductor(relativeState) || !connectsTo(world, relative.relative(BlockFace.DOWN), null)
                ? Connection.NONE
                : Connection.SIDE;
    }

    private static boolean connectsTo(final SignalGetter world, final BlockPosition position, final @Nullable BlockFace side) {
        final BlockData state = world.blockAt(position);
        return state != null && connectsTo(world, position, state, side);
    }

    private static boolean connectsTo(final SignalGetter world, final BlockPosition position, final BlockData state,
                                      final @Nullable BlockFace side) {
        final RedstoneBehaviour behaviour = Redstone.behaviour(state);
        return behaviour != null && behaviour.connectsToWire(world, position, state, side);
    }

    private static int targetStrength(final SignalGetter world, final BlockPosition position) {
        final int blockSignal = blockSignal(world, position);
        return blockSignal == 15 ? blockSignal : Math.max(blockSignal, incomingWireSignal(world, position));
    }

    /**
     * {@return the strongest signal the blocks around emit, dust excluded}
     */
    private static int blockSignal(final SignalGetter world, final BlockPosition position) {
        final int[] silenced = SILENCED.get();
        silenced[0]++;
        try {
            return world.bestNeighborSignal(position);
        } finally {
            silenced[0]--;
        }
    }

    private static int incomingWireSignal(final SignalGetter world, final BlockPosition position) {
        int wireSignal = 0;
        final boolean conductorAbove = world.isConductor(position.relative(BlockFace.UP));
        for (final BlockFace side : Faces.HORIZONTAL) {
            final BlockPosition neighbor = position.relative(side);
            final BlockData neighborState = world.blockAt(neighbor);
            wireSignal = Math.max(wireSignal, ServerRedstoneWorld.wirePower(neighborState));
            final boolean conductor = BlockSupport.isConductor(neighborState);
            if (conductor && !conductorAbove) {
                wireSignal = Math.max(wireSignal, ServerRedstoneWorld.wirePower(world.blockAt(neighbor.relative(BlockFace.UP))));
            } else if (!conductor) {
                wireSignal = Math.max(wireSignal, ServerRedstoneWorld.wirePower(world.blockAt(neighbor.relative(BlockFace.DOWN))));
            }
        }
        return Math.max(0, wireSignal - 1);
    }

    private static void updateNeighborsOfNeighboringWires(final RedstoneWorld world, final BlockPosition position) {
        for (final BlockFace side : Faces.HORIZONTAL) {
            checkCornerChangeAt(world, position.relative(side));
        }
        for (final BlockFace side : Faces.HORIZONTAL) {
            final BlockPosition target = position.relative(side);
            checkCornerChangeAt(world, target.relative(world.isConductor(target) ? BlockFace.UP : BlockFace.DOWN));
        }
    }

    private static void checkCornerChangeAt(final RedstoneWorld world, final BlockPosition position) {
        if (!isWire(world.blockAt(position))) {
            return;
        }
        world.updateNeighbors(position, KEY);
        for (final BlockFace face : Faces.ALL) {
            world.updateNeighbors(position.relative(face), KEY);
        }
    }

    /**
     * {@return the positions in the order a vanilla {@code HashSet<BlockPos>} of default capacity
     * walks them}: by hash bucket, then in insertion order. Vanilla updates the blocks around a
     * dust in that order, and some circuits depend on it.
     */
    static List<BlockPosition> inVanillaHashSetOrder(final List<BlockPosition> positions) {
        final List<BlockPosition> ordered = new ArrayList<>(positions);
        ordered.sort(Comparator.comparingInt(RedstoneWireBlock::vanillaBucket));
        return ordered;
    }

    private static int vanillaBucket(final BlockPosition position) {
        final int hash = (position.blockY() + position.blockZ() * 31) * 31 + position.blockX(); // Vec3i.hashCode
        return (hash ^ (hash >>> 16)) & 15; // HashMap.hash, sixteen buckets
    }

    @Override
    public BlockType type() {
        return Redstone.type(KEY);
    }

    private RedstoneWire defaultWire() {
        return (RedstoneWire) Redstone.defaultState(KEY);
    }

    private RedstoneWire crossWire() {
        RedstoneWire cross = defaultWire();
        for (final BlockFace side : Faces.HORIZONTAL) {
            cross = cross.withConnection(side, Connection.SIDE);
        }
        return cross;
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final SignalGetter world = Redstone.world(context.placer().world());
        if (!canSurviveOn(context.relative(BlockFace.DOWN))) {
            return null;
        }
        return connectionState(world, crossWire(), context.pos());
    }

    /**
     * A dot becomes a cross and a cross a dot, when nothing around decides the shape.
     */
    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(data instanceof final RedstoneWire wire) || player.gameMode() == GameMode.ADVENTURE
                || player.gameMode() == GameMode.SPECTATOR || !(isCross(wire) || isDot(wire))) {
            return false;
        }
        final RedstoneWorld world = Redstone.world(player.world());
        final RedstoneWire toggled = connectionState(world, (isCross(wire) ? defaultWire() : crossWire()).withPower(wire.getPower()), position);
        if (toggled.equals(wire)) {
            return false;
        }
        world.setBlock(position, toggled, RedstoneWorld.Update.NEIGHBORS);
        for (final BlockFace side : Faces.HORIZONTAL) {
            final BlockPosition relative = position.relative(side);
            if (wire.getConnection(side).isConnected() != toggled.getConnection(side).isConnected() && world.isConductor(relative)) {
                world.updateNeighborsExcept(relative, KEY, side.opposite());
            }
        }
        return true;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(KEY);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(KEY);
    }

    @Override
    public boolean isSignalSource(final BlockData state) {
        return shouldSignal();
    }

    @Override
    public boolean connectsToWire(final SignalGetter world, final BlockPosition position, final BlockData state,
                                  final @Nullable BlockFace direction) {
        return true;
    }

    @Override
    public int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        if (!shouldSignal() || direction == BlockFace.DOWN || !(state instanceof final RedstoneWire wire)) {
            return 0;
        }
        final int power = wire.getPower();
        if (power == 0 || direction == BlockFace.UP) {
            return power;
        }
        // the dust powers the block it points at, or the block below
        return connectionState(world, wire, position).getConnection(direction.opposite()).isConnected() ? power : 0;
    }

    @Override
    public int directSignal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return shouldSignal() ? signal(world, position, state, direction) : 0;
    }

    @Override
    public void onPlace(final RedstoneWorld world, final BlockPosition position, final BlockData state, final BlockData previous) {
        if (previous.key().equals(KEY) || !(state instanceof final RedstoneWire wire)) {
            return;
        }
        updatePowerStrength(world, position, wire);
        for (final BlockFace face : Faces.VERTICAL) {
            world.updateNeighbors(position.relative(face), KEY);
        }
        updateNeighborsOfNeighboringWires(world, position);
    }

    @Override
    public void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final RedstoneWire wire)) {
            return;
        }
        for (final BlockFace face : Faces.ALL) {
            world.updateNeighbors(position.relative(face), KEY);
        }
        updatePowerStrength(world, position, wire);
        updateNeighborsOfNeighboringWires(world, position);
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (!(state instanceof final RedstoneWire wire)) {
            return;
        }
        if (canSurviveOn(world.blockAt(position.relative(BlockFace.DOWN)))) {
            updatePowerStrength(world, position, wire);
        } else {
            // Gone without the particles of a block broken
            world.setBlock(position, Redstone.air(), RedstoneWorld.Update.NEIGHBORS);
        }
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (!(state instanceof final RedstoneWire wire)) {
            return state;
        }
        if (direction == BlockFace.DOWN) {
            return canSurviveOn(neighbor) ? state : Redstone.air();
        }
        if (direction == BlockFace.UP) {
            return connectionState(world, wire, position);
        }
        final Connection side = connectingSide(world, position, direction, canConnectUp(world, position));
        return side.isConnected() == wire.getConnection(direction).isConnected() && !isCross(wire)
                ? wire.withConnection(direction, side)
                : connectionState(world, crossWire().withPower(wire.getPower()).withConnection(direction, side), position);
    }

    @Override
    public void updateIndirectNeighborShapes(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final RedstoneWire wire)) {
            return;
        }
        for (final BlockFace side : Faces.HORIZONTAL) {
            final BlockPosition beside = position.relative(side);
            if (wire.getConnection(side) == Connection.NONE || isWire(world.blockAt(beside))) {
                continue;
            }
            final BlockPosition below = beside.relative(BlockFace.DOWN);
            if (isWire(world.blockAt(below))) {
                world.neighborShapeChanged(below, side.opposite());
            }
            final BlockPosition above = beside.relative(BlockFace.UP);
            if (isWire(world.blockAt(above))) {
                world.neighborShapeChanged(above, side.opposite());
            }
        }
    }

    /**
     * {@return vanilla's {@code getConnectionState}: the connections the dust takes from what is
     * around it, a lone dust keeping its dot or cross look}
     */
    private RedstoneWire connectionState(final SignalGetter world, final RedstoneWire state, final BlockPosition position) {
        final boolean wasDot = isDot(state);
        RedstoneWire wire = missingConnections(world, defaultWire().withPower(state.getPower()), position);
        if (wasDot && isDot(wire)) {
            return wire;
        }
        final boolean north = wire.getConnection(BlockFace.NORTH).isConnected();
        final boolean south = wire.getConnection(BlockFace.SOUTH).isConnected();
        final boolean east = wire.getConnection(BlockFace.EAST).isConnected();
        final boolean west = wire.getConnection(BlockFace.WEST).isConnected();
        final boolean northSouthEmpty = !north && !south;
        final boolean eastWestEmpty = !east && !west;
        if (!west && northSouthEmpty) {
            wire = wire.withConnection(BlockFace.WEST, Connection.SIDE);
        }
        if (!east && northSouthEmpty) {
            wire = wire.withConnection(BlockFace.EAST, Connection.SIDE);
        }
        if (!north && eastWestEmpty) {
            wire = wire.withConnection(BlockFace.NORTH, Connection.SIDE);
        }
        if (!south && eastWestEmpty) {
            wire = wire.withConnection(BlockFace.SOUTH, Connection.SIDE);
        }
        return wire;
    }

    private RedstoneWire missingConnections(final SignalGetter world, RedstoneWire wire, final BlockPosition position) {
        final boolean canConnectUp = canConnectUp(world, position);
        for (final BlockFace side : Faces.HORIZONTAL) {
            if (!wire.getConnection(side).isConnected()) {
                wire = wire.withConnection(side, connectingSide(world, position, side, canConnectUp));
            }
        }
        return wire;
    }

    /**
     * Vanilla's {@code DefaultRedstoneWireEvaluator.updatePowerStrength}, firing a
     * {@link RedstoneDustPowerEvent} before the power changes.
     */
    private void updatePowerStrength(final RedstoneWorld world, final BlockPosition position, final RedstoneWire state) {
        final int signal = targetStrength(world, position);
        if (state.getPower() == signal) {
            return;
        }
        // false for dust just removed, whose neighbours still learn it no longer powers them
        final boolean inPlace = state.equals(world.blockAt(position));
        final int target = inPlace ? RedstoneEvents.dustPower(world.world(), position, state, state.getPower(), signal) : signal;
        if (state.getPower() == target) {
            return; // a listener kept the power of the dust
        }
        if (inPlace) {
            world.setBlock(position, state.withPower(target), RedstoneWorld.Update.SHAPES);
        }
        final List<BlockPosition> toUpdate = new ArrayList<>(7);
        toUpdate.add(position);
        for (final BlockFace face : Faces.ALL) {
            toUpdate.add(position.relative(face));
        }
        for (final BlockPosition updated : inVanillaHashSetOrder(toUpdate)) {
            world.updateNeighbors(updated, KEY);
        }
    }
}
