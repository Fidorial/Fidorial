package fr.euphyllia.fidorial.server.world.structure;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.math.Direction;
import fr.fidorial.world.structure.StructureRotation;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class StateTransforms {

    @SuppressWarnings("unchecked")
    private static final Map<BlockState, BlockState>[] CACHE = new Map[]{
            null, new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>()
    };

    private StateTransforms() {
    }

    public static BlockState rotate(final BlockState state, final StructureRotation rotation) {
        if (rotation == StructureRotation.NONE || state.properties().isEmpty()) {
            return state;
        }
        return CACHE[rotation.quarterTurns()].computeIfAbsent(state, s -> compute(s, rotation));
    }

    private static BlockState compute(final BlockState state, final StructureRotation rotation) {
        final Map<String, String> source = state.properties();
        final Map<String, String> props = new HashMap<>(source);

        final String facing = source.get("facing");
        if (facing != null) {
            final Direction direction = Direction.byName(facing);
            if (direction != null && direction.isHorizontal()) {
                props.put("facing", direction.rotate(rotation).serializedName());
            }
        }

        final String axis = source.get("axis");
        if (axis != null && (rotation.quarterTurns() & 1) == 1) {
            if (axis.equals("x")) {
                props.put("axis", "z");
            } else if (axis.equals("z")) {
                props.put("axis", "x");
            }
        }

        final String rot = source.get("rotation");
        if (rot != null) {
            try {
                props.put("rotation", Integer.toString(rotation.rotate(Integer.parseInt(rot), 16)));
            } catch (final NumberFormatException ignored) {
                // not a 16-step rotation, leave it alone
            }
        }

        final EnumMap<Direction, String> sides = new EnumMap<>(Direction.class);
        for (final Direction side : Direction.HORIZONTAL) {
            final String value = source.get(side.serializedName());
            if (value != null) {
                sides.put(side, value);
            }
        }
        if (!sides.isEmpty()) {
            for (final Map.Entry<Direction, String> entry : sides.entrySet()) {
                final Direction target = entry.getKey().rotate(rotation);
                if (sides.containsKey(target)) {
                    props.put(target.serializedName(), entry.getValue());
                }
            }
        }

        final String shape = source.get("shape");
        if (shape != null && state.name().value().endsWith("rail")) {
            final String rotated = rotateRailShape(shape, rotation);
            if (rotated != null) {
                props.put("shape", rotated);
            }
        }

        final String orientation = source.get("orientation");
        if (orientation != null) {
            final int split = orientation.indexOf('_');
            if (split > 0) {
                final Direction front = Direction.byName(orientation.substring(0, split));
                final Direction top = Direction.byName(orientation.substring(split + 1));
                if (front != null && top != null) {
                    props.put("orientation", front.rotate(rotation).serializedName() + "_" + top.rotate(rotation).serializedName());
                }
            }
        }

        return props.equals(source) ? state : BlockState.of(state.name(), props);
    }

    private static @Nullable String rotateRailShape(final String shape, final StructureRotation rotation) {
        if (shape.startsWith("ascending_")) {
            final Direction direction = Direction.byName(shape.substring("ascending_".length()));
            return direction == null ? null : "ascending_" + direction.rotate(rotation).serializedName();
        }
        final int split = shape.indexOf('_');
        if (split < 0) {
            return null;
        }
        final Direction a = Direction.byName(shape.substring(0, split));
        final Direction b = Direction.byName(shape.substring(split + 1));
        if (a == null || b == null) {
            return null;
        }
        final Direction ra = a.rotate(rotation);
        final Direction rb = b.rotate(rotation);
        if (ra.opposite() == rb) {
            return (ra == Direction.NORTH || ra == Direction.SOUTH) ? "north_south" : "east_west";
        }
        final Direction vertical = (ra == Direction.NORTH || ra == Direction.SOUTH) ? ra : rb;
        final Direction horizontal = vertical == ra ? rb : ra;
        return vertical.serializedName() + "_" + horizontal.serializedName();
    }

    public static BlockState with(final BlockState state, final String property, final String value) {
        if (value.equals(state.properties().get(property))) {
            return state;
        }
        final Map<String, String> props = new HashMap<>(state.properties());
        props.put(property, value);
        return BlockState.of(state.name(), props);
    }
}
