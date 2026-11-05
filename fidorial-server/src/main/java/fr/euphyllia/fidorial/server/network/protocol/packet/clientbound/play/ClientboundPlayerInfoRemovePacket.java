package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.UUID;

public record ClientboundPlayerInfoRemovePacket(UUID uuid) implements ClientboundPacket<ClientboundPlayerInfoRemovePacket> {
    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerInfoRemovePacket> CODEC = NetworkEncoder
            .constant(PrimitiveCodecs.VAR_INT, 1)
            .then(PrimitiveCodecs.UUID, ClientboundPlayerInfoRemovePacket::uuid);

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_INFO_REMOVE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundPlayerInfoRemovePacket> codec() {
        return CODEC;
    }
}
