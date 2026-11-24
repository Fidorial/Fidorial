package fr.fidorial.combat;

import fr.fidorial.entity.Entity;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.DamageType;
import org.jspecify.annotations.Nullable;

/**
 * Describes where a hit came from.
 *
 * <p>A source carries the {@code minecraft:damage_type} used by the client to pick the death
 * message and the screen tint, plus the entities involved. {@link #causingEntity()} is the entity
 * credited with the kill (the shooter), while {@link #directEntity()} is what actually touched the
 * victim (the arrow). For a melee hit both are the same entity.</p>
 *
 * <p>Sources are immutable and created by the server: get them from
 * {@link CombatService#sources()}.</p>
 *
 * @see DamageSourceFactory
 * @since 0.1.0
 */
public interface DamageSource {

    /**
     * {@return the registry key of the damage type backing this source}
     *
     * @since 0.1.0
     */
    TypedKey<DamageType> type();

    /**
     * {@return the entity credited with the damage, or {@code null} for environmental damage}
     *
     * @since 0.1.0
     */
    @Nullable Entity causingEntity();

    /**
     * {@return the entity that dealt the hit directly, or {@code null} for environmental damage}
     *
     * @since 0.1.0
     */
    @Nullable Entity directEntity();

    /**
     * {@return the origin used to orient the hurt animation and the knockback, or {@code null} to
     * derive it from {@link #directEntity()}}
     *
     * @since 0.1.0
     */
    @Nullable Location position();

    /**
     * {@return the item the hit was dealt with: the weapon of a melee hit, the bow or crossbow that
     * shot an arrow, the thrown trident; {@link ItemStack#EMPTY} when there is none}
     *
     * @since 0.1.0
     */
    ItemStack weapon();

    /**
     * {@return {@code true} if the damage type of this source is in a damage type tag}
     *
     * @param tag the tag, for instance {@code DamageTypeTagKeys.BYPASSES_ARMOR}
     * @since 0.1.0
     */
    boolean is(TagKey<DamageType> tag);

    /**
     * {@return {@code true} if the entity credited with the hit is the one that touched the
     * victim, as for a melee hit}
     *
     * @since 0.1.0
     */
    boolean isDirect();

    /**
     * {@return a copy of this source whose hit comes from {@code position}}
     *
     * @param position the point the damage comes from, used for the hurt animation and the knockback
     * @since 0.1.0
     */
    DamageSource withPosition(Location position);

    /**
     * {@return a copy of this source dealt with {@code weapon}}
     *
     * @param weapon the item the hit was dealt with, {@link ItemStack#EMPTY} for none
     * @since 0.1.0
     */
    DamageSource withWeapon(ItemStack weapon);
}
