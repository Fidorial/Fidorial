package fr.euphyllia.fidorial.server.entity.mob;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.ai.Navigation;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAnimatePacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.GameEventKeys;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractPathfinderMob extends AbstractMovingMob {

    private static final double GRAVITY = 0.08;
    private static final double VERTICAL_DRAG = 0.98;
    /**
     * What is left of the horizontal speed of a mob after a tick on the ground: the
     * slipperiness of an ordinary block, {@code 0.6}, times {@code 0.91}.
     */
    private static final double GROUND_FRICTION = 0.6 * 0.91;
    private static final double AIR_FRICTION = 0.91;
    /**
     * How much a mob accelerates in the air and in water for a full input.
     */
    private static final double AIR_ACCELERATION = 0.02;
    private static final double WATER_ACCELERATION = 0.02;
    private static final double WATER_DRAG = 0.8;
    /**
     * Vanilla scales the input of a living entity by this factor every tick.
     */
    private static final double INPUT_SCALE = 0.98;
    private static final double JUMP_VELOCITY = 0.42;
    private static final double SWIM_UP_VELOCITY = 0.04;
    private static final double LEAVE_WATER_VELOCITY = 0.3;
    private static final int JUMP_DELAY_TICKS = 10;
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

    private static final int AMBIENT_SOUND_RANGE = 1000;
    private static final int DEFAULT_AMBIENT_SOUND_INTERVAL = 80;

    /**
     * How far past its own box a mob reaches with a bare hand.
     */
    private static final double MELEE_REACH = Math.sqrt(2.04) - 0.6;
    private static final double DEFAULT_TARGET_WIDTH = 0.6;
    private static final double DEFAULT_TARGET_HEIGHT = 1.8;
    private static final double SNEAKING_PLAYER_HEIGHT = 1.5;

    protected final Navigation navigation;

    private double stepDistance;
    private int ambientSoundTime;
    private int jumpDelay;

    protected AbstractPathfinderMob(final int entityId, final UUID uuid, final EntityType type,
                                    final Location location, final float maxHealth) {
        super(entityId, uuid, type, location, maxHealth);
        this.navigation = new Navigation(serverWorld());
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
        goals.tick();
        tickBehaviours(currentTick);

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
     * it}, as mobs did before they had goals to pick their target. A mob that picks its targets as
     * Vanilla does, with {@link NearestAttackableTargetGoal}
     * and the like, answers {@code false}.
     */
    public boolean picksPlayersByItself() {
        return true;
    }

    private void tickAmbientSound() {
        if (ThreadLocalRandom.current().nextInt(AMBIENT_SOUND_RANGE) >= ambientSoundTime++) {
            return;
        }
        ambientSoundTime = -ambientSoundInterval();
        final Sound.Type sound = ambientSound();
        if (sound != null) {
            playSound(sound, soundVolume(), voicePitch());
        }
    }

    /**
     * {@return the sound this mob makes now and then, {@code null} for none}
     */
    protected Sound.@Nullable Type ambientSound() {
        return null;
    }

    /**
     * {@return how many ticks this mob stays silent after a sound}; the chance of the
     * next one then grows by one in a thousand every tick
     */
    protected int ambientSoundInterval() {
        return DEFAULT_AMBIENT_SOUND_INTERVAL;
    }

    @Override
    public void playHurtSound() {
        ambientSoundTime = -ambientSoundInterval();
        super.playHurtSound();
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
                && self.y() < other.y() + heightOf(entity)
                && other.y() < self.y() + height();
    }

    /**
     * Hits a living entity with the {@linkplain #attackDamage() attack damage} of this mob.
     *
     * @return {@code true} if the entity was hurt
     */
    public boolean attack(final AbstractLivingEntity target) {
        final float damage = attackDamage();
        return damage > 0f && server().entities().combat().damage(target, DamageSource.mobAttack(this), damage);
    }

    /**
     * Shows this mob swinging its main hand.
     */
    public final void swingMainHand() {
        sendToTrackers(new ClientboundAnimatePacket(entityId(), ClientboundAnimatePacket.SWING_MAIN_HAND));
    }

    /**
     * {@return the head a player wears to be noticed by this mob from half as far only, {@code null}
     * if no head fools it}
     */
    public @Nullable TypedKey<Item> disguiseHead() {
        return null;
    }

    /**
     * {@return half the width of the box of a living entity}
     */
    public static double halfWidthOf(final AbstractLivingEntity entity) {
        return entity instanceof final AbstractMovingMob mob ? mob.width() / 2.0 : DEFAULT_TARGET_WIDTH / 2.0;
    }

    private static double heightOf(final AbstractLivingEntity entity) {
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
        final Location current = location();
        final double x = current.x();
        final double y = current.y();
        final double z = current.z();

        navigation.tick(x, z);

        double velocityX = velocityX();
        double velocityY = velocityY();
        double velocityZ = velocityZ();
        if (Math.abs(velocityX) < MIN_VELOCITY) velocityX = 0.0;
        if (Math.abs(velocityY) < MIN_VELOCITY) velocityY = 0.0;
        if (Math.abs(velocityZ) < MIN_VELOCITY) velocityZ = 0.0;

        float yaw = yaw();
        boolean onGround = onGround();
        final boolean wasOnGround = onGround;
        final double waterDepth = waterHeight();
        final boolean inWater = waterDepth > 0.0;
        final boolean canSwimUp = inWater && waterDepth > (onGround ? swimThreshold() : 0.0);
        final boolean wasFalling = velocityY <= 0.0;

        if (jumpDelay > 0) {
            jumpDelay--;
        }

        // a goal that sets the speed every tick wins over the speed asked along the path
        final double speed = moveSpeed() > 0.0 ? moveSpeed() : navigation.speed();
        final var waypoint = navigation.currentWaypoint();
        boolean walking = false;
        if (waypoint != null && speed > 0.0) {
            final double dx = waypoint.x() + 0.5 - x;
            final double dz = waypoint.z() + 0.5 - z;
            if (dx * dx + dz * dz > 2.5E-7) {
                yaw = rotateTowards(yaw, (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f, MAX_TURN);
                walking = true;
            }
        }

        if (consumeJumpRequest()) {
            if (canSwimUp) {
                velocityY += SWIM_UP_VELOCITY;
            } else if (onGround && jumpDelay == 0) {
                velocityY = Math.max(JUMP_VELOCITY, velocityY);
                jumpDelay = JUMP_DELAY_TICKS;
            }
        }

        if (walking) {
            final double push = inWater ? WATER_ACCELERATION : onGround ? speed : AIR_ACCELERATION;
            final double acceleration = push * Math.min(1.0, speed * INPUT_SCALE);
            final double radians = Math.toRadians(yaw);
            velocityX -= Math.sin(radians) * acceleration;
            velocityZ += Math.cos(radians) * acceleration;
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
                velocityY += SWIM_UP_VELOCITY;
            } else if (onGround && jumpDelay == 0 && !isBoxBlocked(x, y + 1.0, z)) {
                // too high a block to step onto: jump onto it, if there is room above
                velocityY = JUMP_VELOCITY;
                jumpDelay = JUMP_DELAY_TICKS;
                moved = collide(x, y, z, velocityX, velocityY, velocityZ, stepHeight(), onGround);
                blockedX = isBlocked(moved.x(), velocityX);
                blockedZ = isBlocked(moved.z(), velocityZ);
            }
        }
        final boolean blockedHorizontally = blockedX || blockedZ;
        if (blockedX) velocityX = 0.0;
        if (blockedZ) velocityZ = 0.0;

        if (moved.y() != velocityY) {
            onGround = velocityY < 0.0;
            velocityY = 0.0;
        } else {
            onGround = false;
        }

        final double newX = x + moved.x();
        final double newY = y + moved.y();
        final double newZ = z + moved.z();

        if (inWater) {
            velocityX *= WATER_DRAG;
            velocityY *= WATER_DRAG;
            velocityZ *= WATER_DRAG;
            // as in Vanilla, water pulls down with a sixteenth of the gravity, and sinking settles
            if (wasFalling && Math.abs(velocityY - 0.005) >= 0.003 && Math.abs(velocityY - GRAVITY / 16.0) < 0.003) {
                velocityY = -0.003;
            } else {
                velocityY -= GRAVITY / 16.0;
            }
            if (blockedHorizontally && !isBoxBlocked(newX + velocityX, y + 0.6, newZ + velocityZ)) {
                // against a bank it can climb: hop out of the water
                velocityY = LEAVE_WATER_VELOCITY;
            }
        } else {
            velocityY = (velocityY - GRAVITY) * VERTICAL_DRAG;
            final double friction = wasOnGround ? GROUND_FRICTION : AIR_FRICTION;
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
        if (!isDead()) {
            tickLookControl(waypoint != null, walked * walked > 2.5E-7);
        }

        if (newX != x || newY != y || newZ != z
                || yaw() != current.yaw() || pitch() != current.pitch()) {
            setLocation(current.with(newX, newY, newZ, yaw(), pitch()));
        }
    }

    /**
     * {@return {@code true} if a block stopped a horizontal move}; as in Vanilla, a difference under
     * {@code 0.00001} is rounding, not a wall
     */
    private static boolean isBlocked(final double moved, final double wanted) {
        return Math.abs(moved - wanted) > 1.0E-5;
    }

    /**
     * Turns this mob into another, as vanilla converts a mob: the new one takes its place with its
     * speed, its fire, its effects, its age if both grow up, and whether a player hit it; it starts
     * at full health. This mob is gone afterwards.
     *
     * @param replacement the new mob, created where this one stands and not yet in the world
     */
    protected final void convertTo(final AbstractPathfinderMob replacement) {
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
        world().scheduler().execute(world().key(), location().chunk(), () -> {
            server().entities().spawn(replacement);
            for (final MobEffectInstance effect : activeEffects()) {
                replacement.addEffect(effect);
            }
            server().entities().despawn(this);
        });
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
}
