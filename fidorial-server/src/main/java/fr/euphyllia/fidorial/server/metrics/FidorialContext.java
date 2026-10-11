package fr.euphyllia.fidorial.server.metrics;

import dev.faststats.Metrics;
import dev.faststats.SimpleContext;
import dev.faststats.SimpleMetrics;
import dev.faststats.Token;
import dev.faststats.config.SimpleConfig;
import dev.faststats.internal.Logger;
import dev.faststats.internal.LoggerFactory;
import dev.faststats.internal.PlatformLoggerFactory;
import fr.euphyllia.fidorial.server.FidorialServer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class FidorialContext extends SimpleContext {

    private static final ComponentLogger LOGGER = ComponentLogger.logger("FastStats");
    public static final Path CONFIG_FILE = Path.of("faststats").resolve("config.properties");

    private final FidorialServer server;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            task -> Thread.ofPlatform().name("fidorial-faststats").daemon(true).unstarted(task));

    private FidorialContext(
            final Factory factory,
            final LoggerFactory loggerFactory,
            final FidorialServer server,
            @Token final String token
    ) {
        super(factory, loggerFactory, SimpleConfig.read(CONFIG_FILE, loggerFactory), "fidorial", token);
        this.server = server;
        initializeServices(factory);
    }

    @Override
    @Contract(value = " -> new", pure = true)
    protected Metrics.Factory metricsFactory() {
        return new SimpleMetrics.Factory(this) {
            @Override
            public Metrics create() throws IllegalStateException {
                return new FidorialMetricsImpl(this, server);
            }
        };
    }

    @Override
    protected boolean preSubmissionStart() {
        return ((SimpleConfig) getConfig()).preSubmissionStart(this);
    }

    @Override
    public String getProjectName() {
        return "Fidorial";
    }

    @Override
    protected void scheduleAtFixedRate(
            final Runnable task,
            final long initialDelay,
            final long period,
            final TimeUnit unit
    ) {
        scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    @Override
    public void shutdown() {
        scheduler.shutdown();
        super.shutdown();
    }

    public static final class Factory extends SimpleContext.Factory<FidorialContext, Factory> {
        private final FidorialServer server;
        private final @Token String token;

        public Factory(final FidorialServer server, @Token final String token) {
            this.server = server;
            this.token = token;
        }

        @Override
        public FidorialContext create() {
            final var loggerFactory = new PlatformLoggerFactory(Factory::log);
            return new FidorialContext(this, loggerFactory, server, token);
        }

        private static void log(final Logger.LogLevel level, final @Nullable Throwable throwable, final String message) {
            switch (level) {
                case INFO -> {
                    if (throwable == null) LOGGER.info(message);
                    else LOGGER.info(message, throwable);
                }
                case WARN -> {
                    if (throwable == null) LOGGER.warn(message);
                    else LOGGER.warn(message, throwable);
                }
                case ERROR -> {
                    if (throwable == null) LOGGER.error(message);
                    else LOGGER.error(message, throwable);
                }
            }
        }
    }
}
