package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundLoginAcknowledgedPacket() implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundLoginAcknowledgedPacket> DECODER = _ -> new ServerboundLoginAcknowledgedPacket();

    @Override
    public void handle(final PacketListener listener) {
        ((LoginPacketListener) listener).handleLoginAcknowledged(this);
    }
}
