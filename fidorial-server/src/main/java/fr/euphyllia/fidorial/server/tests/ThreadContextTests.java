package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.util.threading.ThreadContexts;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.Location;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
public final class ThreadContextTests {

    private static final Location SPAWN = new Location(0.5, 65, 0.5, 0f, 0f);

    @ScenarioTest(timeoutTicks = 40)
    public static void entityMovedTripsFromForeignThread(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        final ServerPlayer player = (ServerPlayer) helper.summonPlayer("NotScheduledMover", SPAWN, GameMode.SURVIVAL);
        final ChunkPos fromChunk = SPAWN.chunk();
        final ChunkPos toChunk = new ChunkPos(fromChunk.x() + 1, fromChunk.z());

        helper.sequence()
                .execute(() -> helper.assertTrue((runOnForeignThread(() -> world.entityMoved(player, fromChunk, toChunk))), "Expected a thread violation exception to be thrown"))
                .build();
    }

    @ScenarioTest(timeoutTicks = 40)
    public static void teleportIsProperlyScheduled(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        final ServerPlayer player = (ServerPlayer) helper.summonPlayer("ScheduledMover", SPAWN, GameMode.SURVIVAL);
        final ChunkPos fromChunk = player.chunk();
        final Location newPos = new Location(1024, 248, -1024, 2f, 0f);

        helper.sequence()
                .execute(() -> player.teleport(world, newPos))
                .waitUntil(() -> player.location().equals(newPos),
                        "Expected the player to have moved to " + newPos)
                .build();
    }

    private static boolean runOnForeignThread(final Runnable action) {
        final AtomicReference<@Nullable Throwable> thrown = new AtomicReference<>();
        final CountDownLatch done = new CountDownLatch(1);

        final Thread foreign = new Thread(() -> {
            try {
                action.run();
            } catch (final Throwable t) {
                thrown.set(t);
            } finally {
                done.countDown();
            }
        }, "thread-check-test-foreign-thread");
        foreign.setDaemon(true);
        foreign.start();

        try {
            if (!done.await(5, TimeUnit.SECONDS)) {
                return false;
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        return thrown.get() instanceof ThreadContexts.ThreadViolationException;
    }
}
