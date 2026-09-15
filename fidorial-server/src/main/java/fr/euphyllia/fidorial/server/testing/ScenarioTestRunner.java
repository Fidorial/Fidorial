package fr.euphyllia.fidorial.server.testing;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.testing.ScenarioTestInfo;
import fr.fidorial.testing.ScenarioTestInstance;
import fr.fidorial.testing.ScenarioTestPlayerFactory;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldBuilder;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class ScenarioTestRunner {

    private final List<ScenarioTestInfo> finished = new CopyOnWriteArrayList<>();
    private final AtomicInteger remaining = new AtomicInteger();
    private final AtomicBoolean doneFired = new AtomicBoolean();
    private volatile @Nullable Runnable onComplete;
    private volatile boolean started;

    void start(final List<ScenarioTestInstance> tests, final FidorialServer server) {
        final ScenarioTestPlayerFactory playerFactory = new ScenarioTestPlayerFactory() {
            @Override
            public Player spawn(final String name, final World world, final Location location, final GameMode gameMode) {
                return ScenarioTestPlayer.spawn(server, ((ServerWorld) world), name, location, gameMode);
            }

            @Override
            public void despawn(final Player player) {
                ScenarioTestPlayer.despawn(server, (ServerPlayer) player);
            }
        };

        remaining.set(tests.size());

        for (final ScenarioTestInstance test : tests) {
            final ServerWorld world = isolatedWorldFor(server, test);
            final ScenarioTestInfo info = new ScenarioTestInfo(test, world, playerFactory);
            wire(server, world, info);
        }

        started = true;
        if (tests.isEmpty()) {
            fireCompleteIfDone();
        }
    }

    private ServerWorld isolatedWorldFor(final FidorialServer server, final ScenarioTestInstance test) {
        final String group = test.group().toLowerCase(Locale.ROOT).replace("/", "_");
        final String id = test.id().toLowerCase(Locale.ROOT).replace("/", "_");
        final Key key = Key.key("scenario_test", group + "/" + id);
        final World existing = server.world(key).orElse(null);
        final World world = existing != null
                ? existing
                : server.createWorldSilent(WorldBuilder.builder(key).build());
        return (ServerWorld) world;
    }

    private void wire(final FidorialServer server, final ServerWorld world, final ScenarioTestInfo info) {
        final ChunkPos origin = new ChunkPos(0, 0);
        final int originSectionX = origin.x() >> ThreadedRegionRegionizer.SECTION_SHIFT;
        final int originSectionZ = origin.z() >> ThreadedRegionRegionizer.SECTION_SHIFT;

        server.regionizer().addTicket(world.key(), origin);
        server.regionizer().registerTickHandler((worldKey, sectionX, sectionZ, tick) -> {
            if (!worldKey.equals(world.key())
                    || sectionX != originSectionX
                    || sectionZ != originSectionZ
                    || info.isDone()) {
                return;
            }
            info.tick((int) tick);
            if (info.isDone()) {
                onTestFinished(info);
            }
        });
    }

    private void onTestFinished(final ScenarioTestInfo info) {
        finished.add(info);
        if (remaining.decrementAndGet() == 0) {
            fireCompleteIfDone();
        }
    }

    private void fireCompleteIfDone() {
        if (doneFired.compareAndSet(false, true)) {
            final Runnable action = onComplete;
            if (action != null) action.run();
        }
    }

    void onComplete(final Runnable action) {
        this.onComplete = action;
    }

    boolean isDone() {
        return started && remaining.get() == 0;
    }

    List<ScenarioTestInfo> finished() {
        return finished;
    }

    int failedRequiredCount() {
        return (int) finished.stream()
                .filter(i -> i.state() == ScenarioTestInfo.State.FAILED && i.instance().required())
                .count();
    }
}
