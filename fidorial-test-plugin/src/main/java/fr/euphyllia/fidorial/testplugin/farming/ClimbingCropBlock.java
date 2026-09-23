package fr.euphyllia.fidorial.testplugin.farming;

import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.plant.CropBlock;
import fr.fidorial.world.block.plant.ForwardingCropBlock;

import java.util.random.RandomGenerator;

/**
 * A crop that grows upwards (hemp). Each segment ripens like a crop, then a ripe
 * top segment starts a new one above it, up to a maximum height. Only the bottom
 * segment needs soil: the others stand on the segment below, so breaking the
 * bottom brings the whole stalk down.
 */
public final class ClimbingCropBlock extends ForwardingCropBlock {

    private static final int MAX_SCAN = 64;

    private final int maxHeight;
    private final double climbChance;

    /**
     * @param crop        the regular crop underneath; its soils are what the bottom segment needs
     * @param maxHeight   how many segments the stalk may reach
     * @param climbChance how likely a ripe top segment is to start a new one on a random tick
     */
    public ClimbingCropBlock(final CropBlock crop, final int maxHeight, final double climbChance) {
        super(crop);
        this.maxHeight = maxHeight;
        this.climbChance = climbChance;
    }

    @Override
    public boolean canSurvive(final BlockData data, final BlockAccess world, final BlockPos pos) {
        final BlockData below = world.blockAt(pos.offset(0, -1, 0));
        return below.key().equals(key()) || soils().contains(below.key());
    }

    @Override
    public boolean isRandomlyTicking(final BlockData data) {
        return true;
    }

    @Override
    public void randomTick(final BlockData data, final BlockAccess world, final BlockPos pos, final RandomGenerator random) {
        if (!isRipe(data)) {
            super.randomTick(data, world, pos, random);
            return;
        }
        if (random.nextDouble() >= climbChance) {
            return;
        }
        final BlockPos above = pos.offset(0, 1, 0);
        if (!world.blockAt(above).isAir() || height(world, pos) >= maxHeight) {
            return;
        }
        world.setBlock(above, withAge(data, 0));
    }

    private int height(final BlockAccess world, final BlockPos top) {
        int height = 1;
        BlockPos cursor = top.offset(0, -1, 0);
        while (height < MAX_SCAN && world.blockAt(cursor).key().equals(key())) {
            height++;
            cursor = cursor.offset(0, -1, 0);
        }
        return height;
    }
}
