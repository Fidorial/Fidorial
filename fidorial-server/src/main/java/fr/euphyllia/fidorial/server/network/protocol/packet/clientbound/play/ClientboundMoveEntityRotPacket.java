package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Update_Entity_Rotation
public record ClientboundMoveEntityRotPacket(int entityId, PositionData.AngleRotation rotation, boolean onGround)
        implements ClientboundPacket<ClientboundMoveEntityRotPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundMoveEntityRotPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundMoveEntityRotPacket::entityId)
            .then(PrimitiveCodecs.BOOL, ClientboundMoveEntityRotPacket::onGround)
            .then(PositionData.AngleRotation.CODEC, ClientboundMoveEntityRotPacket::rotation);

    @Override
    public Key name() {
        return PlayClientboundPackets.MOVE_ENTITY_ROT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundMoveEntityRotPacket> codec() {
        return CODEC;
    }
}
