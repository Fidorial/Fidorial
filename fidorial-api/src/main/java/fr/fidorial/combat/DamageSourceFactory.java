package fr.fidorial.combat;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import org.jspecify.annotations.Nullable;

/**
 * Creates {@linkplain DamageSource damage sources}.
 *
 * <p>Obtained from {@link CombatService#sources()}.</p>
 *
 * @since 0.1.0
 */
public interface DamageSourceFactory {

    /**
     * {@return an environmental source with no entity attached}
     *
     * @param type the {@code minecraft:damage_type} to report to the client
     * @since 0.1.0
     */
    DamageSource of(TypedKey<DamageType> type);

    /**
     * {@return a source caused and dealt by the same entity}
     *
     * @param type   the {@code minecraft:damage_type} to report to the client
     * @param source the entity responsible for the hit, or {@code null}
     * @since 0.1.0
     */
    DamageSource of(TypedKey<DamageType> type, @Nullable Entity source);

    /**
     * {@return a source whose credited entity differs from the one that touched the victim}
     *
     * @param type    the {@code minecraft:damage_type} to report to the client
     * @param causing the entity credited with the kill, typically the shooter
     * @param direct  the entity that physically hit the victim, typically the projectile
     * @since 0.1.0
     */
    DamageSource of(TypedKey<DamageType> type, @Nullable Entity causing, @Nullable Entity direct);

    /**
     * {@return a source for damage radiating from a point, such as an explosion}
     *
     * @param type     the {@code minecraft:damage_type} to report to the client
     * @param position the point the damage radiated from
     * @since 0.1.0
     */
    DamageSource at(TypedKey<DamageType> type, Location position);

    /**
     * {@return a source for a melee hit of a player, dealt with the item in their main hand}
     *
     * @param attacker the attacking player
     * @since 0.1.0
     */
    DamageSource playerAttack(Player attacker);

    /**
     * {@return a source for a melee hit of a mob, dealt with the item in its main hand}
     *
     * @param attacker the attacking mob, or {@code null}
     * @since 0.1.0
     */
    DamageSource mobAttack(@Nullable LivingEntity attacker);

    /**
     * {@return a source for an arrow hit}
     *
     * @param arrow   the arrow
     * @param shooter the entity that shot it, or {@code null}
     * @since 0.1.0
     */
    DamageSource arrow(Entity arrow, @Nullable Entity shooter);

    /**
     * {@return a source for a thrown trident hit}
     *
     * @param trident the trident
     * @param thrower the entity that threw it, or {@code null}
     * @since 0.1.0
     */
    DamageSource trident(Entity trident, @Nullable Entity thrower);

    /**
     * {@return a source for a smash attack of a mace}
     *
     * @param attacker the attacking entity
     * @since 0.1.0
     */
    DamageSource maceSmash(LivingEntity attacker);

    /**
     * {@return a source for the damage Thorns deals back}
     *
     * @param wearer the entity wearing the thorny armor
     * @since 0.1.0
     */
    DamageSource thorns(Entity wearer);

    /**
     * {@return a source for a thrown projectile, such as a snowball or an ice ball}
     *
     * @param projectile the projectile that hit, or {@code null} if it is unknown
     * @param thrower    the entity that threw it, or {@code null} if there is none
     * @since 0.1.0
     */
    DamageSource thrown(@Nullable Entity projectile, @Nullable Entity thrower);

    /**
     * {@return a source for damage dealt by falling}
     *
     * @since 0.1.0
     */
    DamageSource fall();

    /**
     * {@return a source for damage dealt by drowning}
     *
     * @since 0.1.0
     */
    DamageSource drown();

    /**
     * {@return a source for damage dealt by standing in lava}
     *
     * @since 0.1.0
     */
    DamageSource lava();

    /**
     * {@return a source for damage dealt by standing in fire}
     *
     * @since 0.1.0
     */
    DamageSource inFire();

    /**
     * {@return a source for damage dealt by burning}
     *
     * @since 0.1.0
     */
    DamageSource onFire();

    /**
     * {@return a source for damage dealt by touching a cactus}
     *
     * @since 0.1.0
     */
    DamageSource cactus();

    /**
     * {@return a source for damage dealt by starving}
     *
     * @since 0.1.0
     */
    DamageSource starve();

    /**
     * {@return a source for damage dealt by falling out of the world}
     *
     * @since 0.1.0
     */
    DamageSource outOfWorld();

    /**
     * {@return a source for damage dealt by freezing, as in powder snow or under the Freezing effect}
     *
     * @since 0.1.0
     */
    DamageSource freeze();

    /**
     * {@return a source for damage dealt by a magic effect, such as Poison or Instant Damage}
     *
     * @since 0.1.0
     */
    DamageSource magic();

    /**
     * {@return a source for damage dealt by the Wither effect}
     *
     * @since 0.1.0
     */
    DamageSource wither();

    /**
     * {@return a source for damage dealt by an unspecified cause}
     *
     * @since 0.1.0
     */
    DamageSource generic();

    /**
     * {@return a source that kills whatever it hits, as {@code /kill} does}
     *
     * @since 0.1.0
     */
    DamageSource genericKill();
}
