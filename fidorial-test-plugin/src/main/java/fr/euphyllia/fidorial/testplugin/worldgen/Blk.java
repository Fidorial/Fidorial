package fr.euphyllia.fidorial.testplugin.worldgen;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public final class Blk {

    private static final List<TypedKey<BlockType>> KEYS = new ArrayList<>();
    private static final List<Map<String, String>> PROPERTIES = new ArrayList<>();

    public static final short AIR = register(BlockTypeKeys.AIR);
    public static final short BEDROCK = register(BlockTypeKeys.BEDROCK);
    public static final short STONE = register(BlockTypeKeys.STONE);
    public static final short DEEPSLATE = register(BlockTypeKeys.DEEPSLATE);
    public static final short GRANITE = register(BlockTypeKeys.GRANITE);
    public static final short DIORITE = register(BlockTypeKeys.DIORITE);
    public static final short ANDESITE = register(BlockTypeKeys.ANDESITE);
    public static final short TUFF = register(BlockTypeKeys.TUFF);
    public static final short CALCITE = register(BlockTypeKeys.CALCITE);
    public static final short DRIPSTONE_BLOCK = register(BlockTypeKeys.DRIPSTONE_BLOCK);
    public static final short POINTED_DRIPSTONE = register(BlockTypeKeys.POINTED_DRIPSTONE);
    public static final short OBSIDIAN = register(BlockTypeKeys.OBSIDIAN);
    public static final short MAGMA_BLOCK = register(BlockTypeKeys.MAGMA_BLOCK);

    public static final short DIRT = register(BlockTypeKeys.DIRT);
    public static final short COARSE_DIRT = register(BlockTypeKeys.COARSE_DIRT);
    public static final short ROOTED_DIRT = register(BlockTypeKeys.ROOTED_DIRT);
    public static final short GRASS_BLOCK = register(BlockTypeKeys.GRASS_BLOCK);
    public static final short PODZOL = register(BlockTypeKeys.PODZOL);
    public static final short MYCELIUM = register(BlockTypeKeys.MYCELIUM);
    public static final short MUD = register(BlockTypeKeys.MUD);
    public static final short PACKED_MUD = register(BlockTypeKeys.PACKED_MUD);
    public static final short CLAY = register(BlockTypeKeys.CLAY);
    public static final short SAND = register(BlockTypeKeys.SAND);
    public static final short RED_SAND = register(BlockTypeKeys.RED_SAND);
    public static final short SANDSTONE = register(BlockTypeKeys.SANDSTONE);
    public static final short RED_SANDSTONE = register(BlockTypeKeys.RED_SANDSTONE);
    public static final short GRAVEL = register(BlockTypeKeys.GRAVEL);
    public static final short MOSS_BLOCK = register(BlockTypeKeys.MOSS_BLOCK);
    public static final short MOSS_CARPET = register(BlockTypeKeys.MOSS_CARPET);

    public static final short WATER = register(BlockTypeKeys.WATER);
    public static final short LAVA = register(BlockTypeKeys.LAVA);
    public static final short ICE = register(BlockTypeKeys.ICE);
    public static final short PACKED_ICE = register(BlockTypeKeys.PACKED_ICE);
    public static final short BLUE_ICE = register(BlockTypeKeys.BLUE_ICE);
    public static final short SNOW_BLOCK = register(BlockTypeKeys.SNOW_BLOCK);
    public static final short SNOW = register(BlockTypeKeys.SNOW);
    public static final short POWDER_SNOW = register(BlockTypeKeys.POWDER_SNOW);

    public static final short TERRACOTTA = register(BlockTypeKeys.TERRACOTTA);
    public static final short WHITE_TERRACOTTA = register(BlockTypeKeys.WHITE_TERRACOTTA);
    public static final short ORANGE_TERRACOTTA = register(BlockTypeKeys.ORANGE_TERRACOTTA);
    public static final short YELLOW_TERRACOTTA = register(BlockTypeKeys.YELLOW_TERRACOTTA);
    public static final short BROWN_TERRACOTTA = register(BlockTypeKeys.BROWN_TERRACOTTA);
    public static final short RED_TERRACOTTA = register(BlockTypeKeys.RED_TERRACOTTA);
    public static final short LIGHT_GRAY_TERRACOTTA = register(BlockTypeKeys.LIGHT_GRAY_TERRACOTTA);

    public static final short COAL_ORE = register(BlockTypeKeys.COAL_ORE);
    public static final short DEEPSLATE_COAL_ORE = register(BlockTypeKeys.DEEPSLATE_COAL_ORE);
    public static final short IRON_ORE = register(BlockTypeKeys.IRON_ORE);
    public static final short DEEPSLATE_IRON_ORE = register(BlockTypeKeys.DEEPSLATE_IRON_ORE);
    public static final short COPPER_ORE = register(BlockTypeKeys.COPPER_ORE);
    public static final short DEEPSLATE_COPPER_ORE = register(BlockTypeKeys.DEEPSLATE_COPPER_ORE);
    public static final short GOLD_ORE = register(BlockTypeKeys.GOLD_ORE);
    public static final short DEEPSLATE_GOLD_ORE = register(BlockTypeKeys.DEEPSLATE_GOLD_ORE);
    public static final short REDSTONE_ORE = register(BlockTypeKeys.REDSTONE_ORE);
    public static final short DEEPSLATE_REDSTONE_ORE = register(BlockTypeKeys.DEEPSLATE_REDSTONE_ORE);
    public static final short LAPIS_ORE = register(BlockTypeKeys.LAPIS_ORE);
    public static final short DEEPSLATE_LAPIS_ORE = register(BlockTypeKeys.DEEPSLATE_LAPIS_ORE);
    public static final short DIAMOND_ORE = register(BlockTypeKeys.DIAMOND_ORE);
    public static final short DEEPSLATE_DIAMOND_ORE = register(BlockTypeKeys.DEEPSLATE_DIAMOND_ORE);
    public static final short EMERALD_ORE = register(BlockTypeKeys.EMERALD_ORE);
    public static final short DEEPSLATE_EMERALD_ORE = register(BlockTypeKeys.DEEPSLATE_EMERALD_ORE);

    public static final short OAK_LOG = register(BlockTypeKeys.OAK_LOG);
    public static final short OAK_LEAVES = register(BlockTypeKeys.OAK_LEAVES);
    public static final short BIRCH_LOG = register(BlockTypeKeys.BIRCH_LOG);
    public static final short BIRCH_LEAVES = register(BlockTypeKeys.BIRCH_LEAVES);
    public static final short SPRUCE_LOG = register(BlockTypeKeys.SPRUCE_LOG);
    public static final short SPRUCE_LEAVES = register(BlockTypeKeys.SPRUCE_LEAVES);
    public static final short JUNGLE_LOG = register(BlockTypeKeys.JUNGLE_LOG);
    public static final short JUNGLE_LEAVES = register(BlockTypeKeys.JUNGLE_LEAVES);
    public static final short ACACIA_LOG = register(BlockTypeKeys.ACACIA_LOG);
    public static final short ACACIA_LEAVES = register(BlockTypeKeys.ACACIA_LEAVES);
    public static final short DARK_OAK_LOG = register(BlockTypeKeys.DARK_OAK_LOG);
    public static final short DARK_OAK_LEAVES = register(BlockTypeKeys.DARK_OAK_LEAVES);
    public static final short CHERRY_LOG = register(BlockTypeKeys.CHERRY_LOG);
    public static final short CHERRY_LEAVES = register(BlockTypeKeys.CHERRY_LEAVES);
    public static final short MANGROVE_LOG = register(BlockTypeKeys.MANGROVE_LOG);
    public static final short MANGROVE_LEAVES = register(BlockTypeKeys.MANGROVE_LEAVES);
    public static final short PALE_OAK_LOG = register(BlockTypeKeys.PALE_OAK_LOG);
    public static final short PALE_OAK_LEAVES = register(BlockTypeKeys.PALE_OAK_LEAVES);

    public static final short SHORT_GRASS = register(BlockTypeKeys.SHORT_GRASS);
    public static final short TALL_GRASS = register(BlockTypeKeys.TALL_GRASS);
    public static final short FERN = register(BlockTypeKeys.FERN);
    public static final short LARGE_FERN = register(BlockTypeKeys.LARGE_FERN);
    public static final short DEAD_BUSH = register(BlockTypeKeys.DEAD_BUSH);
    public static final short DANDELION = register(BlockTypeKeys.DANDELION);
    public static final short POPPY = register(BlockTypeKeys.POPPY);
    public static final short BLUE_ORCHID = register(BlockTypeKeys.BLUE_ORCHID);
    public static final short ALLIUM = register(BlockTypeKeys.ALLIUM);
    public static final short AZURE_BLUET = register(BlockTypeKeys.AZURE_BLUET);
    public static final short OXEYE_DAISY = register(BlockTypeKeys.OXEYE_DAISY);
    public static final short CORNFLOWER = register(BlockTypeKeys.CORNFLOWER);
    public static final short LILY_OF_THE_VALLEY = register(BlockTypeKeys.LILY_OF_THE_VALLEY);
    public static final short RED_TULIP = register(BlockTypeKeys.RED_TULIP);
    public static final short ORANGE_TULIP = register(BlockTypeKeys.ORANGE_TULIP);
    public static final short WHITE_TULIP = register(BlockTypeKeys.WHITE_TULIP);
    public static final short PINK_TULIP = register(BlockTypeKeys.PINK_TULIP);
    public static final short SUNFLOWER = register(BlockTypeKeys.SUNFLOWER);
    public static final short BROWN_MUSHROOM = register(BlockTypeKeys.BROWN_MUSHROOM);
    public static final short RED_MUSHROOM = register(BlockTypeKeys.RED_MUSHROOM);
    public static final short MUSHROOM_STEM = register(BlockTypeKeys.MUSHROOM_STEM);
    public static final short RED_MUSHROOM_BLOCK = register(BlockTypeKeys.RED_MUSHROOM_BLOCK);
    public static final short BROWN_MUSHROOM_BLOCK = register(BlockTypeKeys.BROWN_MUSHROOM_BLOCK);
    public static final short CACTUS = register(BlockTypeKeys.CACTUS);
    public static final short SUGAR_CANE = register(BlockTypeKeys.SUGAR_CANE);
    public static final short PUMPKIN = register(BlockTypeKeys.PUMPKIN);
    public static final short MELON = register(BlockTypeKeys.MELON);
    public static final short LILY_PAD = register(BlockTypeKeys.LILY_PAD);
    public static final short VINE = register(BlockTypeKeys.VINE);
    public static final short BAMBOO = register(BlockTypeKeys.BAMBOO);
    public static final short SWEET_BERRY_BUSH = register(BlockTypeKeys.SWEET_BERRY_BUSH);
    public static final short GLOW_LICHEN = register(BlockTypeKeys.GLOW_LICHEN);
    public static final short SEAGRASS = register(BlockTypeKeys.SEAGRASS);
    public static final short KELP = register(BlockTypeKeys.KELP);
    public static final short KELP_PLANT = register(BlockTypeKeys.KELP_PLANT);
    public static final short SEA_PICKLE = register(BlockTypeKeys.SEA_PICKLE);
    public static final short TALL_SEAGRASS = register(BlockTypeKeys.TALL_SEAGRASS);
    public static final short TUBE_CORAL_BLOCK = register(BlockTypeKeys.TUBE_CORAL_BLOCK);
    public static final short BRAIN_CORAL_BLOCK = register(BlockTypeKeys.BRAIN_CORAL_BLOCK);
    public static final short HORN_CORAL_BLOCK = register(BlockTypeKeys.HORN_CORAL_BLOCK);
    public static final short FIRE_CORAL_BLOCK = register(BlockTypeKeys.FIRE_CORAL_BLOCK);
    public static final short BUBBLE_CORAL_BLOCK = register(BlockTypeKeys.BUBBLE_CORAL_BLOCK);

    public static final short AZALEA = register(BlockTypeKeys.AZALEA);
    public static final short FLOWERING_AZALEA = register(BlockTypeKeys.FLOWERING_AZALEA);
    public static final short HANGING_ROOTS = register(BlockTypeKeys.HANGING_ROOTS);
    public static final short CAVE_VINES = register(BlockTypeKeys.CAVE_VINES);
    public static final short SMALL_DRIPLEAF = register(BlockTypeKeys.SMALL_DRIPLEAF);
    public static final short SPORE_BLOSSOM = register(BlockTypeKeys.SPORE_BLOSSOM);
    public static final short SCULK = register(BlockTypeKeys.SCULK);
    public static final short SCULK_VEIN = register(BlockTypeKeys.SCULK_VEIN);
    public static final short AMETHYST_BLOCK = register(BlockTypeKeys.AMETHYST_BLOCK);
    public static final short BUDDING_AMETHYST = register(BlockTypeKeys.BUDDING_AMETHYST);
    public static final short COBWEB = register(BlockTypeKeys.COBWEB);

    public static final short TALL_GRASS_UPPER = variant(BlockTypeKeys.TALL_GRASS, "half", "upper");
    public static final short LARGE_FERN_UPPER = variant(BlockTypeKeys.LARGE_FERN, "half", "upper");
    public static final short SUNFLOWER_UPPER = variant(BlockTypeKeys.SUNFLOWER, "half", "upper");
    public static final short LILAC = register(BlockTypeKeys.LILAC);
    public static final short LILAC_UPPER = variant(BlockTypeKeys.LILAC, "half", "upper");
    public static final short ROSE_BUSH = register(BlockTypeKeys.ROSE_BUSH);
    public static final short ROSE_BUSH_UPPER = variant(BlockTypeKeys.ROSE_BUSH, "half", "upper");
    public static final short PEONY = register(BlockTypeKeys.PEONY);
    public static final short PEONY_UPPER = variant(BlockTypeKeys.PEONY, "half", "upper");
    public static final short TALL_SEAGRASS_UPPER = variant(BlockTypeKeys.TALL_SEAGRASS, "half", "upper");

    public static final short SNOW_2 = variant(BlockTypeKeys.SNOW, "layers", "2");
    public static final short SNOW_3 = variant(BlockTypeKeys.SNOW, "layers", "3");

    public static final short DRIPSTONE_DOWN = variant(
            BlockTypeKeys.POINTED_DRIPSTONE, Map.of("vertical_direction", "down", "thickness", "tip"));
    public static final short DRIPSTONE_DOWN_MIDDLE = variant(
            BlockTypeKeys.POINTED_DRIPSTONE, Map.of("vertical_direction", "down", "thickness", "frustum"));
    public static final short DRIPSTONE_UP = variant(
            BlockTypeKeys.POINTED_DRIPSTONE, Map.of("vertical_direction", "up", "thickness", "tip"));
    public static final short DRIPSTONE_UP_MIDDLE = variant(
            BlockTypeKeys.POINTED_DRIPSTONE, Map.of("vertical_direction", "up", "thickness", "frustum"));

    public static final short CAVE_VINES_PLANT = register(BlockTypeKeys.CAVE_VINES_PLANT);
    public static final short CAVE_VINES_BERRIES = variant(BlockTypeKeys.CAVE_VINES, "berries", "true");

    public static final short BAMBOO_SMALL_LEAVES = variant(BlockTypeKeys.BAMBOO, "leaves", "small");
    public static final short BAMBOO_LARGE_LEAVES = variant(BlockTypeKeys.BAMBOO, "leaves", "large");

    public static final short VINE_NORTH = variant(BlockTypeKeys.VINE, "north", "true");
    public static final short VINE_SOUTH = variant(BlockTypeKeys.VINE, "south", "true");
    public static final short VINE_EAST = variant(BlockTypeKeys.VINE, "east", "true");
    public static final short VINE_WEST = variant(BlockTypeKeys.VINE, "west", "true");
    public static final short VINE_UP = variant(BlockTypeKeys.VINE, "up", "true");
    public static final short GLOW_LICHEN_DOWN = variant(BlockTypeKeys.GLOW_LICHEN, "down", "true");

    private static final List<TypedKey<BlockType>> TABLE = List.copyOf(KEYS);
    private static final Map<String, String>[] PROPERTY_TABLE = toArray(PROPERTIES);

    private Blk() {
        throw new UnsupportedOperationException("Blk cannot be instantiated.");
    }

    private static short register(final TypedKey<BlockType> key) {
        return variant(key, Map.of());
    }

    private static short variant(final TypedKey<BlockType> key, final String property, final String value) {
        return variant(key, Map.of(property, value));
    }

    private static short variant(final TypedKey<BlockType> key, final Map<String, String> properties) {
        KEYS.add(key);
        PROPERTIES.add(properties);
        return (short) (KEYS.size() - 1);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String>[] toArray(final List<Map<String, String>> values) {
        return values.toArray(new Map[0]);
    }

    public static TypedKey<BlockType> key(final short id) {
        return TABLE.get(id);
    }

    public static Map<String, String> properties(final short id) {
        return PROPERTY_TABLE[id];
    }

    public static int size() {
        return TABLE.size();
    }

    public static boolean isOreReplaceable(final short id) {
        return id == STONE
                || id == DEEPSLATE
                || id == GRANITE
                || id == DIORITE
                || id == ANDESITE
                || id == TUFF;
    }

    public static boolean isReplaceableByFeature(final short id) {
        return id == AIR
                || id == SHORT_GRASS
                || id == TALL_GRASS
                || id == FERN
                || id == LARGE_FERN
                || id == DEAD_BUSH
                || id == SNOW
                || id == VINE
                || id == GLOW_LICHEN;
    }

    public static boolean isSoil(final short id) {
        return id == GRASS_BLOCK
                || id == DIRT
                || id == COARSE_DIRT
                || id == ROOTED_DIRT
                || id == PODZOL
                || id == MYCELIUM
                || id == MOSS_BLOCK
                || id == MUD;
    }

    public static boolean isSolid(final short id) {
        return id != AIR && id != WATER && id != LAVA;
    }
}
