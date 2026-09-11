package fr.euphyllia.fidorial.server.world.structure.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.structure.BlockValidator;
import fr.euphyllia.fidorial.server.world.structure.DataProblems;
import fr.euphyllia.fidorial.server.world.structure.JsonNbt;
import fr.euphyllia.fidorial.server.world.structure.Keys;
import fr.euphyllia.fidorial.server.world.structure.StateParser;
import fr.euphyllia.fidorial.server.world.structure.worldgen.Heightmap;
import fr.euphyllia.fidorial.server.world.structure.worldgen.IntProvider;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class ProcessorParser {

    private final BlockValidator validator;
    private final Function<Key, Set<Key>> blockTags;
    private final DataProblems problems;

    public ProcessorParser(final BlockValidator validator, final Function<Key, Set<Key>> blockTags, final DataProblems problems) {
        this.validator = validator;
        this.blockTags = blockTags;
        this.problems = problems;
    }

    public ProcessorList parseList(final JsonElement element, final String origin) {
        final JsonArray array;
        if (element.isJsonArray()) {
            array = element.getAsJsonArray();
        } else if (element.isJsonObject() && element.getAsJsonObject().has("processors")) {
            array = element.getAsJsonObject().getAsJsonArray("processors");
        } else {
            problems.warn("Processor list " + origin + " is not a list of processors");
            return ProcessorList.EMPTY;
        }
        final List<StructureProcessor> processors = new ArrayList<>();
        for (final JsonElement entry : array) {
            if (entry.isJsonObject()) {
                processors.add(parse(entry.getAsJsonObject(), origin));
            }
        }
        return new ProcessorList(processors);
    }

    public StructureProcessor parse(final JsonObject object, final String origin) {
        final String type = string(object, "processor_type", "minecraft:nop").replace("minecraft:", "");
        try {
            return switch (type) {
                case "nop", "jigsaw_replacement" -> Processors.NOP;
                case "block_ignore" -> Processors.ignore(blockNames(object.getAsJsonArray("blocks")));
                case "block_rot" -> Processors.blockRot(
                        object.has("integrity") ? object.get("integrity").getAsFloat() : 1.0F,
                        object.has("rottable_blocks") ? holderSet(object.get("rottable_blocks")) : null);
                case "gravity" -> Processors.gravity(
                        Heightmap.byName(string(object, "heightmap", "WORLD_SURFACE_WG"), Heightmap.WORLD_SURFACE_WG),
                        object.has("offset") ? object.get("offset").getAsInt() : 0);
                case "protected_blocks" -> Processors.protectedBlocks(holderSet(object.get("value")));
                case "rule" -> Processors.rule(rules(object.getAsJsonArray("rules"), origin));
                case "capped" -> Processors.capped(
                        parse(object.getAsJsonObject("delegate"), origin),
                        IntProvider.parse(object.get("limit")));
                case "lava_submerged_block" -> Processors.lavaSubmerged(validator);
                default -> {
                    problems.warn("Processor type minecraft:" + type + " (in " + origin + ") is not supported, it is ignored");
                    yield Processors.NOP;
                }
            };
        } catch (final RuntimeException invalid) {
            problems.warn("Processor " + type + " in " + origin + " is invalid: " + invalid.getMessage());
            return Processors.NOP;
        }
    }

    private List<Processors.Rule> rules(final @Nullable JsonArray array, final String origin) {
        final List<Processors.Rule> rules = new ArrayList<>();
        if (array == null) {
            return rules;
        }
        for (final JsonElement element : array) {
            final JsonObject rule = element.getAsJsonObject();
            final BlockState output = StateParser.resolve(StateParser.parseJson(rule.get("output_state")), validator);
            if (output == null) {
                problems.warn("A rule of " + origin + " has an unknown output_state, the rule is skipped");
                continue;
            }
            rules.add(new Processors.Rule(
                    ruleTest(rule.getAsJsonObject("input_predicate"), origin),
                    ruleTest(rule.getAsJsonObject("location_predicate"), origin),
                    posRuleTest(rule.has("position_predicate") ? rule.getAsJsonObject("position_predicate") : null),
                    output,
                    modifier(rule.has("block_entity_modifier") ? rule.getAsJsonObject("block_entity_modifier") : null)));
        }
        return rules;
    }

    private RuleTest ruleTest(final @Nullable JsonObject object, final String origin) {
        if (object == null) {
            return RuleTest.ALWAYS_TRUE;
        }
        final String type = string(object, "predicate_type", "minecraft:always_true").replace("minecraft:", "");
        return switch (type) {
            case "always_true" -> RuleTest.ALWAYS_TRUE;
            case "block_match" -> RuleTest.block(Keys.parseOr(string(object, "block", "air"), Keys.AIR));
            case "random_block_match" -> RuleTest.randomBlock(
                    Keys.parseOr(string(object, "block", "air"), Keys.AIR), object.get("probability").getAsFloat());
            case "blockstate_match", "random_blockstate_match" -> {
                final BlockState state = StateParser.resolve(StateParser.parseJson(object.get("block_state")), validator);
                if (state == null) {
                    yield (s, r) -> false;
                }
                yield type.startsWith("random")
                        ? RuleTest.randomBlockState(state, object.get("probability").getAsFloat())
                        : RuleTest.blockState(state);
            }
            case "tag_match" -> RuleTest.tag(tag(string(object, "tag", "")));
            default -> {
                problems.warn("Rule test minecraft:" + type + " (in " + origin + ") is not supported, it never matches");
                yield (s, r) -> false;
            }
        };
    }

    private static PosRuleTest posRuleTest(final @Nullable JsonObject object) {
        if (object == null) {
            return PosRuleTest.ALWAYS_TRUE;
        }
        final String type = string(object, "predicate_type", "minecraft:always_true").replace("minecraft:", "");
        final float minChance = object.has("min_chance") ? object.get("min_chance").getAsFloat() : 0.0F;
        final float maxChance = object.has("max_chance") ? object.get("max_chance").getAsFloat() : 0.0F;
        final int minDist = object.has("min_dist") ? object.get("min_dist").getAsInt() : 0;
        final int maxDist = object.has("max_dist") ? object.get("max_dist").getAsInt() : 0;
        return switch (type) {
            case "linear_pos" -> PosRuleTest.linear(minChance, maxChance, minDist, maxDist);
            case "axis_aligned_linear_pos" -> PosRuleTest.axisAligned(
                    string(object, "axis", "y").charAt(0), minChance, maxChance, minDist, maxDist);
            default -> PosRuleTest.ALWAYS_TRUE;
        };
    }

    private BlockEntityModifier modifier(final @Nullable JsonObject object) {
        if (object == null) {
            return BlockEntityModifier.PASSTHROUGH;
        }
        final String type = string(object, "type", "minecraft:passthrough").replace("minecraft:", "");
        return switch (type) {
            case "clear" -> BlockEntityModifier.CLEAR;
            case "append_static" -> BlockEntityModifier.appendStatic(JsonNbt.compound(object.get("data")));
            case "append_loot" -> BlockEntityModifier.appendLoot(Keys.parseOr(string(object, "loot_table", "empty"), Keys.EMPTY));
            default -> BlockEntityModifier.PASSTHROUGH;
        };
    }

    private Set<Key> blockNames(final @Nullable JsonArray array) {
        final Set<Key> names = new HashSet<>();
        if (array == null) {
            return names;
        }
        for (final JsonElement element : array) {
            final StateParser.Parsed parsed = StateParser.parseJson(element);
            if (parsed != null) {
                names.add(parsed.name());
            }
        }
        return names;
    }

    private Set<Key> holderSet(final JsonElement element) {
        if (element.isJsonArray()) {
            final Set<Key> names = new HashSet<>();
            for (final JsonElement child : element.getAsJsonArray()) {
                names.addAll(holderSet(child));
            }
            return names;
        }
        final String raw = element.getAsString();
        if (raw.startsWith("#")) {
            return tag(raw.substring(1));
        }
        final Key key = Keys.parse(raw);
        return key == null ? Set.of() : Set.of(key);
    }

    private Set<Key> tag(final String raw) {
        final Key key = Keys.parse(raw.startsWith("#") ? raw.substring(1) : raw);
        return key == null ? Set.of() : blockTags.apply(key);
    }

    private static String string(final JsonObject object, final String member, final String fallback) {
        final JsonElement element = object.get(member);
        return element == null || element.isJsonNull() ? fallback : element.getAsString();
    }

    public static Map<String, String> noProperties() {
        return Map.of();
    }
}
