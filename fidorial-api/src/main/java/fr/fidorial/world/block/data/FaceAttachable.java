package fr.fidorial.world.block.data;

import com.google.common.base.Preconditions;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockTrait;

/**
 * A block state carrying the {@code face} property: whether the block hangs on the floor, on a wall
 * or on the ceiling, like levers, buttons and grindstones. On a wall, {@link #getFacing()} points
 * away from it.
 *
 * <p>Block states are immutable: {@code with...} methods return another state and leave this one
 * untouched.</p>
 *
 * @since 0.1.0
 */
@BlockTrait(values =
    @BlockTrait.Values(property = "face", allowed = {"floor", "wall", "ceiling"}, exactly = true)
)
public interface FaceAttachable extends Directional {

    @Override
    default FaceAttachable withFacing(final BlockFace facing) {
        return (FaceAttachable) Directional.super.withFacing(facing);
    }

    /**
     * {@return the surface the block hangs on}
     *
     * @since 0.1.0
     */
    default AttachedFace getAttachedFace() {
        final String face = get("face");
        Preconditions.checkState(face != null, "%s has no face property", key());
        return AttachedFace.fromValue(face);
    }

    /**
     * {@return the same block hanging on another surface}
     *
     * @param face the surface to hang on
     * @since 0.1.0
     */
    default FaceAttachable withAttachedFace(final AttachedFace face) {
        return (FaceAttachable) with("face", face.value());
    }

    /**
     * {@return the side of this block its support is on: {@link BlockFace#DOWN} on the floor,
     * {@link BlockFace#UP} on the ceiling, behind it on a wall}
     *
     * @since 0.1.0
     */
    default BlockFace getSupportSide() {
        return switch (getAttachedFace()) {
            case FLOOR -> BlockFace.DOWN;
            case CEILING -> BlockFace.UP;
            case WALL -> getFacing().opposite();
        };
    }

    /**
     * The surfaces a block can hang on.
     *
     * @since 0.1.0
     */
    enum AttachedFace {
        /**
         * Stored as {@code floor}: on top of the block below.
         */
        FLOOR("floor"),
        /**
         * Stored as {@code wall}: against the side of a block.
         */
        WALL("wall"),
        /**
         * Stored as {@code ceiling}: under the block above.
         */
        CEILING("ceiling");

        private final String value;

        AttachedFace(final String value) {
            this.value = value;
        }

        /**
         * {@return the value of this enum stored as the given string}
         *
         * @param value the value as stored in the block state
         * @throws IllegalArgumentException if the value is unknown
         * @since 0.1.0
         */
        public static AttachedFace fromValue(final String value) {
            for (final AttachedFace face : values()) {
                if (face.value.equals(value)) {
                    return face;
                }
            }
            throw new IllegalArgumentException("Unknown attached face: " + value);
        }

        /**
         * {@return the value as stored in the block state}
         *
         * @since 0.1.0
         */
        public String value() {
            return value;
        }
    }
}
