package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.redstone.RedstoneEvents;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Lightable;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.RedstoneWorld;

/**
 * The redstone lamp, a port of vanilla's {@code RedstoneLampBlock}: it lights up as soon as it is
 * powered, and goes out four game ticks after it no longer is.
 */
public final class RedstoneLampBlock implements BlockBehaviour, RedstoneBehaviour {

    public static final TypedKey<BlockType> KEY = BlockTypeKeys.REDSTONE_LAMP;

    public static final RedstoneLampBlock INSTANCE = new RedstoneLampBlock();

    /**
     * The light a lit lamp gives.
     */
    public static final int LIGHT_EMISSION = 15;

    private static final int TURN_OFF_DELAY = 4;

    private RedstoneLampBlock() {
    }

    @Override
    public BlockType type() {
        return Redstone.type(KEY);
    }

    @Override
    public BlockData placementState(final BlockPlaceContext context) {
        final Lightable lamp = (Lightable) Redstone.defaultState(KEY);
        return lamp.withLit(Redstone.world(context.placer().world()).hasNeighborSignal(context.pos()));
    }

    @Override
    public int lightEmission(final BlockData data) {
        return data instanceof final Lightable lamp && lamp.isLit() ? LIGHT_EMISSION : 0;
    }

    @Override
    public int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(KEY);
    }

    @Override
    public void neighborChanged(final RedstoneWorld world, final BlockPosition position, final BlockData state,
                                final TypedKey<BlockType> source) {
        if (!(state instanceof final Lightable lamp) || lamp.isLit() == world.hasNeighborSignal(position)) {
            return;
        }
        if (lamp.isLit()) {
            world.scheduleTick(position, KEY, TURN_OFF_DELAY);
        } else {
            world.setBlock(position, lamp.withLit(true), RedstoneWorld.Update.SHAPES);
        }
    }

    @Override
    public void tick(final RedstoneWorld world, final BlockPosition position, final BlockData state) {
        if (state instanceof final Lightable lamp && lamp.isLit() && !world.hasNeighborSignal(position)
                && RedstoneEvents.input(world.world(), position, state, false)) {
            world.setBlock(position, lamp.withLit(false), RedstoneWorld.Update.SHAPES);
        }
    }
}
