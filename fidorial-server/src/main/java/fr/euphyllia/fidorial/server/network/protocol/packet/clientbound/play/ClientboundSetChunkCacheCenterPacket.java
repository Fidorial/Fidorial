package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

public record ClientboundSetChunkCacheCenterPacket(int chunkX, int chunkZ) implements ClientboundPacket {

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_CHUNK_CACHE_CENTER;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(chunkX).writeVarInt(chunkZ);
    }
}
