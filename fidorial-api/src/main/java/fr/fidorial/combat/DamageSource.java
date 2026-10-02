package fr.fidorial.combat;

import com.google.common.base.Preconditions;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.Player;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import fr.fidorial.registry.keys.DamageTypeKeys;
import fr.fidorial.world.Location;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Describes where a hit came from.
 *
 * <p>A source carries the {@code minecraft:damage_type} used by the client to pick the death
 * message and the screen tint, plus the entities involved. {@link #causingEntity()} is the entity
 * credited with the kill (the shooter), while {@link #directEntity()} is what actually touched the
 * victim (the arrow). For a melee hit both are the same entity.</p>
 *
 * @since 0.1.0
 */
public final class DamageSource {

    private static final Set<TypedKey<DamageType>> BYPASSES_ARMOR = Set.of(
            DamageTypeKeys.CRAMMING,
            DamageTypeKeys.DRAGON_BREATH,
            DamageTypeKeys.DROWN,
            DamageTypeKeys.DRY_OUT,
            DamageTypeKeys.FALL,
            DamageTypeKeys.FLY_INTO_WALL,
            DamageTypeKeys.FREEZE,
            DamageTypeKeys.GENERIC,
            DamageTypeKeys.GENERIC_KILL,
            DamageTypeKeys.IN_WALL,
            DamageTypeKeys.INDIRECT_MAGIC,
            DamageTypeKeys.MAGIC,
            DamageTypeKeys.OUT_OF_WORLD,
            DamageTypeKeys.OUTSIDE_BORDER,
            DamageTypeKeys.SONIC_BOOM,
            DamageTypeKeys.STALAGMITE,
            DamageTypeKeys.STARVE,
            DamageTypeKeys.WITHER);

    private static final Set<TypedKey<DamageType>> BYPASSES_INVULNERABILITY = Set.of(
            DamageTypeKeys.GENERIC_KILL,
            DamageTypeKeys.OUT_OF_WORLD);

    private static final Set<TypedKey<DamageType>> FIRE = Set.of(
            DamageTypeKeys.CAMPFIRE,
            DamageTypeKeys.HOT_FLOOR,
            DamageTypeKeys.IN_FIRE,
            DamageTypeKeys.LAVA,
            DamageTypeKeys.ON_FIRE,
            DamageTypeKeys.SULFUR_CUBE_HOT,
            DamageTypeKeys.UNATTRIBUTED_FIREBALL);

    private static final Set<TypedKey<DamageType>> PROJECTILE = Set.of(
            DamageTypeKeys.ARROW,
            DamageTypeKeys.FIREBALL,
            DamageTypeKeys.MOB_PROJECTILE,
            DamageTypeKeys.SPEAR,
            DamageTypeKeys.SPIT,
            DamageTypeKeys.THROWN,
            DamageTypeKeys.TRIDENT,
            DamageTypeKeys.WIND_CHARGE,
            DamageTypeKeys.WITHER_SKULL);

    private static final Set<TypedKey<DamageType>> NO_KNOCKBACK = Set.of(
            DamageTypeKeys.DROWN,
            DamageTypeKeys.FALL,
            DamageTypeKeys.FREEZE,
            DamageTypeKeys.IN_WALL,
            DamageTypeKeys.OUT_OF_WORLD,
            DamageTypeKeys.STARVE);

    private final TypedKey<DamageType> type;
    private final @Nullable Entity causingEntity;
    private final @Nullable Entity directEntity;
    private final @Nullable Location position;

    private DamageSource(
            final TypedKey<DamageType> type,
            final @Nullable Entity causingEntity,
            final @Nullable Entity directEntity,
            final @Nullable Location position) {
        Preconditions.checkArgument(type != null, "The type of a damage source must not be null");
        this.type = type;
        this.causingEntity = causingEntity;
        this.directEntity = directEntity;
        this.position = position;
    }

    /**
     * @param type the {@code minecraft:damage_type} to report to the client
     * @return an environmental source with no entity attached
     * @since 0.1.0
     */
    public static DamageSource of(final TypedKey<DamageType> type) {
        return new DamageSource(type, null, null, null);
    }

    /**
     * @param type   the {@code minecraft:damage_type} to report to the client
     * @param source the entity responsible for the hit
     * @since 0.1.0
     */
    public static DamageSource of(final TypedKey<DamageType> type, final @Nullable Entity source) {
        return new DamageSource(type, source, source, null);
    }

    /**
     * @param type    the {@code minecraft:damage_type} to report to the client
     * @param causing the entity credited with the kill, typically the shooter
     * @param direct  the entity that physically hit the victim, typically the projectile
     * @since 0.1.0
     */
    public static DamageSource of(
            final TypedKey<DamageType> type, final @Nullable Entity causing, final @Nullable Entity direct) {
        return new DamageSource(type, causing, direct, null);
    }

    /**
     * Creates a source for damage radiating from a point, such as an explosion.
     *
     * @param type     the {@code minecraft:damage_type} to report to the client
     * @param position the point the damage radiated from, used by the client for the hit direction
     * @return the source
     * @since 0.1.0
     */
    public static DamageSource at(final TypedKey<DamageType> type, final Location position) {
        return new DamageSource(type, null, null, position);
    }

    /**
     * {@return a source for damage dealt by a player melee hit}
     *
     * @param attacker the attacking player
     * @since 0.1.0
     */
    public static DamageSource playerAttack(final Player attacker) {
        return of(DamageTypeKeys.PLAYER_ATTACK, attacker);
    }

    /**
     * {@return a source for damage dealt by a mob melee hit}
     *
     * @param attacker the attacking mob, or {@code null}
     * @since 0.1.0
     */
    public static DamageSource mobAttack(final @Nullable Entity attacker) {
        return of(DamageTypeKeys.MOB_ATTACK, attacker);
    }

    /**
     * {@return a source for damage dealt by falling}
     *
     * @since 0.1.0
     */
    public static DamageSource fall() {
        return of(DamageTypeKeys.FALL);
    }

    /**
     * {@return a source for damage dealt by drowning}
     *
     * @since 0.1.0
     */
    public static DamageSource drown() {
        return of(DamageTypeKeys.DROWN);
    }

    /**
     * {@return a source for damage dealt by standing in lava}
     *
     * @since 0.1.0
     */
    public static DamageSource lava() {
        return of(DamageTypeKeys.LAVA);
    }

    /**
     * {@return a source for damage dealt by standing in fire}
     *
     * @since 0.1.0
     */
    public static DamageSource inFire() {
        return of(DamageTypeKeys.IN_FIRE);
    }

    /**
     * {@return a source for damage dealt by burning}
     *
     * @since 0.1.0
     */
    public static DamageSource onFire() {
        return of(DamageTypeKeys.ON_FIRE);
    }

    /**
     * {@return a source for damage dealt by touching a cactus}
     *
     * @since 0.1.0
     */
    public static DamageSource cactus() {
        return of(DamageTypeKeys.CACTUS);
    }

    /**
     * {@return a source for damage dealt by starving}
     *
     * @since 0.1.0
     */
    public static DamageSource starve() {
        return of(DamageTypeKeys.STARVE);
    }

    /**
     * {@return a source for damage dealt by falling out of the world}
     *
     * @since 0.1.0
     */
    public static DamageSource outOfWorld() {
        return of(DamageTypeKeys.OUT_OF_WORLD);
    }

    /**
     * {@return a source for damage dealt by an unspecified cause}
     *
     * @since 0.1.0
     */
    public static DamageSource generic() {
        return of(DamageTypeKeys.GENERIC);
    }

    /**
     * @return the registry key of the damage type backing this source
     */
    public TypedKey<DamageType> type() {
        return type;
    }

    /**
     * @return the entity credited with the damage, or {@code null} for environmental damage
     */
    public @Nullable Entity causingEntity() {
        return causingEntity;
    }

    /**
     * @return the entity that dealt the hit directly, or {@code null} for environmental damage
     */
    public @Nullable Entity directEntity() {
        return directEntity;
    }

    /**
     * @return the origin used to orient the hurt animation, or {@code null} to derive it from
     * {@link #directEntity()}
     */
    public @Nullable Location position() {
        return position;
    }

    /**
     * @return {@code true} when armour points do not reduce this damage
     */
    public boolean bypassesArmor() {
        return BYPASSES_ARMOR.contains(type);
    }

    /**
     * @return {@code true} when this damage ignores both invulnerability frames and creative mode
     */
    public boolean bypassesInvulnerability() {
        return BYPASSES_INVULNERABILITY.contains(type);
    }

    /**
     * @return {@code true} when this damage counts as fire for resistances and effects
     */
    public boolean isFire() {
        return FIRE.contains(type);
    }

    /**
     * @return {@code true} when the hit came from a projectile
     */
    public boolean isProjectile() {
        return PROJECTILE.contains(type);
    }

    /**
     * @return {@code true} when this damage must never push the victim around
     */
    public boolean noKnockback() {
        return NO_KNOCKBACK.contains(type);
    }

    /**
     * @return the source with {@code position} attached, used to orient the hurt animation
     */
    public DamageSource withPosition(final Location position) {
        return new DamageSource(type, causingEntity, directEntity, position);
    }

    @Override
    public String toString() {
        return "DamageSource[" + type.key() + (causingEntity == null ? "" : " by " + causingEntity) + "]";
    }
}
