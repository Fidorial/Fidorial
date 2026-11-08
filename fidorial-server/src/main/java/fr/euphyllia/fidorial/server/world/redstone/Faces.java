package fr.euphyllia.fidorial.server.world.redstone;

import fr.fidorial.world.BlockFace;

import java.util.List;

/**
 * The orders vanilla walks the six faces of a block in, and the rotations redstone needs. The lists
 * cannot be modified.
 */
public final class Faces {

    /**
     * Down, up, north, south, west, east: the order signals are read in.
     */
    public static final List<BlockFace> ALL = List.of(BlockFace.values());

    /**
     * West, east, down, up, north, south: the order neighbours are told a block changed.
     */
    public static final List<BlockFace> UPDATE_ORDER = List.of(
            BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH);

    /**
     * West, east, north, south, down, up: the order the shapes of neighbours are updated in.
     */
    public static final List<BlockFace> SHAPE_ORDER = List.of(
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN, BlockFace.UP);

    /**
     * North, east, south, west.
     */
    public static final List<BlockFace> HORIZONTAL = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    /**
     * Up, down.
     */
    public static final List<BlockFace> VERTICAL = List.of(BlockFace.UP, BlockFace.DOWN);

    private Faces() {
        throw new UnsupportedOperationException("Faces cannot be instantiated.");
    }

    /**
     * {@return {@code true} for the four horizontal faces}
     *
     * @param face the face
     */
    public static boolean isHorizontal(final BlockFace face) {
        return face != BlockFace.UP && face != BlockFace.DOWN;
    }

    /**
     * {@return the horizontal face a quarter turn clockwise, seen from above}
     *
     * @param face a horizontal face
     * @throws IllegalArgumentException if the face is vertical
     */
    public static BlockFace clockWise(final BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> throw new IllegalArgumentException("Unable to get the clockwise face of " + face);
        };
    }

    /**
     * {@return the horizontal face a quarter turn counterclockwise, seen from above}
     *
     * @param face a horizontal face
     * @throws IllegalArgumentException if the face is vertical
     */
    public static BlockFace counterClockWise(final BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.WEST;
            case WEST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.EAST;
            case EAST -> BlockFace.NORTH;
            default -> throw new IllegalArgumentException("Unable to get the counterclockwise face of " + face);
        };
    }

    /**
     * {@return {@code true} if two faces lie on the same axis}
     *
     * @param first  a face
     * @param second another face
     */
    public static boolean sameAxis(final BlockFace first, final BlockFace second) {
        return first == second || first == second.opposite();
    }
}
