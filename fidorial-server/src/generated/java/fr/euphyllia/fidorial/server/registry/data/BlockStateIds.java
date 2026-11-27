package fr.euphyllia.fidorial.server.registry.data;

import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockProperties;
import fr.fidorial.world.block.BlockRegistry;
import fr.fidorial.world.block.BlockType;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Registers every block type and its full network state table.
 *
 * <p>Generated from Mojang's registry report; do not edit.</p>
 */
public final class BlockStateIds {
    /**
     * Total number of block states.
     */
    public static final int STATE_COUNT = 37840;

    /**
     * Total number of block types.
     */
    public static final int BLOCK_COUNT = 1311;

    private BlockStateIds() {
        throw new UnsupportedOperationException("BlockStateIds cannot be instantiated.");
    }

    private static void register0(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12927, 9));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16988, 11));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16572, 31));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16252, 7));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7695, 49));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 379, 27));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_LOG, List.of(
                    BlockProperties.AXIS
                ), 151, 1));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_PLANKS, List.of(), 19, 0));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8537, 1));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 38, 0));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4092, 9));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 6987, 17));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16026, 3));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14152, 11));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9453, 15));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8359, 1));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7439, 1));
        registry.register(BlockType.of(BlockTypeKeys.ACACIA_WOOD, List.of(
                    BlockProperties.AXIS
                ), 222, 1));
        registry.register(BlockType.of(BlockTypeKeys.ACTIVATOR_RAIL, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.STRAIGHT_RAIL_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 13588, 13));
        registry.register(BlockType.of(BlockTypeKeys.AIR, List.of(), 0, 0));
        registry.register(BlockType.of(BlockTypeKeys.ALLIUM, List.of(), 3818, 0));
        registry.register(BlockType.of(BlockTypeKeys.AMETHYST_BLOCK, List.of(), 27588, 0));
        registry.register(BlockType.of(BlockTypeKeys.AMETHYST_CLUSTER, List.of(
                    BlockProperties.FACING,
                    BlockProperties.WATERLOGGED
                ), 27602, 9));
        registry.register(BlockType.of(BlockTypeKeys.ANCIENT_DEBRIS, List.of(), 26005, 0));
        registry.register(BlockType.of(BlockTypeKeys.ANDESITE, List.of(), 6, 0));
        registry.register(BlockType.of(BlockTypeKeys.ANDESITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20656, 3));
        registry.register(BlockType.of(BlockTypeKeys.ANDESITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 20282, 11));
        registry.register(BlockType.of(BlockTypeKeys.ANDESITE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 23272, 3));
        registry.register(BlockType.of(BlockTypeKeys.ANVIL, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 13375, 0));
        registry.register(BlockType.of(BlockTypeKeys.ATTACHED_MELON_STEM, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 10485, 0));
        registry.register(BlockType.of(BlockTypeKeys.ATTACHED_PUMPKIN_STEM, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 10481, 0));
        registry.register(BlockType.of(BlockTypeKeys.AZALEA, List.of(), 35778, 0));
        registry.register(BlockType.of(BlockTypeKeys.AZALEA_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 603, 27));
        registry.register(BlockType.of(BlockTypeKeys.AZURE_BLUET, List.of(), 3819, 0));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO, List.of(
                    BlockProperties.AGE_0_1,
                    BlockProperties.LEAVES,
                    BlockProperties.STAGE
                ), 19465, 0));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_BLOCK, List.of(
                    BlockProperties.AXIS
                ), 174, 1));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13071, 9));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 17372, 11));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16764, 31));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16444, 7));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 8271, 49));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_MOSAIC, List.of(), 29, 0));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_MOSAIC_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16068, 3));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_MOSAIC_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14712, 11));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_PLANKS, List.of(), 28, 0));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8549, 1));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_SAPLING, List.of(), 19464, 0));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4156, 9));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7211, 17));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16062, 3));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14632, 11));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9837, 15));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8431, 1));
        registry.register(BlockType.of(BlockTypeKeys.BAMBOO_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7495, 1));
        registry.register(BlockType.of(BlockTypeKeys.BARREL, List.of(
                    BlockProperties.FACING,
                    BlockProperties.OPEN
                ), 24928, 1));
        registry.register(BlockType.of(BlockTypeKeys.BARRIER, List.of(
                    BlockProperties.WATERLOGGED
                ), 14793, 1));
        registry.register(BlockType.of(BlockTypeKeys.BASALT, List.of(
                    BlockProperties.AXIS
                ), 9083, 1));
        registry.register(BlockType.of(BlockTypeKeys.BEACON, List.of(), 12135, 0));
        registry.register(BlockType.of(BlockTypeKeys.BEDROCK, List.of(), 88, 0));
        registry.register(BlockType.of(BlockTypeKeys.BEE_NEST, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HONEY_LEVEL
                ), 25954, 0));
        registry.register(BlockType.of(BlockTypeKeys.BEEHIVE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HONEY_LEVEL
                ), 25978, 0));
        registry.register(BlockType.of(BlockTypeKeys.BEETROOTS, List.of(
                    BlockProperties.AGE_0_3
                ), 17617, 0));
        registry.register(BlockType.of(BlockTypeKeys.BELL, List.of(
                    BlockProperties.ATTACHMENT,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 24991, 1));
        registry.register(BlockType.of(BlockTypeKeys.BIG_DRIPLEAF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.TILT,
                    BlockProperties.WATERLOGGED
                ), 35830, 1));
        registry.register(BlockType.of(BlockTypeKeys.BIG_DRIPLEAF_STEM, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35862, 1));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12879, 9));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16860, 11));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16508, 31));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16188, 7));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7631, 49));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 323, 27));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_LOG, List.of(
                    BlockProperties.AXIS
                ), 145, 1));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_PLANKS, List.of(), 17, 0));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8533, 1));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 34, 0));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4220, 9));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 6955, 17));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16014, 3));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 11963, 11));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9325, 15));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8351, 1));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7431, 1));
        registry.register(BlockType.of(BlockTypeKeys.BIRCH_WOOD, List.of(
                    BlockProperties.AXIS
                ), 216, 1));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15839, 8));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2270, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27538, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27586, 1));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_CARPET, List.of(), 15171, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_CONCRETE, List.of(), 17855, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_CONCRETE_POWDER, List.of(), 19247, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19226, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19056, 11));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17836, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17770, 4));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_STAINED_GLASS, List.of(), 9196, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 14120, 31));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_TERRACOTTA, List.of(), 13639, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15915, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_WOOL, List.of(), 2424, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3795, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLACK_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3625, 11));
        registry.register(BlockType.of(BlockTypeKeys.BLACKSTONE, List.of(), 26017, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLACKSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 26422, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLACKSTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 26018, 11));
        registry.register(BlockType.of(BlockTypeKeys.BLACKSTONE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 26098, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLAST_FURNACE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.LIT
                ), 24948, 1));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15775, 8));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2206, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27474, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27578, 1));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_CARPET, List.of(), 15167, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_CONCRETE, List.of(), 17851, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_CONCRETE_POWDER, List.of(), 19243, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19202, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18736, 11));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17820, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_ICE, List.of(), 19461, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_ORCHID, List.of(), 3817, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17746, 4));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_STAINED_GLASS, List.of(), 9192, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13992, 31));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_TERRACOTTA, List.of(), 13635, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15899, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_WOOL, List.of(), 2420, 0));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3771, 3));
        registry.register(BlockType.of(BlockTypeKeys.BLUE_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3305, 11));
        registry.register(BlockType.of(BlockTypeKeys.BONE_BLOCK, List.of(
                    BlockProperties.AXIS
                ), 17658, 1));
        registry.register(BlockType.of(BlockTypeKeys.BOOKSHELF, List.of(), 3835, 0));
        registry.register(BlockType.of(BlockTypeKeys.BRAIN_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19345, 0));
        registry.register(BlockType.of(BlockTypeKeys.BRAIN_CORAL_BLOCK, List.of(), 19329, 0));
        registry.register(BlockType.of(BlockTypeKeys.BRAIN_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19365, 0));
        registry.register(BlockType.of(BlockTypeKeys.BRAIN_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19421, 0));
        registry.register(BlockType.of(BlockTypeKeys.BREWING_STAND, List.of(
                    BlockProperties.HAS_BOTTLE_0,
                    BlockProperties.HAS_BOTTLE_1,
                    BlockProperties.HAS_BOTTLE_2
                ), 11599, 7));
        registry.register(BlockType.of(BlockTypeKeys.BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16104, 3));
        registry.register(BlockType.of(BlockTypeKeys.BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 10825, 11));
        registry.register(BlockType.of(BlockTypeKeys.BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 20680, 3));
        registry.register(BlockType.of(BlockTypeKeys.BRICKS, List.of(), 3832, 0));
        registry.register(BlockType.of(BlockTypeKeys.BRITTLE_ICE, List.of(
                    BlockProperties.AGE_0_3
                ), 17651, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15791, 8));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2222, 3));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27490, 3));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27580, 1));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_CARPET, List.of(), 15168, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_CONCRETE, List.of(), 17852, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_CONCRETE_POWDER, List.of(), 19244, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19208, 3));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18816, 11));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17824, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_MUSHROOM, List.of(), 3828, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_MUSHROOM_BLOCK, List.of(
                    BlockProperties.DOWN,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WEST
                ), 9913, 0));
    }

    private static void register1(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.BROWN_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17752, 4));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_STAINED_GLASS, List.of(), 9193, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 14024, 31));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_TERRACOTTA, List.of(), 13636, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15903, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_WOOL, List.of(), 2421, 0));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3777, 3));
        registry.register(BlockType.of(BlockTypeKeys.BROWN_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3385, 11));
        registry.register(BlockType.of(BlockTypeKeys.BUBBLE_COLUMN, List.of(
                    BlockProperties.DRAG
                ), 19480, 0));
        registry.register(BlockType.of(BlockTypeKeys.BUBBLE_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19347, 0));
        registry.register(BlockType.of(BlockTypeKeys.BUBBLE_CORAL_BLOCK, List.of(), 19330, 0));
        registry.register(BlockType.of(BlockTypeKeys.BUBBLE_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19367, 0));
        registry.register(BlockType.of(BlockTypeKeys.BUBBLE_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19429, 0));
        registry.register(BlockType.of(BlockTypeKeys.BUDDING_AMETHYST, List.of(), 27601, 0));
        registry.register(BlockType.of(BlockTypeKeys.BUSH, List.of(), 2366, 0));
        registry.register(BlockType.of(BlockTypeKeys.CACTUS, List.of(
                    BlockProperties.AGE_0_15
                ), 9010, 0));
        registry.register(BlockType.of(BlockTypeKeys.CACTUS_FLOWER, List.of(), 9026, 0));
        registry.register(BlockType.of(BlockTypeKeys.CAKE, List.of(
                    BlockProperties.BITES
                ), 9110, 0));
        registry.register(BlockType.of(BlockTypeKeys.CALCITE, List.of(), 31358, 0));
        registry.register(BlockType.of(BlockTypeKeys.CALCITE_PILLAR, List.of(
                    BlockProperties.AXIS
                ), 31359, 1));
        registry.register(BlockType.of(BlockTypeKeys.CALCITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 31362, 3));
        registry.register(BlockType.of(BlockTypeKeys.CALCITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 31368, 11));
        registry.register(BlockType.of(BlockTypeKeys.CALCITE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 31448, 3));
        registry.register(BlockType.of(BlockTypeKeys.CALIBRATED_SCULK_SENSOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWER,
                    BlockProperties.SCULK_SENSOR_PHASE,
                    BlockProperties.WATERLOGGED
                ), 32692, 1));
        registry.register(BlockType.of(BlockTypeKeys.CAMPFIRE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.LIT,
                    BlockProperties.SIGNAL_FIRE,
                    BlockProperties.WATERLOGGED
                ), 25063, 3));
        registry.register(BlockType.of(BlockTypeKeys.CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27282, 3));
        registry.register(BlockType.of(BlockTypeKeys.CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27554, 1));
        registry.register(BlockType.of(BlockTypeKeys.CARROTS, List.of(
                    BlockProperties.AGE_0_7
                ), 12815, 0));
        registry.register(BlockType.of(BlockTypeKeys.CARTOGRAPHY_TABLE, List.of(), 24956, 0));
        registry.register(BlockType.of(BlockTypeKeys.CARVED_PUMPKIN, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 9102, 0));
        registry.register(BlockType.of(BlockTypeKeys.CAULDRON, List.of(), 11607, 0));
        registry.register(BlockType.of(BlockTypeKeys.CAVE_AIR, List.of(), 19479, 0));
        registry.register(BlockType.of(BlockTypeKeys.CAVE_VINES, List.of(
                    BlockProperties.AGE_0_25,
                    BlockProperties.BERRIES
                ), 35723, 1));
        registry.register(BlockType.of(BlockTypeKeys.CAVE_VINES_PLANT, List.of(
                    BlockProperties.BERRIES
                ), 35775, 1));
        registry.register(BlockType.of(BlockTypeKeys.CHAIN_COMMAND_BLOCK, List.of(
                    BlockProperties.CONDITIONAL,
                    BlockProperties.FACING
                ), 17635, 6));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12951, 9));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 17052, 11));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16604, 31));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16284, 7));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7759, 49));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 407, 27));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_LOG, List.of(
                    BlockProperties.AXIS
                ), 154, 1));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_PLANKS, List.of(), 20, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8539, 1));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 40, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4284, 9));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7019, 17));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16032, 3));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14232, 11));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9517, 15));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8367, 1));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7447, 1));
        registry.register(BlockType.of(BlockTypeKeys.CHERRY_WOOD, List.of(
                    BlockProperties.AXIS
                ), 225, 1));
        registry.register(BlockType.of(BlockTypeKeys.CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {5543, 5544, 5549, 5550, 5555, 5556, 5561, 5562, 5545, 5546, 5551, 5552, 5557, 5558, 5563, 5564, 5547, 5548, 5553, 5554, 5559, 5560, 5565, 5566}, 1));
        registry.register(BlockType.of(BlockTypeKeys.CHIPPED_ANVIL, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 13379, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_BOOKSHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.SLOT_0_OCCUPIED,
                    BlockProperties.SLOT_1_OCCUPIED,
                    BlockProperties.SLOT_2_OCCUPIED,
                    BlockProperties.SLOT_3_OCCUPIED,
                    BlockProperties.SLOT_4_OCCUPIED,
                    BlockProperties.SLOT_5_OCCUPIED
                ), 3836, 63));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_CINNABAR, List.of(), 31357, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_COPPER, List.of(), 33234, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_DEEPSLATE, List.of(), 37537, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_NETHER_BRICKS, List.of(), 27279, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_PACKED_ICE, List.of(), 15586, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_POLISHED_BLACKSTONE, List.of(), 26431, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_QUARTZ_BLOCK, List.of(), 13504, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_RED_SANDSTONE, List.of(), 15920, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_RESIN_BRICKS, List.of(), 11480, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_SANDSTONE, List.of(), 678, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_SOULSHALE, List.of(), 33215, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_STONE_BRICKS, List.of(), 9904, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_SULFUR, List.of(), 30123, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_TUFF, List.of(), 28472, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHISELED_TUFF_BRICKS, List.of(), 28884, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHORUS_FLOWER, List.of(
                    BlockProperties.AGE_0_5
                ), 17506, 0));
        registry.register(BlockType.of(BlockTypeKeys.CHORUS_PLANT, List.of(
                    BlockProperties.DOWN,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WEST
                ), 17442, 63));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR, List.of(), 30124, 0));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 30947, 3));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 30953, 11));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 31033, 3));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR_BRICKS, List.of(), 30946, 0));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 30125, 3));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 30131, 11));
        registry.register(BlockType.of(BlockTypeKeys.CINNABAR_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 30211, 3));
        registry.register(BlockType.of(BlockTypeKeys.CLAY, List.of(), 9027, 0));
        registry.register(BlockType.of(BlockTypeKeys.CLOSED_EYEBLOSSOM, List.of(), 37836, 0));
        registry.register(BlockType.of(BlockTypeKeys.COAL_BLOCK, List.of(), 15173, 0));
        registry.register(BlockType.of(BlockTypeKeys.COAL_ORE, List.of(), 136, 0));
        registry.register(BlockType.of(BlockTypeKeys.COARSE_DIRT, List.of(), 11, 0));
        registry.register(BlockType.of(BlockTypeKeys.COBBLED_DEEPSLATE, List.of(), 35893, 0));
        registry.register(BlockType.of(BlockTypeKeys.COBBLED_DEEPSLATE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 35974, 3));
        registry.register(BlockType.of(BlockTypeKeys.COBBLED_DEEPSLATE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 35894, 11));
        registry.register(BlockType.of(BlockTypeKeys.COBBLED_DEEPSLATE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 35980, 3));
        registry.register(BlockType.of(BlockTypeKeys.COBBLESTONE, List.of(), 14, 0));
        registry.register(BlockType.of(BlockTypeKeys.COBBLESTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16098, 3));
        registry.register(BlockType.of(BlockTypeKeys.COBBLESTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 7335, 11));
        registry.register(BlockType.of(BlockTypeKeys.COBBLESTONE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 12136, 3));
        registry.register(BlockType.of(BlockTypeKeys.COBWEB, List.of(), 2362, 0));
        registry.register(BlockType.of(BlockTypeKeys.COCOA, List.of(
                    BlockProperties.AGE_0_2,
                    BlockProperties.HORIZONTAL_FACING
                ), 11628, 0));
        registry.register(BlockType.of(BlockTypeKeys.COMMAND_BLOCK, List.of(
                    BlockProperties.CONDITIONAL,
                    BlockProperties.FACING
                ), 12123, 6));
        registry.register(BlockType.of(BlockTypeKeys.COMPARATOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.COMPARATOR_MODE,
                    BlockProperties.POWERED
                ), 13443, 1));
        registry.register(BlockType.of(BlockTypeKeys.COMPOSTER, List.of(
                    BlockProperties.LEVEL_0_8
                ), 25929, 0));
        registry.register(BlockType.of(BlockTypeKeys.CONDUIT, List.of(
                    BlockProperties.WATERLOGGED
                ), 19462, 0));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10137, 31));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_BLOCK, List.of(), 33216, 0));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34970, 3));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10399, 3));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35002, 35003, 35008, 35009, 35014, 35015, 35020, 35021, 35004, 35005, 35010, 35011, 35016, 35017, 35022, 35023, 35006, 35007, 35012, 35013, 35018, 35019, 35024, 35025}, 1));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 33930, 11));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35194, 1));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34954, 1));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25031, 3));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_ORE, List.of(), 33224, 0));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_TORCH, List.of(), 9094, 0));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34442, 15));
        registry.register(BlockType.of(BlockTypeKeys.COPPER_WALL_TORCH, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 9095, 0));
        registry.register(BlockType.of(BlockTypeKeys.CORNFLOWER, List.of(), 3825, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRACKED_DEEPSLATE_BRICKS, List.of(), 37538, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRACKED_DEEPSLATE_TILES, List.of(), 37539, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRACKED_NETHER_BRICKS, List.of(), 27280, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRACKED_POLISHED_BLACKSTONE_BRICKS, List.of(), 26430, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRACKED_STONE_BRICKS, List.of(), 9903, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRAFTER, List.of(
                    BlockProperties.CRAFTING,
                    BlockProperties.ORIENTATION,
                    BlockProperties.TRIGGERED
                ), 37576, 45));
        registry.register(BlockType.of(BlockTypeKeys.CRAFTING_TABLE, List.of(), 6866, 0));
        registry.register(BlockType.of(BlockTypeKeys.CREAKING_HEART, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.CREAKING_HEART_STATE,
                    BlockProperties.NATURAL
                ), 5445, 7));
        registry.register(BlockType.of(BlockTypeKeys.CREEPER_HEAD, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.ROTATION
                ), 13255, 16));
        registry.register(BlockType.of(BlockTypeKeys.CREEPER_WALL_HEAD, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13287, 1));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 25652, 9));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 25700, 11));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 25236, 31));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 25428, 7));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_FUNGUS, List.of(), 25161, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 8015, 49));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_HYPHAE, List.of(
                    BlockProperties.AXIS
                ), 25154, 1));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_NYLIUM, List.of(), 25160, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_PLANKS, List.of(), 25218, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 25232, 1));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_ROOTS, List.of(), 25217, 0));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4348, 9));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 25828, 17));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 25220, 3));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 25492, 11));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_STEM, List.of(
                    BlockProperties.AXIS
                ), 25148, 1));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 25300, 15));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8415, 1));
        registry.register(BlockType.of(BlockTypeKeys.CRIMSON_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 25892, 1));
        registry.register(BlockType.of(BlockTypeKeys.CRYING_OBSIDIAN, List.of(), 26006, 0));
        registry.register(BlockType.of(BlockTypeKeys.CUT_COPPER, List.of(), 33226, 0));
        registry.register(BlockType.of(BlockTypeKeys.CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33882, 3));
        registry.register(BlockType.of(BlockTypeKeys.CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33242, 11));
        registry.register(BlockType.of(BlockTypeKeys.CUT_RED_SANDSTONE, List.of(), 15921, 0));
        registry.register(BlockType.of(BlockTypeKeys.CUT_RED_SANDSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16140, 3));
        registry.register(BlockType.of(BlockTypeKeys.CUT_SANDSTONE, List.of(), 679, 0));
    }

    private static void register2(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.CUT_SANDSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16086, 3));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15743, 8));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2174, 3));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27442, 3));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27574, 1));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_CARPET, List.of(), 15165, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_CONCRETE, List.of(), 17849, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_CONCRETE_POWDER, List.of(), 19241, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19190, 3));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18576, 11));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17812, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17734, 4));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_STAINED_GLASS, List.of(), 9190, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13928, 31));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_TERRACOTTA, List.of(), 13633, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15891, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_WOOL, List.of(), 2418, 0));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3759, 3));
        registry.register(BlockType.of(BlockTypeKeys.CYAN_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3145, 11));
        registry.register(BlockType.of(BlockTypeKeys.DAMAGED_ANVIL, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 13383, 0));
        registry.register(BlockType.of(BlockTypeKeys.DANDELION, List.of(), 3813, 0));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12975, 9));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 17116, 11));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16636, 31));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16316, 7));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7887, 49));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 435, 27));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_LOG, List.of(
                    BlockProperties.AXIS
                ), 157, 1));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_PLANKS, List.of(), 21, 0));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8541, 1));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 42, 0));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4412, 9));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7083, 17));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16038, 3));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14312, 11));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9581, 15));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8383, 1));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7463, 1));
        registry.register(BlockType.of(BlockTypeKeys.DARK_OAK_WOOD, List.of(
                    BlockProperties.AXIS
                ), 228, 1));
        registry.register(BlockType.of(BlockTypeKeys.DARK_PRISMARINE, List.of(), 14893, 0));
        registry.register(BlockType.of(BlockTypeKeys.DARK_PRISMARINE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 15146, 3));
        registry.register(BlockType.of(BlockTypeKeys.DARK_PRISMARINE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 15054, 11));
        registry.register(BlockType.of(BlockTypeKeys.DAYLIGHT_DETECTOR, List.of(
                    BlockProperties.INVERTED,
                    BlockProperties.POWER
                ), 13459, 16));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BRAIN_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19335, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BRAIN_CORAL_BLOCK, List.of(), 19324, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BRAIN_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19355, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BRAIN_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19381, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BUBBLE_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19337, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BUBBLE_CORAL_BLOCK, List.of(), 19325, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BUBBLE_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19357, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BUBBLE_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19389, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_BUSH, List.of(), 2365, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_FIRE_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19339, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_FIRE_CORAL_BLOCK, List.of(), 19326, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_FIRE_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19359, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_FIRE_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19397, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_HORN_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19341, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_HORN_CORAL_BLOCK, List.of(), 19327, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_HORN_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19361, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_HORN_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19405, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_TUBE_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19333, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_TUBE_CORAL_BLOCK, List.of(), 19323, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_TUBE_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19353, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEAD_TUBE_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19373, 0));
        registry.register(BlockType.of(BlockTypeKeys.DECORATED_POT, List.of(
                    BlockProperties.CRACKED,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 37560, 9));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE, List.of(
                    BlockProperties.AXIS
                ), 35890, 1));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 37207, 3));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 37127, 11));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 37213, 3));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_BRICKS, List.of(), 37126, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_COAL_ORE, List.of(), 137, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_COPPER_ORE, List.of(), 33225, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_DIAMOND_ORE, List.of(), 6864, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_EMERALD_ORE, List.of(), 11729, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_GOLD_ORE, List.of(), 133, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_IRON_ORE, List.of(), 135, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_LAPIS_ORE, List.of(), 663, 0));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_REDSTONE_ORE, List.of(
                    BlockProperties.LIT
                ), 8553, 1));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_TILE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 36796, 3));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_TILE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 36716, 11));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_TILE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 36802, 3));
        registry.register(BlockType.of(BlockTypeKeys.DEEPSLATE_TILES, List.of(), 36715, 0));
        registry.register(BlockType.of(BlockTypeKeys.DETECTOR_RAIL, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.STRAIGHT_RAIL_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2326, 13));
        registry.register(BlockType.of(BlockTypeKeys.DIAMOND_BLOCK, List.of(), 6865, 0));
        registry.register(BlockType.of(BlockTypeKeys.DIAMOND_ORE, List.of(), 6863, 0));
        registry.register(BlockType.of(BlockTypeKeys.DIORITE, List.of(), 4, 0));
        registry.register(BlockType.of(BlockTypeKeys.DIORITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20674, 3));
        registry.register(BlockType.of(BlockTypeKeys.DIORITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 20522, 11));
        registry.register(BlockType.of(BlockTypeKeys.DIORITE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 24568, 3));
        registry.register(BlockType.of(BlockTypeKeys.DIRT, List.of(), 10, 0));
        registry.register(BlockType.of(BlockTypeKeys.DIRT_PATH, List.of(), 17621, 0));
        registry.register(BlockType.of(BlockTypeKeys.DISPENSER, List.of(
                    BlockProperties.FACING,
                    BlockProperties.TRIGGERED
                ), 665, 1));
        registry.register(BlockType.of(BlockTypeKeys.DRAGON_EGG, List.of(), 11625, 0));
        registry.register(BlockType.of(BlockTypeKeys.DRAGON_HEAD, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.ROTATION
                ), 13295, 16));
        registry.register(BlockType.of(BlockTypeKeys.DRAGON_WALL_HEAD, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13327, 1));
        registry.register(BlockType.of(BlockTypeKeys.DRIED_GHAST, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HYDRATION,
                    BlockProperties.WATERLOGGED
                ), 19291, 1));
        registry.register(BlockType.of(BlockTypeKeys.DRIED_KELP_BLOCK, List.of(), 19275, 0));
        registry.register(BlockType.of(BlockTypeKeys.DRIPSTONE_BLOCK, List.of(), 35642, 0));
        registry.register(BlockType.of(BlockTypeKeys.DROPPER, List.of(
                    BlockProperties.FACING,
                    BlockProperties.TRIGGERED
                ), 13612, 1));
        registry.register(BlockType.of(BlockTypeKeys.EMERALD_BLOCK, List.of(), 11882, 0));
        registry.register(BlockType.of(BlockTypeKeys.EMERALD_ORE, List.of(), 11728, 0));
        registry.register(BlockType.of(BlockTypeKeys.ENCHANTING_TABLE, List.of(), 11598, 0));
        registry.register(BlockType.of(BlockTypeKeys.END_GATEWAY, List.of(), 17622, 0));
        registry.register(BlockType.of(BlockTypeKeys.END_PORTAL, List.of(), 11615, 0));
        registry.register(BlockType.of(BlockTypeKeys.END_PORTAL_FRAME, List.of(
                    BlockProperties.EYE,
                    BlockProperties.HORIZONTAL_FACING
                ), 11616, 4));
        registry.register(BlockType.of(BlockTypeKeys.END_ROD, List.of(
                    BlockProperties.FACING
                ), 17436, 4));
        registry.register(BlockType.of(BlockTypeKeys.END_STONE, List.of(), 11624, 0));
        registry.register(BlockType.of(BlockTypeKeys.END_STONE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20632, 3));
        registry.register(BlockType.of(BlockTypeKeys.END_STONE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19882, 11));
        registry.register(BlockType.of(BlockTypeKeys.END_STONE_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 24244, 3));
        registry.register(BlockType.of(BlockTypeKeys.END_STONE_BRICKS, List.of(), 17602, 0));
        registry.register(BlockType.of(BlockTypeKeys.ENDER_CHEST, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 11730, 1));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_CHISELED_COPPER, List.of(), 33235, 0));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER, List.of(), 33217, 0));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10169, 31));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34974, 3));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10405, 3));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35026, 35027, 35032, 35033, 35038, 35039, 35044, 35045, 35028, 35029, 35034, 35035, 35040, 35041, 35046, 35047, 35030, 35031, 35036, 35037, 35042, 35043, 35048, 35049}, 1));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 33994, 11));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35226, 1));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34956, 1));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25035, 3));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34506, 15));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_CUT_COPPER, List.of(), 33227, 0));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33888, 3));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33322, 11));
        registry.register(BlockType.of(BlockTypeKeys.EXPOSED_LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35474, 19));
        registry.register(BlockType.of(BlockTypeKeys.FARMLAND, List.of(
                    BlockProperties.MOISTURE
                ), 6875, 0));
        registry.register(BlockType.of(BlockTypeKeys.FERN, List.of(), 2364, 0));
        registry.register(BlockType.of(BlockTypeKeys.FIRE, List.of(
                    BlockProperties.AGE_0_15,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WEST
                ), 4931, 31));
        registry.register(BlockType.of(BlockTypeKeys.FIRE_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19349, 0));
        registry.register(BlockType.of(BlockTypeKeys.FIRE_CORAL_BLOCK, List.of(), 19331, 0));
        registry.register(BlockType.of(BlockTypeKeys.FIRE_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19369, 0));
        registry.register(BlockType.of(BlockTypeKeys.FIRE_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19437, 0));
        registry.register(BlockType.of(BlockTypeKeys.FIREFLY_BUSH, List.of(), 37839, 0));
        registry.register(BlockType.of(BlockTypeKeys.FLETCHING_TABLE, List.of(), 24957, 0));
        registry.register(BlockType.of(BlockTypeKeys.FLOWER_POT, List.of(), 12784, 0));
        registry.register(BlockType.of(BlockTypeKeys.FLOWERING_AZALEA, List.of(), 35779, 0));
        registry.register(BlockType.of(BlockTypeKeys.FLOWERING_AZALEA_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 631, 27));
        registry.register(BlockType.of(BlockTypeKeys.FROGSPAWN, List.of(), 37558, 0));
        registry.register(BlockType.of(BlockTypeKeys.FROSTED_ICE, List.of(
                    BlockProperties.AGE_0_3
                ), 17647, 0));
        registry.register(BlockType.of(BlockTypeKeys.FURNACE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.LIT
                ), 6883, 1));
        registry.register(BlockType.of(BlockTypeKeys.GILDED_BLACKSTONE, List.of(), 26842, 0));
        registry.register(BlockType.of(BlockTypeKeys.GLASS, List.of(), 661, 0));
        registry.register(BlockType.of(BlockTypeKeys.GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10447, 31));
        registry.register(BlockType.of(BlockTypeKeys.GLOW_LICHEN, List.of(
                    BlockProperties.DOWN,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10537, 127));
        registry.register(BlockType.of(BlockTypeKeys.GLOWSTONE, List.of(), 9099, 0));
        registry.register(BlockType.of(BlockTypeKeys.GOLD_BLOCK, List.of(), 3830, 0));
        registry.register(BlockType.of(BlockTypeKeys.GOLD_ORE, List.of(), 132, 0));
        registry.register(BlockType.of(BlockTypeKeys.GOLDEN_DANDELION, List.of(), 3814, 0));
    }

    private static void register3(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.GRANITE, List.of(), 2, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRANITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20650, 3));
        registry.register(BlockType.of(BlockTypeKeys.GRANITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 20202, 11));
        registry.register(BlockType.of(BlockTypeKeys.GRANITE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 21976, 3));
        registry.register(BlockType.of(BlockTypeKeys.GRASS_BLOCK, List.of(
                    BlockProperties.SNOWY
                ), 8, 1));
        registry.register(BlockType.of(BlockTypeKeys.GRAVEL, List.of(), 127, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15711, 8));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2142, 3));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27410, 3));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27570, 1));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_CARPET, List.of(), 15163, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_CONCRETE, List.of(), 17847, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_CONCRETE_POWDER, List.of(), 19239, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19178, 3));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18416, 11));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17804, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17722, 4));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_STAINED_GLASS, List.of(), 9188, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13864, 31));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_TERRACOTTA, List.of(), 13631, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15883, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_WOOL, List.of(), 2416, 0));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3747, 3));
        registry.register(BlockType.of(BlockTypeKeys.GRAY_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2985, 11));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15807, 8));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2238, 3));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27506, 3));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27582, 1));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_CARPET, List.of(), 15169, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_CONCRETE, List.of(), 17853, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_CONCRETE_POWDER, List.of(), 19245, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19214, 3));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18896, 11));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17828, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17758, 4));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_STAINED_GLASS, List.of(), 9194, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 14056, 31));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_TERRACOTTA, List.of(), 13637, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15907, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_WOOL, List.of(), 2422, 0));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3783, 3));
        registry.register(BlockType.of(BlockTypeKeys.GREEN_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3465, 11));
        registry.register(BlockType.of(BlockTypeKeys.GRINDSTONE, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING
                ), 24958, 4));
        registry.register(BlockType.of(BlockTypeKeys.HANGING_ROOTS, List.of(
                    BlockProperties.WATERLOGGED
                ), 35886, 1));
        registry.register(BlockType.of(BlockTypeKeys.HAY_BLOCK, List.of(
                    BlockProperties.AXIS
                ), 15153, 1));
        registry.register(BlockType.of(BlockTypeKeys.HEAVY_CORE, List.of(
                    BlockProperties.WATERLOGGED
                ), 37668, 1));
        registry.register(BlockType.of(BlockTypeKeys.HEAVY_WEIGHTED_PRESSURE_PLATE, List.of(
                    BlockProperties.POWER
                ), 13427, 0));
        registry.register(BlockType.of(BlockTypeKeys.HONEY_BLOCK, List.of(), 26002, 0));
        registry.register(BlockType.of(BlockTypeKeys.HONEYCOMB_BLOCK, List.of(), 26003, 0));
        registry.register(BlockType.of(BlockTypeKeys.HOPPER, List.of(
                    BlockProperties.ENABLED,
                    BlockProperties.HOPPER_FACING
                ), 13493, 0));
        registry.register(BlockType.of(BlockTypeKeys.HORN_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19351, 0));
        registry.register(BlockType.of(BlockTypeKeys.HORN_CORAL_BLOCK, List.of(), 19332, 0));
        registry.register(BlockType.of(BlockTypeKeys.HORN_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19371, 0));
        registry.register(BlockType.of(BlockTypeKeys.HORN_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19445, 0));
        registry.register(BlockType.of(BlockTypeKeys.ICE, List.of(), 8597, 0));
        registry.register(BlockType.of(BlockTypeKeys.ICE_CRYSTAL, List.of(
                    BlockProperties.FACING,
                    BlockProperties.WATERLOGGED
                ), 27589, 9));
        registry.register(BlockType.of(BlockTypeKeys.ICICLE, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.THICKNESS,
                    BlockProperties.VERTICAL_DIRECTION,
                    BlockProperties.WATERLOGGED
                ), 35643, 25));
        registry.register(BlockType.of(BlockTypeKeys.INFESTED_CHISELED_STONE_BRICKS, List.of(), 9912, 0));
        registry.register(BlockType.of(BlockTypeKeys.INFESTED_COBBLESTONE, List.of(), 9908, 0));
        registry.register(BlockType.of(BlockTypeKeys.INFESTED_CRACKED_STONE_BRICKS, List.of(), 9911, 0));
        registry.register(BlockType.of(BlockTypeKeys.INFESTED_DEEPSLATE, List.of(
                    BlockProperties.AXIS
                ), 37540, 1));
        registry.register(BlockType.of(BlockTypeKeys.INFESTED_MOSSY_STONE_BRICKS, List.of(), 9910, 0));
        registry.register(BlockType.of(BlockTypeKeys.INFESTED_STONE, List.of(), 9907, 0));
        registry.register(BlockType.of(BlockTypeKeys.INFESTED_STONE_BRICKS, List.of(), 9909, 0));
        registry.register(BlockType.of(BlockTypeKeys.IRON_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10105, 31));
        registry.register(BlockType.of(BlockTypeKeys.IRON_BLOCK, List.of(), 3831, 0));
        registry.register(BlockType.of(BlockTypeKeys.IRON_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10393, 3));
        registry.register(BlockType.of(BlockTypeKeys.IRON_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 8465, 11));
        registry.register(BlockType.of(BlockTypeKeys.IRON_ORE, List.of(), 134, 0));
        registry.register(BlockType.of(BlockTypeKeys.IRON_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 14827, 15));
        registry.register(BlockType.of(BlockTypeKeys.JACK_O_LANTERN, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 9106, 0));
        registry.register(BlockType.of(BlockTypeKeys.JIGSAW, List.of(
                    BlockProperties.ORIENTATION
                ), 25912, 10));
        registry.register(BlockType.of(BlockTypeKeys.JUKEBOX, List.of(
                    BlockProperties.HAS_RECORD,
                    BlockProperties.PLAYLIST_MODE
                ), 9044, 3));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12903, 9));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16924, 11));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16540, 31));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16220, 7));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7823, 49));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 351, 27));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_LOG, List.of(
                    BlockProperties.AXIS
                ), 148, 1));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_PLANKS, List.of(), 18, 0));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8535, 1));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 36, 0));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4476, 9));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7051, 17));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16020, 3));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 12043, 11));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9389, 15));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8375, 1));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7455, 1));
        registry.register(BlockType.of(BlockTypeKeys.JUNGLE_WOOD, List.of(
                    BlockProperties.AXIS
                ), 219, 1));
        registry.register(BlockType.of(BlockTypeKeys.KELP, List.of(
                    BlockProperties.AGE_0_25
                ), 19248, 0));
        registry.register(BlockType.of(BlockTypeKeys.KELP_PLANT, List.of(), 19274, 0));
        registry.register(BlockType.of(BlockTypeKeys.LADDER, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7307, 1));
        registry.register(BlockType.of(BlockTypeKeys.LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25023, 3));
        registry.register(BlockType.of(BlockTypeKeys.LAPIS_BLOCK, List.of(), 664, 0));
        registry.register(BlockType.of(BlockTypeKeys.LAPIS_ORE, List.of(), 662, 0));
        registry.register(BlockType.of(BlockTypeKeys.LARGE_AMETHYST_BUD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.WATERLOGGED
                ), 27614, 9));
        registry.register(BlockType.of(BlockTypeKeys.LARGE_FERN, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 15597, 1));
        registry.register(BlockType.of(BlockTypeKeys.LAVA, List.of(
                    BlockProperties.LEVEL_0_15
                ), 105, 0));
        registry.register(BlockType.of(BlockTypeKeys.LAVA_CAULDRON, List.of(), 11611, 0));
        registry.register(BlockType.of(BlockTypeKeys.LEAF_LITTER, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.SEGMENT_AMOUNT
                ), 35813, 0));
        registry.register(BlockType.of(BlockTypeKeys.LECTERN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HAS_BOOK,
                    BlockProperties.POWERED
                ), 24970, 3));
        registry.register(BlockType.of(BlockTypeKeys.LEVER, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 8439, 9));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT, List.of(
                    BlockProperties.LEVEL_0_15,
                    BlockProperties.WATERLOGGED
                ), 14795, 31));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15647, 8));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2078, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27346, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27562, 1));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_CARPET, List.of(), 15159, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_CONCRETE, List.of(), 17843, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_CONCRETE_POWDER, List.of(), 19235, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19154, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18096, 11));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17788, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17698, 4));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_STAINED_GLASS, List.of(), 9184, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13736, 31));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_TERRACOTTA, List.of(), 13627, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15867, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_WOOL, List.of(), 2412, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3723, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_BLUE_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2665, 11));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15727, 8));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2158, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27426, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27572, 1));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_CARPET, List.of(), 15164, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_CONCRETE, List.of(), 17848, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_CONCRETE_POWDER, List.of(), 19240, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19184, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18496, 11));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17808, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17728, 4));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_STAINED_GLASS, List.of(), 9189, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13896, 31));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_TERRACOTTA, List.of(), 13632, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15887, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_WOOL, List.of(), 2417, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3753, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_GRAY_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3065, 11));
        registry.register(BlockType.of(BlockTypeKeys.LIGHT_WEIGHTED_PRESSURE_PLATE, List.of(
                    BlockProperties.POWER
                ), 13411, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35450, 19));
        registry.register(BlockType.of(BlockTypeKeys.LILAC, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 15589, 1));
        registry.register(BlockType.of(BlockTypeKeys.LILY_OF_THE_VALLEY, List.of(), 3827, 0));
        registry.register(BlockType.of(BlockTypeKeys.LILY_PAD, List.of(), 11067, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15679, 8));
        registry.register(BlockType.of(BlockTypeKeys.LIME_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2110, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIME_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27378, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIME_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27566, 1));
    }

    private static void register4(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.LIME_CARPET, List.of(), 15161, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_CONCRETE, List.of(), 17845, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_CONCRETE_POWDER, List.of(), 19237, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19166, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIME_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18256, 11));
        registry.register(BlockType.of(BlockTypeKeys.LIME_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17796, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17710, 4));
        registry.register(BlockType.of(BlockTypeKeys.LIME_STAINED_GLASS, List.of(), 9186, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13800, 31));
        registry.register(BlockType.of(BlockTypeKeys.LIME_TERRACOTTA, List.of(), 13629, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15875, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_WOOL, List.of(), 2414, 0));
        registry.register(BlockType.of(BlockTypeKeys.LIME_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3735, 3));
        registry.register(BlockType.of(BlockTypeKeys.LIME_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2825, 11));
        registry.register(BlockType.of(BlockTypeKeys.LODESTONE, List.of(), 26016, 0));
        registry.register(BlockType.of(BlockTypeKeys.LOOM, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 24924, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15631, 8));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2062, 3));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27330, 3));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27560, 1));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_CARPET, List.of(), 15158, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_CONCRETE, List.of(), 17842, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_CONCRETE_POWDER, List.of(), 19234, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19148, 3));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18016, 11));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17784, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17692, 4));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_STAINED_GLASS, List.of(), 9183, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13704, 31));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_TERRACOTTA, List.of(), 13626, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15863, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_WOOL, List.of(), 2411, 0));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3717, 3));
        registry.register(BlockType.of(BlockTypeKeys.MAGENTA_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2585, 11));
        registry.register(BlockType.of(BlockTypeKeys.MAGMA_BLOCK, List.of(), 17655, 0));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13023, 9));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 17244, 11));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16700, 31));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16380, 7));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 8143, 49));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 491, 27));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_LOG, List.of(
                    BlockProperties.AXIS
                ), 163, 1));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_PLANKS, List.of(), 26, 0));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8545, 1));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_PROPAGULE, List.of(
                    BlockProperties.AGE_0_4,
                    BlockProperties.HANGING,
                    BlockProperties.STAGE,
                    BlockProperties.WATERLOGGED
                ), 46, 5));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_ROOTS, List.of(
                    BlockProperties.WATERLOGGED
                ), 169, 1));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4540, 9));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7147, 17));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16050, 3));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14472, 11));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9709, 15));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8399, 1));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7479, 1));
        registry.register(BlockType.of(BlockTypeKeys.MANGROVE_WOOD, List.of(
                    BlockProperties.AXIS
                ), 231, 1));
        registry.register(BlockType.of(BlockTypeKeys.MEDIUM_AMETHYST_BUD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.WATERLOGGED
                ), 27626, 9));
        registry.register(BlockType.of(BlockTypeKeys.MELON, List.of(), 10480, 0));
        registry.register(BlockType.of(BlockTypeKeys.MELON_STEM, List.of(
                    BlockProperties.AGE_0_7
                ), 10497, 0));
        registry.register(BlockType.of(BlockTypeKeys.MOSS_BLOCK, List.of(), 35829, 0));
        registry.register(BlockType.of(BlockTypeKeys.MOSS_CARPET, List.of(), 35780, 0));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_COBBLESTONE, List.of(), 4924, 0));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_COBBLESTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20626, 3));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_COBBLESTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19802, 11));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_COBBLESTONE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 12460, 3));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_STONE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20614, 3));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_STONE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19642, 11));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_STONE_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 21652, 3));
        registry.register(BlockType.of(BlockTypeKeys.MOSSY_STONE_BRICKS, List.of(), 9902, 0));
        registry.register(BlockType.of(BlockTypeKeys.MOVING_PISTON, List.of(
                    BlockProperties.PISTON_TYPE,
                    BlockProperties.FACING
                ), new int[] {3801, 3803, 3805, 3807, 3809, 3811, 3802, 3804, 3806, 3808, 3810, 3812}, 0));
        registry.register(BlockType.of(BlockTypeKeys.MUD, List.of(), 35889, 0));
        registry.register(BlockType.of(BlockTypeKeys.MUD_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16116, 3));
        registry.register(BlockType.of(BlockTypeKeys.MUD_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 10985, 11));
        registry.register(BlockType.of(BlockTypeKeys.MUD_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 22624, 3));
        registry.register(BlockType.of(BlockTypeKeys.MUD_BRICKS, List.of(), 9906, 0));
        registry.register(BlockType.of(BlockTypeKeys.MUDDY_MANGROVE_ROOTS, List.of(
                    BlockProperties.AXIS
                ), 171, 1));
        registry.register(BlockType.of(BlockTypeKeys.MUSHROOM_STEM, List.of(
                    BlockProperties.DOWN,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WEST
                ), 10041, 0));
        registry.register(BlockType.of(BlockTypeKeys.MYCELIUM, List.of(
                    BlockProperties.SNOWY
                ), 11065, 1));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_BRICK_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 11482, 31));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16122, 3));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 11514, 11));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 22948, 3));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_BRICKS, List.of(), 11481, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_GOLD_ORE, List.of(), 138, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_PORTAL, List.of(
                    BlockProperties.HORIZONTAL_AXIS
                ), 9100, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_QUARTZ_ORE, List.of(), 13492, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_SPROUTS, List.of(), 25147, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_WART, List.of(
                    BlockProperties.AGE_0_3
                ), 11594, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHER_WART_BLOCK, List.of(), 17656, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHERITE_BLOCK, List.of(), 26004, 0));
        registry.register(BlockType.of(BlockTypeKeys.NETHERRACK, List.of(), 9080, 0));
        registry.register(BlockType.of(BlockTypeKeys.NOTE_BLOCK, List.of(
                    BlockProperties.INSTRUMENT,
                    BlockProperties.NOTE,
                    BlockProperties.POWERED
                ), 680, 1));
        registry.register(BlockType.of(BlockTypeKeys.OAK_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12831, 9));
        registry.register(BlockType.of(BlockTypeKeys.OAK_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 7243, 11));
        registry.register(BlockType.of(BlockTypeKeys.OAK_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 9048, 31));
        registry.register(BlockType.of(BlockTypeKeys.OAK_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 10793, 7));
        registry.register(BlockType.of(BlockTypeKeys.OAK_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7503, 49));
        registry.register(BlockType.of(BlockTypeKeys.OAK_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 267, 27));
        registry.register(BlockType.of(BlockTypeKeys.OAK_LOG, List.of(
                    BlockProperties.AXIS
                ), 139, 1));
        registry.register(BlockType.of(BlockTypeKeys.OAK_PLANKS, List.of(), 15, 0));
        registry.register(BlockType.of(BlockTypeKeys.OAK_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8529, 1));
        registry.register(BlockType.of(BlockTypeKeys.OAK_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 30, 0));
        registry.register(BlockType.of(BlockTypeKeys.OAK_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4604, 9));
        registry.register(BlockType.of(BlockTypeKeys.OAK_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 6891, 17));
        registry.register(BlockType.of(BlockTypeKeys.OAK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16002, 3));
        registry.register(BlockType.of(BlockTypeKeys.OAK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 5463, 11));
        registry.register(BlockType.of(BlockTypeKeys.OAK_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9197, 15));
        registry.register(BlockType.of(BlockTypeKeys.OAK_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8335, 1));
        registry.register(BlockType.of(BlockTypeKeys.OAK_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7415, 1));
        registry.register(BlockType.of(BlockTypeKeys.OAK_WOOD, List.of(
                    BlockProperties.AXIS
                ), 210, 1));
        registry.register(BlockType.of(BlockTypeKeys.OBSERVER, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED
                ), 17662, 5));
        registry.register(BlockType.of(BlockTypeKeys.OBSIDIAN, List.of(), 4925, 0));
        registry.register(BlockType.of(BlockTypeKeys.OCHRE_FROGLIGHT, List.of(
                    BlockProperties.AXIS
                ), 37549, 1));
        registry.register(BlockType.of(BlockTypeKeys.OPEN_EYEBLOSSOM, List.of(), 37835, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15615, 8));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2046, 3));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27314, 3));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27558, 1));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_CARPET, List.of(), 15157, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_CONCRETE, List.of(), 17841, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_CONCRETE_POWDER, List.of(), 19233, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19142, 3));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 17936, 11));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17780, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_POPLAR_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 547, 27));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17686, 4));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_STAINED_GLASS, List.of(), 9182, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13672, 31));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_TERRACOTTA, List.of(), 13625, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_TULIP, List.of(), 3821, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15859, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_WOOL, List.of(), 2410, 0));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3711, 3));
        registry.register(BlockType.of(BlockTypeKeys.ORANGE_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2505, 11));
        registry.register(BlockType.of(BlockTypeKeys.OXEYE_DAISY, List.of(), 3824, 0));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_CHISELED_COPPER, List.of(), 33237, 0));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER, List.of(), 33219, 0));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10233, 31));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34982, 3));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10417, 3));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35074, 35075, 35080, 35081, 35086, 35087, 35092, 35093, 35076, 35077, 35082, 35083, 35088, 35089, 35094, 35095, 35078, 35079, 35084, 35085, 35090, 35091, 35096, 35097}, 1));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 34122, 11));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35290, 1));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34960, 1));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25043, 3));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34634, 15));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_CUT_COPPER, List.of(), 33229, 0));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33900, 3));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33482, 11));
        registry.register(BlockType.of(BlockTypeKeys.OXIDIZED_LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35522, 19));
        registry.register(BlockType.of(BlockTypeKeys.PACKED_ICE, List.of(), 15174, 0));
        registry.register(BlockType.of(BlockTypeKeys.PACKED_ICE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 15176, 3));
    }

    private static void register5(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.PACKED_ICE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 15182, 11));
        registry.register(BlockType.of(BlockTypeKeys.PACKED_ICE_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 15262, 3));
        registry.register(BlockType.of(BlockTypeKeys.PACKED_ICE_BRICKS, List.of(), 15175, 0));
        registry.register(BlockType.of(BlockTypeKeys.PACKED_MUD, List.of(), 9905, 0));
        registry.register(BlockType.of(BlockTypeKeys.PALE_HANGING_MOSS, List.of(
                    BlockProperties.TIP
                ), 37833, 0));
        registry.register(BlockType.of(BlockTypeKeys.PALE_MOSS_BLOCK, List.of(), 37670, 0));
        registry.register(BlockType.of(BlockTypeKeys.PALE_MOSS_CARPET, List.of(
                    BlockProperties.BOTTOM,
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.WALL_WEST
                ), 37671, 0));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12999, 9));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 17180, 11));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16668, 31));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16348, 7));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7951, 49));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 463, 27));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_LOG, List.of(
                    BlockProperties.AXIS
                ), 160, 1));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_PLANKS, List.of(), 25, 0));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8543, 1));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 44, 0));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4668, 9));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7115, 17));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16044, 3));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14392, 11));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9645, 15));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8391, 1));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7471, 1));
        registry.register(BlockType.of(BlockTypeKeys.PALE_OAK_WOOD, List.of(
                    BlockProperties.AXIS
                ), 22, 1));
        registry.register(BlockType.of(BlockTypeKeys.PEARLESCENT_FROGLIGHT, List.of(
                    BlockProperties.AXIS
                ), 37555, 1));
        registry.register(BlockType.of(BlockTypeKeys.PEONY, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 15593, 1));
        registry.register(BlockType.of(BlockTypeKeys.PETRIFIED_OAK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16092, 3));
        registry.register(BlockType.of(BlockTypeKeys.PIGLIN_HEAD, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.ROTATION
                ), 13335, 16));
        registry.register(BlockType.of(BlockTypeKeys.PIGLIN_WALL_HEAD, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13367, 1));
        registry.register(BlockType.of(BlockTypeKeys.PINK_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15695, 8));
        registry.register(BlockType.of(BlockTypeKeys.PINK_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2126, 3));
        registry.register(BlockType.of(BlockTypeKeys.PINK_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27394, 3));
        registry.register(BlockType.of(BlockTypeKeys.PINK_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27568, 1));
        registry.register(BlockType.of(BlockTypeKeys.PINK_CARPET, List.of(), 15162, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_CONCRETE, List.of(), 17846, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_CONCRETE_POWDER, List.of(), 19238, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19172, 3));
        registry.register(BlockType.of(BlockTypeKeys.PINK_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18336, 11));
        registry.register(BlockType.of(BlockTypeKeys.PINK_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17800, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_PETALS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.FLOWER_AMOUNT
                ), 35781, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17716, 4));
        registry.register(BlockType.of(BlockTypeKeys.PINK_STAINED_GLASS, List.of(), 9187, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13832, 31));
        registry.register(BlockType.of(BlockTypeKeys.PINK_TERRACOTTA, List.of(), 13630, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_TULIP, List.of(), 3823, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15879, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_WOOL, List.of(), 2415, 0));
        registry.register(BlockType.of(BlockTypeKeys.PINK_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3741, 3));
        registry.register(BlockType.of(BlockTypeKeys.PINK_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2905, 11));
        registry.register(BlockType.of(BlockTypeKeys.PISTON, List.of(
                    BlockProperties.EXTENDED,
                    BlockProperties.FACING
                ), 2373, 6));
        registry.register(BlockType.of(BlockTypeKeys.PISTON_HEAD, List.of(
                    BlockProperties.PISTON_TYPE,
                    BlockProperties.FACING,
                    BlockProperties.SHORT
                ), new int[] {2385, 2387, 2389, 2391, 2393, 2395, 2397, 2399, 2401, 2403, 2405, 2407, 2386, 2388, 2390, 2392, 2394, 2396, 2398, 2400, 2402, 2404, 2406, 2408}, 1));
        registry.register(BlockType.of(BlockTypeKeys.PITCHER_CROP, List.of(
                    BlockProperties.AGE_0_4,
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 17605, 1));
        registry.register(BlockType.of(BlockTypeKeys.PITCHER_PLANT, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 17615, 1));
        registry.register(BlockType.of(BlockTypeKeys.PLAYER_HEAD, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.ROTATION
                ), 13215, 16));
        registry.register(BlockType.of(BlockTypeKeys.PLAYER_WALL_HEAD, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13247, 1));
        registry.register(BlockType.of(BlockTypeKeys.PODZOL, List.of(
                    BlockProperties.SNOWY
                ), 12, 1));
        registry.register(BlockType.of(BlockTypeKeys.POINTED_DRIPSTONE, List.of(
                    BlockProperties.THICKNESS,
                    BlockProperties.VERTICAL_DIRECTION,
                    BlockProperties.WATERLOGGED
                ), 35683, 5));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_ANDESITE, List.of(), 7, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_ANDESITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20668, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_ANDESITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 20442, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BASALT, List.of(
                    BlockProperties.AXIS
                ), 9086, 1));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE, List.of(), 26428, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 26432, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 26438, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 26518, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_BRICKS, List.of(), 26429, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 26931, 9));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 26929, 1));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 26923, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 26843, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_BLACKSTONE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 26955, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE, List.of(), 31772, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 32184, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 32190, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 32270, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE_BRICKS, List.of(), 32183, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 31773, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 31779, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CALCITE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 31859, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CINNABAR, List.of(), 30535, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CINNABAR_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 30536, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CINNABAR_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 30542, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_CINNABAR_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 30622, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_DEEPSLATE, List.of(), 36304, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_DEEPSLATE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 36385, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_DEEPSLATE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 36305, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_DEEPSLATE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 36391, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_DIORITE, List.of(), 5, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_DIORITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20620, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_DIORITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19722, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_GRANITE, List.of(), 3, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_GRANITE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20602, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_GRANITE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19482, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_SULFUR, List.of(), 29301, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_SULFUR_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 29302, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_SULFUR_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 29308, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_SULFUR_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 29388, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_TUFF, List.of(), 28061, 0));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_TUFF_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 28062, 3));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_TUFF_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 28068, 11));
        registry.register(BlockType.of(BlockTypeKeys.POLISHED_TUFF_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 28148, 3));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13047, 9));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 17308, 11));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16732, 31));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16412, 7));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 8207, 49));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_LOG, List.of(
                    BlockProperties.AXIS
                ), 166, 1));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_PLANKS, List.of(), 27, 0));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8547, 1));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 86, 0));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4732, 9));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7179, 17));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16056, 3));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14552, 11));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9773, 15));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8407, 1));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7487, 1));
        registry.register(BlockType.of(BlockTypeKeys.POPLAR_WOOD, List.of(
                    BlockProperties.AXIS
                ), 234, 1));
        registry.register(BlockType.of(BlockTypeKeys.POPPY, List.of(), 3816, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTATOES, List.of(
                    BlockProperties.AGE_0_7
                ), 12823, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTENT_SULFUR, List.of(
                    BlockProperties.POTENT_SULFUR_STATE
                ), 28886, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_ACACIA_SAPLING, List.of(), 12790, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_ALLIUM, List.of(), 12801, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_AZALEA_BUSH, List.of(), 37547, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_AZURE_BLUET, List.of(), 12802, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_BAMBOO, List.of(), 19477, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_BIRCH_SAPLING, List.of(), 12788, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_BLUE_ORCHID, List.of(), 12800, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_BROWN_MUSHROOM, List.of(), 12812, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_CACTUS, List.of(), 12814, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_CHERRY_SAPLING, List.of(), 12791, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_CLOSED_EYEBLOSSOM, List.of(), 37838, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_CORNFLOWER, List.of(), 12808, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_CRIMSON_FUNGUS, List.of(), 26012, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_CRIMSON_ROOTS, List.of(), 26014, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_DANDELION, List.of(), 12797, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_DARK_OAK_SAPLING, List.of(), 12792, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_DEAD_BUSH, List.of(), 12813, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_FERN, List.of(), 12796, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_FLOWERING_AZALEA_BUSH, List.of(), 37548, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_GOLDEN_DANDELION, List.of(), 12798, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_JUNGLE_SAPLING, List.of(), 12789, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_LILY_OF_THE_VALLEY, List.of(), 12809, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_MANGROVE_PROPAGULE, List.of(), 12795, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_OAK_SAPLING, List.of(), 12786, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_OPEN_EYEBLOSSOM, List.of(), 37837, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_ORANGE_TULIP, List.of(), 12804, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_OXEYE_DAISY, List.of(), 12807, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_PALE_OAK_SAPLING, List.of(), 12793, 0));
    }

    private static void register6(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.POTTED_PINK_TULIP, List.of(), 12806, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_POPLAR_SAPLING, List.of(), 12794, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_POPPY, List.of(), 12799, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_RED_MUSHROOM, List.of(), 12811, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_RED_TULIP, List.of(), 12803, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_SPRUCE_SAPLING, List.of(), 12787, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_TORCHFLOWER, List.of(), 12785, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_WARPED_FUNGUS, List.of(), 26013, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_WARPED_ROOTS, List.of(), 26015, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_WHITE_TULIP, List.of(), 12805, 0));
        registry.register(BlockType.of(BlockTypeKeys.POTTED_WITHER_ROSE, List.of(), 12810, 0));
        registry.register(BlockType.of(BlockTypeKeys.POWDER_SNOW, List.of(), 32595, 0));
        registry.register(BlockType.of(BlockTypeKeys.POWDER_SNOW_CAULDRON, List.of(
                    BlockProperties.LEVEL_1_3
                ), 11612, 0));
        registry.register(BlockType.of(BlockTypeKeys.POWERED_RAIL, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.STRAIGHT_RAIL_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2302, 13));
        registry.register(BlockType.of(BlockTypeKeys.PRISMARINE, List.of(), 14891, 0));
        registry.register(BlockType.of(BlockTypeKeys.PRISMARINE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 15140, 3));
        registry.register(BlockType.of(BlockTypeKeys.PRISMARINE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14974, 11));
        registry.register(BlockType.of(BlockTypeKeys.PRISMARINE_BRICKS, List.of(), 14892, 0));
        registry.register(BlockType.of(BlockTypeKeys.PRISMARINE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 15134, 3));
        registry.register(BlockType.of(BlockTypeKeys.PRISMARINE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 14894, 11));
        registry.register(BlockType.of(BlockTypeKeys.PRISMARINE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 21004, 3));
        registry.register(BlockType.of(BlockTypeKeys.PUMPKIN, List.of(), 10479, 0));
        registry.register(BlockType.of(BlockTypeKeys.PUMPKIN_STEM, List.of(
                    BlockProperties.AGE_0_7
                ), 10489, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15759, 8));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2190, 3));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27458, 3));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27576, 1));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_CARPET, List.of(), 15166, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_CONCRETE, List.of(), 17850, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_CONCRETE_POWDER, List.of(), 19242, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19196, 3));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18656, 11));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17816, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17740, 4));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_STAINED_GLASS, List.of(), 9191, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13960, 31));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_TERRACOTTA, List.of(), 13634, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15895, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_WOOL, List.of(), 2419, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3765, 3));
        registry.register(BlockType.of(BlockTypeKeys.PURPLE_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3225, 11));
        registry.register(BlockType.of(BlockTypeKeys.PURPUR_BLOCK, List.of(), 17512, 0));
        registry.register(BlockType.of(BlockTypeKeys.PURPUR_PILLAR, List.of(
                    BlockProperties.AXIS
                ), 17519, 1));
        registry.register(BlockType.of(BlockTypeKeys.PURPUR_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 17513, 3));
        registry.register(BlockType.of(BlockTypeKeys.PURPUR_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 17522, 11));
        registry.register(BlockType.of(BlockTypeKeys.QUARTZ_BLOCK, List.of(), 13503, 0));
        registry.register(BlockType.of(BlockTypeKeys.QUARTZ_BRICKS, List.of(), 27281, 0));
        registry.register(BlockType.of(BlockTypeKeys.QUARTZ_PILLAR, List.of(
                    BlockProperties.AXIS
                ), 13505, 1));
        registry.register(BlockType.of(BlockTypeKeys.QUARTZ_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16128, 3));
        registry.register(BlockType.of(BlockTypeKeys.QUARTZ_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 13508, 11));
        registry.register(BlockType.of(BlockTypeKeys.RAIL, List.of(
                    BlockProperties.RAIL_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 7315, 1));
        registry.register(BlockType.of(BlockTypeKeys.RAW_COPPER_BLOCK, List.of(), 37545, 0));
        registry.register(BlockType.of(BlockTypeKeys.RAW_GOLD_BLOCK, List.of(), 37546, 0));
        registry.register(BlockType.of(BlockTypeKeys.RAW_IRON_BLOCK, List.of(), 37544, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15823, 8));
        registry.register(BlockType.of(BlockTypeKeys.RED_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2254, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27522, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27584, 1));
        registry.register(BlockType.of(BlockTypeKeys.RED_CARPET, List.of(), 15170, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_CONCRETE, List.of(), 17854, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_CONCRETE_POWDER, List.of(), 19246, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19220, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18976, 11));
        registry.register(BlockType.of(BlockTypeKeys.RED_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17832, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_MUSHROOM, List.of(), 3829, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_MUSHROOM_BLOCK, List.of(
                    BlockProperties.DOWN,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WEST
                ), 9977, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_NETHER_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20662, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_NETHER_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 20362, 11));
        registry.register(BlockType.of(BlockTypeKeys.RED_NETHER_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 23596, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_NETHER_BRICKS, List.of(), 17657, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_POPLAR_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 519, 27));
        registry.register(BlockType.of(BlockTypeKeys.RED_SAND, List.of(), 126, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_SANDSTONE, List.of(), 15919, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_SANDSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16134, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_SANDSTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 15922, 11));
        registry.register(BlockType.of(BlockTypeKeys.RED_SANDSTONE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 21328, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_SHRUB, List.of(), 2367, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17764, 4));
        registry.register(BlockType.of(BlockTypeKeys.RED_STAINED_GLASS, List.of(), 9195, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 14088, 31));
        registry.register(BlockType.of(BlockTypeKeys.RED_TERRACOTTA, List.of(), 13638, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_TULIP, List.of(), 3820, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15911, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_WOOL, List.of(), 2423, 0));
        registry.register(BlockType.of(BlockTypeKeys.RED_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3789, 3));
        registry.register(BlockType.of(BlockTypeKeys.RED_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 3545, 11));
        registry.register(BlockType.of(BlockTypeKeys.REDSTONE_BLOCK, List.of(), 13491, 0));
        registry.register(BlockType.of(BlockTypeKeys.REDSTONE_LAMP, List.of(
                    BlockProperties.LIT
                ), 11626, 1));
        registry.register(BlockType.of(BlockTypeKeys.REDSTONE_ORE, List.of(
                    BlockProperties.LIT
                ), 8551, 1));
        registry.register(BlockType.of(BlockTypeKeys.REDSTONE_TORCH, List.of(
                    BlockProperties.LIT
                ), 8555, 0));
        registry.register(BlockType.of(BlockTypeKeys.REDSTONE_WALL_TORCH, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.LIT
                ), 8557, 0));
        registry.register(BlockType.of(BlockTypeKeys.REDSTONE_WIRE, List.of(
                    BlockProperties.REDSTONE_EAST,
                    BlockProperties.REDSTONE_NORTH,
                    BlockProperties.POWER,
                    BlockProperties.REDSTONE_SOUTH,
                    BlockProperties.REDSTONE_WEST
                ), 5567, 1160));
        registry.register(BlockType.of(BlockTypeKeys.REINFORCED_DEEPSLATE, List.of(), 37559, 0));
        registry.register(BlockType.of(BlockTypeKeys.REPEATER, List.of(
                    BlockProperties.DELAY,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.LOCKED,
                    BlockProperties.POWERED
                ), 9117, 3));
        registry.register(BlockType.of(BlockTypeKeys.REPEATING_COMMAND_BLOCK, List.of(
                    BlockProperties.CONDITIONAL,
                    BlockProperties.FACING
                ), 17623, 6));
        registry.register(BlockType.of(BlockTypeKeys.RESIN_BLOCK, List.of(), 11068, 0));
        registry.register(BlockType.of(BlockTypeKeys.RESIN_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 11150, 3));
        registry.register(BlockType.of(BlockTypeKeys.RESIN_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 11070, 11));
        registry.register(BlockType.of(BlockTypeKeys.RESIN_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 11156, 3));
        registry.register(BlockType.of(BlockTypeKeys.RESIN_BRICKS, List.of(), 11069, 0));
        registry.register(BlockType.of(BlockTypeKeys.RESIN_CLUMP, List.of(
                    BlockProperties.DOWN,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10665, 127));
        registry.register(BlockType.of(BlockTypeKeys.RESPAWN_ANCHOR, List.of(
                    BlockProperties.CHARGES
                ), 26007, 0));
        registry.register(BlockType.of(BlockTypeKeys.ROOTED_DIRT, List.of(), 35888, 0));
        registry.register(BlockType.of(BlockTypeKeys.ROSE_BUSH, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 15591, 1));
        registry.register(BlockType.of(BlockTypeKeys.SAND, List.of(), 121, 0));
        registry.register(BlockType.of(BlockTypeKeys.SANDSTONE, List.of(), 677, 0));
        registry.register(BlockType.of(BlockTypeKeys.SANDSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16080, 3));
        registry.register(BlockType.of(BlockTypeKeys.SANDSTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 11648, 11));
        registry.register(BlockType.of(BlockTypeKeys.SANDSTONE_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 23920, 3));
        registry.register(BlockType.of(BlockTypeKeys.SCAFFOLDING, List.of(
                    BlockProperties.BOTTOM,
                    BlockProperties.DISTANCE_0_7,
                    BlockProperties.WATERLOGGED
                ), 24892, 31));
        registry.register(BlockType.of(BlockTypeKeys.SCULK, List.of(), 33076, 0));
        registry.register(BlockType.of(BlockTypeKeys.SCULK_CATALYST, List.of(
                    BlockProperties.BLOOM
                ), 33205, 1));
        registry.register(BlockType.of(BlockTypeKeys.SCULK_SENSOR, List.of(
                    BlockProperties.POWER,
                    BlockProperties.SCULK_SENSOR_PHASE,
                    BlockProperties.WATERLOGGED
                ), 32596, 1));
        registry.register(BlockType.of(BlockTypeKeys.SCULK_SHRIEKER, List.of(
                    BlockProperties.CAN_SUMMON,
                    BlockProperties.SHRIEKING,
                    BlockProperties.WATERLOGGED
                ), 33207, 7));
        registry.register(BlockType.of(BlockTypeKeys.SCULK_VEIN, List.of(
                    BlockProperties.DOWN,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 33077, 127));
        registry.register(BlockType.of(BlockTypeKeys.SEA_LANTERN, List.of(), 15152, 0));
        registry.register(BlockType.of(BlockTypeKeys.SEA_PICKLE, List.of(
                    BlockProperties.PICKLES,
                    BlockProperties.WATERLOGGED
                ), 19453, 0));
        registry.register(BlockType.of(BlockTypeKeys.SEAGRASS, List.of(), 2370, 0));
        registry.register(BlockType.of(BlockTypeKeys.SHELF_MUSHROOM, List.of(
                    BlockProperties.AGE_0_1,
                    BlockProperties.HORIZONTAL_FACING
                ), 11640, 0));
        registry.register(BlockType.of(BlockTypeKeys.SHORT_DRY_GRASS, List.of(), 2368, 0));
        registry.register(BlockType.of(BlockTypeKeys.SHORT_GRASS, List.of(), 2363, 0));
        registry.register(BlockType.of(BlockTypeKeys.SHROOMLIGHT, List.of(), 25162, 0));
        registry.register(BlockType.of(BlockTypeKeys.SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17674, 4));
        registry.register(BlockType.of(BlockTypeKeys.SKELETON_SKULL, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.ROTATION
                ), 13095, 16));
        registry.register(BlockType.of(BlockTypeKeys.SKELETON_WALL_SKULL, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13127, 1));
        registry.register(BlockType.of(BlockTypeKeys.SLIME_BLOCK, List.of(), 14792, 0));
        registry.register(BlockType.of(BlockTypeKeys.SMALL_AMETHYST_BUD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.WATERLOGGED
                ), 27638, 9));
        registry.register(BlockType.of(BlockTypeKeys.SMALL_DRIPLEAF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.WATERLOGGED
                ), 35870, 3));
        registry.register(BlockType.of(BlockTypeKeys.SMITHING_TABLE, List.of(), 24986, 0));
        registry.register(BlockType.of(BlockTypeKeys.SMOKER, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.LIT
                ), 24940, 1));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_BASALT, List.of(), 37543, 0));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_QUARTZ, List.of(), 16154, 0));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_QUARTZ_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20644, 3));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_QUARTZ_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 20122, 11));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_RED_SANDSTONE, List.of(), 16155, 0));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_RED_SANDSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20608, 3));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_RED_SANDSTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19562, 11));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_SANDSTONE, List.of(), 16153, 0));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_SANDSTONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 20638, 3));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_SANDSTONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 20042, 11));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_STONE, List.of(), 16146, 0));
        registry.register(BlockType.of(BlockTypeKeys.SMOOTH_STONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16147, 3));
        registry.register(BlockType.of(BlockTypeKeys.SNIFFER_EGG, List.of(
                    BlockProperties.HATCH
                ), 19288, 0));
        registry.register(BlockType.of(BlockTypeKeys.SNOW, List.of(
                    BlockProperties.LAYERS
                ), 8589, 0));
        registry.register(BlockType.of(BlockTypeKeys.SNOW_BLOCK, List.of(), 8598, 0));
        registry.register(BlockType.of(BlockTypeKeys.SNOW_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 8600, 3));
        registry.register(BlockType.of(BlockTypeKeys.SNOW_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 8606, 11));
        registry.register(BlockType.of(BlockTypeKeys.SNOW_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 8686, 3));
        registry.register(BlockType.of(BlockTypeKeys.SNOW_BRICKS, List.of(), 8599, 0));
        registry.register(BlockType.of(BlockTypeKeys.SOUL_CAMPFIRE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.LIT,
                    BlockProperties.SIGNAL_FIRE,
                    BlockProperties.WATERLOGGED
                ), 25095, 3));
    }

    private static void register7(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.SOUL_FIRE, List.of(), 5443, 0));
        registry.register(BlockType.of(BlockTypeKeys.SOUL_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25027, 3));
        registry.register(BlockType.of(BlockTypeKeys.SOUL_SAND, List.of(), 9081, 0));
        registry.register(BlockType.of(BlockTypeKeys.SOUL_SOIL, List.of(), 9082, 0));
        registry.register(BlockType.of(BlockTypeKeys.SOUL_TORCH, List.of(), 9089, 0));
        registry.register(BlockType.of(BlockTypeKeys.SOUL_WALL_TORCH, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 9090, 0));
        registry.register(BlockType.of(BlockTypeKeys.SPAWNER, List.of(), 5444, 0));
        registry.register(BlockType.of(BlockTypeKeys.SPONGE, List.of(), 659, 0));
        registry.register(BlockType.of(BlockTypeKeys.SPORE_BLOSSOM, List.of(), 35777, 0));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 12855, 9));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16796, 11));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 16476, 31));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 16156, 7));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 7567, 49));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 295, 27));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_LOG, List.of(
                    BlockProperties.AXIS
                ), 142, 1));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_PLANKS, List.of(), 16, 0));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8531, 1));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_SAPLING, List.of(
                    BlockProperties.STAGE
                ), 32, 0));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4796, 9));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 6923, 17));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16008, 3));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 11883, 11));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 9261, 15));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8343, 1));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 7423, 1));
        registry.register(BlockType.of(BlockTypeKeys.SPRUCE_WOOD, List.of(
                    BlockProperties.AXIS
                ), 213, 1));
        registry.register(BlockType.of(BlockTypeKeys.STICKY_PISTON, List.of(
                    BlockProperties.EXTENDED,
                    BlockProperties.FACING
                ), 2350, 6));
        registry.register(BlockType.of(BlockTypeKeys.STONE, List.of(), 1, 0));
        registry.register(BlockType.of(BlockTypeKeys.STONE_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16110, 3));
        registry.register(BlockType.of(BlockTypeKeys.STONE_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 10905, 11));
        registry.register(BlockType.of(BlockTypeKeys.STONE_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 22300, 3));
        registry.register(BlockType.of(BlockTypeKeys.STONE_BRICKS, List.of(), 9901, 0));
        registry.register(BlockType.of(BlockTypeKeys.STONE_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 8565, 9));
        registry.register(BlockType.of(BlockTypeKeys.STONE_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 8463, 1));
        registry.register(BlockType.of(BlockTypeKeys.STONE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 16074, 3));
        registry.register(BlockType.of(BlockTypeKeys.STONE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 19962, 11));
        registry.register(BlockType.of(BlockTypeKeys.STONECUTTER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 24987, 0));
        registry.register(BlockType.of(BlockTypeKeys.STRAW_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2286, 3));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_ACACIA_LOG, List.of(
                    BlockProperties.AXIS
                ), 186, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_ACACIA_WOOD, List.of(
                    BlockProperties.AXIS
                ), 249, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_BAMBOO_BLOCK, List.of(
                    BlockProperties.AXIS
                ), 207, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_BIRCH_LOG, List.of(
                    BlockProperties.AXIS
                ), 180, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_BIRCH_WOOD, List.of(
                    BlockProperties.AXIS
                ), 243, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_CHERRY_LOG, List.of(
                    BlockProperties.AXIS
                ), 189, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_CHERRY_WOOD, List.of(
                    BlockProperties.AXIS
                ), 252, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_CRIMSON_HYPHAE, List.of(
                    BlockProperties.AXIS
                ), 25157, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_CRIMSON_STEM, List.of(
                    BlockProperties.AXIS
                ), 25151, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_DARK_OAK_LOG, List.of(
                    BlockProperties.AXIS
                ), 192, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_DARK_OAK_WOOD, List.of(
                    BlockProperties.AXIS
                ), 255, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_JUNGLE_LOG, List.of(
                    BlockProperties.AXIS
                ), 183, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_JUNGLE_WOOD, List.of(
                    BlockProperties.AXIS
                ), 246, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_MANGROVE_LOG, List.of(
                    BlockProperties.AXIS
                ), 201, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_MANGROVE_WOOD, List.of(
                    BlockProperties.AXIS
                ), 261, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_OAK_LOG, List.of(
                    BlockProperties.AXIS
                ), 198, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_OAK_WOOD, List.of(
                    BlockProperties.AXIS
                ), 237, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_PALE_OAK_LOG, List.of(
                    BlockProperties.AXIS
                ), 195, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_PALE_OAK_WOOD, List.of(
                    BlockProperties.AXIS
                ), 258, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_POPLAR_LOG, List.of(
                    BlockProperties.AXIS
                ), 204, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_POPLAR_WOOD, List.of(
                    BlockProperties.AXIS
                ), 264, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_SPRUCE_LOG, List.of(
                    BlockProperties.AXIS
                ), 177, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_SPRUCE_WOOD, List.of(
                    BlockProperties.AXIS
                ), 240, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_WARPED_HYPHAE, List.of(
                    BlockProperties.AXIS
                ), 25140, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRIPPED_WARPED_STEM, List.of(
                    BlockProperties.AXIS
                ), 25134, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRUCTURE_BLOCK, List.of(
                    BlockProperties.STRUCTURE_BLOCK_MODE
                ), 25908, 1));
        registry.register(BlockType.of(BlockTypeKeys.STRUCTURE_VOID, List.of(), 17661, 0));
        registry.register(BlockType.of(BlockTypeKeys.SUGAR_CANE, List.of(
                    BlockProperties.AGE_0_15
                ), 9028, 0));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR, List.of(), 28885, 0));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 29713, 3));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 29719, 11));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 29799, 3));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_BRICKS, List.of(), 29712, 0));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 28891, 3));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_SPIKE, List.of(
                    BlockProperties.THICKNESS,
                    BlockProperties.VERTICAL_DIRECTION,
                    BlockProperties.WATERLOGGED
                ), 35703, 5));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 28897, 11));
        registry.register(BlockType.of(BlockTypeKeys.SULFUR_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 28977, 3));
        registry.register(BlockType.of(BlockTypeKeys.SUNFLOWER, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 15587, 1));
        registry.register(BlockType.of(BlockTypeKeys.SUSPICIOUS_GRAVEL, List.of(
                    BlockProperties.DUSTED
                ), 128, 0));
        registry.register(BlockType.of(BlockTypeKeys.SUSPICIOUS_SAND, List.of(
                    BlockProperties.DUSTED
                ), 122, 0));
        registry.register(BlockType.of(BlockTypeKeys.SWEET_BERRY_BUSH, List.of(
                    BlockProperties.AGE_0_3
                ), 25127, 0));
        registry.register(BlockType.of(BlockTypeKeys.TALL_DRY_GRASS, List.of(), 2369, 0));
        registry.register(BlockType.of(BlockTypeKeys.TALL_GRASS, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 15595, 1));
        registry.register(BlockType.of(BlockTypeKeys.TALL_SEAGRASS, List.of(
                    BlockProperties.DOUBLE_BLOCK_HALF
                ), 2371, 1));
        registry.register(BlockType.of(BlockTypeKeys.TARGET, List.of(
                    BlockProperties.POWER
                ), 25938, 0));
        registry.register(BlockType.of(BlockTypeKeys.TERRACOTTA, List.of(), 15172, 0));
        registry.register(BlockType.of(BlockTypeKeys.TEST_BLOCK, List.of(
                    BlockProperties.TEST_BLOCK_MODE
                ), 25924, 0));
        registry.register(BlockType.of(BlockTypeKeys.TEST_INSTANCE_BLOCK, List.of(), 25928, 0));
        registry.register(BlockType.of(BlockTypeKeys.TINTED_GLASS, List.of(), 32594, 0));
        registry.register(BlockType.of(BlockTypeKeys.TNT, List.of(
                    BlockProperties.UNSTABLE
                ), 3833, 1));
        registry.register(BlockType.of(BlockTypeKeys.TORCH, List.of(), 4926, 0));
        registry.register(BlockType.of(BlockTypeKeys.TORCHFLOWER, List.of(), 3815, 0));
        registry.register(BlockType.of(BlockTypeKeys.TORCHFLOWER_CROP, List.of(
                    BlockProperties.AGE_0_1
                ), 17603, 0));
        registry.register(BlockType.of(BlockTypeKeys.TRAPPED_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {13387, 13388, 13393, 13394, 13399, 13400, 13405, 13406, 13389, 13390, 13395, 13396, 13401, 13402, 13407, 13408, 13391, 13392, 13397, 13398, 13403, 13404, 13409, 13410}, 1));
        registry.register(BlockType.of(BlockTypeKeys.TRIAL_SPAWNER, List.of(
                    BlockProperties.OMINOUS,
                    BlockProperties.TRIAL_SPAWNER_STATE
                ), 37624, 6));
        registry.register(BlockType.of(BlockTypeKeys.TRIPWIRE, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.DISARMED,
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.POWERED,
                    BlockProperties.SOUTH,
                    BlockProperties.WEST
                ), 11754, 127));
        registry.register(BlockType.of(BlockTypeKeys.TRIPWIRE_HOOK, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 11738, 9));
        registry.register(BlockType.of(BlockTypeKeys.TUBE_CORAL, List.of(
                    BlockProperties.WATERLOGGED
                ), 19343, 0));
        registry.register(BlockType.of(BlockTypeKeys.TUBE_CORAL_BLOCK, List.of(), 19328, 0));
        registry.register(BlockType.of(BlockTypeKeys.TUBE_CORAL_FAN, List.of(
                    BlockProperties.WATERLOGGED
                ), 19363, 0));
        registry.register(BlockType.of(BlockTypeKeys.TUBE_CORAL_WALL_FAN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 19413, 0));
        registry.register(BlockType.of(BlockTypeKeys.TUFF, List.of(), 27650, 0));
        registry.register(BlockType.of(BlockTypeKeys.TUFF_BRICK_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 28474, 3));
        registry.register(BlockType.of(BlockTypeKeys.TUFF_BRICK_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 28480, 11));
        registry.register(BlockType.of(BlockTypeKeys.TUFF_BRICK_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 28560, 3));
        registry.register(BlockType.of(BlockTypeKeys.TUFF_BRICKS, List.of(), 28473, 0));
        registry.register(BlockType.of(BlockTypeKeys.TUFF_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 27651, 3));
        registry.register(BlockType.of(BlockTypeKeys.TUFF_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 27657, 11));
        registry.register(BlockType.of(BlockTypeKeys.TUFF_WALL, List.of(
                    BlockProperties.WALL_EAST,
                    BlockProperties.WALL_NORTH,
                    BlockProperties.WALL_SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WALL_WEST
                ), 27737, 3));
        registry.register(BlockType.of(BlockTypeKeys.TURTLE_EGG, List.of(
                    BlockProperties.EGGS,
                    BlockProperties.HATCH
                ), 19276, 0));
        registry.register(BlockType.of(BlockTypeKeys.TWISTING_VINES, List.of(
                    BlockProperties.AGE_0_25
                ), 25190, 0));
        registry.register(BlockType.of(BlockTypeKeys.TWISTING_VINES_PLANT, List.of(), 25216, 0));
        registry.register(BlockType.of(BlockTypeKeys.VAULT, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OMINOUS,
                    BlockProperties.VAULT_STATE
                ), 37636, 4));
        registry.register(BlockType.of(BlockTypeKeys.VERDANT_FROGLIGHT, List.of(
                    BlockProperties.AXIS
                ), 37552, 1));
        registry.register(BlockType.of(BlockTypeKeys.VINE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.UP,
                    BlockProperties.WEST
                ), 10505, 31));
        registry.register(BlockType.of(BlockTypeKeys.VOID_AIR, List.of(), 19478, 0));
        registry.register(BlockType.of(BlockTypeKeys.WALL_TORCH, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 4927, 0));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_BUTTON, List.of(
                    BlockProperties.FACE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 25676, 9));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 25764, 11));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_FENCE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 25268, 31));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_FENCE_GATE, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.IN_WALL,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 25460, 7));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_FUNGUS, List.of(), 25144, 0));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_HANGING_SIGN, List.of(
                    BlockProperties.ATTACHED,
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 8079, 49));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_HYPHAE, List.of(
                    BlockProperties.AXIS
                ), 25137, 1));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_NYLIUM, List.of(), 25143, 0));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_PLANKS, List.of(), 25219, 0));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_PRESSURE_PLATE, List.of(
                    BlockProperties.POWERED
                ), 25234, 1));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_ROOTS, List.of(), 25146, 0));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_SHELF, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED,
                    BlockProperties.SIDE_CHAIN,
                    BlockProperties.WATERLOGGED
                ), 4860, 9));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_SIGN, List.of(
                    BlockProperties.ROTATION,
                    BlockProperties.WATERLOGGED
                ), 25860, 17));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 25226, 3));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 25572, 11));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_STEM, List.of(
                    BlockProperties.AXIS
                ), 25131, 1));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 25364, 15));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_WALL_HANGING_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 8423, 1));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_WALL_SIGN, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 25900, 1));
        registry.register(BlockType.of(BlockTypeKeys.WARPED_WART_BLOCK, List.of(), 25145, 0));
        registry.register(BlockType.of(BlockTypeKeys.WATER, List.of(
                    BlockProperties.LEVEL_0_15
                ), 89, 0));
        registry.register(BlockType.of(BlockTypeKeys.WATER_CAULDRON, List.of(
                    BlockProperties.LEVEL_1_3
                ), 11608, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_CHISELED_COPPER, List.of(), 33238, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10265, 31));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_BLOCK, List.of(), 33220, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34986, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10423, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35098, 35099, 35104, 35105, 35110, 35111, 35116, 35117, 35100, 35101, 35106, 35107, 35112, 35113, 35118, 35119, 35102, 35103, 35108, 35109, 35114, 35115, 35120, 35121}, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 34186, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35322, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34962, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25047, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34698, 15));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_CUT_COPPER, List.of(), 33230, 0));
    }

    private static void register8(final BlockRegistry registry) {
        registry.register(BlockType.of(BlockTypeKeys.WAXED_CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33906, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33562, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_CHISELED_COPPER, List.of(), 33239, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER, List.of(), 33221, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10297, 31));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34990, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10429, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35122, 35123, 35128, 35129, 35134, 35135, 35140, 35141, 35124, 35125, 35130, 35131, 35136, 35137, 35142, 35143, 35126, 35127, 35132, 35133, 35138, 35139, 35144, 35145}, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 34250, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35354, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34964, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25051, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34762, 15));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_CUT_COPPER, List.of(), 33231, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33912, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33642, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_EXPOSED_LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35570, 19));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35546, 19));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_CHISELED_COPPER, List.of(), 33241, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER, List.of(), 33223, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10361, 31));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34998, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10441, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35170, 35171, 35176, 35177, 35182, 35183, 35188, 35189, 35172, 35173, 35178, 35179, 35184, 35185, 35190, 35191, 35174, 35175, 35180, 35181, 35186, 35187, 35192, 35193}, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 34378, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35418, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34968, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25059, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34890, 15));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_CUT_COPPER, List.of(), 33233, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33924, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33802, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_OXIDIZED_LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35618, 19));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_CHISELED_COPPER, List.of(), 33240, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER, List.of(), 33222, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10329, 31));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34994, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10435, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35146, 35147, 35152, 35153, 35158, 35159, 35164, 35165, 35148, 35149, 35154, 35155, 35160, 35161, 35166, 35167, 35150, 35151, 35156, 35157, 35162, 35163, 35168, 35169}, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 34314, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35386, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34966, 1));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25055, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34826, 15));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_CUT_COPPER, List.of(), 33232, 0));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33918, 3));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33722, 11));
        registry.register(BlockType.of(BlockTypeKeys.WAXED_WEATHERED_LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35594, 19));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_CHISELED_COPPER, List.of(), 33236, 0));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER, List.of(), 33218, 0));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_BARS, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 10201, 31));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_BULB, List.of(
                    BlockProperties.LIT,
                    BlockProperties.POWERED
                ), 34978, 3));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_CHAIN, List.of(
                    BlockProperties.AXIS,
                    BlockProperties.WATERLOGGED
                ), 10411, 3));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_CHEST, List.of(
                    BlockProperties.CHEST_TYPE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), new int[] {35050, 35051, 35056, 35057, 35062, 35063, 35068, 35069, 35052, 35053, 35058, 35059, 35064, 35065, 35070, 35071, 35054, 35055, 35060, 35061, 35066, 35067, 35072, 35073}, 1));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_DOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.DOUBLE_BLOCK_HALF,
                    BlockProperties.HINGE,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED
                ), 34058, 11));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_GOLEM_STATUE, List.of(
                    BlockProperties.COPPER_GOLEM_POSE,
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.WATERLOGGED
                ), 35258, 1));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_GRATE, List.of(
                    BlockProperties.WATERLOGGED
                ), 34958, 1));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_LANTERN, List.of(
                    BlockProperties.HANGING,
                    BlockProperties.WATERLOGGED
                ), 25039, 3));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_COPPER_TRAPDOOR, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.OPEN,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 34570, 15));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_CUT_COPPER, List.of(), 33228, 0));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_CUT_COPPER_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 33894, 3));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_CUT_COPPER_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 33402, 11));
        registry.register(BlockType.of(BlockTypeKeys.WEATHERED_LIGHTNING_ROD, List.of(
                    BlockProperties.FACING,
                    BlockProperties.POWERED,
                    BlockProperties.WATERLOGGED
                ), 35498, 19));
        registry.register(BlockType.of(BlockTypeKeys.WEEPING_VINES, List.of(
                    BlockProperties.AGE_0_25
                ), 25163, 0));
        registry.register(BlockType.of(BlockTypeKeys.WEEPING_VINES_PLANT, List.of(), 25189, 0));
        registry.register(BlockType.of(BlockTypeKeys.WET_SPONGE, List.of(), 660, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHEAT, List.of(
                    BlockProperties.AGE_0_7
                ), 6867, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15599, 8));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2030, 3));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27298, 3));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27556, 1));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_CARPET, List.of(), 15156, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_CONCRETE, List.of(), 17840, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_CONCRETE_POWDER, List.of(), 19232, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19136, 3));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 17856, 11));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17776, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17680, 4));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_STAINED_GLASS, List.of(), 9181, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13640, 31));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_TERRACOTTA, List.of(), 13624, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_TULIP, List.of(), 3822, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15855, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_WOOL, List.of(), 2409, 0));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3705, 3));
        registry.register(BlockType.of(BlockTypeKeys.WHITE_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2425, 11));
        registry.register(BlockType.of(BlockTypeKeys.WILDFLOWERS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.FLOWER_AMOUNT
                ), 35797, 0));
        registry.register(BlockType.of(BlockTypeKeys.WITHER_ROSE, List.of(), 3826, 0));
        registry.register(BlockType.of(BlockTypeKeys.WITHER_SKELETON_SKULL, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.ROTATION
                ), 13135, 16));
        registry.register(BlockType.of(BlockTypeKeys.WITHER_SKELETON_WALL_SKULL, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13167, 1));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_BANNER, List.of(
                    BlockProperties.ROTATION
                ), 15663, 8));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_BED, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.OCCUPIED,
                    BlockProperties.PART
                ), 2094, 3));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_CANDLE, List.of(
                    BlockProperties.CANDLES,
                    BlockProperties.LIT,
                    BlockProperties.WATERLOGGED
                ), 27362, 3));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_CANDLE_CAKE, List.of(
                    BlockProperties.LIT
                ), 27564, 1));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_CARPET, List.of(), 15160, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_CONCRETE, List.of(), 17844, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_CONCRETE_POWDER, List.of(), 19236, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_CONCRETE_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 19160, 3));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_CONCRETE_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 18176, 11));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_GLAZED_TERRACOTTA, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 17792, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_POPLAR_LEAVES, List.of(
                    BlockProperties.DISTANCE_1_7,
                    BlockProperties.PERSISTENT,
                    BlockProperties.WATERLOGGED
                ), 575, 27));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_SHULKER_BOX, List.of(
                    BlockProperties.FACING
                ), 17704, 4));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_STAINED_GLASS, List.of(), 9185, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_STAINED_GLASS_PANE, List.of(
                    BlockProperties.EAST,
                    BlockProperties.NORTH,
                    BlockProperties.SOUTH,
                    BlockProperties.WATERLOGGED,
                    BlockProperties.WEST
                ), 13768, 31));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_TERRACOTTA, List.of(), 13628, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_WALL_BANNER, List.of(
                    BlockProperties.HORIZONTAL_FACING
                ), 15871, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_WOOL, List.of(), 2413, 0));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_WOOL_SLAB, List.of(
                    BlockProperties.SLAB_TYPE,
                    BlockProperties.WATERLOGGED
                ), 3729, 3));
        registry.register(BlockType.of(BlockTypeKeys.YELLOW_WOOL_STAIRS, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.HALF,
                    BlockProperties.STAIRS_SHAPE,
                    BlockProperties.WATERLOGGED
                ), 2745, 11));
        registry.register(BlockType.of(BlockTypeKeys.ZOMBIE_HEAD, List.of(
                    BlockProperties.POWERED,
                    BlockProperties.ROTATION
                ), 13175, 16));
        registry.register(BlockType.of(BlockTypeKeys.ZOMBIE_WALL_HEAD, List.of(
                    BlockProperties.HORIZONTAL_FACING,
                    BlockProperties.POWERED
                ), 13207, 1));
    }

    /**
     * Registers every generated block type with the given registry.
     *
     * @param registry the registry to populate
     */
    public static void registerAll(final BlockRegistry registry) {
        Stream.of(
            (Consumer<BlockRegistry>) BlockStateIds::register0,
            BlockStateIds::register1,
            BlockStateIds::register2,
            BlockStateIds::register3,
            BlockStateIds::register4,
            BlockStateIds::register5,
            BlockStateIds::register6,
            BlockStateIds::register7,
            BlockStateIds::register8
        )
        .parallel()
        .forEach(fn -> fn.accept(registry));
    }
}
