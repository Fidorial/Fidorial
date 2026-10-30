package fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.codecs.item.component.DataComponentNetworkCodecs;
import fr.euphyllia.fidorial.server.item.FidorialItemRegistry;
import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.fidorial.item.ItemDefinition;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ItemStackWriter {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ItemStackWriter.class);

    private ItemStackWriter() {
        throw new UnsupportedOperationException("ItemStackWriter cannot be instantiated.");
    }

    public static void writeSlot(final PacketBuffer buf, final ItemStack stack, final ServerRegistries registries) {

        if (stack.isEmpty()) {
            buf.writeVarInt(0);
            return;
        }

        final ItemDefinition definition = definition(stack);
        final ItemStack wire = definition == null
                ? stack
                : new ItemStack(definition.networkType(), stack.count(),
                FidorialItemRegistry.withDefaults(definition, stack.components()));

        final int itemId = registries.registry(RegistryKey.ITEM).networkId(wire.id());
        if (itemId < 0) {
            LOGGER.warn("Item {} is not in the item registry; sending an empty slot instead", stack.id().asString());
            buf.writeVarInt(0);
            return;
        }

        final List<Map.Entry<DataComponentType<?>, Object>> added = encodableAdditions(wire, registries);
        final List<Integer> removed = encodableRemovals(wire, registries);

        buf.writeVarInt(wire.count());
        buf.writeVarInt(itemId);
        buf.writeVarInt(added.size());
        buf.writeVarInt(removed.size());

        for (final Map.Entry<DataComponentType<?>, Object> entry : added) {
            final DataComponentType<?> type = entry.getKey();
            buf.writeVarInt(registries.registry(RegistryKey.DATA_COMPONENT_TYPE).networkId(type.key()));
            DataComponentNetworkCodecs.writeErased(buf, registries, type, entry.getValue());
        }

        for (final int typeId : removed) {
            buf.writeVarInt(typeId);
        }
    }

    public static int networkId(final ItemStack stack, final ServerRegistries registries) {
        final ItemDefinition definition = definition(stack);
        final Key item = definition == null ? stack.id() : definition.networkType();
        return registries.registry(RegistryKey.ITEM).networkId(item);
    }

    private static @Nullable ItemDefinition definition(final ItemStack stack) {
        return FidorialServer.getInstance().registries().items().definition(stack.id()).orElse(null);
    }

    private static List<Map.Entry<DataComponentType<?>, Object>> encodableAdditions(
            final ItemStack stack, final ServerRegistries registries) {

        final List<Map.Entry<DataComponentType<?>, Object>> encodable = new ArrayList<>();

        for (final Map.Entry<DataComponentType<?>, Object> entry : stack.components().entries()) {
            final DataComponentType<?> type = entry.getKey();

            if (!DataComponentNetworkCodecs.hasCodec(type)) {
                LOGGER.debug("Dropping component {} on {}: no wire codec", type, stack.id().asString());
                continue;
            }
            if (registries.registry(RegistryKey.DATA_COMPONENT_TYPE).networkId(type.key()) < 0) {
                LOGGER.debug("Dropping component {} on {}: absent from the registry", type, stack.id().asString());
                continue;
            }
            encodable.add(entry);
        }

        return encodable;
    }

    private static List<Integer> encodableRemovals(final ItemStack stack, final ServerRegistries registries) {

        final List<Integer> ids = new ArrayList<>();

        for (final DataComponentType<?> type : stack.components().removedTypes()) {
            final int id = registries.registry(RegistryKey.DATA_COMPONENT_TYPE).networkId(type.key());
            if (id >= 0) {
                ids.add(id);
            }
        }

        return ids;
    }
}
