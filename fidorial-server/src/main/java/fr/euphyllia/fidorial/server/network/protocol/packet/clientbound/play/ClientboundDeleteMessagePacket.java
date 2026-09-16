package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Delete_Message
public record ClientboundDeleteMessagePacket(int cacheId, byte @Nullable [] signature) implements ClientboundPacket {

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
    public void write(final PacketBuffer buf) {
        if (signature != null) {
            buf.writeVarInt(0);
            buf.writeRawBytes(signature);
        } else {
            buf.writeVarInt(cacheId + 1);
        }
    }
}
