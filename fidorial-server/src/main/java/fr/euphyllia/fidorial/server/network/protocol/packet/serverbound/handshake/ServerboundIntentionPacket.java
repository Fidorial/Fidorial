package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.handshake;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.HandshakePacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundIntentionPacket(int protocolVersion, String hostname, int port, int nextState)
        implements ServerboundPacket {

    public static ServerboundIntentionPacket read(final PacketBuffer buf) {
        final int protocolVersion = buf.readVarInt();
        final String hostname = buf.readString(1024);
        final int port = buf.readUShort();
        final int nextState = buf.readVarInt();
        return new ServerboundIntentionPacket(protocolVersion, hostname, port, nextState);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((HandshakePacketListener) listener).handleIntention(this);
    }
}
