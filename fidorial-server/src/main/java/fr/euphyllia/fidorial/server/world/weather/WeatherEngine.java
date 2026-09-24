package fr.euphyllia.fidorial.server.world.weather;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.WorldManager;
import fr.euphyllia.fidorial.server.world.storage.Dimension;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.weather.Weather;
import fr.fidorial.world.weather.WeatherManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class WeatherEngine implements WeatherManager, AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WeatherEngine.class);

    private final WorldManager worldManager;
    private final ScheduledExecutorService ticker;

    public WeatherEngine(final WorldManager worldManager) {
        this.worldManager = Objects.requireNonNull(worldManager, "The world manager whose worlds get a weather cycle must not be null");
        this.ticker = Executors.newSingleThreadScheduledExecutor(
                r -> Thread.ofPlatform().name("fidorial-weather").unstarted(r));
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
                        LOGGER.error("Error during weather tick", t);
                    }
                },
                50,
                50,
                TimeUnit.MILLISECONDS);
        for (final ServerWorld world : worldManager.worlds()) {
            LOGGER.debug("Initial weather of {}: {}", world.key(), world.weather().weather());
        }
    }

    public void attach(final ServerWorld world) {
        world.weather().attach(packet -> broadcast(world, packet));
    }

    private void tick() {
        for (final ServerWorld world : worldManager.worlds()) {
            attach(world);
            world.weather().tick(world.gameRuleValues().getBoolean(GameRuleKeys.ADVANCE_WEATHER));
        }
    }

    private static void broadcast(final ServerWorld world, final ClientboundPacket packet) {
        final List<ServerPlayer> players = FidorialServer.getInstance().players();
        for (int i = 0, size = players.size(); i < size; i++) {
            final ServerPlayer player = players.get(i);
            if (player.world().equals(world) && player.connection().isInPlayState()) {
                player.connection().send(packet);
            }
        }
    }

    public void syncTo(final ServerWorld world, final Consumer<ClientboundPacket> target) {
        world.weather().syncTo(target);
    }

    public ServerWorld primaryWorld() {
        final ServerWorld overworld = worldManager.dimension(Dimension.OVERWORLD);
        if (overworld != null) {
            return overworld;
        }
        return worldManager.defaultWorld()
                .orElseThrow(() -> new IllegalStateException("No world is loaded, there is no weather to manage"));
    }

    private WorldWeather primary() {
        return primaryWorld().weather();
    }

    @Override
    public Weather weather() {
        return primary().weather();
    }

    @Override
    public boolean isRaining() {
        return primary().isRaining();
    }

    @Override
    public boolean isThundering() {
        return primary().isThundering();
    }

    @Override
    public void setWeather(final Weather weather, final int durationTicks) {
        primary().setWeather(weather, durationTicks);
    }

    @Override
    public void close() {
        ticker.shutdownNow();
        for (final ServerWorld world : worldManager.worlds()) {
            world.weather().detach();
        }
    }
}
