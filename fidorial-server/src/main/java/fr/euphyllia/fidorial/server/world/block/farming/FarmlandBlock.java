package fr.euphyllia.fidorial.server.world.block.farming;

import fr.euphyllia.fidorial.server.world.farming.FarmingEvents;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.data.type.Farmland;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * Farmland: tilled soil crops grow on.
 *
 * <p>On a random tick it soaks up the water standing within {@value #WATER_RANGE} blocks of it,
 * level with it or one block higher, or the rain falling on it. Without either it dries out one
 * step at a time, and once dry turns back into dirt unless something is planted on it. A solid
 * block put on top of it, or something heavy landing on it, turns it back into dirt at once.</p>
 */
public final class FarmlandBlock implements FarmingBehaviour {

    public static final FarmlandBlock INSTANCE = new FarmlandBlock();

    /**
     * How far water wets farmland, in blocks, along both horizontal axes.
     */
    public static final int WATER_RANGE = 4;

    /**
     * The fall from which landing on farmland may trample it, in blocks: the chance grows with
     * every block fallen beyond it.
     */
    private static final double TRAMPLE_THRESHOLD = 0.5;

    private FarmlandBlock() {
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return true;
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final Farmland farmland)) {
            return;
        }
        final int moisture = farmland.getMoisture();
        final int wet = farmland.getMaximumMoisture();
        if (isWatered(world, position) || world.isRainingAt(position.relative(BlockFace.UP))) {
            if (moisture < wet) {
                moisten(world, position, state, farmland.withMoisture(wet));
            }
        } else if (moisture > 0) {
            moisten(world, position, state, farmland.withMoisture(moisture - 1));
        } else if (!world.isTagged(position.relative(BlockFace.UP), BlockTypeTagKeys.MAINTAINS_FARMLAND)) {
            final BlockData reverted = FarmingEvents.revert(world.world(), position, state, dirt());
            if (reverted != null) {
                world.setBlock(position, reverted);
            }
        }
    }

    private static void moisten(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                final BlockData moistened) {
        if (FarmingEvents.moisture(world.world(), position, state, moistened)) {
            world.setBlock(position, moistened);
        }
    }

    private static boolean isWatered(final FarmingWorld world, final BlockPosition position) {
        for (int dy = 0; dy <= 1; dy++) {
            for (int dx = -WATER_RANGE; dx <= WATER_RANGE; dx++) {
                for (int dz = -WATER_RANGE; dz <= WATER_RANGE; dz++) {
                    if (world.hasWater(position.offset(dx, dy, dz))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public BlockData updateShape(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (direction != BlockFace.UP || !Farming.isSolid(neighbor)
                || world.isTagged(position.relative(BlockFace.UP), BlockTypeTagKeys.MAINTAINS_FARMLAND)) {
            return state;
        }
        final BlockData reverted = FarmingEvents.revert(world.world(), position, state, dirt());
        return reverted == null ? state : reverted;
    }

    @Override
    public void entityLanded(final FarmingWorld world, final BlockPosition position, final BlockData state,
                             final Entity entity, final double fallDistance) {
        if (!(entity instanceof LivingEntity) || world.random().nextFloat() >= fallDistance - TRAMPLE_THRESHOLD) {
            return;
        }
        if (!(entity instanceof Player) && !world.world().gameRules().getBoolean(GameRuleKeys.MOB_GRIEFING)) {
            return;
        }
        final BlockData reverted = FarmingEvents.trample(world.world(), position, state, dirt(), entity, fallDistance);
        if (reverted != null) {
            world.setBlock(position, reverted);
        }
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }

    private static BlockData dirt() {
        return Farming.defaultState(BlockTypeKeys.DIRT);
    }
}
