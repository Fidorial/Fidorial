package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.ai.goal.MoveToBlockGoal;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;

/**
 * Brings a drowned ashore at night: in the water near the surface of the sea, it looks for a block
 * to stand on in the open air, up to 8 blocks away, and walks onto it.
 */
final class DrownedGoToBeachGoal extends MoveToBlockGoal {

    private static final int SEARCH_RANGE = 8;
    private static final int VERTICAL_SEARCH_RANGE = 2;
    private static final int DEPTH_BELOW_SEA_LEVEL = 3;

    private final Drowned drowned;

    DrownedGoToBeachGoal(final Drowned drowned, final int priority, final double speedModifier) {
        super(drowned, priority, speedModifier, SEARCH_RANGE, VERTICAL_SEARCH_RANGE);
        this.drowned = drowned;
    }

    @Override
    public boolean canStart() {
        final ServerWorld world = drowned.serverWorld();
        return super.canStart()
                && !Brightness.isDay(world)
                && drowned.isInWater()
                && drowned.location().y() >= world.seaLevel() - DEPTH_BELOW_SEA_LEVEL;
    }

    @Override
    protected boolean isValidTarget(final ServerWorld world, final int x, final int y, final int z) {
        return isAir(world, x, y + 1, z) && isAir(world, x, y + 2, z) && BlockView.isSolidGround(world, x, y, z);
    }

    private static boolean isAir(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && state.isAir();
    }

    @Override
    public void start() {
        drowned.setSearchingForLand(false);
        super.start();
    }
}
