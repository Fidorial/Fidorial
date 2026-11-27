package fr.fidorial.world.block;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import org.jspecify.annotations.Nullable;

/**
 * Where and how a player is placing a block, handed to
 * {@link BlockBehaviour#placementState(BlockPlaceContext)}.
 *
 * <p>The server makes it; the default methods work out what most blocks need from the rest.</p>
 *
 * @since 0.1.0
 */
public interface BlockPlaceContext {

    /**
     * {@return the position the block is placed at}
     *
     * @since 0.1.0
     */
    BlockPosition pos();

    /**
     * {@return the face of the neighbouring block the player clicked}
     *
     * @since 0.1.0
     */
    BlockFace clickedFace();

    /**
     * {@return the location and orientation of the player}
     *
     * @since 0.1.0
     */
    Location placer();

    /**
     * {@return read access to the surrounding blocks}
     *
     * @since 0.1.0
     */
    BlockGetter world();

    /**
     * {@return the height of the click on the clicked face, from {@code 0} to {@code 1}}
     *
     * @since 0.1.0
     */
    float cursorY();

    /**
     * {@return {@code true} when the player sneaks while placing the block: a chest then stays alone,
     * unless it is placed against the side of another chest, which it joins}
     *
     * @since 0.1.0
     */
    boolean sneaking();

    /**
     * {@return the horizontal direction the player looks towards}
     *
     * @since 0.1.0
     */
    default BlockFace horizontalFacing() {
        return switch (Math.floorMod(Math.round(placer().yaw() / 90f), 4)) {
            case 0 -> BlockFace.SOUTH;
            case 1 -> BlockFace.WEST;
            case 2 -> BlockFace.NORTH;
            default -> BlockFace.EAST;
        };
    }

    /**
     * {@return the direction the player looks towards, vertical ones included}
     *
     * @since 0.1.0
     */
    default BlockFace lookingDirection() {
        final double yaw = Math.toRadians(placer().yaw());
        final double pitch = Math.toRadians(placer().pitch());
        final double cosPitch = Math.cos(pitch);

        final double x = -Math.sin(yaw) * cosPitch;
        final double y = -Math.sin(pitch);
        final double z = Math.cos(yaw) * cosPitch;

        final double absX = Math.abs(x);
        final double absY = Math.abs(y);
        final double absZ = Math.abs(z);

        if (absY >= absX && absY >= absZ) {
            return y > 0 ? BlockFace.UP : BlockFace.DOWN;
        }
        if (absX >= absZ) {
            return x > 0 ? BlockFace.EAST : BlockFace.WEST;
        }
        return z > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }

    /**
     * {@return {@code true} when the block should go in the upper half, for slabs and stairs}
     *
     * @since 0.1.0
     */
    default boolean upperHalf() {
        return switch (clickedFace()) {
            case UP -> false;
            case DOWN -> true;
            default -> cursorY() > 0.5f;
        };
    }

    /**
     * {@return the state about to be replaced, or {@code null} if it is not loaded}
     *
     * @since 0.1.0
     */
    default @Nullable BlockData replaced() {
        return world().blockAt(pos());
    }

    /**
     * {@return the state next to the placed block, or {@code null} if it is not loaded}
     *
     * @param face the side to look at
     * @since 0.1.0
     */
    default @Nullable BlockData relative(final BlockFace face) {
        return world().blockAt(pos().relative(face));
    }

    /**
     * {@return {@code true} when the block on that side can hold the placed block: it is loaded, and neither
     * air nor a fluid}
     *
     * @param side the side the placed block would be attached to
     * @since 0.1.0
     */
    default boolean canAttachTo(final BlockFace side) {
        final BlockData neighbour = relative(side);
        return neighbour != null && !neighbour.isAir() && !neighbour.isFluid();
    }

    /**
     * {@return {@code true} when the block replaces a water source, and should be waterlogged}
     *
     * @since 0.1.0
     */
    default boolean intoWater() {
        final BlockData replaced = replaced();
        return replaced != null && replaced.key().equals(BlockTypeKeys.WATER) && replaced.get(BlockProperties.LEVEL_0_15) == 0;
    }
}
