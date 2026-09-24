package fr.euphyllia.fidorial.server.schedulers;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetTimePacket;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.WorldManager;
import fr.euphyllia.fidorial.server.world.time.WorldClocks;
import fr.euphyllia.fidorial.server.world.time.WorldTimeEngine;
import fr.fidorial.registry.keys.GameRuleKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DayNightThread implements AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(DayNightThread.class);

    private static final int SYNC_INTERVAL_TICKS = 20;

    private final WorldManager worldManager;
    private final RegistryHolder registries;
    private final ScheduledExecutorService ticker;
    private final Set<Key> unknownClocks = ConcurrentHashMap.newKeySet();
    private final Set<Key> attached = ConcurrentHashMap.newKeySet();

    private int sinceLastSync;

    private volatile RegionTickProfiler tickProfiler = RegionTickProfiler.NO_OP;

    public DayNightThread(final WorldManager worldManager, final RegistryHolder registries) {
        this.worldManager = worldManager;
        this.registries = registries;
        this.ticker = Executors.newSingleThreadScheduledExecutor(
                r -> Thread.ofPlatform().name("fidorial-daynight-thread").unstarted(r));
    }

    public void start() {
        for (final ServerWorld world : worldManager.worlds()) {
            attach(world);
        }
        ticker.scheduleAtFixedRate(
                () -> {
                    try {
                        tick();
                    } catch (final Throwable t) {
                        LOGGER.error("Day/night cycle tick failed", t);
                    }
                },
                50,
                50,
                TimeUnit.MILLISECONDS);

        for (final ServerWorld world : worldManager.worlds()) {
            final WorldTimeEngine cycle = world.dayNightCycle();
            LOGGER.debug(
                    "Cycle of {}: {} ({} ticks, clock {})",
                    world.dimension().id(),
                    cycle.phase(),
                    cycle.timeOfDay(),
                    cycle.clock());
        }
    }

    public void setTickProfiler(final @Nullable RegionTickProfiler profiler) {
        this.tickProfiler = profiler == null ? RegionTickProfiler.NO_OP : profiler;
    }

    public void attach(final ServerWorld world) {
        if (attached.add(world.dimension().id())) {
            world.dayNightCycle().setBroadcaster(cycle -> broadcast(world, cycle));
        }
    }

    private void tick() {
        final boolean sync = ++sinceLastSync >= SYNC_INTERVAL_TICKS;
        if (sync) {
            sinceLastSync = 0;
        }
        final boolean advanceTime = advanceTime();
        for (final ServerWorld world : worldManager.worlds()) {
            attach(world);
            final WorldTimeEngine cycle = world.dayNightCycle();
            cycle.tick(advanceTime);
            if (sync) {
                broadcast(world, cycle);
            }
        }
    }

    private boolean advanceTime() {
        return worldManager.levelData().gameRules.getBoolean(GameRuleKeys.ADVANCE_TIME);
    }

    public void resyncAll() {
        for (final ServerWorld world : worldManager.worlds()) {
            broadcast(world, world.dayNightCycle());
        }
    }

    private void broadcast(final ServerWorld world, final WorldTimeEngine cycle) {
        final ClientboundSetTimePacket packet = packetFor(cycle);
        if (packet == null) {
            return;
        }
        final List<ServerPlayer> players = FidorialServer.getInstance().players();
        for (int i = 0, size = players.size(); i < size; i++) {
            final ServerPlayer player = players.get(i);
            if (player.world().equals(world)) {
                if (!player.connection().isInPlayState()) continue;
                player.connection().send(packet);
            }
        }
    }

    public void syncTo(final ServerWorld world, final Consumer<ClientboundPacket> target) {
        final ClientboundSetTimePacket packet = packetFor(world.dayNightCycle());
        if (packet != null) {
            target.accept(packet);
        }
    }

    private @Nullable ClientboundSetTimePacket packetFor(final WorldTimeEngine cycle) {
        final int networkId = registries.networkId(WorldClocks.REGISTRY, cycle.clock());
        if (networkId < 0) {
            if (unknownClocks.add(cycle.clock())) {
                LOGGER.warn("Clock missing from register {} : {}", WorldClocks.REGISTRY, cycle.clock());
            }
            return null;
        }
        return new ClientboundSetTimePacket(cycle.worldAge(), List.of(cycle.snapshot(networkId, advanceTime())));
    }

    @Override
    public void close() {
        ticker.shutdownNow();
        for (final ServerWorld world : worldManager.worlds()) {
            world.dayNightCycle().setBroadcaster(null);
        }
        attached.clear();
    }
}
