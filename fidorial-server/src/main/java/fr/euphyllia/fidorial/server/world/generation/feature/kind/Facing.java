package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;

/**
 * The six directions as small numbers, with their steps and names, and the shuffles the features draw.
 *
 * <p>Order: down, up, north, south, west, east. The horizontal ones turn clockwise north, east, south, west.</p>
 */
final class Facing {

    static final int DOWN = 0;
    static final int UP = 1;
    static final int NORTH = 2;
    static final int SOUTH = 3;
    static final int WEST = 4;
    static final int EAST = 5;

    static final int[] DX = {0, 0, 0, 0, -1, 1};
    static final int[] DY = {-1, 1, 0, 0, 0, 0};
    static final int[] DZ = {0, 0, -1, 1, 0, 0};
    static final String[] NAMES = {"down", "up", "north", "south", "west", "east"};
    static final int[] OPPOSITE = {UP, DOWN, SOUTH, NORTH, EAST, WEST};

    /**
     * The horizontal directions, clockwise from north.
     */
    static final int[] HORIZONTAL = {NORTH, EAST, SOUTH, WEST};
    static final int[] ALL = {DOWN, UP, NORTH, SOUTH, WEST, EAST};

    private Facing() {
    }

    static int clockwise(final int direction) {
        return switch (direction) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> direction;
        };
    }

    static int counterClockwise(final int direction) {
        return switch (direction) {
            case NORTH -> WEST;
            case WEST -> SOUTH;
            case SOUTH -> EAST;
            case EAST -> NORTH;
            default -> direction;
        };
    }

    static boolean isHorizontal(final int direction) {
        return direction >= NORTH;
    }

    /**
     * {@return the axis a direction runs along: x, y or z}
     */
    static String axis(final int direction) {
        return direction <= UP ? "y" : direction <= SOUTH ? "z" : "x";
    }

    static int byName(final String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(name)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown direction " + name);
    }

    /**
     * {@return a shuffled copy}: from the last slot down, each slot swaps with one drawn among those before it,
     * itself included
     */
    static int[] shuffled(final int[] values, final BitRandom random) {
        final int[] copy = values.clone();
        for (int i = copy.length; i > 1; i--) {
            final int swapTo = random.nextInt(i);
            final int kept = copy[i - 1];
            copy[i - 1] = copy[swapTo];
            copy[swapTo] = kept;
        }
        return copy;
    }

    static int randomHorizontal(final BitRandom random) {
        return HORIZONTAL[random.nextInt(HORIZONTAL.length)];
    }

    static int random(final BitRandom random) {
        return ALL[random.nextInt(ALL.length)];
    }
}
