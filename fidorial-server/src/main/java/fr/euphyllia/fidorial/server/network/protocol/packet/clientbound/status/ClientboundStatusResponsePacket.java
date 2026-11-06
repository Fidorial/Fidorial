package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.status;

import fr.euphyllia.fidorial.server.codecs.api.status.ServerStatusCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.StatusClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.status.ServerStatus;
import net.kyori.adventure.key.Key;

public record ClientboundStatusResponsePacket(ServerStatus status) implements ClientboundPacket<ClientboundStatusResponsePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundStatusResponsePacket> CODEC = NetworkEncoder.field(
            StringLikeCodecs.json(StringLikeCodecs.MAX_STRING_LENGTH, ServerStatusCodecs.SERVER_STATUS_CODEC),
            ClientboundStatusResponsePacket::status);

    @Override
    public Key name() {
        return StatusClientboundPackets.STATUS_RESPONSE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundStatusResponsePacket> codec() {
        return CODEC;
    }
}
