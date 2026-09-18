package fr.fidorial.event.player;

import fr.fidorial.annotation.ThreadContract;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.Player;
import fr.fidorial.event.Cancellable;

/**
 * Fired when a player swings at an entity, before any damage is applied.
 *
 * @since 0.1.0
 */
public final class PlayerAttackEntityEvent implements PlayerEvent, Cancellable {

    private final Player player;
    private final Entity target;
    private final float attackStrength;
    private final boolean critical;

    private float damage;
    private double knockback;
    private boolean sweeping;
    private boolean cancelled;

    public PlayerAttackEntityEvent(
            final Player player,
            final Entity target,
            final float damage,
            final double knockback,
            final float attackStrength,
            final boolean critical,
            final boolean sweeping) {
        this.player = player;
        this.target = target;
        this.damage = damage;
        this.knockback = knockback;
        this.attackStrength = attackStrength;
        this.critical = critical;
        this.sweeping = sweeping;
    }

    @Override
    @ThreadContract("get -> any; modify -> owner")
    public Player player() {
        return player;
    }

    @ThreadContract("get -> any; modify -> owner")
    public Entity target() {
        return target;
    }

    /**
     * @return the damage about to be dealt, in half-hearts, critical already applied
     */
    public float damage() {
        return damage;
    }

    /**
     * @param damage the damage to deal instead; {@code <= 0} makes the hit harmless
     */
    public void setDamage(final float damage) {
        this.damage = damage;
    }

    /**
     * @return the horizontal knockback strength about to be applied
     */
    public double knockback() {
        return knockback;
    }

    /**
     * @param knockback the knockback strength to apply instead; {@code 0} disables it
     */
    public void setKnockback(final double knockback) {
        this.knockback = knockback;
    }

    /**
     * @return the attack cooldown charge of this swing, between {@code 0.2} and {@code 1}
     */
    public float attackStrength() {
        return attackStrength;
    }

    /**
     * @return {@code true} when this swing landed as a critical hit
     */
    public boolean isCritical() {
        return critical;
    }

    /**
     * @return {@code true} when this swing also hits the entities around the target
     */
    public boolean isSweeping() {
        return sweeping;
    }

    /**
     * @param sweeping {@code false} to keep the hit on the primary target only
     */
    public void setSweeping(final boolean sweeping) {
        this.sweeping = sweeping;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
}
