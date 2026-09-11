package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.Chunk;
import fr.fidorial.world.ChunkPos;

public final class ForceLoadTests {

    private static final ChunkPos POS = new ChunkPos(64, -64);

    @ScenarioTest(timeoutTicks = 200)
    public static void forceLoadedChunkIsReportedByTheApi(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();

        helper.sequence()
                .execute(() -> {
                    helper.assertTrue(!world.isChunkForceLoaded(POS),
                            "The chunk must not be force-loaded before the test");
                    helper.assertTrue(world.setChunkForceLoaded(POS.x(), POS.z(), true),
                            "Forcing the chunk should change its state");
                    helper.assertTrue(!world.setChunkForceLoaded(POS.x(), POS.z(), true),
                            "Forcing an already forced chunk should not change anything");

                    helper.assertTrue(world.isChunkForceLoaded(POS),
                            "World#isChunkForceLoaded should report the forced chunk");
                    helper.assertTrue(world.forceLoadedChunks().contains(POS),
                            "World#forceLoadedChunks should contain the forced chunk");
                })
                .waitUntil(() -> helper.assertTrue(
                        world.getChunkIfLoaded(POS.x(), POS.z()).map(Chunk::isForceLoaded).orElse(false),
                        "The forced chunk should get loaded, and Chunk#isForceLoaded report it"))
                .execute(() -> helper.assertTrue(!world.unloadChunkAsync(POS).join(),
                        "A force-loaded chunk must refuse to unload"))
                .execute(() -> {
                    helper.assertTrue(world.setChunkForceLoaded(POS.x(), POS.z(), false),
                            "Unforcing the chunk should change its state");

                    helper.assertTrue(!world.isChunkForceLoaded(POS),
                            "World#isChunkForceLoaded should no longer report the chunk");
                    helper.assertTrue(!world.forceLoadedChunks().contains(POS),
                            "World#forceLoadedChunks should no longer contain the chunk");
                    helper.assertTrue(!world.getChunkIfLoaded(POS.x(), POS.z()).map(Chunk::isForceLoaded).orElse(false),
                            "Chunk#isForceLoaded should no longer report the chunk");
                })
                .build();
    }
}
