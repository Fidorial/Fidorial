package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.configuration;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.network.protocol.catalog.ConfigurationClientboundPackets;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.registry.Registry;
import fr.euphyllia.fidorial.server.registry.RegistryHolder;
import fr.euphyllia.fidorial.server.registry.biome.FidorialBiomeRegistry;
import fr.euphyllia.fidorial.server.registry.dialog.FidorialDialogRegistry;
import fr.euphyllia.fidorial.server.registry.dimension.FidorialDimensionTypeRegistry;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public record ClientboundUpdateTagsPacket(
        RegistryHolder network,
        FidorialBiomeRegistry biomes,
        FidorialDialogRegistry dialogs,
        FidorialDimensionTypeRegistry dimensionTypes
) implements ClientboundPacket {

    private static final Key BLOCK_REGISTRY = Key.key("block");
    private static final int NETHERRACK_ID = 334;
    private static final int MAGMA_BLOCK_ID = 729;
    private static final int BEDROCK_ID = 36;

    private record Payload(Key registry, Map<Key, List<Integer>> tags) {

    }

    @Override
    public Key name() {
        return ConfigurationClientboundPackets.UPDATE_TAGS;
    }

    @Override
    public void write(final PacketBuffer buf) {
        final List<Payload> payloads = collect();

        buf.writeVarInt(payloads.size());

        for (final Payload payload : payloads) {
            buf.writeKey(payload.registry());
            buf.writeVarInt(payload.tags().size());

            for (final Map.Entry<Key, List<Integer>> tag : payload.tags().entrySet()) {
                buf.writeKey(tag.getKey());
                buf.writeVarInt(tag.getValue().size());

                for (final int id : tag.getValue()) {
                    buf.writeVarInt(id);
                }
            }
        }
    }

    private List<Payload> collect() {
        final List<Payload> payloads = new ArrayList<>(network.size() + 1);
        boolean blockSeen = false;

        for (final Registry reg : network.all()) {
            final Map<Key, List<Key>> tags;
            final ToIntFunction<Key> networkId;

            if (reg.name().equals(FidorialBiomeRegistry.REGISTRY_NAME)) {
                tags = reg.tags();
                networkId = biomes::networkId;
            } else if (reg.name().equals(FidorialDialogRegistry.REGISTRY_NAME)) {
                tags = dialogs.networkTags();
                networkId = dialogs::networkId;
            } else if (reg.name().equals(FidorialDimensionTypeRegistry.REGISTRY_NAME)) {
                tags = reg.tags();
                networkId = dimensionTypes::networkId;
            } else {
                tags = reg.tags();
                final List<Key> entries = reg.entries();
                networkId = entries::indexOf;
            }

            blockSeen |= reg.name().equals(BLOCK_REGISTRY);
            payloads.add(new Payload(reg.name(), resolve(tags, networkId)));
        }

        if (!blockSeen) {
            payloads.add(infiniburnPayload());
        }

        return payloads;
    }

    private static Map<Key, List<Integer>> resolve(
            final Map<Key, List<Key>> tags,
            final ToIntFunction<Key> networkId
    ) {
        final Map<Key, List<Integer>> resolved = new LinkedHashMap<>(tags.size());

        for (final Map.Entry<Key, List<Key>> tag : tags.entrySet()) {
            final List<Integer> ids = new ArrayList<>(tag.getValue().size());

            for (final Key entry : tag.getValue()) {
                final int id = networkId.applyAsInt(entry);
                if (id >= 0) {
                    ids.add(id);
                }
            }

            resolved.put(tag.getKey(), ids);
        }

        return resolved;
    }

    private static Payload infiniburnPayload() {
        final Map<Key, List<Integer>> tags = new LinkedHashMap<>(3);
        tags.put(Key.key("infiniburn_overworld"), List.of(NETHERRACK_ID, MAGMA_BLOCK_ID));
        tags.put(Key.key("infiniburn_nether"), List.of(NETHERRACK_ID, MAGMA_BLOCK_ID));
        tags.put(Key.key("infiniburn_end"), List.of(NETHERRACK_ID, MAGMA_BLOCK_ID, BEDROCK_ID));
        return new Payload(BLOCK_REGISTRY, tags);
    }
}
