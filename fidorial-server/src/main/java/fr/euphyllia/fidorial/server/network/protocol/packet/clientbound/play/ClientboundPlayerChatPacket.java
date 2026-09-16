package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.chat.FilterType;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static fr.euphyllia.fidorial.server.network.codec.ChatTypeCodec.writeChatType;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Player_Chat_Message
public record ClientboundPlayerChatPacket(
        int globalIndex,
        UUID sender,
        int index,
        byte @Nullable [] signature,
        String content,
        Instant timestamp,
        long salt,
        List<PackedSignature> lastSeen,
        @Nullable Component unsignedContent,
        FilterType filterType,
        ChatType.Bound chatType,
        ServerPlayer player
) implements ClientboundPacket {

    public record PackedSignature(int cacheId, byte @Nullable [] signature) {
        public static PackedSignature cached(final int cacheId) {
            return new PackedSignature(cacheId, null);
        }

        public static PackedSignature full(final byte[] signature) {
            return new PackedSignature(-1, signature);
        }
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_CHAT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(globalIndex);
        buf.writeUuid(sender);
        buf.writeVarInt(index);

        buf.writeBoolean(signature != null);
        if (signature != null) {
            buf.writeRawBytes(signature);
        }

        buf.writeString(content);
        buf.writeLong(timestamp.toEpochMilli());
        buf.writeLong(salt);

        buf.writeVarInt(lastSeen.size());
        for (final PackedSignature packed : lastSeen) {
            if (packed.signature() != null) {
                buf.writeVarInt(0);
                buf.writeRawBytes(packed.signature());
            } else {
                buf.writeVarInt(packed.cacheId() + 1);
            }
        }

        buf.writeBoolean(unsignedContent != null);
        if (unsignedContent != null) {
            buf.writeComponent(unsignedContent);
        }

        writeFilterType(buf, filterType);
        writeChatType(buf, chatType, player);
    }

    private static void writeFilterType(final PacketBuffer buf, final FilterType type) {
        switch (type) {
            case FilterType.PassThrough _ -> buf.writeVarInt(0);
            case FilterType.FullyFiltered _ -> buf.writeVarInt(1);
            case FilterType.PartiallyFiltered(final var bits) -> {
                buf.writeVarInt(2);
                buf.writeBitSet(bits);
            }
        }
    }
}
