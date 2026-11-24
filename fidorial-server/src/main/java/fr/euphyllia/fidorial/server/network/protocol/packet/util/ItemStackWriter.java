package fr.euphyllia.fidorial.server.network.protocol.packet.util;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.codecs.item.component.DataComponentNetworkCodecs;
import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.item.FidorialItemRegistry;
import fr.euphyllia.fidorial.server.network.serialization.CodecAwarePacketBuffer;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.fidorial.item.ItemDefinition;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ItemStackWriter {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ItemStackWriter.class);

    public static final NetworkEncoder<CodecAwarePacketBuffer, ItemStack> SLOT = (buf, stack) -> writeSlot(buf, stack, buf.registries());

    private ItemStackWriter() {
        throw new UnsupportedOperationException("ItemStackWriter cannot be instantiated.");
    }

    public static void writeSlot(final PacketBuffer buf, final ItemStack stack, final ServerRegistries registries) {

        if (stack.isEmpty()) {
            buf.writeVarInt(0);
            return;
        }

        final ItemStack wire = wire(stack);
        final int itemId = registries.registry(RegistryKey.ITEM).networkId(wire.id());
        if (itemId < 0) {
            LOGGER.warn("Item {} is not in the item registry; sending an empty slot instead", stack.id());
            buf.writeVarInt(0);
            return;
        }

        buf.writeVarInt(wire.count());
        buf.writeVarInt(itemId);
        writeComponents(buf, wire, registries);
    }

    /**
     * Writes a stack the way the item lists of components carry it, such as the projectiles a
     * crossbow is loaded with: its item, its count, then its components. Unlike a slot it has no
     * leading count and is never empty.
     *
     * @throws IllegalArgumentException if the stack is empty or its item is not in the registry
     */
    public static void writeTemplate(final PacketBuffer buf, final ItemStack stack, final ServerRegistries registries) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("An empty stack cannot be written as a template");
        }
        final ItemStack wire = wire(stack);
        final int itemId = registries.registry(RegistryKey.ITEM).networkId(wire.id());
        if (itemId < 0) {
            throw new IllegalArgumentException("Item " + stack.id() + " is not in the item registry");
        }
        buf.writeVarInt(itemId);
        buf.writeVarInt(wire.count());
        writeComponents(buf, wire, registries);
    }

    /**
     * {@return the stack as clients know it: an item defined by the server is sent as its network type}
     */
    private static ItemStack wire(final ItemStack stack) {
        final ItemDefinition definition = definition(stack);
        return definition == null
                ? stack
                : new ItemStack(definition.networkType(), stack.count(),
                FidorialItemRegistry.withDefaults(definition, stack.components()));
    }

    /**
     * Writes the components a stack adds to and removes from those of its item.
     */
    private static void writeComponents(final PacketBuffer buf, final ItemStack wire, final ServerRegistries registries) {
        final List<Map.Entry<DataComponentType<?>, Object>> added = encodableAdditions(wire, registries);
        final List<Integer> removed = encodableRemovals(wire, registries);

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
        final TypedKey<Item> item = definition == null ? stack.id() : definition.networkType();
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
                LOGGER.debug("Dropping component {} on {}: no wire codec", type, stack.id());
                continue;
            }
            if (registries.registry(RegistryKey.DATA_COMPONENT_TYPE).networkId(type.key()) < 0) {
                LOGGER.debug("Dropping component {} on {}: absent from the registry", type, stack.id());
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
