package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.HeightmapKind;
import fr.euphyllia.fidorial.server.world.generation.feature.TargetTest;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.Trig;

import java.util.Arrays;
import java.util.Map;

/**
 * Ore veins: a string of overlapping balls along a short random segment, replacing the blocks their targets
 * accept; and scattered ores, a handful of single blocks around a point.
 */
final class OreKinds {

    private static final int[][] NEIGHBOURS = {{0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}, {-1, 0, 0}, {1, 0, 0}};

    private OreKinds() {
    }

    /**
     * What an ore replaces and with what.
     */
    private record Target(TargetTest test, char state) {
    }

    /**
     * The settings shared by both kinds of ore.
     */
    private record OreSettings(Target[] targets, int size, float airDiscard) {

        static OreSettings read(final FeatureLibrary library, final JsonObject data) {
            final var list = data.getAsJsonArray("targets");
            final Target[] targets = new Target[list.size()];
            for (int i = 0; i < targets.length; i++) {
                final JsonObject entry = list.get(i).getAsJsonObject();
                targets[i] = new Target(TargetTest.parse(library, entry.get("target")), library.state(entry.get("state")));
            }
            final JsonElement discard = data.get("discard_chance_on_air_exposure");
            return new OreSettings(targets, data.get("size").getAsInt(), discard == null ? 0.0F : discard.getAsFloat());
        }

        /**
         * {@return {@code true} if a block may be replaced, the air around it checked or not as the discard chance
         * decides}
         */
        boolean exposureAllows(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z) {
            final boolean skipCheck;
            if (airDiscard <= 0.0F) {
                skipCheck = true;
            } else if (airDiscard >= 1.0F) {
                skipCheck = false;
            } else {
                skipCheck = random.nextFloat() >= airDiscard;
            }
            return skipCheck || !touchesAir(region, x, y, z);
        }
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("ore", (library, data) -> vein(OreSettings.read(library, data)));
        parsers.put("scattered_ore", (library, data) -> scattered(OreSettings.read(library, data)));
    }

    private static boolean touchesAir(final DecorationRegion region, final int x, final int y, final int z) {
        for (final int[] offset : NEIGHBOURS) {
            if (region.isAir(x + offset[0], y + offset[1], z + offset[2])) {
                return true;
            }
        }
        return false;
    }

    private static Feature vein(final OreSettings settings) {
        return (region, random, x, y, z) -> {
            final int size = settings.size();
            final float angle = random.nextFloat() * (float) Math.PI;
            final float spread = size / 8.0F;
            final int maxRadius = (int) Math.ceil((size / 16.0F * 2.0F + 1.0F) / 2.0F);
            final double x0 = x + Math.sin(angle) * spread;
            final double x1 = x - Math.sin(angle) * spread;
            final double z0 = z + Math.cos(angle) * spread;
            final double z1 = z - Math.cos(angle) * spread;
            final double y0 = y + random.nextInt(3) - 2;
            final double y1 = y + random.nextInt(3) - 2;
            final int spreadCeil = (int) Math.ceil(spread);
            final int xStart = x - spreadCeil - maxRadius;
            final int yStart = y - 2 - maxRadius;
            final int zStart = z - spreadCeil - maxRadius;
            final int width = 2 * (spreadCeil + maxRadius);
            boolean belowSurface = false;
            for (int px = xStart; px < xStart + width && !belowSurface; px++) {
                for (int pz = zStart; pz < zStart + width; pz++) {
                    if (region.height(HeightmapKind.OCEAN_FLOOR_WG, px, pz) >= yStart) {
                        belowSurface = true;
                        break;
                    }
                }
            }
            return belowSurface && fillVein(region, random, settings, x0, x1, z0, z1, y0, y1, xStart, zStart, width);
        };
    }

    private static boolean fillVein(final DecorationRegion region, final BitRandom random, final OreSettings settings,
                                    final double x0, final double x1, final double z0, final double z1,
                                    final double y0, final double y1, final int xStart, final int zStart, final int width) {
        final int size = settings.size();
        final double[] balls = new double[size * 4];
        double largest = 0.0;
        for (int i = 0; i < size; i++) {
            final float t = (float) i / size;
            final double bx = x0 + t * (x1 - x0);
            final double by = y0 + t * (y1 - y0);
            final double bz = z0 + t * (z1 - z0);
            final double swell = random.nextDouble() * size / 16.0;
            final double radius = ((Trig.sin((float) Math.PI * t) + 1.0F) * swell + 1.0) / 2.0;
            balls[i * 4] = bx;
            balls[i * 4 + 1] = by;
            balls[i * 4 + 2] = bz;
            balls[i * 4 + 3] = radius;
            largest = Math.max(largest, radius);
        }
        // a ball fully inside a bigger neighbour adds nothing: drop it
        final double stepLength = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) + (z1 - z0) * (z1 - z0)) / size;
        for (int i = 0; i < size - 1; i++) {
            if (balls[i * 4 + 3] <= 0.0) {
                continue;
            }
            final double radius = balls[i * 4 + 3];
            for (int j = i + 1; j < size; j++) {
                final double other = balls[j * 4 + 3];
                if (other <= 0.0) {
                    continue;
                }
                final double distance = (j - i) * stepLength;
                final double difference = radius - other;
                if (difference * difference > distance * distance) {
                    if (!(difference > 0.0)) {
                        balls[i * 4 + 3] = -1.0;
                        break;
                    }
                    balls[j * 4 + 3] = -1.0;
                }
                if (distance > largest) {
                    break;
                }
            }
        }

        final int lowest = region.minY();
        final int highest = region.maxY();
        final int gridMaxX = xStart + width - 1;
        final int gridMaxZ = zStart + width - 1;
        final int[] columnLow = new int[width * width];
        final int[] columnHigh = new int[width * width];
        Arrays.fill(columnLow, Integer.MAX_VALUE);
        Arrays.fill(columnHigh, Integer.MIN_VALUE);
        int touchedMinX = width;
        int touchedMaxX = -1;
        int touchedMinZ = width;
        int touchedMaxZ = -1;
        for (int i = 0; i < size; i++) {
            final double radius = balls[i * 4 + 3];
            if (radius < 0.0) {
                continue;
            }
            final double radiusSq = radius * radius;
            final double bx = balls[i * 4];
            final double by = balls[i * 4 + 1];
            final double bz = balls[i * 4 + 2];
            final int minX = Math.max((int) Math.floor(bx - radius), xStart);
            final int minZ = Math.max((int) Math.floor(bz - radius), zStart);
            final int maxX = (int) Math.clamp((long) Math.floor(bx + radius), minX, gridMaxX);
            final int maxZ = (int) Math.clamp((long) Math.floor(bz + radius), minZ, gridMaxZ);
            touchedMinX = Math.min(touchedMinX, minX - xStart);
            touchedMaxX = Math.max(touchedMaxX, maxX - xStart);
            touchedMinZ = Math.min(touchedMinZ, minZ - zStart);
            touchedMaxZ = Math.max(touchedMaxZ, maxZ - zStart);
            for (int px = minX; px <= maxX; px++) {
                final double dx = px + 0.5 - bx;
                final double leftAfterX = radiusSq - dx * dx;
                if (leftAfterX <= 0.0) {
                    continue;
                }
                for (int pz = minZ; pz <= maxZ; pz++) {
                    final double dz = pz + 0.5 - bz;
                    final double left = leftAfterX - dz * dz;
                    if (left <= 0.0) {
                        continue;
                    }
                    final double halfSpan = Math.sqrt(left);
                    final int low = Math.max((int) Math.floor(by - 0.5 - halfSpan) + 1, lowest);
                    final int high = Math.min((int) Math.ceil(by - 0.5 + halfSpan) - 1, highest);
                    if (low <= high) {
                        final int index = (px - xStart) * width + (pz - zStart);
                        columnLow[index] = Math.min(columnLow[index], low);
                        columnHigh[index] = Math.max(columnHigh[index], high);
                    }
                }
            }
        }

        int placed = 0;
        for (int gridX = touchedMinX; gridX <= touchedMaxX; gridX++) {
            final int px = xStart + gridX;
            for (int gridZ = touchedMinZ; gridZ <= touchedMaxZ; gridZ++) {
                final int index = gridX * width + gridZ;
                final int low = columnLow[index];
                if (low == Integer.MAX_VALUE) {
                    continue;
                }
                final int high = columnHigh[index];
                final int pz = zStart + gridZ;
                for (int py = low; py <= high; py++) {
                    if (!region.canWrite(px, py, pz)) {
                        continue;
                    }
                    final char current = region.id(px, py, pz);
                    for (final Target target : settings.targets()) {
                        if (target.test().test(current, py, random)) {
                            if (settings.exposureAllows(region, random, px, py, pz)) {
                                region.setRaw(px, py, pz, target.state());
                                placed++;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return placed > 0;
    }

    /**
     * {@return the difference of two draws between 0 and 1, the first minus the second}
     */
    private static float spread(final BitRandom random) {
        final float first = random.nextFloat();
        final float second = random.nextFloat();
        return first - second;
    }

    private static Feature scattered(final OreSettings settings) {
        return (region, random, x, y, z) -> {
            final int tries = random.nextInt(settings.size() + 1);
            for (int i = 0; i < tries; i++) {
                final int reach = Math.min(i, 7);
                final int px = x + Math.round(spread(random) * reach);
                final int py = y + Math.round(spread(random) * reach);
                final int pz = z + Math.round(spread(random) * reach);
                final char current = region.id(px, py, pz);
                for (final Target target : settings.targets()) {
                    if (target.test().test(current, py, random)) {
                        if (settings.exposureAllows(region, random, px, py, pz)) {
                            region.set(px, py, pz, target.state());
                        }
                        break;
                    }
                }
            }
            return true;
        };
    }
}
