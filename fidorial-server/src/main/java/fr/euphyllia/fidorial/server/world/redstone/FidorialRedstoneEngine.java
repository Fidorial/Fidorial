package fr.euphyllia.fidorial.server.world.redstone;

import alternate.current.wire.WireHandler;
import fr.euphyllia.fidorial.server.schedulers.RegionWideTickHandler;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.BlockEditService;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneEngine;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The server's {@link RedstoneEngine}, a port of the vanilla one: signals resolved the vanilla way,
 * block updates run one chain at a time in vanilla order, and scheduled ticks with priorities.
 *
 * <p>It drives the behaviours of the {@link FidorialRedstoneRegistry}, and writes blocks through the
 * {@link BlockEditService}, so that players, light and fluids follow every change it makes. Regions
 * owning a margin around their loaded chunks, it never reaches into another region; where they never
 * merge, it stops at the border.</p>
 *
 * <p>Redstone dust is vanilla's, or {@linkplain WireHandler Alternate Current} in the worlds whose
 * configuration picks it, see {@link DustImplementation}.</p>
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
    /**
     * The Alternate Current handler each thread used last, kept for the next network of the same
     * world. Only one per thread, so that a thread holds on to no world but the last one it updated
     * dust in.
     */
    private final ThreadLocal<@Nullable WireHandler> wireHandlers = new ThreadLocal<>();
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
        this.ticks = new RedstoneBlockTicks(this, worlds);
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
        if (cached != null && cached.world() == world) {
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
    public RegionWideTickHandler tickHandler() {
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

    /**
     * {@return the Alternate Current handler of the current thread for a world}
     *
     * @param serverRedstoneWorld the redstone of the world
     */
    WireHandler wireHandler(final ServerRedstoneWorld serverRedstoneWorld) {
        final WireHandler last = wireHandlers.get();
        if (last != null && last.world() == serverRedstoneWorld) {
            return last;
        }
        final WireHandler created = new WireHandler(serverRedstoneWorld,
                () -> ((ServerWorld) serverRedstoneWorld.world()).configuration().redstone().alternateCurrent().updateOrder());
        if (last == null || !last.busy()) {
            wireHandlers.set(created);
        }
        return created;
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
