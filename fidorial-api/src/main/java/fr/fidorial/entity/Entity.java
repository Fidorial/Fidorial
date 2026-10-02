package fr.fidorial.entity;

import fr.fidorial.command.CommandSource;
import fr.fidorial.scheduler.SchedulerSource;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.Location;
import fr.fidorial.world.World;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Anything living in a {@link World}: players, mobs, items, projectiles.
 *
 * <p>An entity is bound to the region thread owning its chunk. Read its state freely, but change it
 * from that thread, through {@link SchedulerSource#execute(Runnable)} when
 * needed.</p>
 *
 * @since 0.1.0
 */
public interface Entity extends CommandSource, HoverEventSource<HoverEvent.ShowEntity>, Sound.Emitter, Sound.Source.Provider, SchedulerSource {

    /**
     * {@return the network identifier of this entity, unique while the server runs}
     *
     * @since 0.1.0
     */
    int entityId();

    /**
     * {@return the persistent identity of this entity}
     *
     * @since 0.1.0
     */
    UUID uuid();

    /**
     * {@return the name shown for this entity, its custom name or its type name}
     *
     * @since 0.1.0
     */
    Component displayName();

    /**
     * {@return the type of this entity}
     *
     * @since 0.1.0
     */
    EntityType type();

    /**
     * {@return the world this entity is in}
     *
     * @since 0.1.0
     */
    World world();

    /**
     * {@return the current position and orientation of this entity}
     *
     * @since 0.1.0
     */
    Location location();

    /**
     * {@return the position of the chunk holding this entity}
     *
     * @since 0.1.0
     */
    default ChunkPos chunk() {
        return location().chunk();
    }

    /**
     * {@return {@code true} once this entity has left its world for good}
     *
     * @since 0.1.0
     */
    boolean isRemoved();

    /**
     * Removes this entity from its world for good.
     *
     * @since 0.1.0
     */
    void remove();

    /**
     * Teleports this entity to the given location within its current {@linkplain #world() world}.
     *
     * @param location the destination position and orientation
     * @return a future completing with {@code true} if the teleport happened, {@code false} if it was
     * refused (for example because the entity has been {@linkplain #isRemoved() removed})
     * @since 0.1.0
     */
    CompletableFuture<Boolean> teleport(Location location);

    /**
     * Teleports this entity to the given location, moving it to {@code world} when that differs from
     * its current world.
     *
     * <p>Cross-world teleports relocate the entity between worlds and, for players, trigger the
     * client-side dimension change. The call is refused, completing with {@code false}, when the entity
     * is removed or the destination world cannot host it.</p>
     *
     * @param world    the destination world
     * @param location the destination position and orientation
     * @return a future completing with {@code true} if the teleport happened, {@code false} if it was
     * refused
     * @since 0.1.0
     */
    CompletableFuture<Boolean> teleport(World world, Location location);

    /**
     * Teleports this entity within its current world, keeping its orientation.
     *
     * @param x the destination x coordinate
     * @param y the destination y coordinate
     * @param z the destination z coordinate
     * @return a future completing with {@code true} if the teleport happened, {@code false} if it was refused
     * @since 0.1.0
     */
    default CompletableFuture<Boolean> teleport(final double x, final double y, final double z) {
        final Location current = location();
        return teleport(new Location(x, y, z, current.yaw(), current.pitch()));
    }

    /**
     * Teleports this entity to another entity, moving worlds when needed.
     *
     * @param target the entity to teleport to
     * @return a future completing with {@code true} if the teleport happened, {@code false} if it was refused
     * @since 0.1.0
     */
    default CompletableFuture<Boolean> teleport(final Entity target) {
        return teleport(target.world(), target.location());
    }
}
