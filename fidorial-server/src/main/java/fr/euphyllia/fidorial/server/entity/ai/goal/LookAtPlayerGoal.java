package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes a mob look at the closest player it sees for two to four seconds, as vanilla's
 * {@code LookAtPlayerGoal}. Only looking is needed, so the mob keeps walking meanwhile.
 */
public final class LookAtPlayerGoal implements Goal {

    /**
     * Vanilla rolls 2% every other tick.
     */
    public static final float DEFAULT_CHANCE = 0.01f;

    private static final int MIN_LOOK_TICKS = 40;
    private static final int EXTRA_LOOK_TICKS = 40;
    private static final double SNEAKING_VISIBILITY = 0.8;
    private static final double MIN_VISIBLE_RANGE = 2.0;
    private static final Set<Flag> FLAGS = Set.of(Flag.LOOK);

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double range;
    private final float chance;

    private @Nullable ServerPlayer lookAt;
    private int lookTicks;

    public LookAtPlayerGoal(final AbstractPathfinderMob mob, final int priority, final double range) {
        this(mob, priority, range, DEFAULT_CHANCE);
    }

    /**
     * @param range  how far the mob notices players, in blocks
     * @param chance the chance per tick that the mob starts to look at a player
     */
    public LookAtPlayerGoal(final AbstractPathfinderMob mob, final int priority, final double range, final float chance) {
        this.mob = mob;
        this.priority = priority;
        this.range = range;
        this.chance = chance;
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
        if (ThreadLocalRandom.current().nextFloat() >= chance) {
            return false;
        }
        lookAt = nearestVisiblePlayer();
        return lookAt != null;
    }

    @Override
    public boolean shouldContinue() {
        final ServerPlayer player = lookAt;
        return player != null && isAlive(player)
                && mob.distanceSqTo(player) <= range * range
                && lookTicks > 0;
    }

    @Override
    public void start() {
        lookTicks = MIN_LOOK_TICKS + ThreadLocalRandom.current().nextInt(EXTRA_LOOK_TICKS);
    }

    @Override
    public void stop() {
        lookAt = null;
    }

    @Override
    public void tick() {
        final ServerPlayer player = lookAt;
        if (player != null && isAlive(player)) {
            final Location at = player.location();
            mob.setLookTarget(at.x(), at.y() + player.eyeHeight(), at.z());
            lookTicks--;
        }
    }

    private boolean isAlive(final ServerPlayer player) {
        return !player.isRemoved() && !player.isDead() && player.world().equals(mob.world());
    }

    private @Nullable ServerPlayer nearestVisiblePlayer() {
        final List<ServerPlayer> players = mob.server().players().online();
        final Location self = mob.location();
        final double eyeY = self.y() + mob.eyeHeight();
        ServerPlayer best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int i = 0, size = players.size(); i < size; i++) {
            final ServerPlayer player = players.get(i);
            if (!isAlive(player) || player.gameMode() == GameMode.SPECTATOR) {
                continue;
            }
            // as in Vanilla, a sneaking player is noticed from 80% of the range, never less than 2 blocks
            final double visible = Math.max(range * (player.isSneaking() ? SNEAKING_VISIBILITY : 1.0), MIN_VISIBLE_RANGE);
            if (mob.distanceSqTo(player) > visible * visible) {
                continue;
            }
            final Location at = player.location();
            final double dx = at.x() - self.x();
            final double dy = at.y() - eyeY;
            final double dz = at.z() - self.z();
            final double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < bestDistSq && mob.hasLineOfSightTo(player)) {
                bestDistSq = distSq;
                best = player;
            }
        }
        return best;
    }
}
