package fr.euphyllia.fidorial.server.world.block.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * The stem of a melon or a pumpkin: it grows like a crop, and once fully grown bears a fruit on
 * one of the four blocks beside it, then stays attached to it.
 *
 * <p>The fruit needs air to grow into and soil of its own tag under it. The side is picked at
 * random on each try, so that a stem hemmed in on three sides still finds the fourth.</p>
 */
public final class StemBlock implements FarmingBehaviour {

    public static final StemBlock MELON = new StemBlock(BlockTypeKeys.MELON_STEM, BlockTypeKeys.ATTACHED_MELON_STEM,
            BlockTypeKeys.MELON, BlockTypeTagKeys.SUPPORTS_MELON_STEM, BlockTypeTagKeys.SUPPORTS_MELON_STEM_FRUIT);
    public static final StemBlock PUMPKIN = new StemBlock(BlockTypeKeys.PUMPKIN_STEM, BlockTypeKeys.ATTACHED_PUMPKIN_STEM,
            BlockTypeKeys.PUMPKIN, BlockTypeTagKeys.SUPPORTS_PUMPKIN_STEM, BlockTypeTagKeys.SUPPORTS_PUMPKIN_STEM_FRUIT);

    private final TypedKey<BlockType> key;
    private final TypedKey<BlockType> attached;
    private final TypedKey<BlockType> fruit;
    private final TagKey<BlockType> soil;
    private final TagKey<BlockType> fruitSoil;

    private StemBlock(final TypedKey<BlockType> key, final TypedKey<BlockType> attached, final TypedKey<BlockType> fruit,
                      final TagKey<BlockType> soil, final TagKey<BlockType> fruitSoil) {
        this.key = key;
        this.attached = attached;
        this.fruit = fruit;
        this.soil = soil;
        this.fruitSoil = fruitSoil;
    }

    public TypedKey<BlockType> key() {
        return key;
    }

    /**
     * {@return the stem this one turns into while it holds a fruit}
     */
    public TypedKey<BlockType> attached() {
        return attached;
    }

    /**
     * {@return the fruit this stem bears}
     */
    public TypedKey<BlockType> fruit() {
        return fruit;
    }

    TagKey<BlockType> soil() {
        return soil;
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return true;
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (world.lightLevel(position) < Farming.GROWTH_LIGHT || !Farming.rollsGrowth(world, position)) {
            return;
        }
        final int age = Farming.age(state);
        if (age < Farming.maxAge(state)) {
            world.grow(position, Farming.withAge(state, age + 1));
            return;
        }
        final BlockFace side = Farming.HORIZONTAL[world.random().nextInt(Farming.HORIZONTAL.length)];
        final BlockPosition target = position.relative(side);
        final BlockData occupied = world.blockAt(target);
        if (occupied == null || !occupied.isAir() || !world.isTagged(target.relative(BlockFace.DOWN), fruitSoil)) {
            return;
        }
        if (world.grow(position, target, Farming.defaultState(fruit), false)
                && Farming.defaultState(attached) instanceof final Directional holding) {
            world.setBlock(position, holding.withFacing(side));
        }
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return world.isTagged(position.relative(BlockFace.DOWN), soil);
    }

    @Override
    public boolean canFertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public void fertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        final int age = Math.min(Farming.maxAge(state), Farming.age(state) + Farming.fertilizedStages(world.random()));
        world.grow(position, position, Farming.withAge(state, age), true);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
