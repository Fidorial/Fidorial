package fr.fidorial.combat;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.Player;
import fr.fidorial.world.World;
import org.jspecify.annotations.Nullable;

public interface CombatService {

    /**
     * @param attacker the player who swung
     * @param target   the entity being hit
     * @return {@code true} if damage was actually dealt
     * @since 0.1.0
     */
    boolean attack(Player attacker, Entity target);

    /**
     * @param target the entity taking the hit
     * @param source the origin of the damage
     * @param amount damage in half-hearts before reduction; values {@code <= 0} are ignored
     * @return {@code true} if health or absorption was actually removed
     * @since 0.1.0
     */
    boolean damage(LivingEntity target, DamageSource source, float amount);

    /**
     * @param target   the entity to push
     * @param strength the horizontal knockback strength; {@code <= 0} does nothing
     * @param fromX    the x coordinate the push originates from
     * @param fromZ    the z coordinate the push originates from
     * @since 0.1.0
     */
    void knockback(LivingEntity target, double strength, double fromX, double fromZ);

    /**
     * @param target the entity to kill
     * @param killer the entity credited with the kill, or {@code null}
     * @since 0.1.0
     */
    void kill(LivingEntity target, @Nullable Entity killer);

    /**
     * @param attacker the attacking player
     * @return the damage this player's current weapon deals on a fully charged hit
     * @since 0.1.0
     */
    float attackDamage(Player attacker);

    /**
     * @param attacker the attacking player
     * @return the knockback strength applied by this player's hits
     * @since 0.1.0
     */
    double attackKnockback(Player attacker);

    /**
     * @param attacker the attacking player
     * @return the charge of the next hit, between {@code 0.2} and {@code 1}
     * @since 0.1.0
     */
    float attackStrengthScale(Player attacker);

    /**
     * @param attacker the attacking player
     * @return the ticks a fully charged swing takes to recharge, derived from attack speed
     * @since 0.1.0
     */
    int attackCooldownTicks(Player attacker);

    /**
     * @param world the world
     * @return {@code true} when players are allowed to damage each other in this world
     * @since 0.1.0
     */
    boolean pvpEnabled(World world);
}
