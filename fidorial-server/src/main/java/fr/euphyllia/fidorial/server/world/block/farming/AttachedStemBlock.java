package fr.euphyllia.fidorial.server.world.block.farming;

import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.farming.FarmingBehaviour;
import fr.fidorial.world.farming.FarmingWorld;

import java.util.List;

/**
 * A stem holding its fruit: it lets go and becomes a fully grown {@link StemBlock} again as soon
 * as the fruit it faces is gone, ready to bear another one.
 */
public final class AttachedStemBlock implements FarmingBehaviour {

    public static final AttachedStemBlock MELON = new AttachedStemBlock(StemBlock.MELON);
    public static final AttachedStemBlock PUMPKIN = new AttachedStemBlock(StemBlock.PUMPKIN);

    private final StemBlock stem;

    private AttachedStemBlock(final StemBlock stem) {
        this.stem = stem;
    }

    public TypedKey<BlockType> key() {
        return stem.attached();
    }

    @Override
    public boolean canSurvive(final FarmingWorld world, final BlockPosition position, final BlockData state) {
        return world.isTagged(position.relative(BlockFace.DOWN), stem.soil());
    }

    @Override
    public BlockData updateShape(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (!(state instanceof final Directional holding) || holding.getFacing() != direction
                || Farming.is(neighbor, stem.fruit())) {
            return state;
        }
        final BlockData free = Farming.defaultState(stem.key());
        return Farming.withAge(free, Farming.maxAge(free));
    }

    @Override
    public List<ItemStack> drops(final FarmingWorld world, final BlockPosition position, final BlockData state,
                                 final ItemStack tool) {
        return Farming.loot(world, state, tool);
    }
}
