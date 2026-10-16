package fr.euphyllia.fidorial.server.world.block;

import com.google.common.base.Preconditions;
import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.fidorial.annotation.SinceMinecraft;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.Waterlogged;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

@SinceMinecraft("26.4")
public final class ClusterBlock implements BlockBehaviour {

    public static final ClusterBlock AMETHYST_CLUSTER = new ClusterBlock(BlockTypeKeys.AMETHYST_CLUSTER.key());
    public static final ClusterBlock LARGE_AMETHYST_BUD = new ClusterBlock(BlockTypeKeys.LARGE_AMETHYST_BUD.key());
    public static final ClusterBlock MEDIUM_AMETHYST_BUD = new ClusterBlock(BlockTypeKeys.MEDIUM_AMETHYST_BUD.key());
    public static final ClusterBlock SMALL_AMETHYST_BUD = new ClusterBlock(BlockTypeKeys.SMALL_AMETHYST_BUD.key());
    public static final ClusterBlock ICE_CRYSTAL = new ClusterBlock(BlockTypeKeys.ICE_CRYSTAL.key());

    private final Key key;

    private ClusterBlock(final Key key) {
        this.key = key;
    }

    public static BlockFace supportSide(final BlockData data) {
        return ((Directional) data).getFacing().opposite();
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public BlockType type() {
        final BlockType type = Blocks.type(key);
        Preconditions.checkState(type != null, "The block type %s is not registered", key);
        return type;
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockFace facing = context.clickedFace();
        if (!context.canAttachTo(facing.opposite())) {
            return null;
        }
        BlockData state = type().defaultData();
        Preconditions.checkState(state != null, "The block type %s has no default state", key);
        state = ((Directional) state).withFacing(facing);
        state = ((Waterlogged) state).withWaterlogged(context.intoWater());
        return state;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(key);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(key);
    }
}
