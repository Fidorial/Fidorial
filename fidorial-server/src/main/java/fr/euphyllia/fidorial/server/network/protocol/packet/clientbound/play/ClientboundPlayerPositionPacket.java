package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundPlayerPositionPacket(int teleportId, PositionData.PositionMoveRotationData moveData)
        implements ClientboundPacket<ClientboundPlayerPositionPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerPositionPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundPlayerPositionPacket::teleportId)
            .then(PositionData.PositionMoveRotationData.CODEC, ClientboundPlayerPositionPacket::moveData)
            .thenConstant(PrimitiveCodecs.INT, 0); // flags (tout absolu)

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_POSITION;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerPositionPacket> codec() {
        return CODEC;
    }
}
