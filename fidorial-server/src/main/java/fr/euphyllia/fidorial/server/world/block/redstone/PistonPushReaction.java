package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntityTypes;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.type.Piston;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * How each block reacts to a piston, and whether a piston can move it.
 *
 * <p>Vanilla declares the reaction block by block in its code, and none of its data reports carries
 * it: the tables below are written by hand, from the tags where one fits and block by block
 * otherwise. A block they do not name moves along, unless it holds a block entity.</p>
 */
final class PistonPushReaction {

    /**
     * The blocks no tool breaks, a hardness of {@code -1} in vanilla: a piston does not move them
     * either.
     */
    private static final Set<TypedKey<BlockType>> UNBREAKABLE = Set.of(
            BlockTypeKeys.BARRIER, BlockTypeKeys.BEDROCK, BlockTypeKeys.COMMAND_BLOCK, BlockTypeKeys.CHAIN_COMMAND_BLOCK,
            BlockTypeKeys.REPEATING_COMMAND_BLOCK, BlockTypeKeys.END_GATEWAY, BlockTypeKeys.END_PORTAL,
            BlockTypeKeys.END_PORTAL_FRAME, BlockTypeKeys.JIGSAW, BlockTypeKeys.LIGHT, BlockTypeKeys.MOVING_PISTON,
            BlockTypeKeys.NETHER_PORTAL, BlockTypeKeys.STRUCTURE_BLOCK, BlockTypeKeys.TEST_BLOCK,
            BlockTypeKeys.TEST_INSTANCE_BLOCK);

    /**
     * The blocks vanilla keeps a piston from moving by name, whatever their reaction.
     */
    private static final Set<TypedKey<BlockType>> NEVER_PUSHED = Set.of(
            BlockTypeKeys.OBSIDIAN, BlockTypeKeys.CRYING_OBSIDIAN, BlockTypeKeys.RESPAWN_ANCHOR,
            BlockTypeKeys.REINFORCED_DEEPSLATE);

    private static final Set<TypedKey<BlockType>> BLOCKING = Set.of(
            BlockTypeKeys.PISTON_HEAD, BlockTypeKeys.LODESTONE, BlockTypeKeys.GRINDSTONE, BlockTypeKeys.ENCHANTING_TABLE,
            BlockTypeKeys.BEACON, BlockTypeKeys.SPAWNER, BlockTypeKeys.ENDER_CHEST);

    private static final List<TagKey<BlockType>> BLOCKING_TAGS = List.of(
            BlockTypeTagKeys.ANVIL, BlockTypeTagKeys.PORTALS);

    private static final Set<TypedKey<BlockType>> DESTROYED = Set.of(
            // plants
            BlockTypeKeys.SHORT_GRASS, BlockTypeKeys.FERN, BlockTypeKeys.DEAD_BUSH, BlockTypeKeys.BUSH,
            BlockTypeKeys.TALL_GRASS, BlockTypeKeys.LARGE_FERN, BlockTypeKeys.SEAGRASS, BlockTypeKeys.TALL_SEAGRASS,
            BlockTypeKeys.SHORT_DRY_GRASS, BlockTypeKeys.TALL_DRY_GRASS, BlockTypeKeys.FIREFLY_BUSH,
            BlockTypeKeys.BROWN_MUSHROOM, BlockTypeKeys.RED_MUSHROOM, BlockTypeKeys.CRIMSON_FUNGUS,
            BlockTypeKeys.WARPED_FUNGUS, BlockTypeKeys.CRIMSON_ROOTS, BlockTypeKeys.WARPED_ROOTS,
            BlockTypeKeys.NETHER_SPROUTS, BlockTypeKeys.NETHER_WART, BlockTypeKeys.SWEET_BERRY_BUSH, BlockTypeKeys.COCOA,
            BlockTypeKeys.MELON_STEM, BlockTypeKeys.PUMPKIN_STEM, BlockTypeKeys.ATTACHED_MELON_STEM,
            BlockTypeKeys.ATTACHED_PUMPKIN_STEM, BlockTypeKeys.PITCHER_PLANT, BlockTypeKeys.SUGAR_CANE,
            BlockTypeKeys.CACTUS, BlockTypeKeys.CACTUS_FLOWER, BlockTypeKeys.BAMBOO, BlockTypeKeys.BAMBOO_SAPLING,
            BlockTypeKeys.KELP, BlockTypeKeys.KELP_PLANT, BlockTypeKeys.VINE, BlockTypeKeys.GLOW_LICHEN,
            BlockTypeKeys.SCULK_VEIN, BlockTypeKeys.RESIN_CLUMP, BlockTypeKeys.WEEPING_VINES,
            BlockTypeKeys.WEEPING_VINES_PLANT, BlockTypeKeys.TWISTING_VINES, BlockTypeKeys.TWISTING_VINES_PLANT,
            BlockTypeKeys.HANGING_ROOTS, BlockTypeKeys.SPORE_BLOSSOM, BlockTypeKeys.PALE_HANGING_MOSS,
            BlockTypeKeys.AZALEA, BlockTypeKeys.FLOWERING_AZALEA, BlockTypeKeys.MOSS_CARPET,
            BlockTypeKeys.PALE_MOSS_CARPET, BlockTypeKeys.PINK_PETALS, BlockTypeKeys.WILDFLOWERS,
            BlockTypeKeys.LEAF_LITTER, BlockTypeKeys.LILY_PAD, BlockTypeKeys.BIG_DRIPLEAF,
            BlockTypeKeys.BIG_DRIPLEAF_STEM, BlockTypeKeys.SMALL_DRIPLEAF, BlockTypeKeys.SEA_PICKLE,
            BlockTypeKeys.CHORUS_PLANT, BlockTypeKeys.CHORUS_FLOWER, BlockTypeKeys.MANGROVE_PROPAGULE,
            BlockTypeKeys.MELON, BlockTypeKeys.PUMPKIN, BlockTypeKeys.CARVED_PUMPKIN, BlockTypeKeys.JACK_O_LANTERN,
            // dead corals, which no tag holds
            BlockTypeKeys.DEAD_TUBE_CORAL, BlockTypeKeys.DEAD_BRAIN_CORAL, BlockTypeKeys.DEAD_BUBBLE_CORAL,
            BlockTypeKeys.DEAD_FIRE_CORAL, BlockTypeKeys.DEAD_HORN_CORAL, BlockTypeKeys.DEAD_TUBE_CORAL_FAN,
            BlockTypeKeys.DEAD_BRAIN_CORAL_FAN, BlockTypeKeys.DEAD_BUBBLE_CORAL_FAN, BlockTypeKeys.DEAD_FIRE_CORAL_FAN,
            BlockTypeKeys.DEAD_HORN_CORAL_FAN, BlockTypeKeys.DEAD_TUBE_CORAL_WALL_FAN,
            BlockTypeKeys.DEAD_BRAIN_CORAL_WALL_FAN, BlockTypeKeys.DEAD_BUBBLE_CORAL_WALL_FAN,
            BlockTypeKeys.DEAD_FIRE_CORAL_WALL_FAN, BlockTypeKeys.DEAD_HORN_CORAL_WALL_FAN,
            // redstone and what hangs on a block
            BlockTypeKeys.TORCH, BlockTypeKeys.WALL_TORCH, BlockTypeKeys.SOUL_TORCH, BlockTypeKeys.SOUL_WALL_TORCH,
            BlockTypeKeys.REDSTONE_TORCH, BlockTypeKeys.REDSTONE_WALL_TORCH, BlockTypeKeys.COPPER_TORCH,
            BlockTypeKeys.COPPER_WALL_TORCH, BlockTypeKeys.REDSTONE_WIRE, BlockTypeKeys.REPEATER,
            BlockTypeKeys.COMPARATOR, BlockTypeKeys.LEVER, BlockTypeKeys.TRIPWIRE, BlockTypeKeys.TRIPWIRE_HOOK,
            BlockTypeKeys.LADDER, BlockTypeKeys.BELL, BlockTypeKeys.SCAFFOLDING,
            // fluids and what gives way
            BlockTypeKeys.WATER, BlockTypeKeys.LAVA, BlockTypeKeys.BUBBLE_COLUMN, BlockTypeKeys.SNOW,
            BlockTypeKeys.COBWEB, BlockTypeKeys.STRUCTURE_VOID,
            // fragile blocks
            BlockTypeKeys.DRAGON_EGG, BlockTypeKeys.TURTLE_EGG, BlockTypeKeys.FROGSPAWN, BlockTypeKeys.CAKE,
            BlockTypeKeys.DECORATED_POT, BlockTypeKeys.BUDDING_AMETHYST, BlockTypeKeys.AMETHYST_CLUSTER,
            BlockTypeKeys.LARGE_AMETHYST_BUD, BlockTypeKeys.MEDIUM_AMETHYST_BUD, BlockTypeKeys.SMALL_AMETHYST_BUD);

    private static final List<TagKey<BlockType>> DESTROYED_TAGS = List.of(
            BlockTypeTagKeys.REPLACEABLE, BlockTypeTagKeys.LEAVES, BlockTypeTagKeys.SAPLINGS, BlockTypeTagKeys.FLOWERS,
            BlockTypeTagKeys.SMALL_FLOWERS, BlockTypeTagKeys.CROPS, BlockTypeTagKeys.CAVE_VINES, BlockTypeTagKeys.FIRE,
            BlockTypeTagKeys.CORALS, BlockTypeTagKeys.CORAL_PLANTS, BlockTypeTagKeys.WALL_CORALS,
            BlockTypeTagKeys.DOORS, BlockTypeTagKeys.BUTTONS, BlockTypeTagKeys.PRESSURE_PLATES, BlockTypeTagKeys.BEDS,
            BlockTypeTagKeys.CANDLES, BlockTypeTagKeys.CANDLE_CAKES, BlockTypeTagKeys.FLOWER_POTS,
            BlockTypeTagKeys.SKULLS, BlockTypeTagKeys.SHULKER_BOXES, BlockTypeTagKeys.LANTERNS,
            BlockTypeTagKeys.SPELEOTHEMS);

    private PistonPushReaction() {
        throw new UnsupportedOperationException("PistonPushReaction cannot be instantiated.");
    }

    /**
     * {@return how a state reacts to a piston}
     *
     * @param state the state
     */
    static Reaction of(final BlockData state) {
        final TypedKey<BlockType> key = state.key();
        if (state.isAir()) {
            return Reaction.NORMAL;
        }
        if (BLOCKING.contains(key) || UNBREAKABLE.contains(key) || NEVER_PUSHED.contains(key)) {
            return Reaction.BLOCK;
        }
        if (DESTROYED.contains(key)) {
            return Reaction.DESTROY;
        }
        if (!FidorialServer.isInitialized()) {
            return Reaction.NORMAL;
        }
        final ServerRegistry<BlockType> blocks = FidorialServer.getInstance().registries().registry(RegistryKey.BLOCK);
        if (blocks.isTagged(key, BlockTypeTagKeys.GLAZED_TERRACOTTA)) {
            return Reaction.PUSH_ONLY;
        }
        for (final TagKey<BlockType> tag : BLOCKING_TAGS) {
            if (blocks.isTagged(key, tag)) {
                return Reaction.BLOCK;
            }
        }
        for (final TagKey<BlockType> tag : DESTROYED_TAGS) {
            if (blocks.isTagged(key, tag)) {
                return Reaction.DESTROY;
            }
        }
        return Reaction.NORMAL;
    }

    /**
     * {@return {@code true} if a piston can move a block}
     *
     * @param world        the world
     * @param state        the state of the block, {@code null} for one that is not loaded or that the
     *                     calling thread does not own, which never moves
     * @param position     where the block is
     * @param movement     the side the block would move towards
     * @param allowDestroy {@code true} if a block the piston would break counts as moved
     * @param pistonFacing the side the piston faces, the only one a block that is only ever pushed
     *                     moves towards
     */
    static boolean isPushable(final World world, final @Nullable BlockData state, final BlockPosition position, final BlockFace movement,
                              final boolean allowDestroy, final BlockFace pistonFacing) {
        final int minY = world.minY();
        final int maxY = minY + world.height() - 1;
        if (state == null || position.blockY() < minY || position.blockY() > maxY) {
            return false;
        }
        if (state.isAir()) {
            return true;
        }
        final TypedKey<BlockType> key = state.key();
        if (NEVER_PUSHED.contains(key)) {
            return false;
        }
        if ((movement == BlockFace.DOWN && position.blockY() == minY) || (movement == BlockFace.UP && position.blockY() == maxY)) {
            return false;
        }
        if (state instanceof final Piston piston && isPistonBase(state)) {
            if (piston.isExtended()) {
                return false;
            }
        } else {
            if (UNBREAKABLE.contains(key)) {
                return false;
            }
            switch (of(state)) {
                case BLOCK -> {
                    return false;
                }
                case DESTROY -> {
                    return allowDestroy;
                }
                case PUSH_ONLY -> {
                    return movement == pistonFacing;
                }
                case NORMAL -> {
                }
            }
        }
        return BlockEntityTypes.typeIdentifier(key) == null;
    }

    /**
     * {@return {@code true} for the base of a piston, sticky or not}
     *
     * @param state a state
     */
    static boolean isPistonBase(final BlockData state) {
        return state.key().equals(BlockTypeKeys.PISTON) || state.key().equals(BlockTypeKeys.STICKY_PISTON);
    }

    /**
     * What a piston does to a block in its way.
     */
    enum Reaction {
        /**
         * The block moves along.
         */
        NORMAL,
        /**
         * The block breaks.
         */
        DESTROY,
        /**
         * The block stays, and the piston with it.
         */
        BLOCK,
        /**
         * The block is pushed, but a sticky piston or a slime block does not pull it.
         */
        PUSH_ONLY
    }
}
