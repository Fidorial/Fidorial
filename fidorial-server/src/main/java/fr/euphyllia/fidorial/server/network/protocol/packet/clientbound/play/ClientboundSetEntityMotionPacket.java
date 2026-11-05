package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Entity_Velocity
 */
public record ClientboundSetEntityMotionPacket(int entityId, PositionData.VelocityVec3D velocity)
        implements ClientboundPacket<ClientboundSetEntityMotionPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetEntityMotionPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundSetEntityMotionPacket::entityId)
            .then(PositionData.VelocityVec3D.CODEC, ClientboundSetEntityMotionPacket::velocity);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_ENTITY_MOTION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetEntityMotionPacket> codec() {
        return CODEC;
    }
}
