package fr.euphyllia.fidorial.server.world.block;

import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;

import java.util.Objects;

public final class FluidBlock implements BlockBehaviour {

    public static final FluidBlock WATER = new FluidBlock(BlockTypeKeys.WATER, 1, 0);

    public static final FluidBlock LAVA = new FluidBlock(BlockTypeKeys.LAVA, 15, 15);

    private final TypedKey<BlockType> key;
    private final int opacity;
    private final int emission;

    private FluidBlock(final TypedKey<BlockType> key, final int opacity, final int emission) {
        this.key = key;
        this.opacity = opacity;
        this.emission = emission;
    }

    @Override
    public BlockType type() {
        return Objects.requireNonNull(Blocks.type(key));
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return opacity;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return emission;
    }
}
