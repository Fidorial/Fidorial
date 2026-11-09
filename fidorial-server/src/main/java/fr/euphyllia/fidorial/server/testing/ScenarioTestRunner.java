package fr.euphyllia.fidorial.server.testing;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.Location;
import fr.fidorial.testing.ScenarioTestInfo;
import fr.fidorial.testing.ScenarioTestInstance;
import fr.fidorial.testing.ScenarioTestPlayerFactory;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import fr.fidorial.world.WorldSpec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

final class ScenarioTestRunner {

    private final List<ScenarioTestInfo> finished = new CopyOnWriteArrayList<>();
    private final AtomicInteger remaining = new AtomicInteger();
    private final AtomicBoolean doneFired = new AtomicBoolean();
    /**
     * The simulation distance around the origin of each test world.
     */
    static final int TEST_SIMULATION_DISTANCE = 1;

    private static final ChunkPos ORIGIN = new ChunkPos(0, 0);

    private volatile @Nullable Runnable onComplete;
    private volatile boolean started;

    void start(final List<ScenarioTestInstance> tests, final FidorialServer server) {
        final ScenarioTestPlayerFactory playerFactory = new ScenarioTestPlayerFactory() {
            @Override
            public Player spawn(final String name, final Location location, final GameMode gameMode) {
                return ScenarioTestPlayer.spawn(server, name, location, gameMode);
            }

            @Override
            public void despawn(final Player player) {
                ScenarioTestPlayer.despawn(server, (ServerPlayer) player);
            }
        };

        remaining.set(tests.size());

        final List<ServerWorld> worlds = new ArrayList<>(tests.size());
        final List<CompletableFuture<?>> lit = new ArrayList<>(tests.size());
        for (final ScenarioTestInstance test : tests) {
            final ServerWorld world = isolatedWorldFor(server, test);
            worlds.add(world);
            lit.add(prepare(server, world));
        }
        // tests start once the light of their chunks is worked out, rather than racing for the light workers
        CompletableFuture.allOf(lit.toArray(CompletableFuture[]::new)).join();
        for (int i = 0; i < tests.size(); i++) {
            final ServerWorld world = worlds.get(i);
            wire(server, world, new ScenarioTestInfo(tests.get(i), world, playerFactory));
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
        final World existing = server.worlds().get(key).orElse(null);
        final World world = existing != null
                ? existing
                : server.worlds().createWorldSilent(WorldSpec.builder(key).build());
        return (ServerWorld) world;
    }

    /**
     * Makes the origin of a test world tick like a player standing there with a simulation distance of
     * 1: entities tick in the 3x3 chunks around it, blocks and fluids in the 5x5, the 7x7 loaded.
     *
     * @return a future completing once the light of those chunks is worked out
     */
    private CompletableFuture<?> prepare(final FidorialServer server, final ServerWorld world) {
        server.workers().regionizer().addTicket(world.key(), ORIGIN);
        final int loaded = TEST_SIMULATION_DISTANCE + 2;
        final LongArrayList keys = new LongArrayList();
        for (int dx = -loaded; dx <= loaded; dx++) {
            for (int dz = -loaded; dz <= loaded; dz++) {
                try {
                    world.getChunk(ORIGIN.x() + dx, ORIGIN.z() + dz);
                } catch (final IOException e) {
                    throw new UncheckedIOException("Could not load the chunks of test world " + world.key(), e);
                }
                keys.add(ChunkPos.chunkKey(ORIGIN.x() + dx, ORIGIN.z() + dz));
            }
        }
        world.tickingChunks().add(ORIGIN.x(), ORIGIN.z(), TEST_SIMULATION_DISTANCE);
        return world.flushLight(keys);
    }

    private void wire(final FidorialServer server, final ServerWorld world, final ScenarioTestInfo info) {
        final int originSectionX = ThreadedRegionRegionizer.chunkToSection(ORIGIN.x());
        final int originSectionZ = ThreadedRegionRegionizer.chunkToSection(ORIGIN.z());
        // the section has ticked since its chunks loaded: the test counts its ticks from its first one
        final long[] before = {-1L};
        server.workers().regionizer().registerTickHandler((worldKey, sectionX, sectionZ, tick) -> {
            if (!worldKey.equals(world.key())
                    || sectionX != originSectionX
                    || sectionZ != originSectionZ
                    || info.isDone()) {
                return;
            }
            if (before[0] < 0L) {
                before[0] = tick - 1L;
            }
            info.tick((int) (tick - before[0]));
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
