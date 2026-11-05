package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundCommandSuggestionPacket(int id, String text) implements ServerboundPacket {

    private static final int MAX_LENGTH = 32500;

    public static final NetworkDecoder<PacketBuffer, ServerboundCommandSuggestionPacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.VAR_INT,
            StringLikeCodecs.string(MAX_LENGTH),
            ServerboundCommandSuggestionPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleCommandSuggestion(this);
    }
}
