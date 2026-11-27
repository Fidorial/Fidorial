package fr.fidorial.world.block.data.type;

import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockTrait;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Waterlogged;

import java.util.Optional;

/**
 * The block state of a chest, a trapped chest or a copper chest: the way its front faces, whether
 * it holds water, and whether it is half of a large chest.
 *
 * <p>Two chests form a large chest when each one's {@link #partnerDirection()} points at the other:
 * the {@link Type#RIGHT right} half has its partner on its own left, that is on the right of a
 * player facing its front, and the {@link Type#LEFT left} half has it on its own right.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(values = @BlockTrait.Values(property = Names.TYPE, allowed = {"single", "left", "right"}, exactly = true))
public interface Chest extends Directional, Waterlogged {

    @Override
    default Chest withFacing(final BlockFace facing) {
        return (Chest) Directional.super.withFacing(facing);
    }

    @Override
    default Chest withWaterlogged(final boolean waterlogged) {
        return (Chest) Waterlogged.super.withWaterlogged(waterlogged);
    }

    /**
     * {@return whether the chest stands alone or which half of a large chest it is}
     *
     * @since 0.1.0
     */
    default Type getType() {
        return get(type().choice(Names.TYPE, Type.class));
    }

    /**
     * {@return the same chest, standing alone or as another half}
     *
     * <p>This only changes the state: the chest next to it is not told.</p>
     *
     * @param type the half the chest is, {@link Type#SINGLE} to stand alone
     * @since 0.1.0
     */
    default Chest withType(final Type type) {
        return (Chest) with(type().choice(Names.TYPE, Type.class), type);
    }

    /**
     * {@return the side the other half of the large chest is on, empty for a chest standing alone}
     *
     * @since 0.1.0
     */
    default Optional<BlockFace> partnerDirection() {
        return partnerDirection(getFacing(), getType());
    }

    /**
     * {@return the side the other half of a large chest is on, empty for {@link Type#SINGLE}}
     *
     * @param facing the side the front of the chest faces, a horizontal one
     * @param type   the half the chest is
     * @throws IllegalArgumentException if {@code facing} is vertical
     * @since 0.1.0
     */
    static Optional<BlockFace> partnerDirection(final BlockFace facing, final Type type) {
        return switch (type) {
            case SINGLE -> Optional.empty();
            // the left half, seen from the chest, has its partner on its right: a quarter turn clockwise
            case LEFT -> Optional.of(turn(facing, true));
            case RIGHT -> Optional.of(turn(facing, false));
        };
    }

    private static BlockFace turn(final BlockFace facing, final boolean clockwise) {
        return switch (facing) {
            case NORTH -> clockwise ? BlockFace.EAST : BlockFace.WEST;
            case EAST -> clockwise ? BlockFace.SOUTH : BlockFace.NORTH;
            case SOUTH -> clockwise ? BlockFace.WEST : BlockFace.EAST;
            case WEST -> clockwise ? BlockFace.NORTH : BlockFace.SOUTH;
            case UP, DOWN -> throw new IllegalArgumentException("A chest faces a horizontal side, not " + facing);
        };
    }

    /**
     * Whether a chest stands alone, and which half of a large chest it is otherwise.
     *
     * @since 0.1.0
     */
    enum Type {

        /**
         * A chest standing alone. Stored as {@code single}.
         */
        SINGLE("single"),

        /**
         * The left half of a large chest, seen from the chest: the second half of its window.
         * Stored as {@code left}.
         */
        LEFT("left"),

        /**
         * The right half of a large chest, seen from the chest: the first half of its window.
         * Stored as {@code right}.
         */
        RIGHT("right");

        private final String value;

        Type(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static Type fromValue(final String value) {
            for (final Type type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown chest type: " + value);
        }

        /**
         * {@return the value as stored in the block state}
         *
         * @since 0.1.0
         */
        public String value() {
            return value;
        }

        /**
         * {@return the half the partner of a chest of this half is: left for right, right for left,
         * single for single}
         *
         * @since 0.1.0
         */
        public Type opposite() {
            return switch (this) {
                case SINGLE -> SINGLE;
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
            };
        }
    }
}
