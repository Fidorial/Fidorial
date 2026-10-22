package fr.euphyllia.fidorial.testplugin.tests;

import fr.euphyllia.fidorial.testplugin.pregen.PregenTask;
import fr.fidorial.math.Position;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.World;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public final class PregenTests {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(PregenTests.class);

    private static final int CENTER_CHUNK_X = 5_000;
    private static final int CENTER_CHUNK_Z = 5_000;
    private static final int CHUNK_SIZE = 16;

    @ScenarioTest(group = "testplugin.pregen")
    public static void pregenGeneratesChunks(final ScenarioTestHelper helper) {
        final World world = helper.world();

        final int radius = 1;

        final PregenTask task = new PregenTask(
                world,
                LOGGER,
                CENTER_CHUNK_X,
                CENTER_CHUNK_Z,
                radius,
                ignored -> {},
                () -> {},
                () -> {
                }
        );

        helper.sequence()
                .execute(task::start)
                .waitUntil(() -> helper.assertTrue(!task.isRunning(),
                        "Pregen did not finish: " + task.status()))
                .execute(() -> {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -radius; z <= radius; z++) {
                            assertChunkGenerated(
                                    helper,
                                    world,
                                    CENTER_CHUNK_X + x,
                                    CENTER_CHUNK_Z + z
                            );
                        }
                    }
                })
                .build();
    }

    private static void assertChunkGenerated(
            final ScenarioTestHelper helper,
            final World world,
            final int chunkX,
            final int chunkZ
    ) {
        final int blockX = chunkX * CHUNK_SIZE + CHUNK_SIZE / 2;
        final int blockZ = chunkZ * CHUNK_SIZE + CHUNK_SIZE / 2;
        final int blockY = world.minY() + 5;

        helper.assertTrue(
                world.blockKeyAt(Position.block(blockX, blockY, blockZ)).isPresent(),
                "Expected generated chunk at " + chunkX + "," + chunkZ
        );
    }
}
