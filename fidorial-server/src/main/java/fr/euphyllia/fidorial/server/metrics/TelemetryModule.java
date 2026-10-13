package fr.euphyllia.fidorial.server.metrics;

import dev.faststats.ErrorTracker;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.PlayerList;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.network.ClientConnection;

/**
 * Anonymous usage statistics and error reporting (FastStats).
 *
 * <p>Added first, so that it is closed last and still reports errors raised while the other
 * modules shut down.</p>
 */
public final class TelemetryModule implements ServerModule {

    private static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();
    private static final String FASTSTATS_TOKEN = "6c8c21fe427163e998ea50f54a0ce855";

    private final FidorialServer server;
    private final FidorialMetrics collector;
    private final FidorialContext context;

    public TelemetryModule(final FidorialServer server) {
        this.server = server;
        this.collector = new FidorialMetrics(server);
        this.context = new FidorialContext.Factory(server, FASTSTATS_TOKEN)
                .errorTrackerService(ERROR_TRACKER)
                .metrics(collector::create)
                .create();
    }

    @Override
    public void load() {
        context.ready();
    }

    @Override
    public void start() {
        if (context.metrics().isPresent()) {
            server.workers().regionizer().addTickProfiler(collector.tickProfiler());
        }
        final PlayerList online = server.players().connections();
        online.addListener(new PlayerList.Listener() {
            @Override
            public void onConnect(final ClientConnection connection) {
                collector.recordPlayerCount(online.connectionCount());
            }
        });
    }

    @Override
    public void onStarted() {
        collector.onServerStarted();
    }

    @Override
    public void close() {
        context.shutdown();
    }

    public FidorialMetrics metrics() {
        return collector;
    }
}
