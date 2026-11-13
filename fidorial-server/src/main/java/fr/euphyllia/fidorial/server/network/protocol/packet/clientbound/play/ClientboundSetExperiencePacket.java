package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

/**
 * The experience bar of the player: how full it is, the level above it, and the total of the
 * experience points gained.
 */
public record ClientboundSetExperiencePacket(float progress, int level, int total)
        implements ClientboundPacket<ClientboundSetExperiencePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetExperiencePacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.FLOAT, ClientboundSetExperiencePacket::progress)
            .then(PrimitiveCodecs.VAR_INT, ClientboundSetExperiencePacket::level)
            .then(PrimitiveCodecs.VAR_INT, ClientboundSetExperiencePacket::total);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_EXPERIENCE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetExperiencePacket> codec() {
        return CODEC;
    }
}
