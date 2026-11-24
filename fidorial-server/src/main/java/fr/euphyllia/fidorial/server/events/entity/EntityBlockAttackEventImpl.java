package fr.euphyllia.fidorial.server.events.entity;

import com.google.common.base.Preconditions;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.event.entity.EntityBlockAttackEvent;
import fr.fidorial.item.ItemStack;

public final class EntityBlockAttackEventImpl implements EntityBlockAttackEvent {

    private final LivingEntity entity;
    private final DamageSource source;
    private final ItemStack shield;
    private final float damage;
    private float blockedDamage;
    private boolean cancelled;

    public EntityBlockAttackEventImpl(final LivingEntity entity, final DamageSource source, final ItemStack shield,
                                      final float damage, final float blockedDamage) {
        Preconditions.checkArgument(entity != null, "The entity blocking must not be null");
        Preconditions.checkArgument(source != null, "The source of the damage must not be null");
        Preconditions.checkArgument(shield != null, "The shield must not be null");
        this.entity = entity;
        this.source = source;
        this.shield = shield;
        this.damage = damage;
        this.blockedDamage = Math.clamp(blockedDamage, 0f, Math.max(0f, damage));
    }

    @Override
    public LivingEntity entity() {
        return entity;
    }

    @Override
    public DamageSource source() {
        return source;
    }

    @Override
    public ItemStack shield() {
        return shield;
    }

    @Override
    public float damage() {
        return damage;
    }

    @Override
    public float blockedDamage() {
        return blockedDamage;
    }

    @Override
    public void setBlockedDamage(final float blockedDamage) {
        this.blockedDamage = Math.clamp(blockedDamage, 0f, Math.max(0f, damage));
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
