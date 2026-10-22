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

    public void setDebugEvents(final DebugGameEvents debugEvents) {
        this.debugEvents = debugEvents;
    }

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

    public boolean set(final ServerWorld world, final BlockPosition pos, final BlockState state) {
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
        broadcaster.broadcast(pos, blockRegistry.networkId(state));
        fluidNotifier.notifyBlockChanged(world.dimension().id(), pos.blockX(), pos.blockY(), pos.blockZ());
        lightNotifier.onBlockChanged(world.dimension().id(), pos.blockX(), pos.blockY(), pos.blockZ());

        final DebugGameEvents events = debugEvents;
        if (events != null && previous != null && !previous.isAir() && !state.isAir() && !previous.equals(state)) {
            events.emit(world, GameEventKeys.BLOCK_CHANGE, pos);
        }
        return true;
    }

    @FunctionalInterface
    public interface BlockChangeBroadcaster {
        void broadcast(BlockPosition pos, int stateId);
    }

    @FunctionalInterface
    public interface FluidNotifier {
        void notifyBlockChanged(Key world, int x, int y, int z);
    }

    @FunctionalInterface
    public interface LightNotifier {
        void onBlockChanged(Key world, int x, int y, int z);
    }
}
