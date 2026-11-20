package fr.fidorial.event.mob;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.event.Cancellable;
import org.jspecify.annotations.Nullable;

/**
 * Fired when a mob is about to take a new target: one it noticed, one that hurt it, or one a plugin
 * {@linkplain Mob#setTarget(LivingEntity) gave} it.
 *
 * <p>It does not fire when the mob keeps its target, nor when it gives up on one. Cancelling it
 * leaves the mob with its previous target; the mob may try again later, firing the event anew.
 * Setting the target of the mob from a listener fires a nested event.</p>
 *
 * @since 0.1.0
 */
public interface MobTargetEvent extends MobEvent, Cancellable {

    /**
     * {@return the target the mob had, {@code null} for none}
     *
     * @since 0.1.0
     */
    @Nullable LivingEntity previousTarget();

    /**
     * {@return the target the mob is about to take, {@code null} if a listener took it away}
     *
     * @since 0.1.0
     */
    @Nullable LivingEntity target();

    /**
     * Gives the mob another target instead.
     *
     * @param target the new target; {@code null} to leave the mob without any
     * @since 0.1.0
     */
    void setTarget(@Nullable LivingEntity target);
}
