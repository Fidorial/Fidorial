package fr.euphyllia.fidorial.server.world.generation.material;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeClimate;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.data.HeightContext;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.euphyllia.fidorial.server.world.generation.density.DensityProgram;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseField;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import fr.euphyllia.fidorial.server.world.generation.random.XoroshiroSource;
import fr.euphyllia.fidorial.server.world.generation.value.Values;
import fr.fidorial.registry.keys.BlockTypeKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compiles the material rules of a noise settings into evaluators bound to one {@link MaterialContext}. A rule
 * answers the block of the current position, or nothing; a sequence answers with its first rule that does.
 *
 * <p>Conditions that only depend on the column, or on the block, remember their answer until the context moves:
 * most of the tree is shared between the blocks of a column.</p>
 */
public final class MaterialRules {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(MaterialRules.class);

    private static final Key CLAY_BANDS_RANDOM = Key.key("clay_bands");
    private static final Key ORE_RANDOM = Key.key("ore");

    /**
     * A compiled rule.
     */
    @FunctionalInterface
    public interface Rule {
        /**
         * {@return the block at the current position of the context, or {@code null} when the rule does not
         * apply there}
         */
        @Nullable BlockState apply();
    }

    @FunctionalInterface
    interface Condition {
        boolean test();
    }

    private final WorldgenData data;
    private final DensityProgram program;
    private final BlockLookup blocks;
    private final BiomeClimate climate;
    private final HeightContext heights;
    private final PositionalRandom worldRandom;
    private final MaterialContext context;
    private final Set<String> warned = new HashSet<>();

    /**
     * @param context the context the compiled rules read
     */
    public MaterialRules(final WorldgenData data, final DensityProgram program, final BlockLookup blocks, final BiomeClimate climate,
                         final PositionalRandom worldRandom, final MaterialContext context) {
        this.data = data;
        this.program = program;
        this.blocks = blocks;
        this.climate = climate;
        this.heights = program.heights();
        this.worldRandom = worldRandom;
        this.context = context;
    }

    /**
     * {@return the layers of the badlands for a world, bottom first: terracotta with bands of colours}
     */
    public static BlockState[] clayBands(final PositionalRandom random, final BlockLookup blocks) {
        final XoroshiroSource draw = random.named(CLAY_BANDS_RANDOM);
        final BlockState plain = blocks.state(BlockTypeKeys.TERRACOTTA);
        final BlockState[] bands = new BlockState[192];
        Arrays.fill(bands, plain);
        for (int i = 0; i < bands.length; i++) {
            i += draw.nextInt(5) + 1;
            if (i < bands.length) {
                bands[i] = blocks.state(BlockTypeKeys.ORANGE_TERRACOTTA);
            }
        }
        paintBands(draw, bands, 1, blocks.state(BlockTypeKeys.YELLOW_TERRACOTTA));
        paintBands(draw, bands, 2, blocks.state(BlockTypeKeys.BROWN_TERRACOTTA));
        paintBands(draw, bands, 1, blocks.state(BlockTypeKeys.RED_TERRACOTTA));
        final int whiteCount = draw.nextInt(15 - 9 + 1) + 9;
        final BlockState white = blocks.state(BlockTypeKeys.WHITE_TERRACOTTA);
        final BlockState lightGray = blocks.state(BlockTypeKeys.LIGHT_GRAY_TERRACOTTA);
        int painted = 0;
        for (int start = 0; painted < whiteCount && start < bands.length; start += draw.nextInt(16) + 4) {
            bands[start] = white;
            if (start - 1 > 0 && draw.nextBoolean()) {
                bands[start - 1] = lightGray;
            }
            if (start + 1 < bands.length && draw.nextBoolean()) {
                bands[start + 1] = lightGray;
            }
            painted++;
        }
        return bands;
    }

    private static void paintBands(final XoroshiroSource draw, final BlockState[] bands, final int baseWidth, final BlockState state) {
        final int count = draw.nextInt(15 - 6 + 1) + 6;
        for (int i = 0; i < count; i++) {
            final int width = baseWidth + draw.nextInt(3);
            final int start = draw.nextInt(bands.length);
            for (int p = 0; start + p < bands.length && p < width; p++) {
                bands[start + p] = state;
            }
        }
    }

    private void warnOnce(final String message) {
        if (warned.add(message)) {
            LOGGER.warn("[worldgen] {}", message);
        }
    }

    /**
     * {@return a rule: the identifier of a named rule, or an inline definition}
     */
    public Rule rule(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return rule(data.get(WorldgenData.Section.MATERIAL_RULE, element.getAsString()));
        }
        final JsonObject object = element.getAsJsonObject();
        final String type = Values.type(object);
        return switch (type) {
            case "block" -> {
                final BlockState state = blocks.parse(object.get("result_state"));
                yield () -> state;
            }
            case "bandlands" -> () -> context.system().band(context.x, context.y, context.z);
            case "sequence" -> sequence(object.getAsJsonArray("sequence"));
            case "condition" -> {
                final Condition condition = condition(object.get("if_true"));
                final Rule then = rule(object.get("then_run"));
                yield () -> condition.test() ? then.apply() : null;
            }
            case "ore_vein" -> oreVein(object);
            default -> {
                warnOnce("The material rule type " + type + " is not supported, it places nothing");
                yield () -> null;
            }
        };
    }

    private Rule sequence(final JsonArray array) {
        final List<Rule> rules = new ArrayList<>(array.size());
        for (final JsonElement element : array) {
            rules.add(rule(element));
        }
        final Rule[] ordered = rules.toArray(Rule[]::new);
        if (ordered.length == 1) {
            return ordered[0];
        }
        return () -> {
            for (final Rule rule : ordered) {
                final BlockState state = rule.apply();
                if (state != null) {
                    return state;
                }
            }
            return null;
        };
    }

    private Rule oreVein(final JsonObject object) {
        final BlockState ore = blocks.parse(object.get("ore_block"));
        final BlockState rawOre = blocks.parse(object.get("raw_ore_block"));
        final BlockState filler = blocks.parse(object.get("filler_block"));
        final float rawChance = object.has("raw_ore_chance") ? object.get("raw_ore_chance").getAsFloat() : 0.02F;
        final Density density = program.compile(object.get("density"));
        final Density richness = program.compile(object.get("richness"));
        final Density gap = program.compile(object.get("filler_gap"));
        final PositionalRandom random = worldRandom.named(ORE_RANDOM).splitPositional();
        return () -> {
            final int x = context.x;
            final int y = context.y;
            final int z = context.z;
            final float strength = density.compute(context.densities(), x, y, z);
            if (strength <= 0.0F) {
                return null;
            }
            final XoroshiroSource draw = random.at(x, y, z);
            if (draw.nextFloat() > strength) {
                return null;
            }
            final float rich = richness.compute(context.densities(), x, y, z);
            if (draw.nextFloat() < rich && gap.compute(context.densities(), x, y, z) < 0.0F) {
                return draw.nextFloat() < rawChance ? rawOre : ore;
            }
            return filler;
        };
    }

    /**
     * A condition that only depends on the column: computed once per column.
     */
    private abstract class ColumnCondition implements Condition {
        private long stamp = -1;
        private boolean value;

        @Override
        public final boolean test() {
            if (stamp != context.xzStamp) {
                stamp = context.xzStamp;
                value = compute();
            }
            return value;
        }

        abstract boolean compute();
    }

    /**
     * A condition that depends on the block: computed once per block.
     */
    private abstract class BlockCondition implements Condition {
        private long stamp = -1;
        private boolean value;

        @Override
        public final boolean test() {
            if (stamp != context.yStamp) {
                stamp = context.yStamp;
                value = compute();
            }
            return value;
        }

        abstract boolean compute();
    }

    private Condition condition(final JsonElement element) {
        if (element.isJsonPrimitive()) {
            return condition(data.get(WorldgenData.Section.MATERIAL_CONDITION, element.getAsString()));
        }
        final JsonObject object = element.getAsJsonObject();
        final String type = Values.type(object);
        return switch (type) {
            case "biome" -> biome(object.get("biome_is"));
            case "noise_threshold" -> noiseThreshold(object);
            case "vertical_gradient" -> verticalGradient(object);
            case "y_above" -> {
                final int anchor = heights.resolve(object.get("anchor"));
                final int multiplier = object.get("surface_depth_multiplier").getAsInt();
                final boolean addStone = object.has("add_stone_depth") && object.get("add_stone_depth").getAsBoolean();
                yield new BlockCondition() {
                    @Override
                    boolean compute() {
                        return context.y + (addStone ? context.stoneAbove : 0) >= anchor + context.surfaceDepth * multiplier;
                    }
                };
            }
            case "water" -> {
                final int offset = object.get("offset").getAsInt();
                final int multiplier = object.get("surface_depth_multiplier").getAsInt();
                final boolean addStone = object.has("add_stone_depth") && object.get("add_stone_depth").getAsBoolean();
                yield new BlockCondition() {
                    @Override
                    boolean compute() {
                        return context.waterHeight == Integer.MIN_VALUE
                                || context.y + (addStone ? context.stoneAbove : 0) >= context.waterHeight + offset + context.surfaceDepth * multiplier;
                    }
                };
            }
            case "temperature" -> () -> climate.coldEnoughToSnow(context.biome(), context.x, context.y, context.z, context.system().seaLevel());
            case "steep" -> new ColumnCondition() {
                @Override
                boolean compute() {
                    return context.gradientX <= -4 || context.gradientZ >= 4;
                }
            };
            case "not" -> {
                final Condition inner = condition(object.has("invert") ? object.get("invert") : object.get("target"));
                yield () -> !inner.test();
            }
            case "hole" -> new ColumnCondition() {
                @Override
                boolean compute() {
                    return context.surfaceDepth <= 0;
                }
            };
            case "above_preliminary_surface" -> () -> context.y >= context.minSurfaceLevel();
            case "stone_depth" -> stoneDepth(object);
            default -> {
                warnOnce("The material condition type " + type + " is not supported, it is always false");
                yield () -> false;
            }
        };
    }

    private Condition biome(final JsonElement biomes) {
        final BitSet matching = new BitSet();
        final List<String> ids = new ArrayList<>();
        if (biomes.isJsonArray()) {
            for (final JsonElement biome : biomes.getAsJsonArray()) {
                ids.add(biome.getAsString());
            }
        } else {
            ids.add(biomes.getAsString());
        }
        for (final String id : ids) {
            if (id.startsWith("#")) {
                for (final String member : data.tag(WorldgenData.Section.BIOME_TAG, id)) {
                    final BiomeInfo info = context.system().biomes().find(member);
                    if (info != null) {
                        matching.set(info.index());
                    }
                }
            } else {
                final BiomeInfo info = context.system().biomes().find(id);
                if (info != null) {
                    matching.set(info.index());
                }
            }
        }
        return new BlockCondition() {
            @Override
            boolean compute() {
                return matching.get(context.biome().index());
            }
        };
    }

    private Condition noiseThreshold(final JsonObject object) {
        final NoiseField noise = program.noise(Key.key(object.get("noise").getAsString()));
        final double min = object.get("min_threshold").getAsDouble();
        final double max = object.get("max_threshold").getAsDouble();
        final boolean is3d = object.has("is_3d") && object.get("is_3d").getAsBoolean();
        if (is3d) {
            return new BlockCondition() {
                @Override
                boolean compute() {
                    final double value = noise.sample(context.x, context.y, context.z);
                    return value >= min && value <= max;
                }
            };
        }
        // the noise of a column is shared by the conditions on the same noise
        return new ColumnCondition() {
            @Override
            boolean compute() {
                final double value = noise.sample(context.x, 0.0, context.z);
                return value >= min && value <= max;
            }
        };
    }

    private Condition verticalGradient(final JsonObject object) {
        final int trueAtAndBelow = heights.resolve(object.get("true_at_and_below"));
        final int falseAtAndAbove = heights.resolve(object.get("false_at_and_above"));
        final PositionalRandom random = worldRandom.named(Key.key(object.get("random_name").getAsString())).splitPositional();
        return new BlockCondition() {
            @Override
            boolean compute() {
                final int y = context.y;
                if (y <= trueAtAndBelow) {
                    return true;
                }
                if (y >= falseAtAndAbove) {
                    return false;
                }
                final double probability = 1.0 + (y - (double) trueAtAndBelow) / (falseAtAndAbove - (double) trueAtAndBelow) * (0.0 - 1.0);
                return random.at(context.x, y, context.z).nextFloat() < probability;
            }
        };
    }

    private Condition stoneDepth(final JsonObject object) {
        final int offset = object.get("offset").getAsInt();
        final boolean addSurfaceDepth = object.get("add_surface_depth").getAsBoolean();
        final int secondaryRange = object.get("secondary_depth_range").getAsInt();
        final boolean ceiling = "ceiling".equals(object.get("surface_type").getAsString());
        return new BlockCondition() {
            @Override
            boolean compute() {
                final int stoneDepth = ceiling ? context.stoneBelow : context.stoneAbove;
                final int surface = addSurfaceDepth ? context.surfaceDepth : 0;
                final int secondary = secondaryRange == 0 ? 0
                        : (int) (0.0 + (context.surfaceSecondary() - -1.0) / (1.0 - -1.0) * (secondaryRange - 0.0));
                return stoneDepth <= 1 + offset + surface + secondary;
            }
        };
    }
}
