package fr.euphyllia.fidorial.testplugin.effect;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;

/**
 * {@code fidorialtest:bleeding}: one damage every two seconds, twice as often per level.
 */
final class BleedingBehaviour extends PeriodicEffectBehaviour {

    private static final int BASE_INTERVAL = 40;
    private static final int MIN_INTERVAL = 10;
    private static final float DAMAGE = 1f;

    @Override
    protected int interval(final int amplifier) {
        return Math.max(MIN_INTERVAL, BASE_INTERVAL >> amplifier);
    }

    @Override
    protected void pulse(final LivingEntity entity, final MobEffectInstance effect) {
        entity.damage(DamageSource.generic(), DAMAGE);
    }
}
