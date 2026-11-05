package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.world.ChunkNetworkSerializer;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import net.kyori.adventure.key.Key;

import java.util.List;

public record ClientboundChunksBiomesPacket(ChunkNetworkSerializer serializer, List<ChunkColumn> columns)
        implements ClientboundPacket<ClientboundChunksBiomesPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundChunksBiomesPacket> CODEC = NetworkEncoder.member(ClientboundChunksBiomesPacket::encode);

    @Override
    public Key name() {
        return PlayClientboundPackets.CHUNKS_BIOMES;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundChunksBiomesPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeVarInt(columns.size());

        for (final ChunkColumn column : columns) {
            buf.writeInt(column.chunkZ());
            buf.writeInt(column.chunkX());
            buf.writeByteArray(serializer.buildBiomes(buf.nettyBuf().alloc(), column));
        }
    }
}
