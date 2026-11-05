package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.StringLikeCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;

public record ServerboundChatPacket(
        String message,
        long timestamp,
        long salt,
        byte @Nullable [] signature,
        int messageCount,
        BitSet acknowledged,
        byte checksum)
        implements ServerboundPacket {

    private static final int MAX_LENGTH = 256;

    private static final NetworkDecoder<PacketBuffer, byte @Nullable []> SIGNATURE = buf -> buf.readOptionalFixedByteArray(MAX_LENGTH);
    private static final NetworkDecoder<PacketBuffer, BitSet> ACKNOWLEDGED = buf -> buf.readFixedBitSet(20);

    public static final NetworkDecoder<PacketBuffer, ServerboundChatPacket> DECODER = NetworkDecoder.composite(
            StringLikeCodecs.string(MAX_LENGTH),
            PrimitiveCodecs.LONG,
            PrimitiveCodecs.LONG,
            SIGNATURE,
            PrimitiveCodecs.VAR_INT,
            ACKNOWLEDGED,
            PrimitiveCodecs.BYTE,
            ServerboundChatPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleChat(this);
    }
}
