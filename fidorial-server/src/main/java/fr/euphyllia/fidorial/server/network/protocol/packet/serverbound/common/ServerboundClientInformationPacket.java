package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundClientInformationPacket(String language, int viewDistance, int displayedSkinParts) implements ServerboundPacket {

    public static final NetworkDecoder<PacketBuffer, ServerboundClientInformationPacket> DECODER = NetworkDecoder.composite(
            StringLikeCodecs.string(16),
            PrimitiveCodecs.BYTE,
            PrimitiveCodecs.VAR_INT, // chat mode
            PrimitiveCodecs.BOOL, // chat colors
            PrimitiveCodecs.UNSIGNED_BYTE,
            PrimitiveCodecs.VAR_INT, // main hand
            PrimitiveCodecs.BOOL, // text filtering
            PrimitiveCodecs.BOOL, // server listings
            PrimitiveCodecs.VAR_INT, // particle status
            (language, viewDistance, _, _, skinParts, _, _, _, _) ->
                    new ServerboundClientInformationPacket(language, viewDistance, skinParts));

    @Override
    public void handle(final PacketListener listener) {
        if (listener instanceof final PlayPacketListener play) {
            play.handleClientInformation(this);
        } else if (listener instanceof final ConfigurationPacketListener config) {
            config.handleClientInformation(this);
        }
    }
}
