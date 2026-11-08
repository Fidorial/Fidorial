package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.fidorial.math.Location;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.redstone.SupportType;

/**
 * What vanilla's {@code BlockPlaceContext} offers the redstone blocks and Fidorial's does not.
 */
final class Placement {

    private Placement() {
        throw new UnsupportedOperationException("Placement cannot be instantiated.");
    }

    /**
     * {@return the six directions, from the one the player looks the most towards to the one they
     * look the least towards, the face they clicked against first}: vanilla's
     * {@code getNearestLookingDirections}, the order levers, buttons and torches try to attach in
     *
     * @param context the placement
     */
    static BlockFace[] nearestLookingDirections(final BlockPlaceContext context) {
        final BlockFace[] directions = orderedByNearest(context.placer());
        final BlockFace clicked = context.clickedFace().opposite();
        int index = 0;
        while (index < directions.length && directions[index] != clicked) {
            index++;
        }
        if (index > 0 && index < directions.length) {
            System.arraycopy(directions, 0, directions, 1, index);
            directions[0] = clicked;
        }
        return directions;
    }

    /**
     * {@return {@code true} if the block on one side of the placed block can hold it by a whole face}
     *
     * @param context the placement
     * @param side    the side of the placed block its support is on
     */
    static boolean canAttach(final BlockPlaceContext context, final BlockFace side) {
        final BlockData support = context.relative(side);
        return BlockSupport.isFaceSturdy(support, side.opposite(), SupportType.FULL);
    }

    /**
     * Vanilla's {@code Direction.orderedByNearest}.
     */
    private static BlockFace[] orderedByNearest(final Location placer) {
        final double pitch = Math.toRadians(placer.pitch());
        final double yaw = -Math.toRadians(placer.yaw());
        final double pitchSin = Math.sin(pitch);
        final double pitchCos = Math.cos(pitch);
        final double yawSin = Math.sin(yaw);
        final double yawCos = Math.cos(yaw);
        final boolean xPos = yawSin > 0.0;
        final boolean yPos = pitchSin < 0.0;
        final boolean zPos = yawCos > 0.0;
        final double xYaw = xPos ? yawSin : -yawSin;
        final double yMag = yPos ? -pitchSin : pitchSin;
        final double zYaw = zPos ? yawCos : -yawCos;
        final double xMag = xYaw * pitchCos;
        final double zMag = zYaw * pitchCos;
        final BlockFace axisX = xPos ? BlockFace.EAST : BlockFace.WEST;
        final BlockFace axisY = yPos ? BlockFace.UP : BlockFace.DOWN;
        final BlockFace axisZ = zPos ? BlockFace.SOUTH : BlockFace.NORTH;
        if (xYaw > zYaw) {
            if (yMag > xMag) {
                return directions(axisY, axisX, axisZ);
            }
            return zMag > yMag ? directions(axisX, axisZ, axisY) : directions(axisX, axisY, axisZ);
        }
        if (yMag > zMag) {
            return directions(axisY, axisZ, axisX);
        }
        return xMag > yMag ? directions(axisZ, axisX, axisY) : directions(axisZ, axisY, axisX);
    }

    private static BlockFace[] directions(final BlockFace first, final BlockFace second, final BlockFace third) {
        return new BlockFace[]{first, second, third, third.opposite(), second.opposite(), first.opposite()};
    }
}
