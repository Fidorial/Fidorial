package fr.euphyllia.fidorial.server.world.block.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * Nether wart: it grows on soil of the {@code minecraft:supports_nether_wart} tag, in any light,
 * one stage every tenth random tick on average, and no fertilizer hurries it.
 */
public final class NetherWartBlock implements FarmingBehaviour {

    public static final NetherWartBlock INSTANCE = new NetherWartBlock();

    private static final int GROWTH_ODDS = 10;

    private NetherWartBlock() {
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (world.random().nextInt(GROWTH_ODDS) == 0) {
            world.grow(position, Farming.withAge(state, Farming.age(state) + 1));
        }
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return world.isTagged(position.relative(BlockFace.DOWN), BlockTypeTagKeys.SUPPORTS_NETHER_WART);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
