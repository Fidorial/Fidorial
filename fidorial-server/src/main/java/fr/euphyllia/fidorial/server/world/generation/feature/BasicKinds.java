package fr.euphyllia.fidorial.server.world.generation.feature.kind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.feature.BlockTest;
import fr.euphyllia.fidorial.server.world.generation.feature.DecorationRegion;
import fr.euphyllia.fidorial.server.world.generation.feature.Feature;
import fr.euphyllia.fidorial.server.world.generation.feature.FeatureLibrary;
import fr.euphyllia.fidorial.server.world.generation.feature.PlacedFeature;
import fr.euphyllia.fidorial.server.world.generation.feature.StateFlags;
import fr.euphyllia.fidorial.server.world.generation.feature.StateSource;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.IntSource;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The features that pick or chain other features, and the ones that set a single block or a column of blocks.
 */
final class BasicKinds {


    private BasicKinds() {
    }

    static void register(final Map<String, FeatureLibrary.Parser> parsers) {
        parsers.put("no_op", (library, data) -> Feature.NOTHING);
        parsers.put("random_selector", BasicKinds::randomSelector);
        parsers.put("simple_random_selector", (library, data) -> {
            final List<PlacedFeature> features = placedList(library, data.get("features"));
            return (region, random, x, y, z) -> features.get(random.nextInt(features.size())).place(region, random, x, y, z, false);
        });
        parsers.put("weighted_random_selector", (library, data) -> {
            final JsonArray entries = data.getAsJsonArray("features");
            final List<PlacedFeature> features = new ArrayList<>();
            final int[] weights = new int[entries.size()];
            int total = 0;
            for (int i = 0; i < weights.length; i++) {
                final JsonObject entry = entries.get(i).getAsJsonObject();
                features.add(library.placedFeature(entry.get("data")));
                weights[i] = entry.get("weight").getAsInt();
                total += weights[i];
            }
            final int totalWeight = total;
            return (region, random, x, y, z) -> totalWeight > 0
                    && features.get(Values.pickWeighted(random, weights, totalWeight)).place(region, random, x, y, z, false);
        });
        parsers.put("random_boolean_selector", (library, data) -> {
            final PlacedFeature whenTrue = library.placedFeature(data.get("feature_true"));
            final PlacedFeature whenFalse = library.placedFeature(data.get("feature_false"));
            return (region, random, x, y, z) -> (random.nextBoolean() ? whenTrue : whenFalse).place(region, random, x, y, z, false);
        });
        parsers.put("sequence", (library, data) -> {
            final List<PlacedFeature> features = placedList(library, data.get("features"));
            return (region, random, x, y, z) -> {
                for (final PlacedFeature feature : features) {
                    if (!feature.place(region, random, x, y, z, false)) {
                        return false;
                    }
                }
                return true;
            };
        });
        parsers.put("overlay", (library, data) -> {
            final List<PlacedFeature> features = placedList(library, data.get("features"));
            return (region, random, x, y, z) -> {
                boolean placedAny = false;
                for (final PlacedFeature feature : features) {
                    placedAny |= feature.place(region, random, x, y, z, false);
                }
                return placedAny;
            };
        });
        parsers.put("simple_block", BasicKinds::simpleBlock);
        parsers.put("block_column", BasicKinds::blockColumn);
    }

    static List<PlacedFeature> placedList(final FeatureLibrary library, final JsonElement element) {
        final List<PlacedFeature> features = new ArrayList<>();
        if (element.isJsonArray()) {
            for (final JsonElement entry : element.getAsJsonArray()) {
                features.add(library.placedFeature(entry));
            }
        } else if (element.getAsString().startsWith("#")) {
            for (final String id : library.context().data().tag(
                    WorldgenData.Section.PLACED_FEATURE_TAG,
                    element.getAsString().substring(1))) {
                features.add(library.placed(id));
            }
        } else {
            features.add(library.placed(element.getAsString()));
        }
        return features;
    }

    private static Feature randomSelector(final FeatureLibrary library, final JsonObject data) {
        final JsonArray entries = data.getAsJsonArray("features");
        final List<PlacedFeature> features = new ArrayList<>();
        final float[] chances = new float[entries.size()];
        for (int i = 0; i < chances.length; i++) {
            final JsonObject entry = entries.get(i).getAsJsonObject();
            features.add(library.placedFeature(entry.get("feature")));
            chances[i] = entry.get("chance").getAsFloat();
        }
        final PlacedFeature fallback = library.placedFeature(data.get("default"));
        return (region, random, x, y, z) -> {
            for (int i = 0; i < chances.length; i++) {
                if (random.nextFloat() < chances[i]) {
                    return features.get(i).place(region, random, x, y, z, false);
                }
            }
            return fallback.place(region, random, x, y, z, false);
        };
    }

    private static Feature simpleBlock(final FeatureLibrary library, final JsonObject data) {
        final StateSource source = library.source(data.get("to_place"));
        final boolean scheduleTick = data.has("schedule_tick") && data.get("schedule_tick").getAsBoolean();
        final StateFlags states = library.context().states();
        return (region, random, x, y, z) -> {
            final char state = source.pick(region, random, x, y, z);
            if (state == StateSource.NONE || !region.canSurvive(state, x, y, z)) {
                return false;
            }
            if (states.has(state, StateFlags.TWO_BLOCKS_TALL)) {
                final char above = region.id(x, y + 1, z);
                if (!states.isAir(above)) {
                    final long fluids = StateFlags.WATER | StateFlags.WATER_SOURCE | StateFlags.LAVA | StateFlags.LAVA_SOURCE;
                    final boolean sameFluid = (states.flags(state) & fluids) == (states.flags(above) & fluids);
                    if (!sameFluid || !states.has(above, StateFlags.REPLACEABLE)) {
                        return false;
                    }
                }
                placeDoublePlant(region, state, x, y, z);
            } else if (states.is(state, BlockTypeKeys.PALE_MOSS_CARPET)) {
                MossCarpets.place(region, x, y, z, region.ownRandom());
            } else {
                region.set(x, y, z, state);
            }
            if (scheduleTick) {
                region.scheduleFluid(x, y, z);
            }
            return true;
        };
    }

    /**
     * Sets both halves of a two-block plant, each holding water when it replaces water.
     */
    static void placeDoublePlant(final DecorationRegion region, final char state, final int x, final int y, final int z) {
        final StateFlags states = region.states();
        region.set(x, y, z, keepWater(region, states.with(state, "half", "lower"), x, y, z));
        region.set(x, y + 1, z, keepWater(region, states.with(state, "half", "upper"), x, y + 1, z));
    }

    /**
     * {@return a state that holds water when its block holds some now, for the blocks that can}
     */
    static char keepWater(final DecorationRegion region, final char state, final int x, final int y, final int z) {
        final StateFlags states = region.states();
        if (states.property(state, "waterlogged") == null) {
            return state;
        }
        return states.with(state, "waterlogged", Boolean.toString(region.has(x, y, z, StateFlags.WATER)));
    }

    private static Feature blockColumn(final FeatureLibrary library, final JsonObject data) {
        final JsonArray layerData = data.getAsJsonArray("layers");
        final IntSource[] heights = new IntSource[layerData.size()];
        final StateSource[] states = new StateSource[layerData.size()];
        for (int i = 0; i < heights.length; i++) {
            final JsonObject layer = layerData.get(i).getAsJsonObject();
            heights[i] = IntSource.parse(layer.get("height"));
            states[i] = library.source(layer.has("provider") ? layer.get("provider") : layer.get("state"));
        }
        final BlockFace direction = BlockFace.valueOf(data.get("direction").getAsString().toUpperCase(Locale.ROOT));
        final BlockTest allowed = library.test(data.get("allowed_placement"));
        final boolean prioritizeTip = data.has("prioritize_tip") && data.get("prioritize_tip").getAsBoolean();
        return (region, random, x, y, z) -> placeColumn(region, random, x, y, z, heights, states, direction, allowed, prioritizeTip);
    }

    private static boolean placeColumn(final DecorationRegion region, final BitRandom random, final int x, final int y, final int z,
                                       final IntSource[] heights, final StateSource[] states, final BlockFace direction,
                                       final BlockTest allowed, final boolean prioritizeTip) {
        final int[] counts = new int[heights.length];
        int total = 0;
        for (int i = 0; i < counts.length; i++) {
            counts[i] = heights[i].sample(random);
            total += counts[i];
        }
        if (total == 0) {
            return false;
        }
        final int dx = direction.dx();
        final int dy = direction.dy();
        final int dz = direction.dz();
        int nx = x + dx;
        int ny = y + dy;
        int nz = z + dz;
        for (int i = 0; i < total; i++) {
            if (!allowed.test(region, nx, ny, nz)) {
                trim(counts, total, i, prioritizeTip);
                break;
            }
            nx += dx;
            ny += dy;
            nz += dz;
        }
        int px = x;
        int py = y;
        int pz = z;
        for (int i = 0; i < counts.length; i++) {
            for (int j = 0; j < counts[i]; j++) {
                region.set(px, py, pz, states[i].pickOrKeep(region, random, px, py, pz));
                px += dx;
                py += dy;
                pz += dz;
            }
        }
        return true;
    }

    /**
     * Shortens a column to the room it has, taking the blocks away from the tip, or from the base when the tip
     * must stay.
     */
    private static void trim(final int[] counts, final int total, final int room, final boolean keepTip) {
        int excess = total - room;
        final int step = keepTip ? 1 : -1;
        final int start = keepTip ? 0 : counts.length - 1;
        final int end = keepTip ? counts.length : -1;
        for (int i = start; i != end && excess > 0; i += step) {
            final int removed = Math.min(counts[i], excess);
            excess -= removed;
            counts[i] -= removed;
        }
    }
}
