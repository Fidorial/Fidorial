package fr.euphyllia.fidorial.server.world.block.farming;

import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;
import fr.fidorial.world.redstone.SupportType;

import java.util.List;

/**
 * The flower of a cactus: it stands on a block of the
 * {@code minecraft:support_override_cactus_flower} tag, the cactus that grew it to begin with, or
 * on any block whose top can hold something in its middle.
 */
public final class CactusFlowerBlock implements FarmingBehaviour {

    public static final CactusFlowerBlock INSTANCE = new CactusFlowerBlock();

    private CactusFlowerBlock() {
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        final BlockPosition below = position.relative(BlockFace.DOWN);
        return world.isTagged(below, BlockTypeTagKeys.SUPPORT_OVERRIDE_CACTUS_FLOWER)
                || BlockSupport.isFaceSturdy(world.blockAt(below), BlockFace.UP, SupportType.CENTER);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
