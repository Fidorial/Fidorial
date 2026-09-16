package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Clear_Titles
public record ClientboundClearTitlesPacket(boolean reset) implements ClientboundPacket {
    @Override
    public Key name() {
        return PlayClientboundPackets.CLEAR_TITLES;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeBoolean(reset);
    }
}
