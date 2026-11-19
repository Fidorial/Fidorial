package fr.euphyllia.fidorial.server.network.connection.listener;

import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.connection.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.HandshakePacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.handshake.ServerboundIntentionPacket;
import net.kyori.adventure.text.Component;

public final class HandshakePacketHandler implements HandshakePacketListener {

    private static final int INTENT_STATUS = 1;
    private static final int INTENT_LOGIN = 2;
    private static final int INTENT_TRANSFER = 3;

    private final ClientConnection connection;

    public HandshakePacketHandler(final ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleIntention(final ServerboundIntentionPacket packet) {
        connection.setClientProtocol(packet.protocolVersion());
        switch (packet.nextState()) {
            case INTENT_STATUS -> connection.setState(ConnectionState.STATUS);
            case INTENT_LOGIN -> connection.setState(ConnectionState.LOGIN);
            case INTENT_TRANSFER -> beginTransferredLogin();
            default -> connection.close();
        }
    }

    /**
     * A client sent here by another server, with a transfer packet. It logs in like any other client when
     * the server accepts transfers, and is turned away during the login otherwise.
     */
    private void beginTransferredLogin() {
        connection.setTransferred(true);
        connection.setState(ConnectionState.LOGIN);
        if (!connection.server().config().network().acceptsTransfers()) {
            connection.disconnect(Component.translatable("multiplayer.disconnect.transfers_disabled"));
            // The client sends its hello right behind the handshake: ignore it while the channel closes.
            connection.setRejectedState(ConnectionState.LOGIN);
        }
    }
}
