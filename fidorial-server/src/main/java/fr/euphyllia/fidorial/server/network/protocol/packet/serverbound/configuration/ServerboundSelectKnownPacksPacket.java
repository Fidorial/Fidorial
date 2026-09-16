package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;


public record ServerboundSelectKnownPacksPacket(String namespace, String id, String version) implements ServerboundPacket {

    private static final int MAX_LENGTH = 32767;

    public static ServerboundSelectKnownPacksPacket read(final PacketBuffer buf) {
        buf.readVarInt(); // prefixed array so we need to read the length
        final String namespace = buf.readString(MAX_LENGTH);
        final String id = buf.readString(MAX_LENGTH);
        final String version = buf.readString(MAX_LENGTH);
        return new ServerboundSelectKnownPacksPacket(namespace, id, version);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((ConfigurationPacketListener) listener).handleSelectKnownPacks(this);
    }
}
