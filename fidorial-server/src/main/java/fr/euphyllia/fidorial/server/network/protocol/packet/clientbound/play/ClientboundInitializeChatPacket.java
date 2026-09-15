package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.chat.SignedChatSession;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

import java.util.UUID;

public record ClientboundInitializeChatPacket(UUID profileId, SignedChatSession chatSession) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_INFO_UPDATE;
    }

    @Override
    public void write(final PacketBuffer buf) {
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
