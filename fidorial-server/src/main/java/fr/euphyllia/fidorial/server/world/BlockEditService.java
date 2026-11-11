package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.debug.DebugGameEvents;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.GameEventKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BlockEditService {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockEditService.class);

    private final BlockStateRegistry blockRegistry;
    private final BlockChangeBroadcaster broadcaster;
    private final FluidNotifier fluidNotifier;
    private final LightNotifier lightNotifier;

    /**
     * The blocks each thread changed while it {@linkplain #holdBroadcasts() holds} its broadcasts,
     * {@code null} for a thread sending them as they come.
     */
    private final ThreadLocal<@Nullable HeldBroadcasts> held = new ThreadLocal<>();

    private volatile @Nullable DebugGameEvents debugEvents;
    private volatile RedstoneNotifier redstoneNotifier = (_, _, _, _) -> {

    };

    private volatile BlockChangeListener changeListener = (_, _, _, _) -> {

    };

    public BlockEditService(
            final BlockStateRegistry blockRegistry,
            final BlockChangeBroadcaster broadcaster,
            final FluidNotifier fluidNotifier,
            final LightNotifier lightNotifier
    ) {
        this.blockRegistry = blockRegistry;
        this.broadcaster = broadcaster;
        this.fluidNotifier = fluidNotifier;
        this.lightNotifier = lightNotifier;
    }

    public void setDebugEvents(final DebugGameEvents debugEvents) {
        this.debugEvents = debugEvents;
    }

    /**
     * Sets who follows up on block changes for redstone: the redstone engine in effect.
     *
     * @param redstoneNotifier told of every block that changes through {@link #set(ServerWorld, BlockPosition, BlockState, boolean)}
     */
    public void setRedstoneNotifier(final RedstoneNotifier redstoneNotifier) {
        this.redstoneNotifier = redstoneNotifier;
    }

    /**
     * Sets who is told of every block that changes through
     * {@link #set(ServerWorld, BlockPosition, BlockState, boolean)}, whoever changed it and whether
     * or not redstone follows up on its own: the farming engine, whose plants stand or fall with
     * the blocks around them.
     *
     * @param changeListener told of every block that changes
     */
    public void setChangeListener(final BlockChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * Changes a block, then tells the players that see it, the fluids, the light and, unless told
     * not to, redstone.
     *
     * @param world          the world of the block
     * @param pos            where the block is
     * @param state          its new state
     * @param notifyRedstone {@code false} when the redstone engine changes the block itself, and
     *                       follows up on the change in vanilla order
     * @return {@code true} if the block changed
     */
    public boolean set(final ServerWorld world, final BlockPosition pos, final BlockState state, final boolean notifyRedstone) {
        return set(world, pos, state, notifyRedstone, true);
    }

    /**
     * Changes a block, then tells the fluids, the light and, unless told not to, the players that
     * see it and redstone.
     *
     * @param world          the world of the block
     * @param pos            where the block is
     * @param state          its new state
     * @param notifyRedstone {@code false} when the redstone engine changes the block itself, and
     *                       follows up on the change in vanilla order
     * @param visible        {@code false} to keep the change from the players, as vanilla does for
     *                       the blocks a piston is moving: their client works them out from the
     *                       block event of the piston, and would lose its animation to the update
     * @return {@code true} if the block changed
     */
    public boolean set(final ServerWorld world, final BlockPosition pos, final BlockState state, final boolean notifyRedstone,
                       final boolean visible) {
        final BlockState previous;
        try {
            previous = world.getBlock(pos.blockX(), pos.blockY(), pos.blockZ());
            if (!world.setBlock(pos.blockX(), pos.blockY(), pos.blockZ(), state)) {
                return false;
            }
        } catch (final IOException e) {
            LOGGER.error("Block change impossible at {},{},{}", pos.x(), pos.y(), pos.z(), e);
            return false;
        }
        if (visible) {
            broadcast(world, pos, state);
        }
        fluidNotifier.notifyBlockChanged(world.dimension().id(), pos.blockX(), pos.blockY(), pos.blockZ());
        lightNotifier.onBlockChanged(world.dimension().id(), pos.blockX(), pos.blockY(), pos.blockZ());

        final DebugGameEvents events = debugEvents;
        if (events != null && previous != null && !previous.isAir() && !state.isAir() && !previous.equals(state)) {
            events.emit(world, GameEventKeys.BLOCK_CHANGE, pos);
        }
        if (previous != null && !previous.equals(state)) {
            if (notifyRedstone) {
                redstoneNotifier.blockChanged(world, pos, previous, state);
            }
            changeListener.blockChanged(world, pos, previous, state);
        }
        return true;
    }

    /**
     * Writes a block that only changes properties neither light nor fluids depend on, such as the
     * power of redstone dust: players see it, but light is not checked again, fluids are not woken
     * up and redstone is not told. Thousands of dust may change power in one game tick, and each of
     * those checks would cost more than the change itself.
     *
     * @param world the world
     * @param pos   where the block is
     * @param state the new state, giving and blocking the same light as the old one, and holding the
     *              same fluid
     * @return {@code false} if the block could not be written, such as when its chunk is not loaded
     */
    public boolean setQuietly(final ServerWorld world, final BlockPosition pos, final BlockState state) {
        final BlockState previous;
        try {
            previous = world.getBlock(pos.blockX(), pos.blockY(), pos.blockZ());
            if (!world.setBlock(pos.blockX(), pos.blockY(), pos.blockZ(), state)) {
                return false;
            }
        } catch (final IOException e) {
            LOGGER.error("Block change impossible at {},{},{}", pos.x(), pos.y(), pos.z(), e);
            return false;
        }
        broadcast(world, pos, state);
        final DebugGameEvents events = debugEvents;
        if (events != null && previous != null && !previous.isAir() && !state.isAir() && !previous.equals(state)) {
            events.emit(world, GameEventKeys.BLOCK_CHANGE, pos);
        }
        return true;
    }

    /**
     * Holds back, on the calling thread, what players are told of the blocks changing, until
     * {@link #releaseBroadcasts()}: the blocks change as usual, and players are told once, of the
     * state each ended up in.
     *
     * <p>What a block event needs: vanilla sends the event before the blocks it changed, which only
     * leave with the end of the game tick, and a client plays a piston out from the world as it was
     * before the piston moved.</p>
     *
     * @throws IllegalStateException if the thread already holds its broadcasts
     */
    public void holdBroadcasts() {
        if (held.get() != null) {
            throw new IllegalStateException("The broadcasts of this thread are already held");
        }
        held.set(new HeldBroadcasts());
    }

    /**
     * Tells players of the blocks that changed since {@link #holdBroadcasts()}, in the order they
     * first changed, and sends the following changes as they come again.
     */
    public void releaseBroadcasts() {
        final HeldBroadcasts holding = held.get();
        if (holding == null) {
            return;
        }
        held.remove();
        for (final HeldBroadcast change : holding.changes) {
            final BlockPosition pos = change.pos();
            final ChunkColumn column = change.world().loadedColumn(pos.chunkX(), pos.chunkZ());
            if (column != null) {
                final BlockState current = column.getBlock(pos.blockX() & 15, pos.blockY(), pos.blockZ() & 15);
                broadcaster.broadcast(change.world(), pos, blockRegistry.networkId(current));
            }
        }
    }

    private void broadcast(final ServerWorld world, final BlockPosition pos, final BlockState state) {
        final HeldBroadcasts holding = held.get();
        if (holding != null) {
            holding.add(world, pos);
            return;
        }
        broadcaster.broadcast(world, pos, blockRegistry.networkId(state));
    }

    private record HeldBroadcast(ServerWorld world, BlockPosition pos) {
    }

    /**
     * The blocks that changed while broadcasts were held, each once, in the order they first changed.
     */
    private static final class HeldBroadcasts {

        private final List<HeldBroadcast> changes = new ArrayList<>();
        private final Set<HeldBroadcast> seen = new HashSet<>();

        void add(final ServerWorld world, final BlockPosition pos) {
            final HeldBroadcast change = new HeldBroadcast(world, pos);
            if (seen.add(change)) {
                changes.add(change);
            }
        }
    }

    @FunctionalInterface
    public interface BlockChangeBroadcaster {
        void broadcast(ServerWorld world, BlockPosition pos, int stateId);
    }

    @FunctionalInterface
    public interface FluidNotifier {
        void notifyBlockChanged(Key world, int x, int y, int z);
    }

    @FunctionalInterface
    public interface LightNotifier {
        void onBlockChanged(Key world, int x, int y, int z);
    }

    /**
     * Told of a block that changed, so that redstone follows up: the blocks around it react, dust
     * reconnects, observers notice.
     */
    @FunctionalInterface
    public interface RedstoneNotifier {
        void blockChanged(ServerWorld world, BlockPosition pos, BlockState previous, BlockState current);
    }

    /**
     * Told of a block that changed, once everything else followed up on the change.
     */
    @FunctionalInterface
    public interface BlockChangeListener {
        void blockChanged(ServerWorld world, BlockPosition pos, BlockState previous, BlockState current);
    }
}
