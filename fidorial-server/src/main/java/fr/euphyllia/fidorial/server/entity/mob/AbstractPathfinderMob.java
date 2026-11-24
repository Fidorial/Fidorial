package fr.euphyllia.fidorial.server.entity.mob;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Navigation;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.mob.MobConvertEventImpl;
import fr.euphyllia.fidorial.server.events.mob.MobTeleportEventImpl;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSwingAnimationPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockStepping;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.event.mob.MobConvertEvent;
import fr.fidorial.item.component.SwingAnimation;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.world.ChunkPos;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class AbstractPathfinderMob extends AbstractMovingMob {

    private static final double GRAVITY = 0.08;
    private static final double VERTICAL_DRAG = 0.98;
    /**
     * What is left of the horizontal speed of a mob after a tick on the ground, before the air
     * drag: the slipperiness of an ordinary block.
     */
    private static final double GROUND_SLIPPERINESS = 0.6;
    private static final double AIR_FRICTION = 0.91;
    /**
     * How much a mob accelerates in the air and in water or lava for a full input.
     */
    private static final double AIR_ACCELERATION = 0.02;
    private static final double FLUID_ACCELERATION = 0.02;
    private static final double WATER_DRAG = 0.8;
    /**
     * What is left of the speed of a mob after a tick in lava; in lava too shallow to swim in, it
     * keeps more of its vertical speed.
     */
    private static final double LAVA_DRAG = 0.5;
    private static final double SHALLOW_LAVA_VERTICAL_DRAG = 0.8;
    /**
     * Vanilla scales the input of a living entity by this factor every tick.
     */
    private static final double INPUT_SCALE = 0.98;
    private static final double JUMP_VELOCITY = 0.42;
    private static final double SWIM_UP_VELOCITY = 0.04;
    private static final double LEAVE_FLUID_VELOCITY = 0.3;

    /**
     * How a mob that swims moves: it drifts up by {@code SWIM_LIFT} a tick when it rises, gains an
     * eighth of the speed it lacks each tick, is pulled to its waypoint by its speed times the
     * distance along each axis, pushes forward under water, and loses a tenth of its speed a tick.
     */
    private static final double SWIM_LIFT = 0.002;
    private static final double SWIM_SPEED_GAIN = 0.125;
    private static final double SWIM_HORIZONTAL_PULL = 0.005;
    private static final double SWIM_VERTICAL_PULL = 0.1;
    private static final double SWIM_ACCELERATION = 0.01;
    private static final double SWIM_DRAG = 0.9;
    private static final int JUMP_DELAY_TICKS = 10;
    /**
     * On a ladder or up a wall a mob moves at most this fast across and down, and goes up at
     * {@code CLIMB_UP_VELOCITY} before gravity while it pushes against the wall.
     */
    private static final double CLIMB_SPEED = 0.15;
    private static final double CLIMB_UP_VELOCITY = 0.2;
    private static final double MAX_FALL_SPEED = 3.0;
    private static final double MIN_VELOCITY = 0.003;
    /**
     * How far a mob turns towards where it walks in one tick, in degrees.
     */
    private static final float MAX_TURN = 90.0f;

    /**
     * How high a block mobs walk onto without jumping.
     */
    private static final double STEP_HEIGHT = 0.6;

    /**
     * Vanilla counts 60% of the distance walked and plays a step every time it passes a whole number.
     */
    private static final double STEP_INTERVAL = 1.0 / 0.6;

    private static final int TARGET_SCAN_INTERVAL = 10;

    /**
     * How far past its own box a mob reaches with a bare hand.
     */
    private static final double MELEE_REACH = Math.sqrt(2.04) - 0.6;
    private static final double DEFAULT_TARGET_WIDTH = 0.6;
    private static final double DEFAULT_TARGET_HEIGHT = 1.8;
    private static final double SNEAKING_PLAYER_HEIGHT = 1.5;
    /**
     * The entity event that shows the particles of a teleport.
     */
    private static final byte ENTITY_EVENT_TELEPORT = 46;

    protected final Navigation navigation;

    private double stepDistance;
    private double swimSpeed;
    private int jumpDelay;
    /**
     * How fast this mob moves the way it faces this tick, with no path to follow.
     */
    private double forwardSpeed;
    private boolean horizontalCollision;

    protected AbstractPathfinderMob(final int entityId, final UUID uuid, final EntityType type,
                                    final Location location, final float maxHealth) {
        super(entityId, uuid, type, location, maxHealth);
        this.navigation = new Navigation(serverWorld(), this::halfWidth);
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved()) {
            return;
        }
        tickLiving(currentTick);
        if (isRemoved()) {
            return;
        }

        if (isDead()) {
            setMoveSpeed(0.0);
            final Location fallingFrom = location();
            applyPhysics();
            updateChunkMembership(fallingFrom, location());
            syncToClients();
            return;
        }

        final AbstractLivingEntity currentTarget = target();
        if (!picksPlayersByItself()) {
            if (currentTarget != null && (currentTarget.isRemoved() || currentTarget.isDead()
                    || !currentTarget.world().equals(world()))) {
                setTarget(null);
            }
        } else if (currentTick % TARGET_SCAN_INTERVAL == 0) {
            updateTarget();
        } else if (currentTarget != null
                && !(currentTarget instanceof final ServerPlayer player && isValidTarget(player, dropRangeSq()))) {
            setTarget(null);
        }

        tickDamageMemory();
        tickNoActionTime(currentTick);
        tickAmbientSound();
        setMoveSpeed(0.0);
        forwardSpeed = 0.0;
        goals.tick();
        tickBehaviours(currentTick);
        tickMoveControl();

        final Location before = location();
        applyPhysics();
        final Location after = location();

        updateChunkMembership(before, after);

        syncToClients();

        if (after.y() < serverWorld().minY() - 64) {
            server().entities().despawn(this);
        }
    }

    /**
     * {@return {@code true} if this mob goes after the nearest player without any goal asking for
     * it}: within its follow range, and after whoever hits it. Only the mobs plugins define do; the
     * others pick their targets with goals, {@link NearestAttackableTargetGoal} and the like, or
     * have none.
     */
    public boolean picksPlayersByItself() {
        return false;
    }

    /**
     * {@return {@code true} if this mob stands close enough to an entity to hit it}: when its box,
     * widened by {@value #MELEE_REACH} blocks on each side, touches the box of the entity
     */
    public final boolean isWithinMeleeRange(final AbstractLivingEntity entity) {
        final Location self = location();
        final Location other = entity.location();
        final double reach = halfWidth() + MELEE_REACH + halfWidthOf(entity);
        return Math.abs(self.x() - other.x()) < reach
                && Math.abs(self.z() - other.z()) < reach
                && self.y() < other.y() + boxHeightOf(entity)
                && other.y() < self.y() + height();
    }

    /**
     * Hits a living entity with the {@linkplain #attackDamage() attack damage} of this mob.
     *
     * @return {@code true} if the entity was hurt
     */
    public boolean attack(final AbstractLivingEntity target) {
        return server().entities().combat().attack(this, target);
    }

    /**
     * Shows this mob swinging its main hand, with the plain swing of an empty hand.
     */
    public final void swingMainHand() {
        sendToTrackers(new ClientboundSwingAnimationPacket(entityId(), true, SwingAnimation.DEFAULT));
    }

    /**
     * {@return half the width of the box of a living entity}
     */
    public static double halfWidthOf(final AbstractLivingEntity entity) {
        return entity instanceof final AbstractMovingMob mob ? mob.width() / 2.0 : DEFAULT_TARGET_WIDTH / 2.0;
    }

    /**
     * {@return the height of the box of a living entity}
     */
    public static double boxHeightOf(final AbstractLivingEntity entity) {
        return switch (entity) {
            case final AbstractMovingMob mob -> mob.height();
            case final ServerPlayer player -> player.isSneaking() ? SNEAKING_PLAYER_HEIGHT : DEFAULT_TARGET_HEIGHT;
            default -> DEFAULT_TARGET_HEIGHT;
        };
    }

    @Override
    protected void onDeath() {
        navigation.stop();
        goals.stopAll();
        setTarget(null);
        super.onDeath();
    }

    @Override
    protected void onDeathAnimationFinished() {
        server().entities().despawn(this);
    }

    private double dropRangeSq() {
        final double range = followRange() * 1.25;
        return range * range;
    }

    private void updateTarget() {
        final List<ServerPlayer> players = server().players().online();
        if (players.isEmpty()) {
            setTarget(null);
            return;
        }

        final double acquireSq = followRange() * followRange();
        ServerPlayer best = null;
        double bestDistSq = Double.MAX_VALUE;
        if (target() instanceof final ServerPlayer current && isTargetable(current)) {
            final double distSq = distanceSqTo(current);
            if (distSq <= dropRangeSq()) {
                best = current;
                bestDistSq = distSq;
            }
        }
        for (int i = 0, size = players.size(); i < size; i++) {
            final ServerPlayer player = players.get(i);
            if (player == best || !isTargetable(player)) {
                continue;
            }
            final double distSq = distanceSqTo(player);
            if (distSq <= acquireSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                best = player;
            }
        }
        setTarget(best);
    }

    private boolean isTargetable(final ServerPlayer player) {
        if (player.isRemoved() || player.isDead()) {
            return false;
        }
        final GameMode mode = player.gameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
            return false;
        }
        return player.world().equals(world());
    }

    private boolean isValidTarget(final ServerPlayer player, final double maxDistSq) {
        return isTargetable(player) && distanceSqTo(player) <= maxDistSq;
    }

    @Override
    public final Navigation navigation() {
        return navigation;
    }

    /**
     * Moves this mob for one tick as vanilla moves a living entity: it turns towards the next point
     * of its path, accelerates in front of it, moves, then loses speed to friction and gravity.
     *
     * <p>The speed a goal {@linkplain #setMoveSpeed(double) asks for} is vanilla's: the movement
     * speed attribute times the speed modifier of the goal. On the ground it is both how hard the mob
     * pushes and how much of that push it uses, so a cow at {@code 0.2} gains
     * {@code 0.2 * 0.2 * 0.98} blocks per tick each tick and settles at about 1.7 blocks per
     * second.</p>
     */
    @SuppressFBWarnings(
            value = "FE_FLOATING_POINT_EQUALITY",
            justification = "Exact floating-point comparison is intentional."
    )
    private void applyPhysics() {
        driftWithCurrents();
        final Location current = location();
        final double x = current.x();
        final double y = current.y();
        final double z = current.z();

        final double waterDepth = waterHeight();
        final boolean inWater = waterDepth > 0.0;
        // water wins over lava when a mob touches both
        final double lavaDepth = inWater ? 0.0 : lavaHeight();
        final boolean inLava = lavaDepth > 0.0;
        final boolean swimming = inWater && wantsToSwim();
        navigation.tick(x, y, z, swimming);

        double velocityX = velocityX();
        double velocityY = velocityY();
        double velocityZ = velocityZ();
        if (Math.abs(velocityX) < MIN_VELOCITY) velocityX = 0.0;
        if (Math.abs(velocityY) < MIN_VELOCITY) velocityY = 0.0;
        if (Math.abs(velocityZ) < MIN_VELOCITY) velocityZ = 0.0;

        float yaw = yaw();
        boolean onGround = onGround();
        final boolean wasOnGround = onGround;
        final double fluidJumpThreshold = onGround ? swimThreshold() : 0.0;
        final boolean canSwimUp = (inWater && waterDepth > fluidJumpThreshold) || (inLava && lavaDepth > fluidJumpThreshold);
        final boolean wasFalling = velocityY <= 0.0;

        if (jumpDelay > 0) {
            jumpDelay--;
        }

        // a goal that sets the speed every tick wins over the speed asked along the path
        final double speed = moveSpeed() > 0.0 ? moveSpeed() : navigation.headingSpeed();
        final var waypoint = navigation.heading();
        // a mob that hops around moves the way it faces, with no path
        final boolean forward = waypoint == null && forwardSpeed > 0.0;
        boolean walking = false;
        if (swimming) {
            if (risesWhileSwimming()) {
                velocityY += SWIM_LIFT;
            }
            if (waypoint != null && speed > 0.0) {
                // swims to the waypoint itself, up or down as well: the further it is, the harder
                final double dx = waypoint.x() + 0.5 - x;
                final double dy = waypoint.y() - y;
                final double dz = waypoint.z() + 0.5 - z;
                final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 1.0E-4) {
                    yaw = rotateTowards(yaw, (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f, MAX_TURN);
                    swimSpeed += (speed - swimSpeed) * SWIM_SPEED_GAIN;
                    velocityX += swimSpeed * dx * SWIM_HORIZONTAL_PULL;
                    velocityY += swimSpeed * dy / distance * SWIM_VERTICAL_PULL;
                    velocityZ += swimSpeed * dz * SWIM_HORIZONTAL_PULL;
                    if (waterDepth > eyeHeight()) {
                        final double radians = Math.toRadians(yaw);
                        velocityX -= Math.sin(radians) * swimSpeed * SWIM_ACCELERATION;
                        velocityZ += Math.cos(radians) * swimSpeed * SWIM_ACCELERATION;
                    }
                }
            } else {
                swimSpeed = 0.0;
            }
        } else {
            swimSpeed = 0.0;
            if (!onGround) {
                velocityY -= sinkingPull();
            }
            if (waypoint != null && speed > 0.0) {
                final double dx = waypoint.x() + 0.5 - x;
                final double dz = waypoint.z() + 0.5 - z;
                if (dx * dx + dz * dz > 2.5E-7) {
                    yaw = rotateTowards(yaw, (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f, MAX_TURN);
                    walking = true;
                }
            } else if (forward) {
                walking = true;
            }
        }

        if (consumeJumpRequest()) {
            if (canSwimUp) {
                velocityY = swimUpVelocity(velocityY, inLava);
            } else if (onGround && jumpDelay == 0) {
                velocityY = Math.max(jumpVelocity(), velocityY);
                jumpDelay = JUMP_DELAY_TICKS;
            }
        }

        if (walking) {
            final double walkSpeed = forward ? forwardSpeed : speed;
            final double push = inWater || inLava ? FLUID_ACCELERATION : onGround ? walkSpeed : AIR_ACCELERATION;
            final double acceleration = push * Math.min(1.0, walkSpeed * INPUT_SCALE);
            final double radians = Math.toRadians(yaw);
            velocityX -= Math.sin(radians) * acceleration;
            velocityZ += Math.cos(radians) * acceleration;
        }

        if (!inWater && !inLava && onClimbable(x, y, z)) {
            // on a ladder or up a wall a mob moves slowly across and slides down slowly
            velocityX = Math.clamp(velocityX, -CLIMB_SPEED, CLIMB_SPEED);
            velocityZ = Math.clamp(velocityZ, -CLIMB_SPEED, CLIMB_SPEED);
            velocityY = Math.max(velocityY, -CLIMB_SPEED);
        }

        if (velocityY < 0.0 && !onGround) {
            velocityY *= fallDrag();
        }
        velocityY = Math.max(velocityY, -MAX_FALL_SPEED);

        BlockCollider.Movement moved = collide(x, y, z, velocityX, velocityY, velocityZ, stepHeight(), onGround);
        boolean blockedX = isBlocked(moved.x(), velocityX);
        boolean blockedZ = isBlocked(moved.z(), velocityZ);
        if ((blockedX || blockedZ) && walking && velocityY <= 0.0) {
            if (canSwimUp) {
                // too high a bank to step onto: swim up along it
                velocityY = swimUpVelocity(velocityY, inLava);
            } else if (onGround && jumpDelay == 0 && waypoint != null && waypoint.y() > y
                    && !isBoxBlocked(x, y + 1.0, z)) {
                // too high a block to step onto: jump onto it, if there is room above. Only on the
                // way up: against the corner of a wall at its own height, a mob slides along it
                // rather than hopping on the spot
                velocityY = jumpVelocity();
                jumpDelay = JUMP_DELAY_TICKS;
                moved = collide(x, y, z, velocityX, velocityY, velocityZ, stepHeight(), onGround);
                blockedX = isBlocked(moved.x(), velocityX);
                blockedZ = isBlocked(moved.z(), velocityZ);
            }
        }
        final boolean blockedHorizontally = blockedX || blockedZ;
        final double restitution = bounciness();
        if (blockedX) velocityX = restitution > 0.0 ? -velocityX * restitution : 0.0;
        if (blockedZ) velocityZ = restitution > 0.0 ? -velocityZ * restitution : 0.0;

        if (moved.y() != velocityY) {
            onGround = velocityY < 0.0;
            velocityY = bounceBack(velocityY, moved.y(), onGround, restitution);
        } else {
            onGround = false;
        }

        final double newX = x + moved.x();
        final double newY = y + moved.y();
        final double newZ = z + moved.z();

        horizontalCollision = blockedHorizontally;
        if (blockedHorizontally && !inLava && onClimbable(newX, newY, newZ)) {
            // against a wall it may climb, a mob goes up
            velocityY = CLIMB_UP_VELOCITY;
        }

        if (swimming && waterDepth > eyeHeight()) {
            // under water, a mob that swims neither sinks nor floats: it only slows down
            velocityX *= SWIM_DRAG;
            velocityY *= SWIM_DRAG;
            velocityZ *= SWIM_DRAG;
        } else if (inWater) {
            velocityX *= WATER_DRAG;
            velocityY *= WATER_DRAG;
            velocityZ *= WATER_DRAG;
            velocityY = sinkInFluid(velocityY, wasFalling) + liftInFluid(waterDepth);
            if (blockedHorizontally && !isBoxBlocked(newX + velocityX, y + 0.6, newZ + velocityZ)) {
                // against a bank it can climb: hop out of the water
                velocityY = LEAVE_FLUID_VELOCITY;
            }
        } else if (inLava) {
            velocityX *= LAVA_DRAG;
            velocityZ *= LAVA_DRAG;
            if (lavaDepth > swimThreshold()) {
                velocityY *= LAVA_DRAG;
            } else {
                velocityY = sinkInFluid(velocityY * SHALLOW_LAVA_VERTICAL_DRAG, wasFalling);
            }
            velocityY -= GRAVITY / 4.0;
            velocityY += liftInFluid(lavaDepth);
            if (blockedHorizontally && !isBoxBlocked(newX + velocityX, y + 0.6, newZ + velocityZ)) {
                velocityY = LEAVE_FLUID_VELOCITY;
            }
        } else {
            final double airDrag = modifiedFriction(AIR_FRICTION, airDragModifier());
            velocityY = (velocityY - GRAVITY) * verticalDrag(airDrag);
            final double friction = wasOnGround ? modifiedFriction(GROUND_SLIPPERINESS, frictionModifier()) * airDrag : airDrag;
            velocityX *= friction;
            velocityZ *= friction;
        }

        final double stepDx = newX - x;
        final double stepDz = newZ - z;
        final double walked = Math.sqrt(stepDx * stepDx + stepDz * stepDz);
        if (onGround && !inWater) {
            stepDistance += walked;
            if (stepDistance >= STEP_INTERVAL) {
                stepDistance = 0.0;
                server().debug().gameEvents().emit(world(), GameEventKeys.STEP, new PositionData.Vec3D(newX, newY, newZ));
                onStep();
            }

        }

        setVelocity(velocityX, velocityY, velocityZ);
        setOnGround(onGround);
        setYaw(yaw);
        if (onGround && !isDead()) {
            BlockStepping.walkedOn(serverWorld(), this, collisionShapes(), newX, newY, newZ, halfWidth(), height());
        }
        if (!isDead()) {
            tickLookControl(waypoint != null, walked * walked > 2.5E-7);
        }

        if (newX != x || newY != y || newZ != z
                || yaw() != current.yaw() || pitch() != current.pitch()) {
            setLocation(current.with(newX, newY, newZ, yaw(), pitch()));
        }
        riseWithBubbles();
    }

    /**
     * {@return the vertical speed of a mob once it hit the ground or a ceiling}: none, unless it
     * bounces; then it goes back the other way, minus what it lost to gravity and drag over the part
     * of the tick it moved
     *
     * @param velocityY   how fast it went up or down
     * @param movedY      how far it went before it hit
     * @param landed      {@code true} if it hit the ground
     * @param restitution how much of its speed it keeps when it bounces
     */
    private double bounceBack(final double velocityY, final double movedY, final boolean landed, final double restitution) {
        // a mob that falls no faster than the gravity pulls it settles on the ground
        if (restitution <= 0.0 || (landed && -velocityY <= GRAVITY)) {
            return 0.0;
        }
        final double portion = movedY / velocityY;
        final double gravityCompensation = portion * GRAVITY;
        final double drag = 1.0 + portion * (verticalDrag(modifiedFriction(AIR_FRICTION, airDragModifier())) - 1.0);
        return (gravityCompensation - velocityY) * drag * restitution;
    }

    /**
     * {@return what is left of the vertical speed of this mob after a tick in the air}
     *
     * @param airDrag what is left of its horizontal speed
     */
    private double verticalDrag(final double airDrag) {
        return omnidirectionalAirMover() ? airDrag : modifiedFriction(VERTICAL_DRAG, airDragModifier());
    }

    /**
     * {@return a friction once a modifier changed how much speed it takes}: {@code 1} keeps it,
     * {@code 0} takes none, {@code 2} twice as much
     */
    private static double modifiedFriction(final double friction, final double modifier) {
        return modifier == 1.0 ? friction : Math.clamp(1.0 - (1.0 - friction) * modifier, 0.0, 1.0);
    }

    /**
     * {@return how much of the speed the slipperiness of the ground takes from this mob}: {@code 1}
     * for all of it
     */
    protected double frictionModifier() {
        return 1.0;
    }

    /**
     * {@return how much of the speed the air takes from this mob}: {@code 1} for all of it
     */
    protected double airDragModifier() {
        return 1.0;
    }

    /**
     * {@return {@code true} if the air slows this mob down as much up and down as across}, as it
     * slows a ball
     */
    protected boolean omnidirectionalAirMover() {
        return false;
    }

    /**
     * {@return how much of its speed this mob keeps when it bounces off a block}: none for most
     */
    protected double bounciness() {
        return 0.0;
    }

    /**
     * {@return how much faster this mob goes up each tick in a fluid that it does not swim in}:
     * nothing for most, a little for one that floats
     *
     * @param depth how deep in the fluid this mob is
     */
    protected double liftInFluid(final double depth) {
        return 0.0;
    }

    /**
     * {@return the vertical speed of a mob in a fluid once the fluid pulled it down with a sixteenth
     * of the gravity}; a mob that was already sinking that slowly settles at {@code -0.003}
     */
    private static double sinkInFluid(final double velocityY, final boolean wasFalling) {
        if (wasFalling && Math.abs(velocityY - 0.005) >= 0.003 && Math.abs(velocityY - GRAVITY / 16.0) < 0.003) {
            return -0.003;
        }
        return velocityY - GRAVITY / 16.0;
    }

    /**
     * {@return {@code true} if this mob climbs where it stands}: as any living entity, in a block of
     * the {@code climbable} tag such as a ladder or vines
     *
     * @param x where the mob stands
     * @param y where the mob stands
     * @param z where the mob stands
     */
    protected boolean onClimbable(final double x, final double y, final double z) {
        final BlockState state = BlockView.blockAt(serverWorld(), (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
        return state != null && state.isTagged(BlockTypeTagKeys.CLIMBABLE);
    }

    /**
     * {@return {@code true} if a block stopped this mob across during its last move}
     */
    protected final boolean horizontalCollision() {
        return horizontalCollision;
    }

    /**
     * {@return {@code true} if, in water, this mob swims to the waypoints of its path, up and down
     * as well, rather than walking on the bottom}; such a mob needs a navigation that
     * {@linkplain Navigation#setSwims(boolean) goes through the water}
     */
    protected boolean wantsToSwim() {
        return false;
    }

    /**
     * {@return {@code true} if this mob drifts up while it {@linkplain #wantsToSwim() swims}}
     */
    protected boolean risesWhileSwimming() {
        return false;
    }

    /**
     * {@return how much faster than the others this mob goes down each tick it is off the ground and
     * does not swim}; a mob made to walk on the bottom of the water sinks to it
     */
    protected double sinkingPull() {
        return 0.0;
    }

    /**
     * {@return {@code true} if a block stopped a horizontal move}; as in Vanilla, a difference under
     * {@code 0.00001} is rounding, not a wall
     */
    private static boolean isBlocked(final double moved, final double wanted) {
        return Math.abs(moved - wanted) > 1.0E-5;
    }

    /**
     * Turns this mob into another, once the plugins agreed through a {@link MobConvertEvent}: the
     * new one takes its place with its speed, its fire, its effects, its age if both grow up, and
     * whether a player hit it; it starts at full health. This mob is gone afterwards.
     *
     * @param replacement the new mob, created where this one stands and not yet in the world
     * @return {@code false} if a plugin kept this mob as it is
     */
    protected final boolean convertTo(final AbstractPathfinderMob replacement) {
        replacement.setVelocity(velocityX(), velocityY(), velocityZ());
        replacement.setOnGround(onGround());
        replacement.setFireTicks(fireTicks());
        replacement.copyHurtByPlayerFrom(this);
        replacement.setAbsorptionAmount(absorptionAmount());
        replacement.setInvulnerableTicks(invulnerableTicks());
        if (isPersistenceRequired()) {
            replacement.setPersistenceRequired(true);
        }
        if (this instanceof final AbstractAgeableMob from && replacement instanceof final AbstractAgeableMob to) {
            to.copyAgeFrom(from);
        }
        if (server().events().post(new MobConvertEventImpl(this, replacement)).isCancelled()) {
            return false;
        }
        world().scheduler().execute(world().key(), location().chunk(), () -> {
            server().entities().spawn(replacement);
            for (final MobEffectInstance effect : activeEffects()) {
                replacement.addEffect(effect);
            }
            server().entities().despawn(this);
        });
        return true;
    }

    /**
     * Called when this mob ate the grass it grazed.
     */
    public void ate() {
        server().debug().gameEvents().emit(world(), GameEventKeys.EAT, location());
    }

    /**
     * {@return how much this mob likes to walk to a block; a random stroll goes to the best of the
     * spots it considers}
     */
    public float walkTargetValue(final int x, final int y, final int z) {
        return 0.0f;
    }

    protected double fallDrag() {
        return 1.0;
    }

    /**
     * {@return how high a block this mob walks onto without jumping, such as a slab or a path}
     */
    protected double stepHeight() {
        return STEP_HEIGHT;
    }

    protected void onStep() {
    }

    /**
     * {@return how fast this mob goes up when it jumps off the ground}
     */
    protected double jumpVelocity() {
        return JUMP_VELOCITY;
    }

    /**
     * {@return how fast this mob goes up after a jump in a fluid deep enough to swim in}: a little
     * faster each tick it keeps jumping
     *
     * @param velocityY how fast it went up before
     * @param inLava    {@code true} in lava, {@code false} in water
     */
    protected double swimUpVelocity(final double velocityY, final boolean inLava) {
        return velocityY + SWIM_UP_VELOCITY;
    }

    /**
     * Moves this mob the way it faces this tick, as fast as given, when it follows no path; set
     * again every tick, from {@link #tickMoveControl()}, by the mobs that do not walk along paths.
     *
     * @param speed how fast, {@code 0} to stand still
     */
    protected final void setForwardSpeed(final double speed) {
        this.forwardSpeed = speed;
    }

    /**
     * Called every tick once the goals had their say and before this mob moves: a mob that moves on
     * its own, rather than along paths, turns and sets its {@linkplain #setForwardSpeed forward speed}
     * here.
     */
    protected void tickMoveControl() {
    }

    /**
     * Tries to teleport this mob to a point: it lands on the first block below the point an entity
     * may teleport onto, in the column of the point, as long as that block is not one it avoids,
     * and the room it would take up there holds no block it avoids, no fluid, and nothing it would
     * bump into. The point has to be in a chunk the current thread owns.
     *
     * @param showParticles {@code true} to show the particles of a teleport where it lands
     * @param avoids        the blocks this mob does not teleport onto, nor into
     * @return {@code true} if this mob teleported
     */
    public final boolean randomTeleport(final double x, final double y, final double z, final boolean showParticles,
                                        final Predicate<BlockState> avoids) {
        final ServerWorld world = serverWorld();
        final int blockX = (int) Math.floor(x);
        final int blockZ = (int) Math.floor(z);
        if (world.loadedColumn(blockX >> 4, blockZ >> 4) == null
                || !server().workers().regionizer().isOwnedByCurrentThread(world.key(), new ChunkPos(blockX >> 4, blockZ >> 4))) {
            return false;
        }
        double landingY = y;
        for (int blockY = (int) Math.floor(y) - 1; blockY >= world.minY(); blockY--) {
            final BlockState ground = BlockView.blockAt(world, blockX, blockY, blockZ);
            if (ground != null && BlockView.isTagged(BlockTypeTagKeys.ENTITIES_CAN_TELEPORT_TO, ground)) {
                return teleportOnto(ground, x, landingY, z, showParticles, avoids);
            }
            landingY--;
        }
        return false;
    }

    /**
     * {@return {@code true} if this mob may teleport to a point}, once the room there is clear; any
     * point is fine but for the mobs that avoid some
     */
    protected boolean canRandomlyTeleportTo(final double x, final double y, final double z) {
        return true;
    }

    private boolean teleportOnto(final BlockState ground, final double x, final double y, final double z,
                                 final boolean showParticles, final Predicate<BlockState> avoids) {
        if (avoids.test(ground) || isBoxBlocked(x, y, z) || boxHolds(x, y, z, avoids)
                || !canRandomlyTeleportTo(x, y, z)) {
            return false;
        }
        final Location from = location();
        if (server().events().post(new MobTeleportEventImpl(this, from, from.with(x, y, z))).isCancelled()) {
            return false;
        }
        teleportTo(x, y, z);
        if (showParticles) {
            sendToTrackers(new ClientboundEntityEventPacket(entityId(), ENTITY_EVENT_TELEPORT));
        }
        navigation.stop();
        return true;
    }

    /**
     * {@return {@code true} if the room this mob would take up at a point holds a fluid or a block it
     * avoids}
     */
    private boolean boxHolds(final double x, final double y, final double z, final Predicate<BlockState> avoids) {
        final ServerWorld world = serverWorld();
        final double half = halfWidth();
        for (int bx = (int) Math.floor(x - half); bx < (int) Math.ceil(x + half); bx++) {
            for (int by = (int) Math.floor(y); by < (int) Math.ceil(y + height()); by++) {
                for (int bz = (int) Math.floor(z - half); bz < (int) Math.ceil(z + half); bz++) {
                    final BlockState state = BlockView.blockAt(world, bx, by, bz);
                    if (state == null || state.isFluid() || avoids.test(state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
