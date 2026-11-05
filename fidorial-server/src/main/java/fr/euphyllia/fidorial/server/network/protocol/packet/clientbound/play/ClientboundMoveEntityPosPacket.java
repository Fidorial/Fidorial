package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Update_Entity_Position
public record ClientboundMoveEntityPosPacket(int entityId, PositionData.DeltaVec3D delta, boolean onGround)
        implements ClientboundPacket<ClientboundMoveEntityPosPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundMoveEntityPosPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundMoveEntityPosPacket::entityId)
            .then(PrimitiveCodecs.BOOL, ClientboundMoveEntityPosPacket::onGround)
            .then(PositionData.DeltaVec3D.CODEC, ClientboundMoveEntityPosPacket::delta);

    @Override
    public Key name() {
        return PlayClientboundPackets.MOVE_ENTITY_POS;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundMoveEntityPosPacket> codec() {
        return CODEC;
    }
}
