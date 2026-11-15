package fr.euphyllia.fidorial.server.tests;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.mob.MobFactories;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.math.Location;
import fr.fidorial.testing.ScenarioTestHelper;
import fr.fidorial.testing.annotation.ScenarioTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public final class EntitySchedulerTests {

    /// More than a single drain of the queue takes, so that the drain has to be scheduled again.
    private static final int TASKS = 300;

    @ScenarioTest
    public static void tasksFollowTheEntityInOrder(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        // two regions away, whatever the size of a region is
        final double far = ThreadedRegionRegionizer.sectionSize() * 16 * 2 + 0.5;
        final Location home = Location.of(world, 0.5, 65, 0.5, 0f, 0f);
        final Location away = Location.of(world, far, 65, far, 0f, 0f);
        final ServerPlayer player = (ServerPlayer) helper.summonPlayer("Chased", home, GameMode.SURVIVAL);

        final List<Integer> ran = new CopyOnWriteArrayList<>();
        final AtomicBoolean ranOffThread = new AtomicBoolean();
        final AtomicReference<CompletableFuture<Boolean>> teleport = new AtomicReference<>(CompletableFuture.completedFuture(false));

        helper.sequence()
                .execute(() -> {
                    // the teleport is queued first: the tasks are still waiting when the player changes region
                    teleport.set(player.teleport(away));
                    queueTasks(player, 0, ran, ranOffThread);
                })
                .waitUntil(() -> teleport.get().isDone() && ran.size() == TASKS,
                        "Expected the teleport and the tasks queued behind it to have run")
                .execute(() -> {
                    helper.assertTrue(teleport.get().join(), "Expected the teleport to succeed");
                    teleport.set(player.teleport(home));
                    queueTasks(player, TASKS, ran, ranOffThread);
                })
                .waitUntil(() -> teleport.get().isDone() && ran.size() == TASKS * 2,
                        "Expected the teleport back and the tasks queued behind it to have run")
                .execute(() -> {
                    helper.assertTrue(teleport.get().join(), "Expected the teleport back to succeed");
                    helper.assertFalse(ranOffThread.get(), "Expected every task to run on the thread owning the player");
                    helper.assertTrue(ran.equals(IntStream.range(0, TASKS * 2).boxed().toList()),
                            "Expected the tasks to run in the order they were queued");
                })
                .build();
    }

    private static void queueTasks(final ServerPlayer player, final int firstIndex, final List<Integer> ran, final AtomicBoolean ranOffThread) {
        for (int i = 0; i < TASKS; i++) {
            final int index = firstIndex + i;
            player.execute(() -> {
                if (!player.isOwnedByCurrentThread()) {
                    ranOffThread.set(true);
                }
                ran.add(index);
            });
        }
    }

    @ScenarioTest(timeoutTicks = 60)
    public static void removedEntityRetiresItsTasks(final ScenarioTestHelper helper) {
        final ServerWorld world = (ServerWorld) helper.world();
        final AtomicBoolean ran = new AtomicBoolean();
        final AtomicInteger retired = new AtomicInteger();
        final AtomicBoolean delayedRetired = new AtomicBoolean();

        helper.sequence()
                .execute(() -> {
                    final AbstractMovingMob cow = (AbstractMovingMob) MobFactories.create(EntityTypes.COW,
                            FidorialServer.getInstance().entities().ids().allocate(), Location.of(world, 4.5, 65, 4.5, 0f, 0f));
                    cow.goals().clear();
                    FidorialServer.getInstance().entities().spawn(cow);

                    cow.execute(() -> ran.set(true), retired::incrementAndGet);
                    cow.executeDelayed(() -> ran.set(true), () -> delayedRetired.set(true), 5L);
                    cow.remove();

                    helper.assertTrue(retired.get() == 1, "Expected the waiting task to be retired on removal");
                    helper.assertFalse(cow.execute(() -> ran.set(true), retired::incrementAndGet),
                            "Expected a removed entity to refuse tasks");
                    helper.assertTrue(retired.get() == 1, "Expected a refused task not to be retired");
                })
                .waitUntil(delayedRetired::get, "Expected the delayed task to be retired once its delay ran out")
                .execute(() -> helper.assertFalse(ran.get(), "Expected no task of the removed entity to run"))
                .build();
    }
}
