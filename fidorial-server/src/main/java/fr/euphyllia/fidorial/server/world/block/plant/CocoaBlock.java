package fr.euphyllia.fidorial.server.world.block.plant;

import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.world.BlockFace;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.BlockAccess;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockDrop;
import fr.fidorial.world.block.BlockPlaceContext;
import fr.fidorial.world.block.plant.CropBlock;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CocoaBlock extends CropBlock {

    private static final Set<Key> JUNGLE_LOGS = Set.of(
            BlockTypeKeys.JUNGLE_LOG.key(),
            BlockTypeKeys.JUNGLE_WOOD.key(),
            BlockTypeKeys.STRIPPED_JUNGLE_LOG.key(),
            BlockTypeKeys.STRIPPED_JUNGLE_WOOD.key());

    public CocoaBlock() {
        super(CropBlock.builder(BlockTypeKeys.COCOA.key())
                .minLight(0)
                .growth(Growth.fixed(1.0 / 5))
                .placeSound(null)
                .ripeDrops(List.of(BlockDrop.of(ItemStack.of(ItemKeys.COCOA_BEANS.key()), 2, 3)))
                .immatureDrops(List.of(BlockDrop.of(ItemStack.of(ItemKeys.COCOA_BEANS.key())))));
    }

    @Override
    public @Nullable BlockData placementState(final BlockPlaceContext context) {
        final BlockFace clicked = context.clickedFace();
        if (clicked == BlockFace.UP || clicked == BlockFace.DOWN) {
            return null;
        }
        final BlockData data = type().defaultData();
        return data == null ? null : data.with("facing", clicked.opposite().name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean canSurvive(final BlockData data, final BlockAccess world, final BlockPos pos) {
        final BlockFace facing;
        try {
            facing = BlockFace.valueOf(data.get("facing").toUpperCase(Locale.ROOT));
        } catch (final RuntimeException e) {
            return false;
        }
        return JUNGLE_LOGS.contains(world.relative(pos, facing).key());
    }

    @Override
    public boolean breaksInstantly(final BlockData data) {
        return false;
    }
}
