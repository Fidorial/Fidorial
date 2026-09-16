package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.chat.SignedChatSession;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.entity.PlayerProfile;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public record ClientboundPlayerInfoUpdatePacket(PlayerProfile profile, int gameMode, int ping, @Nullable SignedChatSession chatSession)
        implements ClientboundPacket {

    private static final int ADD_PLAYER = 0x01;
    private static final int INITIALIZE_CHAT = 0x02;
    private static final int UPDATE_GAME_MODE = 0x04;
    private static final int UPDATE_LISTED = 0x08;
    private static final int UPDATE_LATENCY = 0x10;

    public ClientboundPlayerInfoUpdatePacket(final PlayerProfile profile, final int gameMode, final int ping) {
        this(profile, gameMode, ping, null);
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.PLAYER_INFO_UPDATE;
    }

    @Override
    public void write(final PacketBuffer buf) {
        int actions = ADD_PLAYER | UPDATE_GAME_MODE | UPDATE_LISTED | UPDATE_LATENCY;
        if (chatSession != null) {
            actions |= INITIALIZE_CHAT;
        }
        buf.writeByte(actions);
        buf.writeVarInt(1);
        buf.writeUuid(profile.uuid());

        buf.writeString(profile.name());
        buf.writeVarInt(profile.properties().size());
        for (final PlayerProfile.Property property : profile.properties()) {
            buf.writeString(property.name());
            buf.writeString(property.value());
            final boolean signed = property.signature() != null;
            buf.writeBoolean(signed);
            if (signed) {
                buf.writeString(property.signature());
            }
        }

        if (chatSession != null) {
            buf.writeBoolean(true);
            buf.writeUuid(chatSession.sessionId());
            buf.writeLong(chatSession.expiresAt().toEpochMilli());
            buf.writeByteArray(chatSession.publicKeyBytes());
            buf.writeByteArray(chatSession.signatureBytes());
        }

        buf.writeVarInt(gameMode);
        buf.writeBoolean(true);
        buf.writeVarInt(ping);
    }
}
