package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.fidorial.math.Position;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#World_Event
public record ClientboundLevelEventPacket(int event, Position position, int data, boolean global)
        implements ClientboundPacket {

    public static final int BLOCK_BREAK = 2001;

    @Override
    public Key name() {
        return PlayClientboundPackets.LEVEL_EVENT;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeInt(event);
        buf.writePosition(position.blockX(), position.blockY(), position.blockZ());
        buf.writeInt(data);
        buf.writeBoolean(global);
    }
}
