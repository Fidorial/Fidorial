package fr.euphyllia.fidorial.server.world.loot;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import fr.euphyllia.fidorial.server.codecs.container.ContainerCodecs;
import fr.euphyllia.fidorial.server.inventory.SimpleContainer;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

/**
 * The loot table a container still has to roll, saved in its block entity as vanilla does
 * ({@code LootTable}, {@code LootTableSeed}), and what rolling it puts into the container.
 *
 * <p>The loot only goes into the empty slots, in a random order. Large stacks are spread over a
 * few more slots, so that a chest is not filled in lumps, but at most half of the empty slots
 * fill up that way; what finds no room is lost, as in vanilla.</p>
 */
public final class ContainerLoot {

    public static final String LOOT_TABLE = "LootTable";
    public static final String LOOT_TABLE_SEED = "LootTableSeed";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ContainerLoot.class);
    private static final String ITEMS = "Items";

    private ContainerLoot() {
        throw new UnsupportedOperationException("ContainerLoot cannot be instantiated.");
    }

    /**
     * {@return the loot table the data of a block entity has yet to roll, empty when it has none or
     * an unreadable one}
     *
     * @param data the data of the block entity
     */
    public static Optional<Key> table(final CompoundBinaryTag data) {
        final BinaryTag tag = data.get(LOOT_TABLE);
        if (!(tag instanceof final StringBinaryTag name) || name.value().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Key.key(name.value()));
        } catch (final InvalidKeyException exception) {
            return Optional.empty();
        }
    }

    /**
     * {@return the data of a block entity given another loot table, or none}
     *
     * @param data  the data of the block entity
     * @param table the table to roll, {@code null} for none
     * @param seed  the seed of the roll, {@code 0} for a random one
     */
    public static CompoundBinaryTag withTable(final CompoundBinaryTag data, final @Nullable Key table, final long seed) {
        if (table == null) {
            return data.remove(LOOT_TABLE).remove(LOOT_TABLE_SEED);
        }
        final CompoundBinaryTag named = data.putString(LOOT_TABLE, table.asString());
        return seed == 0 ? named.remove(LOOT_TABLE_SEED) : named.putLong(LOOT_TABLE_SEED, seed);
    }

    /**
     * Rolls the loot table of a container and puts the loot into its empty shown slots.
     *
     * @param data       the data of the block entity of the container, holding its {@code Items}
     * @param storedSize the number of slots the block entity keeps, whose items are all kept
     * @param shownSize  the number of slots shown, the first ones, the only ones the loot goes into
     * @param position   where the container is, for the logs
     * @return the data with the loot in its {@code Items} and without the loot table, which is
     *         forgotten even when it could not be rolled
     */
    public static CompoundBinaryTag unpack(final CompoundBinaryTag data, final int storedSize, final int shownSize,
                                           final BlockPosition position) {
        Preconditions.checkArgument(shownSize >= 0 && shownSize <= storedSize,
                "The container at %s shows from 0 to the %s slots it keeps, not %s", position, storedSize, shownSize);
        final Optional<Key> table = table(data);
        final CompoundBinaryTag rolled = data.remove(LOOT_TABLE).remove(LOOT_TABLE_SEED);
        final BlockLootTables tables = BlockLootTables.instance();
        if (table.isEmpty() || tables == null) {
            return rolled;
        }
        final long seed = data.getLong(LOOT_TABLE_SEED);
        final RandomGenerator random = seed == 0 ? new SplittableRandom() : new SplittableRandom(seed);
        final List<ItemStack> loot = tables.roll(table.get(), null, ItemStack.EMPTY, random);
        if (loot.isEmpty()) {
            LOGGER.debug("The loot table {} of the container at {} gave nothing", table.get(), position);
            return rolled;
        }
        final Codec<SimpleContainer> codec = ContainerCodecs.containerCodec(() -> new SimpleContainer(storedSize));
        try {
            final SimpleContainer container = rolled.get(ITEMS) == null
                    ? new SimpleContainer(storedSize)
                    : ContainerCodecs.readList(rolled, ITEMS, codec);
            fill(container, shownSize, loot, random);
            return ContainerCodecs.writeList(rolled, ITEMS, codec, container);
        } catch (final IOException exception) {
            LOGGER.error("Unable to put the loot of {} into the container at {}", table.get(), position, exception);
            return rolled;
        }
    }

    /**
     * Puts stacks into the empty slots of a container, in a random order, spreading the larger
     * ones over a few more slots.
     *
     * @param container the container
     * @param slots     how many of its first slots may take the loot
     * @param loot      what to put in it
     * @param random    the source of randomness
     */
    public static void fill(final Container container, final int slots, final List<ItemStack> loot,
                            final RandomGenerator random) {
        final List<Integer> empty = new ArrayList<>();
        for (int slot = 0; slot < Math.min(slots, container.size()); slot++) {
            if (container.get(slot).isEmpty()) {
                empty.add(slot);
            }
        }
        shuffle(empty, random);

        final List<ItemStack> stacks = new ArrayList<>();
        for (final ItemStack stack : loot) {
            ItemStack rest = stack;
            final int max = Math.max(1, ItemDefaults.maxStackSize(rest.id(), rest));
            while (!rest.isEmpty() && rest.count() > max) {
                stacks.add(rest.withCount(max));
                rest = rest.withCount(rest.count() - max);
            }
            if (!rest.isEmpty()) {
                stacks.add(rest);
            }
        }

        final int target = Math.min(empty.size(), Math.max(stacks.size(), empty.size() / 2));
        while (stacks.size() < target) {
            final List<Integer> splittable = new ArrayList<>();
            for (int index = 0; index < stacks.size(); index++) {
                if (stacks.get(index).count() > 1) {
                    splittable.add(index);
                }
            }
            if (splittable.isEmpty()) {
                break;
            }
            final int index = splittable.get(random.nextInt(splittable.size()));
            final ItemStack stack = stacks.get(index);
            final int part = 1 + random.nextInt(stack.count() / 2);
            stacks.set(index, stack.withCount(stack.count() - part));
            stacks.add(stack.withCount(part));
        }
        shuffle(stacks, random);

        final int placed = Math.min(stacks.size(), empty.size());
        for (int index = 0; index < placed; index++) {
            container.set(empty.get(index), stacks.get(index));
        }
        if (stacks.size() > placed) {
            LOGGER.debug("{} stacks of loot found no room and are lost", stacks.size() - placed);
        }
    }

    private static <T> void shuffle(final List<T> list, final RandomGenerator random) {
        for (int index = list.size() - 1; index > 0; index--) {
            final int other = random.nextInt(index + 1);
            final T kept = list.get(index);
            list.set(index, list.get(other));
            list.set(other, kept);
        }
    }
}
