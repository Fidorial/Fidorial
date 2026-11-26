package fr.euphyllia.fidorial.server.world.generation.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.value.HeightSource;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.world.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reads the placement steps written in the data.
 */
final class Placements {

    private Placements() {
    }

    static List<Placement> parseAll(final FeatureLibrary library, final JsonElement element) {
        final List<Placement> placements = new ArrayList<>();
        if (element != null) {
            for (final JsonElement entry : element.getAsJsonArray()) {
                placements.add(parse(library, entry));
            }
        }
        return placements;
    }

    static Placement parse(final FeatureLibrary library, final JsonElement element) {
        final JsonObject object = element.getAsJsonObject();
        return switch (Values.type(object)) {
            case "count" -> {
                final IntSource count = IntSource.parse(object.get("count"));
                yield (run, random, x, y, z, out) -> {
                    final int times = count.sample(random);
                    for (int i = 0; i < times; i++) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "in_square" -> (run, random, x, y, z, out) -> {
                final int px = random.nextInt(16) + x;
                final int pz = random.nextInt(16) + z;
                out.accept(px, y, pz);
            };
            case "height_range" -> {
                final HeightSource height = HeightSource.parse(object.get("height"));
                yield (run, random, x, y, z, out) -> out.accept(x, height.sample(random, run.region().heights()), z);
            }
            case "heightmap" -> {
                final Heightmap kind = Heightmap.byName(object.get("heightmap").getAsString());
                yield (run, random, x, y, z, out) -> {
                    final int height = run.region().height(kind, x, z);
                    if (height > run.region().minY()) {
                        out.accept(x, height, z);
                    }
                };
            }
            case "biome" -> (run, random, x, y, z, out) -> {
                final PlacedFeature checked = run.biomeCheck();
                if (checked == null) {
                    out.accept(x, y, z);
                    return;
                }
                final BiomeInfo biome = run.region().biome(x, y, z);
                if (run.region().context().biomeHasFeature(biome, checked.id())) {
                    out.accept(x, y, z);
                }
            };
            case "block_predicate_filter" -> {
                final BlockTest test = library.test(object.get("predicate"));
                yield (run, random, x, y, z, out) -> {
                    if (test.test(run.region(), x, y, z)) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "offset", "random_offset" -> {
                final IntSource dx = IntSource.parse(object.has("x") ? object.get("x") : object.get("xz_spread"));
                final IntSource dy = IntSource.parse(object.has("y") ? object.get("y") : object.get("y_spread"));
                final IntSource dz = IntSource.parse(object.has("z") ? object.get("z") : object.get("xz_spread"));
                yield (run, random, x, y, z, out) -> {
                    final int ox = dx.sample(random);
                    final int oy = dy.sample(random);
                    final int oz = dz.sample(random);
                    out.accept(x + ox, y + oy, z + oz);
                };
            }
            case "rarity_filter" -> {
                final float chance = 1.0F / object.get("chance").getAsInt();
                yield (run, random, x, y, z, out) -> {
                    if (random.nextFloat() < chance) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "random_chance" -> {
                final float chance = object.get("chance").getAsFloat();
                yield (run, random, x, y, z, out) -> {
                    if (random.nextFloat() < chance) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "surface_water_depth_filter" -> {
                final int maxDepth = object.get("max_water_depth").getAsInt();
                yield (run, random, x, y, z, out) -> {
                    final int floor = run.region().height(Heightmap.OCEAN_FLOOR, x, z);
                    final int surface = run.region().height(Heightmap.WORLD_SURFACE, x, z);
                    if (surface - floor <= maxDepth) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "surface_relative_threshold_filter" -> {
                final Heightmap kind = Heightmap.byName(object.get("heightmap").getAsString());
                final long low = object.has("min_inclusive") ? object.get("min_inclusive").getAsInt() : Integer.MIN_VALUE;
                final long high = object.has("max_inclusive") ? object.get("max_inclusive").getAsInt() : Integer.MAX_VALUE;
                yield (run, random, x, y, z, out) -> {
                    final long surface = run.region().height(kind, x, z);
                    if (surface + low <= y && y <= surface + high) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "environment_scan" -> environmentScan(library, object);
            case "noise_threshold_count" -> {
                final double level = object.get("noise_level").getAsDouble();
                final int below = object.get("below_noise").getAsInt();
                final int above = object.get("above_noise").getAsInt();
                yield (run, random, x, y, z, out) -> {
                    final double noise = run.region().context().vegetationNoise(x / 200.0, z / 200.0);
                    final int times = noise < level ? below : above;
                    for (int i = 0; i < times; i++) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "noise_based_count" -> {
                final int ratio = object.get("noise_to_count_ratio").getAsInt();
                final double factor = object.get("noise_factor").getAsDouble();
                final double offset = object.has("noise_offset") ? object.get("noise_offset").getAsDouble() : 0.0;
                yield (run, random, x, y, z, out) -> {
                    final double noise = run.region().context().vegetationNoise(x / factor, z / factor);
                    final int times = (int) Math.ceil((noise + offset) * ratio);
                    for (int i = 0; i < times; i++) {
                        out.accept(x, y, z);
                    }
                };
            }
            case "randomly_selected" -> {
                final List<Placement> choices = parseAll(library, object.get("placements"));
                yield (run, random, x, y, z, out) -> choices.get(random.nextInt(choices.size())).apply(run, random, x, y, z, out);
            }
            case "cuboid" -> cuboid(object);
            case "fixed_placement" -> {
                final JsonArray positions = object.getAsJsonArray("positions");
                final int[][] fixed = new int[positions.size()][];
                for (int i = 0; i < fixed.length; i++) {
                    fixed[i] = BlockTests.offset(positions.get(i));
                }
                yield (run, random, x, y, z, out) -> {
                    for (final int[] position : fixed) {
                        if ((position[0] >> 4) == (x >> 4) && (position[2] >> 4) == (z >> 4)) {
                            out.accept(position[0], position[1], position[2]);
                        }
                    }
                };
            }
            default -> {
                library.unsupported("placement modifier", Values.type(object));
                yield (run, random, x, y, z, out) -> {
                };
            }
        };
    }

    private static Placement environmentScan(final FeatureLibrary library, final JsonObject object) {
        final BlockFace direction = BlockFace.valueOf(object.get("direction_of_search").getAsString().toUpperCase(Locale.ROOT));
        final BlockTest target = library.test(object.get("target_condition"));
        final BlockTest allowed = object.has("allowed_search_condition")
                ? library.test(object.get("allowed_search_condition")) : BlockTest.ALWAYS;
        final int maxSteps = object.get("max_steps").getAsInt();
        final int sx = direction.dx();
        final int sy = direction.dy();
        final int sz = direction.dz();
        return (run, random, x, y, z, out) -> {
            final DecorationRegion region = run.region();
            int px = x;
            int py = y;
            int pz = z;
            if (!allowed.test(region, px, py, pz)) {
                return;
            }
            for (int i = 0; i < maxSteps; i++) {
                if (target.test(region, px, py, pz)) {
                    out.accept(px, py, pz);
                    return;
                }
                px += sx;
                py += sy;
                pz += sz;
                if (region.isOutsideHeight(py)) {
                    return;
                }
                if (!allowed.test(region, px, py, pz)) {
                    break;
                }
            }
            if (target.test(region, px, py, pz)) {
                out.accept(px, py, pz);
            }
        };
    }

    private static Placement cuboid(final JsonObject object) {
        final IntSource xzSize = IntSource.parse(object.get("xz_size"));
        final IntSource ySize = IntSource.parse(object.get("y_size"));
        final boolean edges = !object.has("include_edges") || object.get("include_edges").getAsBoolean();
        final boolean interior = !object.has("include_interior") || object.get("include_interior").getAsBoolean();
        return (run, random, x, y, z, out) -> {
            final int height = ySize.sample(random);
            final int width = xzSize.sample(random);
            final int length = xzSize.sample(random);
            for (int dx = 0; dx < width; dx++) {
                final boolean xFace = dx == 0 || dx == width - 1;
                for (int dy = 0; dy < height; dy++) {
                    final boolean yFace = dy == 0 || dy == height - 1;
                    for (int dz = 0; dz < length; dz++) {
                        final boolean zFace = dz == 0 || dz == length - 1;
                        // an edge is where two faces meet
                        final boolean edge = xFace && yFace || zFace && yFace || xFace && zFace;
                        final boolean onFace = xFace || yFace || zFace;
                        if ((edges || !edge) && (interior || onFace)) {
                            out.accept(x + dx, y + dy, z + dz);
                        }
                    }
                }
            }
        };
    }
}
