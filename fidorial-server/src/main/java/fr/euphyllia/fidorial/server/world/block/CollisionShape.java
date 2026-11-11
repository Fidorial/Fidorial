package fr.euphyllia.fidorial.server.world.block;

import java.util.Arrays;

/**
 * What entities bump into in a block: a few boxes, in blocks, relative to the corner of the block.
 * Most stay within the block; a fence or a wall reaches up to 1.5.
 *
 * <p>Shapes are immutable and shared by every block state that has them.</p>
 */
public final class CollisionShape {

    /**
     * Nothing to bump into: air, water, grass, a flower.
     */
    public static final CollisionShape EMPTY = new CollisionShape(new double[0]);

    /**
     * A whole block.
     */
    public static final CollisionShape FULL_BLOCK = new CollisionShape(new double[] {0, 0, 0, 1, 1, 1});

    private static final int MIN_X = 0;
    private static final int MIN_Y = 1;
    private static final int MIN_Z = 2;
    private static final int MAX_X = 3;
    private static final int MAX_Y = 4;
    private static final int MAX_Z = 5;
    private static final int STRIDE = 6;

    private final double[] boxes;
    private final double top;
    private final boolean fullBlock;

    private CollisionShape(final double[] boxes) {
        this.boxes = boxes;
        double highest = 0.0;
        for (int index = MAX_Y; index < boxes.length; index += STRIDE) {
            highest = Math.max(highest, boxes[index]);
        }
        this.top = highest;
        this.fullBlock = boxes.length == STRIDE
                && boxes[MIN_X] == 0 && boxes[MIN_Y] == 0 && boxes[MIN_Z] == 0
                && boxes[MAX_X] == 1 && boxes[MAX_Y] == 1 && boxes[MAX_Z] == 1;
    }

    /**
     * {@return a shape made of boxes}
     *
     * @param boxes {@code minX, minY, minZ, maxX, maxY, maxZ} for each box, one after the other
     */
    public static CollisionShape of(final double[] boxes) {
        if (boxes.length % STRIDE != 0) {
            throw new IllegalArgumentException("A collision shape needs 6 numbers per box, got " + boxes.length);
        }
        if (boxes.length == 0) {
            return EMPTY;
        }
        final CollisionShape shape = new CollisionShape(boxes.clone());
        return shape.fullBlock ? FULL_BLOCK : shape;
    }

    public boolean isEmpty() {
        return boxes.length == 0;
    }

    public boolean isFullBlock() {
        return fullBlock;
    }

    /**
     * {@return the height of the highest box, {@code 0} for an empty shape}
     */
    public double top() {
        return top;
    }

    public int boxCount() {
        return boxes.length / STRIDE;
    }

    public double minX(final int box) {
        return boxes[box * STRIDE + MIN_X];
    }

    public double minY(final int box) {
        return boxes[box * STRIDE + MIN_Y];
    }

    public double minZ(final int box) {
        return boxes[box * STRIDE + MIN_Z];
    }

    public double maxX(final int box) {
        return boxes[box * STRIDE + MAX_X];
    }

    public double maxY(final int box) {
        return boxes[box * STRIDE + MAX_Y];
    }

    public double maxZ(final int box) {
        return boxes[box * STRIDE + MAX_Z];
    }

    @Override
    public String toString() {
        return "CollisionShape" + Arrays.toString(boxes);
    }
}
