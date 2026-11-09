package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.data.type.Repeater;
import fr.fidorial.world.fluid.FluidType;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Chunk levels and scheduled ticks as in Folia: what is scheduled in a chunk that does not tick waits
 * for it to tick, is saved with it and resumes once it loads again; fluids never load a chunk; entities
 * only tick where the chunks two away are loaded.
 *
 * <p>Each test works in chunk 10, which the region of the test owns (it is in the section of the
 * origin) but which does not tick until the test adds a source of ticking there.</p>
 */
@SuppressWarnings("unused")
public final class ChunkTickingTests {

    private static final int CHUNK_X = 10;
    private static final int X = CHUNK_X * 16; // first block of chunk 10
    private static final int Z = 2;
    private static final int Y = RedstoneTests.Y;

    private ChunkTickingTests() {
    }

    /**
     * Makes a chunk tick its entities, as a force-loaded chunk does, loading the two rings around it
     * right away rather than in the background.
     */
    static void tickAround(final ServerWorld world, final int chunkX, final int chunkZ) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                try {
                    world.getChunk(chunkX + dx, chunkZ + dz);
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        world.tickingChunks().add(chunkX, chunkZ, 0);
    }

    private static BlockPosition at(final int dx) {
        return Position.block(X + dx, Y, Z);
    }

    /**
     * A lever, a dust, a repeater looking east and its output dust, from {@code dx}.
     */
    private static BlockPosition repeaterLine(final ServerWorld world, final int dx, final int delay) {
        RedstoneTests.placeOnFloor(world, at(dx), BlockTypeKeys.LEVER);
        RedstoneTests.placeOnFloor(world, at(dx + 1), BlockTypeKeys.REDSTONE_WIRE);
        final BlockPosition repeater = at(dx + 2);
        RedstoneTests.place(world, repeater, BlockTypeKeys.REPEATER, BlockFace.UP, -90f, 30f);
        if (delay > 1) {
            final Repeater state = (Repeater) RedstoneTests.at(world, repeater);
            RedstoneTests.set(world, repeater, state.withDelay(delay));
        }
        RedstoneTests.placeOnFloor(world, at(dx + 3), BlockTypeKeys.REDSTONE_WIRE);
        return at(dx + 3);
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void tickWaitsForItsChunkToTick(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        final AtomicReference<@Nullable BlockPosition> output = new AtomicReference<>();
        final int[] waited = {0};
        helper.sequence()
                .execute(() -> {
                    RedstoneTests.load(world, at(0));
                    helper.assertFalse(world.isBlockTicking(CHUNK_X, 0), "Chunk 10 should not tick yet");
                    output.set(repeaterLine(world, 2, 1));
                    RedstoneTests.pull(world, at(2));
                    helper.assertTrue(world.blockTicks().hasScheduledTick(at(4), BlockTypeKeys.REPEATER),
                            "The repeater should have scheduled its tick");
                })
                .waitUntil(() -> ++waited[0] >= 10, "waiting")
                .execute(() -> {
                    helper.assertTrue(RedstoneTests.power(world, output.get()) == 0,
                            "The repeater should not switch while its chunk does not tick");
                    helper.assertTrue(world.blockTicks().hasScheduledTick(at(4), BlockTypeKeys.REPEATER),
                            "The tick should wait, not be dropped");
                    tickAround(world, CHUNK_X, 0);
                })
                .waitUntil(() -> RedstoneTests.power(world, output.get()) == 15,
                        "Once its chunk ticks, the repeater should run its late tick")
                .execute(() -> world.tickingChunks().remove(CHUNK_X, 0, 0))
                .build();
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void pendingTicksAreSavedWithTheChunkAndResume(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        final AtomicReference<@Nullable BlockPosition> output = new AtomicReference<>();
        helper.sequence()
                .execute(() -> {
                    tickAround(world, CHUNK_X, 0);
                    RedstoneTests.load(world, at(0));
                })
                .waitUntil(() -> world.isBlockTicking(CHUNK_X, 0), "Chunk 10 should tick once the chunks around load")
                .execute(() -> {
                    output.set(repeaterLine(world, 2, 4));
                    RedstoneTests.pull(world, at(2));
                    world.unloadChunk(CHUNK_X, 0, false).join();
                    helper.assertTrue(world.loadedColumn(CHUNK_X, 0) == null, "Chunk 10 should be unloaded");
                    helper.assertFalse(world.blockTicks().hasScheduledTick(at(4), BlockTypeKeys.REPEATER),
                            "The tick should leave memory with its chunk");
                    RedstoneTests.load(world, at(0));
                    helper.assertTrue(world.blockTicks().hasScheduledTick(at(4), BlockTypeKeys.REPEATER),
                            "The tick saved with the chunk should be scheduled again as it loads");
                    helper.assertTrue(RedstoneTests.power(world, output.get()) == 0, "The repeater should still be off");
                })
                .waitUntil(() -> RedstoneTests.power(world, output.get()) == 15,
                        "The repeater should switch once the delay saved with its tick is over")
                .execute(() -> world.tickingChunks().remove(CHUNK_X, 0, 0))
                .build();
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void waterWaitsAtTheBorderWithoutLoadingTheNextChunk(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        final BlockPosition source = at(15); // last block of chunk 10
        final BlockPosition next = at(16); // first block of chunk 11
        final int[] waited = {0};
        helper.sequence()
                .execute(() -> {
                    RedstoneTests.load(world, source);
                    helper.assertTrue(world.loadedColumn(CHUNK_X + 1, 0) == null, "Chunk 11 should not be loaded");
                    RedstoneTests.set(world, source, RedstoneTests.defaults(BlockTypeKeys.WATER));
                })
                .waitUntil(() -> ++waited[0] >= 15, "waiting")
                .execute(() -> {
                    helper.assertTrue(world.loadedColumn(CHUNK_X + 1, 0) == null, "The water should not load chunk 11");
                    helper.assertTrue(world.fluidTicks().hasScheduledTick(source, FluidType.WATER),
                            "The tick of the water should wait for its chunk to tick");
                    tickAround(world, CHUNK_X, 0);
                })
                .waitUntil(() -> world.loadedColumn(CHUNK_X + 1, 0) != null
                                && RedstoneTests.at(world, next).key().equals(BlockTypeKeys.WATER),
                        "Once the chunks tick, the water should flow into chunk 11")
                .execute(() -> world.tickingChunks().remove(CHUNK_X, 0, 0))
                .build();
    }

    @ScenarioTest(timeoutTicks = 100)
    public static void entitiesOnlyTickWhereTheChunksAroundAreLoaded(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        final AtomicReference<@Nullable ServerItemEntity> item = new AtomicReference<>();
        final double height = Y + 10;
        final int[] waited = {0};
        helper.sequence()
                .execute(() -> {
                    RedstoneTests.load(world, at(8));
                    item.set(FidorialServer.getInstance().entities().dropItem(world, X + 8.5, height, Z + 0.5,
                            ItemStack.of(ItemKeys.STICK, 1), 0.0, 0.0, 0.0, 0, null));
                })
                .waitUntil(() -> ++waited[0] >= 10, "waiting")
                .execute(() -> {
                    final ServerItemEntity dropped = item.get();
                    helper.assertTrue(dropped != null && dropped.location().y() == height,
                            "The item should hang in the air while its chunk does not tick its entities");
                    tickAround(world, CHUNK_X, 0);
                })
                .waitUntil(() -> {
                    final ServerItemEntity dropped = item.get();
                    return dropped != null && dropped.location().y() < height;
                }, "Once the chunks two away are loaded, the item should fall")
                .execute(() -> world.tickingChunks().remove(CHUNK_X, 0, 0))
                .build();
    }
}
