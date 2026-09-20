package fr.fidorial.entity;

import fr.fidorial.command.CommandSource;
import fr.fidorial.math.Location;
import fr.fidorial.scheduler.SchedulerSource;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Entity extends CommandSource, HoverEventSource<HoverEvent.ShowEntity>, Sound.Emitter, Sound.Source.Provider, SchedulerSource {

    int entityId();

    UUID uuid();

    Component displayName();

    EntityType type();

    World world();

    Location location();

    default ChunkPos chunk() {
        return location().chunk();
    }

    boolean isRemoved();

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
     * @param x the destination x coordinate
     * @param y the destination y coordinate
     * @param z the destination z coordinate
     * @return a future completing with {@code true} if the teleport happened, {@code false} if it was refused
     * @since 0.1.0
     */
    // todo: redundant syntax sugar; remove me
    default CompletableFuture<Boolean> teleport(final double x, final double y, final double z) {
        return teleport(Location.of(world(), x, y, z, location().yaw(), location().pitch()));
    }

    /**
     * @param target the entity to teleport to
     * @return a future completing with {@code true} if the teleport happened, {@code false} if it was refused
     * @since 0.1.0
     */
    default CompletableFuture<Boolean> teleport(final Entity target) {
        return teleport(target.location());
    }
}
