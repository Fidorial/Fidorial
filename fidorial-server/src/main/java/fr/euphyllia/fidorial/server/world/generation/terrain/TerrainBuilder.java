package fr.euphyllia.fidorial.server.world.generation.terrain;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.fluid.FluidBlockCodec;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.carver.CarvingMask;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeClimate;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeTable;
import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.euphyllia.fidorial.server.world.generation.density.DensityContext;
import fr.euphyllia.fidorial.server.world.generation.material.MaterialContext;
import fr.euphyllia.fidorial.server.world.generation.material.MaterialRules;
import fr.euphyllia.fidorial.server.world.generation.noise.NoiseField;
import fr.euphyllia.fidorial.server.world.generation.random.BitRandom;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.world.block.BlockType;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Fills a chunk from its densities: solid where the final density is positive, the aquifers decide the rest; then
 * the material rules turn each solid block into stone, soil, sand or ore, and the carvers hollow out their
 * tunnels. The water and lava left against a body of another level, or against air, are marked to start flowing
 * once the chunk is done, as are the fluids the material rules give.
 *
 * <p>One builder per thread: it reuses its buffers from chunk to chunk.</p>
 */
public final class TerrainBuilder {

    private static final int WAY_BELOW = -32512;

    /**
     * What the builder needs from the generator, shared by every thread.
     *
     * @param finalDensity   the final density of the noise router
     * @param aquifers       the aquifer functions, {@code null} when the settings have none
     * @param globalFluid    the fluid of a world without aquifers
     * @param aquiferRandom  the positional factory of the aquifers
     * @param blocks         the block states
     * @param biomes         the biomes of the generator
     * @param climate        the temperatures of the biomes
     * @param seaLevel       the sea level
     * @param uncarvable     the blocks the carvers leave alone
     * @param badlands       the noises of the eroded badlands pillars
     * @param icebergs       the noises of the frozen ocean icebergs
     * @param worldRandom    the positional factory of the world
     */
    public record Shared(Density finalDensity, AquiferSampler.@Nullable Inputs aquifers, AquiferSampler.FluidPicker globalFluid,
                         PositionalRandom aquiferRandom, BlockLookup blocks, BiomeTable biomes, BiomeClimate climate, int seaLevel,
                         TagKey<BlockType> uncarvable, ExtensionNoises badlands, ExtensionNoises icebergs, PositionalRandom worldRandom,
                         int erodedBadlands, int frozenOcean, int deepFrozenOcean) {
    }

    /**
     * The three noises of a surface extension: where it rises, how high, and its cap.
     */
    public record ExtensionNoises(NoiseField surface, NoiseField pillar, NoiseField roof) {
    }

    private final Shared shared;
    private final BlockState dirt;
    private final MaterialContext materials;
    private final MaterialRules.Rule rule;
    private float[] densities = new float[0];
    private final int[] columnTops = new int[256];
    private @Nullable BlockState[] column = new BlockState[0];
    private boolean[] flowing = new boolean[0];
    // the last block of the material rules looked at, and whether it holds a fluid
    private @Nullable BlockState lastRuleBlock;
    private boolean lastRuleFluid;
    // the last state placed and its number in the palette of the generator: a column is mostly long runs of the
    // same state, so most blocks skip the palette lookup
    private @Nullable BlockState lastPlaced;
    private char lastPlacedId;

    /**
     * @param materials the material context the rule reads
     * @param rule      the material rule of the noise settings, compiled for {@code materials}
     */
    public TerrainBuilder(final Shared shared, final MaterialContext materials, final MaterialRules.Rule rule) {
        this.shared = shared;
        this.dirt = shared.blocks().state(BlockTypeKeys.DIRT);
        this.materials = materials;
        this.rule = rule;
    }

    private static int densityIndex(final int x, final int z, final int y, final int height) {
        return ((x << 4) | z) * height + y;
    }

    /**
     * Fills a chunk.
     *
     * @param chunk   the chunk, all air, its biomes known
     * @param context the density context of this thread, reset for the chunk
     * @param mask    the blocks the carvers reach, {@code null} for none
     */
    public void fill(final GenChunk chunk, final DensityContext context, final @Nullable CarvingMask mask) {
        final int minY = chunk.minY();
        final int height = chunk.height();
        final int maxY = minY + height - 1;
        final int baseX = chunk.chunkX() << 4;
        final int baseZ = chunk.chunkZ() << 4;
        final BlockState air = shared.blocks().air;
        if (densities.length < 256 * height) {
            densities = new float[256 * height];
            column = new BlockState[height];
            flowing = new boolean[height];
        }

        // the coarse grid points first, column by column: they are shared by the blocks around them
        final Density finalDensity = shared.finalDensity();
        for (int x = 0; x <= 16; x += 4) {
            for (int z = 0; z <= 16; z += 4) {
                for (int y = maxY - Math.floorMod(maxY, 8); y >= minY; y -= 8) {
                    finalDensity.compute(context, baseX + x, y, baseZ + z);
                }
            }
        }
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < height; y++) {
                    densities[densityIndex(x, z, y, height)] = finalDensity.compute(context, baseX + x, minY + y, baseZ + z);
                }
            }
        }

        final AquiferSampler aquifer = shared.aquifers() == null ? null : new AquiferSampler(shared.aquifers(), context,
                shared.aquiferRandom(), shared.globalFluid(), air, shared.blocks().water, shared.blocks().lava,
                baseX, minY, baseZ, baseX + 15, maxY, baseZ + 15);

        // the highest block of each column that is not air
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = -1;
                for (int y = height - 1; y >= 0; y--) {
                    final BlockState state = substance(aquifer, baseX + x, minY + y, baseZ + z, densities[densityIndex(x, z, y, height)]);
                    if (state != air) {
                        top = y;
                        break;
                    }
                }
                columnTops[x + (z << 4)] = top;
            }
        }

        final NoiseBiomeGrid biomes = chunk.biomes();
        materials.begin(context, biomes);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int blockX = baseX + x;
                final int blockZ = baseZ + z;
                final int top = prepareColumn(aquifer, x, z, blockX, blockZ, minY, height);
                final int gradientX = columnTops[Math.min(x + 1, 15) + (z << 4)] - columnTops[Math.max(x - 1, 0) + (z << 4)];
                final int gradientZ = columnTops[x + (Math.min(z + 1, 15) << 4)] - columnTops[x + (Math.max(z - 1, 0) << 4)];
                final int initialHeight = minY + top + 1;
                final BiomeInfo surfaceBiome = shared.biomes().get(biomes.biome(blockX, initialHeight, blockZ));
                int columnTop = top;
                if (surfaceBiome.index() == shared.erodedBadlands()) {
                    columnTop = extendErodedBadlands(blockX, blockZ, minY, height, top);
                }
                fillColumn(chunk, aquifer, x, z, blockX, blockZ, minY, columnTop, gradientX, gradientZ, mask);
                if (surfaceBiome.index() == shared.frozenOcean() || surfaceBiome.index() == shared.deepFrozenOcean()) {
                    extendFrozenOcean(chunk, surfaceBiome, x, z, blockX, blockZ, initialHeight, mask);
                }
            }
        }
    }

    private @Nullable BlockState substance(final @Nullable AquiferSampler aquifer, final int x, final int y, final int z, final double density) {
        if (aquifer != null) {
            return aquifer.substance(x, y, z, density);
        }
        if (density > 0.0) {
            return null;
        }
        return shared.globalFluid().at(x, y, z).at(y, shared.blocks().air);
    }

    /**
     * Fills the column buffer from the densities, up to the top of the column.
     *
     * @return the index of the highest block that is not air, -1 for none
     */
    private int prepareColumn(final @Nullable AquiferSampler aquifer, final int x, final int z, final int blockX, final int blockZ,
                              final int minY, final int height) {
        final BlockState air = shared.blocks().air;
        Arrays.fill(column, air);
        Arrays.fill(flowing, false);
        final int top = columnTops[x + (z << 4)];
        for (int y = 0; y <= top; y++) {
            final BlockState state = substance(aquifer, blockX, minY + y, blockZ, densities[densityIndex(x, z, y, height)]);
            if (state != air) {
                column[y] = state;
                if (state != null && aquifer != null && aquifer.scheduleFlow()) {
                    flowing[y] = true;
                }
            }
        }
        return top;
    }

    /**
     * Raises the pillars of the eroded badlands above the terrain: where two noises allow it, the empty space of
     * the column becomes solid up to a height drawn from them, unless water lies in the way.
     *
     * @return the index of the highest block that is not air
     */
    private int extendErodedBadlands(final int blockX, final int blockZ, final int minY, final int height, final int top) {
        final ExtensionNoises noises = shared.badlands();
        final double pillar = Math.min(Math.abs(noises.surface().sample(blockX, 0.0, blockZ) * 8.25),
                (double) (noises.pillar().sample(blockX * 0.2, 0.0, blockZ * 0.2) * 15.0F));
        if (pillar <= 0.0) {
            return top;
        }
        final double roof = Math.abs(noises.roof().sample(blockX * 0.75, 0.0, blockZ * 0.75) * 1.5);
        final double extensionTop = 64.0 + Math.min(pillar * pillar * 2.5, Math.ceil(roof * 50.0) + 24.0);
        final int start = Math.min((int) Math.floor(extensionTop), minY + height - 1);
        if (minY + top + 1 > start) {
            return top;
        }
        final BlockState air = shared.blocks().air;
        for (int y = start; y >= minY; y--) {
            final BlockState state = column[y - minY];
            if (state == null) {
                break;
            }
            if (shared.blocks().isWaterBlock(state)) {
                return top;
            }
        }
        int newTop = top;
        for (int y = start; y >= minY && column[y - minY] == air; y--) {
            column[y - minY] = null;
            newTop = Math.max(newTop, y - minY);
        }
        return newTop;
    }

    private int ceilingBelow(final int index) {
        for (int probe = index - 1; probe >= 0; probe--) {
            if (column[probe] != null) {
                return probe + 1;
            }
        }
        return WAY_BELOW;
    }

    private void fillColumn(final GenChunk chunk, final @Nullable AquiferSampler aquifer, final int x, final int z,
                            final int blockX, final int blockZ, final int minY, final int top,
                            final int gradientX, final int gradientZ, final @Nullable CarvingMask mask) {
        final BlockState air = shared.blocks().air;
        final BlockLookup blocks = shared.blocks();
        materials.column(blockX, blockZ, gradientX, gradientZ);
        int solidAbove = 0;
        int fluidHeight = Integer.MIN_VALUE;
        int nextCeiling = Integer.MAX_VALUE;
        boolean carvedTop = false;
        for (int index = top; index >= 0; index--) {
            final int y = minY + index;
            final boolean carved = mask != null && mask.isCarved(x, y, z);
            final BlockState noise = column[index];
            if (noise == air) {
                solidAbove = 0;
                fluidHeight = Integer.MIN_VALUE;
                carvedTop &= carved;
                continue;
            }
            if (noise != null) {
                if (fluidHeight == Integer.MIN_VALUE) {
                    fluidHeight = y + 1;
                }
                place(chunk, x, y, z, noise);
                if (flowing[index]) {
                    chunk.markFluidUpdate(x, y, z);
                }
                carvedTop &= carved;
                continue;
            }
            if (index <= nextCeiling) {
                nextCeiling = ceilingBelow(index);
            }
            solidAbove++;
            final int solidBelow = index - nextCeiling + 1;
            materials.block(solidAbove, solidBelow, fluidHeight, y);
            BlockState block = rule.apply();
            if (carved && (block == null || !blocks.is(block, shared.uncarvable()))) {
                final BlockState hollow = aquifer != null ? aquifer.substance(blockX, y, blockZ, 0.0)
                        : shared.globalFluid().at(blockX, y, blockZ).at(y, air);
                if (hollow == air) {
                    carvedTop |= solidAbove == 1;
                    continue;
                }
                if (hollow != null) {
                    carvedTop |= solidAbove == 1;
                    place(chunk, x, y, z, hollow);
                    if (aquifer != null && aquifer.scheduleFlow()) {
                        chunk.markFluidUpdate(x, y, z);
                    }
                    continue;
                }
            }
            if (carvedTop) {
                if (block != null && blocks.is(block, dirt)) {
                    materials.block(1, solidBelow, fluidHeight, y);
                    block = rule.apply();
                }
                carvedTop = false;
            }
            if (block != null) {
                place(chunk, x, y, z, block);
                if (holdsFluid(block)) {
                    chunk.markFluidUpdate(x, y, z);
                }
            }
        }
    }

    /**
     * Places a block, reusing the palette number of the previous one when it is the same state. The palette of a
     * generator never renumbers a state, and a builder serves one generator, so the number stays valid from chunk
     * to chunk.
     */
    private void place(final GenChunk chunk, final int x, final int y, final int z, final BlockState state) {
        if (state != lastPlaced) {
            lastPlacedId = chunk.palette().id(state);
            lastPlaced = state;
        }
        chunk.setId(x, y, z, lastPlacedId);
    }

    private boolean holdsFluid(final BlockState block) {
        if (block != lastRuleBlock) {
            lastRuleBlock = block;
            lastRuleFluid = !FluidBlockCodec.fromBlock(block).isEmpty();
        }
        return lastRuleFluid;
    }

    /**
     * Grows the icebergs of the frozen oceans: packed ice with a cap of snow, rising above the sea where two noises
     * allow it.
     */
    private void extendFrozenOcean(final GenChunk chunk, final BiomeInfo biome, final int x, final int z,
                                   final int blockX, final int blockZ, final int initialHeight, final @Nullable CarvingMask mask) {
        final ExtensionNoises noises = shared.icebergs();
        final int seaLevel = shared.seaLevel();
        final double iceberg = Math.min(Math.abs(noises.surface().sample(blockX, 0.0, blockZ) * 8.25),
                (double) (noises.pillar().sample(blockX * 1.28, 0.0, blockZ * 1.28) * 15.0F));
        if (iceberg <= 1.8) {
            return;
        }
        final double roof = Math.abs(noises.roof().sample(blockX * 1.17, 0.0, blockZ * 1.17) * 1.5);
        double top = Math.min(iceberg * iceberg * 1.2, Math.ceil(roof * 40.0) + 14.0);
        if (shared.climate().meltsIcebergs(biome, blockX, blockZ, seaLevel)) {
            top -= 2.0;
        }
        if (top <= 2.0) {
            return;
        }
        final double bottom = seaLevel - top - 7.0;
        top += seaLevel;
        final BlockLookup blocks = shared.blocks();
        final BlockState snow = blocks.state(BlockTypeKeys.SNOW_BLOCK);
        final BlockState packedIce = blocks.state(BlockTypeKeys.PACKED_ICE);
        final BitRandom random = shared.worldRandom().at(blockX, 0, blockZ);
        final int maxSnowDepth = 2 + random.nextInt(4);
        final int minSnowHeight = seaLevel + 18 + random.nextInt(10);
        int snowDepth = 0;
        final int minSurface = materials.minSurfaceLevel();
        for (int y = Math.max(initialHeight, (int) top + 1); y >= minSurface; y--) {
            final BlockState state = chunk.get(x, y, z);
            if (state.isAir() && y < (int) top && random.nextDouble() > 0.01
                    || blocks.isWaterBlock(state) && y > (int) bottom && y < seaLevel && random.nextDouble() > 0.15) {
                final BlockState placed;
                if (snowDepth <= maxSnowDepth && y > minSnowHeight) {
                    placed = snow;
                    snowDepth++;
                } else {
                    placed = packedIce;
                }
                // carved blocks stay hollow, but they count as placed
                if (chunk.contains(y) && (mask == null || !mask.isCarved(x, y, z))) {
                    place(chunk, x, y, z, placed);
                }
            }
        }
    }
}
