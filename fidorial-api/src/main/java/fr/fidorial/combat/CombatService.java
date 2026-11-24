package fr.fidorial.combat;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.service.ServiceRegistry;
import fr.fidorial.world.World;
import org.jspecify.annotations.Nullable;

/**
 * The combat rules: hits, damage, blocking, knockback and death, registered as a
 * {@linkplain ServiceRegistry service} so plugins can replace them.
 *
 * @since 0.1.0
 */
public interface CombatService {

    /**
     * {@return the factory of the damage sources this service understands}
     *
     * @since 0.1.0
     */
    DamageSourceFactory sources();

    /**
     * Makes an entity hit another with the item in its main hand, as a player does when they click
     * an entity, or a mob when it strikes its target.
     *
     * @param attacker the entity swinging
     * @param target   the entity being hit
     * @return {@code true} if damage was actually dealt
     * @since 0.1.0
     */
    boolean attack(LivingEntity attacker, Entity target);

    /**
     * Hurts an entity, running the full pipeline: invulnerability frames, shield, armor,
     * Resistance, enchantments, absorption, events, client packets and death.
     *
     * @param target the entity taking the hit
     * @param source the origin of the damage
     * @param amount damage in half-hearts before reduction; values {@code <= 0} are ignored
     * @return {@code true} if the hit hurt the entity, even when armor took all of it; {@code false}
     *         when it was refused or cancelled, absorbed by the invulnerability left by the last hit,
     *         or blocked by a shield
     * @since 0.1.0
     */
    boolean damage(LivingEntity target, DamageSource source, float amount);

    /**
     * Pushes an entity away from a point, the way a hit does.
     *
     * @param target   the entity to push
     * @param strength the horizontal knockback strength; {@code <= 0} does nothing
     * @param fromX    the x coordinate the push originates from
     * @param fromZ    the z coordinate the push originates from
     * @since 0.1.0
     */
    void knockback(LivingEntity target, double strength, double fromX, double fromZ);

    /**
     * Kills an entity outright, bypassing armor, shields, totems and invulnerability.
     *
     * @param target the entity to kill
     * @param killer the entity credited with the kill, or {@code null}
     * @since 0.1.0
     */
    void kill(LivingEntity target, @Nullable Entity killer);

    /**
     * {@return the damage a fully charged melee hit of this entity deals, before enchantments}: its
     * {@code minecraft:attack_damage} attribute, the weapon in its main hand and its effects included
     *
     * @param attacker the attacking entity
     * @since 0.1.0
     */
    float attackDamage(LivingEntity attacker);

    /**
     * {@return the extra knockback a melee hit of this entity deals}: its
     * {@code minecraft:attack_knockback} attribute, plus one for a sprinting player
     *
     * @param attacker the attacking entity
     * @since 0.1.0
     */
    double attackKnockback(LivingEntity attacker);

    /**
     * {@return the charge of the next hit of a player, between {@code 0} and {@code 1}}
     *
     * @param attacker the attacking player
     * @since 0.1.0
     */
    float attackStrengthScale(Player attacker);

    /**
     * {@return the ticks a swing of this player takes to recharge, from their attack speed}
     *
     * @param attacker the attacking player
     * @since 0.1.0
     */
    int attackCooldownTicks(Player attacker);

    /**
     * {@return {@code true} if this entity holds a raised shield, past its raising delay, that
     * is not disabled}
     *
     * @param entity the entity
     * @since 0.1.0
     */
    boolean isBlocking(LivingEntity entity);

    /**
     * Lowers the shield of an entity and keeps it from raising it again for a while, as an axe does.
     *
     * @param entity  the entity
     * @param seconds how long the shield stays disabled; {@code <= 0} does nothing
     * @since 0.1.0
     */
    void disableBlocking(LivingEntity entity, float seconds);

    /**
     * @param world the world
     * @return {@code true} when players are allowed to damage each other in this world
     * @since 0.1.0
     */
    boolean pvpEnabled(World world);
}
