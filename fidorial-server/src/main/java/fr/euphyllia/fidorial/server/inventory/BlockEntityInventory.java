package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.adventure.ComponentCodecs;
import fr.euphyllia.fidorial.server.codecs.container.ContainerCodecs;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.loot.ContainerLoot;
import fr.fidorial.inventory.BlockInventory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The slots of a block, read from and written to its block entity the way vanilla saves them:
 * {@code Items}, {@code CustomName}, {@code LootTable} and {@code LootTableSeed}.
 *
 * <p>The block entity is the only place the slots live: every write saves it at once, and every
 * read checks it is still the data last read, so that any number of these views over one block,
 * held by its viewers, its comparators or plugins, always agree. The data of a block entity is
 * immutable and replaced whole on every change, so that checking it is checking its identity.</p>
 *
 * <p>A view shows the first {@link #size()} slots of the block entity, out of the
 * {@linkplain #storedSize() stored} ones: a barrel shows the rows its world gives, and keeps the
 * items of the rows beyond in its block entity. Writing a shown slot keeps the hidden ones as they
 * were.</p>
 *
 * <p>Views over the same block share a lock: players looking into a block from two regions click
 * on two threads.</p>
 */
public abstract class BlockEntityInventory implements BlockInventory {

    public static final String ITEMS = "Items";
    public static final String CUSTOM_NAME = "CustomName";

    /**
     * The most slots a block entity can save: {@code Slot} is a byte.
     */
    private static final int MAX_STORED_SIZE = 256;

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockEntityInventory.class);
    private static final Object[] LOCKS = new Object[64];

    /**
     * The codecs of the {@code Items} of block entities, by number of slots kept.
     */
    private static final Map<Integer, Codec<SimpleContainer>> CODECS = new ConcurrentHashMap<>();

    static {
        for (int index = 0; index < LOCKS.length; index++) {
            LOCKS[index] = new Object();
        }
    }

    private final ServerWorld world;
    private final BlockPosition position;
    private final String block;
    private final int size;
    private final Codec<SimpleContainer> codec;
    private final Object lock;
    private final ItemStack[] slots;
    private @Nullable CompoundBinaryTag readFrom;

    /**
     * @param world      the world of the block
     * @param position   where the block is
     * @param block      what the block is, for the logs, such as {@code chest}
     * @param size       how many slots the view shows
     * @param storedSize how many slots the block entity keeps, the shown ones first
     */
    protected BlockEntityInventory(final ServerWorld world, final BlockPosition position, final String block,
                                   final int size, final int storedSize) {
        Preconditions.checkArgument(world != null, "A block inventory needs the world of its block");
        Preconditions.checkArgument(position != null, "A block inventory in %s needs the position of its block", world.key());
        Preconditions.checkArgument(block != null, "The block inventory at %s needs to say what its block is", position);
        Preconditions.checkArgument(size >= 0 && size <= storedSize,
                "The %s at %s shows from 0 to the %s slots it keeps, not %s", block, position, storedSize, size);
        Preconditions.checkArgument(storedSize <= MAX_STORED_SIZE,
                "The %s at %s keeps at most %s slots, not %s", block, position, MAX_STORED_SIZE, storedSize);
        this.world = world;
        this.position = position;
        this.block = block;
        this.size = size;
        this.codec = CODECS.computeIfAbsent(storedSize, slots -> ContainerCodecs.containerCodec(() -> new SimpleContainer(slots)));
        this.lock = LOCKS[Math.floorMod(Objects.hash(world.key(), position), LOCKS.length)];
        this.slots = new ItemStack[storedSize];
        Arrays.fill(slots, ItemStack.EMPTY);
    }

    /**
     * Tells the comparators reading the block that its contents changed.
     */
    protected abstract void contentsChanged();

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
        return size;
    }

    /**
     * {@return how many slots the block entity keeps, the shown ones and the hidden ones}
     */
    public final int storedSize() {
        return slots.length;
    }

    private void checkSlot(final int slot) {
        if (slot < 0 || slot >= size) {
            throw new IndexOutOfBoundsException("Invalid slot " + slot + " of the " + block + " at " + position
                    + ", which shows " + size);
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
            if (data == null || isEmpty(0, size)) {
                return;
            }
            Arrays.fill(slots, 0, size, ItemStack.EMPTY);
            write(data);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (lock) {
            refresh();
            return isEmpty(0, size);
        }
    }

    @Override
    public ItemStack[] contents() {
        synchronized (lock) {
            refresh();
            final ItemStack[] shown = new ItemStack[size];
            System.arraycopy(slots, 0, shown, 0, size);
            return shown;
        }
    }

    @Override
    public void setContents(final @Nullable ItemStack[] contents) {
        synchronized (lock) {
            final CompoundBinaryTag data = refresh();
            if (data == null) {
                return;
            }
            Arrays.fill(slots, 0, size, ItemStack.EMPTY);
            final int limit = Math.min(contents.length, size);
            for (int slot = 0; slot < limit; slot++) {
                slots[slot] = contents[slot] == null ? ItemStack.EMPTY : contents[slot];
            }
            write(data);
        }
    }

    /**
     * {@return every slot the block entity keeps, the hidden ones after the shown ones}
     */
    public final ItemStack[] storedContents() {
        synchronized (lock) {
            refresh();
            return slots.clone();
        }
    }

    /**
     * Empties every slot the block entity keeps, the hidden ones included.
     */
    public final void clearStored() {
        synchronized (lock) {
            final CompoundBinaryTag data = refresh();
            if (data == null || isEmpty(0, slots.length)) {
                return;
            }
            Arrays.fill(slots, ItemStack.EMPTY);
            write(data);
        }
    }

    /**
     * Empties the slots the view does not show.
     *
     * @return the stacks taken out, in slot order, empty when the hidden slots held nothing
     */
    public final List<ItemStack> clearHidden() {
        synchronized (lock) {
            final CompoundBinaryTag data = refresh();
            if (data == null || isEmpty(size, slots.length)) {
                return List.of();
            }
            final List<ItemStack> cleared = new ArrayList<>();
            for (int slot = size; slot < slots.length; slot++) {
                if (!slots[slot].isEmpty()) {
                    cleared.add(slots[slot]);
                    slots[slot] = ItemStack.EMPTY;
                }
            }
            write(data);
            return List.copyOf(cleared);
        }
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
                .resultOrPartial(message -> LOGGER.debug("Unreadable name of the {} at {}: {}", block, position, message));
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
                    .resultOrPartial(message -> LOGGER.warn("Unable to name the {} at {}: {}", block, position, message))
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
     * @return the data of the block entity, {@code null} when the block is gone
     */
    private @Nullable CompoundBinaryTag refresh() {
        CompoundBinaryTag data = world.blockEntityData(position);
        if (data == null) {
            readFrom = null;
            Arrays.fill(slots, ItemStack.EMPTY);
            return null;
        }
        if (ContainerLoot.table(data).isPresent()) {
            data = ContainerLoot.unpack(data, slots.length, size, position);
            if (world.setBlockEntityData(position, data)) {
                contentsChanged();
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
            final SimpleContainer read = ContainerCodecs.readList(data, ITEMS, codec);
            for (int slot = 0; slot < slots.length; slot++) {
                slots[slot] = read.get(slot);
            }
        } catch (final IOException exception) {
            LOGGER.error("Unable to read the {} at {}, it shows empty", block, position, exception);
        }
    }

    private void write(final CompoundBinaryTag data) {
        final SimpleContainer written = new SimpleContainer(slots.length);
        for (int slot = 0; slot < slots.length; slot++) {
            written.set(slot, slots[slot]);
        }
        final CompoundBinaryTag saved;
        try {
            saved = ContainerCodecs.writeList(data, ITEMS, codec, written);
        } catch (final IOException exception) {
            LOGGER.error("Unable to save the {} at {}", block, position, exception);
            return;
        }
        if (world.setBlockEntityData(position, saved)) {
            // the slots are what was just saved
            readFrom = saved;
            contentsChanged();
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

    private boolean isEmpty(final int from, final int to) {
        for (int slot = from; slot < to; slot++) {
            if (!slots[slot].isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final boolean equals(final @Nullable Object other) {
        return other instanceof final BlockEntityInventory inventory && inventory.getClass() == getClass()
                && inventory.world == world && inventory.position.equals(position) && inventory.size == size;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(world.key(), position, size);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + world.key().asString() + " " + position + ", " + size + " slots]";
    }
}
