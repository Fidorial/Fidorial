package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundAddEntityMotionPacket(int entityId, PositionData.VelocityVec3D velocity)
        implements ClientboundPacket<ClientboundAddEntityMotionPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundAddEntityMotionPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundAddEntityMotionPacket::entityId)
            .then(PositionData.VelocityVec3D.CODEC, ClientboundAddEntityMotionPacket::velocity);

    @Override
    public Key name() {
        return PlayClientboundPackets.ADD_ENTITY_MOTION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundAddEntityMotionPacket> codec() {
        return CODEC;
    }
}
