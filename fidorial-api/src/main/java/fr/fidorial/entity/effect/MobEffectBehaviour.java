package fr.fidorial.entity.effect;

import fr.fidorial.entity.LivingEntity;

/**
 * What an effect does to the entity it runs on. Fidorial gives behaviour to few vanilla effects, so a
 * plugin can {@linkplain MobEffectRegistry#attach attach} one to a vanilla effect as well as to an
 * effect it {@linkplain MobEffectRegistry#register registers}.
 *
 * <p>Every hook runs on the thread of the region owning the entity.</p>
 *
 * @since 0.1.0
 */
public interface MobEffectBehaviour {

    /**
     * Called when the effect is added to an entity, or changed by a new application.
     *
     * @param entity the affected entity
     * @param effect the effect now running
     * @since 0.1.0
     */
    default void onApplied(final LivingEntity entity, final MobEffectInstance effect) {
    }

    /**
     * Called every tick while the effect runs, before its duration goes down.
     *
     * @param entity the affected entity
     * @param effect the running effect
     * @since 0.1.0
     */
    default void onTick(final LivingEntity entity, final MobEffectInstance effect) {
    }

    /**
     * Called when the effect is removed or runs out.
     *
     * @param entity the entity that had it
     * @param effect the effect that stopped
     * @since 0.1.0
     */
    default void onRemoved(final LivingEntity entity, final MobEffectInstance effect) {
    }
}
