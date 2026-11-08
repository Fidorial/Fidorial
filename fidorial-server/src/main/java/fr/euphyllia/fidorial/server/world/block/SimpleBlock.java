package fr.euphyllia.fidorial.server.world.block;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class SimpleBlock implements BlockBehaviour {

    private final TypedKey<BlockType> key;
    private final int opacity;
    private final int emission;

    private SimpleBlock(final TypedKey<BlockType> key, final int opacity, final int emission) {
        this.key = key;
        this.opacity = opacity;
        this.emission = emission;
    }

    public static SimpleBlock opaque(final TypedKey<BlockType> key) {
        return new SimpleBlock(key, 15, 0);
    }

    public static SimpleBlock transparent(final TypedKey<BlockType> key) {
        return new SimpleBlock(key, 0, 0);
    }

    public static SimpleBlock of(final TypedKey<BlockType> key, final int opacity, final int emission) {
        return new SimpleBlock(key, opacity, emission);
    }

    @Override
    public BlockType type() {
        return Objects.requireNonNull(Blocks.type(key));
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return opacity;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return emission;
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        return placementState(type(), context);
    }

    /**
     * Places a block the generic way: its facing, axis, rail shape, half and waterlogging follow the
     * placing player, every other property keeps its default value.
     *
     * <p>Behaviours that only add to the generic placement, such as the redstone ones powering a door
     * placed next to a signal, start from it.</p>
     *
     * @param type    the block to place
     * @param context where and how the player places it
     * @return the state to place, {@code null} for a block type without states
     */
    public static @Nullable BlockData placementState(final BlockType type, final BlockPlaceContext context) {
        BlockData data = type.defaultData();
        if (data == null) {
            return null;
        }

        if (type.hasProperty("facing")) {
            final int faceCount = Objects.requireNonNull(type.property("facing")).values().size();
            final BlockFace facing;
            if (faceCount >= 6) {
                facing = context.lookingDirection().opposite();
            } else if (facesPlayerDirection(type)) {
                facing = context.horizontalFacing().opposite();
            } else {
                facing = context.horizontalFacing();
            }
            data = data.with("facing", faceName(facing));
        }

        if (type.hasProperty("axis")) {
            data = data.with("axis", axisOf(context.clickedFace()));
        }

        if (type.hasProperty("shape") && isPlainRailShape(type.property("shape"))) {
            final boolean northSouth = switch (context.horizontalFacing()) {
                case NORTH, SOUTH -> true;
                default -> false;
            };
            data = data.with("shape", northSouth ? "north_south" : "east_west");
        }

        if (type.hasProperty("waterlogged") && context.intoWater()) {
            data = data.with("waterlogged", "true");
        }

        if (type.hasProperty("half") && isTopBottomHalf(type)) {
            data = data.with("half", context.upperHalf() ? "top" : "bottom");
        }

        return data;
    }

    private static boolean facesPlayerDirection(final BlockType type) {
        final boolean stairsLikeShape = type.hasProperty("half")
                && isTopBottomHalf(type)
                && type.hasProperty("shape")
                && !isPlainRailShape(type.property("shape"));
        return !stairsLikeShape;
    }

    private static boolean isPlainRailShape(final @Nullable BlockProperty shape) {
        return shape != null && shape.isValid("north_south") && shape.isValid("east_west");
    }

    private static boolean isTopBottomHalf(final BlockType type) {
        final var half = type.property("half");
        return half != null && half.isValid("top") && half.isValid("bottom");
    }

    private static String faceName(final BlockFace face) {
        return face.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String axisOf(final BlockFace face) {
        return switch (face) {
            case UP, DOWN -> "y";
            case NORTH, SOUTH -> "z";
            case EAST, WEST -> "x";
        };
    }
}
