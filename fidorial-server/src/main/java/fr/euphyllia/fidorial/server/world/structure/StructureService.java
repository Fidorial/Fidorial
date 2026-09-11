package fr.euphyllia.fidorial.server.world.structure;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.datapack.DatapackContents;
import fr.euphyllia.fidorial.server.datapack.DatapackLoader;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelChunkWithLightPacket;
import fr.euphyllia.fidorial.server.world.ChunkGenerator;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.structure.gen.ColumnTarget;
import fr.euphyllia.fidorial.server.world.structure.gen.StructureChunkGenerator;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.JigsawAssembler;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.PlacedPiece;
import fr.euphyllia.fidorial.server.world.structure.jigsaw.StructureStart;
import fr.euphyllia.fidorial.server.world.structure.math.Box;
import fr.euphyllia.fidorial.server.world.structure.math.LegacyRandom;
import fr.euphyllia.fidorial.server.world.structure.math.Rotations;
import fr.euphyllia.fidorial.server.world.structure.processor.ProcessorList;
import fr.euphyllia.fidorial.server.world.structure.template.StructureTemplateImpl;
import fr.euphyllia.fidorial.server.world.structure.worldgen.JigsawStructure;
import fr.euphyllia.fidorial.server.world.structure.worldgen.TerrainAdaptation;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import fr.fidorial.world.structure.Datapack;
import fr.fidorial.world.structure.StructureManager;
import fr.fidorial.world.structure.StructureRotation;
import fr.fidorial.world.structure.StructureTemplate;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public final class StructureService implements StructureManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(StructureService.class);

    private final Path folder;
    private final BlockValidator validator;
    private final BooleanSupplier generationEnabled;
    private final Map<Key, StructureChunkGenerator> generators = new ConcurrentHashMap<>();
    private final ExecutorService worker = Executors.newFixedThreadPool(2, runnable -> {
        final Thread thread = new Thread(runnable, "fidorial-structures");
        thread.setDaemon(true);
        return thread;
    });
    private volatile StructureRegistry registry;

    public StructureService(final Path folder, final BlockValidator validator, final BooleanSupplier generationEnabled) {
        this.folder = folder;
        this.validator = validator;
        this.generationEnabled = generationEnabled;
        this.registry = StructureRegistry.empty(validator);
    }

    public Path folder() {
        return folder;
    }

    public void load() {
        final long start = System.nanoTime();
        DatapackContents contents;
        try {
            contents = DatapackLoader.load(folder);
        } catch (final IOException failure) {
            LOGGER.error("[datapacks] Unable to read {}", folder, failure);
            contents = DatapackContents.empty();
        }
        final StructureRegistry loaded = StructureRegistry.build(contents, validator);
        this.registry = loaded;
        for (final StructureChunkGenerator generator : generators.values()) {
            generator.reload(loaded);
        }
        LOGGER.info("[datapacks] {} pack(s) in {}: {} template(s), {} jigsaw structure(s), {} structure set(s) generated ({} ms)",
                contents.packs().size(), folder, contents.templates().size(), loaded.structures().size(),
                loaded.structureSets().size(), (System.nanoTime() - start) / 1_000_000L);
    }

    public StructureRegistry registry() {
        return registry;
    }

    public boolean generationEnabled() {
        return generationEnabled.getAsBoolean();
    }

    public ChunkGenerator wrap(final Key world, final ChunkGenerator base, final long seed) {
        if (base instanceof final StructureChunkGenerator already) {
            return already;
        }
        final StructureChunkGenerator generator = new StructureChunkGenerator(world, base, seed, registry, generationEnabled);
        generators.put(world, generator);
        return generator;
    }

    public void forget(final Key world) {
        generators.remove(world);
    }

    public @Nullable StructureChunkGenerator generator(final World world) {
        return world instanceof final ServerWorld serverWorld
                && serverWorld.generator instanceof final StructureChunkGenerator generator ? generator : null;
    }

    @Override
    public List<Datapack> datapacks() {
        return registry.contents().packs();
    }

    @Override
    public Set<Key> templates() {
        return registry.templateKeys();
    }

    @Override
    public Optional<StructureTemplate> template(final Key key) {
        return Optional.ofNullable(registry.template(key));
    }

    @Override
    public Set<Key> structures() {
        return registry.structures().keySet();
    }

    @Override
    public CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(this::load, worker);
    }

    public void shutdown() {
        worker.shutdownNow();
    }

    @Override
    public CompletableFuture<Integer> placeTemplate(final World world, final BlockPos origin, final Key key,
                                                    final StructureRotation rotation) {
        final StructureChunkGenerator generator = generator(world);
        final StructureTemplateImpl template = registry.template(key);
        if (generator == null || template == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown template " + key.asString()));
        }
        final ServerWorld serverWorld = (ServerWorld) world;
        final Box box = Rotations.box(origin.x(), origin.y(), origin.z(),
                template.sizeX(), template.sizeY(), template.sizeZ(), rotation);
        final StructureGenerator structures = generator.structures();
        return forEachChunk(serverWorld, box, target -> structures.placer().placeTemplate(
                template, origin.x(), origin.y(), origin.z(), rotation, ProcessorList.EMPTY, target, true));
    }

    @Override
    public CompletableFuture<Integer> placeStructure(final World world, final BlockPos position, final Key key) {
        final StructureChunkGenerator generator = generator(world);
        final JigsawStructure structure = registry.structure(key);
        if (generator == null || structure == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown structure " + key.asString()));
        }
        final StructureGenerator structures = generator.structures();
        return CompletableFuture
                .supplyAsync(() -> structures.assembler().generate(structure, position.x() >> 4, position.z() >> 4, false), worker)
                .thenCompose(start -> placeStart((ServerWorld) world, structures, start));
    }

    public CompletableFuture<Integer> placeJigsaw(final World world, final BlockPos position, final Key pool,
                                                  final Key target, final int maxDepth) {
        final StructureChunkGenerator generator = generator(world);
        if (generator == null || registry.pool(pool) == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown pool " + pool.asString()));
        }
        final StructureGenerator structures = generator.structures();
        final JigsawStructure synthetic = new JigsawStructure(pool, Set.of(), pool, target, Math.clamp(maxDepth, 0, 20),
                (random, minY, height) -> position.y(), null, 128, 128, false, TerrainAdaptation.NONE, true, 0, 0, List.of());
        return CompletableFuture.supplyAsync(() -> {
            final LegacyRandom random = new LegacyRandom(structures.terrain().seed()
                    ^ LegacyRandom.positionSeed(position.x(), position.y(), position.z()));
            final JigsawAssembler.Stub stub = structures.assembler().stub(synthetic, pool, target,
                    position.x(), position.y(), position.z(), random, false);
            return stub == null ? StructureStart.EMPTY : structures.assembler().assemble(stub);
        }, worker).thenCompose(start -> placeStart((ServerWorld) world, structures, start));
    }

    @Override
    public CompletableFuture<Optional<BlockPos>> locate(final World world, final BlockPos origin, final Key structure,
                                                        final int radiusCells) {
        final StructureChunkGenerator generator = generator(world);
        if (generator == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        final StructureGenerator structures = generator.structures();
        return CompletableFuture.supplyAsync(() -> {
            final StructureGenerator.Located located = structures.locate(structure, origin.x(), origin.z(), radiusCells);
            return Optional.ofNullable(located).map(found -> new BlockPos(found.x(), found.y(), found.z()));
        }, worker);
    }

    private CompletableFuture<Integer> placeStart(final ServerWorld world, final StructureGenerator structures,
                                                  final StructureStart start) {
        if (start.isEmpty() || start.bounds() == null) {
            return CompletableFuture.completedFuture(0);
        }
        return forEachChunk(world, start.bounds(), target -> {
            int written = 0;
            for (final PlacedPiece piece : start.pieces()) {
                final int before = target.written();
                structures.placer().place(piece, start.structure(), target);
                written += target.written() - before;
            }
            return written;
        }).thenApply(_ -> start.pieces().size());
    }

    private interface ChunkWork {
        int run(ColumnTarget target);
    }

    private CompletableFuture<Integer> forEachChunk(final ServerWorld world, final Box box, final ChunkWork work) {
        final AtomicInteger total = new AtomicInteger();
        final List<CompletableFuture<Void>> pending = new ArrayList<>();
        for (int chunkX = box.minX() >> 4; chunkX <= box.maxX() >> 4; chunkX++) {
            for (int chunkZ = box.minZ() >> 4; chunkZ <= box.maxZ() >> 4; chunkZ++) {
                final int cx = chunkX;
                final int cz = chunkZ;
                final CompletableFuture<Void> future = new CompletableFuture<>();
                pending.add(future);
                world.scheduler().execute(world.key(), new ChunkPos(cx, cz), () -> {
                    try {
                        final ChunkColumn column = world.getChunk(cx, cz);
                        final ColumnTarget target = new ColumnTarget(column);
                        final int written = work.run(target);
                        if (written > 0) {
                            total.addAndGet(written);
                            world.markDirty(cx, cz);
                            world.relightChunk(cx, cz);
                            resend(world, column);
                        }
                        future.complete(null);
                    } catch (final IOException failure) {
                        future.completeExceptionally(new UncheckedIOException(failure));
                    } catch (final RuntimeException failure) {
                        future.completeExceptionally(failure);
                    }
                });
            }
        }
        return CompletableFuture.allOf(pending.toArray(new CompletableFuture[0])).thenApply(_ -> total.get());
    }

    private static void resend(final ServerWorld world, final ChunkColumn column) {
        final FidorialServer server = FidorialServer.getInstance();
        final ClientboundLevelChunkWithLightPacket packet;
        synchronized (world.lightManager()) {
            packet = new ClientboundLevelChunkWithLightPacket(
                    server.chunkSerializer(), column, world.generator.dimensionType().hasSkylight());
        }
        final int range = server.config().viewDistance() + 1;
        for (final ServerPlayer player : server.players()) {
            if (player.isRemoved() || player.world() != world) {
                continue;
            }
            final ChunkPos chunk = player.chunk();
            if (Math.abs(chunk.x() - column.chunkX()) <= range && Math.abs(chunk.z() - column.chunkZ()) <= range) {
                player.connection().send(packet);
            }
        }
    }
}
