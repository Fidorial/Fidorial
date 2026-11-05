package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.registry.data.BlockStateIds;
import fr.euphyllia.fidorial.server.util.annotations.NeedsToBeRevisited;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.registry.keys.BiomeKeys;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.dimension.DimensionTypeDefinition;

/**
 * The {@code minecraft:debug} generator: every block state laid out on a grid at Y 70, over a
 * barrier floor at Y 60, in a plains biome.
 *
 * <p>States are taken in network ID order, which is vanilla's block registry order times each
 * block's possible states, so the grid matches vanilla block for block.</p>
 */
@NeedsToBeRevisited("Hooks for mob spawning are not exposed in the API and thus this can't fully replicate vanilla debug dimension; lacks a way to activate it other than NBT editing")
public final class DebugChunkGenerator implements ChunkGenerator {

    private static final int BARRIER_Y = 60;
    private static final int DISPLAY_Y = 70;

    private static final BlockState AIR = BlockState.withoutProperties(BlockTypeKeys.AIR);
    private static final BlockState BARRIER = BlockState.withoutProperties(BlockTypeKeys.BARRIER);

    private final DimensionTypeDefinition dimensionType;
    private final BlockState[] states;
    private final int gridWidth;
    private final int gridHeight;

    public DebugChunkGenerator(final DimensionTypeDefinition dimensionType, final BlockStateRegistry blockStates) {
        this.dimensionType = dimensionType;
        this.states = new BlockState[BlockStateIds.STATE_COUNT];
        for (int id = 0; id < states.length; id++) {
            states[id] = blockStates.byId(id);
        }
        this.gridWidth = (int) Math.ceil((float) Math.sqrt(states.length));
        this.gridHeight = (int) Math.ceil((float) states.length / gridWidth);
    }

    @Override
    public ChunkColumn generate(final int chunkX, final int chunkZ) {
        final int minY = dimensionType.minY();
        final int height = dimensionType.height();
        final ChunkColumn chunk = new ChunkColumn(chunkX, chunkZ, minY, height, AIR, BiomeKeys.PLAINS);
        final boolean barrier = BARRIER_Y >= minY && BARRIER_Y < minY + height;
        final boolean display = DISPLAY_Y >= minY && DISPLAY_Y < minY + height;
        final int baseX = chunkX << 4;
        final int baseZ = chunkZ << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (barrier) {
                    chunk.setBlock(x, BARRIER_Y, z, BARRIER);
                }
                if (display) {
                    chunk.setBlock(x, DISPLAY_Y, z, stateAt(baseX + x, baseZ + z));
                }
            }
        }
        return chunk;
    }

    private BlockState stateAt(final int worldX, final int worldZ) {
        if (worldX <= 0 || worldZ <= 0 || worldX % 2 == 0 || worldZ % 2 == 0) {
            return AIR;
        }
        final int gridX = worldX / 2;
        final int gridZ = worldZ / 2;
        if (gridX >= gridWidth || gridZ >= gridHeight) {
            return AIR;
        }
        final int index = Math.abs(gridX * gridWidth + gridZ);
        return index < states.length ? states[index] : AIR;
    }

    @Override
    public ChunkGeneratorConfig describeForSave() {
        return new ChunkGeneratorConfig.Debug();
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }

    @Override
    public boolean generatesStructures() {
        return false;
    }
}
