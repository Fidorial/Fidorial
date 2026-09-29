package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkCodec;
import fr.euphyllia.fidorial.server.datapack.known.KnownPack;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import net.kyori.adventure.key.Key;

import java.util.List;

public record ClientboundSelectKnownPacksPacket(List<KnownPack> knownPacks) implements ClientboundPacket {

    private static final NetworkCodec<PacketBuffer, List<KnownPack>> KNOWN_PACKS =
            KnownPack.NETWORK_CODEC.listOf(PacketBuffer::readVarInt, PacketBuffer::writeVarInt, Integer.MAX_VALUE);

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.SELECT_KNOWN_PACKS;
    }

    @Override
    public void write(final PacketBuffer buf) {
        KNOWN_PACKS.write(buf, knownPacks);
    }
}
