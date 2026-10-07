package fr.euphyllia.fidorial.server.entity.projectile;

import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.GameMode;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * The projectile thrown by a frostbite: it deals up to {@value #MAX_DAMAGE} damage, less as it slows down,
 * and shatters on the first block or entity it meets.
 *
 * <p>It is never saved with the chunk: an ice ball only lives for a few seconds.</p>
 */
public final class IceBall extends AbstractEntity {

    public static final float MAX_DAMAGE = 4.0f;

    // Flight of every thrown item (snowball, egg, ...).
    private static final double GRAVITY = 0.03;
    private static final double DRAG = 0.99;

    private static final int AIM_ROUNDS = 4;

    private static final double MAX_STEP = 0.25;
    private static final double HIT_MARGIN = 0.125;
    private static final int MAX_LIFETIME_TICKS = 200;

    private static final double PLAYER_WIDTH = 0.6;
    private static final double PLAYER_HEIGHT = 1.8;

    /** Entity event of a thrown item breaking: the client plays the break particles. */
    private static final byte ENTITY_EVENT_BREAK = 3;

    private final @Nullable AbstractLivingEntity owner;
    private final double launchSpeed;

    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private int age;

    public IceBall(final int entityId, final Location location, final @Nullable AbstractLivingEntity owner,
                   final double velocityX, final double velocityY, final double velocityZ) {
        super(entityId, UUID.randomUUID(), EntityTypes.ICE_BALL, location);
        this.owner = owner;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.launchSpeed = speed();
    }

    public static IceBall thrownAt(final int entityId, final Location from, final @Nullable AbstractLivingEntity owner,
                                   final double targetX, final double targetY, final double targetZ,
                                   final double speed) {
        final double dx = targetX - from.x();
        final double dy = targetY - from.y();
        final double dz = targetZ - from.z();
        final double horizontal = Math.sqrt(dx * dx + dz * dz);

        double flightTicks = Math.sqrt(dx * dx + dy * dy + dz * dz) / speed;
        double aimY = dy;
        for (int round = 0; round < AIM_ROUNDS; round++) {
            aimY = dy + GRAVITY * flightTicks * Math.max(0.0, flightTicks - 1.0) / 2.0;
            final double horizontalSpeed = horizontal * speed / Math.sqrt(dx * dx + aimY * aimY + dz * dz);
            if (horizontalSpeed < 1.0E-6) {
                break;
            }
            flightTicks = horizontal / horizontalSpeed;
        }

        final double length = Math.sqrt(dx * dx + aimY * aimY + dz * dz);
        if (length < 1.0E-6) {
            return new IceBall(entityId, from, owner, 0.0, 0.0, 0.0);
        }
        final double scale = speed / length;
        return new IceBall(entityId, from, owner, dx * scale, aimY * scale, dz * scale);
    }

    public @Nullable AbstractLivingEntity owner() {
        return owner;
    }

    @Override
    public Sound.Source soundSource() {
        return Sound.Source.NEUTRAL;
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved()) {
            return;
        }
        if (++age > MAX_LIFETIME_TICKS || !(world() instanceof final ServerWorld world)) {
            server().despawnEntity(this);
            return;
        }

        final Location from = location();
        double x = from.x();
        double y = from.y();
        double z = from.z();

        // Small steps, so that a fast ball does not fly through a wall or a mob.
        final int steps = Math.max(1, (int) Math.ceil(speed() / MAX_STEP));
        for (int step = 0; step < steps; step++) {
            final double nextX = x + velocityX / steps;
            final double nextY = y + velocityY / steps;
            final double nextZ = z + velocityZ / steps;

            final BlockState state = BlockView.blockAt(world,
                    (int) Math.floor(nextX), (int) Math.floor(nextY), (int) Math.floor(nextZ));
            if (state == null) {
                // Flew out of the loaded chunks.
                server().despawnEntity(this);
                return;
            }
            if (!BlockView.isPassable(state)) {
                moveTo(world, from, x, y, z);
                shatter();
                return;
            }

            x = nextX;
            y = nextY;
            z = nextZ;

            final AbstractLivingEntity hit = findHit(world, x, y, z);
            if (hit != null) {
                moveTo(world, from, x, y, z);
                server().combat().damage(hit, DamageSource.thrown(this, owner), damage());
                shatter();
                return;
            }
        }

        moveTo(world, from, x, y, z);
        velocityX *= DRAG;
        velocityY = velocityY * DRAG - GRAVITY;
        velocityZ *= DRAG;
    }

    /**
     * {@return the damage of a hit right now: the full {@value #MAX_DAMAGE} at launch speed, less once slowed down}
     */
    public float damage() {
        if (launchSpeed <= 0.0) {
            return MAX_DAMAGE;
        }
        return (float) (MAX_DAMAGE * Math.clamp(speed() / launchSpeed, 0.0, 1.0));
    }

    private double speed() {
        return Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
    }

    private void moveTo(final ServerWorld world, final Location from, final double x, final double y, final double z) {
        final Location to = from.with(x, y, z);
        setLocation(to);
        final ChunkPos fromChunk = from.chunk();
        final ChunkPos toChunk = to.chunk();
        if (!fromChunk.equals(toChunk)) {
            world.entityMoved(this, fromChunk, toChunk);
        }
    }

    private @Nullable AbstractLivingEntity findHit(final ServerWorld world, final double x, final double y, final double z) {
        final AbstractEntity hit = world.entityManager().findInChunkRange(
                (int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4, 1, entity -> canHit(entity, x, y, z));
        return (AbstractLivingEntity) hit;
    }

    private boolean canHit(final AbstractEntity entity, final double x, final double y, final double z) {
        if (entity == owner
                || !(entity instanceof final AbstractLivingEntity living)
                || living.isRemoved()
                || living.isDead()) {
            return false;
        }
        if (living instanceof final ServerPlayer player && player.gameMode() == GameMode.SPECTATOR) {
            return false;
        }
        return isInside(living, x, y, z);
    }

    private static boolean isInside(final AbstractLivingEntity entity, final double x, final double y, final double z) {
        final double width;
        final double height;
        if (entity instanceof final AbstractMovingMob mob) {
            width = mob.width();
            height = mob.height();
        } else if (entity instanceof ServerPlayer) {
            width = PLAYER_WIDTH;
            height = PLAYER_HEIGHT;
        } else {
            width = entity.type().width();
            height = entity.type().height();
        }
        final Location location = entity.location();
        final double half = width / 2.0 + HIT_MARGIN;
        return Math.abs(x - location.x()) <= half
                && Math.abs(z - location.z()) <= half
                && y >= location.y() - HIT_MARGIN
                && y <= location.y() + height + HIT_MARGIN;
    }

    private void shatter() {
        final Location location = location();
        sendToTrackers(new ClientboundEntityEventPacket(entityId(), ENTITY_EVENT_BREAK));
        sendToTrackers(new ClientboundSoundPacket(
                Sound.sound(SoundEvents.ICE_BALL_BREAK, soundSource(), 1.0f, 1.0f),
                location.x(), location.y(), location.z()));
        server().despawnEntity(this);
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        // The client simulates the flight itself: it needs the velocity, and the thrower as entity data.
        final Location location = location();
        connection.send(new ClientboundAddEntityPacket(
                entityId(),
                uuid(),
                EntityTypes.networkId(type()),
                LocationPositionData.vec3(location),
                new PositionData.VelocityVec3D(velocityX, velocityY, velocityZ),
                location.pitch(),
                location.yaw(),
                0.0f,
                owner == null ? 0 : owner.entityId()));
    }
}
