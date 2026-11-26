package fr.euphyllia.fidorial.server.world.block;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.inventory.BlockEntityInventory;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.inventory.BlockInventory;
import fr.fidorial.inventory.Container;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.redstone.RedstoneWorld;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * What the blocks keeping slots in their block entity share, chests and barrels: how full a
 * comparator reads them, telling their comparators they changed, spilling them when they go, and
 * the name they take from a renamed item.
 */
public final class ContainerBlocks {

    /**
     * The blocks whose comparators are about to be told their contents changed.
     */
    private static final Set<Pending> PENDING = ConcurrentHashMap.newKeySet();

    private ContainerBlocks() {
        throw new UnsupportedOperationException("ContainerBlocks cannot be instantiated.");
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
     * {@return the slots a block keeps at a position, a chest or a barrel; empty for any other block,
     * or when its chunk is not loaded}
     */
    public static Optional<? extends BlockInventory> inventoryAt(final ServerWorld world, final BlockPosition position) {
        final Optional<? extends BlockInventory> chest = world.chestAt(position);
        return chest.isPresent() ? chest : world.barrelAt(position);
    }

    /**
     * {@return the level a comparator reads out of a container: {@code 0} when it is empty, then from
     * {@code 1} to {@code 15} as its slots fill up, each stack counting for how full it is}
     */
    public static int signal(final Container container) {
        Preconditions.checkArgument(container != null, "A comparator reads the level of a container, not of null");
        if (container.size() == 0) {
            return 0;
        }
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
     * Tells the comparators reading a block that its contents changed, on the thread of the region
     * owning it. Several changes in a row tell them once.
     *
     * @param world    the world of the block
     * @param position where the block is
     * @param tell     what to tell, given the redstone of the world: at least that the analog output
     *                 of {@code position} changed
     */
    public static void contentsChanged(final ServerWorld world, final BlockPosition position, final Consumer<RedstoneWorld> tell) {
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final Pending pending = new Pending(world.key(), position);
        if (!PENDING.add(pending)) {
            return;
        }
        final boolean scheduled = world.scheduler().execute(world.key(), position.chunk(), () -> {
            PENDING.remove(pending);
            tell.accept(FidorialServer.getInstance().worlds().redstone().world(world));
        });
        if (!scheduled) {
            PENDING.remove(pending);
        }
    }

    /**
     * Spills on the ground, where the block stands, every item its block entity keeps, those of the
     * slots it no longer shows included, its loot rolled first when it still had a table. The slots
     * are emptied first, so that the windows still open on the block do not hand the same items out
     * a second time.
     *
     * @param inventory the slots of the block being removed
     */
    public static void spill(final BlockEntityInventory inventory) {
        Preconditions.checkArgument(inventory != null, "Only the slots of a block can be spilled, not null");
        if (!FidorialServer.isInitialized()) {
            return;
        }
        final ItemStack[] contents = inventory.storedContents();
        inventory.clearStored();
        final FidorialServer server = FidorialServer.getInstance();
        final BlockPosition position = inventory.position();
        for (final ItemStack stack : contents) {
            if (!stack.isEmpty()) {
                server.entities().dropNaturally(inventory.world(),
                        position.blockX() + 0.5, position.blockY() + 0.5, position.blockZ() + 0.5, stack);
            }
        }
    }

    /**
     * Names the block just placed at a position after the item that placed it, when that item was
     * renamed and the block keeps slots.
     */
    public static void nameFromItem(final ServerWorld world, final BlockPosition position, final ItemStack item) {
        final Component name = item.get(DataComponentTypes.CUSTOM_NAME);
        if (name != null) {
            inventoryAt(world, position).ifPresent(inventory -> inventory.setCustomName(name));
        }
    }

    private record Pending(Key world, BlockPosition position) {
    }
}
