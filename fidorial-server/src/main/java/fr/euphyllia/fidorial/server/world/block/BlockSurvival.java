package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.redstone.SupportType;

import java.util.Map;

/**
 * Tells whether a block can stand at a place, as a mob that puts a block down checks: a flower, a
 * mushroom or a nether plant needs a block of its support tag below it, the farming blocks follow
 * their own rules, and any other block stands anywhere.
 */
public final class BlockSurvival {

    /**
     * The plants that only stand on the blocks of a tag, but for the small flowers.
     */
    private static final Map<TypedKey<BlockType>, TagKey<BlockType>> SUPPORT_TAGS = Map.of(
            BlockTypeKeys.WITHER_ROSE, BlockTypeTagKeys.SUPPORTS_WITHER_ROSE,
            BlockTypeKeys.CRIMSON_FUNGUS, BlockTypeTagKeys.SUPPORTS_CRIMSON_FUNGUS,
            BlockTypeKeys.WARPED_FUNGUS, BlockTypeTagKeys.SUPPORTS_WARPED_FUNGUS,
            BlockTypeKeys.CRIMSON_ROOTS, BlockTypeTagKeys.SUPPORTS_CRIMSON_ROOTS,
            BlockTypeKeys.WARPED_ROOTS, BlockTypeTagKeys.SUPPORTS_WARPED_ROOTS
    );

    private BlockSurvival() {
        throw new UnsupportedOperationException("BlockSurvival cannot be instantiated.");
    }

    /**
     * {@return {@code true} if a block can stand at a place}: a small flower on a block of the
     * {@code minecraft:supports_vegetation} tag, a mushroom on a block whose top holds something by
     * its center, the wither rose and the nether plants on the blocks of their own support tag, a
     * farming block by its rules
     *
     * @param world    the world of the place
     * @param position the place
     * @param state    the block that would stand there
     */
    public static boolean canSurvive(final ServerWorld world, final BlockPosition position, final BlockState state) {
        final BlockState below = BlockView.blockAt(world, position.blockX(), position.blockY() - 1, position.blockZ());
        if (below == null) {
            return false;
        }
        final TagKey<BlockType> support = SUPPORT_TAGS.get(state.name());
        if (support != null) {
            return BlockView.isTagged(support, below);
        }
        if (BlockView.isTagged(BlockTypeTagKeys.SMALL_FLOWERS, state)) {
            return BlockView.isTagged(BlockTypeTagKeys.SUPPORTS_VEGETATION, below);
        }
        final FidorialServer server = FidorialServer.getInstance();
        if (state.name().equals(BlockTypeKeys.BROWN_MUSHROOM) || state.name().equals(BlockTypeKeys.RED_MUSHROOM)) {
            return BlockSupport.isFaceSturdy(server.registries().blockStates().resolve(below), BlockFace.UP, SupportType.CENTER);
        }
        final BlockData data = server.registries().blockStates().resolve(state);
        return data == null || server.worlds().farming().world(world).canSurvive(position, data);
    }
}
