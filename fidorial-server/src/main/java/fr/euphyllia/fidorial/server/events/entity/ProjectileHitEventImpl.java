package fr.euphyllia.fidorial.server.events.entity;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.projectile.Projectile;
import fr.fidorial.event.entity.ProjectileHitEvent;
import fr.fidorial.math.BlockPosition;
import org.jspecify.annotations.Nullable;

public final class ProjectileHitEventImpl implements ProjectileHitEvent {

    private final Projectile projectile;
    private final @Nullable Entity hitEntity;
    private final @Nullable BlockPosition hitBlock;
    private boolean cancelled;

    private ProjectileHitEventImpl(final Projectile projectile, final @Nullable Entity hitEntity,
                                   final @Nullable BlockPosition hitBlock) {
        Preconditions.checkArgument(projectile != null, "The projectile must not be null");
        this.projectile = projectile;
        this.hitEntity = hitEntity;
        this.hitBlock = hitBlock;
    }

    public static ProjectileHitEventImpl entity(final Projectile projectile, final Entity hit) {
        Preconditions.checkArgument(hit != null, "The entity hit must not be null");
        return new ProjectileHitEventImpl(projectile, hit, null);
    }

    public static ProjectileHitEventImpl block(final Projectile projectile, final BlockPosition hit) {
        Preconditions.checkArgument(hit != null, "The block hit must not be null");
        return new ProjectileHitEventImpl(projectile, null, hit);
    }

    @Override
    public Projectile projectile() {
        return projectile;
    }

    @Override
    public @Nullable Entity hitEntity() {
        return hitEntity;
    }

    @Override
    public @Nullable BlockPosition hitBlock() {
        return hitBlock;
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
