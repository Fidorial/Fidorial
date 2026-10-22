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
import fr.fidorial.world.block.data.type.Speleothem;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

@SinceMinecraft("26.4")
public class SpeleothemBlock implements BlockBehaviour {

    public static final SpeleothemBlock POINTED_DRIPSTONE = new SpeleothemBlock(BlockTypeKeys.POINTED_DRIPSTONE.key());
    public static final SpeleothemBlock SULFUR_SPIKE = new SpeleothemBlock(BlockTypeKeys.SULFUR_SPIKE.key());

    private final Key key;

    protected SpeleothemBlock(final Key key) {
        this.key = key;
    }

    public static BlockFace supportSide(final BlockData data) {
        return ((Speleothem) data).getVerticalDirection().opposite();
    }

    @Override
    public final Key key() {
        return key;
    }

    @Override
    public final BlockType type() {
        final BlockType type = Blocks.type(key);
        Preconditions.checkState(type != null, "The block type %s is not registered", key);
        return type;
    }

    public final boolean is(final @Nullable BlockData data) {
        return data != null && data.key().equals(key);
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockFace tip = tipDirection(context);
        if (tip == null) {
            return null;
        }
        final BlockData state = type().defaultData();
        Preconditions.checkState(state != null, "The block type %s has no default state", key);
        return ((Speleothem) state)
                .withVerticalDirection(tip)
                .withThickness(Speleothem.Thickness.TIP)
                .withWaterlogged(context.intoWater());
    }

    private @Nullable BlockFace tipDirection(final BlockPlaceContext context) {
        final BlockFace preferred = switch (context.clickedFace()) {
            case UP -> BlockFace.UP;
            case DOWN -> BlockFace.DOWN;
            default -> context.placer().pitch() > 0f ? BlockFace.UP : BlockFace.DOWN;
        };
        if (canPoint(context, preferred)) {
            return preferred;
        }
        return canPoint(context, preferred.opposite()) ? preferred.opposite() : null;
    }

    private boolean canPoint(final BlockPlaceContext context, final BlockFace tip) {
        final BlockData support = context.relative(tip.opposite());
        if (is(support)) {
            return ((Speleothem) support).getVerticalDirection() == tip;
        }
        return context.canAttachTo(tip.opposite());
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
