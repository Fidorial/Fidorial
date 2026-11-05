package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundCustomQueryPacket(int transactionId, Key channel, byte[] payload)
        implements ClientboundPacket<ClientboundCustomQueryPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundCustomQueryPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundCustomQueryPacket::transactionId)
            .then(StringLikeCodecs.KEY, ClientboundCustomQueryPacket::channel)
            .then(PrimitiveCodecs.REMAINING_BYTES, ClientboundCustomQueryPacket::payload);

    @Override
    public Key name() {
        return LoginClientboundPackets.CUSTOM_QUERY;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundCustomQueryPacket> codec() {
        return CODEC;
    }
}
