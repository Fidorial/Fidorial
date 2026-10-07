package fr.euphyllia.fidorial.testplugin.effect;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;

final class RegenerationBehaviour extends PeriodicEffectBehaviour {

    private static final int BASE_INTERVAL = 50;
    private static final float HEAL = 1f;

    @Override
    protected int interval(final int amplifier) {
        return BASE_INTERVAL >> amplifier;
    }

    @Override
    protected void pulse(final LivingEntity entity, final MobEffectInstance effect) {
        if (entity.health() < entity.maxHealth()) {
            entity.heal(HEAL);
        }
    }
}
