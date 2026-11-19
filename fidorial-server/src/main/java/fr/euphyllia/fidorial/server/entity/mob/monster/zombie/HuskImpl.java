package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.mob.monster.Husk;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

/**
 * The husk, the zombie of the deserts: the sun does not burn it, its bare hands leave its target
 * hungry, and water or powder snow turns it into a zombie. It spawns under the open sky.
 */
public final class HuskImpl extends ZombieImpl implements Husk {

    public static final float MAX_HEALTH = 20f;

    private static final double BABY_EYE_HEIGHT = 0.825;
    private static final int HUNGER_TICKS_PER_DIFFICULTY = 140;

    public HuskImpl(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    /**
     * @param spawned {@code true} for a husk that spawns, with the random traits of a zombie that does
     */
    public HuskImpl(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.HUSK, location, spawned);
    }

    /**
     * {@return {@code true} if husks may spawn on a block}: on solid ground in the dark, under the
     * open sky
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkSurfaceMonsterSpawnRules(world, x, y, z);
    }

    @Override
    public double eyeHeight() {
        return isBaby() ? BABY_EYE_HEIGHT : super.eyeHeight();
    }

    /**
     * A husk that hits with its bare hands leaves its target hungry: 7 seconds for each whole point
     * of the difficulty where it stands.
     */
    @Override
    protected void onAttackLanded(final AbstractLivingEntity target) {
        final int duration = HUNGER_TICKS_PER_DIFFICULTY * (int) localDifficulty().effective();
        if (duration <= 0 || target.isDead()) {
            return;
        }
        target.addEffect(server().registries().effects().builder(MobEffectKeys.HUNGER).duration(duration).build());
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.HUSK_HURT;
    }
    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.HUSK_DEATH;
    }

    @Override
    protected Sound.Type stepSound() {
        return SoundEvents.HUSK_STEP;
    }

    @Override
    protected ZombieImpl drownedForm(final int entityId, final Location location) {
        return new ZombieImpl(entityId, location, false);
    }

    @Override
    protected Sound.Type drownedSound() {
        return SoundEvents.HUSK_CONVERTED_TO_ZOMBIE;
    }

    @Override
    protected ZombieImpl frozenForm(final int entityId, final Location location) {
        return new ZombieImpl(entityId, location, false);
    }

    @Override
    protected Sound.Type frozenSound() {
        return SoundEvents.HUSK_CONVERTED_TO_ZOMBIE;
    }
}
