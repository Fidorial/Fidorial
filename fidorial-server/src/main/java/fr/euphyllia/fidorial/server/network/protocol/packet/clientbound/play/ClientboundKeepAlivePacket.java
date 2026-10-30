package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Keep_Alive_(clientbound)
public record ClientboundKeepAlivePacket(long id) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.KEEP_ALIVE;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeLong(id);
    }
}
