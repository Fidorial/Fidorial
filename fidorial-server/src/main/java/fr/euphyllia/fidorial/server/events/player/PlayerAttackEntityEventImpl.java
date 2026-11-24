package fr.euphyllia.fidorial.server.events.player;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.event.player.PlayerAttackEntityEvent;
import fr.fidorial.item.ItemStack;

public final class PlayerAttackEntityEventImpl implements PlayerAttackEntityEvent {

    private final Player player;
    private final Entity target;
    private final ItemStack weapon;
    private final float attackStrength;
    private final boolean critical;
    private final boolean smash;

    private float damage;
    private double knockback;
    private boolean sweeping;
    private boolean cancelled;

    public PlayerAttackEntityEventImpl(final Player player, final Entity target, final ItemStack weapon,
                                       final float damage, final double knockback, final float attackStrength,
                                       final boolean critical, final boolean smash, final boolean sweeping) {
        Preconditions.checkArgument(player != null, "The attacking player must not be null");
        Preconditions.checkArgument(target != null, "The target must not be null");
        Preconditions.checkArgument(weapon != null, "The weapon must not be null");
        this.player = player;
        this.target = target;
        this.weapon = weapon;
        this.damage = damage;
        this.knockback = knockback;
        this.attackStrength = attackStrength;
        this.critical = critical;
        this.smash = smash;
        this.sweeping = sweeping;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public Entity target() {
        return target;
    }

    @Override
    public ItemStack weapon() {
        return weapon;
    }

    @Override
    public float damage() {
        return damage;
    }

    @Override
    public void setDamage(final float damage) {
        this.damage = damage;
    }

    @Override
    public double knockback() {
        return knockback;
    }

    @Override
    public void setKnockback(final double knockback) {
        this.knockback = knockback;
    }

    @Override
    public float attackStrength() {
        return attackStrength;
    }

    @Override
    public boolean isCritical() {
        return critical;
    }

    @Override
    public boolean isSmash() {
        return smash;
    }

    @Override
    public boolean isSweeping() {
        return sweeping;
    }

    @Override
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
