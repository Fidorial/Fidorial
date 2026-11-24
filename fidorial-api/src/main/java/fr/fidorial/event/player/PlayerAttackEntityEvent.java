package fr.fidorial.event.player;

import fr.fidorial.entity.Entity;
import fr.fidorial.event.Cancellable;
import fr.fidorial.item.ItemStack;

/**
 * Fired when a player swings at an entity, before any damage is applied.
 *
 * @since 0.1.0
 */
public interface PlayerAttackEntityEvent extends PlayerEvent, Cancellable {

    /**
     * {@return the entity being hit}
     *
     * @since 0.1.0
     */
    Entity target();

    /**
     * {@return the item the player swings, {@link ItemStack#EMPTY} for a bare hand}
     *
     * @since 0.1.0
     */
    ItemStack weapon();

    /**
     * {@return the damage about to be dealt, in half-hearts, cooldown, critical hit, smash attack
     * and enchantments already applied}
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
     * {@return the extra knockback strength the swing adds to the push every hit gives}
     *
     * @since 0.1.0
     */
    double knockback();

    /**
     * Changes the extra knockback of the swing.
     *
     * @param knockback the extra knockback to apply instead; {@code 0} keeps only the push every hit gives
     * @since 0.1.0
     */
    void setKnockback(double knockback);

    /**
     * {@return the attack cooldown charge of this swing, between {@code 0} and {@code 1}}
     *
     * @since 0.1.0
     */
    float attackStrength();

    /**
     * {@return {@code true} when this swing landed as a critical hit}
     *
     * @since 0.1.0
     */
    boolean isCritical();

    /**
     * {@return {@code true} when this swing is a smash attack of a mace}
     *
     * @since 0.1.0
     */
    boolean isSmash();

    /**
     * {@return {@code true} when this swing also hits the entities around the target}
     *
     * @since 0.1.0
     */
    boolean isSweeping();

    /**
     * Keeps the swing on its target, or lets it sweep.
     *
     * @param sweeping {@code false} to keep the hit on the primary target only
     * @since 0.1.0
     */
    void setSweeping(boolean sweeping);
}
