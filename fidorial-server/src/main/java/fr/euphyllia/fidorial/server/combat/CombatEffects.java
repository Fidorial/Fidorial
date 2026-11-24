package fr.euphyllia.fidorial.server.combat;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.AttributeKeys;
import fr.fidorial.registry.keys.EntityTypeTagKeys;
import fr.fidorial.registry.keys.MobEffectKeys;

/**
 * What the effects that take part in a fight do to a living entity over time: Regeneration heals,
 * Poison and Wither hurt, Absorption gives yellow hearts, Instant Health and Instant Damage act
 * at once.
 *
 * <p>Strength, Weakness, Haste and Mining Fatigue change attributes, and Resistance and Fire
 * Resistance soften hits: {@link CombatAttributes} and the {@link CombatEngine} read those.</p>
 */
public final class CombatEffects {

    private static final int REGENERATION_INTERVAL = 50;
    private static final int POISON_INTERVAL = 25;
    private static final int WITHER_INTERVAL = 40;
    private static final float ABSORPTION_PER_LEVEL = 4.0f;
    private static final float INSTANT_AMOUNT = 4.0f;
    private static final float INSTANT_DAMAGE_AMOUNT = 6.0f;

    private CombatEffects() {
        throw new UnsupportedOperationException("CombatEffects cannot be instantiated.");
    }

    /**
     * Runs what an effect does once applied.
     *
     * @param entity the entity
     * @param effect the effect, as just applied
     */
    public static void onApplied(final AbstractLivingEntity entity, final MobEffectInstance effect) {
        if (effect.type().equals(MobEffectKeys.ABSORPTION)) {
            entity.setAbsorptionAmount(Math.max(entity.absorptionAmount(), ABSORPTION_PER_LEVEL * (effect.amplifier() + 1)));
        } else if (effect.type().equals(MobEffectKeys.INSTANT_HEALTH)) {
            instant(entity, effect.amplifier(), true);
        } else if (effect.type().equals(MobEffectKeys.INSTANT_DAMAGE)) {
            instant(entity, effect.amplifier(), false);
        }
    }

    /**
     * Runs what an effect does once removed.
     *
     * @param entity the entity
     * @param effect the effect, as it was
     */
    public static void onRemoved(final AbstractLivingEntity entity, final MobEffectInstance effect) {
        if (effect.type().equals(MobEffectKeys.ABSORPTION)) {
            entity.setAbsorptionAmount(Math.min(entity.absorptionAmount(),
                    (float) entity.attributeValue(AttributeKeys.MAX_ABSORPTION)));
        }
    }

    /**
     * Runs what an effect does each tick.
     *
     * @param entity the entity
     * @param effect the effect, before its duration counts down
     * @param age    the ticks the entity has lived, which time an effect that never ends
     */
    public static void onTick(final AbstractLivingEntity entity, final MobEffectInstance effect, final long age) {
        if (effect.type().equals(MobEffectKeys.REGENERATION)) {
            if (pulses(effect, REGENERATION_INTERVAL, age) && entity.health() < entity.maxHealth()) {
                entity.heal(1.0f);
                CombatEngine.syncHealth(entity);
            }
        } else if (effect.type().equals(MobEffectKeys.POISON)) {
            if (pulses(effect, POISON_INTERVAL, age) && entity.health() > 1.0f && !isUndead(entity)) {
                entity.damage(DamageSourceFactoryImpl.INSTANCE.magic(), 1.0f);
            }
        } else if (effect.type().equals(MobEffectKeys.WITHER)) {
            if (pulses(effect, WITHER_INTERVAL, age)) {
                entity.damage(DamageSourceFactoryImpl.INSTANCE.wither(), 1.0f);
            }
        }
    }

    /**
     * {@return {@code true} on the ticks an effect that acts at intervals acts}: every
     * {@code interval} ticks at level I, twice as often per level above
     */
    private static boolean pulses(final MobEffectInstance effect, final int interval, final long age) {
        final int every = interval >> Math.min(effect.amplifier(), 30);
        if (every <= 0) {
            return true;
        }
        return effect.duration() < 0 ? age % every == 0 : effect.duration() % every == 0;
    }

    private static void instant(final AbstractLivingEntity entity, final int amplifier, final boolean healing) {
        // undead mobs are healed by what hurts the living, and hurt by what heals them
        final boolean heals = healing != isUndead(entity);
        if (heals) {
            entity.heal(INSTANT_AMOUNT * (1 << Math.min(amplifier, 30)));
            CombatEngine.syncHealth(entity);
        } else {
            entity.damage(DamageSourceFactoryImpl.INSTANCE.magic(), INSTANT_DAMAGE_AMOUNT * (1 << Math.min(amplifier, 30)));
        }
    }

    private static boolean isUndead(final AbstractLivingEntity entity) {
        return entity.server().registries().registry(RegistryKey.ENTITY_TYPE)
                .isTagged(entity.type().key(), EntityTypeTagKeys.UNDEAD);
    }
}
