package fr.fidorial.event.entity;

import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a raised shield is about to block a hit. Cancelling it lets the hit through as if the
 * shield were lowered.
 *
 * @since 0.1.0
 */
public interface EntityBlockAttackEvent extends Event, Cancellable {

    /**
     * {@return the entity holding the shield}
     *
     * @since 0.1.0
     */
    LivingEntity entity();

    /**
     * {@return the hit being blocked}
     *
     * @since 0.1.0
     */
    DamageSource source();

    /**
     * {@return the item blocking the hit}
     *
     * @since 0.1.0
     */
    ItemStack shield();

    /**
     * {@return the damage of the hit, in half-hearts, before the shield}
     *
     * @since 0.1.0
     */
    float damage();

    /**
     * {@return how much of the damage the shield takes away, in half-hearts}
     *
     * @since 0.1.0
     */
    float blockedDamage();

    /**
     * Changes how much of the damage the shield takes away.
     *
     * @param blockedDamage the damage to block, clamped between {@code 0} and {@link #damage()}
     * @since 0.1.0
     */
    void setBlockedDamage(float blockedDamage);
}
