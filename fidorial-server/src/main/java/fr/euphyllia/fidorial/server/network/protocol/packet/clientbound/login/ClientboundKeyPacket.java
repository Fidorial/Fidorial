package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundKeyPacket(String serverId, byte[] publicKey, byte[] challenge,
                                   boolean shouldAuthenticate) implements ClientboundPacket<ClientboundKeyPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundKeyPacket> CODEC = NetworkEncoder
            .field(StringLikeCodecs.STRING, ClientboundKeyPacket::serverId)
            .then(PrimitiveCodecs.BYTE_ARRAY, ClientboundKeyPacket::publicKey)
            .then(PrimitiveCodecs.BYTE_ARRAY, ClientboundKeyPacket::challenge)
            .then(PrimitiveCodecs.BOOL, ClientboundKeyPacket::shouldAuthenticate);

    @Override
    public Key name() {
        return LoginClientboundPackets.KEY;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundKeyPacket> codec() {
        return CODEC;
    }
}
