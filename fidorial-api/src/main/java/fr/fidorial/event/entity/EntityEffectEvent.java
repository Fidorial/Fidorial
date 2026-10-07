package fr.fidorial.event.entity;

import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.event.Cancellable;
import fr.fidorial.event.Event;
import org.jspecify.annotations.Nullable;

/**
 * Fired before a status effect of an entity is added, changed or removed.
 *
 * <p>Cancelling it keeps the previous effects untouched, except for {@link Action#EXPIRED}: an effect
 * that ran out is always removed.</p>
 *
 * @since 0.1.0
 */
public class EntityEffectEvent implements Event, Cancellable {

    private final LivingEntity entity;
    private final Action action;
    private final @Nullable MobEffectInstance previous;
    private @Nullable MobEffectInstance effect;
    private boolean cancelled;

    /**
     * Creates an event.
     *
     * @param entity   the affected entity
     * @param action   what happens to the effect
     * @param previous the effect running before, or {@code null} when it is {@link Action#ADDED}
     * @param effect   the effect running after, or {@code null} when it is removed
     * @since 0.1.0
     */
    public EntityEffectEvent(final LivingEntity entity, final Action action,
                             final @Nullable MobEffectInstance previous, final @Nullable MobEffectInstance effect) {
        this.entity = entity;
        this.action = action;
        this.previous = previous;
        this.effect = effect;
    }

    /**
     * {@return the affected entity}
     *
     * @since 0.1.0
     */
    public LivingEntity entity() {
        return entity;
    }

    /**
     * {@return what happens to the effect}
     *
     * @since 0.1.0
     */
    public Action action() {
        return action;
    }

    /**
     * {@return the effect running before, {@code null} when the effect is added}
     *
     * @since 0.1.0
     */
    public @Nullable MobEffectInstance previous() {
        return previous;
    }

    /**
     * {@return the effect running after, {@code null} when the effect is removed}
     *
     * @since 0.1.0
     */
    public @Nullable MobEffectInstance effect() {
        return effect;
    }

    /**
     * Replaces the effect about to be applied, for instance to shorten it.
     *
     * @param effect the effect to apply instead, of the same type
     * @throws IllegalStateException    if the effect is being removed
     * @throws IllegalArgumentException if the effect is of another type
     * @since 0.1.0
     */
    public void setEffect(final MobEffectInstance effect) {
        if (this.effect == null) {
            throw new IllegalStateException("The effect is being removed (" + action + ")");
        }
        if (!this.effect.type().equals(effect.type())) {
            throw new IllegalArgumentException("Cannot replace " + this.effect.type() + " by " + effect.type());
        }
        this.effect = effect;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * What happens to the effect.
     *
     * @since 0.1.0
     */
    public enum Action {
        /**
         * The entity did not have the effect yet.
         */
        ADDED,
        /**
         * The entity already had the effect, which gets a new level or duration.
         */
        CHANGED,
        /**
         * The effect is removed by a plugin, a command or the game.
         */
        REMOVED,
        /**
         * The effect ran out; this removal cannot be cancelled.
         */
        EXPIRED
    }
}
