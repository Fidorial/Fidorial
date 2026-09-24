package fr.euphyllia.fidorial.server.world.weather;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.fidorial.world.weather.Weather;
import fr.fidorial.world.weather.WeatherManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public final class WorldWeather implements WeatherManager {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(WorldWeather.class);

    public static final String FILE_NAME = "weather.dat";
    private static final String TAG_DATA = "data";
    private static final String TAG_DATA_VERSION = "DataVersion";

    private static final int RAIN_MIN = 12_000, RAIN_BOUND = 24_000; // 10 a 20 min
    private static final int CLEAR_MIN = 12_000, CLEAR_BOUND = 180_000; // 10 min a 2 h 30
    private static final int THUNDER_MIN = 3_600, THUNDER_BOUND = 15_600; // 3 a 13 min
    private static final int THUNDER_OFF_MIN = 12_000, THUNDER_OFF_BOUND = 180_000;

    private final Key worldKey;
    private final WeatherState state;
    private final boolean hasCycle;
    private volatile @Nullable Consumer<ClientboundPacket> broadcaster;

    public WorldWeather(final Key worldKey, final WeatherState state, final boolean hasCycle) {
        this.worldKey = Preconditions.checkNotNull(worldKey, "The key of the world owning this weather must not be null");
        this.state = Preconditions.checkNotNull(state, "The weather state of " + worldKey + " must not be null");
        this.hasCycle = hasCycle;
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

    public boolean hasCycle() {
        return hasCycle;
    }

    public synchronized boolean attach(final Consumer<ClientboundPacket> broadcaster) {
        Preconditions.checkNotNull(broadcaster, "The broadcaster of the weather of %s must not be null", worldKey);
        if (this.broadcaster != null) {
            return false;
        }
        if (state.rainTime <= 0) {
            state.rainTime = state.raining ? nextRainDuration() : nextClearDuration();
        }
        if (state.thunderTime <= 0) {
            state.thunderTime = state.thundering ? nextThunderDuration() : nextThunderOffDuration();
        }
        this.broadcaster = broadcaster;
        return true;
    }

    public void detach() {
        this.broadcaster = null;
    }

    public synchronized void tick(final boolean advance) {
        if (!hasCycle || !advance) {
            return;
        }
        if (state.clearWeatherTime > 0) {
            state.clearWeatherTime--;
            if (state.clearWeatherTime == 0) {
                state.rainTime = nextClearDuration();
                state.thunderTime = nextThunderOffDuration();
            }
            return;
        }

        if (--state.rainTime <= 0) {
            setRaining(!state.raining);
            state.rainTime = state.raining ? nextRainDuration() : nextClearDuration();
        }
        if (--state.thunderTime <= 0) {
            setThundering(!state.thundering);
            state.thunderTime = state.thundering ? nextThunderDuration() : nextThunderOffDuration();
        }
    }

    @Override
    public synchronized Weather weather() {
        if (state.raining && state.thundering) return Weather.THUNDER;
        if (state.raining) return Weather.RAIN;
        return Weather.CLEAR;
    }

    @Override
    public synchronized boolean isRaining() {
        return state.raining;
    }

    @Override
    public synchronized boolean isThundering() {
        return state.raining && state.thundering;
    }

    private void setRaining(final boolean raining) {
        if (state.raining == raining) {
            return;
        }
        state.raining = raining;
        send(new ClientboundGameEventPacket(
                raining ? ClientboundGameEventPacket.BEGIN_RAINING : ClientboundGameEventPacket.END_RAINING, 0f));
        LOGGER.debug("Rain in {} : {}", worldKey, raining);
    }

    private void setThundering(final boolean thundering) {
        if (state.thundering == thundering) {
            return;
        }
        state.thundering = thundering;
        send(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, thundering ? 1f : 0f));
        LOGGER.debug("Thunderstorm in {} : {}", worldKey, thundering);
    }

    private void send(final ClientboundPacket packet) {
        final Consumer<ClientboundPacket> target = broadcaster;
        if (target != null) {
            target.accept(packet);
        }
    }

    @Override
    public synchronized void setWeather(final Weather weather, final int durationTicks) {
        Preconditions.checkNotNull(weather, "The weather to apply in %s must not be null", worldKey);
        switch (weather) {
            case CLEAR -> {
                setRaining(false);
                setThundering(false);
                state.clearWeatherTime = Math.max(durationTicks, 0);
                state.rainTime = durationTicks > 0 ? 0 : nextClearDuration();
                state.thunderTime = durationTicks > 0 ? 0 : nextThunderOffDuration();
            }
            case RAIN -> {
                state.clearWeatherTime = 0;
                setRaining(true);
                setThundering(false);
                state.rainTime = durationTicks > 0 ? durationTicks : nextRainDuration();
                state.thunderTime = nextThunderOffDuration();
            }
            case THUNDER -> {
                state.clearWeatherTime = 0;
                setRaining(true);
                setThundering(true);
                final int d = durationTicks > 0 ? durationTicks : nextThunderDuration();
                state.rainTime = d;
                state.thunderTime = d;
            }
        }
    }

    public synchronized void syncTo(final Consumer<ClientboundPacket> target) {
        if (!state.raining) {
            return;
        }
        target.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.BEGIN_RAINING, 0f));
        target.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.RAIN_LEVEL_CHANGE, 1f));
        if (state.thundering) {
            target.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE, 1f));
        }
    }

    public boolean restore(final Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        final CompoundBinaryTag root = BinaryTagIO.reader().readNamed(file, BinaryTagIO.Compression.GZIP).getValue();
        synchronized (this) {
            state.load(root.getCompound(TAG_DATA));
        }
        return true;
    }

    public void save(final Path file) throws IOException {
        final CompoundBinaryTag.Builder data = CompoundBinaryTag.builder();
        synchronized (this) {
            state.save(data);
        }
        final CompoundBinaryTag root = CompoundBinaryTag.builder()
                .put(TAG_DATA, data.build())
                .putInt(TAG_DATA_VERSION, VersionConstants.DATA_VERSION)
                .build();

        Files.createDirectories(file.getParent());
        final Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        BinaryTagIO.writer().writeNamed(Map.entry("", root), tmp, BinaryTagIO.Compression.GZIP);
        try {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public String toString() {
        return "WorldWeather{world=" + worldKey + ", weather=" + weather() + ", cycle=" + hasCycle + '}';
    }
}
