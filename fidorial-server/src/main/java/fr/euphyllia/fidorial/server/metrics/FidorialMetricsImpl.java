package fr.euphyllia.fidorial.server.metrics;

import com.google.gson.JsonObject;
import dev.faststats.SimpleMetrics;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;

import static fr.euphyllia.fidorial.server.VersionConstants.FIDORIAL_VERSION;
import static fr.euphyllia.fidorial.server.VersionConstants.MINECRAFT_VERSION_ID;

public final class FidorialMetricsImpl extends SimpleMetrics {

    private static final String SERVER_TYPE = "Fidorial";
    private final FidorialServer server;

    FidorialMetricsImpl(final Factory factory, final FidorialServer server) throws IllegalStateException {
        super(factory);
        this.server = server;
    }

    @Override
    protected void appendDefaultData(final JsonObject metrics) {
        metrics.addProperty("game_version", MINECRAFT_VERSION_ID);
        metrics.addProperty("online_mode", isOnlineMode());
        metrics.addProperty("platform_version", FIDORIAL_VERSION);
        metrics.addProperty("player_count", server.players().count());
        metrics.addProperty("plugin_version", FIDORIAL_VERSION);
        metrics.addProperty("server_type", SERVER_TYPE);
    }

    private boolean isOnlineMode() {
        final ServerConfiguration.Network network = server.config().network();
        return network.onlineMode() || network.proxy() instanceof ServerConfiguration.ProxyForwarding.Velocity;
    }
}
