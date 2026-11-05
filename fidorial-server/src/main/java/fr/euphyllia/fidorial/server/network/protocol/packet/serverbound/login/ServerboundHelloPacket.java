package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.LoginPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

import java.util.UUID;

public record ServerboundHelloPacket(String username, UUID profileId) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundHelloPacket> DECODER = NetworkDecoder.composite(
            StringLikeCodecs.string(16),
            PrimitiveCodecs.UUID,
            ServerboundHelloPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((LoginPacketListener) listener).handleHello(this);
    }
}
