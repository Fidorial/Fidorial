package fr.fidorial.entity.mob;

import fr.fidorial.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A mob that leaves the others alone until provoked, then holds a grudge for a while: an enderman.
 *
 * <p>Once provoked it stays angry at the one that provoked it until a time of its world, and keeps
 * that grudge when saved. With the {@code universal_anger} game rule, a mob angry at no one in
 * particular is angry at every player; with {@code forgive_dead_players}, it forgives a player
 * that dies.</p>
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @since 0.1.0
 */
public interface Angerable extends Mob {

    /**
     * {@return {@code true} until the anger of this mob runs out}
     *
     * @since 0.1.0
     */
    boolean isAngry();

    /**
     * {@return how many ticks are left before the anger of this mob runs out, {@code 0} when it is
     * not angry}
     *
     * @since 0.1.0
     */
    long remainingAngerTicks();

    /**
     * {@return the one this mob holds a grudge against, {@code null} when it is angry at no one in
     * particular or not angry}
     *
     * @since 0.1.0
     */
    @Nullable UUID angryAt();

    /**
     * {@return {@code true} if this mob is angry at a living entity}: it may attack it, and it holds
     * a grudge against it, or it is a player while this mob is angry at every player
     *
     * @param entity the entity
     * @since 0.1.0
     */
    boolean isAngryAt(LivingEntity entity);

    /**
     * Makes this mob angry at a living entity for a while, and go after it.
     *
     * @param entity the entity to hold a grudge against
     * @param ticks  how long the anger lasts, if nothing starts it over
     * @since 0.1.0
     */
    void setAngryAt(LivingEntity entity, long ticks);

    /**
     * Calms this mob down: it forgets who hurt it and gives up its target.
     *
     * @since 0.1.0
     */
    void stopBeingAngry();
}
