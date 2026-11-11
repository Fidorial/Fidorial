package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Teleport_Entity
public record ClientboundEntityPositionSyncPacket(
        int entityId,
        PositionData.LinearPositionPath position,
        PositionData.FloatRotation rotation,
        float headYaw,
        boolean onGround)
        implements ClientboundPacket<ClientboundEntityPositionSyncPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundEntityPositionSyncPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundEntityPositionSyncPacket::entityId)
            .then(PositionData.LinearPositionPath.CODEC, ClientboundEntityPositionSyncPacket::position)
            .then(PositionData.FloatRotation.CODEC, ClientboundEntityPositionSyncPacket::rotation)
            .then(PrimitiveCodecs.FLOAT, ClientboundEntityPositionSyncPacket::headYaw)
            .then(PrimitiveCodecs.BOOL, ClientboundEntityPositionSyncPacket::onGround);

    @Override
    public Key name() {
        return PlayClientboundPackets.ENTITY_POSITION_SYNC;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundEntityPositionSyncPacket> codec() {
        return CODEC;
    }
}
