package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import static fr.euphyllia.fidorial.server.network.protocol.packet.util.ChatTypeCodec.writeChatType;

public record ClientboundDisguisedChatPacket(Component message, ChatType.Bound chatType, ServerPlayer player) implements ClientboundPacket<ClientboundDisguisedChatPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundDisguisedChatPacket> CODEC = NetworkEncoder.member(ClientboundDisguisedChatPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.DISGUISED_CHAT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundDisguisedChatPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeComponent(message);
        writeChatType(buf, chatType, player);
    }
}
