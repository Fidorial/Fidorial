package fr.euphyllia.fidorial.server.world.generation.feature;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Fluid;
import fr.fidorial.world.BlockFace;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reads the block tests written in the data: what is at a block, whether a plant would stay there, whether a
 * face can hold something, and their combinations.
 */
final class BlockTests {

    private BlockTests() {
    }

    static BlockTest parse(final FeatureLibrary library, final JsonElement element) {
        final JsonObject object = element.getAsJsonObject();
        final StateFlags states = library.context().states();
        final int[] offset = offset(object.get("offset"));
        final int ox = offset[0];
        final int oy = offset[1];
        final int oz = offset[2];
        return switch (Values.type(object)) {
            case "true", "unobstructed" -> BlockTest.ALWAYS;
            case "not" -> parse(library, object.get("predicate")).negate();
            case "all_of" -> {
                final BlockTest[] parts = parseAll(library, object.getAsJsonArray("predicates"));
                yield (region, x, y, z) -> {
                    for (final BlockTest part : parts) {
                        if (!part.test(region, x, y, z)) {
                            return false;
                        }
                    }
                    return true;
                };
            }
            case "any_of" -> {
                final BlockTest[] parts = parseAll(library, object.getAsJsonArray("predicates"));
                yield (region, x, y, z) -> {
                    for (final BlockTest part : parts) {
                        if (part.test(region, x, y, z)) {
                            return true;
                        }
                    }
                    return false;
                };
            }
            case "matching_blocks" -> {
                final StateFlags.Membership blocks = library.blockSet(object.get("blocks"));
                yield (region, x, y, z) -> blocks.test(region.id(x + ox, y + oy, z + oz));
            }
            case "matching_block_tag" -> {
                final StateFlags.Membership tag = states.membership(states.blocks().tag(object.get("tag").getAsString()));
                yield (region, x, y, z) -> tag.test(region.id(x + ox, y + oy, z + oz));
            }
            case "matching_fluids" -> {
                final FluidMatch match = fluids(object);
                yield (region, x, y, z) -> match.test(states, region.id(x + ox, y + oy, z + oz));
            }
            case "replaceable" -> (region, x, y, z) -> region.has(x + ox, y + oy, z + oz, StateFlags.REPLACEABLE);
            case "solid" -> (region, x, y, z) -> region.has(x + ox, y + oy, z + oz, StateFlags.SOLID);
            case "inside_world_bounds" -> (region, x, y, z) -> !region.isOutsideHeight(y + oy);
            case "has_sturdy_face" -> {
                final BlockFace face = BlockFace.valueOf(object.get("direction").getAsString().toUpperCase(Locale.ROOT));
                yield (region, x, y, z) -> states.sturdy(region.id(x + ox, y + oy, z + oz), face);
            }
            case "would_survive" -> {
                final char state = library.state(object.get("state"));
                yield (region, x, y, z) -> region.canSurvive(state, x + ox, y + oy, z + oz);
            }
            case "below_heightmap" -> {
                final Heightmap kind = Heightmap.byName(object.get("heightmap").getAsString());
                yield (region, x, y, z) -> y < region.height(kind, x, z);
            }
            case "height_range" -> {
                final JsonElement low = object.get("min_inclusive");
                final JsonElement high = object.get("max_inclusive");
                yield (region, x, y, z) -> y >= region.heights().resolve(low) && y <= region.heights().resolve(high);
            }
            case "matching_biomes" -> {
                final Set<String> biomes = new HashSet<>(library.biomeSet(object.get("biomes")));
                yield (region, x, y, z) -> {
                    final BiomeInfo biome = region.biome(x, y, z);
                    return biomes.contains(biome.key().key().asString());
                };
            }
            case "volume_match" -> {
                final int[] min = offset(object.get("min"));
                final int[] max = offset(object.get("max"));
                final BlockTest match = parse(library, object.get("match"));
                yield (region, x, y, z) -> {
                    for (int dx = min[0]; dx <= max[0]; dx++) {
                        for (int dz = min[2]; dz <= max[2]; dz++) {
                            for (int dy = min[1]; dy <= max[1]; dy++) {
                                if (!match.test(region, x + dx, y + dy, z + dz)) {
                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                };
            }
            default -> {
                library.unsupported("block predicate", Values.type(object));
                yield (region, x, y, z) -> false;
            }
        };
    }

    private static BlockTest[] parseAll(final FeatureLibrary library, final JsonArray array) {
        final BlockTest[] parts = new BlockTest[array.size()];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parse(library, array.get(i));
        }
        return parts;
    }

    /**
     * {@return an offset written as three numbers, zero when absent}
     */
    static int[] offset(final JsonElement element) {
        if (element == null) {
            return new int[3];
        }
        final JsonArray array = element.getAsJsonArray();
        return new int[] {array.get(0).getAsInt(), array.get(1).getAsInt(), array.get(2).getAsInt()};
    }

    /**
     * The fluids a block may hold, and whether it must be a source.
     */
    private record FluidMatch(Set<TypedKey<Fluid>> fluids, int source) {

        boolean test(final StateFlags states, final char id) {
            if (!fluids.contains(states.fluid(id))) {
                return false;
            }
            if (source == 0) {
                return true;
            }
            final boolean isSource = states.has(id, StateFlags.WATER_SOURCE | StateFlags.LAVA_SOURCE);
            return source > 0 == isSource;
        }
    }

    private static FluidMatch fluids(final JsonObject object) {
        final List<String> names = new ArrayList<>();
        final JsonElement fluids = object.get("fluids");
        if (fluids.isJsonArray()) {
            for (final JsonElement fluid : fluids.getAsJsonArray()) {
                names.add(fluid.getAsString());
            }
        } else {
            names.add(fluids.getAsString());
        }
        final Set<TypedKey<Fluid>> keys = new HashSet<>();
        for (final String name : names) {
            keys.add(TypedKey.withRegistry(RegistryKey.FLUID, Key.key(name)));
        }
        int source = 0;
        if (object.has("state") && object.getAsJsonObject("state").has("source")) {
            source = Boolean.parseBoolean(object.getAsJsonObject("state").get("source").getAsString()) ? 1 : -1;
        }
        return new FluidMatch(Set.copyOf(keys), source);
    }
}
