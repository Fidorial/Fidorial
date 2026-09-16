package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

import java.util.UUID;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Session
public record ServerboundChatSessionUpdatePacket(
        UUID sessionId,
        long publicKeyExpiresAt,
        byte[] publicKey,
        byte[] keySignature
) implements ServerboundPacket {

    private static final int MAX_PUBLIC_KEY_LENGTH = 512;
    private static final int MAX_SIGNATURE_LENGTH = 4096;

    public static ServerboundChatSessionUpdatePacket read(final PacketBuffer buf) {
        final UUID sessionId = buf.readUuid();
        final long expiresAt = buf.readLong();
        final byte[] publicKey = buf.readByteArray(MAX_PUBLIC_KEY_LENGTH);
        final byte[] signature = buf.readByteArray(MAX_SIGNATURE_LENGTH);
        return new ServerboundChatSessionUpdatePacket(sessionId, expiresAt, publicKey, signature);
    }

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleChatSessionUpdate(this);
    }
}
