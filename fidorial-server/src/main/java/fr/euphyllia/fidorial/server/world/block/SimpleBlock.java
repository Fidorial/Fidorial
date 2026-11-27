package fr.euphyllia.fidorial.server.world.block;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockProperties.Names;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Bisected;
import fr.fidorial.world.block.data.Orientable;
import fr.fidorial.world.block.property.RailTrack;
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
     * @return the state to place
     */
    public static BlockData placementState(final BlockType type, final BlockPlaceContext context) {
        BlockData data = type.defaultData();

        final BlockProperty.Choice<BlockFace> facingProperty = choice(type, Names.FACING, BlockFace.class);
        if (facingProperty != null) {
            final int faceCount = facingProperty.values().size();
            final BlockFace facing;
            if (faceCount >= 6) {
                facing = context.lookingDirection().opposite();
            } else if (facesPlayerDirection(type)) {
                facing = context.horizontalFacing().opposite();
            } else {
                facing = context.horizontalFacing();
            }
            data = data.with(facingProperty, facing);
        }

        final BlockProperty.Choice<Orientable.Axis> axis = choice(type, Names.AXIS, Orientable.Axis.class);
        if (axis != null) {
            data = data.with(axis, axisOf(context.clickedFace()));
        }

        final BlockProperty.Choice<RailTrack> shape = choice(type, Names.SHAPE, RailTrack.class);
        if (shape != null && isPlainRailShape(shape)) {
            final boolean northSouth = switch (context.horizontalFacing()) {
                case NORTH, SOUTH -> true;
                default -> false;
            };
            data = data.with(shape, northSouth ? RailTrack.NORTH_SOUTH : RailTrack.EAST_WEST);
        }

        if (type.hasProperty(Names.WATERLOGGED) && context.intoWater()) {
            data = data.with(type.flag(Names.WATERLOGGED), true);
        }

        final BlockProperty.Choice<Bisected.Half> half = choice(type, Names.HALF, Bisected.Half.class);
        if (half != null && isTopBottomHalf(half)) {
            data = data.with(half, context.upperHalf() ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
        }

        return data;
    }

    private static boolean facesPlayerDirection(final BlockType type) {
        final BlockProperty.Choice<Bisected.Half> half = choice(type, Names.HALF, Bisected.Half.class);
        final BlockProperty.Choice<RailTrack> rail = choice(type, Names.SHAPE, RailTrack.class);
        final boolean stairsLikeShape = half != null
                && isTopBottomHalf(half)
                && type.hasProperty(Names.SHAPE)
                && (rail == null || !isPlainRailShape(rail));
        return !stairsLikeShape;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> BlockProperty.@Nullable Choice<E> choice(final BlockType type, final String name, final Class<E> values) {
        return type.property(name) instanceof final BlockProperty.Choice<?> choice && choice.type() == values ? (BlockProperty.Choice<E>) choice : null;
    }

    private static boolean isPlainRailShape(final BlockProperty.Choice<RailTrack> shape) {
        return shape.values().contains(RailTrack.NORTH_SOUTH) && shape.values().contains(RailTrack.EAST_WEST);
    }

    private static boolean isTopBottomHalf(final BlockProperty.Choice<Bisected.Half> half) {
        return half.values().contains(Bisected.Half.TOP) && half.values().contains(Bisected.Half.BOTTOM);
    }

    private static Orientable.Axis axisOf(final BlockFace face) {
        return switch (face) {
            case UP, DOWN -> Orientable.Axis.Y;
            case NORTH, SOUTH -> Orientable.Axis.Z;
            case EAST, WEST -> Orientable.Axis.X;
        };
    }
}
