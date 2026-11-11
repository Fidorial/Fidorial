package fr.euphyllia.fidorial.server.world.farming;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.BlockEditService;
import fr.euphyllia.fidorial.server.world.BlockStateRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.ServerWorldManager;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingEngine;
import fr.fidorial.world.farming.FarmingWorld;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * The server's {@link FarmingEngine}.
 *
 * <p>On every game tick of a region, it gives random ticks to the chunks of that region standing
 * within {@value #TICK_DISTANCE} chunks of a player, and to its force-loaded chunks: in each of
 * their sections, as many blocks as the {@code random_tick_speed} game rule says are picked at
 * random, and those whose {@link FarmingBehaviour} asks for it are ticked. Which chunks tick is
 * worked out again every second.</p>
 *
 * <p>A region only ticks while something keeps it awake, and a player only keeps awake the region
 * they stand in. So that a field lying across the border of two regions grows evenly, the engine
 * keeps awake the regions a player reaches without standing in them, for as long as the player
 * stays that close.</p>
 *
 * <p>It drives the behaviours of the {@link FidorialFarmingRegistry}, and writes blocks through the
 * {@link BlockEditService}, so that players, light, fluids and redstone follow every change it
 * makes.</p>
 */
public final class FidorialFarmingEngine implements FarmingEngine {

    /**
     * How far from a player chunks get random ticks, in chunks.
     */
    public static final int TICK_DISTANCE = 8;

    /**
     * How often a region works out again which of its chunks tick, in game ticks.
     */
    static final int REFRESH_INTERVAL = 20;

    /**
     * How long a region is kept awake after the last player came close to it, in nanoseconds:
     * several refreshes, so that one late tick does not send it to sleep.
     */
    private static final long LEASE_NANOS = 5_000_000_000L;

    private static final long[] NO_CHUNKS = new long[0];

    private static final ComponentLogger LOGGER = ComponentLogger.logger(FidorialFarmingEngine.class);

    private final ServerWorldManager worlds;
    private final ThreadedRegionRegionizer regionizer;
    private final BlockStateRegistry blockStates;
    private final FidorialFarmingRegistry registry;
    private final BlockEditService blockEdits;
    private final ServerRegistries registries;
    private final Supplier<? extends Collection<ServerPlayer>> players;
    private final Map<Key, ServerFarmingWorld> views = new ConcurrentHashMap<>();
    private final Map<RegionKey, TickingChunks> regions = new ConcurrentHashMap<>();
    private final Map<RegionKey, Lease> leases = new ConcurrentHashMap<>();

    /**
     * @param players the players online, whatever their world
     */
    public FidorialFarmingEngine(
            final ServerWorldManager worlds,
            final ThreadedRegionRegionizer regionizer,
            final BlockStateRegistry blockStates,
            final FidorialFarmingRegistry registry,
            final BlockEditService blockEdits,
            final ServerRegistries registries,
            final Supplier<? extends Collection<ServerPlayer>> players
    ) {
        this.worlds = worlds;
        this.regionizer = regionizer;
        this.blockStates = blockStates;
        this.registry = registry;
        this.blockEdits = blockEdits;
        this.registries = registries;
        this.players = players;
    }

    @Override
    public FarmingWorld world(final World world) {
        if (!(world instanceof final ServerWorld serverWorld)) {
            throw new IllegalArgumentException("The farming engine only drives the worlds of the server, not " + world);
        }
        return view(serverWorld);
    }

    /**
     * {@return the farming view of a world, created on first use}
     *
     * @param world the world
     */
    public ServerFarmingWorld view(final ServerWorld world) {
        final ServerFarmingWorld cached = views.get(world.key());
        if (cached != null && cached.serverWorld() == world) {
            return cached;
        }
        // first use, or a world unloaded then loaded again under the same key
        final ServerFarmingWorld created = new ServerFarmingWorld(this, world);
        views.put(world.key(), created);
        return created;
    }

    /**
     * Forgets what this engine holds about a region, and lets go of what it kept awake there: what
     * the server calls instead of {@link #tick} while a plugin provides the engine in effect.
     *
     * @param worldKey the key of the world of the region
     * @param sectionX the X coordinate of the region, in regions
     * @param sectionZ the Z coordinate of the region, in regions
     */
    public void releaseRegion(final Key worldKey, final int sectionX, final int sectionZ) {
        final RegionKey key = new RegionKey(worldKey, sectionX, sectionZ);
        regions.remove(key);
        release(key);
    }

    @Override
    public void tick(final Key worldKey, final int sectionX, final int sectionZ, final long currentTick) {
        final RegionKey key = new RegionKey(worldKey, sectionX, sectionZ);
        final ServerWorld world = worlds.world(worldKey);
        if (world == null) {
            regions.remove(key);
            release(key);
            return;
        }
        TickingChunks ticking = regions.get(key);
        if (ticking == null || currentTick - ticking.refreshedAt() >= REFRESH_INTERVAL) {
            ticking = new TickingChunks(tickingChunks(world, key), currentTick);
            regions.put(key, ticking);
            expire(key);
        }
        final int speed = world.gameRules().getInt(GameRuleKeys.RANDOM_TICK_SPEED);
        if (speed <= 0 || ticking.chunks().length == 0) {
            return;
        }
        final ServerFarmingWorld view = view(world);
        for (final long chunk : ticking.chunks()) {
            final ChunkColumn column = world.loadedColumn(unpackX(chunk), unpackZ(chunk));
            if (column != null) {
                randomTicks(view, column, speed);
            }
        }
    }

    /**
     * Works out which chunks of a region get random ticks: its loaded chunks close enough to a
     * player, and those that are force-loaded. The regions the players standing in this one reach
     * are kept awake on the way.
     */
    private long[] tickingChunks(final ServerWorld world, final RegionKey region) {
        final int shift = ThreadedRegionRegionizer.sectionShift();
        final int minX = region.sectionX() << shift;
        final int minZ = region.sectionZ() << shift;
        final int maxX = minX + ThreadedRegionRegionizer.sectionSize() - 1;
        final int maxZ = minZ + ThreadedRegionRegionizer.sectionSize() - 1;
        final LongSet chunks = new LongOpenHashSet();
        for (final ServerPlayer player : players.get()) {
            if (player.world() != world || player.isRemoved()) {
                continue;
            }
            final ChunkPos center = player.location().chunk();
            final int fromX = Math.max(minX, center.x() - TICK_DISTANCE);
            final int toX = Math.min(maxX, center.x() + TICK_DISTANCE);
            final int fromZ = Math.max(minZ, center.z() - TICK_DISTANCE);
            final int toZ = Math.min(maxZ, center.z() + TICK_DISTANCE);
            for (int x = fromX; x <= toX; x++) {
                for (int z = fromZ; z <= toZ; z++) {
                    if (world.loadedColumn(x, z) != null) {
                        chunks.add(pack(x, z));
                    }
                }
            }
            if (center.x() >= minX && center.x() <= maxX && center.z() >= minZ && center.z() <= maxZ) {
                keepNeighboursAwake(region, center);
            }
        }
        for (final ChunkPos forced : world.forceLoadedChunks()) {
            if (forced.x() >= minX && forced.x() <= maxX && forced.z() >= minZ && forced.z() <= maxZ
                    && world.loadedColumn(forced.x(), forced.z()) != null) {
                chunks.add(pack(forced.x(), forced.z()));
            }
        }
        return chunks.isEmpty() ? NO_CHUNKS : chunks.toLongArray();
    }

    /**
     * Keeps awake the regions a player reaches without standing in them: those of the four corners
     * of the square of chunks ticking around the player.
     */
    private void keepNeighboursAwake(final RegionKey own, final ChunkPos center) {
        final long expiry = System.nanoTime() + LEASE_NANOS;
        for (int corner = 0; corner < 4; corner++) {
            final ChunkPos reached = new ChunkPos(
                    center.x() + ((corner & 1) == 0 ? -TICK_DISTANCE : TICK_DISTANCE),
                    center.z() + ((corner & 2) == 0 ? -TICK_DISTANCE : TICK_DISTANCE));
            final RegionKey neighbour = RegionKey.of(own.world(), reached);
            if (neighbour.equals(own)) {
                continue;
            }
            leases.compute(neighbour, (_, lease) -> {
                if (lease == null) {
                    regionizer.addTicket(own.world(), reached);
                    return new Lease(reached, expiry);
                }
                return new Lease(lease.ticket(), expiry);
            });
        }
    }

    /**
     * Lets a region go back to sleep once no player came close to it for a while.
     */
    private void expire(final RegionKey region) {
        final long now = System.nanoTime();
        leases.computeIfPresent(region, (_, lease) -> {
            if (now - lease.expiresAt() < 0) {
                return lease;
            }
            regionizer.removeTicket(region.world(), lease.ticket());
            return null;
        });
    }

    private static long pack(final int chunkX, final int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static int unpackX(final long chunk) {
        return (int) (chunk >> 32);
    }

    private static int unpackZ(final long chunk) {
        return (int) chunk;
    }

    private void release(final RegionKey region) {
        final Lease lease = leases.remove(region);
        if (lease != null) {
            regionizer.removeTicket(region.world(), lease.ticket());
        }
    }

    private void randomTicks(final ServerFarmingWorld view, final ChunkColumn column, final int speed) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int originX = column.chunkX() << 4;
        final int originZ = column.chunkZ() << 4;
        for (final ChunkSection section : column.sections()) {
            if (section == null || section.isEmpty()) {
                continue;
            }
            final int originY = section.sectionY() << 4;
            for (int pick = 0; pick < speed; pick++) {
                final int picked = random.nextInt(ChunkSection.BLOCK_COUNT);
                final int x = picked & 15;
                final int y = (picked >> 4) & 15;
                final int z = picked >> 8;
                final BlockState state = section.getBlock(x, y, z);
                if (state.isAir()) {
                    continue;
                }
                final FarmingBehaviour behaviour = registry.behaviourOrNull(state.name());
                if (behaviour != null) {
                    randomTick(view, behaviour, Position.block(originX + x, originY + y, originZ + z), state);
                }
            }
        }
    }

    private void randomTick(final ServerFarmingWorld view, final FarmingBehaviour behaviour, final BlockPosition position,
                            final BlockState state) {
        final BlockData data = blockStates.resolve(state);
        if (data == null) {
            return;
        }
        try {
            if (behaviour.ticksRandomly(data)) {
                behaviour.randomTick(view, position, data);
            }
        } catch (final RuntimeException exception) {
            LOGGER.error("The farming behaviour of {} failed on a random tick at {} in {}",
                    state.name(), position, view.world().key(), exception);
        }
    }

    /**
     * Gives one block a random tick, as if it had been picked: what a command or a test growing a
     * plant on purpose needs.
     *
     * @param world    the world of the block
     * @param position the block, owned by the calling thread
     * @return {@code true} if the block has a behaviour that took the tick
     */
    public boolean randomTick(final ServerWorld world, final BlockPosition position) {
        final ServerFarmingWorld view = view(world);
        final BlockData data = view.blockAt(position);
        if (data == null || !view.owns(position)) {
            return false;
        }
        final FarmingBehaviour behaviour = registry.behaviourOrNull(data.key());
        if (behaviour == null || !behaviour.ticksRandomly(data)) {
            return false;
        }
        behaviour.randomTick(view, position, data);
        return true;
    }

    void runTick(final ServerWorld world, final BlockPosition position, final TypedKey<BlockType> block) {
        final ServerFarmingWorld view = view(world);
        final BlockData state = view.blockAt(position);
        if (state == null || !state.key().equals(block)) {
            return; // unloaded, or replaced since
        }
        final FarmingBehaviour behaviour = registry.behaviourOrNull(block);
        if (behaviour == null) {
            return;
        }
        try {
            behaviour.tick(view, position, state);
        } catch (final RuntimeException exception) {
            LOGGER.error("The farming behaviour of {} failed to tick at {} in {}", block, position, world.key(), exception);
        }
    }

    boolean isTagged(final TypedKey<BlockType> block, final TagKey<BlockType> tag) {
        return registries.registry(RegistryKey.BLOCK).isTagged(block, tag);
    }

    /**
     * {@return {@code true} if rain or snow may fall where a block is, going by its biome}
     */
    boolean hasPrecipitation(final ServerWorld world, final BlockPosition position) {
        if (world.loadedColumn(position.chunkX(), position.chunkZ()) == null) {
            return false;
        }
        try {
            final TypedKey<BiomeDefinition> biome = world.getBiome(position.blockX(), position.blockY(), position.blockZ());
            return biome == null || registries.registry(RegistryKey.BIOME).find(biome)
                    .map(BiomeDefinition::hasPrecipitation).orElse(true);
        } catch (final IOException | RuntimeException exception) {
            return true;
        }
    }

    ThreadedRegionRegionizer regionizer() {
        return regionizer;
    }

    BlockStateRegistry blockStates() {
        return blockStates;
    }

    /**
     * {@return the registry holding the behaviours this engine drives}
     */
    public FidorialFarmingRegistry registry() {
        return registry;
    }

    BlockEditService blockEdits() {
        return blockEdits;
    }

    private record RegionKey(Key world, int sectionX, int sectionZ) {

        static RegionKey of(final Key world, final ChunkPos chunk) {
            return new RegionKey(world, ThreadedRegionRegionizer.chunkToSection(chunk.x()),
                    ThreadedRegionRegionizer.chunkToSection(chunk.z()));
        }
    }

    /**
     * The chunks of a region getting random ticks, as worked out on one of its game ticks.
     */
    private record TickingChunks(long[] chunks, long refreshedAt) {
    }

    /**
     * What keeps awake a region no player stands in: the ticket taken on it, and until when.
     */
    private record Lease(ChunkPos ticket, long expiresAt) {
    }
}
