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
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A crop ripening in stages on farmland: wheat, carrots, potatoes, beetroots and the seedling of
 * the torchflower.
 *
 * <p>It needs soil of the {@code minecraft:supports_crops} tag under it, and either a view of the
 * sky or a light of {@value Farming#SURVIVAL_LIGHT} to stay planted. With a light of
 * {@value Farming#GROWTH_LIGHT} it gains one stage on a random tick, the more often the better its
 * field {@linkplain FarmingWorld#growthSpeed feeds} it.</p>
 */
public final class CropBlock implements FarmingBehaviour {

    public static final CropBlock WHEAT = new CropBlock(BlockTypeKeys.WHEAT, false, null);
    public static final CropBlock CARROTS = new CropBlock(BlockTypeKeys.CARROTS, false, null);
    public static final CropBlock POTATOES = new CropBlock(BlockTypeKeys.POTATOES, false, null);
    public static final CropBlock BEETROOTS = new CropBlock(BlockTypeKeys.BEETROOTS, true, null);
    public static final CropBlock TORCHFLOWER = new CropBlock(BlockTypeKeys.TORCHFLOWER_CROP, true, BlockTypeKeys.TORCHFLOWER);

    public static final List<CropBlock> ALL = List.of(WHEAT, CARROTS, POTATOES, BEETROOTS, TORCHFLOWER);

    private final TypedKey<BlockType> key;
    private final boolean slow;
    private final @Nullable TypedKey<BlockType> ripe;

    /**
     * @param slow {@code true} for a crop skipping one growth in three, and gaining at most one
     *             stage from a fertilizer
     * @param ripe the block the crop turns into past its last stage, {@code null} for a crop that
     *             stays itself once ripe
     */
    private CropBlock(final TypedKey<BlockType> key, final boolean slow,
                      final @Nullable TypedKey<BlockType> ripe) {
        this.key = key;
        this.slow = slow;
        this.ripe = ripe;
    }

    public TypedKey<BlockType> key() {
        return key;
    }

    /**
     * {@return {@code true} if the crop has stages left to grow through}
     */
    private boolean isGrowing(final BlockData state) {
        return ripe != null || Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return isGrowing(state);
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (slow && world.random().nextInt(3) == 0) {
            return;
        }
        if (world.lightLevel(position) >= Farming.GROWTH_LIGHT && Farming.rollsGrowth(world, position)) {
            world.grow(position, grown(state, 1));
        }
    }

    /**
     * {@return a crop a few stages further, the block it ripens into once past the last one}
     */
    private BlockData grown(final BlockData state, final int stages) {
        final int age = Farming.age(state) + stages;
        final int last = Farming.maxAge(state);
        if (age > last && ripe != null) {
            return Farming.defaultState(ripe);
        }
        return Farming.withAge(state, Math.min(age, last));
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return world.isTagged(position.relative(BlockFace.DOWN), BlockTypeTagKeys.SUPPORTS_CROPS)
                && Farming.hasSurvivalLight(world, position);
    }

    @Override
    public boolean canFertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return isGrowing(state);
    }

    @Override
    public void fertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        final int stages;
        if (ripe != null) {
            stages = 1;
        } else if (slow) {
            stages = Farming.fertilizedStages(world.random()) / 3;
        } else {
            stages = Farming.fertilizedStages(world.random());
        }
        if (stages > 0) {
            world.grow(position, position, grown(state, stages), true);
        }
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
