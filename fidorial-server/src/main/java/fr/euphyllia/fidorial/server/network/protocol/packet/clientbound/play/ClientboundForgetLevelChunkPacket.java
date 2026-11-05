package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play;

import fr.euphyllia.fidorial.server.codecs.networking.CommonNetworkCodecs.PrimitiveCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.PlayClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import net.kyori.adventure.key.Key;

// https://minecraft.wiki/w/Java_Edition_protocol/Packets#Unload_Chunk
public record ClientboundForgetLevelChunkPacket(int chunkX, int chunkZ) implements ClientboundPacket<ClientboundForgetLevelChunkPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundForgetLevelChunkPacket> CODEC = NetworkEncoder
            .field(PrimitiveCodecs.INT, ClientboundForgetLevelChunkPacket::chunkZ)
            .then(PrimitiveCodecs.INT, ClientboundForgetLevelChunkPacket::chunkX);

    @Override
    public Key name() {
        return PlayClientboundPackets.FORGET_LEVEL_CHUNK;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundForgetLevelChunkPacket> codec() {
        return CODEC;
    }
}
