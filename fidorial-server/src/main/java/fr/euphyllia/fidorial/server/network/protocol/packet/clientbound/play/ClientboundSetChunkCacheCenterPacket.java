package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

public record ClientboundSetChunkCacheCenterPacket(int chunkX, int chunkZ) implements ClientboundPacket<ClientboundSetChunkCacheCenterPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetChunkCacheCenterPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.VAR_INT, ClientboundSetChunkCacheCenterPacket::chunkX)
            .then(PrimitiveCodecs.VAR_INT, ClientboundSetChunkCacheCenterPacket::chunkZ);

    @Override
    public Key name() {
        return PlayClientboundPackets.SET_CHUNK_CACHE_CENTER;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSetChunkCacheCenterPacket> codec() {
        return CODEC;
    }
}
