package fr.fidorial.event.entity;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import org.jspecify.annotations.Nullable;

/**
 * Fired before an entity takes damage, once invulnerability frames have been cleared but before
 * the shield, armor, Resistance, enchantments, absorption and knockback are applied.
 *
 * @since 0.1.0
 */
public interface EntityDamageEvent extends Event, Cancellable {

    /**
     * {@return the entity about to be hurt}
     *
     * @since 0.1.0
     */
    LivingEntity entity();

    /**
     * {@return the origin of the hit, carrying the damage type and the entities involved}
     *
     * @since 0.1.0
     */
    DamageSource source();

    /**
     * {@return the entity that caused the damage, or {@code null} for environmental damage}
     *
     * @since 0.1.0
     */
    @Nullable Entity damager();

    /**
     * {@return the damage about to be dealt, in half-hearts, before any reduction}
     *
     * @since 0.1.0
     */
    float damage();

    /**
     * Changes the damage about to be dealt.
     *
     * @param damage the damage to deal instead; {@code <= 0} makes the hit harmless
     * @since 0.1.0
     */
    void setDamage(float damage);

    /**
     * {@return the horizontal knockback strength about to be applied}
     *
     * @since 0.1.0
     */
    double knockback();

    /**
     * Changes the knockback about to be applied.
     *
     * @param knockback the knockback strength to apply instead; {@code 0} disables it
     * @since 0.1.0
     */
    void setKnockback(double knockback);
}
