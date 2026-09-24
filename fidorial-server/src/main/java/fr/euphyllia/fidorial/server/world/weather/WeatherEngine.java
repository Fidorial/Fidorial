package fr.euphyllia.fidorial.server.world.weather;

import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.world.storage.LevelData;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.weather.Weather;
import fr.fidorial.world.weather.WeatherManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class WeatherEngine implements WeatherManager, AutoCloseable {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WeatherEngine.class);

    private static final int RAIN_MIN = 12_000, RAIN_BOUND = 24_000; // 10 a 20 min
    private static final int CLEAR_MIN = 12_000, CLEAR_BOUND = 180_000; // 10 min a 2 h 30
    private static final int THUNDER_MIN = 3_600, THUNDER_BOUND = 15_600; // 3 a 13 min
    private static final int THUNDER_OFF_MIN = 12_000, THUNDER_OFF_BOUND = 180_000;

    private final LevelData level;
    private final Consumer<ClientboundPacket> broadcaster;
    private final ScheduledExecutorService ticker;

    public WeatherEngine(final LevelData level, final Consumer<ClientboundPacket> broadcaster) {
        this.level = level;
        this.broadcaster = broadcaster;
        this.ticker = Executors.newSingleThreadScheduledExecutor(
                r -> Thread.ofPlatform().name("fidorial-weather").unstarted(r));
    }

    private static int nextRainDuration() {
        return ThreadLocalRandom.current().nextInt(RAIN_MIN, RAIN_BOUND);
    }

    private static int nextClearDuration() {
        return ThreadLocalRandom.current().nextInt(CLEAR_MIN, CLEAR_BOUND);
    }

    private static int nextThunderDuration() {
        return ThreadLocalRandom.current().nextInt(THUNDER_MIN, THUNDER_BOUND);
    }

    private static int nextThunderOffDuration() {
        return ThreadLocalRandom.current().nextInt(THUNDER_OFF_MIN, THUNDER_OFF_BOUND);
    }

    public void start() {
        synchronized (this) {
            if (level.rainTime <= 0) {
                level.rainTime = level.raining ? nextRainDuration() : nextClearDuration();
            }
            if (level.thunderTime <= 0) {
                level.thunderTime = level.thundering ? nextThunderDuration() : nextThunderOffDuration();
            }
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
        LOGGER.debug("Initial weather: {}", weather());
    }

    private synchronized void tick() {
        if (!level.gameRules.getBoolean(GameRuleKeys.ADVANCE_WEATHER)) {
            return;
        }
        if (level.clearWeatherTime > 0) {
            level.clearWeatherTime--;
            if (level.clearWeatherTime == 0) {
                level.rainTime = nextClearDuration();
                level.thunderTime = nextThunderOffDuration();
            }
            return;
        }

        if (--level.rainTime <= 0) {
            setRaining(!level.raining);
            level.rainTime = level.raining ? nextRainDuration() : nextClearDuration();
        }
        if (--level.thunderTime <= 0) {
            setThundering(!level.thundering);
            level.thunderTime = level.thundering ? nextThunderDuration() : nextThunderOffDuration();
        }
    }

    @Override
    public synchronized Weather weather() {
        if (level.raining && level.thundering) return Weather.THUNDER;
        if (level.raining) return Weather.RAIN;
        return Weather.CLEAR;
    }

    @Override
    public synchronized boolean isRaining() {
        return level.raining;
    }

    private void setRaining(final boolean raining) {
        if (level.raining == raining) {
            return;
        }
        level.raining = raining;
        broadcaster.accept(new ClientboundGameEventPacket(
                raining ? ClientboundGameEventPacket.BEGIN_RAINING : ClientboundGameEventPacket.END_RAINING, 0f));
        LOGGER.debug("Rain : {}", raining);
    }

    @Override
    public synchronized boolean isThundering() {
        return level.raining && level.thundering;
    }

    private void setThundering(final boolean thundering) {
        if (level.thundering == thundering) {
            return;
        }
        level.thundering = thundering;
        broadcaster.accept(
                new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thundering ? 1f : 0f));
        LOGGER.debug("Thunderstorm : {}", thundering);
    }

    @Override
    public synchronized void setWeather(final Weather weather, final int durationTicks) {
        switch (weather) {
            case CLEAR -> {
                setRaining(false);
                setThundering(false);
                level.clearWeatherTime = Math.max(durationTicks, 0);
                level.rainTime = durationTicks > 0 ? 0 : nextClearDuration();
                level.thunderTime = durationTicks > 0 ? 0 : nextThunderOffDuration();
            }
            case RAIN -> {
                level.clearWeatherTime = 0;
                setRaining(true);
                setThundering(false);
                level.rainTime = durationTicks > 0 ? durationTicks : nextRainDuration();
                level.thunderTime = nextThunderOffDuration();
            }
            case THUNDER -> {
                level.clearWeatherTime = 0;
                setRaining(true);
                setThundering(true);
                final int d = durationTicks > 0 ? durationTicks : nextThunderDuration();
                level.rainTime = d;
                level.thunderTime = d;
            }
        }
    }

    public synchronized void syncTo(final Consumer<ClientboundPacket> target) {
        if (!level.raining) {
            return;
        }
        target.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.BEGIN_RAINING, 0f));
        target.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1f));
        if (level.thundering) {
            target.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 1f));
        }
    }

    @Override
    public void close() {
        ticker.shutdownNow();
    }
}
