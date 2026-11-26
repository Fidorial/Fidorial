package fr.euphyllia.fidorial.server.world.generation.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.value.Values;

/**
 * A test of the block a feature is about to replace, such as "any stone an ore can replace": the state, its
 * height, and sometimes a draw from the stream.
 */
@FunctionalInterface
public interface TargetTest {

    boolean test(char state, int y, BitRandom random);

    /**
     * Reads a test written in the data, by its {@code predicate_type}.
     */
    static TargetTest parse(final FeatureLibrary library, final JsonElement element) {
        final JsonObject object = element.getAsJsonObject();
        final StateFlags states = library.context().states();
        final String type = Values.typeName(object.get("predicate_type").getAsString());
        return switch (type) {
            case "always_true" -> (state, y, random) -> true;
            case "tag_match" -> {
                final StateFlags.Membership tag = states.membership(states.blocks().tag(object.get("tag").getAsString()));
                yield (state, y, random) -> tag.test(state);
            }
            case "block_match" -> {
                final StateFlags.Membership block = library.blockSet(object.get("block"));
                yield (state, y, random) -> block.test(state);
            }
            case "blockstate_match" -> {
                final char expected = library.state(object.get("block_state"));
                yield (state, y, random) -> state == expected;
            }
            case "random_block_match" -> {
                final StateFlags.Membership block = library.blockSet(object.get("block"));
                final float probability = object.get("probability").getAsFloat();
                yield (state, y, random) -> block.test(state) && random.nextFloat() < probability;
            }
            case "random_blockstate_match" -> {
                final char expected = library.state(object.get("block_state"));
                final float probability = object.get("probability").getAsFloat();
                yield (state, y, random) -> state == expected && random.nextFloat() < probability;
            }
            case "height_match" -> {
                final int low = object.get("min_inclusive").getAsInt();
                final int high = object.get("max_inclusive").getAsInt();
                yield (state, y, random) -> low <= y && y <= high;
            }
            case "all_of", "any_of" -> {
                final var rules = object.getAsJsonArray("rules");
                final TargetTest[] parts = new TargetTest[rules.size()];
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parse(library, rules.get(i));
                }
                final boolean all = type.equals("all_of");
                yield (state, y, random) -> {
                    for (final TargetTest part : parts) {
                        if (part.test(state, y, random) != all) {
                            return !all;
                        }
                    }
                    return all;
                };
            }
            case "not" -> {
                final TargetTest inner = parse(library, object.has("rule") ? object.get("rule") : object.get("predicate"));
                yield (state, y, random) -> !inner.test(state, y, random);
            }
            default -> {
                library.unsupported("rule test", type);
                yield (state, y, random) -> false;
            }
        };
    }
}
