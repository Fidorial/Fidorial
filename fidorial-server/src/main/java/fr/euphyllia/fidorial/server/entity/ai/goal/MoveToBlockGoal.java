package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The base of the goals that send a mob onto a block it looks for around itself: every 10 to 20
 * seconds it searches the nearest block that fits, level by level and ring by ring, walks onto it,
 * and stays there for a minute or more. It gives up after a minute without reaching it, or as soon
 * as the block does not fit any more.
 */
public abstract class MoveToBlockGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE, Flag.JUMP);
    private static final int GIVE_UP_TICKS = 1200;
    private static final int STAY_TICKS = 1200;
    private static final int INTERVAL_TICKS = 200;
    private static final int REPATH_INTERVAL_TICKS = 40;
    private static final double ACCEPTED_DISTANCE = 1.0;

    protected final AbstractPathfinderMob mob;
    private final int priority;
    private final double speedModifier;
    private final int searchRange;
    private final int verticalSearchRange;

    /**
     * The block the mob goes onto.
     */
    protected @Nullable BlockPosition target;
    private int nextStartTicks;
    private int tryTicks;
    private int maxStayTicks;
    private boolean reachedTarget;

    /**
     * @param speedModifier       what the movement speed of the mob is multiplied by on its way
     * @param searchRange         how far the block may be along each horizontal axis
     * @param verticalSearchRange how far the block under the feet of the mob may be above or below
     */
    protected MoveToBlockGoal(final AbstractPathfinderMob mob, final int priority, final double speedModifier,
                              final int searchRange, final int verticalSearchRange) {
        this.mob = mob;
        this.priority = priority;
        this.speedModifier = speedModifier;
        this.searchRange = searchRange;
        this.verticalSearchRange = verticalSearchRange;
    }

    /**
     * {@return {@code true} if the mob may go onto a block}
     */
    protected abstract boolean isValidTarget(ServerWorld world, int x, int y, int z);

    @Override
    public final int priority() {
        return priority;
    }

    @Override
    public final Set<Flag> flags() {
        return FLAGS;
    }

    @Override
    public boolean canStart() {
        if (nextStartTicks > 0) {
            nextStartTicks--;
            return false;
        }
        nextStartTicks = INTERVAL_TICKS + ThreadLocalRandom.current().nextInt(INTERVAL_TICKS);
        return findNearestBlock();
    }

    @Override
    public boolean shouldContinue() {
        final BlockPosition block = target;
        return block != null && tryTicks >= -maxStayTicks && tryTicks <= GIVE_UP_TICKS
                && isValidTarget(mob.serverWorld(), block.blockX(), block.blockY(), block.blockZ());
    }

    @Override
    public void start() {
        walkToTarget();
        tryTicks = 0;
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        maxStayTicks = random.nextInt(random.nextInt(STAY_TICKS) + STAY_TICKS) + STAY_TICKS;
    }

    @Override
    public void stop() {
        mob.navigation().stop();
        target = null;
    }

    @Override
    public void tick() {
        final BlockPosition block = target;
        if (block == null) {
            return;
        }
        final Location at = mob.location();
        final double dx = block.blockX() + 0.5 - at.x();
        final double dy = block.blockY() + 1.5 - at.y();
        final double dz = block.blockZ() + 0.5 - at.z();
        if (dx * dx + dy * dy + dz * dz >= ACCEPTED_DISTANCE * ACCEPTED_DISTANCE) {
            reachedTarget = false;
            tryTicks++;
            if (tryTicks % REPATH_INTERVAL_TICKS == 0) {
                walkToTarget();
            }
        } else {
            reachedTarget = true;
            tryTicks--;
        }
    }

    /**
     * {@return {@code true} while the mob stands on the block it went to}
     */
    protected final boolean isReachedTarget() {
        return reachedTarget;
    }

    private void walkToTarget() {
        final BlockPosition block = target;
        if (block != null) {
            mob.navigation().moveTo(mob.location(), Position.block(block.blockX(), block.blockY() + 1, block.blockZ()),
                    mob.movementSpeed() * speedModifier);
        }
    }

    /**
     * Looks for the nearest block that fits: the level of the ground under the mob first, then one
     * level up, one down, and so on; on each level, the block under the mob then the rings around it.
     */
    private boolean findNearestBlock() {
        final ServerWorld world = mob.serverWorld();
        final Location at = mob.location();
        final int originX = (int) Math.floor(at.x());
        final int originY = (int) Math.floor(at.y());
        final int originZ = (int) Math.floor(at.z());
        for (int y = 0; y <= verticalSearchRange; y = y > 0 ? -y : 1 - y) {
            for (int r = 0; r < searchRange; r++) {
                for (int x = 0; x <= r; x = x > 0 ? -x : 1 - x) {
                    for (int z = x < r && x > -r ? r : 0; z <= r; z = z > 0 ? -z : 1 - z) {
                        if (isValidTarget(world, originX + x, originY + y - 1, originZ + z)) {
                            target = Position.block(originX + x, originY + y - 1, originZ + z);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
