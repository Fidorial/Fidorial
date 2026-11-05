package fr.euphyllia.fidorial.server.network.connection.listener;

import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.connection.ConnectionState;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.HandshakePacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.handshake.ServerboundIntentionPacket;

public final class HandshakePacketHandler implements HandshakePacketListener {

    private final ClientConnection connection;

    public HandshakePacketHandler(final ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handleIntention(final ServerboundIntentionPacket packet) {
        connection.setClientProtocol(packet.protocolVersion());
        switch (packet.nextState()) {
            case 1 -> connection.setState(ConnectionState.STATUS);
            case 2 -> connection.setState(ConnectionState.LOGIN);
            default -> connection.close();
        }
    }
}
