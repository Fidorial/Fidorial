package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.registry.RegistryEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;

import java.util.List;

public record ClientboundRegistryDataPacket(Key registryId, List<RegistryEntry> entries)
        implements ClientboundPacket {

    public static ClientboundRegistryDataPacket knownOnly(final Key registryId, final List<Key> keys) {
        return new ClientboundRegistryDataPacket(registryId, keys.stream().map(RegistryEntry::known).toList());
    }

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.REGISTRY_DATA;
    }

    @Override
    public void write(final PacketBuffer buf) {
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
