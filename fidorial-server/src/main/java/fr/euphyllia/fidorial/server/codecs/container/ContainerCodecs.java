package fr.euphyllia.fidorial.server.codecs.container;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.euphyllia.fidorial.server.codecs.item.ItemStackCodecs;
import fr.euphyllia.fidorial.server.world.storage.datafixers.DataFixerType;
import fr.euphyllia.fidorial.server.world.storage.datafixers.registry.DataFixersRegistry;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentMap;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static fr.euphyllia.fidorial.server.codecs.CommonCodecs.KEY_CODEC;

public final class ContainerCodecs {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ContainerCodecs.class);

    private ContainerCodecs() {
    }

    private record SlotEntry(int slot, ItemStack stack) {
    }

    public static final MapCodec<ItemStack> ITEM_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KEY_CODEC.fieldOf("id").forGetter(ItemStack::id),
            Codec.INT.fieldOf("count").orElse(1).forGetter(ItemStack::count),
            ItemStackCodecs.COMPONENT_MAP_CODEC.optionalFieldOf("components", DataComponentMap.EMPTY).forGetter(ItemStack::components)
    ).apply(instance, ItemStack::new));

    private static final MapCodec<SlotEntry> SLOT_ENTRY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BYTE.fieldOf("Slot").forGetter(e -> (byte) e.slot()),
            ITEM_CODEC.forGetter(SlotEntry::stack)
    ).apply(instance, (slot, stack) -> new SlotEntry(slot & 0xFF, stack)));

    public static <C extends Container> Codec<C> containerCodec(final Supplier<C> factory) {
        return containerCodec(factory, Integer.MAX_VALUE);
    }

    public static <C extends Container> Codec<C> containerCodec(final Supplier<C> factory, final int savedSlots) {
        return SLOT_ENTRY_CODEC.codec().listOf().xmap(
                entries -> {
                    final C container = factory.get();
                    for (final SlotEntry entry : entries) {
                        if (entry.slot() < container.size() && entry.stack().count() > 0) {
                            container.set(entry.slot(), entry.stack());
                        }
                    }
                    return container;
                },
                container -> {
                    final List<SlotEntry> entries = new ArrayList<>();
                    final int slots = Math.min(savedSlots, container.size());
                    for (int slot = 0; slot < slots; slot++) {
                        final ItemStack stack = container.get(slot);
                        if (!stack.isEmpty()) {
                            entries.add(new SlotEntry(slot, stack));
                        }
                    }
                    return entries;
                }
        );
    }

    public static CompoundBinaryTag readRoot(final byte @Nullable [] payload, final DataFixerType fixerType) throws IOException {
        if (payload == null) {
            return CompoundBinaryTag.empty();
        }
        return DataFixersRegistry.update(fixerType, BinaryTagIO.reader().readNamed(new ByteArrayInputStream(payload)).getValue(), false);
    }

    public static byte[] writeRoot(final String rootName, final CompoundBinaryTag root, final int dataVersion) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryTagIO.writer().writeNamed(Map.entry(rootName, root.putInt("DataVersion", dataVersion)), baos);
        return baos.toByteArray();
    }

    public static <C extends Container> C readList(final CompoundBinaryTag root, final String listFieldName, final Codec<C> codec) throws IOException {
        final BinaryTag list = root.get(listFieldName);
        return codec.parse(BinaryTagOps.binaryTagOps(), list != null ? list : ListBinaryTag.empty())
                .resultOrPartial(msg -> LOGGER.warn("Dropped invalid entries in {}: {}", listFieldName, msg))
                .orElseThrow(() -> new IOException("Failed to decode " + listFieldName));
    }

    public static <C extends Container> CompoundBinaryTag writeList(final CompoundBinaryTag root, final String listFieldName, final Codec<C> codec, final C container) throws IOException {
        return root.put(listFieldName, codec.encodeStart(BinaryTagOps.binaryTagOps(), container)
                .getOrThrow(msg -> new IOException("Failed to encode " + listFieldName + ": " + msg)));
    }

    public static Optional<ItemStack> readItem(final BinaryTag tag) {
        return ITEM_CODEC.codec().parse(BinaryTagOps.binaryTagOps(), tag)
                .resultOrPartial(msg -> LOGGER.warn("Dropped invalid item: {}", msg));
    }

    public static BinaryTag writeItem(final ItemStack stack) throws IOException {
        return ITEM_CODEC.codec().encodeStart(BinaryTagOps.binaryTagOps(), stack)
                .getOrThrow(msg -> new IOException("Failed to encode item: " + msg));
    }
}
