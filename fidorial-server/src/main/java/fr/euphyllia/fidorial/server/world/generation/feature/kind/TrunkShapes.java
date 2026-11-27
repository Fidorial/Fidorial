package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Trig;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.world.block.data.Orientable;

import java.util.ArrayList;
import java.util.List;

/**
 * The trunks of the trees: straight, leaning, forked, two by two, with branches, bent; each returns the points
 * where crowns grow.
 */
final class TrunkShapes {

    private TrunkShapes() {
    }

    /**
     * A kind of trunk.
     */
    abstract static class Shape {

        private final int baseHeight;
        private final int randomA;
        private final int randomB;

        Shape(final JsonObject data) {
            this.baseHeight = data.get("base_height").getAsInt();
            this.randomA = data.get("height_rand_a").getAsInt();
            this.randomB = data.get("height_rand_b").getAsInt();
        }

        int height(final BitRandom random) {
            return baseHeight + random.nextInt(randomA + 1) + random.nextInt(randomB + 1);
        }

        abstract List<Trees.Attachment> place(Trees.Build build, int treeHeight, int x, int y, int z);

        boolean validPos(final Trees.Build build, final int x, final int y, final int z) {
            return build.validTreePos(x, y, z);
        }

        boolean isFree(final Trees.Build build, final int x, final int y, final int z) {
            return validPos(build, x, y, z) || build.isLog(x, y, z);
        }

        void placeLogIfFree(final Trees.Build build, final int x, final int y, final int z) {
            if (isFree(build, x, y, z)) {
                build.placeLog(x, y, z);
            }
        }
    }

    static Shape parse(final FeatureLibrary library, final JsonObject data) {
        return switch (Values.type(data)) {
            case "straight_trunk_placer" -> new Straight(data);
            case "forking_trunk_placer" -> new Forking(data);
            case "giant_trunk_placer" -> new Giant(data);
            case "mega_jungle_trunk_placer" -> new MegaJungle(data);
            case "dark_oak_trunk_placer" -> new DarkOak(data);
            case "fancy_trunk_placer" -> new Fancy(data);
            case "bending_trunk_placer" -> new Bending(data);
            case "upwards_branching_trunk_placer" -> new UpwardsBranching(library, data);
            case "cherry_trunk_placer" -> new Cherry(data);
            case "poplar_trunk_placer" -> new Poplar(data);
            default -> {
                library.unsupported("trunk placer", Values.type(data));
                yield new Straight(data);
            }
        };
    }

    private static final class Straight extends Shape {

        private final IntSource width;

        Straight(final JsonObject data) {
            super(data);
            this.width = data.has("trunk_width") ? IntSource.parse(data.get("trunk_width")) : IntSource.constant(1);
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final int trunkWidth = width.sample(build.random);
            final int toNorthWest = (trunkWidth - 1) / 2;
            final int toSouthEast = trunkWidth / 2;
            final int minX = x - toNorthWest;
            final int minZ = z - toNorthWest;
            final int maxX = x + toSouthEast;
            final int maxZ = z + toSouthEast;
            for (int pz = minZ; pz <= maxZ; pz++) {
                for (int px = minX; px <= maxX; px++) {
                    build.placeBelowTrunk(px, y - 1, pz);
                }
            }
            for (int dy = 0; dy < treeHeight; dy++) {
                for (int pz = minZ; pz <= maxZ; pz++) {
                    for (int px = minX; px <= maxX; px++) {
                        build.placeLog(px, y + dy, pz);
                    }
                }
            }
            return List.of(new Trees.Attachment(minX, y + treeHeight, minZ, 0, 0, trunkWidth, trunkWidth));
        }
    }

    private static final class Forking extends Shape {

        Forking(final JsonObject data) {
            super(data);
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            build.placeBelowTrunk(x, y - 1, z);
            final List<Trees.Attachment> attachments = new ArrayList<>();
            final int lean = Facing.randomHorizontal(random);
            final int leanHeight = treeHeight - random.nextInt(4) - 1;
            int leanSteps = 3 - random.nextInt(3);
            int tx = x;
            int tz = z;
            int top = Integer.MIN_VALUE;
            for (int dy = 0; dy < treeHeight; dy++) {
                final int py = y + dy;
                if (dy >= leanHeight && leanSteps > 0) {
                    tx += Facing.DX[lean];
                    tz += Facing.DZ[lean];
                    leanSteps--;
                }
                if (build.placeLog(tx, py, tz)) {
                    top = py + 1;
                }
            }
            if (top != Integer.MIN_VALUE) {
                attachments.add(Trees.Attachment.of(tx, top, tz, 1, false));
            }
            tx = x;
            tz = z;
            final int branch = Facing.randomHorizontal(random);
            if (branch != lean) {
                final int branchStart = leanHeight - random.nextInt(2) - 1;
                int branchSteps = 1 + random.nextInt(3);
                top = Integer.MIN_VALUE;
                for (int dy = branchStart; dy < treeHeight && branchSteps > 0; branchSteps--) {
                    if (dy >= 1) {
                        final int py = y + dy;
                        tx += Facing.DX[branch];
                        tz += Facing.DZ[branch];
                        if (build.placeLog(tx, py, tz)) {
                            top = py + 1;
                        }
                    }
                    dy++;
                }
                if (top != Integer.MIN_VALUE) {
                    attachments.add(Trees.Attachment.of(tx, top, tz, 0, false));
                }
            }
            return attachments;
        }
    }

    private static class Giant extends Shape {

        Giant(final JsonObject data) {
            super(data);
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            build.placeBelowTrunk(x, y - 1, z);
            build.placeBelowTrunk(x + 1, y - 1, z);
            build.placeBelowTrunk(x, y - 1, z + 1);
            build.placeBelowTrunk(x + 1, y - 1, z + 1);
            for (int dy = 0; dy < treeHeight; dy++) {
                placeLogIfFree(build, x, y + dy, z);
                if (dy < treeHeight - 1) {
                    placeLogIfFree(build, x + 1, y + dy, z);
                    placeLogIfFree(build, x + 1, y + dy, z + 1);
                    placeLogIfFree(build, x, y + dy, z + 1);
                }
            }
            return new ArrayList<>(List.of(Trees.Attachment.of(x, y + treeHeight, z, 0, true)));
        }
    }

    private static final class MegaJungle extends Giant {

        MegaJungle(final JsonObject data) {
            super(data);
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final List<Trees.Attachment> attachments = super.place(build, treeHeight, x, y, z);
            final BitRandom random = build.random;
            for (int branchHeight = treeHeight - 2 - random.nextInt(4); branchHeight > treeHeight / 2;
                 branchHeight -= 2 + random.nextInt(4)) {
                final float angle = random.nextFloat() * (float) (Math.PI * 2);
                int bx = 0;
                int bz = 0;
                for (int b = 0; b < 5; b++) {
                    bx = (int) (1.5F + Trig.cos(angle) * b);
                    bz = (int) (1.5F + Trig.sin(angle) * b);
                    build.placeLog(x + bx, y + branchHeight - 3 + b / 2, z + bz);
                }
                attachments.add(Trees.Attachment.of(x + bx, y + branchHeight, z + bz, -2, false));
            }
            return attachments;
        }
    }

    private static final class DarkOak extends Shape {

        DarkOak(final JsonObject data) {
            super(data);
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            final List<Trees.Attachment> attachments = new ArrayList<>();
            build.placeBelowTrunk(x, y - 1, z);
            build.placeBelowTrunk(x + 1, y - 1, z);
            build.placeBelowTrunk(x, y - 1, z + 1);
            build.placeBelowTrunk(x + 1, y - 1, z + 1);
            final int lean = Facing.randomHorizontal(random);
            final int leanHeight = treeHeight - random.nextInt(4);
            int leanSteps = 2 - random.nextInt(3);
            int tx = x;
            int tz = z;
            final int top = y + treeHeight - 1;
            for (int dy = 0; dy < treeHeight; dy++) {
                if (dy >= leanHeight && leanSteps > 0) {
                    tx += Facing.DX[lean];
                    tz += Facing.DZ[lean];
                    leanSteps--;
                }
                final int py = y + dy;
                if (build.airOrLeaves(tx, py, tz)) {
                    build.placeLog(tx, py, tz);
                    build.placeLog(tx + 1, py, tz);
                    build.placeLog(tx, py, tz + 1);
                    build.placeLog(tx + 1, py, tz + 1);
                }
            }
            attachments.add(Trees.Attachment.of(tx, top, tz, 0, true));
            for (int ox = -1; ox <= 2; ox++) {
                for (int oz = -1; oz <= 2; oz++) {
                    if ((ox < 0 || ox > 1 || oz < 0 || oz > 1) && random.nextInt(3) <= 0) {
                        final int length = random.nextInt(3) + 2;
                        for (int b = 0; b < length; b++) {
                            build.placeLog(x + ox, top - b - 1, z + oz);
                        }
                        attachments.add(Trees.Attachment.of(x + ox, top, z + oz, 0, false));
                    }
                }
            }
            return attachments;
        }
    }

    /**
     * The large oak: a tall trunk with limbs reaching out to clusters of leaves around it.
     */
    private static final class Fancy extends Shape {

        private record Cluster(int x, int y, int z, int branchBase) {
        }

        Fancy(final JsonObject data) {
            super(data);
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            final int height = treeHeight + 2;
            final int trunkHeight = (int) Math.floor(height * 0.618);
            build.placeBelowTrunk(x, y - 1, z);
            final int clustersPerY = Math.min(1, (int) Math.floor(1.382 + Math.pow(1.0 * height / 13.0, 2.0)));
            final int trunkTop = y + trunkHeight;
            int relativeY = height - 5;
            final List<Cluster> clusters = new ArrayList<>();
            clusters.add(new Cluster(x, y + relativeY, z, trunkTop));
            for (; relativeY >= 0; relativeY--) {
                final float shape = treeShape(height, relativeY);
                if (shape < 0.0F) {
                    continue;
                }
                for (int i = 0; i < clustersPerY; i++) {
                    final double radius = 1.0 * shape * (random.nextFloat() + 0.328);
                    final double angle = random.nextFloat() * 2.0F * Math.PI;
                    final double cx = radius * Math.sin(angle) + 0.5;
                    final double cz = radius * Math.cos(angle) + 0.5;
                    final int startX = x + (int) Math.floor(cx);
                    final int startY = y + relativeY - 1;
                    final int startZ = z + (int) Math.floor(cz);
                    if (limb(build, startX, startY, startZ, startX, startY + 5, startZ, false)) {
                        final int dx = x - startX;
                        final int dz = z - startZ;
                        final double branchHeight = startY - Math.sqrt(dx * dx + dz * dz) * 0.381;
                        final int branchTop = branchHeight > trunkTop ? trunkTop : (int) branchHeight;
                        if (limb(build, x, branchTop, z, startX, startY, startZ, false)) {
                            clusters.add(new Cluster(startX, startY, startZ, branchTop));
                        }
                    }
                }
            }
            limb(build, x, y, z, x, y + trunkHeight, z, true);
            for (final Cluster cluster : clusters) {
                final boolean atTrunk = cluster.x() == x && cluster.y() == cluster.branchBase() && cluster.z() == z;
                if (!atTrunk && keepsBranch(height, cluster.branchBase() - y)) {
                    limb(build, x, cluster.branchBase(), z, cluster.x(), cluster.y(), cluster.z(), true);
                }
            }
            final List<Trees.Attachment> attachments = new ArrayList<>();
            for (final Cluster cluster : clusters) {
                if (keepsBranch(height, cluster.branchBase() - y)) {
                    attachments.add(Trees.Attachment.of(cluster.x(), cluster.y(), cluster.z(), 0, false));
                }
            }
            return attachments;
        }

        /**
         * Walks a straight line between two blocks: checks it is free, or sets logs along it.
         */
        private boolean limb(final Trees.Build build, final int sx, final int sy, final int sz, final int ex, final int ey,
                             final int ez, final boolean place) {
            if (!place && sx == ex && sy == ey && sz == ez) {
                return true;
            }
            final int ddx = ex - sx;
            final int ddy = ey - sy;
            final int ddz = ez - sz;
            final int steps = Math.max(Math.abs(ddx), Math.max(Math.abs(ddy), Math.abs(ddz)));
            final float stepX = (float) ddx / steps;
            final float stepY = (float) ddy / steps;
            final float stepZ = (float) ddz / steps;
            for (int i = 0; i <= steps; i++) {
                final int px = sx + (int) Math.floor(0.5F + i * stepX);
                final int py = sy + (int) Math.floor(0.5F + i * stepY);
                final int pz = sz + (int) Math.floor(0.5F + i * stepZ);
                if (place) {
                    build.placeLog(px, py, pz, build.axis(logAxis(sx, sz, px, pz)));
                } else if (!isFree(build, px, py, pz)) {
                    return false;
                }
            }
            return true;
        }

        private static Orientable.Axis logAxis(final int sx, final int sz, final int px, final int pz) {
            final int xDiff = Math.abs(px - sx);
            final int zDiff = Math.abs(pz - sz);
            final int most = Math.max(xDiff, zDiff);
            if (most > 0) {
                return xDiff == most ? Orientable.Axis.X : Orientable.Axis.Z;
            }
            return Orientable.Axis.Y;
        }

        private static boolean keepsBranch(final int height, final int localY) {
            return localY >= height * 0.2;
        }

        private static float treeShape(final int height, final int y) {
            if (y < height * 0.3F) {
                return -1.0F;
            }
            final float radius = height / 2.0F;
            final float adjacent = radius - y;
            float distance = (float) Math.sqrt(radius * radius - adjacent * adjacent);
            if (adjacent == 0.0F) {
                distance = radius;
            } else if (Math.abs(adjacent) >= radius) {
                return 0.0F;
            }
            return distance * 0.5F;
        }
    }

    private static final class Bending extends Shape {

        private final int minHeightForLeaves;
        private final IntSource bendLength;

        Bending(final JsonObject data) {
            super(data);
            this.minHeightForLeaves = Trees.integer(data, "min_height_for_leaves", 1);
            this.bendLength = IntSource.parse(data.get("bend_length"));
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            final int direction = Facing.randomHorizontal(random);
            final int logHeight = treeHeight - 1;
            int px = x;
            int py = y;
            int pz = z;
            build.placeBelowTrunk(x, y - 1, z);
            final List<Trees.Attachment> points = new ArrayList<>();
            for (int i = 0; i <= logHeight; i++) {
                if (i + 1 >= logHeight + random.nextInt(2)) {
                    px += Facing.DX[direction];
                    pz += Facing.DZ[direction];
                }
                if (build.validTreePos(px, py, pz)) {
                    build.placeLog(px, py, pz);
                }
                if (i >= minHeightForLeaves) {
                    points.add(Trees.Attachment.of(px, py, pz, 0, false));
                }
                py++;
            }
            final int length = bendLength.sample(random);
            for (int i = 0; i <= length; i++) {
                if (build.validTreePos(px, py, pz)) {
                    build.placeLog(px, py, pz);
                }
                points.add(Trees.Attachment.of(px, py, pz, 0, false));
                px += Facing.DX[direction];
                pz += Facing.DZ[direction];
            }
            return points;
        }
    }

    private static final class UpwardsBranching extends Shape {

        private final IntSource extraSteps;
        private final float branchChance;
        private final IntSource extraLength;
        private final StateFlags.Membership growThrough;

        UpwardsBranching(final FeatureLibrary library, final JsonObject data) {
            super(data);
            this.extraSteps = IntSource.parse(data.get("extra_branch_steps"));
            this.branchChance = data.get("place_branch_per_log_probability").getAsFloat();
            this.extraLength = IntSource.parse(data.get("extra_branch_length"));
            this.growThrough = library.blockSet(data.get("can_grow_through"));
        }

        @Override
        boolean validPos(final Trees.Build build, final int x, final int y, final int z) {
            return super.validPos(build, x, y, z) || growThrough.test(build.region.id(x, y, z));
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            final List<Trees.Attachment> attachments = new ArrayList<>();
            for (int dy = 0; dy < treeHeight; dy++) {
                final int py = y + dy;
                if (build.placeLog(x, py, z) && dy < treeHeight - 1 && random.nextFloat() < branchChance) {
                    final int direction = Facing.randomHorizontal(random);
                    final int length = extraLength.sample(random);
                    final int start = Math.max(0, length - extraLength.sample(random) - 1);
                    final int steps = extraSteps.sample(random);
                    branch(build, treeHeight, attachments, x, py, z, direction, start, steps);
                }
                if (dy == treeHeight - 1) {
                    attachments.add(Trees.Attachment.of(x, py + 1, z, 0, false));
                }
            }
            return attachments;
        }

        private void branch(final Trees.Build build, final int treeHeight, final List<Trees.Attachment> attachments,
                            final int x, final int currentY, final int z, final int direction, final int start, int steps) {
            int topY = currentY + start;
            int bx = x;
            int bz = z;
            for (int index = start; index < treeHeight && steps > 0; index++, steps--) {
                if (index >= 1) {
                    final int py = currentY + index;
                    bx += Facing.DX[direction];
                    bz += Facing.DZ[direction];
                    topY = py;
                    if (build.placeLog(bx, py, bz)) {
                        topY = py + 1;
                    }
                    attachments.add(Trees.Attachment.of(bx, py, bz, 0, false));
                }
            }
            if (topY - currentY > 1) {
                attachments.add(Trees.Attachment.of(bx, topY, bz, 0, false));
                attachments.add(Trees.Attachment.of(bx, topY - 2, bz, 0, false));
            }
        }
    }

    private static final class Cherry extends Shape {

        private final IntSource branchCount;
        private final IntSource branchLength;
        private final int startMin;
        private final int startMax;
        private final IntSource branchEnd;

        Cherry(final JsonObject data) {
            super(data);
            this.branchCount = IntSource.parse(data.get("branch_count"));
            this.branchLength = IntSource.parse(data.get("branch_horizontal_length"));
            final IntSource start = IntSource.parse(data.get("branch_start_offset_from_top"));
            this.startMin = start.min();
            this.startMax = start.max();
            this.branchEnd = IntSource.parse(data.get("branch_end_offset_from_top"));
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            build.placeBelowTrunk(x, y - 1, z);
            final int first = Math.max(0, treeHeight - 1 + random.nextIntBetweenInclusive(startMin, startMax));
            int second = Math.max(0, treeHeight - 1 + random.nextIntBetweenInclusive(startMin, startMax - 1));
            if (second >= first) {
                second++;
            }
            final int count = branchCount.sample(random);
            final boolean middle = count == 3;
            final boolean bothSides = count >= 2;
            final int trunkHeight;
            if (middle) {
                trunkHeight = treeHeight;
            } else if (bothSides) {
                trunkHeight = Math.max(first, second) + 1;
            } else {
                trunkHeight = first + 1;
            }
            for (int dy = 0; dy < trunkHeight; dy++) {
                build.placeLog(x, y + dy, z);
            }
            final List<Trees.Attachment> attachments = new ArrayList<>();
            if (middle) {
                attachments.add(Trees.Attachment.of(x, y + trunkHeight, z, 0, false));
            }
            final int direction = Facing.randomHorizontal(random);
            final Orientable.Axis axis = Facing.axis(direction);
            attachments.add(branch(build, treeHeight, x, y, z, axis, direction, first, first < trunkHeight - 1));
            if (bothSides) {
                attachments.add(branch(build, treeHeight, x, y, z, axis, Facing.OPPOSITE[direction], second, second < trunkHeight - 1));
            }
            return attachments;
        }

        private Trees.Attachment branch(final Trees.Build build, final int treeHeight, final int x, final int y, final int z,
                                        final Orientable.Axis axis, final int direction, final int offset, final boolean middleContinues) {
            final BitRandom random = build.random;
            int px = x;
            int py = y + offset;
            int pz = z;
            final int endOffset = treeHeight - 1 + branchEnd.sample(random);
            final boolean extend = middleContinues || endOffset < offset;
            final int distance = branchLength.sample(random) + (extend ? 1 : 0);
            final int endX = x + Facing.DX[direction] * distance;
            final int endY = y + endOffset;
            final int endZ = z + Facing.DZ[direction] * distance;
            final int sideways = extend ? 2 : 1;
            for (int i = 0; i < sideways; i++) {
                px += Facing.DX[direction];
                pz += Facing.DZ[direction];
                build.placeLog(px, py, pz, build.axis(axis));
            }
            final int vertical = endY > py ? 1 : -1;
            while (true) {
                final int remaining = Math.abs(endX - px) + Math.abs(endY - py) + Math.abs(endZ - pz);
                if (remaining == 0) {
                    return Trees.Attachment.of(endX, endY + 1, endZ, 0, false);
                }
                final float chance = (float) Math.abs(endY - py) / remaining;
                final boolean up = random.nextFloat() < chance;
                if (up) {
                    py += vertical;
                    build.placeLog(px, py, pz);
                } else {
                    px += Facing.DX[direction];
                    pz += Facing.DZ[direction];
                    build.placeLog(px, py, pz, build.axis(axis));
                }
            }
        }
    }

    private static final class Poplar extends Shape {

        private final IntSource aboveBranches;
        private final IntSource branchAmount;

        Poplar(final JsonObject data) {
            super(data);
            this.aboveBranches = IntSource.parse(data.get("trunk_height_above_branches"));
            this.branchAmount = IntSource.parse(data.get("branch_amount"));
        }

        @Override
        List<Trees.Attachment> place(final Trees.Build build, final int treeHeight, final int x, final int y, final int z) {
            final BitRandom random = build.random;
            build.placeBelowTrunk(x, y - 1, z);
            final int upToBranches = treeHeight - aboveBranches.sample(random);
            for (int dy = 0; dy < treeHeight; dy++) {
                build.placeLog(x, y + dy, z);
                final int[] shuffled = Facing.shuffled(Facing.ALL, random);
                if (upToBranches - 1 == dy) {
                    final int branches = branchAmount.sample(random);
                    int placed = 0;
                    for (final int direction : shuffled) {
                        if (!Facing.isHorizontal(direction)) {
                            continue;
                        }
                        if (placed++ >= branches) {
                            break;
                        }
                        build.placeLog(x + Facing.DX[direction], y + dy, z + Facing.DZ[direction], build.axis(Facing.axis(direction)));
                    }
                }
            }
            return List.of(Trees.Attachment.of(x, y + upToBranches, z, 0, false));
        }
    }
}
