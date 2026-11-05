package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.ResourcePackStatusCodec;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import net.kyori.adventure.resource.ResourcePackStatus;

import java.util.UUID;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Resource_Pack_Response
public record ServerboundResourcePackPacket(UUID id, ResourcePackStatus status) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundResourcePackPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.UUID,
            PrimitiveCodecs.VAR_INT.map(ResourcePackStatusCodec::fromWireId),
            ServerboundResourcePackPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        if (listener instanceof final PlayPacketListener play) {
            play.handleResourcePackResponse(this);
        } else if (listener instanceof final ConfigurationPacketListener config) {
            config.handleResourcePackResponse(this);
        }
    }
}
