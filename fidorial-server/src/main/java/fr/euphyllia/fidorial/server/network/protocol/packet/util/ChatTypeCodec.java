package fr.euphyllia.fidorial.server.network.protocol.packet.util;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public final class ChatTypeCodec {

    public static void writeChatType(final CodecAwarePacketBuffer buf, final ChatType.Bound chatType, final ServerPlayer player) {
        final Key chatTypeKey = chatType.type().key();
        Preconditions.checkState(chatTypeKey != null, "Inline chat types are not supported");
        final int networkId = buf.registries().registry(RegistryKey.CHAT_TYPE).networkId(TypedKey.withRegistry(RegistryKey.CHAT_TYPE, chatTypeKey));
        buf.writeVarInt(networkId + 1);
        buf.writeComponent(player.prepareMessageForSend(chatType.name()));
        final Component target = chatType.target();
        buf.writeBoolean(target != null);
        if (target != null) {
            buf.writeComponent(player.prepareMessageForSend(target));
        }
    }
}
