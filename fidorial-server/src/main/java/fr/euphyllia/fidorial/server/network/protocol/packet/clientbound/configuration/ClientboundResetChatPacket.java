package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Reset_Chat
public record ClientboundResetChatPacket() implements ClientboundPacket {

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.RESET_CHAT;
    }

    @Override
    public void write(final PacketBuffer buf) {
    }
}
