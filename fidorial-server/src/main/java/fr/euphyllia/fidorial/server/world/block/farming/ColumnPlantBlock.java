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
 * A plant growing as a column of its own block: sugar cane and cactus.
 *
 * <p>Only the block at the top grows: each random tick ages it, and once it reached its last stage
 * a new block grows above it, as long as the column stands lower than {@value #MAX_HEIGHT} blocks
 * and there is air above. A block of the column stands on another one, or on its soil.</p>
 */
public final class ColumnPlantBlock implements FarmingBehaviour {

    public static final ColumnPlantBlock SUGAR_CANE = new ColumnPlantBlock(BlockTypeKeys.SUGAR_CANE, false);
    public static final ColumnPlantBlock CACTUS = new ColumnPlantBlock(BlockTypeKeys.CACTUS, true);

    /**
     * How tall a column grows on its own.
     */
    public static final int MAX_HEIGHT = 3;

    /**
     * The stage at which a cactus may flower.
     */
    private static final int FLOWERING_AGE = 8;
    private static final double FLOWER_CHANCE = 0.1;
    private static final double TALL_FLOWER_CHANCE = 0.25;

    private final TypedKey<BlockType> key;
    private final boolean cactus;

    private ColumnPlantBlock(final TypedKey<BlockType> key, final boolean cactus) {
        this.key = key;
        this.cactus = cactus;
    }

    public TypedKey<BlockType> key() {
        return key;
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return true;
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        final BlockPosition above = position.relative(BlockFace.UP);
        final BlockData room = world.blockAt(above);
        if (room == null || !room.isAir()) {
            return;
        }
        int height = 1;
        while (height <= MAX_HEIGHT && Farming.is(world.blockAt(position.relative(BlockFace.DOWN, height)), key)) {
            height++;
        }
        final int age = Farming.age(state);
        if (cactus && age == FLOWERING_AGE
                && world.random().nextDouble() <= (height >= MAX_HEIGHT ? TALL_FLOWER_CHANCE : FLOWER_CHANCE)) {
            world.grow(position, above, Farming.defaultState(BlockTypeKeys.CACTUS_FLOWER), false);
        } else if (age >= Farming.maxAge(state) && height < MAX_HEIGHT) {
            if (world.grow(position, above, Farming.defaultState(key), false)) {
                world.setBlock(position, Farming.withAge(state, 0));
            }
            return;
        }
        if (age < Farming.maxAge(state)) {
            world.setBlock(position, Farming.withAge(state, age + 1));
        }
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        final BlockPosition below = position.relative(BlockFace.DOWN);
        final boolean onItself = Farming.is(world.blockAt(below), key);
        return cactus ? cactusCanSurvive(world, position, below, onItself) : caneCanSurvive(world, below, onItself);
    }

    /**
     * Sugar cane stands on soil of the {@code minecraft:supports_sugar_cane} tag with water, or a
     * block of the {@code minecraft:supports_sugar_cane_adjacently} tag, right beside that soil.
     */
    private static boolean caneCanSurvive(final FarmingWorld world, final BlockPosition below, final boolean onItself) {
        if (onItself) {
            return true;
        }
        if (!world.isTagged(below, BlockTypeTagKeys.SUPPORTS_SUGAR_CANE)) {
            return false;
        }
        for (final BlockFace side : Farming.HORIZONTAL) {
            final BlockPosition beside = below.relative(side);
            if (world.hasWater(beside) || world.isTagged(beside, BlockTypeTagKeys.SUPPORTS_SUGAR_CANE_ADJACENTLY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A cactus stands on soil of the {@code minecraft:supports_cactus} tag, with nothing solid nor
     * any lava touching its sides, and no fluid above it.
     */
    private static boolean cactusCanSurvive(final FarmingWorld world, final BlockPosition position,
                                            final BlockPosition below, final boolean onItself) {
        for (final BlockFace side : Farming.HORIZONTAL) {
            final BlockData beside = world.blockAt(position.relative(side));
            if (Farming.isSolid(beside) || Farming.is(beside, BlockTypeKeys.LAVA)) {
                return false;
            }
        }
        final BlockData above = world.blockAt(position.relative(BlockFace.UP));
        if (above != null && above.isFluid()) {
            return false;
        }
        return onItself || world.isTagged(below, BlockTypeTagKeys.SUPPORTS_CACTUS);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
