package fr.euphyllia.fidorial.server.events.entity;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.projectile.Projectile;
import fr.fidorial.event.entity.EntityShootProjectileEvent;
import fr.fidorial.item.ItemStack;

public final class EntityShootProjectileEventImpl implements EntityShootProjectileEvent {

    private final LivingEntity shooter;
    private final ItemStack weapon;
    private final Projectile projectile;
    private final float force;
    private boolean cancelled;

    public EntityShootProjectileEventImpl(final LivingEntity shooter, final ItemStack weapon, final Projectile projectile,
                                          final float force) {
        Preconditions.checkArgument(shooter != null, "The shooter must not be null");
        Preconditions.checkArgument(weapon != null, "The weapon must not be null");
        Preconditions.checkArgument(projectile != null, "The projectile must not be null");
        this.shooter = shooter;
        this.weapon = weapon;
        this.projectile = projectile;
        this.force = force;
    }

    @Override
    public LivingEntity shooter() {
        return shooter;
    }

    @Override
    public ItemStack weapon() {
        return weapon;
    }

    @Override
    public Projectile projectile() {
        return projectile;
    }

    @Override
    public float force() {
        return force;
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
