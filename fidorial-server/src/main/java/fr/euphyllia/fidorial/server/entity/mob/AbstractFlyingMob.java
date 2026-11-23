package fr.euphyllia.fidorial.server.entity.mob;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.world.block.BlockStepping;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;

import java.util.UUID;

/**
 * A mob that flies by itself rather than walking: each tick its {@linkplain #tickFlight flight}
 * sets its speed, then it moves, then gravity and the air act on that speed.
 *
 * <p>By default it flies as vanilla's flying mobs, the ghast and the phantom: no gravity, and nine
 * tenths of its speed kept every tick. A mob that falls while it flies, as the bat, gives itself a
 * {@linkplain #gravity() gravity} and its own drags.</p>
 */
public abstract class AbstractFlyingMob extends AbstractMovingMob {

    private static final double DRAG = 0.91;
    /**
     * Before a mob thinks, vanilla stops any speed smaller than this.
     */
    private static final double MIN_VELOCITY = 0.003;
    private static final double VOID_MARGIN = 64.0;
    /**
     * What a mob in a cobweb really moves of what it would: a quarter across, a twentieth up or down.
     */
    private static final double COBWEB_HORIZONTAL = 0.25;
    private static final double COBWEB_VERTICAL = 0.05;

    protected AbstractFlyingMob(final int entityId, final UUID uuid, final EntityType type,
                                final Location location, final float maxHealth) {
        super(entityId, uuid, type, location, maxHealth);
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
            driftWithCurrents();
            stopTinyVelocities();
            final Location falling = location();
            applyFlightPhysics();
            updateChunkMembership(falling, location());
            syncToClients();
            return;
        }

        tickDamageMemory();
        tickNoActionTime(currentTick);
        tickAmbientSound();
        driftWithCurrents();
        stopTinyVelocities();

        goals.tick();
        tickBehaviours(currentTick);
        tickFlight(currentTick);

        final Location before = location();
        applyFlightPhysics();
        final Location after = location();

        updateChunkMembership(before, after);
        syncToClients();

        if (after.y() < serverWorld().minY() - VOID_MARGIN) {
            server().entities().despawn(this);
        }
    }

    @Override
    protected void onDeath() {
        goals.stopAll();
        super.onDeath();
    }

    @Override
    protected void onDeathAnimationFinished() {
        server().entities().despawn(this);
    }

    /**
     * Thinks where to fly this tick: sets the speed this mob moves with next.
     *
     * @param currentTick the tick of the server
     */
    protected void tickFlight(final long currentTick) {
    }

    /**
     * {@return {@code false} while this mob stays where it is}, such as a bat hanging from a ceiling
     */
    protected boolean isFlightEnabled() {
        return true;
    }

    /**
     * {@return how much of its horizontal speed this mob keeps after a tick}
     *
     * @param onGround whether the mob stood on a block before it moved
     */
    protected double horizontalDrag(final boolean onGround) {
        return DRAG;
    }

    /**
     * {@return how much of its vertical speed this mob keeps after a tick, once gravity pulled it}
     */
    protected double verticalDrag() {
        return DRAG;
    }

    /**
     * {@return how much faster this mob falls each tick}
     */
    protected double gravity() {
        return 0.0;
    }

    private void stopTinyVelocities() {
        final double velocityX = Math.abs(velocityX()) < MIN_VELOCITY ? 0.0 : velocityX();
        final double velocityY = Math.abs(velocityY()) < MIN_VELOCITY ? 0.0 : velocityY();
        final double velocityZ = Math.abs(velocityZ()) < MIN_VELOCITY ? 0.0 : velocityZ();
        setVelocity(velocityX, velocityY, velocityZ);
    }

    @SuppressFBWarnings(
            value = "FE_FLOATING_POINT_EQUALITY",
            justification = "Exact floating-point comparison is intentional."
    )
    private void applyFlightPhysics() {
        final Location current = location();
        final double x = current.x();
        final double y = current.y();
        final double z = current.z();

        double velocityX = 0.0;
        double velocityY = 0.0;
        double velocityZ = 0.0;
        double newX = x;
        double newY = y;
        double newZ = z;
        if (isFlightEnabled()) {
            final boolean wasOnGround = onGround();
            final boolean stuck = isInCobweb();
            final double moveX = stuck ? velocityX() * COBWEB_HORIZONTAL : velocityX();
            final double moveY = stuck ? velocityY() * COBWEB_VERTICAL : velocityY();
            final double moveZ = stuck ? velocityZ() * COBWEB_HORIZONTAL : velocityZ();
            final BlockCollider.Movement moved = collide(x, y, z, moveX, moveY, moveZ, 0.0, false);
            if (!stuck) {
                // a cobweb takes all the speed away; otherwise a block only stops the way it blocks
                velocityX = moved.x() != moveX ? 0.0 : moveX;
                velocityY = moved.y() != moveY ? 0.0 : moveY;
                velocityZ = moved.z() != moveZ ? 0.0 : moveZ;
            }
            newX = x + moved.x();
            newY = y + moved.y();
            newZ = z + moved.z();
            setOnGround(isBoxBlocked(newX, newY - 0.001, newZ));
            if (onGround() && !isDead()) {
                BlockStepping.walkedOn(serverWorld(), this, collisionShapes(), newX, newY, newZ, halfWidth(), height());
            }

            velocityY = (velocityY - gravity()) * verticalDrag();
            final double horizontalDrag = horizontalDrag(wasOnGround);
            velocityX *= horizontalDrag;
            velocityZ *= horizontalDrag;
        }

        setVelocity(velocityX, velocityY, velocityZ);

        if (!isDead()) {
            final double stepX = newX - x;
            final double stepZ = newZ - z;
            tickLookControl(false, stepX * stepX + stepZ * stepZ > 2.5E-7);
        }

        if (newX != x || newY != y || newZ != z
                || yaw() != current.yaw() || pitch() != current.pitch()) {
            setLocation(current.with(newX, newY, newZ, yaw(), pitch()));
        }
        if (isFlightEnabled()) {
            riseWithBubbles();
        }
    }
}
