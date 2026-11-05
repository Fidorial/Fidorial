package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

public record ServerboundChatCommandPacket(String command) implements ServerboundPacket {

    private static final int MAX_LENGTH = 32767;

    public static final NetworkDecoder<PacketBuffer, ServerboundChatCommandPacket> DECODER = NetworkDecoder.composite(
            StringLikeCodecs.string(MAX_LENGTH),
            ServerboundChatCommandPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleChatCommand(this);
    }
}
