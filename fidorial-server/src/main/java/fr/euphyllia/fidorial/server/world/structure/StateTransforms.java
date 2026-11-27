package fr.euphyllia.fidorial.server.world.structure;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.data.Orientable;
import fr.fidorial.world.block.property.Orientation;
import fr.fidorial.world.block.property.RailTrack;
import fr.fidorial.world.structure.StructureRotation;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class StateTransforms {

    private static final List<BlockFace> HORIZONTAL = List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    @SuppressWarnings("unchecked")
    private static final Map<BlockState, BlockState>[] CACHE = new Map[]{
            null, new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>()
    };

    private StateTransforms() {
    }

    public static BlockState rotate(final BlockState state, final StructureRotation rotation) {
        if (rotation == StructureRotation.NONE || state.type().properties().isEmpty()) {
            return state;
        }
        return CACHE[rotation.quarterTurns()].computeIfAbsent(state, s -> compute(s, rotation));
    }

    private static BlockState compute(final BlockState state, final StructureRotation rotation) {
        final BlockData source = state.data();
        BlockData data = source;

        final BlockProperty<BlockFace> facing = state.matching(BlockProperties.FACING);
        if (facing != null && HORIZONTAL.contains(source.get(facing))) {
            data = data.with(facing, rotate(source.get(facing), rotation));
        }

        final BlockProperty<Orientable.Axis> axis = state.matching(BlockProperties.AXIS);
        if (axis != null && (rotation.quarterTurns() & 1) == 1) {
            final Orientable.Axis current = source.get(axis);
            if (current != Orientable.Axis.Y) {
                data = data.with(axis, current == Orientable.Axis.X ? Orientable.Axis.Z : Orientable.Axis.X);
            }
        }

        final BlockProperty<Integer> steps = state.matching(BlockProperties.ROTATION);
        if (steps != null) {
            data = data.with(steps, rotation.rotate(source.get(steps), 16));
        }

        final Map<BlockFace, BlockProperty<?>> sides = new EnumMap<>(BlockFace.class);
        for (final BlockFace side : HORIZONTAL) {
            final BlockProperty<?> property = state.type().property(side.name().toLowerCase(Locale.ROOT));
            if (property != null) {
                sides.put(side, property);
            }
        }
        for (final Map.Entry<BlockFace, BlockProperty<?>> side : sides.entrySet()) {
            final BlockProperty<?> target = sides.get(rotate(side.getKey(), rotation));
            if (target != null) {
                data = copy(data, source, side.getValue(), target);
            }
        }

        final BlockProperty<RailTrack> rail = state.matching(BlockProperties.RAIL_SHAPE);
        if (rail != null && state.isTagged(BlockTypeTagKeys.RAILS)) {
            final RailTrack rotated = rotate(source.get(rail), rotation);
            if (rail.indexOf(rotated) >= 0) {
                data = data.with(rail, rotated);
            }
        }

        final BlockProperty<Orientation> orientation = state.matching(BlockProperties.ORIENTATION);
        if (orientation != null) {
            final String[] faces = source.get(orientation).name().split("_");
            final BlockFace front = rotate(BlockFace.valueOf(faces[0]), rotation);
            final BlockFace top = rotate(BlockFace.valueOf(faces[1]), rotation);
            data = data.with(orientation, Orientation.valueOf(front.name() + "_" + top.name()));
        }

        return data == source ? state : BlockState.of(data);
    }

    @SuppressWarnings("unchecked")
    private static BlockData copy(final BlockData data, final BlockData source, final BlockProperty<?> from, final BlockProperty<?> to) {
        final Object value = source.get(from);
        return ((BlockProperty<Object>) to).indexOf(value) >= 0 ? data.with((BlockProperty<Object>) to, value) : data;
    }

    private static BlockFace rotate(final BlockFace face, final StructureRotation rotation) {
        final int index = HORIZONTAL.indexOf(face);
        return index < 0 ? face : HORIZONTAL.get((index + rotation.quarterTurns()) % 4);
    }

    private static RailTrack rotate(final RailTrack track, final StructureRotation rotation) {
        return switch (track) {
            case NORTH_SOUTH, EAST_WEST -> (rotation.quarterTurns() & 1) == 0 ? track
                    : track == RailTrack.NORTH_SOUTH ? RailTrack.EAST_WEST : RailTrack.NORTH_SOUTH;
            case ASCENDING_NORTH -> ascending(rotate(BlockFace.NORTH, rotation));
            case ASCENDING_EAST -> ascending(rotate(BlockFace.EAST, rotation));
            case ASCENDING_SOUTH -> ascending(rotate(BlockFace.SOUTH, rotation));
            case ASCENDING_WEST -> ascending(rotate(BlockFace.WEST, rotation));
            case NORTH_EAST -> corner(rotate(BlockFace.NORTH, rotation), rotate(BlockFace.EAST, rotation));
            case NORTH_WEST -> corner(rotate(BlockFace.NORTH, rotation), rotate(BlockFace.WEST, rotation));
            case SOUTH_EAST -> corner(rotate(BlockFace.SOUTH, rotation), rotate(BlockFace.EAST, rotation));
            case SOUTH_WEST -> corner(rotate(BlockFace.SOUTH, rotation), rotate(BlockFace.WEST, rotation));
        };
    }

    private static RailTrack ascending(final BlockFace face) {
        return switch (face) {
            case NORTH -> RailTrack.ASCENDING_NORTH;
            case EAST -> RailTrack.ASCENDING_EAST;
            case SOUTH -> RailTrack.ASCENDING_SOUTH;
            default -> RailTrack.ASCENDING_WEST;
        };
    }

    private static RailTrack corner(final BlockFace a, final BlockFace b) {
        final boolean north = a == BlockFace.NORTH || b == BlockFace.NORTH;
        final boolean east = a == BlockFace.EAST || b == BlockFace.EAST;
        if (north) {
            return east ? RailTrack.NORTH_EAST : RailTrack.NORTH_WEST;
        }
        return east ? RailTrack.SOUTH_EAST : RailTrack.SOUTH_WEST;
    }
}
