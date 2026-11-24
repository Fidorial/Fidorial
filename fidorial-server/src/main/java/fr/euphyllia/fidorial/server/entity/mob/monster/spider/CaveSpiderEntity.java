package fr.euphyllia.fidorial.server.entity.mob.monster.spider;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.MobEffectKeys;

/**
 * The cave spider: a smaller spider with 12 health, whose bite poisons for 7 seconds on normal
 * difficulty and 15 on hard. It spawns without any of the random traits of a spider.
 */
public final class CaveSpiderEntity extends SpiderEntity {

    public static final float MAX_HEALTH = 12f;

    private static final double HALF_WIDTH = 0.35;
    private static final double HEIGHT = 0.5;
    private static final double EYE_HEIGHT = 0.45;

    private static final int NORMAL_POISON_TICKS = 7 * 20;
    private static final int HARD_POISON_TICKS = 15 * 20;

    public CaveSpiderEntity(final int entityId, final Location location) {
        super(entityId, EntityTypes.CAVE_SPIDER, location, MAX_HEALTH);
    }

    @Override
    public double height() {
        return HEIGHT;
    }

    @Override
    protected double halfWidth() {
        return HALF_WIDTH;
    }

    @Override
    public double eyeHeight() {
        return EYE_HEIGHT;
    }

    @Override
    public boolean attack(final AbstractLivingEntity target) {
        if (!super.attack(target)) {
            return false;
        }
        final int duration = switch (difficulty()) {
            case LocalDifficulty.NORMAL -> NORMAL_POISON_TICKS;
            case LocalDifficulty.HARD -> HARD_POISON_TICKS;
            default -> 0;
        };
        if (duration > 0) {
            target.addEffect(server().registries().effects().builder(MobEffectKeys.POISON).duration(duration).build());
        }
        return true;
    }
}
