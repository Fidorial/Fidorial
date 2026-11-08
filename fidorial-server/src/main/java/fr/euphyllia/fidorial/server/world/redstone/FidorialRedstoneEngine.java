package fr.euphyllia.fidorial.server.world.redstone;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.BlockEditService;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneEngine;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The server's {@link RedstoneEngine}, a port of the vanilla one: signals resolved the vanilla way,
 * block updates run one chain at a time in vanilla order, and scheduled ticks with priorities.
 *
 * <p>It drives the behaviours of the {@link FidorialRedstoneRegistry}, and writes blocks through the
 * {@link BlockEditService}, so that players, light and fluids follow every change it makes. What
 * crosses into another region is handed over to the thread of that region.</p>
 */
public final class FidorialRedstoneEngine implements RedstoneEngine {

    /**
     * Vanilla's {@code max-chained-neighbor-updates}.
     */
    public static final int MAX_CHAINED_NEIGHBOR_UPDATES = 1_000_000;

    /**
     * How deep shape updates may cause one another, vanilla's update limit.
     */
    static final int SHAPE_UPDATE_LIMIT = 512;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialRedstoneEngine.class);

    private final ThreadedRegionRegionizer regionizer;
    private final BlockStateRegistry blockStates;
    private final FidorialRedstoneRegistry registry;
    private final BlockEditService blockEdits;
    private final RedstoneBlockTicks ticks;
    private final ThreadLocal<NeighborUpdater> updaters =
            ThreadLocal.withInitial(() -> new NeighborUpdater(MAX_CHAINED_NEIGHBOR_UPDATES));
    private final Map<Key, ServerRedstoneWorld> views = new ConcurrentHashMap<>();

    public FidorialRedstoneEngine(
            final ServerWorldManager worlds,
            final ThreadedRegionRegionizer regionizer,
            final BlockStateRegistry blockStates,
            final FidorialRedstoneRegistry registry,
            final BlockEditService blockEdits
    ) {
        this.regionizer = regionizer;
        this.blockStates = blockStates;
        this.registry = registry;
        this.blockEdits = blockEdits;
        this.ticks = new RedstoneBlockTicks(this, worlds, regionizer);
    }

    @Override
    public RedstoneWorld world(final World world) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            throw new IllegalArgumentException("The redstone engine only drives the worlds of the server, not " + world);
        }
        return view(serverWorld);
    }

    /**
     * {@return the redstone view of a world, created on first use}
     *
     * @param world the world
     */
    public ServerRedstoneWorld view(final ServerWorld world) {
        final ServerRedstoneWorld cached = views.get(world.key());
        if (cached != null && cached.serverWorld() == world) {
            return cached;
        }
        // first use, or a world unloaded then loaded again under the same key
        final ServerRedstoneWorld created = new ServerRedstoneWorld(this, world);
        views.put(world.key(), created);
        return created;
    }

    /**
     * {@return the handler running the scheduled ticks, to register on the regionizer}
     */
    public RegionTickHandler tickHandler() {
        return ticks;
    }

    void runTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        final ServerRedstoneWorld view = view(world);
        final BlockData state = view.blockAt(position);
        if (state == null || !state.key().equals(block)) {
            return; // unloaded, or replaced since: vanilla drops the tick too
        }
        final RedstoneBehaviour behaviour = registry.behaviourOrNull(block);
        if (behaviour == null) {
            return;
        }
        try {
            behaviour.tick(view, position, state);
        } catch (final RuntimeException exception) {
            LOGGER.error("The redstone behaviour of {} failed to tick at {} in {}", block, position, world.key(), exception);
        }
    }

    NeighborUpdater updater() {
        return updaters.get();
    }

    RedstoneBlockTicks ticks() {
        return ticks;
    }

    ThreadedRegionRegionizer regionizer() {
        return regionizer;
    }

    BlockStateRegistry blockStates() {
        return blockStates;
    }

    FidorialRedstoneRegistry registry() {
        return registry;
    }

    BlockEditService blockEdits() {
        return blockEdits;
    }
}
