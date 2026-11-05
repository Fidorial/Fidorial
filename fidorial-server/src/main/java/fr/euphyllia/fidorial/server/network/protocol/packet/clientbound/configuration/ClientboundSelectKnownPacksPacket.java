package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.datapack.known.KnownPack;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import net.kyori.adventure.key.Key;

import java.util.List;

public record ClientboundSelectKnownPacksPacket(List<KnownPack> knownPacks) implements ClientboundPacket<ClientboundSelectKnownPacksPacket> {

    private static final NetworkCodec<PacketBuffer, List<KnownPack>> KNOWN_PACKS =
            KnownPack.NETWORK_CODEC.listOf(PacketBuffer::readVarInt, PacketBuffer::writeVarInt, Integer.MAX_VALUE);

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundSelectKnownPacksPacket> CODEC =
            NetworkEncoder.field(KNOWN_PACKS, ClientboundSelectKnownPacksPacket::knownPacks);

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.SELECT_KNOWN_PACKS;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundSelectKnownPacksPacket> codec() {
        return CODEC;
    }
}
