package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.BlockTest;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Cracks opened in a frozen surface: a winding cut, wide in its middle and narrowing toward both ends, flooded up
 * to a level below the rim and capped with a thin crust.
 */
final class CrevasseKinds {

    /**
     * Height step between two columns beyond which the ground is too steep to cut through.
     */
    private static final int MAX_STEP = 1;

    private CrevasseKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("crevasse", CrevasseKinds::crevasse);
    }

    /**
     * A column to cut: its top, the block that was there, and how far below it the cut goes.
     */
    private record Column(int x, int y, int z, char top, int depth) {

        int bottom() {
            return y - depth;
        }

        boolean spans(final int height) {
            return height >= bottom() && height <= y;
        }
    }

    /**
     * The settings of a kind of crevasse.
     */
    private record Shape(int maxWidth, int maxLength, int minFootprint, int cursorSearch, int columnSearch, float twistiness,
                         float tapering, float turnFactor, IntSource carveDepth, IntSource fillDepth, StateSource crust,
                         StateSource fill, BlockTest carvable, BlockTest barrier) {
    }

    private static Feature crevasse(final FeatureLibrary library, final JsonObject data) {
        final Shape shape = new Shape(data.get("max_width").getAsInt(), data.get("max_length").getAsInt(),
                data.get("min_footprint").getAsInt(), data.get("cursor_surface_search_range").getAsInt(),
                data.get("column_surface_search_range").getAsInt(), data.get("twistiness").getAsFloat(),
                data.get("tapering").getAsFloat(), data.get("turn_factor").getAsFloat(),
                IntSource.parse(data.get("carve_depth")), IntSource.parse(data.get("fill_depth")),
                library.source(data.get("surface_block")), library.source(data.get("fill_block")),
                library.test(data.get("carvable_blocks")), library.test(data.get("acceptable_barrier")));
        return (region, random, x, y, z) -> new Cut(region, shape).place(random, x, y, z);
    }

    /**
     * One crevasse being cut.
     */
    private static final class Cut {

        private final DecorationRegion region;
        private final StateFlags states;
        private final Shape shape;
        private final Map<Long, Column> columns = new HashMap<>();

        Cut(final DecorationRegion region, final Shape shape) {
            this.region = region;
            this.states = region.states();
            this.shape = shape;
        }

        private static long key(final int x, final int z) {
            return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
        }

        private @Nullable Column column(final int x, final int z) {
            return columns.get(key(x, z));
        }

        boolean place(final BitRandom random, final int x, final int y, final int z) {
            final int carveDepth = shape.carveDepth().sample(random);
            if (y <= region.minY() + carveDepth) {
                return false;
            }
            final int halfLength = shape.maxLength() / 2;
            final float heading = random.nextFloat() * (float) (Math.PI * 2);
            final int fillDepth = shape.fillDepth().sample(random);
            for (int side = 0; side < 2; side++) {
                trace(random, x, y, z, halfLength, carveDepth, heading + side * (float) Math.PI);
            }
            keepConnected(x, z);
            if (columns.size() < shape.minFootprint()) {
                return false;
            }
            int lowestTop = Integer.MAX_VALUE;
            for (final Column column : columns.values()) {
                lowestTop = Math.min(lowestTop, column.y());
            }
            final int waterLevel = Math.min(y - fillDepth, lowestTop - 1);
            final List<Column> ordered = new ArrayList<>(columns.values());
            ordered.sort(Comparator.comparingInt(Column::x).thenComparingInt(Column::z));
            for (final Column column : ordered) {
                carve(column, waterLevel, random);
            }
            return true;
        }

        /**
         * Finds, going down from a little above a spot, the first carvable block with air over it.
         *
         * @return the spot and its block as {x, y, z, block}, {@code null} when there is none in reach
         */
        private int @Nullable [] surface(final int x, final int y, final int z, final int range) {
            final int top = y + range / 2;
            boolean openAbove = states.isAir(region.id(x, top + 1, z));
            final int lowest = Math.max(region.minY(), top - range);
            for (int height = top; height >= lowest; height--) {
                final char block = region.id(x, height, z);
                if (openAbove && shape.carvable().test(region, x, height, z)) {
                    return new int[] {x, height, z, block};
                }
                openAbove = states.isAir(block);
            }
            return null;
        }

        /**
         * Walks one half of the crevasse from its middle, gathering the columns to cut.
         */
        private void trace(final BitRandom random, final int startX, final int startY, final int startZ, final int length,
                           final int carveDepth, final float startHeading) {
            float heading = startHeading;
            double cursorX = startX;
            double cursorZ = startZ;
            int lastTop = startY;
            float swing = 0.0F;
            for (int step = 0; step < length; step++) {
                cursorX += Math.cos(heading);
                cursorZ += Math.sin(heading);
                final float turn = (float) random.nextGaussian();
                final float strength = random.nextFloat();
                swing = swing * 0.5F + turn * strength * shape.twistiness();
                heading += swing * shape.turnFactor();
                final int blockX = (int) Math.floor(cursorX);
                final int blockZ = (int) Math.floor(cursorZ);
                final int[] found = surface(blockX, lastTop, blockZ, shape.cursorSearch());
                if (found == null) {
                    continue;
                }
                final float narrowing = 1.0F - (float) step / length * shape.tapering();
                final float radius = Math.max(shape.maxWidth() * 0.5F * narrowing, 0.5F);
                final float radiusSquared = radius * radius;
                final int depthLimit = Math.max((int) (carveDepth * narrowing), 1);
                final int reach = (int) Math.ceil(radius);
                for (int x = blockX - reach; x <= blockX + reach; x++) {
                    for (int z = blockZ - reach; z <= blockZ + reach; z++) {
                        final float dx = x - blockX;
                        final float dz = z - blockZ;
                        if (dx * dx + dz * dz > radiusSquared || columns.containsKey(key(x, z))) {
                            continue;
                        }
                        final int[] top = surface(x, found[1], z, shape.columnSearch());
                        if (top == null) {
                            continue;
                        }
                        int depth = 1;
                        int below = top[1] - 1;
                        while (depth < depthLimit && shape.carvable().test(region, x, below, z)) {
                            below--;
                            depth++;
                        }
                        columns.put(key(x, z), new Column(x, top[1], z, (char) top[3], depth - 1));
                    }
                }
                lastTop = found[1];
            }
        }

        private boolean steep(final Column column) {
            for (final int direction : Facing.HORIZONTAL) {
                final Column next = column(column.x() + Facing.DX[direction], column.z() + Facing.DZ[direction]);
                if (next != null && Math.abs(column.y() - next.y()) > MAX_STEP) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Keeps only the columns joined to the middle one through gentle slopes.
         */
        private void keepConnected(final int x, final int z) {
            final Column middle = column(x, z);
            if (middle == null || steep(middle)) {
                columns.clear();
                return;
            }
            final List<Column> kept = new ArrayList<>();
            final Set<Long> seen = new HashSet<>();
            kept.add(middle);
            seen.add(key(x, z));
            for (int index = 0; index < kept.size(); index++) {
                final Column column = kept.get(index);
                for (final int direction : Facing.HORIZONTAL) {
                    final int nx = column.x() + Facing.DX[direction];
                    final int nz = column.z() + Facing.DZ[direction];
                    final Column next = column(nx, nz);
                    if (next != null && !seen.contains(key(nx, nz)) && !steep(next)) {
                        kept.add(next);
                        seen.add(key(nx, nz));
                    }
                }
            }
            columns.clear();
            for (final Column column : kept) {
                columns.put(key(column.x(), column.z()), column);
            }
        }

        private boolean exposesLiquid(final int x, final int y, final int z) {
            for (final int direction : Facing.HORIZONTAL) {
                if (states.hasFluid(region.id(x + Facing.DX[direction], y, z + Facing.DZ[direction]))) {
                    return true;
                }
            }
            return false;
        }

        private void carve(final Column column, final int waterLevel, final BitRandom random) {
            final char air = region.id(region.blocks().air);
            final int bottom = column.bottom();
            boolean carved = false;
            for (int y = column.y(); y >= bottom; y--) {
                final char fill = shape.fill().pick(region, random, column.x(), y, column.z());
                final boolean flood = fill != StateSource.NONE && y <= waterLevel;
                if (!flood && exposesLiquid(column.x(), y, column.z())) {
                    break;
                }
                final char placed = flood ? fill : air;
                closeSides(column, y, placed);
                region.set(column.x(), y, column.z(), placed);
                if (flood && y == bottom) {
                    closeBottom(column, y, placed);
                }
                carved = true;
            }
            if (carved) {
                final char crust = shape.crust().pick(region, random, column.x(), column.y(), column.z());
                if (crust != StateSource.NONE) {
                    region.set(column.x(), column.y(), column.z(), crust);
                }
            }
        }

        /**
         * Walls the cut where it would open onto something that does not hold liquids back.
         */
        private void closeSides(final Column column, final int y, final char placed) {
            for (final int direction : Facing.HORIZONTAL) {
                final int nx = column.x() + Facing.DX[direction];
                final int nz = column.z() + Facing.DZ[direction];
                if (shape.barrier().test(region, nx, y, nz)) {
                    continue;
                }
                final Column next = column(nx, nz);
                if (next != null && next.spans(y)) {
                    continue;
                }
                if (!PlantKinds.sameBlock(states, region.id(nx, y, nz), placed)) {
                    region.set(nx, y, nz, column.top());
                }
            }
        }

        private void closeBottom(final Column column, final int y, final char placed) {
            if (shape.barrier().test(region, column.x(), y - 1, column.z())) {
                return;
            }
            if (!PlantKinds.sameBlock(states, region.id(column.x(), y - 1, column.z()), placed)) {
                region.set(column.x(), y - 1, column.z(), column.top());
            }
        }
    }
}