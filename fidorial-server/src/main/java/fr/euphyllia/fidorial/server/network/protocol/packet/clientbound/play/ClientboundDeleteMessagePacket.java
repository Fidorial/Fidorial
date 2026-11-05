package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Delete_Message
public record ClientboundDeleteMessagePacket(int cacheId, byte @Nullable [] signature) implements ClientboundPacket<ClientboundDeleteMessagePacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundDeleteMessagePacket> CODEC = NetworkEncoder.member(ClientboundDeleteMessagePacket::encode);

    public static ClientboundDeleteMessagePacket cached(final int cacheId) {
        return new ClientboundDeleteMessagePacket(cacheId, null);
    }

    public static ClientboundDeleteMessagePacket full(final byte[] signature) {
        return new ClientboundDeleteMessagePacket(-1, signature);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.DELETE_CHAT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundDeleteMessagePacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        if (signature != null) {
            buf.writeVarInt(0);
            buf.writeRawBytes(signature);
        } else {
            buf.writeVarInt(cacheId + 1);
        }
    }
}
