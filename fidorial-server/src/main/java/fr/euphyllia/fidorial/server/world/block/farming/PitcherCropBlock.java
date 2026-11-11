package fr.euphyllia.fidorial.server.world.block.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * The pitcher crop: planted as one block, it grows a second one above itself from its
 * {@value #TALL_AGE}th stage on, both halves then ripening together.
 *
 * <p>The lower half does the growing and holds the harvest; the upper half only stands as long as
 * the lower one does, and the lower one, once tall, as long as its upper half does.</p>
 */
public final class PitcherCropBlock implements FarmingBehaviour {

    public static final PitcherCropBlock INSTANCE = new PitcherCropBlock();

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.PITCHER_CROP;

    /**
     * The stage from which the crop is two blocks tall.
     */
    private static final int TALL_AGE = 3;

    private static final String HALF = "half";
    private static final String UPPER = "upper";

    private PitcherCropBlock() {
    }

    private static boolean isUpper(final BlockData state) {
        return UPPER.equals(state.get(HALF));
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return !isUpper(state) && Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (Farming.rollsGrowth(world, position) && canGrow(world, position, state)) {
            grow(world, position, state, false);
        }
    }

    private static boolean canGrow(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        final int age = Farming.age(state);
        if (isUpper(state) || age >= Farming.maxAge(state) || !Farming.hasSurvivalLight(world, position)) {
            return false;
        }
        if (age + 1 < TALL_AGE) {
            return true;
        }
        final BlockData above = world.blockAt(position.relative(BlockFace.UP));
        return above != null && (above.isAir() || Farming.is(above, KEY));
    }

    private static void grow(final FarmingWorld world, final BlockPosition position, final BlockData state,
                             final boolean fertilized) {
        final int age = Farming.age(state) + 1;
        if (!world.grow(position, position, Farming.withAge(state, age), fertilized) || age < TALL_AGE) {
            return;
        }
        world.setBlock(position.relative(BlockFace.UP), Farming.withAge(state, age).with(HALF, UPPER));
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (isUpper(state)) {
            final BlockData below = world.blockAt(position.relative(BlockFace.DOWN));
            return below != null && Farming.is(below, KEY) && !isUpper(below);
        }
        if (!world.isTagged(position.relative(BlockFace.DOWN), BlockTypeTagKeys.SUPPORTS_CROPS)
                || !Farming.hasSurvivalLight(world, position)) {
            return false;
        }
        if (Farming.age(state) < TALL_AGE) {
            return true;
        }
        final BlockData above = world.blockAt(position.relative(BlockFace.UP));
        return above != null && Farming.is(above, KEY) && isUpper(above);
    }

    @Override
    public BlockData updateShape(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        // the upper half follows the stage of the lower one, whatever made it grow
        if (isUpper(state) && direction == BlockFace.DOWN && Farming.is(neighbor, KEY)
                && Farming.age(neighbor) != Farming.age(state)) {
            return Farming.withAge(state, Farming.age(neighbor));
        }
        return state;
    }

    @Override
    public boolean canFertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return canGrow(world, position, state);
    }

    @Override
    public void fertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        grow(world, position, state, true);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
