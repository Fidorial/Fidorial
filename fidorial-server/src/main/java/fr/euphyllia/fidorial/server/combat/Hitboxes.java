package fr.euphyllia.fidorial.server.combat;

import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.fidorial.math.Location;

/**
 * The boxes entities take up, as hits and projectiles see them.
 */
public final class Hitboxes {

    private Hitboxes() {
        throw new UnsupportedOperationException("Hitboxes cannot be instantiated.");
    }

    /**
     * {@return half the width of the box of an entity}
     */
    public static double halfWidth(final AbstractEntity entity) {
        return entity instanceof final AbstractLivingEntity living
                ? AbstractPathfinderMob.halfWidthOf(living)
                : entity.type().width() / 2.0;
    }

    /**
     * {@return the height of the box of an entity}
     */
    public static double height(final AbstractEntity entity) {
        return entity instanceof final AbstractLivingEntity living
                ? AbstractPathfinderMob.boxHeightOf(living)
                : entity.type().height();
    }

    /**
     * {@return the box of an entity, grown by {@code margin} on every side}
     */
    public static BlockCollider.Box box(final AbstractEntity entity, final double margin) {
        final Location at = entity.location();
        return BlockCollider.Box.at(at.x(), at.y() - margin, at.z(), halfWidth(entity) + margin, height(entity) + 2.0 * margin);
    }

    /**
     * {@return {@code true} if a point is inside the box of an entity grown by {@code margin}}
     */
    public static boolean contains(final AbstractEntity entity, final double margin, final double x, final double y,
                                   final double z) {
        final BlockCollider.Box box = box(entity, margin);
        return x >= box.minX() && x <= box.maxX()
                && y >= box.minY() && y <= box.maxY()
                && z >= box.minZ() && z <= box.maxZ();
    }

    /**
     * {@return the direction an entity looks, as a unit vector {@code {x, y, z}}}
     *
     * @param yaw   its yaw, in degrees
     * @param pitch its pitch, in degrees
     */
    public static double[] look(final float yaw, final float pitch) {
        final double yawRadians = Math.toRadians(yaw);
        final double pitchRadians = Math.toRadians(pitch);
        return new double[]{
                -Math.sin(yawRadians) * Math.cos(pitchRadians),
                -Math.sin(pitchRadians),
                Math.cos(yawRadians) * Math.cos(pitchRadians)};
    }

    /**
     * {@return the distance from a point to the nearest point of a box, {@code 0} inside it}
     */
    public static double distanceTo(final BlockCollider.Box box, final double x, final double y, final double z) {
        final double dx = Math.max(Math.max(box.minX() - x, 0.0), x - box.maxX());
        final double dy = Math.max(Math.max(box.minY() - y, 0.0), y - box.maxY());
        final double dz = Math.max(Math.max(box.minZ() - z, 0.0), z - box.maxZ());
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * {@return {@code true} if two boxes overlap}
     */
    public static boolean overlap(final BlockCollider.Box a, final BlockCollider.Box b) {
        return a.minX() < b.maxX() && a.maxX() > b.minX()
                && a.minY() < b.maxY() && a.maxY() > b.minY()
                && a.minZ() < b.maxZ() && a.maxZ() > b.minZ();
    }

    /**
     * {@return where along a segment it first enters a box, between {@code 0} and {@code 1}, or a
     * negative value if it misses it}
     *
     * @param box the box
     * @param x   where the segment starts along the x axis
     * @param y   where the segment starts along the y axis
     * @param z   where the segment starts along the z axis
     * @param dx  how far the segment goes along the x axis
     * @param dy  how far the segment goes along the y axis
     * @param dz  how far the segment goes along the z axis
     */
    public static double entry(final BlockCollider.Box box, final double x, final double y, final double z,
                               final double dx, final double dy, final double dz) {
        double enter = 0.0;
        double exit = 1.0;
        final double[] origin = {x, y, z};
        final double[] delta = {dx, dy, dz};
        final double[] min = {box.minX(), box.minY(), box.minZ()};
        final double[] max = {box.maxX(), box.maxY(), box.maxZ()};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(delta[axis]) < 1.0E-9) {
                if (origin[axis] < min[axis] || origin[axis] > max[axis]) {
                    return -1.0;
                }
                continue;
            }
            double near = (min[axis] - origin[axis]) / delta[axis];
            double far = (max[axis] - origin[axis]) / delta[axis];
            if (near > far) {
                final double swap = near;
                near = far;
                far = swap;
            }
            enter = Math.max(enter, near);
            exit = Math.min(exit, far);
            if (enter > exit) {
                return -1.0;
            }
        }
        return enter;
    }
}
