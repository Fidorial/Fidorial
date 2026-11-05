package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
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

    public static final NetworkDecoder<PacketBuffer, ServerboundChatSessionUpdatePacket> DECODER = NetworkDecoder.composite(
            PrimitiveCodecs.UUID,
            PrimitiveCodecs.LONG,
            PrimitiveCodecs.byteArray(MAX_PUBLIC_KEY_LENGTH),
            PrimitiveCodecs.byteArray(MAX_SIGNATURE_LENGTH),
            ServerboundChatSessionUpdatePacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((PlayPacketListener) listener).handleChatSessionUpdate(this);
    }
}
