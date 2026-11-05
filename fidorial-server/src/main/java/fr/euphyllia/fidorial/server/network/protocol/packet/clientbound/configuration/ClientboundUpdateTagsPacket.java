package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Map;

/**
 * The tags of every registry, their members by network ID.
 */
public record ClientboundUpdateTagsPacket(Collection<ServerRegistry<?>> registries) implements ClientboundPacket<ClientboundUpdateTagsPacket> {

    public static final NetworkEncoder<CodecAwarePacketBuffer, ClientboundUpdateTagsPacket> CODEC = NetworkEncoder.member(ClientboundUpdateTagsPacket::encode);

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.UPDATE_TAGS;
    }

    @Override
    public NetworkEncoder<CodecAwarePacketBuffer, ClientboundUpdateTagsPacket> codec() {
        return CODEC;
    }

    private void encode(final CodecAwarePacketBuffer buf) {
        buf.writeVarInt(registries.size());
        for (final ServerRegistry<?> registry : registries) {
            final Map<Key, IntList> tags = registry.networkTags();
            buf.writeKey(registry.name());
            buf.writeVarInt(tags.size());
            for (final Map.Entry<Key, IntList> tag : tags.entrySet()) {
                final IntList ids = tag.getValue();
                buf.writeKey(tag.getKey());
                buf.writeVarInt(ids.size());
                for (int i = 0; i < ids.size(); i++) {
                    buf.writeVarInt(ids.getInt(i));
                }
            }
        }
    }
}
