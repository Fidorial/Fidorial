package fr.euphyllia.fidorial.server.entity;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.ClientboundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.World;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class AbstractEntity implements Entity {

    public static final ComponentLogger LOGGER = ComponentLogger.logger(AbstractEntity.class);

    private final int entityId;
    private final EntityType type;
    private final AtomicBoolean removed = new AtomicBoolean(false);
    private UUID uuid;
    private volatile Location location;

    private final EntityTaskQueue tasks = new EntityTaskQueue(this);

    protected AbstractEntity(final int entityId, final UUID uuid, final EntityType type, final Location location) {
        this.entityId = entityId;
        this.uuid = uuid;
        this.type = type;
        this.location = location;
    }

    @Override
    public final int entityId() {
        return entityId;
    }

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public Component displayName() {
        return Component.translatable("entity." + type.key().key().asString().replace(':', '.'));
    }

    public final void restoreUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public final EntityType type() {
        return type;
    }

    @Override
    public final World world() {
        return location.world();
    }

    @Override
    public final Location location() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    /**
     * Moves this entity within its world, and tells the world when it changed chunk.
     *
     * @param world the world of this entity
     * @param to    where it goes
     */
    protected final void moveWithin(final ServerWorld world, final Location to) {
        final ChunkPos fromChunk = location().chunk();
        setLocation(to);
        final ChunkPos toChunk = to.chunk();
        if (!fromChunk.equals(toChunk)) {
            world.entityMoved(this, fromChunk, toChunk);
        }
    }

    @Override
    public final boolean isRemoved() {
        return removed.get();
    }

    @Override
    public FidorialServer server() {
        return FidorialServer.getInstance();
    }

    @Override
    public final void remove() {
        if (removed.compareAndSet(false, true)) {
            onRemoved();
            tasks.retireAll();
        }
    }

    protected void onRemoved() {
    }

    protected void onLeftWorld(final ServerWorld from, final ChunkPos chunk) {
    }

    protected void onEnteredWorld(final ServerWorld to, final ChunkPos chunk) {
    }

    public void tick(final long currentTick) {
    }

    public final void sendToTrackers(final ClientboundPacket<?> packet) {
        server().entities().tracker().sendToViewers(this, packet);
    }

    public void sendSpawnPackets(final ClientConnection connection) {
        connection.send(ClientboundAddEntityPacket.of(this));
    }

    @Override
    public final boolean equals(final @Nullable Object o) {
        return o instanceof final AbstractEntity other && other.entityId == entityId;
    }

    @Override
    public final int hashCode() {
        return Integer.hashCode(entityId);
    }

    @Override
    public String toString() {
        return type.key() + "#" + entityId;
    }

    @Override
    public CompletableFuture<Boolean> teleport(final Location location) {
        if (isRemoved() || !(location.world() instanceof final ServerWorld target)) {
            return CompletableFuture.completedFuture(false);
        }

        final ChunkPos destChunk = location.chunk();
        final CompletableFuture<Boolean> result = new CompletableFuture<>();

        final boolean scheduled = execute(() -> {
            try {
                final Location previous = location();
                final ChunkPos fromChunk = previous.chunk();
                if (!(previous.world() instanceof final ServerWorld old)) {
                    result.complete(false);
                    return;
                }

                if (old == target) {
                    setLocation(location);
                    target.entityMoved(this, fromChunk, destChunk);
                    finishTeleport(location);
                    result.complete(true);
                    return;
                }

                old.removeEntity(this);
                onLeftWorld(old, fromChunk);

                final boolean arrivalScheduled = target.scheduler().execute(target.key(), destChunk, () -> {
                    try {
                        setLocation(location);
                        target.addEntity(this);
                        onEnteredWorld(target, destChunk);
                        finishTeleport(location);
                        result.complete(true);
                    } catch (final Exception exception) {
                        LOGGER.error("An error occurred while teleporting the entity : ", exception);
                        result.complete(false);
                    }
                });

                if (!arrivalScheduled) {
                    LOGGER.warn("{} was removed from {} but could not be scheduled to arrive in {}", this, old.key(), target.key());
                    result.complete(false);
                    return;
                }
                setLocation(location);
            } catch (final Exception exception) {
                LOGGER.error("An error occurred while teleporting the entity : ", exception);
                result.complete(false);
            }
        }, () -> result.complete(false));

        if (!scheduled) {
            result.complete(false);
        }

        return result;
    }

    private void finishTeleport(final Location location) {
        sendToTrackers(new ClientboundEntityPositionSyncPacket(
                entityId(),
                new PositionData.LinearPositionPath(LocationPositionData.vec3(location)),
                LocationPositionData.floatRotation(location),
                location.yaw(),
                false));
        server().entities().tracker().update(this, server().players().online());
    }

    @Override
    public HoverEvent<HoverEvent.ShowEntity> asHoverEvent(final UnaryOperator<HoverEvent.ShowEntity> op) {
        return HoverEvent.showEntity(op.apply(HoverEvent.ShowEntity.showEntity(type().key().key(), uuid(), displayName())));
    }

    @Override
    public boolean execute(final Runnable task, final Runnable retired) {
        return tasks.execute(task, retired);
    }

    /// Attempts to execute the task right away if on the correct thread, and schedules it otherwise.
    public final boolean runOrExecute(final Runnable task) {
        if (isOwnedByCurrentThread()) {
            task.run();
            return true;
        }
        return execute(task);
    }

    @Override
    public boolean executeDelayed(final Runnable task, final Runnable retired, final long delayTicks) {
        return tasks.executeDelayed(task, retired, delayTicks);
    }

    @Override
    public boolean isOwnedByCurrentThread() {
        final Location at = location;
        return !isRemoved() && server().scheduler().isOwnedByCurrentThread(at.world().key(), at.chunk());
    }

    /// Runs the task on the thread owning this entity; completes with `fallback` if the entity is removed first.
    public final <T> CompletableFuture<T> supply(final Supplier<T> task, final T fallback) {
        final CompletableFuture<T> result = new CompletableFuture<>();
        if (!execute(() -> result.complete(task.get()), () -> result.complete(fallback))) {
            result.complete(fallback);
        }
        return result;
    }
}
