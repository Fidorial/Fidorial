package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Update_Entity_Position_and_Rotation
public record ClientboundMoveEntityPosRotPacket(
        int entityId, PositionData.DeltaVec3D delta, PositionData.AngleRotation rotation, boolean onGround)
        implements ClientboundPacket<ClientboundMoveEntityPosRotPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundMoveEntityPosRotPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundMoveEntityPosRotPacket::entityId)
            .then(PrimitiveCodecs.BOOL, ClientboundMoveEntityPosRotPacket::onGround)
            .then(PositionData.DeltaVec3D.CODEC, ClientboundMoveEntityPosRotPacket::delta)
            .then(PositionData.AngleRotation.CODEC, ClientboundMoveEntityPosRotPacket::rotation);

    @Override
    public Key name() {
        return PlayClientboundPackets.MOVE_ENTITY_POS_ROT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundMoveEntityPosRotPacket> codec() {
        return CODEC;
    }
}
