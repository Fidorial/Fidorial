package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.login;

import fr.euphyllia.fidorial.auth.GameProfile;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.LoginClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundLoginFinishedPacket(GameProfile profile, byte options) implements ClientboundPacket<ClientboundLoginFinishedPacket> {

    private static final NetworkEncoder<PacketBuffer, GameProfile.Property> PROPERTY = NetworkEncoder
            .field(StringLikeCodecs.STRING, GameProfile.Property::name)
            .then(StringLikeCodecs.STRING, GameProfile.Property::value)
            .then(PrimitiveCodecs.prefixedNullable(StringLikeCodecs.STRING), GameProfile.Property::signature);

    private static final NetworkEncoder<PacketBuffer, GameProfile> PROFILE = NetworkEncoder
            .field(PrimitiveCodecs.UUID, GameProfile::uuid)
            .then(StringLikeCodecs.STRING, GameProfile::name)
            .then(PROPERTY.listOf(PacketBuffer::writeVarInt), GameProfile::properties)
            .then(PrimitiveCodecs.UUID, GameProfile::sessionId);

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundLoginFinishedPacket> CODEC = NetworkEncoder
            .field(PROFILE, ClientboundLoginFinishedPacket::profile)
            .then(PrimitiveCodecs.BYTE, ClientboundLoginFinishedPacket::options);

    @Override
    public Key name() {
        return LoginClientboundPackets.LOGIN_FINISHED;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundLoginFinishedPacket> codec() {
        return CODEC;
    }
}
