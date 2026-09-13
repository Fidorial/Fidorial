package fr.euphyllia.fidorial.server.world.block.crop;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.BlockEditService;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.WorldManager;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.BlockPos;
import fr.fidorial.world.block.crop.CropRegistry;
import fr.fidorial.world.block.crop.CropType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class CropGrowth implements RegionTickHandler {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(CropGrowth.class);
    private static final int SWEEP_INTERVAL = 4;
    private static final int SAMPLES_PER_SECTION = 12;

    private static final double TICKS_BETWEEN_SAMPLES =
            (double) SWEEP_INTERVAL * ChunkSection.BLOCK_COUNT / SAMPLES_PER_SECTION;

    private final WorldManager worldManager;
    private final CropRegistry crops;
    private final BlockEditService blockEdits;
    private final Map<Key, ServerWorld> worldsById = new ConcurrentHashMap<>();

    public CropGrowth(final WorldManager worldManager, final CropRegistry crops, final BlockEditService blockEdits) {
        this.worldManager = worldManager;
        this.crops = crops;
        this.blockEdits = blockEdits;
    }

    @Override
    public void tick(final Key worldId, final int sectionX, final int sectionZ, final long currentTick) {
        if (Math.floorMod(sectionX * 31 + sectionZ, SWEEP_INTERVAL) != Math.floorMod(currentTick, SWEEP_INTERVAL)) {
            return;
        }
        final ServerWorld world = worldById(worldId);
        if (world == null) {
            return;
        }

        final int span = 1 << ThreadedRegionRegionizer.SECTION_SHIFT;
        final int firstChunkX = sectionX << ThreadedRegionRegionizer.SECTION_SHIFT;
        final int firstChunkZ = sectionZ << ThreadedRegionRegionizer.SECTION_SHIFT;

        for (int chunkX = firstChunkX; chunkX < firstChunkX + span; chunkX++) {
            for (int chunkZ = firstChunkZ; chunkZ < firstChunkZ + span; chunkZ++) {
                final ChunkColumn column = world.loadedColumn(chunkX, chunkZ);
                if (column == null) {
                    continue;
                }
                try {
                    sweepColumn(world, column);
                } catch (final Throwable t) {
                    LOGGER.error("Crop sweep failed on chunk {},{}", chunkX, chunkZ, t);
                }
            }
        }
    }

    private void sweepColumn(final ServerWorld world, final ChunkColumn column) {
        for (final ChunkSection section : column.sections()) {
            if (section == null || section.isEmpty() || !holdsCrops(section)) {
                continue;
            }
            sweepSection(world, column, section);
        }
    }

    private boolean holdsCrops(final ChunkSection section) {
        return section.blocks().contains(state -> crops.byBlock(state.name()) != null);
    }

    private void sweepSection(final ServerWorld world, final ChunkColumn column, final ChunkSection section) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int baseY = section.sectionY() << 4;

        for (int sample = 0; sample < SAMPLES_PER_SECTION; sample++) {
            final int localX = random.nextInt(16);
            final int localY = random.nextInt(16);
            final int localZ = random.nextInt(16);

            final BlockState state = section.getBlock(localX, localY, localZ);
            final CropType crop = crops.byBlock(state.name());
            if (crop == null) {
                continue;
            }

            final BlockPos pos = new BlockPos(
                    (column.chunkX() << 4) + localX,
                    baseY + localY,
                    (column.chunkZ() << 4) + localZ);
            grow(world, pos, state, crop, random);
        }
    }

    private void grow(
            final ServerWorld world,
            final BlockPos pos,
            final BlockState state,
            final CropType crop,
            final ThreadLocalRandom random) {

        final int age = age(state, crop);
        if (age < 0 || age >= crop.maxAge()) {
            return;
        }

        final BlockState soil = column(world, pos.offset(0, -1, 0));
        if (!crop.acceptsSoil(soil.name()) || !CropPlanting.soilIsUsable(crop, soil)) {
            return;
        }

        if (crop.minLight() > 0 && world.lightLevelAt(pos.x(), pos.y(), pos.z()) < crop.minLight()) {
            return;
        }

        if (random.nextDouble() >= growthChance(crop)) {
            return;
        }

        blockEdits.set(world, pos, CropPlanting.stateAtAge(crop, age + 1));
    }

    private static double growthChance(final CropType crop) {
        return Math.min(1.0, TICKS_BETWEEN_SAMPLES / crop.averageTicksPerStage());
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

    private static BlockState column(final ServerWorld world, final BlockPos pos) {
        final ChunkColumn column = world.loadedColumn(pos.chunkX(), pos.chunkZ());
        if (column == null) {
            return BlockState.of(BlockTypeKeys.AIR.key());
        }
        return column.getBlock(pos.x() & 15, pos.y(), pos.z() & 15);
    }

    private @Nullable ServerWorld worldById(final Key id) {
        final ServerWorld cached = worldsById.get(id);
        if (cached != null) {
            return cached;
        }
        for (final ServerWorld world : worldManager.worlds()) {
            if (world.dimension().id().equals(id)) {
                worldsById.put(id, world);
                return world;
            }
        }
        return null;
    }

}
