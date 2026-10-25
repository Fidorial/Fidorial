package fr.fidorial.entity;

import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.world.World;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A stack of items lying on the ground ({@code minecraft:item}), dropped by a player, thrown out of
 * a window or spilled by a block.
 *
 * <p>It falls, slides, merges with similar stacks nearby and despawns after
 * {@value #DEFAULT_LIFETIME} ticks. Once its {@linkplain #pickupDelay() pickup delay} is over, the
 * first player who walks over it takes as much of it as fits in their inventory.</p>
 *
 * <p>Like every entity, change it from the region thread owning its chunk.</p>
 *
 * @see World#dropItem(Location, ItemStack)
 * @since 0.1.0
 */
public interface ItemEntity extends Entity {

    /**
     * How many ticks an item stays on the ground before it despawns: five minutes.
     *
     * @since 0.1.0
     */
    int DEFAULT_LIFETIME = 6000;

    /**
     * A {@linkplain #setPickupDelay(int) pickup delay} that never runs out: nobody can pick the item
     * up, and it never merges with another one.
     *
     * @since 0.1.0
     */
    int INFINITE_PICKUP_DELAY = Short.MAX_VALUE;

    /**
     * The pickup delay of an item a player throws away, so that it does not land straight back
     * in their inventory: two seconds.
     *
     * @since 0.1.0
     */
    int THROWN_PICKUP_DELAY = 40;

    /**
     * The pickup delay of an item spilled by a block or a window: half a second.
     *
     * @since 0.1.0
     */
    int DEFAULT_PICKUP_DELAY = 10;

    /**
     * {@return the stack this entity carries, never empty while the entity is alive}
     *
     * @since 0.1.0
     */
    ItemStack item();

    /**
     * Replaces the stack this entity carries. An empty stack removes the entity.
     *
     * @param item the new stack
     * @since 0.1.0
     */
    void setItem(ItemStack item);

    /**
     * {@return how many ticks are left before the item may be picked up, {@code 0} when it may be
     * picked up now}
     *
     * @since 0.1.0
     */
    int pickupDelay();

    /**
     * Sets how many ticks are left before the item may be picked up.
     *
     * @param ticks the delay, {@code 0} for right away, {@link #INFINITE_PICKUP_DELAY} for never
     * @since 0.1.0
     */
    void setPickupDelay(int ticks);

    /**
     * {@return how many ticks this item has been on the ground}
     *
     * @since 0.1.0
     */
    int age();

    /**
     * Sets how many ticks this item has been on the ground: it despawns once this reaches
     * {@value #DEFAULT_LIFETIME}, unless its lifetime is {@linkplain #setUnlimitedLifetime(boolean)
     * unlimited}.
     *
     * @param ticks the age
     * @since 0.1.0
     */
    void setAge(int ticks);

    /**
     * {@return whether this item stays on the ground forever}
     *
     * @since 0.1.0
     */
    boolean hasUnlimitedLifetime();

    /**
     * Sets whether this item stays on the ground forever instead of despawning after
     * {@value #DEFAULT_LIFETIME} ticks.
     *
     * @param unlimited {@code true} to keep the item forever
     * @since 0.1.0
     */
    void setUnlimitedLifetime(boolean unlimited);

    /**
     * {@return the only player allowed to pick this item up, {@code null} when anybody may}
     *
     * @since 0.1.0
     */
    @Nullable UUID owner();

    /**
     * Restricts who may pick this item up.
     *
     * @param owner the only player allowed to, {@code null} to let anybody
     * @since 0.1.0
     */
    void setOwner(@Nullable UUID owner);

    /**
     * {@return the entity that dropped or threw this item, {@code null} when unknown}
     *
     * @since 0.1.0
     */
    @Nullable UUID thrower();

    /**
     * Sets the entity that dropped or threw this item.
     *
     * @param thrower its UUID, {@code null} when unknown
     * @since 0.1.0
     */
    void setThrower(@Nullable UUID thrower);

    /**
     * {@return the velocity along x, in blocks per tick}
     *
     * @since 0.1.0
     */
    double velocityX();

    /**
     * {@return the velocity along y, in blocks per tick, positive upwards}
     *
     * @since 0.1.0
     */
    double velocityY();

    /**
     * {@return the velocity along z, in blocks per tick}
     *
     * @since 0.1.0
     */
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
     * {@return whether the item rests on a block}
     *
     * @since 0.1.0
     */
    boolean onGround();
}
