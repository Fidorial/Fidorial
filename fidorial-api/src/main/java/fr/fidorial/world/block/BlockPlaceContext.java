package fr.fidorial.world.block;

import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

public record BlockPlaceContext(BlockPos pos, BlockFace clickedFace, Location placer, BlockGetter world, float cursorY) {

    private static final Key WATER = BlockTypeKeys.WATER.key();

    public BlockFace horizontalFacing() {
        return switch (Math.floorMod(Math.round(placer.yaw() / 90f), 4)) {
            case 0 -> BlockFace.SOUTH;
            case 1 -> BlockFace.WEST;
            case 2 -> BlockFace.NORTH;
            default -> BlockFace.EAST;
        };
    }

    public BlockFace lookingDirection() {
        final double yaw = Math.toRadians(placer.yaw());
        final double pitch = Math.toRadians(placer.pitch());
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

    public boolean upperHalf() {
        return switch (clickedFace) {
            case UP -> false;
            case DOWN -> true;
            default -> cursorY > 0.5f;
        };
    }

    public @Nullable BlockData replaced() {
        return world.blockAt(pos);
    }

    public @Nullable BlockData relative(final BlockFace face) {
        return world.blockAt(pos.relative(face));
    }

    public boolean intoWater() {
        final BlockData replaced = replaced();
        return replaced != null && replaced.key().equals(WATER) && "0".equals(replaced.get("level"));
    }

}
