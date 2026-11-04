package fr.euphyllia.fidorial.server.registry.data;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BlockEntityTypeKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;

/**
 * The block entity type each block carries.
 *
 * <p>Joined from Mojang's registry report and PrismarineJS's
 * {@code minecraft-data} block entities report; do not edit.</p>
 */
public final class BlockEntityTypeBlocks {
    private static final Object2ObjectOpenHashMap<TypedKey<BlockType>, TypedKey<BlockEntityType>> BLOCK_ENTITY_TYPE = new Object2ObjectOpenHashMap<>();

    private BlockEntityTypeBlocks() {
        throw new UnsupportedOperationException("BlockEntityTypeBlocks cannot be instantiated.");
    }

    /**
     * @param block the block
     * @return the block entity type it carries, {@code null} when it carries none
     */
    public static @Nullable TypedKey<BlockEntityType> blockEntityType(
            final TypedKey<BlockType> block) {
        return BLOCK_ENTITY_TYPE.get(block);
    }

    private static void registerBlockEntityTypes0() {
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.FURNACE, BlockEntityTypeKeys.FURNACE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.EXPOSED_COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WEATHERED_COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.OXIDIZED_COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_EXPOSED_COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_WEATHERED_COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_OXIDIZED_COPPER_CHEST, BlockEntityTypeKeys.CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.TRAPPED_CHEST, BlockEntityTypeKeys.TRAPPED_CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ENDER_CHEST, BlockEntityTypeKeys.ENDER_CHEST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.JUKEBOX, BlockEntityTypeKeys.JUKEBOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DISPENSER, BlockEntityTypeKeys.DISPENSER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DROPPER, BlockEntityTypeKeys.DROPPER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.OAK_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SPRUCE_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BIRCH_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ACACIA_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHERRY_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.JUNGLE_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DARK_OAK_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PALE_OAK_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MANGROVE_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.POPLAR_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BAMBOO_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.OAK_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SPRUCE_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BIRCH_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ACACIA_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHERRY_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.JUNGLE_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DARK_OAK_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PALE_OAK_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MANGROVE_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.POPLAR_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BAMBOO_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CRIMSON_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WARPED_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CRIMSON_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WARPED_WALL_SIGN, BlockEntityTypeKeys.SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.OAK_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SPRUCE_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BIRCH_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ACACIA_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHERRY_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.JUNGLE_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DARK_OAK_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PALE_OAK_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CRIMSON_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WARPED_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MANGROVE_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.POPLAR_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BAMBOO_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.OAK_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SPRUCE_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BIRCH_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ACACIA_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHERRY_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.JUNGLE_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DARK_OAK_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PALE_OAK_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MANGROVE_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.POPLAR_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CRIMSON_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WARPED_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BAMBOO_WALL_HANGING_SIGN, BlockEntityTypeKeys.HANGING_SIGN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SPAWNER, BlockEntityTypeKeys.MOB_SPAWNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CREAKING_HEART, BlockEntityTypeKeys.CREAKING_HEART);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MOVING_PISTON, BlockEntityTypeKeys.PISTON);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BREWING_STAND, BlockEntityTypeKeys.BREWING_STAND);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ENCHANTING_TABLE, BlockEntityTypeKeys.ENCHANTING_TABLE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.END_PORTAL, BlockEntityTypeKeys.END_PORTAL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BEACON, BlockEntityTypeKeys.BEACON);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SKELETON_SKULL, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SKELETON_WALL_SKULL, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WITHER_SKELETON_SKULL, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WITHER_SKELETON_WALL_SKULL, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ZOMBIE_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ZOMBIE_WALL_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PLAYER_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PLAYER_WALL_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CREEPER_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CREEPER_WALL_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DRAGON_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DRAGON_WALL_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PIGLIN_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PIGLIN_WALL_HEAD, BlockEntityTypeKeys.SKULL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DAYLIGHT_DETECTOR, BlockEntityTypeKeys.DAYLIGHT_DETECTOR);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.HOPPER, BlockEntityTypeKeys.HOPPER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.COMPARATOR, BlockEntityTypeKeys.COMPARATOR);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WHITE_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ORANGE_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MAGENTA_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIGHT_BLUE_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.YELLOW_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIME_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PINK_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.GRAY_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIGHT_GRAY_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CYAN_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PURPLE_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BLUE_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BROWN_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.GREEN_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.RED_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BLACK_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WHITE_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ORANGE_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MAGENTA_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIGHT_BLUE_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.YELLOW_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIME_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PINK_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.GRAY_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIGHT_GRAY_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CYAN_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PURPLE_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BLUE_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BROWN_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.GREEN_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.RED_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BLACK_WALL_BANNER, BlockEntityTypeKeys.BANNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.STRUCTURE_BLOCK, BlockEntityTypeKeys.STRUCTURE_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.END_GATEWAY, BlockEntityTypeKeys.END_GATEWAY);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.COMMAND_BLOCK, BlockEntityTypeKeys.COMMAND_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.REPEATING_COMMAND_BLOCK, BlockEntityTypeKeys.COMMAND_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHAIN_COMMAND_BLOCK, BlockEntityTypeKeys.COMMAND_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WHITE_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ORANGE_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MAGENTA_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIGHT_BLUE_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.YELLOW_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIME_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PINK_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.GRAY_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LIGHT_GRAY_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CYAN_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PURPLE_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BLUE_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BROWN_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.GREEN_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.RED_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BLACK_SHULKER_BOX, BlockEntityTypeKeys.SHULKER_BOX);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CONDUIT, BlockEntityTypeKeys.CONDUIT);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BARREL, BlockEntityTypeKeys.BARREL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SMOKER, BlockEntityTypeKeys.SMOKER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BLAST_FURNACE, BlockEntityTypeKeys.BLAST_FURNACE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.LECTERN, BlockEntityTypeKeys.LECTERN);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BELL, BlockEntityTypeKeys.BELL);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.JIGSAW, BlockEntityTypeKeys.JIGSAW);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CAMPFIRE, BlockEntityTypeKeys.CAMPFIRE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SOUL_CAMPFIRE, BlockEntityTypeKeys.CAMPFIRE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BEE_NEST, BlockEntityTypeKeys.BEEHIVE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BEEHIVE, BlockEntityTypeKeys.BEEHIVE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SCULK_SENSOR, BlockEntityTypeKeys.SCULK_SENSOR);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CALIBRATED_SCULK_SENSOR, BlockEntityTypeKeys.CALIBRATED_SCULK_SENSOR);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SCULK_CATALYST, BlockEntityTypeKeys.SCULK_CATALYST);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SCULK_SHRIEKER, BlockEntityTypeKeys.SCULK_SHRIEKER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHISELED_BOOKSHELF, BlockEntityTypeKeys.CHISELED_BOOKSHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.ACACIA_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BAMBOO_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.BIRCH_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CHERRY_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CRIMSON_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DARK_OAK_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.JUNGLE_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.MANGROVE_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.OAK_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.PALE_OAK_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.POPLAR_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SPRUCE_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WARPED_SHELF, BlockEntityTypeKeys.SHELF);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SUSPICIOUS_SAND, BlockEntityTypeKeys.BRUSHABLE_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.SUSPICIOUS_GRAVEL, BlockEntityTypeKeys.BRUSHABLE_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.DECORATED_POT, BlockEntityTypeKeys.DECORATED_POT);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.CRAFTER, BlockEntityTypeKeys.CRAFTER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.TRIAL_SPAWNER, BlockEntityTypeKeys.TRIAL_SPAWNER);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.VAULT, BlockEntityTypeKeys.VAULT);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.TEST_BLOCK, BlockEntityTypeKeys.TEST_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.TEST_INSTANCE_BLOCK, BlockEntityTypeKeys.TEST_INSTANCE_BLOCK);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.EXPOSED_COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WEATHERED_COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.OXIDIZED_COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_EXPOSED_COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_WEATHERED_COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.WAXED_OXIDIZED_COPPER_GOLEM_STATUE, BlockEntityTypeKeys.COPPER_GOLEM_STATUE);
        BLOCK_ENTITY_TYPE.put(BlockTypeKeys.POTENT_SULFUR, BlockEntityTypeKeys.POTENT_SULFUR);
    }

    /**
     * Fills the lookup table. Called once during server start-up;
     * calling it again is harmless but pointless.
     */
    public static void bootstrap() {
        registerBlockEntityTypes0();
    }
}
