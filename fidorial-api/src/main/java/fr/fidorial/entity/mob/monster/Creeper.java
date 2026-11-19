package fr.fidorial.entity.mob.monster;

import fr.fidorial.entity.mob.Mob;

/**
 * A creeper ({@code minecraft:creeper}): it walks up to a player and swells until it explodes,
 * twice as hard when lightning charged it.
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Creeper extends Mob {

    /**
     * How many ticks a creeper swells before it explodes, unless told otherwise.
     *
     * @since 0.1.0
     */
    int DEFAULT_FUSE_TICKS = 30;

    /**
     * How big the explosion of a creeper is, unless told otherwise; twice that for a charged one.
     *
     * @since 0.1.0
     */
    int DEFAULT_EXPLOSION_RADIUS = 3;

    /**
     * {@return {@code true} if lightning charged this creeper}: its explosion is twice as big
     *
     * @since 0.1.0
     */
    boolean isPowered();

    /**
     * Charges this creeper, as lightning does, or takes its charge away.
     *
     * @param powered {@code true} to charge it
     * @since 0.1.0
     */
    void setPowered(boolean powered);

    /**
     * {@return {@code true} once this creeper was lit}: it then swells until it explodes, whatever
     * happens around it
     *
     * @since 0.1.0
     */
    boolean isIgnited();

    /**
     * Lights this creeper, as flint and steel does.
     *
     * @since 0.1.0
     */
    void ignite();

    /**
     * {@return how many ticks this creeper has swollen}: it explodes once it reaches its
     * {@linkplain #fuseTicks() fuse}
     *
     * @since 0.1.0
     */
    int swell();

    /**
     * {@return how many ticks this creeper swells before it explodes}
     *
     * @since 0.1.0
     */
    int fuseTicks();

    /**
     * Sets how many ticks this creeper swells before it explodes.
     *
     * @param ticks the fuse, kept between {@code 1} and {@code 32767}
     * @since 0.1.0
     */
    void setFuseTicks(int ticks);

    /**
     * {@return how big the explosion of this creeper is, before a charge doubles it}
     *
     * @since 0.1.0
     */
    int explosionRadius();

    /**
     * Sets how big the explosion of this creeper is, before a charge doubles it.
     *
     * @param radius the radius, kept between {@code 0} and {@code 127}
     * @since 0.1.0
     */
    void setExplosionRadius(int radius);

    /**
     * Makes this creeper explode at once, then vanish.
     *
     * @since 0.1.0
     */
    void explode();
}
