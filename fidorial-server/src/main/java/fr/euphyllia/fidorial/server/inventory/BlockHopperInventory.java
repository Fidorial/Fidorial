package fr.euphyllia.fidorial.server.inventory;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.hopper.HopperBlock;
import fr.euphyllia.fidorial.server.world.block.hopper.Hoppers;
import fr.fidorial.inventory.HopperInventory;
import fr.fidorial.math.BlockPosition;

/**
 * The {@value #SIZE} slots of a hopper, kept in its block entity as {@link BlockEntityInventory}
 * tells. Its cooldown is not saved: {@link Hoppers} keeps it while the hopper ticks.
 */
public final class BlockHopperInventory extends BlockEntityInventory implements HopperInventory {

    public BlockHopperInventory(final ServerWorld world, final BlockPosition position) {
        super(world, position, "hopper", SIZE, SIZE);
    }

    @Override
    public int transferCooldown() {
        return Hoppers.INSTANCE.cooldown(world(), position());
    }

    @Override
    public void setTransferCooldown(final int ticks) {
        Preconditions.checkArgument(ticks >= 0, "The hopper at %s cannot wait %s game ticks, a cooldown is never negative",
                position(), ticks);
        Hoppers.INSTANCE.setCooldown(world(), position(), ticks);
    }

    @Override
    protected void contentsChanged() {
        HopperBlock.contentsChanged(world(), position());
    }
}
