package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.world.redstone.Faces;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.player.Player;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.data.type.Repeater;
import fr.fidorial.world.redstone.RedstoneWorld;
import fr.fidorial.world.redstone.SignalGetter;
import org.jspecify.annotations.Nullable;

/**
 * The repeater, a port of vanilla's {@code RepeaterBlock}: a diode with a delay of one to four
 * redstone ticks, locked by a powered repeater or comparator on its side.
 */
public final class RepeaterBlock extends DiodeBlock {

    public static final RepeaterBlock INSTANCE = new RepeaterBlock();

    private RepeaterBlock() {
        super(BlockTypeKeys.REPEATER);
    }

    @Override
    protected int delay(final BlockData state) {
        return ((Repeater) state).getDelay() * 2;
    }

    @Override
    protected boolean isLocked(final SignalGetter world, final BlockPosition position, final BlockData state) {
        return alternateSignal(world, position, state) > 0;
    }

    @Override
    protected boolean sideInputDiodesOnly() {
        return true;
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData state = super.placementState(context);
        if (!(state instanceof final Repeater repeater)) {
            return state;
        }
        return repeater.withLocked(isLocked(Redstone.world(context.placer().world()), context.pos(), repeater));
    }

    /**
     * Using the repeater cycles its delay.
     */
    @Override
    public boolean use(final Player player, final BlockPosition position, final BlockData data) {
        if (!(data instanceof final Repeater repeater) || player.gameMode() == GameMode.ADVENTURE
                || player.gameMode() == GameMode.SPECTATOR) {
            return false;
        }
        final int delay = repeater.getDelay() >= repeater.getMaximumDelay() ? repeater.getMinimumDelay() : repeater.getDelay() + 1;
        Redstone.world(player.world()).setBlock(position, repeater.withDelay(delay), RedstoneWorld.Update.NEIGHBORS);
        return true;
    }

    @Override
    public boolean connectsToWire(final SignalGetter world, final BlockPosition position, final BlockData state,
                                  final @Nullable BlockFace direction) {
        return direction != null && Faces.sameAxis(facing(state), direction);
    }

    @Override
    public BlockData updateShape(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                 final BlockFace direction, final BlockData neighbor) {
        if (direction == BlockFace.DOWN && !canSurviveOn(neighbor)) {
            return Redstone.air();
        }
        if (state instanceof final Repeater repeater && !Faces.sameAxis(direction, repeater.getFacing())) {
            return repeater.withLocked(isLocked(world, position, repeater));
        }
        return state;
    }
}
