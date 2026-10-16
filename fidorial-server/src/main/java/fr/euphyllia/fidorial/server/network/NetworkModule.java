package fr.euphyllia.fidorial.server.network;

import fr.euphyllia.fidorial.auth.EncryptionUtils;
import fr.euphyllia.fidorial.auth.MojangChatSigningKeys;
import fr.euphyllia.fidorial.auth.MojangSessionService;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.network.protocol.ProtocolMap;
import fr.euphyllia.fidorial.server.registry.RegistryModule;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.security.KeyPair;

/**
 * The network listener, the protocol and the authentication of players.
 *
 * <p>Started after the worlds, so that players can only connect once there is somewhere to
 * spawn.</p>
 */
public final class NetworkModule implements ServerModule {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(NetworkModule.class);

    private final ServerConfiguration config;
    private final boolean headless;
    private final RegistryModule registries;
    private final ProtocolMap protocolMap = ProtocolMap.load();
    private final KeyPair keyPair = EncryptionUtils.generateServerKeyPair();
    private final MojangSessionService sessionService = new MojangSessionService();
    private final MojangChatSigningKeys chatSigningKeys = new MojangChatSigningKeys();
    private final NettyServer listener;

    /**
     * @param headless whether the port must not be bound
     */
    public NetworkModule(
            final FidorialServer server, final ServerConfiguration config, final boolean headless,
            final RegistryModule registries) {
        this.config = config;
        this.headless = headless;
        this.registries = registries;
        this.listener = new NettyServer(server, config.network().port());
    }

    @Override
    public void start() throws Exception {
        // From now on players may be connected: registry changes need to resync them.
        registries.setStarted(true);
        if (!headless) {
            listener.bind();
            LOGGER.info("Listening on port {}", config.network().port());
        }
    }

    @Override
    public void stop() {
        listener.shutdown();
    }

    public NettyServer listener() {
        return listener;
    }

    public ProtocolMap protocolMap() {
        return protocolMap;
    }

    public KeyPair keyPair() {
        return keyPair;
    }

    public MojangSessionService sessionService() {
        return sessionService;
    }

    public MojangChatSigningKeys chatSigningKeys() {
        return chatSigningKeys;
    }
}
