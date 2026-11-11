package fr.euphyllia.fidorial.server.world.block.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * What a plant bears and that only has to be picked up: a melon, a pumpkin, a cactus flower. It
 * neither grows nor needs anything, and gives its harvest when broken.
 */
public final class ProduceBlock implements FarmingBehaviour {

    public static final ProduceBlock INSTANCE = new ProduceBlock();

    private ProduceBlock() {
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
