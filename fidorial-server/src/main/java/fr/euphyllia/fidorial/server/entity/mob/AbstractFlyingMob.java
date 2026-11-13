package fr.euphyllia.fidorial.server.entity.mob;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.fidorial.entity.EntityType;
import fr.fidorial.math.Location;

import java.util.UUID;


public abstract class AbstractFlyingMob extends AbstractMovingMob {

    private static final double HORIZONTAL_DRAG = 0.91;
    private static final double VERTICAL_DRAG = 0.91;
    private static final double MIN_VELOCITY = 1.0E-3;
    private static final double VOID_MARGIN = 64.0;

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
            final Location falling = location();
            applyFlightPhysics();
            updateChunkMembership(falling, location());
            syncToClients();
            return;
        }

        tickDamageMemory();

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

    protected void tickFlight(final long currentTick) {
    }

    protected boolean isFlightEnabled() {
        return true;
    }

    protected double horizontalDrag() {
        return HORIZONTAL_DRAG;
    }

    protected double verticalDrag() {
        return VERTICAL_DRAG;
    }

    protected double gravity() {
        return 0.0;
    }

    @SuppressFBWarnings(
            value = "FE_FLOATING_POINT_EQUALITY",
            justification = "Exact floating-point comparison is intentional."
    )
    private void applyFlightPhysics() {
        if (!isFlightEnabled()) {
            setVelocity(0.0, 0.0, 0.0);
            return;
        }

        final Location current = location();
        final double x = current.x();
        final double y = current.y();
        final double z = current.z();

        double velocityX = velocityX();
        double velocityY = velocityY() - gravity();
        double velocityZ = velocityZ();

        final BlockCollider.Movement moved = collide(x, y, z, velocityX, velocityY, velocityZ, 0.0, false);
        if (moved.x() != velocityX) velocityX = 0.0;
        if (moved.z() != velocityZ) velocityZ = 0.0;
        if (moved.y() != velocityY) velocityY = 0.0;
        final double newX = x + moved.x();
        final double newY = y + moved.y();
        final double newZ = z + moved.z();

        setOnGround(isBoxBlocked(newX, newY - 0.001, newZ));

        velocityX *= horizontalDrag();
        velocityZ *= horizontalDrag();
        velocityY *= verticalDrag();
        if (Math.abs(velocityX) < MIN_VELOCITY) velocityX = 0.0;
        if (Math.abs(velocityY) < MIN_VELOCITY) velocityY = 0.0;
        if (Math.abs(velocityZ) < MIN_VELOCITY) velocityZ = 0.0;

        setVelocity(velocityX, velocityY, velocityZ);

        if (newX != x || newY != y || newZ != z
                || yaw() != current.yaw() || pitch() != current.pitch()) {
            setLocation(current.with(newX, newY, newZ, yaw(), pitch()));
        }
    }
}
