package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.debug.DebugValues;
import fr.euphyllia.fidorial.server.debug.EntityDebugState;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.GoalSelector;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityPosPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityPosRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityRotPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundRotateHeadPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.ai.Goals;
import fr.fidorial.entity.ai.Navigator;
import fr.fidorial.entity.mob.Mob;
import fr.fidorial.entity.mob.MobDefinition;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.EntityTypeTagKeys;
import fr.fidorial.world.ChunkPos;
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

    /**
     * How fast a head turns towards what the mob looks at, in degrees per tick, as in Vanilla.
     */
    public static final float HEAD_TURN_SPEED = 10.0f;
    /**
     * How far a mob looks up or down, in degrees, as in Vanilla.
     */
    public static final float MAX_HEAD_PITCH = 40.0f;
    /**
     * How far a head turns away from the body, in degrees, as in Vanilla.
     */
    public static final float MAX_HEAD_YAW = 75.0f;

    private static final int LOOK_COOLDOWN_TICKS = 2;
    private static final float HEAD_STABLE_ANGLE = 15.0f;
    private static final int BODY_FACES_HEAD_DELAY_TICKS = 10;
    private static final int BODY_FACES_HEAD_DURATION_TICKS = 10;

    /**
     * How close a player has to be for a mob to stay active, as in Vanilla.
     */
    private static final double ACTIVITY_RANGE = 32.0;
    private static final int ACTIVITY_SCAN_INTERVAL = 10;

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
    private float bodyYaw;

    private double lookX;
    private double lookY;
    private double lookZ;
    private float lookYawSpeed;
    private float lookPitchLimit;
    private int lookCooldown;
    private float lastStableHeadYaw;
    private int headStableTicks;

    private boolean jumpRequested;
    private int noActionTime;

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
        this.bodyYaw = yaw;
        this.lastStableHeadYaw = yaw;
        setHeadYaw(yaw);
        this.walksOnPowderSnow = server().registries().registry(RegistryKey.ENTITY_TYPE)
                .isTagged(type.key(), EntityTypeTagKeys.POWDER_SNOW_WALKABLE_MOBS);
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

    /**
     * {@return {@code true} if this mob, standing at a point, would overlap the shape of a block}
     */
    protected final boolean isBoxBlocked(final double x, final double y, final double z) {
        return BlockCollider.collides(serverWorld(), collisionShapes(), BlockCollider.Box.at(x, y, z, halfWidth(), height()));
    }

    /**
     * Moves this mob, standing at a point, as far as the blocks around let it towards where it heads.
     *
     * @param stepHeight how high a block it climbs without jumping, {@code 0} for none
     * @param onGround   whether it stands on something before the move
     * @return how far it got
     */
    protected final BlockCollider.Movement collide(final double x, final double y, final double z,
                                                   final double dx, final double dy, final double dz,
                                                   final double stepHeight, final boolean onGround) {
        return BlockCollider.move(serverWorld(), collisionShapes(), BlockCollider.Box.at(x, y, z, halfWidth(), height()),
                dx, dy, dz, stepHeight, onGround);
    }

    /**
     * {@return the shapes this mob bumps into}: those of the blocks, powder snow being solid to the
     * mobs that walk on it
     */
    protected final BlockCollider.Shapes collisionShapes() {
        return walksOnPowderSnow ? BlockCollider.Shapes.POWDER_SNOW_WALKER : BlockCollider.Shapes.DEFAULT;
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

    /**
     * Turns the whole mob at once: its body and its head.
     */
    protected final void setRotation(final float yaw, final float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.bodyYaw = yaw;
        setHeadYaw(yaw);
    }

    /**
     * Turns the mob towards where it walks; its head follows through {@link #tickLookControl}.
     */
    protected final void setYaw(final float yaw) {
        this.yaw = yaw;
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
            this.bodyYaw = yaw;
            setHeadYaw(yaw);
            setLookTarget(x, y, z);
        }
    }

    /**
     * Makes this mob turn its head towards a point, as vanilla's look control: the head turns at
     * {@value #HEAD_TURN_SPEED} degrees per tick, the body follows it, and the mob looks away again
     * two ticks after the last call.
     */
    public final void setLookTarget(final double x, final double y, final double z) {
        setLookTarget(x, y, z, HEAD_TURN_SPEED, MAX_HEAD_PITCH);
    }

    /**
     * @param yawSpeed   how fast the head turns, in degrees per tick
     * @param pitchLimit how far the mob looks up or down, in degrees
     * @see #setLookTarget(double, double, double)
     */
    public final void setLookTarget(final double x, final double y, final double z,
                                    final float yawSpeed, final float pitchLimit) {
        this.lookX = x;
        this.lookY = y;
        this.lookZ = z;
        this.lookYawSpeed = yawSpeed;
        this.lookPitchLimit = pitchLimit;
        this.lookCooldown = LOOK_COOLDOWN_TICKS;
    }

    /**
     * Makes this mob turn its head towards the eyes of an entity.
     *
     * @see #setLookTarget(double, double, double)
     */
    public final void setLookTarget(final Entity entity, final float yawSpeed, final float pitchLimit) {
        final Location other = entity.location();
        setLookTarget(other.x(), other.y() + eyeHeightOf(entity), other.z(), yawSpeed, pitchLimit);
    }

    /**
     * {@return how high above its feet an entity has its eyes}
     */
    public static double eyeHeightOf(final Entity entity) {
        if (entity instanceof final AbstractMovingMob mob) {
            return mob.eyeHeight();
        }
        if (entity instanceof final ServerPlayer player) {
            return player.eyeHeight();
        }
        return 0.0;
    }

    /**
     * {@return how high above its feet this mob has its eyes}; vanilla's default is 85% of the height
     */
    public double eyeHeight() {
        return height() * 0.85;
    }

    /**
     * {@return the direction the body of this mob faces, which follows its head when it stands still}
     */
    public final float bodyYaw() {
        return bodyYaw;
    }

    /**
     * Turns the head towards what the mob looks at, or back in front of it, then the body after the
     * head, as vanilla's look and body rotation controls.
     *
     * @param navigating whether the mob follows a path
     * @param moved      whether the mob moved horizontally this tick
     */
    protected final void tickLookControl(final boolean navigating, final boolean moved) {
        float head = headYaw();
        float newPitch = 0f;
        if (lookCooldown > 0) {
            lookCooldown--;
            final Location self = location();
            final double dx = lookX - self.x();
            final double dy = lookY - (self.y() + eyeHeight());
            final double dz = lookZ - self.z();
            if (Math.abs(dx) > 1.0E-5 || Math.abs(dz) > 1.0E-5) {
                head = rotateTowards(head, (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f, lookYawSpeed);
            }
            final double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (Math.abs(dy) > 1.0E-5 || horizontal > 1.0E-5) {
                newPitch = rotateTowards(0f, (float) -Math.toDegrees(Math.atan2(dy, horizontal)), lookPitchLimit);
            }
        } else {
            head = rotateTowards(head, bodyYaw, HEAD_TURN_SPEED);
        }
        if (navigating) {
            head = clampAround(head, bodyYaw, MAX_HEAD_YAW);
        }

        if (moved) {
            bodyYaw = yaw;
            head = clampAround(head, bodyYaw, MAX_HEAD_YAW);
            lastStableHeadYaw = head;
            headStableTicks = 0;
        } else if (Math.abs(head - lastStableHeadYaw) > HEAD_STABLE_ANGLE) {
            headStableTicks = 0;
            lastStableHeadYaw = head;
            bodyYaw = clampAround(bodyYaw, head, MAX_HEAD_YAW);
        } else if (++headStableTicks > BODY_FACES_HEAD_DELAY_TICKS) {
            final float turned = Math.clamp(
                    (headStableTicks - BODY_FACES_HEAD_DELAY_TICKS) / (float) BODY_FACES_HEAD_DURATION_TICKS, 0f, 1f);
            bodyYaw = clampAround(bodyYaw, head, MAX_HEAD_YAW * (1f - turned));
        }

        setHeadYaw(head);
        this.pitch = newPitch;
    }

    /**
     * {@return an angle turned towards another by at most {@code maxTurn} degrees}
     */
    protected static float rotateTowards(final float from, final float to, final float maxTurn) {
        return from + Math.clamp(wrapDegrees(to - from), -maxTurn, maxTurn);
    }

    /**
     * {@return an angle brought within {@code maxDifference} degrees of another}
     */
    protected static float clampAround(final float angle, final float center, final float maxDifference) {
        final float difference = wrapDegrees(center - angle);
        return center - Math.clamp(difference, -maxDifference, maxDifference);
    }

    protected static float wrapDegrees(final float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }

    /**
     * Asks this mob to jump on its next move: off the ground, or up through the water it floats in.
     */
    public final void requestJump() {
        this.jumpRequested = true;
    }

    protected final boolean consumeJumpRequest() {
        final boolean requested = jumpRequested;
        jumpRequested = false;
        return requested;
    }

    /**
     * {@return for how many ticks no player has been within {@value #ACTIVITY_RANGE} blocks}; as in
     * Vanilla, a mob left alone for a while stops strolling
     */
    public final int noActionTime() {
        return noActionTime;
    }

    protected final void tickNoActionTime(final long currentTick) {
        noActionTime++;
        if ((currentTick + entityId()) % ACTIVITY_SCAN_INTERVAL == 0 && nearestPlayer(ACTIVITY_RANGE) != null) {
            noActionTime = 0;
        }
    }

    protected final void resetNoActionTime() {
        noActionTime = 0;
    }

    /**
     * {@return how deep this mob stands in water, {@code 0} when it does not touch any}
     */
    public final double waterHeight() {
        final Location loc = location();
        final double half = halfWidth() - 1.0E-3;
        final int minX = (int) Math.floor(loc.x() - half);
        final int maxX = (int) Math.floor(loc.x() + half);
        final int minY = (int) Math.floor(loc.y());
        final int maxY = (int) Math.floor(loc.y() + height());
        final int minZ = (int) Math.floor(loc.z() - half);
        final int maxZ = (int) Math.floor(loc.z() + half);
        final ServerWorld world = serverWorld();
        double surface = Double.NEGATIVE_INFINITY;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    final double top = BlockView.waterSurface(world, x, y, z);
                    if (!Double.isNaN(top) && top > loc.y()) {
                        surface = Math.max(surface, top);
                    }
                }
            }
        }
        return surface == Double.NEGATIVE_INFINITY ? 0.0 : Math.min(surface - loc.y(), height());
    }

    /**
     * {@return {@code true} if this mob touches water}
     */
    public final boolean isInWater() {
        return waterHeight() > 0.0;
    }

    /**
     * {@return how deep in water this mob starts to swim}: {@code 0.4} blocks as in Vanilla, any water
     * for the mobs whose eyes are lower than that
     */
    public double swimThreshold() {
        return eyeHeight() < 0.4 ? 0.0 : 0.4;
    }

    /**
     * {@return {@code true} if this mob is deep enough in water to swim}
     */
    public final boolean isDeepEnoughToSwim() {
        return waterHeight() > swimThreshold();
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
        final List<ServerPlayer> players = server().players().online();
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
        final List<String> behaviors = new ArrayList<>(2);
        for (final Goal running : goals.running()) {
            behaviors.add(GoalSelector.debugName(running));
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
                type().key().key().value() + "#" + entityId(), health(), maxHealth(), behaviors, memories);
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
            server().workers().regionizer().moveTicket(serverWorld().dimension().id(), fromChunk, toChunk);
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
                    headYaw(),
                    onGround));
            sentX = current.x();
            sentY = current.y();
            sentZ = current.z();
            sentYaw = yaw;
            sentPitch = pitch;
            sentHeadYaw = headYaw();
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

        final float headYaw = headYaw();
        if (Math.abs(headYaw - sentHeadYaw) > 1.0f) {
            sendToTrackers(new ClientboundRotateHeadPacket(entityId(), headYaw));
            sentHeadYaw = headYaw;
        }
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        server().debug().entities().sendCurrent(this, connection);
    }
}
