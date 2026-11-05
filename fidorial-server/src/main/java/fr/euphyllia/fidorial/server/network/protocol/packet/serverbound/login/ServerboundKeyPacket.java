package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundKeyPacket(byte[] encryptedSecret, byte[] encryptedToken) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundKeyPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.byteArray(256),
            PrimitiveCodecs.byteArray(256),
            ServerboundKeyPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((LoginPacketListener) listener).handleKey(this);
    }
}
