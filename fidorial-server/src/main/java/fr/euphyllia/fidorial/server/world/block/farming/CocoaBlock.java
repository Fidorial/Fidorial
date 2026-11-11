package fr.euphyllia.fidorial.server.world.block.farming;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A cocoa pod: it hangs from the side of a block of the {@code minecraft:supports_cocoa} tag, which
 * it faces, and ripens one stage every fifth random tick on average, in any light.
 */
public final class CocoaBlock implements BlockBehaviour, FarmingBehaviour {

    public static final CocoaBlock INSTANCE = new CocoaBlock();

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.COCOA;

    private static final int GROWTH_ODDS = 5;

    private CocoaBlock() {
    }

    @Override
    public TypedKey<BlockType> key() {
        return KEY;
    }

    @Override
    public BlockType type() {
        final BlockType type = Blocks.type(KEY);
        Preconditions.checkState(type != null, "The block type %s is not registered", KEY);
        return type;
    }

    /**
     * Hangs the pod from the block the player clicked the side of, or failing that from the one
     * they look towards.
     */
    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        if (!(type().defaultData() instanceof final Directional pod)) {
            return null;
        }
        final BlockFace clicked = context.clickedFace();
        if (clicked != BlockFace.UP && clicked != BlockFace.DOWN) {
            return pod.withFacing(clicked.opposite());
        }
        return pod.withFacing(context.horizontalFacing());
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(KEY);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(KEY);
    }

    @Override
    public boolean ticksRandomly(final BlockData state) {
        return Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public void randomTick(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        if (world.random().nextInt(GROWTH_ODDS) == 0) {
            world.grow(position, Farming.withAge(state, Farming.age(state) + 1));
        }
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return state instanceof final Directional pod
                && world.isTagged(position.relative(pod.getFacing()), BlockTypeTagKeys.SUPPORTS_COCOA);
    }

    @Override
    public boolean canFertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return Farming.age(state) < Farming.maxAge(state);
    }

    @Override
    public void fertilize(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        world.grow(position, position, Farming.withAge(state, Farming.age(state) + 1), true);
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
