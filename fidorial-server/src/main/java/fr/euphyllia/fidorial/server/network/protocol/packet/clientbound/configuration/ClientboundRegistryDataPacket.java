package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.registry.RegistryEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;

import java.util.List;

public record ClientboundRegistryDataPacket(Key registryId, List<RegistryEntry> entries)
        implements ClientboundPacket<ClientboundRegistryDataPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundRegistryDataPacket> CODEC = NetworkEncoder.member(ClientboundRegistryDataPacket::encode);

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.REGISTRY_DATA;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundRegistryDataPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeKey(registryId);
        buf.writeVarInt(entries.size());
        for (final RegistryEntry entry : entries) {
            buf.writeKey(entry.key());
            final BinaryTag data = entry.data();
            buf.writeBoolean(data != null);
            if (data != null) {
                buf.writeNbt(data);
            }
        }
    }
}
