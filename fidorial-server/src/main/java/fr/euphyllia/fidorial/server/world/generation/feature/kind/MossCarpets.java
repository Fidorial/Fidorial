package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.BlockProperty;
import fr.fidorial.world.block.property.WallHeight;

import java.util.List;

/**
 * Lays a pale moss carpet: a base on the ground that climbs the walls around it, and sometimes a second layer
 * hanging on the walls above, each side kept at random.
 */
final class MossCarpets {

    private static final BlockFace[] SIDES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final List<BlockProperty.Choice<WallHeight>> SIDE_HEIGHTS =
            List.of(BlockProperties.WALL_NORTH, BlockProperties.WALL_EAST, BlockProperties.WALL_SOUTH, BlockProperties.WALL_WEST);

    private MossCarpets() {
    }

    static void place(final DecorationRegion region, final int x, final int y, final int z, final BitRandom random) {
        final StateFlags states = region.states();
        final char plain = region.id(region.blocks().state(BlockTypeKeys.PALE_MOSS_CARPET));
        char base = shape(region, plain, x, y, z, true);
        region.set(x, y, z, base);
        final char topper = topper(region, x, y, z, random);
        if (!states.isAir(topper)) {
            region.set(x, y + 1, z, topper);
            base = shape(region, base, x, y, z, true);
            region.set(x, y, z, base);
        }
    }

    private static boolean wallAt(final DecorationRegion region, final int x, final int y, final int z, final BlockFace side) {
        return region.states().sturdy(region.id(x + side.dx(), y, z + side.dz()), side.opposite());
    }

    /**
     * {@return the carpet with its sides worked out from the walls around}
     */
    private static char shape(final DecorationRegion region, char state, final int x, final int y, final int z, boolean newSides) {
        final StateFlags states = region.states();
        final boolean isBase = Boolean.TRUE.equals(states.get(state, BlockProperties.BOTTOM));
        newSides |= isBase;
        char above = StateSource.NONE;
        char below = StateSource.NONE;
        for (int i = 0; i < SIDES.length; i++) {
            final BlockProperty.Choice<WallHeight> property = SIDE_HEIGHTS.get(i);
            WallHeight side = wallAt(region, x, y, z, SIDES[i]) ? (newSides ? WallHeight.LOW : states.get(state, property)) : WallHeight.NONE;
            if (side == WallHeight.LOW) {
                if (above == StateSource.NONE) {
                    above = region.id(x, y + 1, z);
                }
                if (states.is(above, BlockTypeKeys.PALE_MOSS_CARPET) && states.get(above, property) != WallHeight.NONE
                        && !Boolean.TRUE.equals(states.get(above, BlockProperties.BOTTOM))) {
                    side = WallHeight.TALL;
                }
                if (!isBase) {
                    if (below == StateSource.NONE) {
                        below = region.id(x, y - 1, z);
                    }
                    if (states.is(below, BlockTypeKeys.PALE_MOSS_CARPET) && states.get(below, property) == WallHeight.NONE) {
                        side = WallHeight.NONE;
                    }
                }
            }
            if (side != null) {
                state = states.with(state, property, side);
            }
        }
        return state;
    }

    private static char topper(final DecorationRegion region, final int x, final int y, final int z, final BitRandom random) {
        final StateFlags states = region.states();
        final char previous = region.id(x, y + 1, z);
        final boolean carpetAbove = states.is(previous, BlockTypeKeys.PALE_MOSS_CARPET);
        if (carpetAbove && Boolean.TRUE.equals(states.get(previous, BlockProperties.BOTTOM))
                || !carpetAbove && !states.has(previous, StateFlags.REPLACEABLE)) {
            return region.id(region.blocks().air);
        }
        final char hanging = states.with(region.id(region.blocks().state(BlockTypeKeys.PALE_MOSS_CARPET)), BlockProperties.BOTTOM, false);
        char state = shape(region, hanging, x, y + 1, z, true);
        for (final BlockProperty.Choice<WallHeight> property : SIDE_HEIGHTS) {
            if (states.get(state, property) != WallHeight.NONE && !random.nextBoolean()) {
                state = states.with(state, property, WallHeight.NONE);
            }
        }
        boolean anySide = false;
        for (final BlockProperty.Choice<WallHeight> property : SIDE_HEIGHTS) {
            anySide |= states.get(state, property) != WallHeight.NONE;
        }
        return anySide && state != previous ? state : region.id(region.blocks().air);
    }
}
