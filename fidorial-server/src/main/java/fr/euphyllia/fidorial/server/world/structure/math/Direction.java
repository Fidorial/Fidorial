package fr.euphyllia.fidorial.server.world.structure.math;

import fr.fidorial.world.structure.StructureRotation;
import org.jspecify.annotations.Nullable;

public enum Direction {
    DOWN("down", 0, -1, 0),
    UP("up", 0, 1, 0),
    NORTH("north", 0, 0, -1),
    SOUTH("south", 0, 0, 1),
    WEST("west", -1, 0, 0),
    EAST("east", 1, 0, 0);

    public static final Direction[] HORIZONTAL = {NORTH, EAST, SOUTH, WEST};

    private final String serializedName;
    private final int stepX;
    private final int stepY;
    private final int stepZ;

    Direction(final String serializedName, final int stepX, final int stepY, final int stepZ) {
        this.serializedName = serializedName;
        this.stepX = stepX;
        this.stepY = stepY;
        this.stepZ = stepZ;
    }

    public static @Nullable Direction byName(final String name) {
        for (final Direction direction : values()) {
            if (direction.serializedName.equals(name)) {
                return direction;
            }
        }
        return null;
    }

    public String serializedName() {
        return serializedName;
    }

    public int stepX() {
        return stepX;
    }

    public int stepY() {
        return stepY;
    }

    public int stepZ() {
        return stepZ;
    }

    public boolean isHorizontal() {
        return stepY == 0;
    }

    public Direction opposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }

    public Direction clockwise() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
            default -> this;
        };
    }

    public Direction rotate(final StructureRotation rotation) {
        Direction result = this;
        for (int i = 0; i < rotation.quarterTurns(); i++) {
            result = result.clockwise();
        }
        return result;
    }
}
