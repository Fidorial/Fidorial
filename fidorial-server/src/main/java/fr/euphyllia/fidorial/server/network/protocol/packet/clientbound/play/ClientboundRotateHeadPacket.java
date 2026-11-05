package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundRotateHeadPacket(int entityId, float headYaw) implements ClientboundPacket<ClientboundRotateHeadPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundRotateHeadPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundRotateHeadPacket::entityId)
            .then(PrimitiveCodecs.ANGLE, ClientboundRotateHeadPacket::headYaw);

    @Override
    public Key name() {
        return PlayClientboundPackets.ROTATE_HEAD;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundRotateHeadPacket> codec() {
        return CODEC;
    }
}
