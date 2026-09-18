package fr.euphyllia.fidorial.server.world.block.crop;

import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.BlockEditService;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.entity.GameMode;
import fr.fidorial.item.ItemStack;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.crop.CropDrop;
import fr.fidorial.world.block.crop.CropRegistry;
import fr.fidorial.world.block.crop.CropType;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CropHarvest {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(CropHarvest.class);

    private final CropRegistry crops;
    private final BlockEditService blockEdits;

    public CropHarvest(final CropRegistry crops, final BlockEditService blockEdits) {
        this.crops = crops;
        this.blockEdits = blockEdits;
    }

    public void onBlockBroken(final ServerWorld world, final ServerPlayer breaker, final BlockPos pos) {
        try {
            final BlockState broken = blockAt(world, pos);
            final CropType crop = crops.byBlock(broken.name());
            if (crop != null) {
                collect(breaker, crop, age(broken, crop));
                return;
            }
            uproot(world, breaker, pos.offset(0, 1, 0));
        } catch (final Throwable t) {
            LOGGER.error("Crop harvest failed at {}", pos, t);
        }
    }

    private void uproot(final ServerWorld world, final ServerPlayer breaker, final BlockPos pos) {
        final BlockState state = blockAt(world, pos);
        final CropType crop = crops.byBlock(state.name());
        if (crop == null) {
            return;
        }
        blockEdits.set(world, pos, BlockState.of(BlockTypeKeys.AIR.key()));
        collect(breaker, crop, age(state, crop));
    }

    @NeedsToBeRevisited("We'll need to add the item drop.")
    private void collect(final ServerPlayer breaker, final CropType crop, final int age) {
        if (age < 0 || breaker.gameMode() == GameMode.CREATIVE) {
            return;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final List<CropDrop> drops = crop.dropsAt(age);

        for (final CropDrop drop : drops) {
            final ItemStack rolled = drop.roll(random);
            if (rolled.isEmpty()) {
                continue;
            }
            LOGGER.info("DROP ITEM : {} count : {}", rolled.id(), rolled.count());
        }
    }

    private static int age(final BlockState state, final CropType crop) {
        final String raw = state.properties().get(crop.ageProperty());
        if (raw == null) {
            return -1;
        }
        try {
            return Integer.parseInt(raw);
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    private static BlockState blockAt(final ServerWorld world, final BlockPos pos) {
        final @Nullable ChunkColumn column = world.loadedColumn(pos.chunkX(), pos.chunkZ());
        if (column == null) {
            return BlockState.of(BlockTypeKeys.AIR.key());
        }
        return column.getBlock(pos.x() & 15, pos.y(), pos.z() & 15);
    }
}
