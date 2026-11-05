package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.status;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundStatusResponsePacket(String json) implements ClientboundPacket<ClientboundStatusResponsePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundStatusResponsePacket> CODEC =
            NetworkEncoder.field(StringLikeCodecs.STRING, ClientboundStatusResponsePacket::json);

    @Override
    public Key name() {
        return StatusClientboundPackets.STATUS_RESPONSE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundStatusResponsePacket> codec() {
        return CODEC;
    }
}
