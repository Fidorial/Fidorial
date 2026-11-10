package fr.euphyllia.fidorial.server.world.redstone;

import alternate.current.wire.WireHandler;
import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.util.threading.ThreadContexts;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.AnaloguePowerable;
import fr.fidorial.world.block.data.Waterlogged;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SupportType;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * The redstone of one world, as {@link FidorialRedstoneEngine} drives it.
 *
 * <p>Reads only look at loaded chunks. Writes and behaviour calls happen on the thread owning the
 * block: an update reaching a block of another region is handed over to that region, and dropped
 * when its chunk is not loaded.</p>
 */
public final class ServerRedstoneWorld implements RedstoneWorld {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ServerRedstoneWorld.class);

    private final FidorialRedstoneEngine engine;
    private final ServerWorld world;
    private final BlockData air;
    private final BlockData water;
    private volatile @Nullable DustImplementation dustOverride;

    ServerRedstoneWorld(final FidorialRedstoneEngine engine, final ServerWorld world) {
        this.engine = engine;
        this.world = world;
        this.air = Objects.requireNonNull(engine.blockStates().resolve(BlockState.withoutProperties(BlockTypeKeys.AIR)),
                "minecraft:air is not registered");
        this.water = Objects.requireNonNull(engine.blockStates().resolve(BlockState.withProperties(BlockTypeKeys.WATER, Map.of("level", "0"))),
                "minecraft:water is not registered");
    }

    /**
     * {@return {@code true} for a repeater or a comparator}
     *
     * @param state the state, {@code null} for a block that is not loaded
     */
    public static boolean isDiode(final @Nullable BlockData state) {
        return state != null && (state.key().equals(BlockTypeKeys.REPEATER) || state.key().equals(BlockTypeKeys.COMPARATOR));
    }

    /**
     * {@return the power redstone dust carries, {@code 0} for any other block}
     *
     * @param state the state, {@code null} for a block that is not loaded
     */
    public static int wirePower(final @Nullable BlockData state) {
        return state instanceof final AnaloguePowerable dust && state.key().equals(BlockTypeKeys.REDSTONE_WIRE) ? dust.getPower() : 0;
    }

    @Override
    public World world() {
        return world;
    }

    /**
     * {@return how the dust of this world works out its power}: as the configuration of the world
     * says, unless {@linkplain #overrideDustImplementation overridden}
     */
    public DustImplementation dustImplementation() {
        final DustImplementation override = dustOverride;
        return override != null ? override : world.configuration().redstone().dustImplementation();
    }

    /**
     * Makes the dust of this world work out its power one way, whatever its configuration says, for
     * as long as the world stays loaded: what tests and tools comparing implementations need.
     *
     * @param implementation the implementation, {@code null} to follow the configuration again
     */
    public void overrideDustImplementation(final @Nullable DustImplementation implementation) {
        this.dustOverride = implementation;
    }

    /**
     * {@return the Alternate Current handler of the current thread for this world, {@code null} when
     * its dust is vanilla}
     */
    public @Nullable WireHandler wireHandler() {
        return dustImplementation() == DustImplementation.ALTERNATE_CURRENT ? engine.wireHandler(this) : null;
    }


    /**
     * {@return the state of air, what a block that can no longer stand turns into}
     */
    public BlockData air() {
        return air;
    }

    /**
     * {@return {@code true} if the current thread owns a block, and may change it}
     *
     * @param position the block
     */
    public boolean ownsBlock(final BlockPosition position) {
        return owns(position);
    }

    /**
     * {@return the redstone behaviour in effect for a state, {@code null} for a block taking no part
     * in redstone or that is not loaded}
     *
     * @param state the state, {@code null} for a block that is not loaded
     */
    public @Nullable RedstoneBehaviour behaviourOf(final @Nullable BlockData state) {
        return behaviour(state);
    }

    @Override
    public @Nullable BlockData blockAt(final BlockPosition position) {
        final int y = position.blockY();
        if (y < world.minY() || y >= world.minY() + world.height()) {
            return air;
        }
        final ChunkColumn column = world.loadedColumn(position.blockX() >> 4, position.blockZ() >> 4);
        if (column == null) {
            return null;
        }
        return engine.blockStates().resolve(column.getBlock(position.blockX() & 15, y, position.blockZ() & 15));
    }

    private @Nullable RedstoneBehaviour behaviour(final @Nullable BlockData state) {
        return state == null ? null : engine.registry().behaviourOrNull(state.key());
    }

    @Override
    public int signal(final BlockPosition position, final BlockFace direction) {
        final BlockData state = blockAt(position);
        if (state == null) {
            return 0;
        }
        final RedstoneBehaviour behaviour = behaviour(state);
        final int signal = behaviour == null ? 0 : behaviour.signal(this, position, state, direction);
        if (signal >= 15 || !BlockSupport.isConductor(state)) {
            return signal;
        }
        return Math.max(signal, directSignalTo(position));
    }

    @Override
    public int directSignal(final BlockPosition position, final BlockFace direction) {
        final BlockData state = blockAt(position);
        final RedstoneBehaviour behaviour = behaviour(state);
        return state == null || behaviour == null ? 0 : behaviour.directSignal(this, position, state, direction);
    }

    @Override
    public int directSignalTo(final BlockPosition position) {
        int result = 0;
        for (final BlockFace face : Faces.ALL) {
            result = Math.max(result, directSignal(position.relative(face), face));
            if (result >= 15) {
                return result;
            }
        }
        return result;
    }

    @Override
    public int bestNeighborSignal(final BlockPosition position) {
        int best = 0;
        for (final BlockFace face : Faces.ALL) {
            final int signal = signal(position.relative(face), face);
            if (signal >= 15) {
                return 15;
            }
            best = Math.max(best, signal);
        }
        return best;
    }

    @Override
    public boolean hasNeighborSignal(final BlockPosition position) {
        for (final BlockFace face : Faces.ALL) {
            if (signal(position.relative(face), face) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int controlInputSignal(final BlockPosition position, final BlockFace direction, final boolean diodesOnly) {
        final BlockData state = blockAt(position);
        if (state == null) {
            return 0;
        }
        if (diodesOnly) {
            return isDiode(state) ? directSignal(position, direction) : 0;
        }
        if (state.key().equals(BlockTypeKeys.REDSTONE_BLOCK)) {
            return 15;
        }
        if (state.key().equals(BlockTypeKeys.REDSTONE_WIRE)) {
            return wirePower(state);
        }
        final RedstoneBehaviour behaviour = behaviour(state);
        return behaviour != null && behaviour.isSignalSource(state) ? directSignal(position, direction) : 0;
    }

    @Override
    public boolean hasAnalogOutput(final BlockPosition position) {
        final BlockData state = blockAt(position);
        final RedstoneBehaviour behaviour = behaviour(state);
        return state != null && behaviour != null && behaviour.hasAnalogOutput(state);
    }

    @Override
    public int analogOutput(final BlockPosition position, final BlockFace direction) {
        final BlockData state = blockAt(position);
        final RedstoneBehaviour behaviour = behaviour(state);
        if (state == null || behaviour == null || !behaviour.hasAnalogOutput(state)) {
            return 0;
        }
        return behaviour.analogOutput(this, position, state, direction);
    }

    @Override
    public boolean isConductor(final BlockPosition position) {
        return BlockSupport.isConductor(blockAt(position));
    }

    @Override
    public boolean isFaceSturdy(final BlockPosition position, final BlockFace face, final SupportType support) {
        return BlockSupport.isFaceSturdy(blockAt(position), face, support);
    }

    @Override
    public boolean setBlock(final BlockPosition position, final BlockData data, final Update update) {
        return setBlock(position, data, update, FidorialRedstoneEngine.SHAPE_UPDATE_LIMIT, true);
    }

    /**
     * Replaces a block as {@link #setBlock(BlockPosition, BlockData, Update)} does, without telling
     * the players that see it: what vanilla does for the blocks a piston is moving, which clients
     * work out themselves from the block event of the piston. Light and fluids still follow.
     *
     * @param position where the block is
     * @param data     the new state
     * @param update   how far the change spreads
     * @return {@code false} if the block already held that state, or its chunk is not loaded
     * @throws IllegalStateException if the calling thread does not own {@code position}
     */
    public boolean setBlockUnseen(final BlockPosition position, final BlockData data, final Update update) {
        return setBlock(position, data, update, FidorialRedstoneEngine.SHAPE_UPDATE_LIMIT, false);
    }

    boolean setBlock(final BlockPosition position, final BlockData data, final Update update, final int limit) {
        return setBlock(position, data, update, limit, true);
    }

    private boolean setBlock(final BlockPosition position, final BlockData data, final Update update, final int limit,
                             final boolean visible) {
        ThreadContexts.checkOwnedByCurrentThread(world, position, "redstone setBlock");
        final BlockData previous = blockAt(position);
        if (previous == null || previous.equals(data)) {
            return false;
        }
        if (!engine.blockEdits().set(world, position, engine.blockStates().toBlockState(data), false, visible)) {
            return false;
        }
        afterChange(position, previous, data, update, limit);
        return true;
    }

    @Override
    public boolean destroyBlock(final BlockPosition position) {
        return destroyBlock(position, FidorialRedstoneEngine.SHAPE_UPDATE_LIMIT);
    }

    private boolean destroyBlock(final BlockPosition position, final int limit) {
        final BlockData state = blockAt(position);
        if (state == null || state.isAir()) {
            return false;
        }
        final BlockData left = state instanceof final Waterlogged waterlogged && waterlogged.isWaterlogged() ? water : air;
        if (!setBlock(position, left, Update.NEIGHBORS, limit)) {
            return false;
        }
        world.sendToChunkViewers(position.chunkX(), position.chunkZ(),
                new ClientboundLevelEventPacket(ClientboundLevelEventPacket.BLOCK_BREAK, position, state.networkId(), false));
        return true;
    }

    /**
     * Changes the power of redstone dust, Alternate Current's {@code LevelHelper.setWireState}: the
     * block is written and players see it, but nothing else follows, the handler updating the blocks
     * around the dust itself. Light and fluids are left alone, the power of dust changing neither.
     *
     * @param position     where the dust is
     * @param state        the dust with its new power
     * @param updateShapes {@code true} for a dust just placed: the blocks around it follow its shape,
     *                     as a placement makes them
     * @return {@code false} if the dust already held that state, or its chunk is not loaded
     * @throws IllegalStateException if the calling thread does not own {@code position}
     */
    public boolean setWireState(final BlockPosition position, final BlockData state, final boolean updateShapes) {
        ThreadContexts.checkOwnedByCurrentThread(world, position, "redstone setWireState");
        final BlockData previous = blockAt(position);
        if (previous == null || previous.equals(state)) {
            return false;
        }
        if (!engine.blockEdits().setQuietly(world, position, engine.blockStates().toBlockState(state))) {
            return false;
        }
        if (updateShapes) {
            updateShapesAround(position, previous, state, FidorialRedstoneEngine.SHAPE_UPDATE_LIMIT);
        }
        return true;
    }

    /**
     * Runs one neighbour update right away, rather than queuing it behind the chain under way:
     * Alternate Current's {@code InstantNeighborUpdater}, its handler ordering the updates itself.
     * Handed over to the region owning {@code position} when another one does.
     *
     * @param position the block to tell
     * @param source   the block type that caused the update
     */
    public void updateNeighborNow(final BlockPosition position, final TypedKey<BlockType> source) {
        runNeighborChanged(position, source);
    }

    /**
     * Runs one shape update right away, rather than queuing it behind the chain under way. Handed
     * over to the region owning {@code position} when another one does.
     *
     * @param position  the block to tell
     * @param direction the side of {@code position} the changed block is on
     * @param neighbor  the new state of the changed block
     */
    public void updateShapeNow(final BlockPosition position, final BlockFace direction, final BlockData neighbor) {
        runShapeUpdate(position, direction, neighbor, FidorialRedstoneEngine.SHAPE_UPDATE_LIMIT);
    }

    @Override
    public void blockChanged(final BlockPosition position, final BlockData previous, final BlockData current) {
        if (previous.equals(current)) {
            return;
        }
        if (!owns(position)) {
            handOver(position, () -> blockChanged(position, previous, current));
            return;
        }
        afterChange(position, previous, current, Update.NEIGHBORS, FidorialRedstoneEngine.SHAPE_UPDATE_LIMIT);
    }

    /**
     * What vanilla's {@code Level.setBlock} does once the block is written: removal and placement
     * hooks, neighbour updates, comparators, then shape updates.
     */
    private void afterChange(final BlockPosition position, final BlockData previous, final BlockData current,
                             final Update update, final int limit) {
        final RedstoneBehaviour previousBehaviour = behaviour(previous);
        final RedstoneBehaviour currentBehaviour = behaviour(current);
        if (previousBehaviour != null && update == Update.NEIGHBORS && !previous.key().equals(current.key())) {
            try {
                previousBehaviour.onRemoved(this, position, previous);
            } catch (final RuntimeException exception) {
                fail(previous, "was removed", position, exception);
            }
            final BlockData after = blockAt(position);
            if (after == null || !after.key().equals(current.key())) {
                return; // replaced while the previous block was removed: that change spread on its own
            }
        }
        if (currentBehaviour != null) {
            try {
                currentBehaviour.onPlace(this, position, current, previous);
            } catch (final RuntimeException exception) {
                fail(current, "was placed", position, exception);
            }
            if (!current.equals(blockAt(position))) {
                return; // changed again while being placed: that change spread on its own
            }
        }
        if (update == Update.NEIGHBORS) {
            updateNeighbors(position, previous.key());
            if (currentBehaviour != null && currentBehaviour.hasAnalogOutput(current)) {
                analogOutputChanged(position);
            }
        }
        if (update != Update.QUIET) {
            updateShapesAround(position, previous, current, limit);
        }
    }

    /**
     * What vanilla's {@code Level.setBlock} does for the shapes around a block that changed: those of
     * the blocks the previous state reached beyond its neighbours, those of its neighbours, then those
     * the new state reaches.
     */
    private void updateShapesAround(final BlockPosition position, final BlockData previous, final BlockData current, final int limit) {
        if (limit <= 0) {
            return;
        }
        final RedstoneBehaviour previousBehaviour = behaviour(previous);
        final RedstoneBehaviour currentBehaviour = behaviour(current);
        final NeighborUpdater updater = engine.updater();
        final int previousLimit = updater.indirectLimit(limit - 1);
        try {
            if (previousBehaviour != null) {
                previousBehaviour.updateIndirectNeighborShapes(this, position, previous);
            }
            for (final BlockFace face : Faces.SHAPE_ORDER) {
                // the neighbour on that side sees this block on its opposite side
                updater.shapeUpdate(this, position.relative(face), face.opposite(), current, limit - 1);
            }
            if (currentBehaviour != null) {
                currentBehaviour.updateIndirectNeighborShapes(this, position, current);
            }
        } catch (final RuntimeException exception) {
            fail(current, "updated the shapes around it", position, exception);
        } finally {
            updater.indirectLimit(previousLimit);
        }
    }

    @Override
    public void updateNeighbors(final BlockPosition position, final TypedKey<BlockType> source) {
        engine.updater().updateNeighbors(this, position, source, null);
    }

    @Override
    public void updateNeighborsExcept(final BlockPosition position, final TypedKey<BlockType> source, final BlockFace skipped) {
        engine.updater().updateNeighbors(this, position, source, skipped);
    }

    @Override
    public void neighborChanged(final BlockPosition position, final TypedKey<BlockType> source) {
        engine.updater().neighborChanged(this, position, source);
    }

    @Override
    public void neighborShapeChanged(final BlockPosition position, final BlockFace direction) {
        final BlockData neighbor = blockAt(position.relative(direction));
        if (neighbor != null) {
            final NeighborUpdater updater = engine.updater();
            updater.shapeUpdate(this, position, direction, neighbor, updater.indirectLimit());
        }
    }

    @Override
    public void analogOutputChanged(final BlockPosition position) {
        final BlockData source = blockAt(position);
        final TypedKey<BlockType> sourceBlock = source == null ? BlockTypeKeys.AIR : source.key();
        for (final BlockFace face : Faces.HORIZONTAL) {
            final BlockPosition next = position.relative(face);
            final BlockData state = blockAt(next);
            if (state == null) {
                continue;
            }
            if (state.key().equals(BlockTypeKeys.COMPARATOR)) {
                neighborChanged(next, sourceBlock);
            } else if (BlockSupport.isConductor(state)) {
                final BlockPosition behind = next.relative(face);
                final BlockData behindState = blockAt(behind);
                if (behindState != null && behindState.key().equals(BlockTypeKeys.COMPARATOR)) {
                    neighborChanged(behind, sourceBlock);
                }
            }
        }
    }

    /**
     * Runs one neighbour update, on the thread owning {@code position}.
     */
    void runNeighborChanged(final BlockPosition position, final TypedKey<BlockType> source) {
        if (!owns(position)) {
            handOver(position, () -> engine.updater().neighborChanged(this, position, source));
            return;
        }
        final BlockData state = blockAt(position);
        final RedstoneBehaviour behaviour = behaviour(state);
        if (state == null || behaviour == null || !RedstoneEvents.update(world, position, state, source)) {
            return;
        }
        try {
            behaviour.neighborChanged(this, position, state, source);
        } catch (final RuntimeException exception) {
            fail(state, "reacted to a neighbour", position, exception);
        }
    }

    /**
     * Runs one shape update, on the thread owning {@code position}: what vanilla's
     * {@code executeShapeUpdate} then {@code updateOrDestroy} do.
     */
    void runShapeUpdate(final BlockPosition position, final BlockFace direction, final BlockData neighbor, final int limit) {
        if (!owns(position)) {
            handOver(position, () -> engine.updater().shapeUpdate(this, position, direction, neighbor, limit));
            return;
        }
        final BlockData state = blockAt(position);
        final RedstoneBehaviour behaviour = behaviour(state);
        if (state == null || behaviour == null) {
            return;
        }
        final BlockData updated;
        try {
            updated = behaviour.updateShape(this, position, state, direction, neighbor);
        } catch (final RuntimeException exception) {
            fail(state, "followed the shape of a neighbour", position, exception);
            return;
        }
        if (updated.equals(state)) {
            return;
        }
        if (updated.isAir()) {
            destroyBlock(position, limit);
        } else {
            setBlock(position, updated, Update.SHAPES, limit);
        }
    }

    @Override
    public void scheduleTick(final BlockPosition position, final TypedKey<BlockType> block, final int delay, final TickPriority priority) {
        engine.ticks().schedule(world, position, block, delay, priority);
    }

    @Override
    public boolean hasScheduledTick(final BlockPosition position, final TypedKey<BlockType> block) {
        return engine.ticks().hasScheduledTick(world, position, block);
    }

    @Override
    public boolean willTickThisTick(final BlockPosition position, final TypedKey<BlockType> block) {
        return engine.ticks().willTickThisTick(world, position, block);
    }

    @Override
    public void blockEvent(final BlockPosition position, final TypedKey<BlockType> block, final int type, final int data) {
        Preconditions.checkArgument(type >= 0 && type <= 255, "The type of a block event is between 0 and 255, not %s", type);
        Preconditions.checkArgument(data >= 0 && data <= 255, "The data of a block event is between 0 and 255, not %s", data);
        engine.events().add(world, position, block, type, data);
    }

    private boolean owns(final BlockPosition position) {
        return engine.regionizer().isOwnedByCurrentThread(world.key(), position.chunk());
    }

    /**
     * Hands a task over to the region owning {@code position}, unless its chunk is not loaded:
     * there is nothing to update there.
     */
    private void handOver(final BlockPosition position, final Runnable task) {
        if (world.loadedColumn(position.chunkX(), position.chunkZ()) != null) {
            engine.regionizer().execute(world.key(), position.chunk(), task);
        }
    }

    private void fail(final BlockData state, final String what, final BlockPosition position, final RuntimeException exception) {
        LOGGER.error("The redstone behaviour of {} failed when it {} at {} in {}", state.key(), what, position, world.key(), exception);
    }
}
