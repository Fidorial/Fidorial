package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.chat.SignedChatSession;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.UUID;

public record ClientboundInitializeChatPacket(UUID profileId, SignedChatSession chatSession) implements ClientboundPacket<ClientboundInitializeChatPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundInitializeChatPacket> CODEC = NetworkEncoder.member(ClientboundInitializeChatPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_INFO_UPDATE;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundInitializeChatPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeByte(0x02);
        buf.writeVarInt(1);
        buf.writeUuid(profileId);

        buf.writeBoolean(true);
        buf.writeUuid(chatSession.sessionId());
        buf.writeLong(chatSession.expiresAt().toEpochMilli());
        buf.writeByteArray(chatSession.publicKeyBytes());
        buf.writeByteArray(chatSession.signatureBytes());
    }
}
