package fr.euphyllia.fidorial.server.world.block.blockentity;

import fr.euphyllia.fidorial.server.registry.data.BlockEntityTypeIds;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BlockEntityTypeKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves which {@code minecraft:block_entity_type} a block carries.
 *
 * <p>Mojang's registry report exposes block entity <em>types</em> and their
 * protocol IDs, but not the block &rarr; block entity association: that link
 * lives in code. This class rebuilds it from an explicit table for one-off
 * blocks plus a handful of suffix rules covering the large colour/wood
 * families (signs, beds, banners, shulker boxes, &hellip;).</p>
 *
 * <p>Protocol IDs are never hard-coded here: they are read from the generated
 * {@link BlockEntityTypeIds} lookup table, so a Minecraft update that reorders
 * the registry is picked up automatically.</p>
 *
 * @since 0.1.0
 */
public final class BlockEntityTypes {

    /**
     * Suffix rules, evaluated in insertion order. The first match wins, so the
     * more specific suffixes must be declared first.
     */
    private static final Map<String, TypedKey<BlockEntityType>> SUFFIX_RULES = suffixRules();

    private static final Map<TypedKey<BlockType>, TypedKey<BlockEntityType>> EXACT_BLOCKS = exactBlocks();

    /**
     * Blocks whose name matches a suffix rule but which carry no block entity.
     */
    private static final Set<TypedKey<BlockType>> SUFFIX_EXCLUSIONS = Set.of(BlockTypeKeys.PISTON_HEAD);

    /**
     * Sentinel stored in {@link #CACHE} for blocks resolved to carry no block
     * entity.
     */
    private static final TypedKey<BlockEntityType> NONE = TypedKey.withRegistry(RegistryKey.BLOCK_ENTITY_TYPE, "fidorial:none");

    /**
     * Memorizes {@link #computeResolution(TypedKey)} per block.
     */
    private static final Map<TypedKey<BlockType>, TypedKey<BlockEntityType>> CACHE = new ConcurrentHashMap<>();

    private BlockEntityTypes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the block entity type identifier carried by a block.
     *
     * @param blockIdentifier the block, e.g. {@code minecraft:chest}
     *
     * @return the block entity type identifier, or {@link Optional#empty()} when
     *         the block has no block entity
     */
    public static Optional<TypedKey<BlockEntityType>> typeIdentifier(final TypedKey<BlockType> blockIdentifier) {
        final TypedKey<BlockEntityType> resolved = resolve(blockIdentifier);
        return resolved == NONE ? Optional.empty() : Optional.of(resolved);
    }

    /**
     * Returns the protocol ID of the block entity carried by a block.
     *
     * @param blockIdentifier the block
     *
     * @return the protocol ID, or {@link BlockEntityTypeIds#UNKNOWN} when the
     *         block carries no block entity, or when the resolved type is absent
     *         from the current registry
     */
    public static int protocolId(final TypedKey<BlockType> blockIdentifier) {
        final TypedKey<BlockEntityType> resolved = resolve(blockIdentifier);
        return resolved == NONE ? BlockEntityTypeIds.UNKNOWN : BlockEntityTypeIds.id(resolved.key());
    }

    /**
     * Returns whether a block carries a block entity known to the current registry.
     *
     * @param blockIdentifier the block
     *
     * @return {@code true} when a protocol ID could be resolved
     */
    public static boolean hasBlockEntity(final TypedKey<BlockType> blockIdentifier) {
        return protocolId(blockIdentifier) != BlockEntityTypeIds.UNKNOWN;
    }

    /**
     * Resolves and caches the block entity type identifier for a block, or
     * {@link #NONE} when it carries none.
     */
    private static TypedKey<BlockEntityType> resolve(final TypedKey<BlockType> blockIdentifier) {
        final TypedKey<BlockEntityType> cached = CACHE.get(blockIdentifier);
        if (cached != null) {
            return cached;
        }
        final TypedKey<BlockEntityType> computed = computeResolution(blockIdentifier);
        CACHE.put(blockIdentifier, computed);
        return computed;
    }

    /**
     * Performs the actual resolution uncached.
     */
    private static TypedKey<BlockEntityType> computeResolution(final TypedKey<BlockType> blockIdentifier) {
        final TypedKey<BlockEntityType> exact = EXACT_BLOCKS.get(blockIdentifier);
        if (exact != null) {
            return exact;
        }

        if (SUFFIX_EXCLUSIONS.contains(blockIdentifier)) {
            return NONE;
        }

        final String value = blockIdentifier.key().value();
        for (final Map.Entry<String, TypedKey<BlockEntityType>> rule : SUFFIX_RULES.entrySet()) {
            if (value.endsWith(rule.getKey())) {
                return rule.getValue();
            }
        }

        return NONE;
    }

    private static Map<String, TypedKey<BlockEntityType>> suffixRules() {

        final Map<String, TypedKey<BlockEntityType>> rules = new LinkedHashMap<>();

        rules.put("_hanging_sign", BlockEntityTypeKeys.HANGING_SIGN);
        rules.put("_wall_sign", BlockEntityTypeKeys.SIGN);
        rules.put("_sign", BlockEntityTypeKeys.SIGN);
        rules.put("_bed", TypedKey.withRegistry(RegistryKey.BLOCK_ENTITY_TYPE, "bed"));
        rules.put("_banner", BlockEntityTypeKeys.BANNER);
        rules.put("_shulker_box", BlockEntityTypeKeys.SHULKER_BOX);
        rules.put("_skull", BlockEntityTypeKeys.SKULL);
        rules.put("_head", BlockEntityTypeKeys.SKULL);
        rules.put("_campfire", BlockEntityTypeKeys.CAMPFIRE);
        rules.put("_beehive", BlockEntityTypeKeys.BEEHIVE);
        rules.put("_command_block", BlockEntityTypeKeys.COMMAND_BLOCK);

        /*
         * Iteration order is significant, so the map cannot be copied into an
         * unordered Map.copyOf view.
         */
        return Collections.unmodifiableMap(rules);
    }

    private static Map<TypedKey<BlockType>, TypedKey<BlockEntityType>> exactBlocks() {

        final Map<TypedKey<BlockType>, TypedKey<BlockEntityType>> blocks = new LinkedHashMap<>();

        put(blocks, "barrel", "barrel");
        put(blocks, "beacon", "beacon");
        put(blocks, "bee_nest", "beehive");
        put(blocks, "beehive", "beehive");
        put(blocks, "bell", "bell");
        put(blocks, "blast_furnace", "blast_furnace");
        put(blocks, "brewing_stand", "brewing_stand");
        put(blocks, "calibrated_sculk_sensor", "calibrated_sculk_sensor");
        put(blocks, "campfire", "campfire");
        put(blocks, "chest", "chest");
        put(blocks, "chiseled_bookshelf", "chiseled_bookshelf");
        put(blocks, "command_block", "command_block");
        put(blocks, "comparator", "comparator");
        put(blocks, "conduit", "conduit");
        put(blocks, "crafter", "crafter");
        put(blocks, "creaking_heart", "creaking_heart");
        put(blocks, "daylight_detector", "daylight_detector");
        put(blocks, "decorated_pot", "decorated_pot");
        put(blocks, "dispenser", "dispenser");
        put(blocks, "dropper", "dropper");
        put(blocks, "enchanting_table", "enchanting_table");
        put(blocks, "end_gateway", "end_gateway");
        put(blocks, "end_portal", "end_portal");
        put(blocks, "ender_chest", "ender_chest");
        put(blocks, "furnace", "furnace");
        put(blocks, "hopper", "hopper");
        put(blocks, "jigsaw", "jigsaw");
        put(blocks, "jukebox", "jukebox");
        put(blocks, "lectern", "lectern");
        put(blocks, "moving_piston", "piston");
        put(blocks, "sculk_catalyst", "sculk_catalyst");
        put(blocks, "sculk_sensor", "sculk_sensor");
        put(blocks, "sculk_shrieker", "sculk_shrieker");
        put(blocks, "shulker_box", "shulker_box");
        put(blocks, "skull", "skull");
        put(blocks, "smoker", "smoker");
        put(blocks, "soul_campfire", "campfire");
        put(blocks, "spawner", "mob_spawner");
        put(blocks, "structure_block", "structure_block");
        put(blocks, "suspicious_gravel", "brushable_block");
        put(blocks, "suspicious_sand", "brushable_block");
        put(blocks, "test_block", "test_block");
        put(blocks, "test_instance_block", "test_instance_block");
        put(blocks, "trapped_chest", "trapped_chest");
        put(blocks, "trial_spawner", "trial_spawner");
        put(blocks, "vault", "vault");

        return Map.copyOf(blocks);
    }

    private static void put(final Map<TypedKey<BlockType>, TypedKey<BlockEntityType>> blocks, final String block, final String blockEntityType) {
        blocks.put(TypedKey.withRegistry(RegistryKey.BLOCK, block), TypedKey.withRegistry(RegistryKey.BLOCK_ENTITY_TYPE, blockEntityType));
    }
}
