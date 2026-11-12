package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAnimal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Makes an animal in love join a partner and breed, as vanilla's {@code BreedGoal}: the closest
 * animal of its kind in love within 8 blocks, which it walks up to; after three seconds together,
 * less than 3 blocks apart, the baby is born.
 */
public final class BreedGoal implements Goal {

    private static final double PARTNER_RANGE = 8.0;
    private static final double BREED_DISTANCE_SQ = 3.0 * 3.0;
    /**
     * Vanilla counts 60 every other tick before it gives up, and breeds from 30 on.
     */
    private static final int GIVE_UP_TICKS = 120;
    private static final int BREED_TICKS = 60;
    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE, Flag.LOOK);

    private final AbstractAnimal animal;
    private final int priority;
    private final double speed;

    private @Nullable AbstractAnimal partner;
    private int loveTime;

    /**
     * @param speed the movement speed of the animal times vanilla's speed modifier
     */
    public BreedGoal(final AbstractAnimal animal, final int priority, final double speed) {
        this.animal = animal;
        this.priority = priority;
        this.speed = speed;
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
        if (!animal.isInLove()) {
            return false;
        }
        partner = findFreePartner();
        return partner != null;
    }

    @Override
    public boolean shouldContinue() {
        final AbstractAnimal current = partner;
        return current != null
                && !current.isRemoved() && !current.isDead()
                && current.isInLove()
                && loveTime < GIVE_UP_TICKS
                && !current.isPanicking();
    }

    @Override
    public void stop() {
        partner = null;
        loveTime = 0;
    }

    @Override
    public void tick() {
        final AbstractAnimal current = partner;
        if (current == null) {
            return;
        }
        animal.setLookTarget(current, AbstractMovingMob.HEAD_TURN_SPEED, AbstractMovingMob.MAX_HEAD_PITCH);
        animal.navigation().moveTo(animal.location(), current.location(), speed);
        loveTime++;
        if (loveTime >= BREED_TICKS && animal.distanceSqTo(current) < BREED_DISTANCE_SQ) {
            animal.spawnChildFromBreeding(current);
        }
    }

    private @Nullable AbstractAnimal findFreePartner() {
        final Location self = animal.location();
        final AbstractAnimal[] best = new AbstractAnimal[1];
        final double[] bestDistSq = {Double.MAX_VALUE};
        animal.serverWorld().entityManager().forEachNear(self.chunk(), PARTNER_RANGE, (final AbstractEntity entity) -> {
            if (!(entity instanceof final AbstractAnimal other)
                    || other.isRemoved() || other.isDead()
                    || !animal.canMate(other)
                    || other.isPanicking()) {
                return;
            }
            final double distSq = animal.distanceSqTo(other);
            if (distSq <= PARTNER_RANGE * PARTNER_RANGE && distSq < bestDistSq[0]) {
                bestDistSq[0] = distSq;
                best[0] = other;
            }
        });
        return best[0];
    }
}
