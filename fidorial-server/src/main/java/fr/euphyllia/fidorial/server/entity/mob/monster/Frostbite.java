package fr.euphyllia.fidorial.server.entity.mob.monster;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.goal.RangedAttackGoal;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.entity.projectile.IceBall;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public final class Frostbite extends Zombie {

    private static final int FREEZING_TICKS_PER_DIFFICULTY = 140;

    private static final double RANGED_MIN_RANGE = 4.0;
    private static final double RANGED_MAX_RANGE = 12.0;
    private static final int THROW_COOLDOWN_TICKS = 40;

    private static final int MIN_ICE_BALLS = 4;
    private static final int MAX_ICE_BALLS = 16;

    private static final double THROW_SPEED = 1.6;
    private static final double TARGET_AIM_HEIGHT = 0.9;

    private int iceBalls;

    public Frostbite(final int entityId, final Location location) {
        super(entityId, EntityTypes.FROSTBITE, location, SpawnData.roll());
        this.iceBalls = ThreadLocalRandom.current().nextInt(MIN_ICE_BALLS, MAX_ICE_BALLS + 1);
        goals.add(new RangedAttackGoal(this, 0, RANGED_MIN_RANGE, RANGED_MAX_RANGE, THROW_COOLDOWN_TICKS,
                this::hasIceBalls, this::throwIceBall));
    }

    public int iceBalls() {
        return iceBalls;
    }

    public void setIceBalls(final int iceBalls) {
        this.iceBalls = Math.max(0, iceBalls);
    }

    public boolean hasIceBalls() {
        return iceBalls > 0;
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.FROSTBITE_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.FROSTBITE_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.FROSTBITE_DEATH;
    }

    @Override
    protected Sound.Type stepSound() {
        return SoundEvents.FROSTBITE_STEP;
    }

    @Override
    protected EntityType waterConversionType() {
        return EntityTypes.ZOMBIE;
    }

    @Override
    protected Sound.Type waterConversionSound() {
        return SoundEvents.FROSTBITE_CONVERTED_TO_ZOMBIE;
    }

    @Override
    protected @Nullable EntityType powderSnowConversionType() {
        return null; // already frozen
    }

    @Override
    protected void onAttackLanded(final ServerPlayer target) {
        final int duration = FREEZING_TICKS_PER_DIFFICULTY * difficulty();
        if (duration <= 0 || target.isDead()) {
            return;
        }
        target.addEffect(server().effects().builder(MobEffectKeys.FREEZING).duration(duration).build());
    }

    private void throwIceBall(final ServerPlayer target) {
        if (iceBalls <= 0) {
            return;
        }
        iceBalls--;

        final Location self = location();
        final Location from = Location.of(world(), self.x(), self.y() + height() * 0.85, self.z(), yaw(), pitch());
        final Location aim = target.location();
        final IceBall ball = IceBall.thrownAt(server().entityIds().allocate(), from, this,
                aim.x(), aim.y() + TARGET_AIM_HEIGHT, aim.z(), THROW_SPEED);
        playSound(SoundEvents.ICE_BALL_THROW, 1.0f, 0.4f / (ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f));
        server().spawnEntity(ball);
    }
}
