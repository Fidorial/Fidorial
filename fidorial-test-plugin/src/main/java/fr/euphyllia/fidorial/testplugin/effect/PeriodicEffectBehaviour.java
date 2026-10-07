package fr.euphyllia.fidorial.testplugin.effect;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectBehaviour;
import fr.fidorial.entity.effect.MobEffectInstance;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

abstract class PeriodicEffectBehaviour implements MobEffectBehaviour {

    private final Map<UUID, Integer> ticks = new ConcurrentHashMap<>();

    protected abstract int interval(int amplifier);

    protected abstract void pulse(LivingEntity entity, MobEffectInstance effect);

    @Override
    public final void onTick(final LivingEntity entity, final MobEffectInstance effect) {
        final int elapsed = ticks.merge(entity.uuid(), 1, Integer::sum);
        final int interval = interval(effect.amplifier());
        if (interval <= 0 || elapsed % interval == 0) {
            pulse(entity, effect);
        }
    }

    @Override
    public void onRemoved(final LivingEntity entity, final MobEffectInstance effect) {
        ticks.remove(entity.uuid());
    }
}
