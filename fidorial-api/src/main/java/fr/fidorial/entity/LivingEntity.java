package fr.fidorial.entity;

import fr.fidorial.combat.CombatService;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.event.entity.EntityEffectEvent;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * An entity with health that can be hurt and killed.
 *
 * @since 0.1.0
 */
public interface LivingEntity extends Entity {

    /**
     * @return the current health in half-hearts, between {@code 0} and {@link #maxHealth()}
     */
    float health();

    /**
     * @param health the health to set, clamped between {@code 0} and {@link #maxHealth()}
     */
    void setHealth(float health);

    /**
     * @return the health ceiling in half-hearts
     */
    float maxHealth();

    /**
     * @return the yellow absorption hearts soaking damage before {@link #health()} does
     */
    float absorptionAmount();

    /**
     * @param absorption the absorption hearts to set; negative values are treated as {@code 0}
     */
    void setAbsorptionAmount(float absorption);

    /**
     * @return the current head yaw
     */
    float headYaw();

    /**
     * @param headYaw the head yaw to set
     */
    void setHeadYaw(float headYaw);

    /**
     * @return the armor points reducing incoming damage, {@code 0} when unarmored
     */
    default double armor() {
        return 0.0;
    }

    /**
     * @return the armor toughness softening the armor falloff on heavy hits
     */
    default double armorToughness() {
        return 0.0;
    }

    /**
     * @return the fraction of knockback ignored, between {@code 0} (none) and {@code 1} (immune)
     */
    default double knockbackResistance() {
        return 0.0;
    }

    /**
     * @return the remaining ticks this entity keeps burning, {@code 0} when not on fire
     */
    int fireTicks();

    /**
     * @param ticks the burning ticks to set; {@code 0} extinguishes the entity
     */
    void setFireTicks(int ticks);

    /**
     * Applies a status effect. When the entity already has an effect of the same type, the two are
     * merged the vanilla way: a higher amplifier replaces it, and an equal one only refreshes a longer
     * duration. Fires an {@link EntityEffectEvent}.
     *
     * @param effect the effect to apply
     * @return {@code true} if the effects of the entity changed
     * @since 0.1.0
     */
    boolean addEffect(MobEffectInstance effect);

    /**
     * {@return the running effect of a type, or empty when the entity does not have it}
     *
     * @param type the effect, for instance {@code MobEffectKeys.SPEED}
     * @since 0.1.0
     */
    Optional<MobEffectInstance> effect(TypedKey<MobEffect> type);

    /**
     * {@return {@code true} if the entity has an effect of this type}
     *
     * @param type the effect, for instance {@code MobEffectKeys.SPEED}
     * @since 0.1.0
     */
    boolean hasEffect(TypedKey<MobEffect> type);

    /**
     * {@return a snapshot of the running effects}
     *
     * @since 0.1.0
     */
    Collection<MobEffectInstance> activeEffects();

    /**
     * Removes an effect. Fires an {@link EntityEffectEvent}.
     *
     * @param type the effect to remove
     * @return {@code true} if the entity had the effect and it was removed
     * @since 0.1.0
     */
    boolean removeEffect(TypedKey<MobEffect> type);

    /**
     * Removes every effect, firing an {@link EntityEffectEvent} for each.
     *
     * @return {@code true} if at least one effect was removed
     * @since 0.1.0
     */
    boolean clearEffects();

    /**
     * Hurts this entity, running the full pipeline: invulnerability frames, armor, absorption,
     * events, client packets and death.
     *
     * @param source the origin of the hit
     * @param amount the damage in half-hearts before reduction; {@code <= 0} is ignored
     * @return {@code true} if health or absorption was actually removed
     * @since 0.1.0
     */
    default boolean damage(final DamageSource source, final float amount) {
        return server().services()
                .find(CombatService.class)
                .map(combat -> combat.damage(this, source, amount))
                .orElse(false);
    }

    /**
     * Kills this entity outright, bypassing armor and invulnerability.
     *
     * @param killer the entity credited with the kill, or {@code null}
     * @since 0.1.0
     */
    default void kill(final @Nullable Entity killer) {
        server().services().find(CombatService.class).ifPresent(combat -> combat.kill(this, killer));
    }

    /**
     * @param amount the health to restore in half-hearts; {@code <= 0} is ignored
     * @since 0.1.0
     */
    default void heal(final float amount) {
        if (amount <= 0f || isDead()) {
            return;
        }
        setHealth(Math.min(maxHealth(), health() + amount));
    }

    /**
     * @return {@code true} when this entity has run out of health
     */
    default boolean isDead() {
        return health() <= 0f;
    }

    /**
     * @return {@code true} when this entity is still in the world and has health left
     */
    default boolean isAlive() {
        return !isDead() && !isRemoved();
    }
}
