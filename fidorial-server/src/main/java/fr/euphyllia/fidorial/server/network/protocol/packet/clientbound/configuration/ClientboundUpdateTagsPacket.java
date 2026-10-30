package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The tags of every registry, their members by network ID.
 */
public record ClientboundUpdateTagsPacket(Collection<ServerRegistry<?>> registries) implements ClientboundPacket {

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.UPDATE_TAGS;
    }

    @Override
    public void write(final PacketBuffer buf) {
        buf.writeVarInt(registries.size());
        for (final ServerRegistry<?> registry : registries) {
            final Map<Key, List<Key>> tags = registry.tagMap();
            buf.writeKey(registry.name());
            buf.writeVarInt(tags.size());
            for (final Map.Entry<Key, List<Key>> tag : tags.entrySet()) {
                final IntList ids = new IntArrayList(tag.getValue().size());
                for (final Key member : tag.getValue()) {
                    final int id = registry.networkId(member);
                    if (id >= 0) {
                        ids.add(id);
                    }
                }
                buf.writeKey(tag.getKey());
                buf.writeVarInt(ids.size());
                for (int i = 0; i < ids.size(); i++) {
                    buf.writeVarInt(ids.getInt(i));
                }
            }
        }
    }
}
