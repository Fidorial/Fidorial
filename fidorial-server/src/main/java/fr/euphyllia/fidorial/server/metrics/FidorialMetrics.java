package fr.euphyllia.fidorial.server.metrics;

import dev.faststats.Metrics;
import dev.faststats.data.Metric;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.schedulers.RegionTickProfiler;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.plugin.PluginMeta;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class FidorialMetrics {

    private static final long MIB = 1024L * 1024L;

    private final FidorialServer server;
    private final TickTimeRecorder tickTimes = new TickTimeRecorder();
    private final AtomicInteger peakPlayers = new AtomicInteger();
    private volatile String[] plugins = new String[0];

    public FidorialMetrics(final FidorialServer server) {
        this.server = server;
    }

    public Metrics create(final Metrics.Factory factory) {
        return factory
                .addMetric(Metric.stringArray("plugins", () -> plugins))
                .addMetric(Metric.number("plugin_count", () -> plugins.length))

                // World load
                .addMetric(Metric.number("world_count", () -> server.worldManager().worlds().size()))
                .addMetric(Metric.number("loaded_chunks", this::loadedChunks))
                .addMetric(Metric.number("entity_count", this::entityCount))
                .addMetric(Metric.number("active_regions", () -> server.regionizer().tpsSnapshots().size()))

                .addMetric(Metric.number("mspt_average", tickTimes::averageMspt))
                .addMetric(Metric.number("slow_tick_percent", tickTimes::slowTickPercent))
                .addMetric(Metric.number("peak_players", () -> Math.max(peakPlayers.get(), server.playerCount())))

                .addMetric(Metric.string("proxy_mode", this::proxyMode))
                .addMetric(Metric.string("network_transport", () -> server.network().transport()))
                .addMetric(Metric.number("view_distance", () -> server.config().general().viewDistance()))
                .addMetric(Metric.number("max_memory_mb", FidorialMetrics::maxMemoryMib))
                .addMetric(Metric.numberMap("worker_threads", this::workerThreads))
                .addMetric(Metric.booleanMap("features", this::features))

                .onFlush(this::startNewWindow)
                .create();
    }

    public RegionTickProfiler tickProfiler() {
        return tickTimes;
    }

    public void onServerStarted() {
        this.plugins = server.plugins().loaded().stream()
                .map(PluginMeta::id)
                .filter(id -> server.plugins().isEnabled(id))
                .sorted()
                .toArray(String[]::new);
        recordPlayerCount(server.playerCount());
    }

    public void recordPlayerCount(final int playerCount) {
        peakPlayers.accumulateAndGet(playerCount, Math::max);
    }

    private void startNewWindow() {
        tickTimes.reset();
        peakPlayers.set(server.playerCount());
    }

    private long loadedChunks() {
        long chunks = 0L;
        for (final ServerWorld world : server.worldManager().worlds()) {
            chunks += world.loadedChunkCount();
        }
        return chunks;
    }

    private long entityCount() {
        long entities = 0L;
        for (final ServerWorld world : server.worldManager().worlds()) {
            entities += world.entityManager().count();
        }
        return entities;
    }

    private String proxyMode() {
        return switch (server.config().network().proxy()) {
            case final ServerConfiguration.ProxyForwarding.None _ -> "none";
            case final ServerConfiguration.ProxyForwarding.Velocity _ -> "velocity";
        };
    }

    private Map<String, Integer> workerThreads() {
        final ServerConfiguration.Threading threading = server.config().threading();
        final Map<String, Integer> threads = new LinkedHashMap<>(4);
        threads.put("region", threading.regionWorkers());
        threads.put("chunk", threading.chunkWorkers());
        threads.put("light", threading.lightWorkers());
        threads.put("ai", threading.aiWorkers());
        return threads;
    }

    private Map<String, Boolean> features() {
        final ServerConfiguration config = server.config();
        final Map<String, Boolean> features = new LinkedHashMap<>(5);
        features.put("whitelist", server.whitelist().enabled());
        features.put("secure_chat", config.network().enforceSecureChat());
        features.put("code_of_conduct", config.codeOfConduct().enabled());
        features.put("spark", config.spark().enabled());
        features.put("resource_pack", config.resourcePack() != null);
        return features;
    }

    private static @Nullable Long maxMemoryMib() {
        final long max = Runtime.getRuntime().maxMemory();
        return max == Long.MAX_VALUE ? null : max / MIB;
    }
}
