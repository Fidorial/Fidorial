package fr.euphyllia.fidorial.server.world.block.redstone;

import fr.euphyllia.fidorial.server.registry.data.BlockStateLightProperties;
import fr.euphyllia.fidorial.server.world.block.SimpleBlock;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.block.BlockBehaviour;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.data.Openable;
import fr.fidorial.world.block.data.Powerable;
import fr.fidorial.world.redstone.RedstoneBehaviour;
import fr.fidorial.world.redstone.SignalGetter;
import org.jspecify.annotations.Nullable;

/**
 * What doors, trapdoors and fence gates share: a signal opens them and losing it closes them, and,
 * one placed next to a signal is placed powered and open.
 *
 * <p>Apart from that, they are placed the generic way, {@link SimpleBlock#placementState(BlockType,
 * BlockPlaceContext)}.</p>
 */
abstract class PoweredOpenable implements BlockBehaviour, RedstoneBehaviour {

    protected final TypedKey<BlockType> key;

    PoweredOpenable(final TypedKey<BlockType> key) {
        this.key = key;
    }

    @Override
    public final TypedKey<BlockType> key() {
        return key;
    }

    @Override
    public final BlockType type() {
        return Redstone.type(key);
    }

    @Override
    public final int lightEmission(final BlockData data) {
        return BlockStateLightProperties.emission(key);
    }

    @Override
    public final int lightOpacity(final BlockData data) {
        return BlockStateLightProperties.opacity(key);
    }

    @Override
    public final @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData state = SimpleBlock.placementState(type(), context);
        if (!(state instanceof final Openable openable) || !(state instanceof Powerable)) {
            return state;
        }
        final boolean signal = placementSignal(Redstone.world(context.placer().world()), context.pos());
        return ((Powerable) openable.withOpen(signal)).withPowered(signal);
    }

    /**
     * {@return {@code true} if the block placed at a position starts powered}
     *
     * @param world    the world it is placed in
     * @param position where it is placed
     */
    protected boolean placementSignal(final SignalGetter world, final BlockPosition position) {
        return world.hasNeighborSignal(position);
    }
}
