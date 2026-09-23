package fr.euphyllia.fidorial.server.world.block;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.WorldManager;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.fidorial.scheduler.RegionTickHandler;
import fr.fidorial.world.BlockPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomTicks implements RegionTickHandler {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(RandomTicks.class);

    private static final int RANDOM_TICK_SPEED = 3; // Todo Make it modifiable by gamerules in the future

    private static final int SWEEP_INTERVAL = 4;

    private static final int SAMPLES_PER_SWEEP = RANDOM_TICK_SPEED * SWEEP_INTERVAL;

    private final WorldManager worldManager;
    private final BlockUpdateService blocks;
    private final Map<Key, ServerWorld> worldsById = new ConcurrentHashMap<>();

    public RandomTicks(final WorldManager worldManager, final BlockUpdateService blocks) {
        this.worldManager = worldManager;
        this.blocks = blocks;
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
                    LOGGER.error("Random tick sweep failed on chunk {},{}", chunkX, chunkZ, t);
                }
            }
        }
    }

    private void sweepColumn(final ServerWorld world, final ChunkColumn column) {
        for (final ChunkSection section : column.sections()) {
            if (section == null || section.isEmpty() || !section.blocks().contains(blocks::isRandomlyTicking)) {
                continue;
            }
            sweepSection(world, column, section);
        }
    }

    private void sweepSection(final ServerWorld world, final ChunkColumn column, final ChunkSection section) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int baseY = section.sectionY() << 4;

        for (int sample = 0; sample < SAMPLES_PER_SWEEP; sample++) {
            final int localX = random.nextInt(16);
            final int localY = random.nextInt(16);
            final int localZ = random.nextInt(16);

            final BlockState state = section.getBlock(localX, localY, localZ);
            if (!blocks.isRandomlyTicking(state)) {
                continue;
            }
            final BlockPos pos = new BlockPos(
                    (column.chunkX() << 4) + localX,
                    baseY + localY,
                    (column.chunkZ() << 4) + localZ);
            blocks.randomTick(world, pos, state);
        }
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
