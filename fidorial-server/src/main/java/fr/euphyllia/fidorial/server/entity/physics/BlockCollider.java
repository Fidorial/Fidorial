package fr.euphyllia.fidorial.server.entity.physics;

import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockCollision;
import fr.euphyllia.fidorial.server.world.block.CollisionShape;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.registry.keys.BlockTypeKeys;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/**
 * Moves the box of an entity through the blocks of a world the way Vanilla does.
 *
 * <p>A move is made one axis at a time, the vertical one first, then the longer of the two
 * horizontal ones: along each axis the box goes as far as the boxes of the blocks in its way let
 * it. A block whose box the entity already overlaps does not hold it back, so that an entity stuck
 * in a block can always get out of it.</p>
 *
 * <p>An entity on the ground that bumps into something no higher than its step height climbs onto
 * it without jumping: a slab, a path, a carpet, a layer of snow.</p>
 *
 * <p>Blocks are seen through their {@link BlockCollision collision shape}: grass, flowers, water or
 * rails are no obstacle; a slab is half a block; a fence one and a half. A block in a chunk that is
 * not loaded is a full block, so that nothing falls out of the loaded world.</p>
 */
public final class BlockCollider {

    /**
     * How close two faces must be to count as touching, in blocks.
     */
    private static final double EPSILON = 1.0E-7;

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    private BlockCollider() {
        throw new UnsupportedOperationException("BlockCollider cannot be instantiated.");
    }

    /**
     * Which shape an entity bumps into in a block: the block's own, but for the few blocks that
     * treat some entities differently.
     */
    @FunctionalInterface
    public interface Shapes {

        /**
         * The shapes of the blocks, the same for every entity.
         */
        Shapes DEFAULT = BlockCollision::shape;

        /**
         * The shapes of the blocks for an entity that walks on powder snow rather than sinking in it.
         */
        Shapes POWDER_SNOW_WALKER = state -> state.name().equals(BlockTypeKeys.POWDER_SNOW)
                ? PowderSnow.WALKED_ON
                : BlockCollision.shape(state);

        CollisionShape shapeOf(BlockState state);
    }

    /**
     * The box of an entity.
     */
    public record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

        /**
         * {@return the box of an entity standing at a point}
         *
         * @param x         the middle of its feet
         * @param y         the bottom of its feet
         * @param z         the middle of its feet
         * @param halfWidth half of its width
         * @param height    its height
         */
        public static Box at(final double x, final double y, final double z, final double halfWidth, final double height) {
            return new Box(x - halfWidth, y, z - halfWidth, x + halfWidth, y + height, z + halfWidth);
        }

        public Box move(final double dx, final double dy, final double dz) {
            return new Box(minX + dx, minY + dy, minZ + dz, maxX + dx, maxY + dy, maxZ + dz);
        }

        /**
         * {@return this box stretched towards a direction, to cover what it sweeps through}
         */
        public Box expandTowards(final double dx, final double dy, final double dz) {
            return new Box(
                    dx < 0 ? minX + dx : minX, dy < 0 ? minY + dy : minY, dz < 0 ? minZ + dz : minZ,
                    dx > 0 ? maxX + dx : maxX, dy > 0 ? maxY + dy : maxY, dz > 0 ? maxZ + dz : maxZ);
        }
    }

    /**
     * How far an entity got, out of the move it tried.
     */
    public record Movement(double x, double y, double z) {

        double horizontalDistanceSquared() {
            return x * x + z * z;
        }

        Movement add(final Movement other) {
            return new Movement(x + other.x, y + other.y, z + other.z);
        }
    }

    /**
     * Moves a box as far as it gets towards where it heads.
     *
     * @param box        the box of the entity, where it stands
     * @param dx         how far it tries to go along X
     * @param dy         how far it tries to go along Y
     * @param dz         how far it tries to go along Z
     * @param stepHeight how high a block it climbs without jumping, {@code 0} for none
     * @param onGround   whether it stands on something before the move
     * @return how far it got
     */
    public static Movement move(final ServerWorld world, final Shapes shapes, final Box box,
                                final double dx, final double dy, final double dz,
                                final double stepHeight, final boolean onGround) {
        if (dx == 0.0 && dy == 0.0 && dz == 0.0) {
            return new Movement(0.0, 0.0, 0.0);
        }
        final double[] obstacles = obstacles(world, shapes,
                box.expandTowards(dx, dy, dz).expandTowards(0.0, Math.max(0.0, stepHeight), 0.0));

        final Movement moved = collide(box, dx, dy, dz, obstacles);
        final boolean collidedX = moved.x() != dx;
        final boolean collidedZ = moved.z() != dz;
        final boolean landed = onGround || (moved.y() != dy && dy < 0.0);
        if (stepHeight <= 0.0 || !landed || (!collidedX && !collidedZ)) {
            return moved;
        }

        // climb what is in the way, if it is low enough: try the move from as high as it may step...
        Movement stepped = collide(box, dx, stepHeight, dz, obstacles);
        // ...and from as high as the ceiling lets it, if that is lower
        final Movement up = collide(box.expandTowards(dx, 0.0, dz), 0.0, stepHeight, 0.0, obstacles);
        if (up.y() < stepHeight) {
            final Movement across = collide(box.move(0.0, up.y(), 0.0), dx, 0.0, dz, obstacles)
                    .add(new Movement(0.0, up.y(), 0.0));
            if (across.horizontalDistanceSquared() > stepped.horizontalDistanceSquared()) {
                stepped = across;
            }
        }
        if (stepped.horizontalDistanceSquared() <= moved.horizontalDistanceSquared()) {
            return moved;
        }
        // then settle back down onto what it climbed
        final Movement settle = collide(box.move(stepped.x(), stepped.y(), stepped.z()), 0.0, dy - stepped.y(), 0.0, obstacles);
        return stepped.add(settle);
    }

    /**
     * {@return {@code true} if a box overlaps the shape of a block}
     */
    public static boolean collides(final ServerWorld world, final Shapes shapes, final Box box) {
        final double[] obstacles = obstacles(world, shapes, box);
        for (int index = 0; index < obstacles.length; index += 6) {
            if (box.maxX() > obstacles[index] + EPSILON && box.minX() < obstacles[index + 3] - EPSILON
                    && box.maxY() > obstacles[index + 1] + EPSILON && box.minY() < obstacles[index + 4] - EPSILON
                    && box.maxZ() > obstacles[index + 2] + EPSILON && box.minZ() < obstacles[index + 5] - EPSILON) {
                return true;
            }
        }
        return false;
    }

    /**
     * Moves a box along each axis in turn: the vertical one, then the longer of the two others.
     */
    private static Movement collide(final Box box, final double dx, final double dy, final double dz,
                                    final double[] obstacles) {
        Box current = box;
        double movedY = 0.0;
        if (dy != 0.0) {
            movedY = clip(Y, current, dy, obstacles);
            current = current.move(0.0, movedY, 0.0);
        }
        double movedX = 0.0;
        double movedZ = 0.0;
        if (Math.abs(dx) < Math.abs(dz)) {
            if (dz != 0.0) {
                movedZ = clip(Z, current, dz, obstacles);
                current = current.move(0.0, 0.0, movedZ);
            }
            if (dx != 0.0) {
                movedX = clip(X, current, dx, obstacles);
            }
        } else {
            if (dx != 0.0) {
                movedX = clip(X, current, dx, obstacles);
                current = current.move(movedX, 0.0, 0.0);
            }
            if (dz != 0.0) {
                movedZ = clip(Z, current, dz, obstacles);
            }
        }
        return new Movement(movedX, movedY, movedZ);
    }

    /**
     * {@return how far a box gets along one axis before it hits one of the obstacles}
     */
    private static double clip(final int axis, final Box box, final double distance, final double[] obstacles) {
        double allowed = distance;
        final double boxMin = min(box, axis);
        final double boxMax = max(box, axis);
        for (int index = 0; index < obstacles.length; index += 6) {
            if (!overlapsAcross(axis, box, obstacles, index)) {
                continue;
            }
            final double obstacleMin = obstacles[index + axis];
            final double obstacleMax = obstacles[index + 3 + axis];
            if (allowed > 0.0 && obstacleMin >= boxMax - EPSILON) {
                allowed = Math.min(allowed, obstacleMin - boxMax);
            } else if (allowed < 0.0 && obstacleMax <= boxMin + EPSILON) {
                allowed = Math.max(allowed, obstacleMax - boxMin);
            }
        }
        return Math.abs(allowed) < EPSILON ? 0.0 : allowed;
    }

    /**
     * {@return {@code true} if a box and an obstacle overlap on the two axes other than {@code axis}}
     */
    private static boolean overlapsAcross(final int axis, final Box box, final double[] obstacles, final int index) {
        for (int other = 0; other < 3; other++) {
            if (other == axis) {
                continue;
            }
            if (max(box, other) <= obstacles[index + other] + EPSILON
                    || min(box, other) >= obstacles[index + 3 + other] - EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static double min(final Box box, final int axis) {
        return switch (axis) {
            case X -> box.minX();
            case Y -> box.minY();
            default -> box.minZ();
        };
    }

    private static double max(final Box box, final int axis) {
        return switch (axis) {
            case X -> box.maxX();
            case Y -> box.maxY();
            default -> box.maxZ();
        };
    }

    /**
     * {@return the boxes of the blocks a region reaches, in world coordinates, six numbers each}:
     * the blocks it covers and those right below, whose shape may stick up into it, as a fence does.
     */
    private static double[] obstacles(final ServerWorld world, final Shapes shapes, final Box region) {
        final int fromX = floor(region.minX() - EPSILON);
        final int toX = floor(region.maxX() + EPSILON);
        final int fromY = floor(region.minY() - EPSILON) - 1;
        final int toY = floor(region.maxY() + EPSILON);
        final int fromZ = floor(region.minZ() - EPSILON);
        final int toZ = floor(region.maxZ() + EPSILON);
        final int worldMin = world.minY();
        final int worldMax = world.minY() + world.height();

        final DoubleArrayList boxes = new DoubleArrayList();
        ChunkColumn column = null;
        for (int x = fromX; x <= toX; x++) {
            for (int z = fromZ; z <= toZ; z++) {
                if (column == null || column.chunkX() != x >> 4 || column.chunkZ() != z >> 4) {
                    column = world.loadedColumn(x >> 4, z >> 4);
                }
                for (int y = fromY; y <= toY; y++) {
                    if (y < worldMin || y >= worldMax) {
                        continue; // above and below the world is open
                    }
                    final CollisionShape shape = column == null
                            ? CollisionShape.FULL_BLOCK
                            : shapes.shapeOf(column.getBlock(x & 15, y, z & 15));
                    for (int box = 0; box < shape.boxCount(); box++) {
                        boxes.add(x + shape.minX(box));
                        boxes.add(y + shape.minY(box));
                        boxes.add(z + shape.minZ(box));
                        boxes.add(x + shape.maxX(box));
                        boxes.add(y + shape.maxY(box));
                        boxes.add(z + shape.maxZ(box));
                    }
                }
            }
        }
        return boxes.toDoubleArray();
    }

    private static int floor(final double value) {
        return (int) Math.floor(value);
    }

    /**
     * The shape of powder snow under the feet of those who walk on it.
     */
    private static final class PowderSnow {
        static final CollisionShape WALKED_ON = CollisionShape.of(new double[] {0, 0, 0, 1, 0.9, 1});
    }
}
