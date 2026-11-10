package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Directional;
import fr.fidorial.world.block.data.type.Piston;
import fr.fidorial.world.block.data.type.PistonHead;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import org.jspecify.annotations.Nullable;

/**
 * The head of an extended piston: it only stands in front of its base, and the two go together. Breaking either breaks the other.
 *
 * <p>The head also passes the block updates it receives on to its base, which is how a piston
 * hears of a change next to its head.</p>
 */
public final class PistonHeadBlock implements RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.PISTON_HEAD;
    public static final PistonHeadBlock INSTANCE = new PistonHeadBlock();

    private PistonHeadBlock() {
    }

    /**
     * {@return {@code true} if a state is the extended base a head belongs to}
     */
    private static boolean isFittingBase(final PistonHead head, final @Nullable BlockData base) {
        final TypedKey<BlockType> expected = head.isSticky() ? BlockTypeKeys.STICKY_PISTON : BlockTypeKeys.PISTON;
        return base instanceof final Piston piston && base.key().equals(expected)
                && piston.isExtended() && piston.getFacing() == head.getFacing();
    }

    /**
     * {@return {@code true} if the head has its base behind it, or the moving block the base turns
     * into while it retracts}
     */
    private static boolean canSurvive(final RedstoneWorld world, final BlockPosition position, final PistonHead head) {
        final BlockData base = world.blockAt(position.relative(head.getFacing().opposite()));
        if (base == null) {
            return true; // not loaded: nothing says the base is gone
        }
        return isFittingBase(head, base) || (base.key().equals(BlockTypeKeys.MOVING_PISTON)
                && base instanceof final Directional moving && moving.getFacing() == head.getFacing());
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (state instanceof final PistonHead head && direction == head.getFacing().opposite() && !canSurvive(world, position, head)) {
            return Redstone.air();
        }
        return state;
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (state instanceof final PistonHead head && canSurvive(world, position, head)) {
            world.neighborChanged(position.relative(head.getFacing().opposite()), source);
        }
    }

    @Override
    public void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (!(state instanceof final PistonHead head)) {
            return;
        }
        final BlockPosition basePosition = position.relative(head.getFacing().opposite());
        if (isFittingBase(head, world.blockAt(basePosition)) && Redstone.owns(world, basePosition)) {
            world.destroyBlock(basePosition);
        }
    }
}
