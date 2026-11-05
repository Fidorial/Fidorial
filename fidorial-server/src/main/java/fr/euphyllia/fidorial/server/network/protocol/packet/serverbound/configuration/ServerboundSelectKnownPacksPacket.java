package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkDecoder;
import fr.euphyllia.fidorial.server.datapack.known.KnownPack;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

import java.util.List;

public record ServerboundSelectKnownPacksPacket(List<KnownPack> knownPacks) implements ServerboundPacket {

    private static final int MAX_PACKS = 64;

    private static final NetworkCodec<PacketBuffer, List<KnownPack>> KNOWN_PACKS =
            KnownPack.NETWORK_CODEC.listOf(PacketBuffer::readVarInt, PacketBuffer::writeVarInt, MAX_PACKS);

    public static final NetworkDecoder<PacketBuffer, ServerboundSelectKnownPacksPacket> DECODER = NetworkDecoder.composite(
            KNOWN_PACKS,
            ServerboundSelectKnownPacksPacket::new);

    @Override
    public void handle(final PacketListener listener) {
        ((ConfigurationPacketListener) listener).handleSelectKnownPacks(this);
    }
}
