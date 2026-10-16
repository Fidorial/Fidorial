package fr.euphyllia.fidorial.server.world.block;

import fr.fidorial.annotation.SinceMinecraft;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.data.type.Icicle;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

@SinceMinecraft("26.4")
public final class IcicleBlock extends SpeleothemBlock {

    public static final Key KEY = BlockTypeKeys.ICICLE.key();

    public static final IcicleBlock INSTANCE = new IcicleBlock();

    private IcicleBlock() {
        super(KEY);
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockData state = super.placementState(context);
        if (state == null) {
            return null;
        }
        final BlockData support = context.relative(supportSide(state));
        return ((Icicle) state).withAttached(!is(support));
    }
}
