package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.ai.RandomPositions;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Position;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/**
 * Makes a mob wander, as vanilla's {@code WaterAvoidingRandomStrollGoal}: once every
 * {@value #INTERVAL} ticks on average it walks to a random spot on land, up to 10 blocks away, or
 * 15 to get out of the water it is in. A mob no player has been near for a while stays put.
 */
public final class RandomStrollGoal implements Goal {

    private static final int INTERVAL = 120;
    private static final int IDLE_LIMIT_TICKS = 100;
    private static final int HORIZONTAL = 10;
    private static final int HORIZONTAL_FROM_WATER = 15;
    private static final int VERTICAL = 7;
    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE);

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final DoubleSupplier speed;
    private final Terrain terrain;
    private @Nullable Position destination;

    /**
     * @param speed the movement speed of the mob times vanilla's speed modifier
     */
    public RandomStrollGoal(final AbstractPathfinderMob mob, final int priority, final double speed) {
        this(mob, priority, () -> speed);
    }

    /**
     * @param speed the movement speed of the mob times vanilla's speed modifier, asked for at each
     *              stroll: for a mob whose speed changes, as a zombie turning into a baby
     */
    public RandomStrollGoal(final AbstractPathfinderMob mob, final int priority, final DoubleSupplier speed) {
        this(mob, priority, speed, Terrain.LAND);
    }

    /**
     * @param speed   the movement speed of the mob times the speed modifier of the goal, asked for
     *                at each stroll
     * @param terrain where the mob strolls to
     */
    public RandomStrollGoal(final AbstractPathfinderMob mob, final int priority, final DoubleSupplier speed,
                            final Terrain terrain) {
        this.mob = mob;
        this.priority = priority;
        this.speed = speed;
        this.terrain = terrain;
    }

    /**
     * Where a mob strolls to.
     */
    public enum Terrain {
        /**
         * Solid ground out of the water; a mob in the water looks for the shore from further away.
         */
        LAND,
        /**
         * Any solid ground, under water or not.
         */
        ANY_GROUND,
        /**
         * The ground or anywhere in the water, for a mob at home in it.
         */
        WATER_TOO
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
        if (mob.noActionTime() >= IDLE_LIMIT_TICKS) {
            return false;
        }
        if (ThreadLocalRandom.current().nextInt(INTERVAL) != 0) {
            return false;
        }
        destination = switch (terrain) {
            case WATER_TOO -> RandomPositions.amphibious(mob, HORIZONTAL, VERTICAL);
            case ANY_GROUND -> RandomPositions.any(mob, HORIZONTAL, VERTICAL);
            case LAND -> mob.isInWater()
                    ? RandomPositions.land(mob, HORIZONTAL_FROM_WATER, VERTICAL)
                    : RandomPositions.land(mob, HORIZONTAL, VERTICAL);
        };
        return destination != null;
    }

    @Override
    public boolean shouldContinue() {
        return mob.navigation().isNavigating();
    }

    @Override
    public void start() {
        final Position target = destination;
        if (target != null) {
            mob.navigation().moveTo(mob.location(), target, speed.getAsDouble());
        }
    }

    @Override
    public void stop() {
        destination = null;
        mob.navigation().stop();
    }
}
