package fr.euphyllia.fidorial.server.entity.item;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityPosPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMotionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTakeItemEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.ExperienceOrb;
import fr.fidorial.entity.GameMode;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An orb of experience ({@code minecraft:experience_orb}), as in Vanilla: it falls and bounces,
 * flies to the closest player within 8 blocks, merges with orbs of the same value and gives its
 * value to the player who touches it, once every other tick.
 */
public final class ServerExperienceOrb extends AbstractEntity implements ExperienceOrb {

    /**
     * Orb entity data: its value, after the 8 entries every entity has.
     */
    private static final int METADATA_VALUE = 8;

    private static final double HALF_WIDTH = 0.25;
    private static final double HEIGHT = 0.5;
    private static final double EYE_HEIGHT = HEIGHT * 0.85;

    private static final double GRAVITY = 0.03;
    private static final double AIR_DRAG = 0.98;
    private static final double GROUND_FRICTION = 0.6;
    private static final double BOUNCE = 0.4;
    private static final double WATER_DRAG = 0.99;
    private static final double WATER_LIFT = 5.0E-4;
    private static final double MAX_WATER_RISE = 0.06;
    private static final double LAVA_HOP = 0.2;

    private static final double FOLLOW_RANGE_SQ = (double) FOLLOW_RANGE * FOLLOW_RANGE;
    private static final double FOLLOW_ACCELERATION = 0.1;

    /**
     * Vanilla only lets an orb merge with one orb in 40, so that a big pile keeps several orbs.
     */
    public static final int ORB_GROUPS_PER_AREA = 40;
    private static final double MERGE_DISTANCE = 0.5;
    private static final int MERGE_INTERVAL = 20;

    private static final int DEFAULT_HEALTH = 5;
    private static final int LAVA_DAMAGE = 4;
    private static final int FIRE_DAMAGE = 1;

    /**
     * A player takes an orb from this far around their hitbox, sideways then up and down.
     */
    private static final double PICKUP_REACH_SIDE = 1.0;
    private static final double PICKUP_REACH_VERTICAL = 0.5;
    private static final double PLAYER_HALF_WIDTH = 0.3;
    private static final double PLAYER_HEIGHT = 1.8;

    /**
     * How far below the bottom of the world an orb falls before it is gone.
     */
    private static final int VOID_DEPTH = 64;

    /**
     * The longest the client goes without a full position.
     */
    private static final int POSITION_SYNC_INTERVAL = 60;
    /**
     * How much the speed of an orb must change for the client to be told.
     */
    private static final double VELOCITY_SYNC_THRESHOLD = 0.05;

    private final int value;
    private volatile int count = 1;
    private volatile int age;
    private volatile int health = DEFAULT_HEALTH;

    private volatile double velocityX;
    private volatile double velocityY;
    private volatile double velocityZ;
    private volatile boolean onGround;

    private @Nullable ServerPlayer followingPlayer;
    private int ticksExisted;
    private double sentX;
    private double sentY;
    private double sentZ;
    private double sentVelocityX;
    private double sentVelocityY;
    private double sentVelocityZ;
    private boolean sentOnGround;
    private int ticksSinceSync;

    public ServerExperienceOrb(final int entityId, final UUID uuid, final Location location, final int value) {
        super(entityId, uuid, EntityTypes.EXPERIENCE_ORB, location);
        if (value < 0) {
            throw new IllegalArgumentException("An experience orb cannot be worth less than nothing, got " + value);
        }
        this.value = value;
        this.sentX = location.x();
        this.sentY = location.y();
        this.sentZ = location.z();
    }

    public ServerExperienceOrb(final int entityId, final Location location, final int value) {
        this(entityId, UUID.randomUUID(), location, value);
    }

    /**
     * Throws this orb a little way in a random direction, as vanilla does with a new orb.
     */
    public void scatter() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        setVelocity(
                (random.nextDouble() * 0.2 - 0.1) * 2.0,
                random.nextDouble() * 0.2 * 2.0,
                (random.nextDouble() * 0.2 - 0.1) * 2.0);
    }

    @Override
    public int value() {
        return value;
    }

    @Override
    public int count() {
        return count;
    }

    public void setCount(final int count) {
        this.count = Math.max(1, count);
    }

    @Override
    public int age() {
        return age;
    }

    @Override
    public void setAge(final int ticks) {
        this.age = Math.max(0, ticks);
    }

    public int health() {
        return health;
    }

    public void setHealth(final int health) {
        this.health = health;
    }

    public double velocityX() {
        return velocityX;
    }

    public double velocityY() {
        return velocityY;
    }

    public double velocityZ() {
        return velocityZ;
    }

    public void setVelocity(final double x, final double y, final double z) {
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
    }

    public boolean onGround() {
        return onGround;
    }

    public void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public Component displayName() {
        return Component.translatable("entity.minecraft.experience_orb");
    }

    @Override
    public Sound.Source soundSource() {
        return Sound.Source.AMBIENT;
    }

    /**
     * {@return {@code true} if this orb may take experience of a value in}: as in Vanilla, an orb of
     * that value, one in {@value #ORB_GROUPS_PER_AREA} picked by {@code group}
     */
    public boolean canMerge(final int group, final int otherValue) {
        return !isRemoved() && (entityId() - group) % ORB_GROUPS_PER_AREA == 0 && value == otherValue;
    }

    /**
     * {@return {@code true} if this orb touches the cube of one block around a point}, where vanilla
     * looks for an orb to take new experience in
     */
    public boolean touchesBoxAround(final double x, final double y, final double z) {
        final Location self = location();
        return Math.abs(self.x() - x) < 0.5 + HALF_WIDTH
                && Math.abs(self.z() - z) < 0.5 + HALF_WIDTH
                && self.y() < y + 0.5 && self.y() + HEIGHT > y - 0.5;
    }

    /**
     * Makes this orb give its value once more, and stay as long as a new orb would.
     */
    @SuppressWarnings("NonAtomicVolatileUpdate") // only written by the owning region thread
    public void absorbOne() {
        count++;
        age = 0;
        markSaveDirty();
    }

    @Override
    @SuppressWarnings("NonAtomicVolatileUpdate") // only written by the owning region thread
    public void tick(final long currentTick) {
        if (isRemoved() || !(world() instanceof final ServerWorld world)) {
            return;
        }
        ticksExisted++;

        final Location before = location();
        if (before.y() < world.minY() - VOID_DEPTH) {
            discard();
            return;
        }
        final BlockState inside = BlockView.blockAt(world, floor(before.x()), floor(before.y()), floor(before.z()));
        if (inside == null) {
            // Its chunk is going away: stay still until it is saved.
            return;
        }

        final boolean inLava = BlockTypeKeys.LAVA.equals(inside.name());
        if (inLava || BlockView.isTagged(BlockTypeTagKeys.FIRE, inside)) {
            health -= inLava ? LAVA_DAMAGE : FIRE_DAMAGE;
            if (health <= 0) {
                discard();
                return;
            }
        }

        final boolean colliding = collides(world, before.x(), before.y(), before.z());
        final BlockState atEyes = BlockView.blockAt(world, floor(before.x()), floor(before.y() + EYE_HEIGHT), floor(before.z()));
        if (atEyes != null && BlockView.isWater(atEyes)) {
            velocityX *= WATER_DRAG;
            velocityY = Math.min(velocityY + WATER_LIFT, MAX_WATER_RISE);
            velocityZ *= WATER_DRAG;
        } else if (!colliding) {
            velocityY -= GRAVITY;
        }
        if (inLava) {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            velocityX = random.nextDouble(-LAVA_HOP, LAVA_HOP);
            velocityY = LAVA_HOP;
            velocityZ = random.nextDouble(-LAVA_HOP, LAVA_HOP);
        }

        if (ticksExisted % MERGE_INTERVAL == 1) {
            mergeWithNeighbours(world);
        }
        followNearbyPlayer(before);

        move(world, before, colliding);

        if (++age >= DEFAULT_LIFETIME) {
            discard();
            return;
        }
        tryPickup(world);
        if (!isRemoved()) {
            syncToClients();
        }
    }

    /**
     * Pulls this orb towards the closest player within {@value #FOLLOW_RANGE} blocks, the harder the
     * closer they are; it keeps the player it follows until they are out of range.
     */
    @SuppressWarnings("NonAtomicVolatileUpdate") // only written by the owning region thread
    private void followNearbyPlayer(final Location self) {
        ServerPlayer player = followingPlayer;
        if (player == null || !attracts(player) || distanceSq(self, player.location()) > FOLLOW_RANGE_SQ) {
            player = nearestPlayer(self);
            followingPlayer = player;
        }
        if (player == null) {
            return;
        }
        final Location target = player.location();
        final double dx = target.x() - self.x();
        final double dy = target.y() + player.eyeHeight() / 2.0 - self.y();
        final double dz = target.z() - self.z();
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-4) {
            return;
        }
        final double power = 1.0 - length / FOLLOW_RANGE;
        final double pull = power * power * FOLLOW_ACCELERATION / length;
        velocityX += dx * pull;
        velocityY += dy * pull;
        velocityZ += dz * pull;
    }

    private @Nullable ServerPlayer nearestPlayer(final Location self) {
        final List<ServerPlayer> players = server().players().online();
        ServerPlayer best = null;
        double bestDistSq = FOLLOW_RANGE_SQ;
        for (int i = 0, size = players.size(); i < size; i++) {
            final ServerPlayer player = players.get(i);
            if (!attracts(player)) {
                continue;
            }
            final double distSq = distanceSq(self, player.location());
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = player;
            }
        }
        return best;
    }

    private boolean attracts(final ServerPlayer player) {
        return !player.isRemoved() && !player.isDead() && !player.isAwaitingRespawn()
                && player.gameMode() != GameMode.SPECTATOR
                && player.world().equals(world());
    }

    private static double distanceSq(final Location a, final Location b) {
        final double dx = a.x() - b.x();
        final double dy = a.y() - b.y();
        final double dz = a.z() - b.z();
        return dx * dx + dy * dy + dz * dz;
    }

    @SuppressFBWarnings(
            value = "FE_FLOATING_POINT_EQUALITY",
            justification = "Exact floating-point comparison is intentional."
    )
    private void move(final ServerWorld world, final Location from, final boolean colliding) {
        double x = from.x();
        double y = from.y();
        final double z = from.z();

        if (colliding && followingPlayer == null) {
            // Stuck inside a block: climb out the top when there is room.
            final double top = Math.floor(y) + 1.0;
            if (!collides(world, x, top, z)) {
                y = top;
                velocityY = 0.0;
            }
        }

        final double fallSpeed = velocityY;
        final BlockCollider.Movement moved = BlockCollider.move(world, BlockCollider.Shapes.DEFAULT,
                BlockCollider.Box.at(x, y, z, HALF_WIDTH, HEIGHT), velocityX, velocityY, velocityZ, 0.0, onGround);
        double vx = velocityX;
        double vy = velocityY;
        double vz = velocityZ;
        if (Math.abs(moved.x() - vx) > 1.0E-5) {
            vx = 0.0;
        }
        if (Math.abs(moved.z() - vz) > 1.0E-5) {
            vz = 0.0;
        }
        final boolean landed = moved.y() != vy && vy < 0.0;
        if (moved.y() != vy) {
            vy = 0.0;
        }
        onGround = landed;

        final double friction = onGround ? AIR_DRAG * GROUND_FRICTION : AIR_DRAG;
        vx *= friction;
        vy *= friction;
        vz *= friction;
        if (landed && fallSpeed < -GRAVITY) {
            // as in Vanilla, an orb bounces back with 40% of the speed it landed at
            vy = -fallSpeed * BOUNCE;
        }
        velocityX = vx;
        velocityY = vy;
        velocityZ = vz;

        x += moved.x();
        y += moved.y();
        final double newZ = z + moved.z();
        if (x != from.x() || y != from.y() || newZ != from.z()) {
            final Location to = from.with(x, y, newZ);
            setLocation(to);
            final ChunkPos fromChunk = from.chunk();
            final ChunkPos toChunk = to.chunk();
            if (!fromChunk.equals(toChunk)) {
                world.entityMoved(this, fromChunk, toChunk);
            }
        }
    }

    private static boolean collides(final ServerWorld world, final double x, final double y, final double z) {
        return BlockCollider.collides(world, BlockCollider.Shapes.DEFAULT, BlockCollider.Box.at(x, y, z, HALF_WIDTH, HEIGHT));
    }

    private void mergeWithNeighbours(final ServerWorld world) {
        final Location self = location();
        final ChunkPos chunk = self.chunk();
        final List<ServerExperienceOrb> neighbours = new ArrayList<>(4);
        world.entityManager().forEachInChunkRange(chunk.x(), chunk.z(), 1, entity -> {
            if (entity != this && entity instanceof final ServerExperienceOrb other
                    && other.canMerge(entityId(), value) && isMergeNeighbour(self, other.location())) {
                neighbours.add(other);
            }
        });
        for (final ServerExperienceOrb other : neighbours) {
            if (isRemoved()) {
                return;
            }
            if (!other.isRemoved() && other.isOwnedByCurrentThread()) {
                merge(other);
            }
        }
    }

    private static boolean isMergeNeighbour(final Location a, final Location b) {
        final double reach = HALF_WIDTH * 2 + MERGE_DISTANCE;
        return Math.abs(a.x() - b.x()) <= reach
                && Math.abs(a.z() - b.z()) <= reach
                && Math.abs(a.y() - b.y()) <= HEIGHT + MERGE_DISTANCE;
    }

    @SuppressWarnings("NonAtomicVolatileUpdate") // only written by the owning region thread
    private void merge(final ServerExperienceOrb other) {
        count += other.count;
        age = Math.min(age, other.age);
        other.discard();
        markSaveDirty();
    }

    private void tryPickup(final ServerWorld world) {
        final Location self = location();
        final ChunkPos chunk = self.chunk();
        final List<ServerPlayer> candidates = new ArrayList<>(2);
        world.entityManager().forEachInChunkRange(chunk.x(), chunk.z(), 1, entity -> {
            if (entity instanceof final ServerPlayer player && canBeTakenBy(player, self)) {
                candidates.add(player);
            }
        });
        for (final ServerPlayer player : candidates) {
            if (isRemoved()) {
                return;
            }
            giveTo(player);
        }
    }

    private boolean canBeTakenBy(final ServerPlayer player, final Location self) {
        if (!attracts(player) || !player.isOwnedByCurrentThread()) {
            return false;
        }
        final Location feet = player.location();
        final double sideReach = PLAYER_HALF_WIDTH + PICKUP_REACH_SIDE + HALF_WIDTH;
        return Math.abs(self.x() - feet.x()) < sideReach
                && Math.abs(self.z() - feet.z()) < sideReach
                && self.y() + HEIGHT > feet.y() - PICKUP_REACH_VERTICAL
                && self.y() < feet.y() + PLAYER_HEIGHT + PICKUP_REACH_VERTICAL;
    }

    /**
     * Gives the value of this orb to a player once, unless they took an orb a tick ago.
     */
    @SuppressWarnings("NonAtomicVolatileUpdate") // only written by the owning region thread
    private void giveTo(final ServerPlayer player) {
        if (!player.startTakingExperience()) {
            return;
        }
        // the client plays the pickup sound and flies the orb to the player
        final ClientboundTakeItemEntityPacket taken = new ClientboundTakeItemEntityPacket(entityId(), player.entityId(), 1);
        sendToTrackers(taken);
        // TODO: Mending repairs a damaged item with the value first, once enchantments can repair
        if (value > 0) {
            player.giveExperience(value);
        }
        if (--count <= 0) {
            discard();
        } else {
            markSaveDirty();
        }
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        final Location location = location();
        connection.send(new ClientboundAddEntityPacket(
                entityId(),
                uuid(),
                EntityTypes.networkId(type()),
                LocationPositionData.vec3(location),
                new PositionData.VelocityVec3D(velocityX, velocityY, velocityZ),
                0.0f,
                0.0f,
                0.0f,
                0));
        connection.send(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.varInt(METADATA_VALUE, value)));
    }

    private void syncToClients() {
        final Location current = location();
        final double dx = current.x() - sentX;
        final double dy = current.y() - sentY;
        final double dz = current.z() - sentZ;
        final boolean moved = Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1.0 / 4096.0;
        ticksSinceSync++;

        if (moved || onGround != sentOnGround || ticksSinceSync >= POSITION_SYNC_INTERVAL) {
            final PositionData.Vec3D currentVec = LocationPositionData.vec3(current);
            final PositionData.DeltaVec3D delta = moved && ticksSinceSync < POSITION_SYNC_INTERVAL
                    ? PositionData.DeltaVec3D.between(new PositionData.Vec3D(sentX, sentY, sentZ), currentVec)
                    : null;
            if (delta != null) {
                sendToTrackers(new ClientboundMoveEntityPosPacket(entityId(), delta, onGround));
                sentX += delta.x() / 4096.0;
                sentY += delta.y() / 4096.0;
                sentZ += delta.z() / 4096.0;
            } else {
                sendToTrackers(new ClientboundEntityPositionSyncPacket(
                        entityId(),
                        new PositionData.LinearPositionPath(currentVec),
                        new PositionData.FloatRotation(0.0f, 0.0f),
                        0.0f,
                        onGround));
                sentX = current.x();
                sentY = current.y();
                sentZ = current.z();
                ticksSinceSync = 0;
            }
            sentOnGround = onGround;
        }

        if (Math.abs(velocityX - sentVelocityX) + Math.abs(velocityY - sentVelocityY) + Math.abs(velocityZ - sentVelocityZ)
                > VELOCITY_SYNC_THRESHOLD) {
            sentVelocityX = velocityX;
            sentVelocityY = velocityY;
            sentVelocityZ = velocityZ;
            sendToTrackers(new ClientboundSetEntityMotionPacket(
                    entityId(), new PositionData.VelocityVec3D(velocityX, velocityY, velocityZ)));
        }
    }

    public void discard() {
        if (isRemoved()) {
            return;
        }
        if (isOwnedByCurrentThread()) {
            server().entities().despawn(this);
        } else {
            execute(() -> server().entities().despawn(this));
        }
    }

    private void markSaveDirty() {
        if (world() instanceof final ServerWorld world) {
            final ChunkPos chunk = chunk();
            world.markEntitiesDirty(chunk.x(), chunk.z());
        }
    }

    private static int floor(final double value) {
        return (int) Math.floor(value);
    }
}
