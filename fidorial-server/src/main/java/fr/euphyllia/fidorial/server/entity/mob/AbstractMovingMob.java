package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.debug.DebugValues;
import fr.euphyllia.fidorial.server.debug.EntityDebugState;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.GoalSelector;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityPosPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityPosRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRotateHeadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.Player;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.ai.Goals;
import fr.fidorial.entity.ai.Navigator;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.entity.mob.MobDefinition;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

public abstract class AbstractMovingMob extends AbstractMob implements Mob {

    private static final int POSITION_SYNC_INTERVAL = 100;
    private static final double MAX_RELATIVE_DELTA = 7.9;

    private static final double DEFAULT_HALF_WIDTH = 0.3;
    private static final double DEFAULT_HEIGHT = 1.7;
    private static final double DEFAULT_MOVEMENT_SPEED = 0.25;

    private static final Key POWDER_SNOW_WALKABLE = Key.key("powder_snow_walkable_mobs");

    protected final GoalSelector goals = new GoalSelector();

    private @Nullable ServerPlayer target;
    private double moveSpeed;
    private double followRangeOverride = -1.0;

    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private boolean onGround;
    private float yaw;
    private float pitch;

    private double sentX;
    private double sentY;
    private double sentZ;
    private float sentYaw;
    private float sentPitch;
    private float sentHeadYaw;
    private int ticksSinceSync;
    private final boolean walksOnPowderSnow;
    private final EntityDebugState debugState = new EntityDebugState();

    protected AbstractMovingMob(final int entityId, final UUID uuid, final EntityType type,
                                final Location location, final float maxHealth) {
        super(entityId, uuid, type, location, maxHealth);
        this.yaw = location.yaw();
        this.pitch = location.pitch();
        this.sentX = location.x();
        this.sentY = location.y();
        this.sentZ = location.z();
        this.sentYaw = yaw;
        this.sentPitch = pitch;
        this.sentHeadYaw = yaw;
        this.walksOnPowderSnow = server().registries().frozen()
                .isTagged(RegistryKey.ENTITY_TYPE.key(), POWDER_SNOW_WALKABLE, type.key());
    }

    public final ServerWorld serverWorld() {
        return (ServerWorld) world();
    }

    @Override
    public final FidorialServer server() {
        return FidorialServer.getInstance();
    }

    @Override
    public Optional<MobDefinition> definition() {
        return Optional.empty();
    }

    @Override
    public final Goals goals() {
        return goals;
    }

    @Override
    public Navigator navigation() {
        return Navigator.NONE;
    }

    @Override
    public final @Nullable ServerPlayer target() {
        return target;
    }

    @Override
    public final void setTarget(final @Nullable Player target) {
        this.target = target instanceof final ServerPlayer serverPlayer ? serverPlayer : null;
    }

    @Override
    public final double followRange() {
        return followRangeOverride >= 0.0 ? followRangeOverride : defaultFollowRange();
    }

    @Override
    public final void setFollowRange(final double range) {
        this.followRangeOverride = range;
    }

    protected double defaultFollowRange() {
        return 16.0;
    }

    @Override
    public double movementSpeed() {
        return DEFAULT_MOVEMENT_SPEED;
    }

    @Override
    public final void setMoveSpeed(final double speed) {
        this.moveSpeed = speed;
    }

    protected final double moveSpeed() {
        return moveSpeed;
    }

    @Override
    public double height() {
        return DEFAULT_HEIGHT;
    }

    @Override
    public double width() {
        return halfWidth() * 2.0;
    }

    protected double halfWidth() {
        return DEFAULT_HALF_WIDTH;
    }

    protected final boolean isBoxBlocked(final double x, final double y, final double z) {
        final double half = halfWidth();
        final int minBlockY = (int) Math.floor(y);
        final int maxBlockY = (int) Math.floor(y + height() - 0.01);
        final ServerWorld world = serverWorld();
        for (int blockY = minBlockY; blockY <= maxBlockY; blockY++) {
            if (isBlocking(world, (int) Math.floor(x - half), blockY, (int) Math.floor(z - half))
                    || isBlocking(world, (int) Math.floor(x + half), blockY, (int) Math.floor(z - half))
                    || isBlocking(world, (int) Math.floor(x - half), blockY, (int) Math.floor(z + half))
                    || isBlocking(world, (int) Math.floor(x + half), blockY, (int) Math.floor(z + half))) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlocking(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        if (state == null) {
            return true;
        }
        if (BlockView.isPassable(state)) {
            return false;
        }
        return walksOnPowderSnow || !BlockView.isPowderSnow(state);
    }

    public final boolean walksOnPowderSnow() {
        return walksOnPowderSnow;
    }

    public final boolean isInPowderSnow() {
        final Location loc = location();
        final double half = halfWidth() - 1.0E-5;
        final int minX = (int) Math.floor(loc.x() - half);
        final int maxX = (int) Math.floor(loc.x() + half);
        final int minY = (int) Math.floor(loc.y() + 1.0E-5);
        final int maxY = (int) Math.floor(loc.y() + height() - 1.0E-5);
        final int minZ = (int) Math.floor(loc.z() - half);
        final int maxZ = (int) Math.floor(loc.z() + half);
        final ServerWorld world = serverWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final BlockState state = BlockView.blockAt(world, x, y, z);
                    if (state != null && BlockView.isPowderSnow(state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public final double velocityX() {
        return this.velocityX;
    }

    @Override
    public final double velocityY() {
        return this.velocityY;
    }

    @Override
    public final double velocityZ() {
        return this.velocityZ;
    }

    @Override
    public final void setVelocity(final double x, final double y, final double z) {
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
    }

    @Override
    public final boolean onGround() {
        return this.onGround;
    }

    public final void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }

    public final float yaw() {
        return this.yaw;
    }

    public final float pitch() {
        return this.pitch;
    }

    protected final void setRotation(final float yaw, final float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public final void lookAt(final double x, final double y, final double z) {
        final Location self = location();
        final double dx = x - self.x();
        final double dy = y - (self.y() + 1.2);
        final double dz = z - self.z();
        final double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal > 1.0E-4 || Math.abs(dy) > 1.0E-4) {
            this.yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            this.pitch = (float) Math.toDegrees(-Math.atan2(dy, horizontal));
        }
    }

    @Override
    public final double distanceSqTo(final Entity entity) {
        final Location self = location();
        final Location other = entity.location();
        final double dx = self.x() - other.x();
        final double dy = self.y() - other.y();
        final double dz = self.z() - other.z();
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public final @Nullable ServerPlayer nearestPlayer(final double maxDistance) {
        final List<ServerPlayer> players = server().players();
        final double maxDistSq = maxDistance < 0.0 ? Double.MAX_VALUE : maxDistance * maxDistance;
        ServerPlayer best = null;
        double bestDistSq = Double.MAX_VALUE;
        for (final ServerPlayer player : players) {
            if (player.isRemoved() || player.isDead()
                    || player.gameMode() == GameMode.SPECTATOR
                    || !player.world().equals(world())) {
                continue;
            }
            final double distSq = distanceSqTo(player);
            if (distSq <= maxDistSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                best = player;
            }
        }
        return best;
    }

    @Override
    public final boolean hasLineOfSightTo(final Entity entity) {
        final Location self = location();
        final Location other = entity.location();
        return BlockView.hasLineOfSight(serverWorld(),
                self.x(), self.y() + 1.2, self.z(),
                other.x(), other.y() + 1.5, other.z());
    }

    public final EntityDebugState debugState() {
        return debugState;
    }

    public final GoalSelector goalSelector() {
        return goals;
    }

    public DebugValues.BrainInfo brainSnapshot() {
        final List<String> behaviors = new ArrayList<>(1);
        final Goal active = goals.active();
        if (active != null) {
            behaviors.add(GoalSelector.debugName(active));
        }
        final List<String> memories = new ArrayList<>(2);
        final ServerPlayer currentTarget = target;
        if (currentTarget != null) {
            memories.add("attack_target: " + currentTarget.name());
        }
        final Position waypoint = navigation().currentWaypoint();
        if (waypoint != null) {
            memories.add("walk_target: " + waypoint.x() + ", " + waypoint.y() + ", " + waypoint.z());
        }
        return DebugValues.BrainInfo.forMob(
                type().key().value() + "#" + entityId(), health(), maxHealth(), behaviors, memories);
    }

    public final void forEachIntersectedBlock(final BiConsumer<BlockPosition, DebugValues.BlockIntersection> action) {
        final Location loc = location();
        final double half = halfWidth() - 1.0E-5;
        final int minX = (int) Math.floor(loc.x() - half);
        final int maxX = (int) Math.floor(loc.x() + half);
        final int minY = (int) Math.floor(loc.y() + 1.0E-5);
        final int maxY = (int) Math.floor(loc.y() + height() - 1.0E-5);
        final int minZ = (int) Math.floor(loc.z() - half);
        final int maxZ = (int) Math.floor(loc.z() + half);
        final ServerWorld world = serverWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final BlockState state = BlockView.blockAt(world, x, y, z);
                    if (state == null) {
                        continue;
                    }
                    final DebugValues.BlockIntersection kind = state.isAir() ? DebugValues.BlockIntersection.IN_AIR
                            : state.isFluid() ? DebugValues.BlockIntersection.IN_FLUID
                            : DebugValues.BlockIntersection.IN_BLOCK;
                    action.accept(Position.block(x, y, z), kind);
                }
            }
        }
    }

    protected final void updateChunkMembership(final Location before, final Location after) {
        final ChunkPos fromChunk = before.chunk();
        final ChunkPos toChunk = after.chunk();
        if (!fromChunk.equals(toChunk)) {
            serverWorld().entityMoved(this, fromChunk, toChunk);
            server().regionizer().moveTicket(serverWorld().dimension().id(), fromChunk, toChunk);
        }
    }

    protected final void syncToClients() {
        final Location current = location();
        final PositionData.Vec3D currentVec = LocationPositionData.vec3(current);
        final double dx = current.x() - sentX;
        final double dy = current.y() - sentY;
        final double dz = current.z() - sentZ;
        final boolean moved = Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1.0 / 4096.0;
        final boolean rotated = Math.abs(yaw - sentYaw) > 1.0f || Math.abs(pitch - sentPitch) > 1.0f;
        ticksSinceSync++;

        final boolean needsAbsoluteSync = ticksSinceSync >= POSITION_SYNC_INTERVAL
                || Math.abs(dx) > MAX_RELATIVE_DELTA
                || Math.abs(dy) > MAX_RELATIVE_DELTA
                || Math.abs(dz) > MAX_RELATIVE_DELTA;

        if (needsAbsoluteSync && (moved || rotated || ticksSinceSync >= POSITION_SYNC_INTERVAL)) {
            sendToTrackers(new ClientboundEntityPositionSyncPacket(
                    entityId(),
                    new PositionData.LinearPositionPath(currentVec),
                    new PositionData.FloatRotation(yaw, pitch),
                    onGround));
            sentX = current.x();
            sentY = current.y();
            sentZ = current.z();
            sentYaw = yaw;
            sentPitch = pitch;
            ticksSinceSync = 0;
        } else if (moved) {
            final PositionData.DeltaVec3D delta = PositionData.DeltaVec3D.between(new PositionData.Vec3D(sentX, sentY, sentZ), currentVec);
            if (delta == null) {
                throw new IllegalStateException("Unexpected delta size");
            }
            if (rotated) {
                sendToTrackers(new ClientboundMoveEntityPosRotPacket(
                        entityId(), delta, new PositionData.AngleRotation(yaw, pitch), onGround));
                sentYaw = yaw;
                sentPitch = pitch;
            } else {
                sendToTrackers(new ClientboundMoveEntityPosPacket(entityId(), delta, onGround));
            }

            sentX += delta.x() / 4096.0;
            sentY += delta.y() / 4096.0;
            sentZ += delta.z() / 4096.0;
        } else if (rotated) {
            sendToTrackers(new ClientboundMoveEntityRotPacket(entityId(), new PositionData.AngleRotation(yaw, pitch), onGround));
            sentYaw = yaw;
            sentPitch = pitch;
        }

        if (Math.abs(yaw - sentHeadYaw) > 1.0f) {
            sendToTrackers(new ClientboundRotateHeadPacket(entityId(), yaw));
            sentHeadYaw = yaw;
        }
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        server().entityDebug().sendCurrent(this, connection);
    }
}
