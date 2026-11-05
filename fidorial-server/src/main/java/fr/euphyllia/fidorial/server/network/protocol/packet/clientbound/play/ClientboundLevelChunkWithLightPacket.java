package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.euphyllia.fidorial.server.world.ChunkNetworkSerializer;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Chunk_Data_and_Update_Light
public final class ClientboundLevelChunkWithLightPacket implements ClientboundPacket<ClientboundLevelChunkWithLightPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundLevelChunkWithLightPacket> CODEC = NetworkEncoder.member(ClientboundLevelChunkWithLightPacket::encode);

    private final byte[] payload;

    public ClientboundLevelChunkWithLightPacket(
            final ChunkNetworkSerializer serializer, final ChunkColumn column, final boolean hasSkylight) {
        this(serializer, column, ByteBufAllocator.DEFAULT, hasSkylight);
    }

    public ClientboundLevelChunkWithLightPacket(
            final ChunkNetworkSerializer serializer,
            final ChunkColumn column,
            final ByteBufAllocator allocator,
            final boolean hasSkylight) {

        final ByteBuf scratch = allocator.buffer();
        try {
            serializer.writeChunk(new PacketBuffer(scratch), allocator, column, hasSkylight);
            this.payload = new byte[scratch.readableBytes()];
            scratch.readBytes(this.payload);
        } finally {
            scratch.release();
        }
    }

    @Override
    public Key name() {
        return PlayClientboundPackets.LEVEL_CHUNK_WITH_LIGHT;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundLevelChunkWithLightPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeRawBytes(payload);
    }
}
