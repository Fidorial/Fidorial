package fr.euphyllia.fidorial.testplugin.farming;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.plant.CropBlock;

import java.util.random.RandomGenerator;

/**
 * An invasive crop (mint): once ripe, it keeps planting itself on any free spot
 * of soil next to it.
 */
public final class SpreadingCropBlock extends CropBlock {

    private static final BlockFace[] HORIZONTAL = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

    private final double spreadChance;

    /**
     * @param builder      the crop settings; its soils are also where it may spread
     * @param spreadChance how likely a ripe plant is to try spreading on a random tick
     */
    public SpreadingCropBlock(final CropBlock.Builder builder, final double spreadChance) {
        super(builder);
        this.spreadChance = spreadChance;
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
        if (random.nextDouble() >= spreadChance || world.lightLevel(pos) < DEFAULT_MIN_LIGHT) {
            return;
        }
        final BlockPos target = pos.relative(HORIZONTAL[random.nextInt(HORIZONTAL.length)]);
        if (!world.blockAt(target).isAir()) {
            return;
        }
        if (!soils().contains(world.blockAt(target.offset(0, -1, 0)).key())) {
            return;
        }
        world.setBlock(target, withAge(data, 0));
    }
}
