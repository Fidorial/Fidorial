package fr.euphyllia.fidorial.testplugin.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Ageable;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * A plant of the plugin's own, written against the API alone: the chorus flower, which the server
 * leaves inert, turned into a crop of the End.
 *
 * <p>It is planted on end stone, ripens in any light one stage every fourth random tick on
 * average, takes fertilizer one stage at a time, and gives chorus fruits once ripe.</p>
 */
public final class ChorusSprout implements FarmingBehaviour {

    public static final TypedKey<BlockType> BLOCK = BlockTypeKeys.CHORUS_FLOWER;
    public static final TypedKey<BlockType> SOIL = BlockTypeKeys.END_STONE;

    private static final int GROWTH_ODDS = 4;

    private static int age(final BlockData state) {
        return state instanceof final Ageable ageable ? ageable.getAge() : 0;
    }

    private static boolean isRipe(final BlockData state) {
        return !(state instanceof final Ageable ageable) || ageable.getAge() >= ageable.getMaximumAge();
    }

    private static BlockData older(final BlockData state) {
        return state instanceof final Ageable ageable ? ageable.withAge(ageable.getAge() + 1) : state;
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return !isRipe(state);
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (world.random().nextInt(GROWTH_ODDS) == 0) {
            world.grow(position, older(state));
        }
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        final BlockData below = world.blockAt(position.relative(BlockFace.DOWN));
        return below != null && below.key().equals(SOIL);
    }

    @Override
    public boolean canFertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return !isRipe(state);
    }

    @Override
    public void fertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        world.grow(position, position, older(state), true);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        if (!isRipe(state)) {
            return List.of(ItemStack.of(TestFarming.SEED));
        }
        return List.of(ItemStack.of(ItemKeys.CHORUS_FRUIT, 1 + world.random().nextInt(3)), ItemStack.of(TestFarming.SEED));
    }

    /**
     * {@return the growth stage of a sprout, for the tests}
     */
    public static int stage(final BlockData state) {
        return age(state);
    }
}
