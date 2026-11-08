package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.debug.DebugGameEvents;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.GameEventKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public final class BlockEditService {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockEditService.class);

    private final BlockStateRegistry blockRegistry;
    private final BlockChangeBroadcaster broadcaster;
    private final FluidNotifier fluidNotifier;
    private final LightNotifier lightNotifier;

    private volatile @Nullable DebugGameEvents debugEvents;
    private volatile RedstoneNotifier redstoneNotifier = (_, _, _, _) -> {

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
        broadcaster.broadcast(world, pos, blockRegistry.networkId(state));
        fluidNotifier.notifyBlockChanged(world.dimension().id(), pos.blockX(), pos.blockY(), pos.blockZ());
        lightNotifier.onBlockChanged(world.dimension().id(), pos.blockX(), pos.blockY(), pos.blockZ());

        final DebugGameEvents events = debugEvents;
        if (events != null && previous != null && !previous.isAir() && !state.isAir() && !previous.equals(state)) {
            events.emit(world, GameEventKeys.BLOCK_CHANGE, pos);
        }
        if (notifyRedstone && previous != null && !previous.equals(state)) {
            redstoneNotifier.blockChanged(world, pos, previous, state);
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
        broadcaster.broadcast(world, pos, blockRegistry.networkId(state));
        final DebugGameEvents events = debugEvents;
        if (events != null && previous != null && !previous.isAir() && !state.isAir() && !previous.equals(state)) {
            events.emit(world, GameEventKeys.BLOCK_CHANGE, pos);
        }
        return true;
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
}
