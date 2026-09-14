package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

public record ClientboundDisguisedChatPacket(Component message, ChatType chatType, Component senderName, @Nullable Component targetName) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.DISGUISED_CHAT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeComponent(message);
        writeChatType(buf, chatType);
        buf.writeComponent(senderName);
        buf.writeBoolean(targetName != null);
        if (targetName != null) {
            buf.writeComponent(targetName);
        }
    }

    private static void writeChatType(final PacketBuffer buf, final ChatType chatType) {
        final Key chatTypeKey = chatType.key();
        Preconditions.checkState(chatTypeKey != null, "Inline chat types are not supported");
        final int networkId = FidorialServer.getInstance().chatTypes().networkId(chatTypeKey);
        buf.writeVarInt(networkId + 1);
    }
}
