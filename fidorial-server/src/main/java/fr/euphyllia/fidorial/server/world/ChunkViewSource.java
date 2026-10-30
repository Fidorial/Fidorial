package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import it.unimi.dsi.fastutil.longs.LongSet;

public interface ChunkViewSource {

    void collectViewedChunks(LongSet target);

    boolean isViewing(long chunkKey);

    void send(ClientboundPacket packet);
}
