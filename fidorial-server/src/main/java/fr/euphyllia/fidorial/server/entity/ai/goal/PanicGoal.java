package fr.euphyllia.fidorial.server.entity.ai.goal;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.RandomPositions;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.data.DamageType;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Makes a mob run away from what just hurt it, as vanilla's {@code PanicGoal}: it runs to a random
 * spot nearby, or to water if it burns, and calms down once it got there. It starts again as long
 * as the mob remembers the damage, a couple of seconds.
 */
public final class PanicGoal implements Goal {

    private static final int FLEE_HORIZONTAL = 5;
    private static final int FLEE_VERTICAL = 4;
    private static final int WATER_HORIZONTAL = 5;
    private static final int WATER_VERTICAL = 1;
    private static final Set<Flag> FLAGS = Set.of(Flag.MOVE);

    private final AbstractPathfinderMob mob;
    private final int priority;
    private final double speed;

    private final TagKey<DamageType> panicCauses;
    private @Nullable Position destination;
    private boolean running;

    /**
     * @param speed       the movement speed of the mob times vanilla's speed modifier
     * @param panicCauses the damage types the mob runs away from
     */
    public PanicGoal(final AbstractPathfinderMob mob, final int priority, final double speed,
                     final TagKey<DamageType> panicCauses) {
        this.mob = mob;
        this.priority = priority;
        this.speed = speed;
        this.panicCauses = panicCauses;
    }

    public boolean isPanicking() {
        return running;
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
        if (!shouldPanic()) {
            return false;
        }
        Position target = null;
        if (mob.fireTicks() > 0) {
            target = nearestWater();
        }
        if (target == null) {
            target = RandomPositions.any(mob, FLEE_HORIZONTAL, FLEE_VERTICAL);
        }
        destination = target;
        return target != null;
    }

    private boolean shouldPanic() {
        final DamageSource source = mob.lastDamageSource();
        return source != null
                && mob.server().registries().registry(RegistryKey.DAMAGE_TYPE).isTagged(source.type(), panicCauses);
    }

    @Override
    public boolean shouldContinue() {
        return mob.navigation().isNavigating();
    }

    @Override
    public void start() {
        running = true;
        final Position target = destination;
        if (target != null) {
            mob.navigation().moveTo(mob.location(), target, speed);
        }
    }

    @Override
    public void stop() {
        running = false;
        destination = null;
    }

    /**
     * {@return the closest water a burning mob can run to, {@code null} if there is none around or if
     * the mob is stuck in a block}
     */
    private @Nullable Position nearestWater() {
        final ServerWorld world = mob.serverWorld();
        final Location from = mob.location();
        final int originX = (int) Math.floor(from.x());
        final int originY = (int) Math.floor(from.y());
        final int originZ = (int) Math.floor(from.z());

        final BlockState feet = BlockView.blockAt(world, originX, originY, originZ);
        if (feet == null || !BlockView.isPassable(feet)) {
            return null;
        }
        Position best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (int dx = -WATER_HORIZONTAL; dx <= WATER_HORIZONTAL; dx++) {
            for (int dz = -WATER_HORIZONTAL; dz <= WATER_HORIZONTAL; dz++) {
                for (int dy = -WATER_VERTICAL; dy <= WATER_VERTICAL; dy++) {
                    final int distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                    if (distance >= bestDistance) {
                        continue;
                    }
                    final BlockState state = BlockView.blockAt(world, originX + dx, originY + dy, originZ + dz);
                    if (state != null && BlockView.isWater(state)) {
                        bestDistance = distance;
                        best = Position.block(originX + dx, originY + dy, originZ + dz);
                    }
                }
            }
        }
        return best;
    }

}
