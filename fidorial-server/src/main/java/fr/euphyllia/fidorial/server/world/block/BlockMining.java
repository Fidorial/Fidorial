package fr.euphyllia.fidorial.server.world.block;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.world.block.BlockType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * How hard each block is to mine, how fast each tool mines it and what it takes for it to drop
 * anything, read from the {@code block_mining.json} dataset written by the
 * {@code generateBlockMining} task.
 */
public final class BlockMining {

    public static final String RESOURCE = "/fidorial-data/block_mining.json";

    private static final ComponentLogger LOGGER = ComponentLogger.logger(BlockMining.class);

    /**
     * The hardness of a block the dataset does not know: it takes time to mine.
     */
    private static final float UNKNOWN_HARDNESS = 1.0f;

    /**
     * How fast an item mines a block no rule of its {@code minecraft:tool} component names, and a
     * bare hand any block.
     */
    private static final float DEFAULT_SPEED = 1.0f;

    private static volatile Dataset dataset = Dataset.EMPTY;

    private BlockMining() {
        throw new UnsupportedOperationException("BlockMining cannot be instantiated.");
    }

    /**
     * Reads the bundled dataset. Without it, no block is mined at once and every block drops its
     * loot whatever breaks it.
     */
    public static void bootstrap() {
        try (final InputStream input = BlockMining.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                LOGGER.warn("Missing resource {}: no block will be mined at once. "
                        + "Run ':fidorial-server:generateBlockMining'.", RESOURCE);
                return;
            }
            dataset = load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (final IOException exception) {
            throw new UncheckedIOException("Failed to load " + RESOURCE, exception);
        }
    }

    static Dataset load(final Reader reader) {
        final JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        final Map<TypedKey<BlockType>, Float> hardness = new HashMap<>();
        final Set<TypedKey<BlockType>> requiresTool = new HashSet<>();
        final JsonObject blocks = root.getAsJsonObject("blocks");
        if (blocks != null) {
            for (final Map.Entry<String, JsonElement> entry : blocks.entrySet()) {
                final TypedKey<BlockType> block = TypedKey.withRegistry(RegistryKey.BLOCK, entry.getKey());
                final JsonObject definition = entry.getValue().getAsJsonObject();
                final JsonElement value = definition.get("hardness");
                if (value != null) {
                    hardness.put(block, value.getAsFloat());
                }
                final JsonElement tool = definition.get("requires_tool");
                if (tool != null && tool.getAsBoolean()) {
                    requiresTool.add(block);
                }
            }
        }
        final Map<TypedKey<Item>, List<ToolRule>> tools = new HashMap<>();
        final JsonObject components = root.getAsJsonObject("tools");
        if (components != null) {
            for (final Map.Entry<String, JsonElement> entry : components.entrySet()) {
                final List<ToolRule> rules = rules(entry.getValue().getAsJsonObject());
                if (!rules.isEmpty()) {
                    tools.put(TypedKey.withRegistry(RegistryKey.ITEM, entry.getKey()), rules);
                }
            }
        }
        LOGGER.debug("Read the hardness of {} blocks, {} needing a tool to drop anything, and the rules of {} tools",
                hardness.size(), requiresTool.size(), tools.size());
        return new Dataset(Map.copyOf(hardness), Set.copyOf(requiresTool), Map.copyOf(tools));
    }

    /**
     * Reads the rules of a {@code minecraft:tool} component that say whether the tool is correct
     * for the drops of the blocks they name, or how fast it mines them; the rules saying neither are
     * left out.
     */
    private static List<ToolRule> rules(final JsonObject component) {
        final JsonElement rules = component.get("rules");
        if (rules == null || !rules.isJsonArray()) {
            return List.of();
        }
        final List<ToolRule> read = new ArrayList<>();
        for (final JsonElement element : rules.getAsJsonArray()) {
            final JsonObject rule = element.getAsJsonObject();
            final JsonElement correct = rule.get("correct_for_drops");
            final JsonElement speed = rule.get("speed");
            final JsonElement blocks = rule.get("blocks");
            if (blocks == null || (correct == null && speed == null)) {
                continue;
            }
            read.add(ToolRule.of(blocks, correct == null ? null : correct.getAsBoolean(),
                    speed == null ? null : speed.getAsFloat()));
        }
        return List.copyOf(read);
    }

    /**
     * {@return the hardness of a block: {@code 0} for one breaking at a touch, negative for one no
     * tool breaks}
     *
     * @param block the block type
     */
    public static float hardness(final TypedKey<BlockType> block) {
        return dataset.hardness().getOrDefault(block, UNKNOWN_HARDNESS);
    }

    /**
     * {@return {@code true} if a block breaks as soon as a player starts mining it, whatever they
     * hold}: a crop, a flower, a torch
     *
     * @param block the block type
     */
    public static boolean breaksInstantly(final TypedKey<BlockType> block) {
        return hardness(block) == 0.0f;
    }

    /**
     * {@return {@code true} if a block only drops something when broken with the right tool}: stone
     * and ores without a pickaxe, cobwebs without a sword or shears; {@code false} for a block the
     * dataset does not know
     *
     * @param block the block type
     */
    public static boolean requiresTool(final TypedKey<BlockType> block) {
        return dataset.requiresTool().contains(block);
    }

    /**
     * {@return {@code true} if a block broken with an item may drop its loot}: a block needing no
     * tool always may; otherwise the first rule of the {@code minecraft:tool} component of the item
     * that names the block and says whether the tool is correct for its drops decides, and an item
     * without such a rule, an empty hand among them, never is
     *
     * @param block     the block type being broken
     * @param tool      what breaks it, {@link ItemStack#EMPTY} for an empty hand
     * @param blockTags whether a block type belongs to a tag
     */
    public static boolean canHarvest(final TypedKey<BlockType> block, final ItemStack tool,
                                     final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags) {
        if (!requiresTool(block)) {
            return true;
        }
        if (tool.isEmpty()) {
            return false;
        }
        for (final ToolRule rule : dataset.tools().getOrDefault(tool.id(), List.of())) {
            final Boolean correct = rule.correctForDrops();
            if (correct != null && rule.matches(block, blockTags)) {
                return correct;
            }
        }
        return false;
    }

    /**
     * {@return how fast an item mines a block, before what the player adds to it}: the speed of the
     * first rule of the {@code minecraft:tool} component of the item that names the block and gives
     * a speed; {@code 1} for an item without such a rule, an empty hand among them
     *
     * @param block     the block type being mined
     * @param tool      what mines it, {@link ItemStack#EMPTY} for an empty hand
     * @param blockTags whether a block type belongs to a tag
     */
    public static float toolSpeed(final TypedKey<BlockType> block, final ItemStack tool,
                                  final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags) {
        if (tool.isEmpty()) {
            return DEFAULT_SPEED;
        }
        for (final ToolRule rule : dataset.tools().getOrDefault(tool.id(), List.of())) {
            final Float speed = rule.speed();
            if (speed != null && rule.matches(block, blockTags)) {
                return speed;
            }
        }
        return DEFAULT_SPEED;
    }

    /**
     * What the dataset holds.
     *
     * @param hardness     the hardness of each block
     * @param requiresTool the blocks that only drop something when broken with the right tool
     * @param tools        the rules of the tools that are, or are not, correct for some drops, or
     *                     that mine some blocks faster
     */
    record Dataset(Map<TypedKey<BlockType>, Float> hardness, Set<TypedKey<BlockType>> requiresTool,
                   Map<TypedKey<Item>, List<ToolRule>> tools) {

        static final Dataset EMPTY = new Dataset(Map.of(), Set.of(), Map.of());
    }

    /**
     * A rule of a {@code minecraft:tool} component that says whether the tool is correct for the
     * drops of some blocks, how fast it mines them, or both.
     *
     * @param tag             the tag of the blocks the rule is about, {@code null} when it names
     *                        them one by one
     * @param blocks          the blocks the rule names one by one, empty when it names a tag
     * @param correctForDrops whether the tool is correct for the drops of these blocks,
      *                        {@code null} when the rule does not say
      * @param speed           how fast the tool mines these blocks, {@code null} when the rule does
     *                        not say
     */
    record ToolRule(@Nullable TagKey<BlockType> tag, Set<TypedKey<BlockType>> blocks,
                    @Nullable Boolean correctForDrops, @Nullable Float speed) {

        /**
         * Reads the {@code blocks} of a rule: a tag ({@code #minecraft:mineable/pickaxe}), one
         * block, or a list of blocks.
         */
        static ToolRule of(final JsonElement blocks, final @Nullable Boolean correctForDrops,
                           final @Nullable Float speed) {
            if (blocks.isJsonArray()) {
                final Set<TypedKey<BlockType>> named = new HashSet<>();
                for (final JsonElement block : blocks.getAsJsonArray()) {
                    named.add(TypedKey.withRegistry(RegistryKey.BLOCK, block.getAsString()));
                }
                return new ToolRule(null, Set.copyOf(named), correctForDrops, speed);
            }
            final String name = blocks.getAsString();
            if (name.startsWith("#")) {
                return new ToolRule(TagKey.withRegistry(RegistryKey.BLOCK, name.substring(1)), Set.of(),
                        correctForDrops, speed);
            }
            return new ToolRule(null, Set.of(TypedKey.withRegistry(RegistryKey.BLOCK, name)), correctForDrops, speed);
        }

        boolean matches(final TypedKey<BlockType> block,
                        final BiPredicate<TypedKey<BlockType>, TagKey<BlockType>> blockTags) {
            return tag != null ? blockTags.test(block, tag) : blocks.contains(block);
        }
    }
}
