package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.FaceAttachable.AttachedFace;
import fr.fidorial.world.block.data.type.Switch;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import fr.fidorial.world.redstone.SupportType;
import org.jspecify.annotations.Nullable;

/**
 * What levers and buttons share, ports of vanilla's {@code FaceAttachedHorizontalDirectionalBlock}
 * and of the signal part of {@code LeverBlock} and {@code ButtonBlock}: hung on a whole face, they
 * power every neighbour while on, and strongly power the block they hang on.
 */
abstract class SwitchBlock implements BlockBehaviour, RedstoneBehaviour {

    protected final TypedKey<BlockType> key;

    SwitchBlock(final TypedKey<BlockType> key) {
        this.key = key;
    }

    protected static boolean isOn(final BlockData state) {
        return state instanceof final Switch control && control.isPowered();
    }

    @Override
    public final TypedKey<BlockType> key() {
        return key;
    }

    @Override
    public final BlockType type() {
        return Redstone.type(key);
    }

    /**
     * On the first surface, in the order the player looks at them, that holds it.
     */
    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final Switch defaults = (Switch) Redstone.defaultState(key);
        for (final BlockFace direction : Placement.nearestLookingDirections(context)) {
            final Switch state = switch (direction) {
                case UP -> defaults.withAttachedFace(AttachedFace.CEILING).withFacing(context.horizontalFacing());
                case DOWN -> defaults.withAttachedFace(AttachedFace.FLOOR).withFacing(context.horizontalFacing());
                default -> defaults.withAttachedFace(AttachedFace.WALL).withFacing(direction.opposite());
            };
            if (Placement.canAttach(context, state.getSupportSide())) {
                return state;
            }
        }
        return null;
    }

    @Override
    public int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(key);
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(key);
    }

    @Override
    public boolean isSignalSource(final BlockData state) {
        return true;
    }

    @Override
    public int signal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        return isOn(state) ? 15 : 0;
    }

    @Override
    public int directSignal(final SignalGetter world, final BlockPosition position, final BlockData state, final BlockFace direction) {
        // strongly powers the block it hangs on, which reads it from the opposite of its support side
        return isOn(state) && state instanceof final Switch control && control.getSupportSide().opposite() == direction ? 15 : 0;
    }

    @Override
    public void onRemoved(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (isOn(state)) {
            updateNeighbours(world, position, state);
        }
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (!(state instanceof final Switch control) || direction != control.getSupportSide()) {
            return state;
        }
        return BlockSupport.isFaceSturdy(neighbor, direction.opposite(), SupportType.FULL) ? state : Redstone.air();
    }

    /**
     * Tells the neighbours of the switch, and those of the block it hangs on, that it changed.
     */
    protected final void updateNeighbours(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        world.updateNeighbors(position, key);
        if (state instanceof final Switch control) {
            world.updateNeighbors(position.relative(control.getSupportSide()), key);
        }
    }
}
