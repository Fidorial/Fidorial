package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.PlacedFeature;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.fidorial.registry.keys.BlockTypeKeys;

import java.util.Map;

/**
 * The features of the seas: icebergs, and the coral trees and claws grown from a coral block feature.
 */
final class WaterKinds {

    private WaterKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("iceberg", (library, data) -> new Iceberg(library.state(data.get("state"))));
        parsers.put("coral_tree", WaterKinds::coralTree);
        parsers.put("coral_claw", WaterKinds::coralClaw);
    }

    private static Feature coralTree(final FeatureLibrary library, final JsonObject data) {
        final PlacedFeature block = library.placedFeature(data.get("feature"));
        return (region, random, x, y, z) -> {
            int px = x;
            int py = y;
            int pz = z;
            final int trunk = random.nextInt(3) + 1;
            for (int i = 0; i < trunk; i++) {
                if (!block.place(region, random, px, py, pz, false)) {
                    return true;
                }
                py++;
            }
            final int topY = py;
            final int branches = random.nextInt(3) + 2;
            final int[] directions = Facing.shuffled(Facing.HORIZONTAL, random);
            for (int b = 0; b < branches; b++) {
                final int direction = directions[b];
                px = x + Facing.DX[direction];
                py = topY;
                pz = z + Facing.DZ[direction];
                final int height = random.nextInt(5) + 2;
                int segment = 0;
                for (int j = 0; j < height && block.place(region, random, px, py, pz, false); j++) {
                    segment++;
                    py++;
                    if (j == 0 || segment >= 2 && random.nextFloat() < 0.25F) {
                        px += Facing.DX[direction];
                        pz += Facing.DZ[direction];
                        segment = 0;
                    }
                }
            }
            return true;
        };
    }

    private static Feature coralClaw(final FeatureLibrary library, final JsonObject data) {
        final PlacedFeature block = library.placedFeature(data.get("feature"));
        return (region, random, x, y, z) -> {
            if (!block.place(region, random, x, y, z, false)) {
                return false;
            }
            final int claw = Facing.randomHorizontal(random);
            final int branches = random.nextInt(2) + 2;
            final int[] choices = Facing.shuffled(new int[] {claw, Facing.clockwise(claw), Facing.counterClockwise(claw)}, random);
            for (int b = 0; b < branches; b++) {
                final int branch = choices[b];
                int px = x + Facing.DX[branch];
                int py = y;
                int pz = z + Facing.DZ[branch];
                final int sideways = random.nextInt(2) + 1;
                final int segment;
                final int inwards;
                if (branch == claw) {
                    segment = claw;
                    inwards = random.nextInt(3) + 2;
                } else {
                    py++;
                    segment = random.nextInt(2) == 0 ? branch : Facing.UP;
                    inwards = random.nextInt(3) + 3;
                }
                for (int i = 0; i < sideways && block.place(region, random, px, py, pz, false); i++) {
                    px += Facing.DX[segment];
                    py += Facing.DY[segment];
                    pz += Facing.DZ[segment];
                }
                px -= Facing.DX[segment];
                py -= Facing.DY[segment];
                pz -= Facing.DZ[segment];
                py++;
                for (int i = 0; i < inwards; i++) {
                    px += Facing.DX[claw];
                    pz += Facing.DZ[claw];
                    if (!block.place(region, random, px, py, pz, false)) {
                        break;
                    }
                    if (random.nextFloat() < 0.25F) {
                        py++;
                    }
                }
            }
            return true;
        };
    }

    /**
     * A floating mass of ice: round and sometimes very tall, or elliptic and low, with a part under the sea,
     * sometimes topped with snow and sometimes cut through by a channel.
     */
    private record Iceberg(char main) implements Feature {

        @Override
        public boolean place(final DecorationRegion region, final BitRandom random, final int originX, final int originY, final int originZ) {
            final int oy = region.heights().seaLevel();
            final boolean snowOnTop = random.nextDouble() > 0.7;
            final double shapeAngle = random.nextDouble() * 2.0 * Math.PI;
            final int ellipseA = 11 - random.nextInt(5);
            final int ellipseC = 3 + random.nextInt(3);
            final boolean ellipse = random.nextDouble() > 0.7;
            int overWater = ellipse ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
            if (!ellipse && random.nextDouble() > 0.9) {
                overWater += random.nextInt(19) + 7;
            }
            final int underWater = Math.min(overWater + random.nextInt(11), 18);
            final int width = Math.min(overWater + random.nextInt(7) - random.nextInt(5), 11);
            final int a = ellipse ? ellipseA : 11;
            for (int dx = -a; dx < a; dx++) {
                for (int dz = -a; dz < a; dz++) {
                    for (int dy = 0; dy < overWater; dy++) {
                        final int radius = ellipse ? radiusEllipse(dy, overWater, width) : radiusRound(random, dy, overWater, width);
                        if (ellipse || dx < radius) {
                            block(region, random, originX, oy, originZ, overWater, dx, dy, dz, radius, a, ellipse, ellipseC, shapeAngle, snowOnTop);
                        }
                    }
                }
            }
            smooth(region, originX, oy, originZ, width, overWater, ellipse, ellipseA);
            for (int dx = -a; dx < a; dx++) {
                for (int dz = -a; dz < a; dz++) {
                    for (int dy = -1; dy > -underWater; dy--) {
                        final int reach = ellipse ? (int) Math.ceil(a * (1.0F - (float) Math.pow(dy, 2.0) / (underWater * 8.0F))) : a;
                        final int radius = radiusSteep(random, -dy, underWater, width);
                        if (dx < radius) {
                            block(region, random, originX, oy, originZ, underWater, dx, dy, dz, radius, reach, ellipse, ellipseC, shapeAngle, snowOnTop);
                        }
                    }
                }
            }
            final boolean cut = ellipse ? random.nextDouble() > 0.1 : random.nextDouble() > 0.7;
            if (cut) {
                cutOut(region, random, width, overWater, originX, oy, originZ, ellipse, ellipseA, shapeAngle, ellipseC);
            }
            return true;
        }

        private void cutOut(final DecorationRegion region, final BitRandom random, final int width, final int height,
                            final int ox, final int oy, final int oz, final boolean ellipse, final int ellipseA,
                            final double shapeAngle, final int ellipseC) {
            final int signX = random.nextBoolean() ? -1 : 1;
            final int signZ = random.nextBoolean() ? -1 : 1;
            int offsetX = random.nextInt(Math.max(width / 2 - 2, 1));
            if (random.nextBoolean()) {
                offsetX = width / 2 + 1 - random.nextInt(Math.max(width - width / 2 - 1, 1));
            }
            int offsetZ = random.nextInt(Math.max(width / 2 - 2, 1));
            if (random.nextBoolean()) {
                offsetZ = width / 2 + 1 - random.nextInt(Math.max(width - width / 2 - 1, 1));
            }
            if (ellipse) {
                offsetX = random.nextInt(Math.max(ellipseA - 5, 1));
                offsetZ = offsetX;
            }
            final int localX = signX * offsetX;
            final int localZ = signZ * offsetZ;
            final double angle = ellipse ? shapeAngle + Math.PI / 2 : random.nextDouble() * 2.0 * Math.PI;
            for (int dy = 0; dy < height - 3; dy++) {
                final int radius = radiusRound(random, dy, height, width);
                carve(region, radius, dy, ox, oy, oz, false, angle, localX, localZ, ellipseA, ellipseC);
            }
            for (int dy = -1; dy > -height + random.nextInt(5); dy--) {
                final int radius = radiusSteep(random, -dy, height, width);
                carve(region, radius, dy, ox, oy, oz, true, angle, localX, localZ, ellipseA, ellipseC);
            }
        }

        private void carve(final DecorationRegion region, final int radius, final int dy, final int ox, final int oy, final int oz,
                           final boolean underWater, final double angle, final int localX, final int localZ,
                           final int ellipseA, final int ellipseC) {
            final int a = radius + 1 + ellipseA / 3;
            final int c = Math.min(radius - 3, 3) + ellipseC / 2 - 1;
            final StateFlags states = region.states();
            for (int dx = -a; dx < a; dx++) {
                for (int dz = -a; dz < a; dz++) {
                    if (ellipseDistance(dx, dz, localX, localZ, a, c, angle) >= 0.0) {
                        continue;
                    }
                    final int px = ox + dx;
                    final int py = oy + dy;
                    final int pz = oz + dz;
                    final char state = region.id(px, py, pz);
                    if (isIceberg(states, state) || states.is(state, BlockTypeKeys.SNOW_BLOCK)) {
                        if (underWater) {
                            region.set(px, py, pz, region.blocks().water);
                        } else {
                            region.set(px, py, pz, region.blocks().air);
                            if (states.is(region.id(px, py + 1, pz), BlockTypeKeys.SNOW)) {
                                region.set(px, py + 1, pz, region.blocks().air);
                            }
                        }
                    }
                }
            }
        }

        private void block(final DecorationRegion region, final BitRandom random, final int ox, final int oy, final int oz,
                           final int height, final int dx, final int dy, final int dz, final int radius, final int a,
                           final boolean ellipse, final int ellipseC, final double shapeAngle, final boolean snowOnTop) {
            final double distance = ellipse
                    ? ellipseDistance(dx, dz, 0, 0, a, ellipseC(dy, height, ellipseC), shapeAngle)
                    : circleDistance(dx, dz, radius, random);
            if (distance >= 0.0) {
                return;
            }
            final double limit = ellipse ? -0.5 : -6 - random.nextInt(3);
            if (distance > limit && random.nextDouble() > 0.9) {
                return;
            }
            setIceberg(region, random, ox + dx, oy + dy, oz + dz, height - dy, height, ellipse, snowOnTop);
        }

        private void setIceberg(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                                final int fromTop, final int height, final boolean ellipse, final boolean snowOnTop) {
            final StateFlags states = region.states();
            final char state = region.id(x, y, z);
            final boolean water = states.isWaterBlock(state);
            if (!states.isAir(state) && !states.is(state, BlockTypeKeys.SNOW_BLOCK) && !states.is(state, BlockTypeKeys.ICE) && !water) {
                return;
            }
            final boolean randomness = !ellipse || random.nextDouble() > 0.05;
            final int divisor = ellipse ? 3 : 2;
            if (snowOnTop && !water && fromTop <= random.nextInt(Math.max(1, height / divisor)) + height * 0.6 && randomness) {
                region.set(x, y, z, region.blocks().state(BlockTypeKeys.SNOW_BLOCK));
            } else {
                region.set(x, y, z, main);
            }
        }

        private static int ellipseC(final int dy, final int height, final int ellipseC) {
            return dy > 0 && height - dy <= 3 ? ellipseC - (4 - (height - dy)) : ellipseC;
        }

        private static double circleDistance(final int dx, final int dz, final int radius, final BitRandom random) {
            final float offset = 10.0F * Math.clamp(random.nextFloat(), 0.2F, 0.8F) / radius;
            return offset + Math.pow(dx, 2.0) + Math.pow(dz, 2.0) - Math.pow(radius, 2.0);
        }

        private static double ellipseDistance(final int dx, final int dz, final int originX, final int originZ,
                                              final int a, final int c, final double angle) {
            final double rx = (dx - originX) * Math.cos(angle) - (dz - originZ) * Math.sin(angle);
            final double rz = (dx - originX) * Math.sin(angle) + (dz - originZ) * Math.cos(angle);
            return Math.pow(rx / a, 2.0) + Math.pow(rz / c, 2.0) - 1.0;
        }

        private static int radiusRound(final BitRandom random, final int dy, final int height, final int width) {
            final float k = 3.5F - random.nextFloat();
            float scale = (1.0F - (float) Math.pow(dy, 2.0) / (height * k)) * width;
            if (height > 15 + random.nextInt(5)) {
                final int flattened = dy < 3 + random.nextInt(6) ? dy / 2 : dy;
                scale = (1.0F - flattened / (height * k * 0.4F)) * width;
            }
            return (int) Math.ceil(scale / 2.0F);
        }

        private static int radiusEllipse(final int dy, final int height, final int width) {
            final float scale = (1.0F - (float) Math.pow(dy, 2.0) / (height * 1.0F)) * width;
            return (int) Math.ceil(scale / 2.0F);
        }

        private static int radiusSteep(final BitRandom random, final int dy, final int height, final int width) {
            final float k = 1.0F + random.nextFloat() / 2.0F;
            final float scale = (1.0F - dy / (height * k)) * width;
            return (int) Math.ceil(scale / 2.0F);
        }

        private static boolean isIceberg(final StateFlags states, final char state) {
            return states.is(state, BlockTypeKeys.PACKED_ICE) || states.is(state, BlockTypeKeys.SNOW_BLOCK) || states.is(state, BlockTypeKeys.BLUE_ICE);
        }

        private void smooth(final DecorationRegion region, final int ox, final int oy, final int oz, final int width,
                            final int height, final boolean ellipse, final int ellipseA) {
            final StateFlags states = region.states();
            final int a = ellipse ? ellipseA : width / 2;
            for (int dx = -a; dx <= a; dx++) {
                for (int dz = -a; dz <= a; dz++) {
                    for (int dy = 0; dy <= height; dy++) {
                        final int px = ox + dx;
                        final int py = oy + dy;
                        final int pz = oz + dz;
                        final char state = region.id(px, py, pz);
                        if (!isIceberg(states, state) && !states.is(state, BlockTypeKeys.SNOW)) {
                            continue;
                        }
                        if (region.isAir(px, py - 1, pz)) {
                            region.set(px, py, pz, region.blocks().air);
                            region.set(px, py + 1, pz, region.blocks().air);
                        } else if (isIceberg(states, state)) {
                            int open = 0;
                            final int[][] sides = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                            for (final int[] side : sides) {
                                if (!isIceberg(states, region.id(px + side[0], py, pz + side[1]))) {
                                    open++;
                                }
                            }
                            if (open >= 3) {
                                region.set(px, py, pz, region.blocks().air);
                            }
                        }
                    }
                }
            }
        }
    }
}
