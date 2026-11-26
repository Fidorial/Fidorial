package fr.euphyllia.fidorial.server.world.block.chest;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.inventory.BlockChestInventory;
import fr.euphyllia.fidorial.server.inventory.LargeChestInventory;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.inventory.ChestInventory;
import fr.fidorial.inventory.Container;
import fr.fidorial.inventory.DoubleChestInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.SoundEvent;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.type.Chest;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Every chest the server knows, and what they share: finding the other half of a large chest, what
 * its window shows, its name, how full a comparator reads it, and when a solid block keeps a lid
 * shut.
 */
public final class ChestBlocks {

    public static final ChestBlock CHEST = new ChestBlock(BlockTypeKeys.CHEST, ChestFamily.WOODEN, 0, false,
            SoundEventKeys.BLOCK_CHEST_OPEN, SoundEventKeys.BLOCK_CHEST_CLOSE);

    public static final TrappedChestBlock TRAPPED_CHEST = TrappedChestBlock.INSTANCE;

    /**
     * The copper chests, from the least oxidized to the most, the unwaxed ones first.
     */
    public static final List<ChestBlock> COPPER = List.of(
            copper(BlockTypeKeys.COPPER_CHEST, 0, false),
            copper(BlockTypeKeys.EXPOSED_COPPER_CHEST, 1, false),
            copper(BlockTypeKeys.WEATHERED_COPPER_CHEST, 2, false),
            copper(BlockTypeKeys.OXIDIZED_COPPER_CHEST, 3, false),
            copper(BlockTypeKeys.WAXED_COPPER_CHEST, 0, true),
            copper(BlockTypeKeys.WAXED_EXPOSED_COPPER_CHEST, 1, true),
            copper(BlockTypeKeys.WAXED_WEATHERED_COPPER_CHEST, 2, true),
            copper(BlockTypeKeys.WAXED_OXIDIZED_COPPER_CHEST, 3, true));

    /**
     * Every chest, to register with the block registry and the redstone registry.
     */
    public static final List<ChestBlock> ALL = all();

    private static final Map<TypedKey<BlockType>, ChestBlock> BY_KEY =
            ALL.stream().collect(Collectors.toUnmodifiableMap(ChestBlock::key, Function.identity()));

    private static final String SINGLE_TITLE = "container.chest";
    private static final String DOUBLE_TITLE = "container.chestDouble";

    /**
     * The chests whose comparators are about to be told their contents changed.
     */
    private static final Set<Pending> PENDING = ConcurrentHashMap.newKeySet();

    private ChestBlocks() {
        throw new UnsupportedOperationException("ChestBlocks cannot be instantiated.");
    }

    private static ChestBlock copper(final TypedKey<BlockType> key, final int oxidation, final boolean waxed) {
        final TypedKey<SoundEvent> open = switch (oxidation) {
            case 2 -> SoundEventKeys.BLOCK_COPPER_CHEST_WEATHERED_OPEN;
            case 3 -> SoundEventKeys.BLOCK_COPPER_CHEST_OXIDIZED_OPEN;
            default -> SoundEventKeys.BLOCK_COPPER_CHEST_OPEN;
        };
        final TypedKey<SoundEvent> close = switch (oxidation) {
            case 2 -> SoundEventKeys.BLOCK_COPPER_CHEST_WEATHERED_CLOSE;
            case 3 -> SoundEventKeys.BLOCK_COPPER_CHEST_OXIDIZED_CLOSE;
            default -> SoundEventKeys.BLOCK_COPPER_CHEST_CLOSE;
        };
        return new ChestBlock(key, ChestFamily.COPPER, oxidation, waxed, open, close);
    }

    private static List<ChestBlock> all() {
        final List<ChestBlock> all = new ArrayList<>();
        all.add(CHEST);
        all.add(TRAPPED_CHEST);
        all.addAll(COPPER);
        return List.copyOf(all);
    }

    /**
     * {@return the behaviour of a chest, {@code null} for a block that is not one}
     *
     * @param block the block type
     */
    public static @Nullable ChestBlock behaviour(final TypedKey<BlockType> block) {
        return BY_KEY.get(block);
    }

    /**
     * {@return the copper chest of an oxidation}
     *
     * @param oxidation from {@code 0} (none) to {@code 3}
     * @param waxed     whether it is waxed
     */
    public static ChestBlock copper(final int oxidation, final boolean waxed) {
        return COPPER.get(Math.clamp(oxidation, 0, 3) + (waxed ? 4 : 0));
    }

    /**
     * {@return the state at a position, {@code null} when its chunk is not loaded}
     */
    public static @Nullable BlockData stateAt(final ServerWorld world, final BlockPosition position) {
        final ChunkColumn column = world.loadedColumn(position.chunkX(), position.chunkZ());
        if (column == null || !FidorialServer.isInitialized()) {
            return null;
        }
        final BlockState state = column.getBlock(position.blockX() & 15, position.blockY(), position.blockZ() & 15);
        return FidorialServer.getInstance().registries().blockStates().resolve(state);
    }

    /**
     * {@return the state of the chest at a position, empty when there is none or it is not loaded}
     */
    public static Optional<Chest> chestAt(final ServerWorld world, final BlockPosition position) {
        return stateAt(world, position) instanceof final Chest chest && behaviour(chest.key()) != null
                ? Optional.of(chest)
                : Optional.empty();
    }

    /**
     * {@return the slots of the chest at a position, empty when there is none, it has no block entity
     * or it is not loaded}
     */
    public static Optional<ChestInventory> inventoryAt(final ServerWorld world, final BlockPosition position) {
        if (chestAt(world, position).isEmpty() || world.blockEntityData(position) == null) {
            return Optional.empty();
        }
        return Optional.of(new BlockChestInventory(world, position));
    }

    /**
     * {@return where the other half of a large chest is: beside it, on the side its state points to,
     * a chest of its family facing the same way and pointing back; empty for a chest standing alone}
     *
     * @param world    the world of the chest
     * @param position where the chest is
     * @param chest    its state
     */
    public static Optional<BlockPosition> partner(final ServerWorld world, final BlockPosition position, final Chest chest) {
        final ChestBlock behaviour = behaviour(chest.key());
        final Optional<BlockFace> side = chest.partnerDirection();
        if (behaviour == null || side.isEmpty()) {
            return Optional.empty();
        }
        final BlockPosition other = position.relative(side.get());
        if (stateAt(world, other) instanceof final Chest partner && behaviour.joins(partner)
                && partner.getFacing() == chest.getFacing()
                && partner.partnerDirection().filter(side.get().opposite()::equals).isPresent()) {
            return Optional.of(other);
        }
        return Optional.empty();
    }

    /**
     * {@return the halves a chest opens: the chest alone, or the two halves of the large chest it
     * belongs to, the {@code type=right} one first}
     */
    public static List<BlockPosition> halves(final ServerWorld world, final BlockPosition position, final Chest chest) {
        final Optional<BlockPosition> partner = partner(world, position, chest);
        if (partner.isEmpty()) {
            return List.of(position);
        }
        return chest.getType() == Chest.Type.RIGHT ? List.of(position, partner.get()) : List.of(partner.get(), position);
    }

    /**
     * {@return what the window of the halves shows: the slots of a chest, or those of a large chest}
     *
     * @param halves one chest, or the two halves of a large chest in the order of {@link #halves}
     */
    public static Container contents(final ServerWorld world, final List<BlockPosition> halves) {
        final BlockChestInventory first = new BlockChestInventory(world, halves.getFirst());
        return halves.size() < 2 ? first : new LargeChestInventory(first, new BlockChestInventory(world, halves.get(1)));
    }

    /**
     * {@return the large chest the chest at a position is half of, empty while it stands alone}
     */
    public static Optional<DoubleChestInventory> largeChest(final ServerWorld world, final BlockPosition position) {
        return chestAt(world, position)
                .map(chest -> halves(world, position, chest))
                .filter(halves -> halves.size() == 2)
                .map(halves -> (DoubleChestInventory) contents(world, halves));
    }

    /**
     * {@return the title of a window: the name of a chest, of the first half of a large chest that has
     * one, or the default name}
     *
     * @param contents what the window shows, from {@link #contents}
     */
    public static Component title(final Container contents) {
        if (contents instanceof final DoubleChestInventory large) {
            return large.first().customName()
                    .or(() -> large.second().customName())
                    .orElseGet(() -> Component.translatable(DOUBLE_TITLE));
        }
        if (contents instanceof final ChestInventory chest) {
            return chest.customName().orElseGet(() -> Component.translatable(SINGLE_TITLE));
        }
        return Component.translatable(SINGLE_TITLE);
    }

    /**
     * {@return {@code true} if a solid block sits on the lid of the chest at a position, keeping it
     * shut}
     */
    public static boolean isBlockedAbove(final ServerWorld world, final BlockPosition position) {
        return BlockSupport.isConductor(stateAt(world, position.relative(BlockFace.UP)));
    }

    /**
     * {@return the level a comparator reads out of a container: {@code 0} when it is empty, then from
     * {@code 1} to {@code 15} as its slots fill up, each stack counting for how full it is}
     */
    public static int signal(final Container container) {
        float fullness = 0.0f;
        boolean any = false;
        for (final ItemStack stack : container.contents()) {
            if (!stack.isEmpty()) {
                fullness += stack.count() / (float) Math.max(1, ItemDefaults.maxStackSize(stack.id(), stack));
                any = true;
            }
        }
        if (!any) {
            return 0;
        }
        return Math.min(15, (int) Math.floor(fullness / container.size() * 14.0f) + 1);
    }

    /**
     * Tells the comparators reading a chest, and the other half of its large chest, that its
     * contents changed, on the thread of the region owning it. Several changes in a row tell them
     * once.
     *
     * @param world    the world of the chest
     * @param position where the chest is
     */
    public static void contentsChanged(final ServerWorld world, final BlockPosition position) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final Pending pending = new Pending(world.key(), position);
        if (!PENDING.add(pending)) {
            return;
        }
        final boolean scheduled = world.scheduler().execute(world.key(), position.chunk(), () -> {
            PENDING.remove(pending);
            final RedstoneWorld redstone = FidorialServer.getInstance().worlds().redstone().world(world);
            redstone.analogOutputChanged(position);
            chestAt(world, position).flatMap(chest -> partner(world, position, chest)).ifPresent(redstone::analogOutputChanged);
        });
        if (!scheduled) {
            PENDING.remove(pending);
        }
    }

    /**
     * Names the chest just placed at a position after the item that placed it, when that item was
     * renamed.
     */
    public static void nameFromItem(final ServerWorld world, final BlockPosition position, final ItemStack item) {
        final Component name = item.get(DataComponentTypes.CUSTOM_NAME);
        if (name != null) {
            inventoryAt(world, position).ifPresent(chest -> chest.setCustomName(name));
        }
    }

    private record Pending(Key world, BlockPosition position) {
    }
}
