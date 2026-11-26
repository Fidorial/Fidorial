package fr.euphyllia.fidorial.server.world.generation.carver;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.data.HeightContext;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.random.LegacyRandom;
import fr.euphyllia.fidorial.server.world.generation.value.FloatSource;
import fr.euphyllia.fidorial.server.world.generation.value.HeightSource;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Trig;
import fr.euphyllia.fidorial.server.world.generation.value.Values;

/**
 * Digs tunnels and canyons through the terrain. A carver starts in some chunks, picked by chance, and wanders up
 * to eight chunks away: each chunk asks the carvers of the chunks around it which of its blocks they reach.
 *
 * <p>The paths are drawn with the 48-bit generator and a sine table, as tunnels have been drawn for a long time:
 * the same seed digs the same tunnels.</p>
 */
public abstract sealed class Carver permits Carver.Cave, Carver.Canyon {

    /**
     * How many chunks away from where it starts a carver reaches.
     */
    public static final int RANGE = 8;

    protected final float probability;
    protected final HeightSource y;

    protected Carver(final float probability, final HeightSource y) {
        this.probability = probability;
        this.y = y;
    }

    static float sin(final double angle) {
        return Trig.sin(angle);
    }

    static float cos(final double angle) {
        return Trig.cos(angle);
    }

    /**
     * Reads a carver definition.
     */
    public static Carver parse(final JsonElement element) {
        final JsonObject object = element.getAsJsonObject();
        final String type = Values.type(object);
        final float probability = object.get("probability").getAsFloat();
        final HeightSource y = HeightSource.parse(object.get("y"));
        return switch (type) {
            case "cave" -> new Cave(probability, y,
                    IntSource.parse(object.get("count")),
                    FloatSource.parse(object.get("thickness")),
                    object.has("weird_thickness_bias") && object.get("weird_thickness_bias").getAsBoolean(),
                    FloatSource.parse(object.get("room_vertical_radius_multiplier")),
                    FloatSource.parse(object.get("horizontal_radius_multiplier")),
                    FloatSource.parse(object.get("vertical_radius_multiplier")),
                    object.has("start_vertical_radius_multiplier") ? FloatSource.parse(object.get("start_vertical_radius_multiplier"))
                            : FloatSource.constant(1.0F),
                    FloatSource.parse(object.get("floor_level")));
            case "canyon" -> {
                final JsonObject shape = object.getAsJsonObject("shape");
                yield new Canyon(probability, y, FloatSource.parse(object.get("vertical_rotation")),
                        FloatSource.parse(shape.get("distance_factor")),
                        FloatSource.parse(shape.get("thickness")),
                        shape.get("width_smoothness").getAsInt(),
                        FloatSource.parse(shape.get("horizontal_radius_factor")),
                        shape.get("vertical_radius_default_factor").getAsFloat(),
                        shape.get("vertical_radius_center_factor").getAsFloat(),
                        FloatSource.parse(shape.get("y_scale")));
            }
            default -> throw new IllegalArgumentException("Unknown carver type " + type);
        };
    }

    /**
     * {@return whether the carver starts in the chunk the stream was seeded for}
     */
    public boolean starts(final BitRandom random) {
        return random.nextFloat() <= probability;
    }

    /**
     * Carves the blocks of one chunk reached by the carver starting in another.
     *
     * @param chunkX  the chunk being carved
     * @param chunkZ  the same along z
     * @param sourceX the chunk the carver starts in
     * @param sourceZ the same along z
     */
    public abstract void carve(HeightContext heights, BitRandom random, int chunkX, int chunkZ, int sourceX, int sourceZ, CarvingMask mask);

    /**
     * A test on a block of an ellipsoid, given where the block is relative to the centre, in radii.
     */
    @FunctionalInterface
    interface SkipTest {
        boolean skip(double xd, double yd, double zd, int y);
    }

    /**
     * Carves the blocks of an ellipsoid that fall in the chunk.
     */
    static void carveEllipsoid(final int chunkX, final int chunkZ, final double x, final double y, final double z,
                               final double horizontalRadius, final double verticalRadius, final CarvingMask mask, final SkipTest skip) {
        final double centerX = (chunkX << 4) + 8;
        final double centerZ = (chunkZ << 4) + 8;
        final double reach = 16.0 + horizontalRadius * 2.0;
        if (Math.abs(x - centerX) > reach || Math.abs(z - centerZ) > reach) {
            return;
        }
        final int minBlockX = chunkX << 4;
        final int minBlockZ = chunkZ << 4;
        final int fromX = Math.max((int) Math.floor(x - horizontalRadius) - minBlockX - 1, 0);
        final int toX = Math.min((int) Math.floor(x + horizontalRadius) - minBlockX, 15);
        final int fromY = Math.max((int) Math.floor(y - verticalRadius) - 1, mask.minY());
        final int toY = Math.min((int) Math.floor(y + verticalRadius) + 1, mask.maxY());
        final int fromZ = Math.max((int) Math.floor(z - horizontalRadius) - minBlockZ - 1, 0);
        final int toZ = Math.min((int) Math.floor(z + horizontalRadius) - minBlockZ, 15);
        for (int localX = fromX; localX <= toX; localX++) {
            final double xd = (minBlockX + localX + 0.5 - x) / horizontalRadius;
            for (int localZ = fromZ; localZ <= toZ; localZ++) {
                final double zd = (minBlockZ + localZ + 0.5 - z) / horizontalRadius;
                if (xd * xd + zd * zd >= 1.0) {
                    continue;
                }
                for (int worldY = toY; worldY > fromY; worldY--) {
                    final double yd = (worldY - 0.5 - y) / verticalRadius;
                    if (!skip.skip(xd, yd, zd, worldY)) {
                        mask.carve(localX, worldY, localZ);
                    }
                }
            }
        }
    }

    /**
     * {@return whether a tunnel can still reach the chunk from where it is, with the steps it has left}
     */
    static boolean canReach(final int chunkX, final int chunkZ, final double x, final double z,
                            final int step, final int steps, final float thickness) {
        final double dx = x - ((chunkX << 4) + 8);
        final double dz = z - ((chunkZ << 4) + 8);
        final double remaining = steps - step;
        final double reach = thickness + 2.0F + 16.0F;
        return dx * dx + dz * dz - remaining * remaining <= reach * reach;
    }

    /**
     * Winding tunnels that fork, sometimes from a round room.
     */
    static final class Cave extends Carver {
        private final IntSource count;
        private final FloatSource thickness;
        private final boolean weirdThicknessBias;
        private final FloatSource roomVerticalRadius;
        private final FloatSource horizontalRadius;
        private final FloatSource verticalRadius;
        private final FloatSource startVerticalRadius;
        private final FloatSource floorLevel;

        Cave(final float probability, final HeightSource y, final IntSource count, final FloatSource thickness,
             final boolean weirdThicknessBias, final FloatSource roomVerticalRadius, final FloatSource horizontalRadius,
             final FloatSource verticalRadius, final FloatSource startVerticalRadius, final FloatSource floorLevel) {
            super(probability, y);
            this.count = count;
            this.thickness = thickness;
            this.weirdThicknessBias = weirdThicknessBias;
            this.roomVerticalRadius = roomVerticalRadius;
            this.horizontalRadius = horizontalRadius;
            this.verticalRadius = verticalRadius;
            this.startVerticalRadius = startVerticalRadius;
            this.floorLevel = floorLevel;
        }

        @Override
        public void carve(final HeightContext heights, final BitRandom random, final int chunkX, final int chunkZ,
                          final int sourceX, final int sourceZ, final CarvingMask mask) {
            final int maxDistance = (4 * 2 - 1) << 4;
            final int caves = count.sample(random);
            for (int cave = 0; cave < caves; cave++) {
                final double x = (sourceX << 4) + random.nextInt(16);
                final double startY = y.sample(random, heights);
                final double z = (sourceZ << 4) + random.nextInt(16);
                final double horizontalMultiplier = horizontalRadius.sample(random);
                final double verticalMultiplier = verticalRadius.sample(random);
                final double startMultiplier = startVerticalRadius.sample(random);
                final double floor = floorLevel.sample(random);
                final SkipTest skip = (xd, yd, zd, worldY) -> yd <= floor || xd * xd + yd * yd + zd * zd >= 1.0;
                int tunnels = 1;
                if (random.nextInt(4) == 0) {
                    final double yScale = roomVerticalRadius.sample(random);
                    final float roomThickness = 1.0F + random.nextFloat() * 6.0F;
                    final double radius = 1.5 + sin(Math.PI / 2) * roomThickness;
                    carveEllipsoid(chunkX, chunkZ, x + 1.0, startY, z, radius, radius * yScale, mask, skip);
                    tunnels += random.nextInt(4);
                }
                for (int i = 0; i < tunnels; i++) {
                    final float yaw = random.nextFloat() * (float) (Math.PI * 2);
                    final float pitch = (random.nextFloat() - 0.5F) / 4.0F;
                    final float tunnelThickness = thickness(random);
                    final int distance = maxDistance - random.nextInt(maxDistance / 4);
                    tunnel(chunkX, chunkZ, random.nextLong(), x, startY, z, horizontalMultiplier, verticalMultiplier,
                            tunnelThickness, yaw, pitch, 0, distance, startMultiplier, mask, skip);
                }
            }
        }

        private float thickness(final BitRandom random) {
            float value = thickness.sample(random);
            if (weirdThicknessBias && random.nextInt(10) == 0) {
                value *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
            }
            return value;
        }

        private void tunnel(final int chunkX, final int chunkZ, final long seed, double x, double y, double z,
                            final double horizontalMultiplier, final double verticalMultiplier, final float thickness,
                            float yaw, float pitch, final int firstStep, final int steps, final double yScale,
                            final CarvingMask mask, final SkipTest skip) {
            final LegacyRandom random = new LegacyRandom(seed);
            final int split = random.nextInt(steps / 2) + steps / 4;
            final boolean steep = random.nextInt(6) == 0;
            float yawChange = 0.0F;
            float pitchChange = 0.0F;
            for (int step = firstStep; step < steps; step++) {
                final double horizontal = 1.5 + sin((float) Math.PI * step / steps) * thickness;
                final double vertical = horizontal * yScale;
                final float pitchCos = cos(pitch);
                x += cos(yaw) * pitchCos;
                y += sin(pitch);
                z += sin(yaw) * pitchCos;
                pitch *= steep ? 0.92F : 0.7F;
                pitch += pitchChange * 0.1F;
                yaw += yawChange * 0.1F;
                pitchChange *= 0.9F;
                yawChange *= 0.75F;
                final float pitchRandom1 = random.nextFloat();
                final float pitchRandom2 = random.nextFloat();
                final float pitchRandom3 = random.nextFloat();

                pitchChange += (pitchRandom1 - pitchRandom2) * pitchRandom3 * 2.0F;

                final float yawRandom1 = random.nextFloat();
                final float yawRandom2 = random.nextFloat();
                final float yawRandom3 = random.nextFloat();

                yawChange += (yawRandom1 - yawRandom2) * yawRandom3 * 4.0F;
                if (step == split && thickness > 1.0F) {
                    tunnel(chunkX, chunkZ, random.nextLong(), x, y, z, horizontalMultiplier, verticalMultiplier,
                            random.nextFloat() * 0.5F + 0.5F, yaw - (float) (Math.PI / 2), pitch / 3.0F, step, steps, 1.0, mask, skip);
                    tunnel(chunkX, chunkZ, random.nextLong(), x, y, z, horizontalMultiplier, verticalMultiplier,
                            random.nextFloat() * 0.5F + 0.5F, yaw + (float) (Math.PI / 2), pitch / 3.0F, step, steps, 1.0, mask, skip);
                    return;
                }
                if (random.nextInt(4) != 0) {
                    if (!canReach(chunkX, chunkZ, x, z, step, steps, thickness)) {
                        return;
                    }
                    carveEllipsoid(chunkX, chunkZ, x, y, z, horizontal * horizontalMultiplier, vertical * verticalMultiplier, mask, skip);
                }
            }
        }
    }

    /**
     * Long, deep, narrow ravines.
     */
    static final class Canyon extends Carver {
        private final FloatSource verticalRotation;
        private final FloatSource distanceFactor;
        private final FloatSource thickness;
        private final int widthSmoothness;
        private final FloatSource horizontalRadiusFactor;
        private final float verticalRadiusDefault;
        private final float verticalRadiusCenter;
        private final FloatSource yScale;

        Canyon(final float probability, final HeightSource y, final FloatSource verticalRotation, final FloatSource distanceFactor,
               final FloatSource thickness, final int widthSmoothness, final FloatSource horizontalRadiusFactor,
               final float verticalRadiusDefault, final float verticalRadiusCenter, final FloatSource yScale) {
            super(probability, y);
            this.verticalRotation = verticalRotation;
            this.distanceFactor = distanceFactor;
            this.thickness = thickness;
            this.widthSmoothness = widthSmoothness;
            this.horizontalRadiusFactor = horizontalRadiusFactor;
            this.verticalRadiusDefault = verticalRadiusDefault;
            this.verticalRadiusCenter = verticalRadiusCenter;
            this.yScale = yScale;
        }

        @Override
        public void carve(final HeightContext heights, final BitRandom random, final int chunkX, final int chunkZ,
                          final int sourceX, final int sourceZ, final CarvingMask mask) {
            final int maxDistance = (4 * 2 - 1) * 16;
            final double x = (sourceX << 4) + random.nextInt(16);
            final int startY = y.sample(random, heights);
            final double z = (sourceZ << 4) + random.nextInt(16);
            final float yaw = random.nextFloat() * (float) (Math.PI * 2);
            final float pitch = verticalRotation.sample(random);
            final double scale = yScale.sample(random);
            final float width = thickness.sample(random);
            final int distance = (int) (maxDistance * distanceFactor.sample(random));
            ravine(heights, chunkX, chunkZ, random.nextLong(), x, startY, z, width, yaw, pitch, distance, scale, mask);
        }

        private void ravine(final HeightContext heights, final int chunkX, final int chunkZ, final long seed,
                            double x, double y, double z, final float thickness, float yaw, float pitch,
                            final int distance, final double scale, final CarvingMask mask) {
            final LegacyRandom random = new LegacyRandom(seed);
            final float[] widths = new float[heights.height()];
            float width = 1.0F;
            for (int i = 0; i < widths.length; i++) {
                if (i == 0 || random.nextInt(widthSmoothness) == 0) {
                    width = 1.0F + random.nextFloat() * random.nextFloat();
                }
                widths[i] = width * width;
            }
            final SkipTest skip = (xd, yd, zd, worldY) ->
                    (xd * xd + zd * zd) * widths[worldY - heights.minY() - 1] + yd * yd / 6.0 >= 1.0;
            float yawChange = 0.0F;
            float pitchChange = 0.0F;
            for (int step = 0; step < distance; step++) {
                double horizontal = 1.5 + sin(step * (float) Math.PI / distance) * thickness;
                double vertical = horizontal * scale;
                horizontal *= horizontalRadiusFactor.sample(random);
                final float middle = 1.0F - Math.abs(0.5F - (float) step / distance) * 2.0F;
                final float factor = verticalRadiusDefault + verticalRadiusCenter * middle;
                vertical = factor * vertical * (random.nextFloat() * (1.0F - 0.75F) + 0.75F);
                final float pitchCos = cos(pitch);
                final float pitchSin = sin(pitch);
                x += cos(yaw) * pitchCos;
                y += pitchSin;
                z += sin(yaw) * pitchCos;
                pitch *= 0.7F;
                pitch += pitchChange * 0.05F;
                yaw += yawChange * 0.05F;
                pitchChange *= 0.8F;
                yawChange *= 0.5F;
                final float pitchRandom1 = random.nextFloat();
                final float pitchRandom2 = random.nextFloat();
                final float pitchRandom3 = random.nextFloat();

                pitchChange += (pitchRandom1 - pitchRandom2) * pitchRandom3 * 2.0F;

                final float yawRandom1 = random.nextFloat();
                final float yawRandom2 = random.nextFloat();
                final float yawRandom3 = random.nextFloat();

                yawChange += (yawRandom1 - yawRandom2) * yawRandom3 * 4.0F;
                if (random.nextInt(4) != 0) {
                    if (!canReach(chunkX, chunkZ, x, z, step, distance, thickness)) {
                        return;
                    }
                    carveEllipsoid(chunkX, chunkZ, x, y, z, horizontal, vertical, mask, skip);
                }
            }
        }
    }
}
