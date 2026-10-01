/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.euphyllia.fidorial.server.spark;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.command.spark.SparkCommand;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.fidorial.command.CommandSender;
import fr.fidorial.plugin.PluginMeta;
import me.lucko.spark.api.Spark;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import me.lucko.spark.common.monitor.tick.TickStatistics;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import me.lucko.spark.common.sampler.source.SourceMetadata;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.common.util.SparkThreadFactory;
import me.lucko.spark.common.util.classfinder.ClassFinder;
import me.lucko.spark.common.util.classfinder.FallbackClassFinder;
import me.lucko.spark.common.util.classfinder.InstrumentationClassFinder;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class SparkService implements SparkPlugin {

    /**
     * Must match the region ticking thread names used by {@code ThreadedRegionRegionizer}.
     */
    private static final String REGION_THREAD_REGEX = "^fidorial-region-worker-\\d+$";

    private static final String COMMAND_NAME = "spark";
    private static final Set<String> COMMAND_ALIASES = Set.of();
    private static final long SCHEDULER_TERMINATION_SECONDS = 5L;
    private static final String VERSION_RESOURCE = "/spark-internal.properties";
    private static final String UNKNOWN_VERSION = "unknown";

    private static final ComponentLogger LOGGER = ComponentLogger.logger("spark");

    private final FidorialServer server;
    private final Path dataDirectory;
    private final String version = readVersion();

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2, new SparkThreadFactory("spark-fidorial", true));
    private final SparkTickBridge tickBridge = new SparkTickBridge();

    private volatile @Nullable SparkPlatform platform;

    public SparkService(final FidorialServer server, final Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    private static String readVersion() {
        try (final InputStream in = SparkService.class.getResourceAsStream(VERSION_RESOURCE)) {
            if (in == null) {
                return UNKNOWN_VERSION;
            }
            final Properties properties = new Properties();
            properties.load(in);
            return properties.getProperty("spark-version", UNKNOWN_VERSION);
        } catch (final IOException e) {
            return UNKNOWN_VERSION;
        }
    }

    /**
     * Boots the profiler and registers {@code /spark} as a built-in command.
     *
     * <p>Called by {@link FidorialServer#start()} before plugins are loaded, so that plugin loading
     * itself can be profiled.</p>
     */
    public void enable() {
        if (this.platform != null) {
            return;
        }

        try {
            Files.createDirectories(this.dataDirectory);
        } catch (final IOException e) {
            LOGGER.error("Could not create the spark data directory at {}", this.dataDirectory, e);
            return;
        }

        this.server.regionizer().setTickProfiler(this.tickBridge);
        this.server.dayNightEngine().setTickProfiler(this.tickBridge);

        final SparkPlatform platform = new SparkPlatform(this);
        platform.enable();
        this.platform = platform;

        try {
            this.server
                    .commandManager()
                    .registerInternal(
                            SparkCommand.create(platform, COMMAND_NAME, this.scheduler), COMMAND_ALIASES);
        } catch (final Throwable t) {
            detachTickProfiler();
            shutdownPlatform();
            throw t;
        }

        warnIfServerConfigUnencodable();

        LOGGER.info("spark {} enabled - use /{} to profile the server", this.version, COMMAND_NAME);
    }

    /**
     * Tears the profiler down. Safe to call more than once, and safe to call if {@link #enable()}
     * never succeeded.
     */
    public void disable() {
        if (this.platform != null) {
            try {
                this.server.commandManager().unregister("minecraft", COMMAND_NAME);
            } catch (final Throwable t) {
                log(Level.WARNING, "Could not unregister the spark command", t);
            }
        }

        detachTickProfiler();
        shutdownPlatform();
        shutdownScheduler();
    }

    private void warnIfServerConfigUnencodable() {
        final DataResult<JsonElement> result = ServerConfiguration.CODEC.encodeStart(JsonOps.INSTANCE, this.server.config());
        if (result instanceof final DataResult.Error<JsonElement> error) {
            log(Level.WARNING, "spark will not be able to report the server configuration: " + error.message());
        }
    }

    private void detachTickProfiler() {
        try {
            this.server.regionizer().setTickProfiler(null);
            this.server.dayNightEngine().setTickProfiler(null);
        } catch (final Throwable t) {
            log(Level.WARNING, "Could not detach the spark tick profiler", t);
        }
        this.tickBridge.clear();
    }

    private void shutdownPlatform() {
        final SparkPlatform platform = this.platform;
        if (platform == null) {
            return;
        }
        this.platform = null;
        try {
            platform.disable();
        } catch (final Throwable t) {
            log(Level.SEVERE, "Error while disabling the spark platform", t);
        }
        try {
            this.server.services().unregisterAll(this);
        } catch (final Throwable t) {
            log(Level.WARNING, "Could not unregister the spark API service", t);
        }
    }

    private void shutdownScheduler() {
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(SCHEDULER_TERMINATION_SECONDS, TimeUnit.SECONDS)) {
                this.scheduler.shutdownNow();
            }
        } catch (final InterruptedException e) {
            this.scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public Path getPluginDirectory() {
        return this.dataDirectory;
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public Stream<SparkCommandSender> getCommandSenders() {
        final Stream<? extends CommandSender> players = this.server.onlinePlayers().stream();
        return Stream.concat(players, Stream.of(this.server.getConsole())).map(SparkCommandSender::new);
    }

    @Override
    public void executeAsync(final Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void executeSync(final Runnable task) {
        this.tickBridge.executeSync(task);
    }

    @Override
    public void log(final Level level, final String msg) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            LOGGER.error(msg);
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            LOGGER.warn(msg);
        } else {
            LOGGER.info(msg);
        }
    }

    @Override
    public void log(final Level level, final String msg, final Throwable throwable) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            LOGGER.error(msg, throwable);
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            LOGGER.warn(msg, throwable);
        } else {
            LOGGER.info(msg, throwable);
        }
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return new ThreadDumper.Regex(Set.of(REGION_THREAD_REGEX));
    }

    @Override
    public TickHook createTickHook() {
        return this.tickBridge.tickHook();
    }

    @Override
    public TickReporter createTickReporter() {
        return this.tickBridge.tickReporter();
    }

    @Override
    public TickStatistics createTickStatistics() {
        return new SparkTickStatistics(this.server);
    }

    @Override
    public ClassSourceLookup createClassSourceLookup() {
        return new SparkClassSourceLookup(this.server.plugins());
    }

    @Override
    public ClassFinder createClassFinder() {
        return ClassFinder.combining(
                new InstrumentationClassFinder(this),
                new SparkClassFinder(this.server.plugins()),
                FallbackClassFinder.INSTANCE);
    }

    @Override
    public PlayerPingProvider createPlayerPingProvider() {
        return new SparkPlayerPingProvider(this.server);
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new SparkWorldInfoProvider(this.server);
    }

    @Override
    public ServerConfigProvider createServerConfigProvider() {
        return new SparkServerConfigProvider(this.server);
    }

    @Override
    public Collection<SourceMetadata> getKnownSources() {
        return SourceMetadata.gather(
                this.server.plugins().loaded(),
                PluginMeta::name,
                PluginMeta::version,
                meta -> String.join(", ", meta.authors()),
                meta -> null // Fidorial does not store a plugin description
        );
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new SparkPlatformInfo(this.server);
    }

    @Override
    public void registerApi(final Spark api) {
        this.server.services().register(Spark.class, api, this);
    }
}
