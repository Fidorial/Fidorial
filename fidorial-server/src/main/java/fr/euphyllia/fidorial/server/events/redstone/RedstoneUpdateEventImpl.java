package fr.euphyllia.fidorial.server.events.redstone;

import com.google.common.base.Preconditions;
import fr.fidorial.event.redstone.RedstoneUpdateEvent;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.World;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;

public final class RedstoneUpdateEventImpl extends AbstractRedstoneEvent implements RedstoneUpdateEvent {

    private final TypedKey<BlockType> source;

    public RedstoneUpdateEventImpl(final World world, final BlockPosition position, final BlockData block,
                                   final TypedKey<BlockType> source) {
        super(world, position, block);
        Preconditions.checkArgument(source != null, "The source of a redstone update event must not be null");
        this.source = source;
    }

    @Override
    public TypedKey<BlockType> source() {
        return source;
    }
}
