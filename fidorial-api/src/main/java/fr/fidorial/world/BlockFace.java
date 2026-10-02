package fr.fidorial.world;

/**
 * One of the six sides of a block, in protocol order.
 *
 * @since 0.1.0
 */
public enum BlockFace {
    /**
     * Towards negative Y.
     */
    DOWN(0, -1, 0),
    /**
     * Towards positive Y.
     */
    UP(0, 1, 0),
    /**
     * Towards negative Z.
     */
    NORTH(0, 0, -1),
    /**
     * Towards positive Z.
     */
    SOUTH(0, 0, 1),
    /**
     * Towards negative X.
     */
    WEST(-1, 0, 0),
    /**
     * Towards positive X.
     */
    EAST(1, 0, 0);

    private static final BlockFace[] BY_ID = values();

    private final int dx;
    private final int dy;
    private final int dz;

    BlockFace(final int dx, final int dy, final int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    /**
     * {@return the face with the given protocol identifier, or {@link #UP} when it is out of range}
     *
     * @param id the protocol identifier, {@code 0} to {@code 5}
     * @since 0.1.0
     */
    public static BlockFace byId(final int id) {
        return (id >= 0 && id < BY_ID.length) ? BY_ID[id] : UP;
    }

    /**
     * {@return the face pointing the other way}
     *
     * @since 0.1.0
     */
    public BlockFace opposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }

    /**
     * {@return the step this face makes on the X axis, {@code -1}, {@code 0} or {@code 1}}
     *
     * @since 0.1.0
     */
    public int dx() {
        return dx;
    }

    /**
     * {@return the step this face makes on the Y axis, {@code -1}, {@code 0} or {@code 1}}
     *
     * @since 0.1.0
     */
    public int dy() {
        return dy;
    }

    /**
     * {@return the step this face makes on the Z axis, {@code -1}, {@code 0} or {@code 1}}
     *
     * @since 0.1.0
     */
    public int dz() {
        return dz;
    }
}
