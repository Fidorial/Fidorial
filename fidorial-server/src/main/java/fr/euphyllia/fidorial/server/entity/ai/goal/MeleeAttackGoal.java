package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes a mob walk up to its target and hit it: it heads for where it last saw the target, looks
 * for a new path every 4 to 10 ticks once the target has moved a block (less often from afar),
 * and strikes at most once per attack interval, when the target is
 * {@linkplain AbstractPathfinderMob#isWithinMeleeRange within reach} and in sight.
 *
 * <p>Vanilla checks whether to start only once a second; so does this goal.</p>
 */
public class MeleeAttackGoal implements Goal {

    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE, Flag.LOOK);
    private static final int DEFAULT_ATTACK_INTERVAL = 20;
    private static final int START_CHECK_INTERVAL = 20;
    private static final float LOOK_SPEED = 30.0f;
    private static final double FAR_SQ = 1024.0;
    private static final double AWAY_SQ = 256.0;

    protected final AbstractPathfinderMob mob;
    private final int priority;
    private final double speedModifier;
    private final boolean followsUnseenTarget;
    private final int attackInterval;

    private double pathedX;
    private double pathedY;
    private double pathedZ;
    private boolean pathed;
    private int ticksUntilNextPath;
    private int ticksUntilNextAttack;
    private int ticksUntilStartCheck;

    /**
     * @param speedModifier       what the {@linkplain AbstractPathfinderMob#movementSpeed() movement
     *                            speed} of the mob is multiplied by while it attacks
     * @param followsUnseenTarget {@code true} if the mob keeps heading for a target it does not see
     */
    public MeleeAttackGoal(final AbstractPathfinderMob mob, final int priority, final double speedModifier,
                           final boolean followsUnseenTarget) {
        this(mob, priority, speedModifier, followsUnseenTarget, DEFAULT_ATTACK_INTERVAL);
    }

    public MeleeAttackGoal(final AbstractPathfinderMob mob, final int priority, final double speedModifier,
                           final boolean followsUnseenTarget, final int attackInterval) {
        this.mob = mob;
        this.priority = priority;
        this.speedModifier = speedModifier;
        this.followsUnseenTarget = followsUnseenTarget;
        this.attackInterval = attackInterval;
    }

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
        if (ticksUntilStartCheck > 0) {
            ticksUntilStartCheck--;
            return false;
        }
        ticksUntilStartCheck = START_CHECK_INTERVAL - 1;
        final AbstractLivingEntity target = mob.target();
        return target != null && !target.isDead() && !target.isRemoved();
    }

    @Override
    public boolean shouldContinue() {
        final AbstractLivingEntity target = mob.target();
        if (target == null || target.isDead() || target.isRemoved()) {
            return false;
        }
        if (!followsUnseenTarget) {
            return mob.navigation().isNavigating() || mob.isWithinMeleeRange(target);
        }
        return !isOutOfTheGame(target);
    }

    @Override
    public void start() {
        final AbstractLivingEntity target = mob.target();
        if (target != null) {
            walkTo(target);
        }
        mob.setAggressive(true);
        ticksUntilNextPath = 0;
        ticksUntilNextAttack = 0;
    }

    @Override
    public void stop() {
        if (isOutOfTheGame(mob.target())) {
            mob.setTarget(null);
        }
        mob.setAggressive(false);
        mob.navigation().stop();
        pathed = false;
    }

    @Override
    public void tick() {
        final AbstractLivingEntity target = mob.target();
        if (target == null) {
            return;
        }

        mob.setLookTarget(target, LOOK_SPEED, LOOK_SPEED);

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        ticksUntilNextPath = Math.max(ticksUntilNextPath - 1, 0);
        if (touches(target)) {
            // nothing pushes entities apart yet: the mob stops against its target instead of
            // walking into it
            mob.navigation().stop();
            pathed = false;
        } else if ((followsUnseenTarget || mob.hasLineOfSightTo(target))
                && ticksUntilNextPath <= 0
                && (!pathed || movedSincePathedSq(target) >= 1.0 || random.nextFloat() < 0.05f)) {
            ticksUntilNextPath = 4 + random.nextInt(7);
            final double distanceSq = mob.distanceSqTo(target);
            if (distanceSq > FAR_SQ) {
                ticksUntilNextPath += 10;
            } else if (distanceSq > AWAY_SQ) {
                ticksUntilNextPath += 5;
            }
            if (!walkTo(target)) {
                ticksUntilNextPath += 15;
            }
        }

        ticksUntilNextAttack = Math.max(ticksUntilNextAttack - 1, 0);
        if (ticksUntilNextAttack <= 0 && mob.isWithinMeleeRange(target) && mob.hasLineOfSightTo(target)) {
            ticksUntilNextAttack = attackInterval;
            mob.swingMainHand();
            mob.attack(target);
        }
    }

    /**
     * @return {@code false} if the mob has no way to its target for now
     */
    private boolean walkTo(final AbstractLivingEntity target) {
        final Location to = target.location();
        pathedX = to.x();
        pathedY = to.y();
        pathedZ = to.z();
        pathed = true;
        mob.navigation().moveTo(mob.location(), to, mob.movementSpeed() * speedModifier);
        return mob.navigation().isNavigating() || !mob.navigation().lastPathFailed();
    }

    private double movedSincePathedSq(final AbstractLivingEntity target) {
        final Location at = target.location();
        final double dx = at.x() - pathedX;
        final double dy = at.y() - pathedY;
        final double dz = at.z() - pathedZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private boolean touches(final AbstractLivingEntity target) {
        final Location self = mob.location();
        final Location other = target.location();
        final double reach = mob.width() / 2.0 + AbstractPathfinderMob.halfWidthOf(target);
        return Math.abs(self.x() - other.x()) < reach
                && Math.abs(self.z() - other.z()) < reach
                && Math.abs(self.y() - other.y()) < 1.0;
    }

    private static boolean isOutOfTheGame(final @Nullable AbstractLivingEntity target) {
        return target instanceof final ServerPlayer player
                && (player.gameMode() == GameMode.CREATIVE || player.gameMode() == GameMode.SPECTATOR);
    }

    /**
     * {@return in how many ticks the mob may strike again}
     */
    protected final int ticksUntilNextAttack() {
        return ticksUntilNextAttack;
    }

    /**
     * {@return how many ticks the mob waits between two blows}
     */
    protected final int attackInterval() {
        return attackInterval;
    }
}
