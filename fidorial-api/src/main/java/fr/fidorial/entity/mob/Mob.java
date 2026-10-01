package fr.fidorial.entity.mob;

import fr.fidorial.entity.Entity;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.ai.Goals;
import fr.fidorial.entity.ai.Navigator;
import fr.fidorial.math.Location;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A living entity that moves and thinks, whether it is a built-in mob or one a plugin defined.
 *
 * @since 0.1.0
 */
public interface Mob extends LivingEntity {

    /**
     * @return the definition this mob was built from, empty for a built-in mob
     * @since 0.1.0
     */
    Optional<MobDefinition> definition();

    /**
     * @return the goals driving this mob, mutable
     * @since 0.1.0
     */
    Goals goals();

    /**
     * @return the pathfinder, or {@link Navigator#NONE} when this mob does not walk paths
     * @since 0.1.0
     */
    Navigator navigation();

    /**
     * @return the player this mob is after, or {@code null}
     * @since 0.1.0
     */
    @Nullable Player target();

    /**
     * Sets the player this mob is after.
     *
     * <p>A mob re-picks its own target every few ticks among the players within
     * {@link #followRange()}, so a target set from outside that range is dropped shortly after.
     * Widen {@link #setFollowRange(double)} first to make it stick.</p>
     *
     * @param target the player to chase, or {@code null} to clear
     * @since 0.1.0
     */
    void setTarget(@Nullable Player target);

    /**
     * @param maxDistance the search radius in blocks, negative for unlimited
     * @return the closest player able to be seen by mobs, or {@code null}
     * @since 0.1.0
     */
    @Nullable Player nearestPlayer(double maxDistance);

    /**
     * @return the radius in blocks this mob picks targets within
     * @since 0.1.0
     */
    double followRange();

    /**
     * @param range the radius in blocks to pick targets within, negative to restore the default
     * @since 0.1.0
     */
    void setFollowRange(double range);

    /**
     * @return the walking speed this mob uses when no goal overrides it
     * @since 0.1.0
     */
    double movementSpeed();

    /**
     * Sets the speed the mob walks at <em>this tick</em>.
     *
     * <p>The value is cleared at the start of every tick, so a goal moving a mob sets it on each
     * one of its own ticks.</p>
     *
     * @param speed the speed in blocks per tick
     * @since 0.1.0
     */
    void setMoveSpeed(double speed);

    /**
     * @return the hitbox width in blocks
     * @since 0.1.0
     */
    double width();

    /**
     * @return the hitbox height in blocks
     * @since 0.1.0
     */
    double height();

    /**
     * @return the damage a hit from this mob deals, {@code 0} when it does not attack
     * @since 0.1.0
     */
    float attackDamage();

    /**
     * @return {@code true} when the mob is standing on a block
     * @since 0.1.0
     */
    boolean onGround();

    double velocityX();

    double velocityY();

    double velocityZ();

    /**
     * Overwrites the velocity, in blocks per tick.
     *
     * @param x the velocity along x
     * @param y the velocity along y, positive upwards
     * @param z the velocity along z
     * @since 0.1.0
     */
    void setVelocity(double x, double y, double z);

    /**
     * Turns the head and body towards a point.
     *
     * @param x the x coordinate to look at
     * @param y the y coordinate to look at
     * @param z the z coordinate to look at
     * @since 0.1.0
     */
    void lookAt(double x, double y, double z);

    /**
     * @param entity the entity to look at
     * @since 0.1.0
     */
    default void lookAt(final Entity entity) {
        final Location other = entity.location();
        lookAt(other.x(), other.y() + 1.5, other.z());
    }

    /**
     * @param other the entity to measure to
     * @return the squared distance between the two entities
     * @since 0.1.0
     */
    double distanceSqTo(Entity other);

    /**
     * @param other the entity to look for
     * @return {@code true} when no full block stands between the two
     * @since 0.1.0
     */
    boolean hasLineOfSightTo(Entity other);

    /**
     * Walks towards a location for this tick.
     *
     * @param destination the location to walk to
     * @param speed       the speed in blocks per tick
     * @since 0.1.0
     */
    default void navigateTo(final Location destination, final double speed) {
        navigation().moveTo(location(), destination);
        setMoveSpeed(speed);
    }

    /**
     * Plays a sound at this mob, heard by the players tracking it.
     *
     * @param sound  the sound to play
     * @param volume the volume, {@code 1} for the usual mob loudness
     * @param pitch  the pitch, between {@code 0.5} and {@code 2}
     * @since 0.1.0
     */
    void playSound(Sound.Type sound, float volume, float pitch);

    /**
     * @return the behaviours attached to this mob, in the order they were added
     * @since 0.1.0
     */
    List<MobBehaviour> behaviours();

    /**
     * @param type the behaviour implementation to look for
     * @param <T>  the behaviour type
     * @return the first behaviour of that type, empty when none is attached
     * @since 0.1.0
     */
    <T extends MobBehaviour> Optional<T> behaviour(Class<T> type);
}
