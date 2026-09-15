package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import static fr.euphyllia.fidorial.server.network.codec.ChatTypeCodec.writeChatType;

public record ClientboundDisguisedChatPacket(Component message, ChatType.Bound chatType, ServerPlayer player) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.DISGUISED_CHAT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeComponent(message);
        writeChatType(buf, chatType, player);
    }
}
