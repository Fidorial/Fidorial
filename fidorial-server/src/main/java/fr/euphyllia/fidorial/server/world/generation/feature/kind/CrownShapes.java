package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Values;

/**
 * The crowns of the trees: rows of leaves stacked over each point the trunk leaves for them, each kind with its
 * own outline.
 */
final class CrownShapes {

    private CrownShapes() {
    }

    /**
     * {@return the difference of two draws below a bound, the first minus the second}: from {@code 1 - bound} to
     * {@code bound - 1}, most often near zero
     */
    private static int spread(final BitRandom random, final int bound) {
        final int first = random.nextInt(bound);
        final int second = random.nextInt(bound);
        return first - second;
    }

    /**
     * A kind of crown.
     */
    abstract static class Shape {

        final IntSource radius;
        final IntSource offset;

        Shape(final JsonObject data) {
            this.radius = IntSource.parse(data.get("radius"));
            this.offset = IntSource.parse(data.get("offset"));
        }

        /**
         * Grows a crown on one point, its offset drawn for it.
         */
        final void create(final Trees.Build build, final int treeHeight, final Trees.Attachment attachment,
                          final int foliageHeight, final int leafRadius) {
            create(build, treeHeight, attachment, foliageHeight, leafRadius, offset.sample(build.random));
        }

        abstract void create(Trees.Build build, int treeHeight, Trees.Attachment attachment, int foliageHeight, int leafRadius, int offset);

        abstract int foliageHeight(BitRandom random, int treeHeight);

        int foliageRadius(final BitRandom random, final int trunkHeight) {
            return radius.sample(random);
        }

        /**
         * {@return {@code true} if a block of a row stays empty}, given its distances from the centre
         */
        abstract boolean skip(BitRandom random, int dx, int y, int dz, int currentRadius, boolean doubleTrunk);

        boolean skipSigned(final BitRandom random, final int dx, final int y, final int dz, final int currentRadius,
                           final boolean doubleTrunk) {
            final int ax;
            final int az;
            if (doubleTrunk) {
                ax = Math.min(Math.abs(dx), Math.abs(dx - 1));
                az = Math.min(Math.abs(dz), Math.abs(dz - 1));
            } else {
                ax = Math.abs(dx);
                az = Math.abs(dz);
            }
            return skip(random, ax, y, az, currentRadius, doubleTrunk);
        }

        /**
         * Lays a square row of leaves around a centre, minus the blocks the outline leaves out.
         */
        void row(final Trees.Build build, final int x, final int y, final int z, final int currentRadius, final int dy,
                 final boolean doubleTrunk) {
            final int extra = doubleTrunk ? 1 : 0;
            for (int dx = -currentRadius; dx <= currentRadius + extra; dx++) {
                for (int dz = -currentRadius; dz <= currentRadius + extra; dz++) {
                    if (!skipSigned(build.random, dx, dy, dz, currentRadius, doubleTrunk)) {
                        build.tryLeaf(x + dx, y + dy, z + dz);
                    }
                }
            }
        }

        /**
         * Lays a row, then lets leaves hang under its rim, sometimes two blocks down.
         */
        final void rowWithHangingLeaves(final Trees.Build build, final int x, final int y, final int z, final int currentRadius,
                                        final int dy, final boolean doubleTrunk, final float hangingChance,
                                        final float extensionChance) {
            row(build, x, y, z, currentRadius, dy, doubleTrunk);
            final int extra = doubleTrunk ? 1 : 0;
            final int logY = y - 1;
            for (final int along : Facing.HORIZONTAL) {
                final int toEdge = Facing.clockwise(along);
                final boolean positive = Facing.DX[toEdge] + Facing.DZ[toEdge] > 0;
                final int edgeDistance = positive ? currentRadius + extra : currentRadius;
                int px = x + Facing.DX[toEdge] * edgeDistance - Facing.DX[along] * currentRadius;
                final int py = y + dy - 1;
                int pz = z + Facing.DZ[toEdge] * edgeDistance - Facing.DZ[along] * currentRadius;
                for (int step = -currentRadius; step < currentRadius + extra; step++) {
                    final boolean leavesAbove = build.hasLeaf(px, py + 1, pz);
                    if (leavesAbove && hang(build, hangingChance, x, logY, z, px, py, pz)) {
                        hang(build, extensionChance, x, logY, z, px, py - 1, pz);
                    }
                    px += Facing.DX[along];
                    pz += Facing.DZ[along];
                }
            }
        }

        private static boolean hang(final Trees.Build build, final float chance, final int logX, final int logY, final int logZ,
                                    final int x, final int y, final int z) {
            if (Math.abs(x - logX) + Math.abs(y - logY) + Math.abs(z - logZ) >= 7) {
                return false;
            }
            return !(build.random.nextFloat() > chance) && build.tryLeaf(x, y, z);
        }
    }

    static Shape parse(final JsonObject data) {
        return switch (Values.type(data)) {
            case "blob_foliage_placer" -> new Blob(data);
            case "bush_foliage_placer" -> new Bush(data);
            case "fancy_foliage_placer" -> new Fancy(data);
            case "acacia_foliage_placer" -> new Acacia(data);
            case "cherry_foliage_placer" -> new Cherry(data);
            case "dark_oak_foliage_placer" -> new DarkOak(data);
            case "jungle_foliage_placer" -> new Jungle(data);
            case "mega_pine_foliage_placer" -> new MegaPine(data);
            case "pine_foliage_placer" -> new Pine(data);
            case "poplar_foliage_placer" -> new Poplar(data);
            case "random_spread_foliage_placer" -> new RandomSpread(data);
            case "spruce_foliage_placer" -> new Spruce(data);
            default -> throw new IllegalArgumentException("Unknown foliage placer " + Values.type(data));
        };
    }

    private static class Blob extends Shape {

        final int height;

        Blob(final JsonObject data) {
            super(data);
            this.height = data.get("height").getAsInt();
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final int total = foliageHeight + a.heightOffset();
            for (int dy = offset; dy >= offset - total; dy--) {
                final int r = Math.max(leafRadius + a.radiusOffset() - 1 - dy / 2, 0);
                row(build, a.x(), a.y(), a.z(), r, dy, a.doubleTrunk());
            }
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return height;
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return dx == r && dz == r && (random.nextInt(2) == 0 || y == 0);
        }
    }

    private static final class Bush extends Blob {

        Bush(final JsonObject data) {
            super(data);
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final int total = foliageHeight + a.heightOffset();
            for (int dy = offset; dy >= offset - total; dy--) {
                final int r = leafRadius + a.radiusOffset() - 1 - dy;
                row(build, a.x(), a.y(), a.z(), r, dy, a.doubleTrunk());
            }
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return dx == r && dz == r && random.nextInt(2) == 0;
        }
    }

    private static final class Fancy extends Blob {

        Fancy(final JsonObject data) {
            super(data);
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            for (int dy = offset; dy >= offset - foliageHeight; dy--) {
                final int r = leafRadius + (dy != offset && dy != offset - foliageHeight ? 1 : 0);
                row(build, a.x(), a.y(), a.z(), r, dy, a.doubleTrunk());
            }
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            final float fx = dx + 0.5F;
            final float fz = dz + 0.5F;
            return fx * fx + fz * fz > r * r;
        }
    }

    private static final class Acacia extends Shape {

        Acacia(final JsonObject data) {
            super(data);
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final boolean doubleTrunk = a.doubleTrunk();
            final int y = a.y() + offset;
            final int total = foliageHeight + a.heightOffset();
            row(build, a.x(), y, a.z(), leafRadius + a.radiusOffset(), -1 - total, doubleTrunk);
            row(build, a.x(), y, a.z(), leafRadius - 1, -total, doubleTrunk);
            row(build, a.x(), y, a.z(), leafRadius + a.radiusOffset() - 1, 0, doubleTrunk);
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return 0;
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return y == 0 ? (dx > 1 || dz > 1) && dx != 0 && dz != 0 : dx == r && dz == r && r > 0;
        }
    }

    private static final class Cherry extends Shape {

        private final IntSource height;
        private final float wideBottomHoleChance;
        private final float cornerHoleChance;
        private final float hangingChance;
        private final float extensionChance;

        Cherry(final JsonObject data) {
            super(data);
            this.height = IntSource.parse(data.get("height"));
            this.wideBottomHoleChance = data.get("wide_bottom_layer_hole_chance").getAsFloat();
            this.cornerHoleChance = data.get("corner_hole_chance").getAsFloat();
            this.hangingChance = data.get("hanging_leaves_chance").getAsFloat();
            this.extensionChance = data.get("hanging_leaves_extension_chance").getAsFloat();
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final boolean doubleTrunk = a.doubleTrunk();
            final int y = a.y() + offset;
            final int r = leafRadius + a.radiusOffset() - 1;
            final int total = foliageHeight + a.heightOffset();
            row(build, a.x(), y, a.z(), r - 2, total - 3, doubleTrunk);
            row(build, a.x(), y, a.z(), r - 1, total - 4, doubleTrunk);
            for (int dy = total - 5; dy >= 0; dy--) {
                row(build, a.x(), y, a.z(), r, dy, doubleTrunk);
            }
            rowWithHangingLeaves(build, a.x(), y, a.z(), r, -1, doubleTrunk, hangingChance, extensionChance);
            rowWithHangingLeaves(build, a.x(), y, a.z(), r - 1, -2, doubleTrunk, hangingChance, extensionChance);
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return height.sample(random);
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            if (y == -1 && (dx == r || dz == r) && random.nextFloat() < wideBottomHoleChance) {
                return true;
            }
            final boolean corner = dx == r && dz == r;
            if (r > 2) {
                return corner || dx + dz > r * 2 - 2 && random.nextFloat() < cornerHoleChance;
            }
            return corner && random.nextFloat() < cornerHoleChance;
        }
    }

    private static final class DarkOak extends Shape {

        DarkOak(final JsonObject data) {
            super(data);
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final int y = a.y() + offset;
            final boolean doubleTrunk = a.doubleTrunk();
            if (doubleTrunk) {
                row(build, a.x(), y, a.z(), leafRadius + 2, -1, true);
                row(build, a.x(), y, a.z(), leafRadius + 3, 0, true);
                row(build, a.x(), y, a.z(), leafRadius + 2, 1, true);
                if (build.random.nextBoolean()) {
                    row(build, a.x(), y, a.z(), leafRadius, 2, true);
                }
            } else {
                row(build, a.x(), y, a.z(), leafRadius + 2, -1, false);
                row(build, a.x(), y, a.z(), leafRadius + 1, 0, false);
            }
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return 4;
        }

        @Override
        boolean skipSigned(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            final boolean outerCorner = y == 0 && doubleTrunk && (dx == -r || dx >= r) && (dz == -r || dz >= r);
            return outerCorner || super.skipSigned(random, dx, y, dz, r, doubleTrunk);
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            if (y == -1 && !doubleTrunk) {
                return dx == r && dz == r;
            }
            return y == 1 && dx + dz > r * 2 - 2;
        }
    }

    private static final class Jungle extends Shape {

        private final int height;

        Jungle(final JsonObject data) {
            super(data);
            this.height = data.get("height").getAsInt();
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final int leafHeight = (a.doubleTrunk() ? foliageHeight : 1 + build.random.nextInt(2)) + a.heightOffset();
            for (int dy = offset; dy >= offset - leafHeight; dy--) {
                final int r = leafRadius + a.radiusOffset() + 1 - dy;
                row(build, a.x(), a.y(), a.z(), r, dy, a.doubleTrunk());
            }
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return height;
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return dx + dz >= 7 || dx * dx + dz * dz > r * r;
        }
    }

    private static final class MegaPine extends Shape {

        private final IntSource crownHeight;

        MegaPine(final JsonObject data) {
            super(data);
            this.crownHeight = IntSource.parse(data.get("crown_height"));
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            int previous = 0;
            final int total = foliageHeight + a.heightOffset();
            for (int y = a.y() - total + offset; y <= a.y() + offset; y++) {
                final int dy = a.y() - y;
                final int smooth = leafRadius + a.radiusOffset() + (int) Math.floor((float) dy / total * 3.5F);
                final int jagged = dy > 0 && smooth == previous && (y & 1) == 0 ? smooth + 1 : smooth;
                row(build, a.x(), y, a.z(), jagged, 0, a.doubleTrunk());
                previous = smooth;
            }
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return crownHeight.sample(random);
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return dx + dz >= 7 || dx * dx + dz * dz > r * r;
        }
    }

    private static final class Pine extends Shape {

        private final IntSource height;

        Pine(final JsonObject data) {
            super(data);
            this.height = IntSource.parse(data.get("height"));
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            int r = 0;
            final int total = foliageHeight + a.heightOffset();
            for (int dy = offset; dy >= offset - total; dy--) {
                row(build, a.x(), a.y(), a.z(), r, dy, a.doubleTrunk());
                if (r >= 1 && dy == offset - total + 1) {
                    r--;
                } else if (r < leafRadius + a.radiusOffset()) {
                    r++;
                }
            }
        }

        @Override
        int foliageRadius(final BitRandom random, final int trunkHeight) {
            return super.foliageRadius(random, trunkHeight) + random.nextInt(Math.max(trunkHeight + 1, 1));
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return height.sample(random);
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return dx == r && dz == r && r > 0;
        }
    }

    private static final class Spruce extends Shape {

        private final IntSource trunkHeight;

        Spruce(final JsonObject data) {
            super(data);
            this.trunkHeight = IntSource.parse(data.get("trunk_height"));
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            int r = build.random.nextInt(2);
            int max = 1;
            int min = 0;
            final int total = foliageHeight + a.heightOffset();
            for (int dy = offset; dy >= -total; dy--) {
                row(build, a.x(), a.y(), a.z(), r, dy, a.doubleTrunk());
                if (r >= max) {
                    r = min;
                    min = 1;
                    max = Math.min(max + 1, leafRadius + a.radiusOffset());
                } else {
                    r++;
                }
            }
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return Math.max(4, treeHeight - trunkHeight.sample(random));
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return dx == r && dz == r && r > 0;
        }
    }

    private static final class RandomSpread extends Shape {

        private final IntSource height;
        private final int attempts;

        RandomSpread(final JsonObject data) {
            super(data);
            this.height = IntSource.parse(data.get("foliage_height"));
            this.attempts = data.get("leaf_placement_attempts").getAsInt();
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final BitRandom random = build.random;
            for (int i = 0; i < attempts; i++) {
                final int dx = spread(random, leafRadius);
                final int dy = spread(random, foliageHeight);
                final int dz = spread(random, leafRadius);
                build.tryLeaf(a.x() + dx, a.y() + dy, a.z() + dz);
            }
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return height.sample(random);
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            return false;
        }
    }

    /**
     * A tall narrow crown in the shape of a rhombus, a few of its leaves replaced by sideways logs inside.
     */
    private static final class Poplar extends Shape {

        private final IntSource height;
        private final float sideHoleChance;

        Poplar(final JsonObject data) {
            super(data);
            this.height = IntSource.parse(data.get("height"));
            this.sideHoleChance = data.get("side_hole_chance").getAsFloat();
        }

        @Override
        void create(final Trees.Build build, final int treeHeight, final Trees.Attachment a, final int foliageHeight,
                    final int leafRadius, final int offset) {
            final boolean doubleTrunk = a.doubleTrunk();
            final int x = a.x();
            final int y = a.y() + offset;
            final int z = a.z();
            final int r = leafRadius + a.radiusOffset() - 1;
            final boolean flip = build.random.nextBoolean();
            final int total = foliageHeight + a.heightOffset();
            rhombusRow(build, x, y, z, r - 2, total - 1, doubleTrunk, total, flip);
            rhombusRow(build, x, y, z, r - 1, total - 2, doubleTrunk, total, flip);
            rhombusRow(build, x, y, z, r - 1, total - 3, doubleTrunk, total, flip);
            for (int dy = total - 4; dy >= 1; dy--) {
                rhombusRow(build, x, y, z, r, dy, doubleTrunk, total, flip);
            }
            innerLogs(build, x, y, z, r, total - 4, doubleTrunk, total, flip);
            rhombusRow(build, x, y, z, r - 1, 0, doubleTrunk, total, flip);
            rhombusRow(build, x, y, z, (int) Math.clamp((long) (r - 2), 1, 2), -1, doubleTrunk, total, flip);
        }

        private void innerLogs(final Trees.Build build, final int x, final int y, final int z, final int r, final int dy,
                               final boolean doubleTrunk, final int total, final boolean flip) {
            final int extra = doubleTrunk ? 1 : 0;
            for (int dx = -r; dx <= r + extra; dx++) {
                for (int dz = -r; dz <= r + extra; dz++) {
                    final int ax = Math.abs(dx);
                    final int az = Math.abs(dz);
                    final int cut = cornerCut(dx, dz, r, partialRow(total, dy), flip);
                    if (inRhombus(r, ax, az, cut, 2) && (az == 0 && r - ax >= 4 || ax == 0 && r - az >= 4)) {
                        final int px = x + dx;
                        final int py = y + dy;
                        final int pz = z + dz;
                        final char expected = build.tree.foliage().pickOrKeep(build.region, build.random, px, py, pz);
                        if (build.region.id(px, py, pz) == expected) {
                            final char log = build.tree.trunk().pickOrKeep(build.region, build.random, px, py, pz);
                            build.setLeaf(px, py, pz, build.states.withIfAllowed(log, "axis", az == 0 ? "x" : "z"));
                        }
                    }
                }
            }
        }

        private void rhombusRow(final Trees.Build build, final int x, final int y, final int z, final int r, final int dy,
                                final boolean doubleTrunk, final int total, final boolean flip) {
            final int extra = doubleTrunk ? 1 : 0;
            for (int dx = -r; dx <= r + extra; dx++) {
                for (int dz = -r; dz <= r + extra; dz++) {
                    if (!skipRhombus(build.random, dx, dy, dz, r, total, flip)) {
                        build.tryLeaf(x + dx, y + dy, z + dz);
                    }
                }
            }
        }

        private boolean skipRhombus(final BitRandom random, final int dx, final int dy, final int dz, final int r,
                                    final int total, final boolean flip) {
            final boolean partial = partialRow(total, dy);
            final int cut = cornerCut(dx, dz, r, partial, flip);
            final int ax = Math.abs(dx);
            final int az = Math.abs(dz);
            if (partial && (ax == r || az == r)) {
                return true;
            }
            final int sideRemoval = random.nextFloat() <= sideHoleChance ? 1 : 0;
            return !inRhombus(r, ax, az, cut, sideRemoval);
        }

        private static int cornerCut(final int dx, final int dz, final int r, final boolean partial, final boolean flip) {
            final boolean smallCorner = flip ? dx > 0 && dz > 0 || dz < 0 && dx < 0 : dx > 0 && dz < 0 || dz > 0 && dx < 0;
            return smallCorner ? r - 1 : partial ? r + 1 : r;
        }

        private static boolean inRhombus(final int r, final int ax, final int az, final int cut, final int removal) {
            return ax + az <= r * 2 - (cut + removal);
        }

        private static boolean partialRow(final int total, final int dy) {
            return total - 1 == dy || total - 2 == dy;
        }

        @Override
        int foliageHeight(final BitRandom random, final int treeHeight) {
            return height.sample(random);
        }

        @Override
        boolean skip(final BitRandom random, final int dx, final int y, final int dz, final int r, final boolean doubleTrunk) {
            throw new IllegalStateException("The rows of this crown need more than the distances");
        }
    }
}
