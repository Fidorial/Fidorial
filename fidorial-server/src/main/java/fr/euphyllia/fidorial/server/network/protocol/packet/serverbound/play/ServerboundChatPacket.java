package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;

public record ServerboundChatPacket(
        Component message,
        long timestamp,
        long salt,
        byte @Nullable [] signature,
        int messageCount,
        BitSet acknowledged,
        byte checksum)
        implements ServerboundPacket {

    private static final int MAX_LENGTH = 256;

    public static ServerboundChatPacket read(final PacketBuffer buf) {
        final String rawMessage = buf.readString(MAX_LENGTH);
        final Component message = Component.text(rawMessage);
        final long timestamp = buf.readLong();
        final long salt = buf.readLong();
        final byte[] sig = buf.readOptionalFixedByteArray(MAX_LENGTH);
        final int count = buf.readVarInt();
        final BitSet acknowledged = buf.readFixedBitSet(20);
        final byte checksum = buf.readByte();
        return new ServerboundChatPacket(message, timestamp, salt, sig, count, acknowledged, checksum);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleChat(this);
    }
}
