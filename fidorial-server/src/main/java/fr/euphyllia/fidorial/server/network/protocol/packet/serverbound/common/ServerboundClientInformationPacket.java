package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.common;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.PlayPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;


public record ServerboundClientInformationPacket(String language, int viewDistance, int displayedSkinParts) implements ServerboundPacket {

    public static ServerboundClientInformationPacket read(final PacketBuffer buf) {
        final String language = buf.readString(16);
        final int viewDistance = buf.readByte();
        final int chatMode = buf.readVarInt();
        final boolean chatColors = buf.readBoolean();
        final int skinParts = buf.readUByte();
        final int mainHand = buf.readVarInt();
        final boolean enableTextFiltering = buf.readBoolean();
        final boolean allowServerListings = buf.readBoolean();
        final int particleStatus = buf.readVarInt();
        return new ServerboundClientInformationPacket(language, viewDistance, skinParts);
    }

    @Override
    public void handle(final PacketListener listener) {
        if (listener instanceof final PlayPacketListener play) {
            play.handleClientInformation(this);
        } else if (listener instanceof final ConfigurationPacketListener config) {
            config.handleClientInformation(this);
        }
    }
}
