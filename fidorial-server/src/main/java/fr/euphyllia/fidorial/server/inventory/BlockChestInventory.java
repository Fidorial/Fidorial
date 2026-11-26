package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.codecs.container.ContainerCodecs;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.chest.ChestBlocks;
import fr.euphyllia.fidorial.server.world.loot.ContainerLoot;
import fr.fidorial.inventory.ChestInventory;
import fr.fidorial.inventory.DoubleChestInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import io.papermc.adventurex.nbt.dfu.BinaryTagOps;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The 27 slots of one chest block, read from and written to its block entity the way vanilla saves
 * them: {@code Items}, {@code CustomName}, {@code LootTable} and {@code LootTableSeed}.
 *
 * <p>The block entity is the only place the slots live: every write saves it at once, and every
 * read checks it is still the data last read, so that any number of these views over one chest,
 * held by its viewers, its comparators or plugins, always agree. The data of a block entity is
 * immutable and replaced whole on every change, so that checking it is checking its identity.</p>
 *
 * <p>Views over the same chest share a lock: players looking into a chest from two regions click
 * on two threads.</p>
 */
public final class BlockChestInventory implements ChestInventory {

    public static final String ITEMS = "Items";
    public static final String CUSTOM_NAME = "CustomName";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockChestInventory.class);
    private static final Codec<SimpleContainer> CODEC = ContainerCodecs.containerCodec(() -> new SimpleContainer(SIZE));
    private static final Object[] LOCKS = new Object[64];

    static {
        Arrays.setAll(LOCKS, _ -> new Object());
    }

    private final ServerWorld world;
    private final BlockPosition position;
    private final Object lock;
    private final ItemStack[] slots = new ItemStack[SIZE];
    private @Nullable CompoundBinaryTag readFrom;

    public BlockChestInventory(final ServerWorld world, final BlockPosition position) {
        Preconditions.checkArgument(world != null, "A chest inventory needs the world of its chest");
        Preconditions.checkArgument(position != null, "A chest inventory in %s needs the position of its chest", world.key());
        this.world = world;
        this.position = position;
        this.lock = LOCKS[Math.floorMod(Objects.hash(world.key(), position), LOCKS.length)];
        Arrays.fill(slots, ItemStack.EMPTY);
    }

    @Override
    public ServerWorld world() {
        return world;
    }

    @Override
    public BlockPosition position() {
        return position;
    }

    @Override
    public int size() {
        return SIZE;
    }

    private static void checkSlot(final int slot) {
        if (slot < 0 || slot >= SIZE) {
            throw new IndexOutOfBoundsException("Invalid slot: " + slot);
        }
    }

    @Override
    public ItemStack get(final int slot) {
        checkSlot(slot);
        synchronized (lock) {
            refresh();
            return slots[slot];
        }
    }

    @Override
    public void set(final int slot, final @Nullable ItemStack stack) {
        checkSlot(slot);
        synchronized (lock) {
            final CompoundBinaryTag data = refresh();
            final ItemStack normalized = stack == null ? ItemStack.EMPTY : stack;
            if (data == null || slots[slot].equals(normalized)) {
                return;
            }
            slots[slot] = normalized;
            write(data);
        }
    }

    @Override
    public void clear() {
        synchronized (lock) {
            final CompoundBinaryTag data = refresh();
            if (data == null || isEmptyNow()) {
                return;
            }
            Arrays.fill(slots, ItemStack.EMPTY);
            write(data);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            refresh();
            return isEmptyNow();
        }
    }

    @Override
    public ItemStack[] contents() {
        synchronized (lock) {
            refresh();
            return slots.clone();
        }
    }

    @Override
    public void setContents(final @Nullable ItemStack[] contents) {
        synchronized (lock) {
            final CompoundBinaryTag data = refresh();
            if (data == null) {
                return;
            }
            Arrays.fill(slots, ItemStack.EMPTY);
            final int limit = Math.min(contents.length, SIZE);
            for (int slot = 0; slot < limit; slot++) {
                slots[slot] = contents[slot] == null ? ItemStack.EMPTY : contents[slot];
            }
            write(data);
        }
    }

    @Override
    public Optional<DoubleChestInventory> doubleChest() {
        return ChestBlocks.largeChest(world, position);
    }

    @Override
    public Optional<Component> customName() {
        final CompoundBinaryTag data = world.blockEntityData(position);
        if (data == null) {
            return Optional.empty();
        }
        final BinaryTag name = data.get(CUSTOM_NAME);
        if (name == null) {
            return Optional.empty();
        }
        return ComponentCodecs.COMPONENT_CODEC.parse(BinaryTagOps.binaryTagOps(), name)
                .resultOrPartial(message -> LOGGER.debug("Unreadable name of the chest at {}: {}", position, message));
    }

    @Override
    public void setCustomName(final @Nullable Component name) {
        synchronized (lock) {
            final CompoundBinaryTag data = world.blockEntityData(position);
            if (data == null) {
                return;
            }
            if (name == null) {
                replace(data, data.remove(CUSTOM_NAME));
                return;
            }
            ComponentCodecs.COMPONENT_CODEC.encodeStart(BinaryTagOps.binaryTagOps(), name)
                    .resultOrPartial(message -> LOGGER.warn("Unable to name the chest at {}: {}", position, message))
                    .ifPresent(tag -> replace(data, data.put(CUSTOM_NAME, tag)));
        }
    }

    @Override
    public Optional<Key> lootTable() {
        final CompoundBinaryTag data = world.blockEntityData(position);
        return data == null ? Optional.empty() : ContainerLoot.table(data);
    }

    @Override
    public void setLootTable(final @Nullable Key table, final long seed) {
        synchronized (lock) {
            final CompoundBinaryTag data = world.blockEntityData(position);
            if (data != null) {
                replace(data, ContainerLoot.withTable(data, table, seed));
            }
        }
    }

    /**
     * Brings the slots up to date with the block entity, rolling its loot table first when it still
     * has one.
     *
     * @return the data of the block entity, {@code null} when the chest is gone
     */
    private @Nullable CompoundBinaryTag refresh() {
        CompoundBinaryTag data = world.blockEntityData(position);
        if (data == null) {
            readFrom = null;
            Arrays.fill(slots, ItemStack.EMPTY);
            return null;
        }
        if (ContainerLoot.table(data).isPresent()) {
            data = ContainerLoot.unpack(data, SIZE, position);
            if (world.setBlockEntityData(position, data)) {
                ChestBlocks.contentsChanged(world, position);
            }
        }
        if (data != readFrom) {
            read(data);
            readFrom = data;
        }
        return data;
    }

    private void read(final CompoundBinaryTag data) {
        Arrays.fill(slots, ItemStack.EMPTY);
        if (data.get(ITEMS) == null) {
            return;
        }
        try {
            final SimpleContainer read = ContainerCodecs.readList(data, ITEMS, CODEC);
            for (int slot = 0; slot < SIZE; slot++) {
                slots[slot] = read.get(slot);
            }
        } catch (final IOException exception) {
            LOGGER.error("Unable to read the chest at {}, it shows empty", position, exception);
        }
    }

    private void write(final CompoundBinaryTag data) {
        final SimpleContainer written = new SimpleContainer(SIZE);
        for (int slot = 0; slot < SIZE; slot++) {
            written.set(slot, slots[slot]);
        }
        final CompoundBinaryTag saved;
        try {
            saved = ContainerCodecs.writeList(data, ITEMS, CODEC, written);
        } catch (final IOException exception) {
            LOGGER.error("Unable to save the chest at {}", position, exception);
            return;
        }
        if (world.setBlockEntityData(position, saved)) {
            // the slots are what was just saved
            readFrom = saved;
            ChestBlocks.contentsChanged(world, position);
        }
    }

    /**
     * Saves data that only differs from {@code previous} by what is not a slot: slots read from
     * {@code previous} are still those of the saved data.
     */
    private void replace(final CompoundBinaryTag previous, final CompoundBinaryTag next) {
        if (world.setBlockEntityData(position, next) && readFrom == previous) {
            readFrom = next;
        }
    }

    private boolean isEmptyNow() {
        for (final ItemStack stack : slots) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(final @Nullable Object other) {
        return other instanceof final BlockChestInventory chest && chest.world == world && chest.position.equals(position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world.key(), position);
    }

    @Override
    public String toString() {
        return "BlockChestInventory[" + world.key().asString() + " " + position + "]";
    }
}
