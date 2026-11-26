package fr.euphyllia.fidorial.server.inventory;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.chest.ChestBlocks;
import fr.fidorial.inventory.ChestInventory;
import fr.fidorial.inventory.DoubleChestInventory;
import fr.fidorial.math.BlockPosition;

import java.util.Optional;

/**
 * The 27 slots of one chest block, kept in its block entity as {@link BlockEntityInventory} tells.
 */
public final class BlockChestInventory extends BlockEntityInventory implements ChestInventory {

    public BlockChestInventory(final ServerWorld world, final BlockPosition position) {
        super(world, position, "chest", SIZE, SIZE);
    }

    @Override
    public Optional<DoubleChestInventory> doubleChest() {
        return ChestBlocks.largeChest(world(), position());
    }

    @Override
    protected void contentsChanged() {
        ChestBlocks.contentsChanged(world(), position());
    }
}
