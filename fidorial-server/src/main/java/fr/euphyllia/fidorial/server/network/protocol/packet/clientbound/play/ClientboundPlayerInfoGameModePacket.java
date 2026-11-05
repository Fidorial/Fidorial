package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.UUID;

public record ClientboundPlayerInfoGameModePacket(UUID uuid, int gameMode)
        implements ClientboundPacket<ClientboundPlayerInfoGameModePacket> {

    private static final int ACTIONS = 0x04;

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerInfoGameModePacket> CODEC = NetworkEncoder
            .constant(PrimitiveCodecs.UNSIGNED_BYTE, ACTIONS)
            .thenConstant(PrimitiveCodecs.VAR_INT, 1)
            .then(PrimitiveCodecs.UUID, ClientboundPlayerInfoGameModePacket::uuid)
            .then(PrimitiveCodecs.VAR_INT, ClientboundPlayerInfoGameModePacket::gameMode);

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_INFO_UPDATE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerInfoGameModePacket> codec() {
        return CODEC;
    }
}
