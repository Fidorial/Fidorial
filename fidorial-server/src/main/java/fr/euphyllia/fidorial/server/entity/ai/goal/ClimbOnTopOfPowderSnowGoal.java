package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;

import java.util.Set;

/**
 * Makes a mob that walks on powder snow jump every tick while it is caught in some, until it stands
 * on top: as long as the block above it is powder snow too, or one it can go through.
 */
public final class ClimbOnTopOfPowderSnowGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.JUMP);

    private final AbstractMovingMob mob;
    private final int priority;

    public ClimbOnTopOfPowderSnowGoal(final AbstractMovingMob mob, final int priority) {
        this.mob = mob;
        this.priority = priority;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public Set<Flag> flags() {
        return FLAGS;
    }

    @Override
    public boolean canStart() {
        if (!mob.walksOnPowderSnow() || !mob.isInPowderSnow()) {
            return false;
        }
        final Location at = mob.location();
        final BlockState above = BlockView.blockAt(mob.serverWorld(),
                (int) Math.floor(at.x()), (int) Math.floor(at.y()) + 1, (int) Math.floor(at.z()));
        return above != null
                && (above.name().equals(BlockTypeKeys.POWDER_SNOW) || BlockCollision.shape(above).isEmpty());
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        mob.requestJump();
    }
}
