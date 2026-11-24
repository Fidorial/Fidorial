package fr.fidorial.entity;

import fr.fidorial.combat.CombatService;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.event.entity.EntityEffectEvent;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Attribute;
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
     * {@return the value of an attribute of this entity: its base, with the modifiers of what it
     * wears and holds, of its effects and of its enchantments applied}
     *
     * @param attribute the attribute, for instance {@code AttributeKeys.ATTACK_DAMAGE}
     * @since 0.1.0
     */
    double attributeValue(TypedKey<Attribute> attribute);

    /**
     * {@return the armor points reducing incoming damage, {@code 0} when unarmored}
     *
     * @since 0.1.0
     */
    double armor();

    /**
     * {@return the armor toughness softening the armor falloff on heavy hits}
     *
     * @since 0.1.0
     */
    double armorToughness();

    /**
     * {@return the fraction of knockback ignored, between {@code 0} (none) and {@code 1} (immune)}
     *
     * @since 0.1.0
     */
    double knockbackResistance();

    /**
     * @return the remaining ticks this entity keeps burning, {@code 0} when not on fire
     */
    int fireTicks();

    /**
     * @param ticks the burning ticks to set; {@code 0} extinguishes the entity
     */
    void setFireTicks(int ticks);

    /**
     * {@return the stack worn or held in {@code slot}, {@link ItemStack#EMPTY} when the slot is
     * empty or the entity has none}
     *
     * @param slot the equipment slot
     * @since 0.1.0
     */
    ItemStack equipment(EquipmentSlot slot);

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
     * Hurts this entity, running the full pipeline of the {@link CombatService}: invulnerability
     * frames, shield, armor, Resistance, enchantments, absorption, events, client packets and death.
     *
     * @param source the origin of the hit
     * @param amount the damage in half-hearts before reduction; {@code <= 0} is ignored
     * @return {@code true} if the hit hurt this entity, even when armor took all of it; {@code false}
     *         when it was refused or cancelled, absorbed by the invulnerability left by the last hit,
     *         or blocked by a shield
     * @since 0.1.0
     */
    boolean damage(DamageSource source, float amount);

    /**
     * Kills this entity outright, bypassing armor, shields, totems and invulnerability.
     *
     * @param killer the entity credited with the kill, or {@code null}
     * @since 0.1.0
     */
    void kill(@Nullable Entity killer);

    /**
     * Restores some health, up to {@link #maxHealth()}. A dead entity is not healed.
     *
     * @param amount the health to restore in half-hearts; {@code <= 0} is ignored
     * @since 0.1.0
     */
    void heal(float amount);

    /**
     * {@return {@code true} when this entity has run out of health}
     *
     * @since 0.1.0
     */
    boolean isDead();

    /**
     * {@return {@code true} when this entity is still in the world and has health left}
     *
     * @since 0.1.0
     */
    boolean isAlive();
}
