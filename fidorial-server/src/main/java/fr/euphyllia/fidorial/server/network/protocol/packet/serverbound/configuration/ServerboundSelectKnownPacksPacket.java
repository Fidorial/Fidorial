package fr.euphyllia.fidorial.server.network.protocol.packet.serverbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.datapack.known.KnownPack;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.packet.listener.ConfigurationPacketListener;
import fr.fidorial.protocol.PacketListener;
import fr.fidorial.protocol.ServerboundPacket;

import java.util.List;

public record ServerboundSelectKnownPacksPacket(List<KnownPack> knownPacks) implements ServerboundPacket {

    private static final int MAX_PACKS = 64;

    private static final NetworkCodec<PacketBuffer, List<KnownPack>> KNOWN_PACKS =
            KnownPack.NETWORK_CODEC.listOf(PacketBuffer::readVarInt, PacketBuffer::writeVarInt, MAX_PACKS);

    public static ServerboundSelectKnownPacksPacket read(final PacketBuffer buf) {
        return new ServerboundSelectKnownPacksPacket(KNOWN_PACKS.read(buf));
    }

    @Override
    public void handle(final PacketListener listener) {
        ((ConfigurationPacketListener) listener).handleSelectKnownPacks(this);
    }
}
