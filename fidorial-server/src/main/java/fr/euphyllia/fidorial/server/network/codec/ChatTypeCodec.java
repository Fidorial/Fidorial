package fr.euphyllia.fidorial.server.network.codec;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;

public final class ChatTypeCodec {

    public static void writeChatType(final PacketBuffer buf, final ChatType.Bound chatType, final ServerPlayer player) {
        final Key chatTypeKey = chatType.type().key();
        Preconditions.checkState(chatTypeKey != null, "Inline chat types are not supported");
        final int networkId = FidorialServer.getInstance().chatTypes().networkId(chatTypeKey);
        buf.writeVarInt(networkId + 1);
        buf.writeComponent(player.prepareMessageForSend(chatType.name()));
        buf.writeBoolean(chatType.target() != null);
        if (chatType.target() != null) {
            buf.writeComponent(player.prepareMessageForSend(chatType.target()));
        }
    }
}
