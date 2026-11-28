package fr.euphyllia.fidorial.server.world.generation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.ChunkGenerator;
import fr.euphyllia.fidorial.server.world.ChunkGeneratorConfig;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.chunk.BitPacking;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.euphyllia.fidorial.server.world.chunk.PalettedContainer;
import fr.euphyllia.fidorial.server.world.fluid.FluidBlockCodec;
import fr.euphyllia.fidorial.server.world.fluid.FluidStateImpl;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.euphyllia.fidorial.server.world.generation.biome.JitteredBiomeGrid;
import fr.euphyllia.fidorial.server.world.generation.block.BlockLookup;
import fr.euphyllia.fidorial.server.world.generation.block.StatePalette;
import fr.euphyllia.fidorial.server.world.generation.carver.Carver;
import fr.euphyllia.fidorial.server.world.generation.carver.CarvingMask;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeClimate;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeInfo;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeSource;
import fr.euphyllia.fidorial.server.world.generation.climate.BiomeTable;
import fr.euphyllia.fidorial.server.world.generation.climate.ClimateSampler;
import fr.euphyllia.fidorial.server.world.generation.climate.ClimateTable;
import fr.euphyllia.fidorial.server.world.generation.climate.EndBiomes;
import fr.euphyllia.fidorial.server.world.generation.climate.MultiNoiseBiomes;
import fr.euphyllia.fidorial.server.world.generation.data.HeightContext;
import fr.euphyllia.fidorial.server.world.generation.data.WorldgenData;
import fr.euphyllia.fidorial.server.world.generation.density.Density;
import fr.euphyllia.fidorial.server.world.generation.density.DensityContext;
import fr.euphyllia.fidorial.server.world.generation.density.DensityProgram;
import fr.euphyllia.fidorial.server.world.generation.feature.Decorator;
import fr.euphyllia.fidorial.server.world.generation.material.MaterialContext;
import fr.euphyllia.fidorial.server.world.generation.material.MaterialRules;
import fr.euphyllia.fidorial.server.world.generation.random.LegacyRandom;
import fr.euphyllia.fidorial.server.world.generation.random.PositionalRandom;
import fr.euphyllia.fidorial.server.world.generation.terrain.AquiferSampler;
import fr.euphyllia.fidorial.server.world.generation.terrain.FluidLevel;
import fr.euphyllia.fidorial.server.world.generation.terrain.GenChunk;
import fr.euphyllia.fidorial.server.world.generation.terrain.NoiseBiomeGrid;
import fr.euphyllia.fidorial.server.world.generation.terrain.TerrainBuilder;
import fr.euphyllia.fidorial.server.world.structure.FutureCache;
import fr.euphyllia.fidorial.server.world.structure.template.StructureTemplateImpl;
import fr.euphyllia.fidorial.server.world.tick.ChunkTicks;
import fr.euphyllia.fidorial.server.world.tick.PendingTick;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Position;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BiomeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.dimension.DimensionTypeDefinition;
import fr.fidorial.world.redstone.TickPriority;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Generates a dimension from noise settings and a biome source: the terrain shaped by the density
 * functions, the aquifers, the material rules and the carvers, then the features of the biomes.
 *
 * <p>Everything follows from the seed and the data: the terrain of a chunk does not depend on the chunks generated
 * before it. Decorations spread over the neighbouring chunks, so a feature crossing a chunk border may meet what an
 * earlier neighbour already placed. The terrain of a chunk is kept for a while, since the decoration of its
 * neighbours reads it. Safe to use from several threads.</p>
 */
public final class NoiseChunkGenerator implements ChunkGenerator {

    private static final int TERRAIN_CACHE = 1024;
    private static final int CARVER_BIOME_CACHE = 1 << 16;
    private static final int BIOME_CELL_CACHE = 4096;
    private static final int PROTECTED_TOP = 7;

    private static final Key AQUIFER_RANDOM = Key.key( "aquifer");
    private static final Key SURFACE_NOISE = Key.key( "surface");
    private static final Key SURFACE_SECONDARY_NOISE = Key.key( "surface_secondary");
    private static final Key CLAY_BANDS_OFFSET_NOISE = Key.key( "clay_bands_offset");
    private static final Key BADLANDS_SURFACE_NOISE = Key.key( "badlands_surface");
    private static final Key BADLANDS_PILLAR_NOISE = Key.key( "badlands_pillar");
    private static final Key BADLANDS_PILLAR_ROOF_NOISE = Key.key( "badlands_pillar_roof");
    private static final Key ICEBERG_SURFACE_NOISE = Key.key( "iceberg_surface");
    private static final Key ICEBERG_PILLAR_NOISE = Key.key( "iceberg_pillar");
    private static final Key ICEBERG_PILLAR_ROOF_NOISE = Key.key( "iceberg_pillar_roof");

    /**
     * What the generator reads from the server.
     *
     * @param data             the world generation definitions
     * @param blocks           the block states and tags
     * @param biomeDefinitions the definition of a biome from the biome registry, or {@code null}
     * @param templates        a structure template of the loaded data packs, or {@code null} when none has that key
     */
    public record Environment(WorldgenData data, BlockLookup blocks, Function<String, @Nullable JsonElement> biomeDefinitions,
                              Function<Key, @Nullable StructureTemplateImpl> templates) {
    }

    private final Environment environment;
    private final DimensionTypeDefinition dimensionType;
    private final Key settingsKey;
    private final Key presetKey;
    private final long seed;
    private final NoiseSettings settings;
    private final HeightContext heights;
    private final PositionalRandom worldRandom;
    private final DensityProgram program;
    private final BiomeTable biomes;
    private final BiomeSource biomeSource;
    private final JitteredBiomeGrid zoom;
    private final BiomeClimate biomeClimate;
    private final StatePalette palette = new StatePalette();
    private final TerrainBuilder.Shared terrain;
    private final MaterialContext.MaterialSystem materialSystem;
    private final Map<String, Carver> carvers = new ConcurrentHashMap<>();
    private final FutureCache<Long, GenChunk> terrainCache = new FutureCache<>(TERRAIN_CACHE);
    private final Map<Long, Character> carverBiomes = new ConcurrentHashMap<>();
    // the biome cells of chunks, by mixed chunk key: a chunk reads the cells of its neighbours at its borders
    private final Map<Long, char[]> biomeCells = new ConcurrentHashMap<>();
    private final Map<String, Carver> carversById = new ConcurrentHashMap<>();
    private final ThreadLocal<Worker> workers;
    private final Decorator decorator;

    /**
     * The state a thread keeps to generate chunks.
     */
    private final class Worker {
        final DensityContext density = program.newContext();
        final BiomeSource.Sampler biomeSampler = biomeSource.newSampler();
        final TerrainBuilder builder;

        Worker() {
            final MaterialContext materials = materialSystem.newContext();
            final MaterialRules rules = new MaterialRules(environment.data(), program, environment.blocks(), biomeClimate, worldRandom, materials);
            this.builder = new TerrainBuilder(terrain, materials, rules.rule(settings.materialRule()));
        }
    }

    /**
     * @param environment   what the generator reads from the server
     * @param dimensionType the dimension the chunks are made for; its height has to hold the terrain
     * @param settingsKey   the noise settings, such as {@code minecraft:overworld}
     * @param presetKey     the parameter list of the biome source, such as {@code minecraft:overworld}, or
     *                      {@link EndBiomes#PRESET} for the biomes of the End
     * @param seed          the seed of the world
     */
    public NoiseChunkGenerator(final Environment environment, final DimensionTypeDefinition dimensionType,
                               final Key settingsKey, final Key presetKey, final long seed) {
        this.environment = environment;
        this.dimensionType = dimensionType;
        this.settingsKey = settingsKey;
        this.presetKey = presetKey;
        this.seed = seed;
        final WorldgenData data = environment.data();
        final BlockLookup blocks = environment.blocks();
        this.settings = NoiseSettings.read(data, settingsKey.asString());
        this.heights = new HeightContext(settings.minY(), settings.height(), settings.seaLevel());
        this.worldRandom = settings.legacyRandom() ? PositionalRandom.ofLegacyWorld(seed) : PositionalRandom.ofWorld(seed);
        this.program = new DensityProgram(data, heights, worldRandom, seed);
        this.zoom = new JitteredBiomeGrid(seed);
        this.biomeClimate = new BiomeClimate();
        // air comes first: it is the number read outside the chunks
        palette.id(blocks.air);

        if (EndBiomes.PRESET.equals(presetKey)) {
            final List<String> ids = EndBiomes.BIOMES.stream().map(biome -> biome.key().asString()).toList();
            this.biomes = BiomeTable.of(ids, environment.biomeDefinitions(), data);
            this.biomeSource = EndBiomes.of(program.compile(settings.routed("erosion")), biomes);
        } else {
            final JsonElement parameters = data.get(WorldgenData.Section.BIOME_PARAMETERS, presetKey.asString());
            final Set<String> ordered = new LinkedHashSet<>();
            for (final JsonElement entry : parameters.getAsJsonObject().getAsJsonArray("biomes")) {
                ordered.add(WorldgenData.normalize(entry.getAsJsonObject().get("biome").getAsString()));
            }
            this.biomes = BiomeTable.of(new ArrayList<>(ordered), environment.biomeDefinitions(), data);
            this.biomeSource = new MultiNoiseBiomes(new ClimateSampler(
                    program.compile(settings.routed("temperature")),
                    program.compile(settings.routed("vegetation")),
                    program.compile(settings.routed("continents")),
                    program.compile(settings.routed("erosion")),
                    program.compile(settings.routed("depth")),
                    program.compile(settings.routed("ridges")),
                    ClimateTable.fromReport(parameters, id -> (char) biomes.find(id).index())));
        }

        final Density finalDensity = program.compile(settings.routed("final_density"));
        final AquiferSampler.Inputs aquifers = settings.aquifers() == null ? null : aquiferInputs(settings.aquifers());
        final BlockState defaultFluid = blocks.parse(settings.defaultFluid());
        final FluidLevel lavaLevel = new FluidLevel(-54, blocks.lava);
        final FluidLevel seaLevel = new FluidLevel(settings.seaLevel(), defaultFluid);
        final int lavaBelow = Math.min(-54, settings.seaLevel());
        final AquiferSampler.FluidPicker globalFluid = (x, y, z) -> y < lavaBelow ? lavaLevel : seaLevel;

        this.materialSystem = new MaterialContext.MaterialSystem(settings.seaLevel(),
                program.compile(settings.routed("chunk_surface_level")),
                program.noise(SURFACE_NOISE), program.noise(SURFACE_SECONDARY_NOISE),
                program.noise(CLAY_BANDS_OFFSET_NOISE), worldRandom, biomes,
                MaterialRules.clayBands(worldRandom, blocks));

        this.terrain = new TerrainBuilder.Shared(finalDensity, aquifers, globalFluid,
                worldRandom.named(AQUIFER_RANDOM).splitPositional(), blocks, biomes, biomeClimate, settings.seaLevel(),
                BlockTypeTagKeys.UNCARVABLE,
                new TerrainBuilder.ExtensionNoises(program.noise(BADLANDS_SURFACE_NOISE),
                        program.noise(BADLANDS_PILLAR_NOISE), program.noise(BADLANDS_PILLAR_ROOF_NOISE)),
                new TerrainBuilder.ExtensionNoises(program.noise(ICEBERG_SURFACE_NOISE),
                        program.noise(ICEBERG_PILLAR_NOISE), program.noise(ICEBERG_PILLAR_ROOF_NOISE)),
                worldRandom,
                indexOf(BiomeKeys.ERODED_BADLANDS), indexOf(BiomeKeys.FROZEN_OCEAN), indexOf(BiomeKeys.DEEP_FROZEN_OCEAN));

        this.workers = ThreadLocal.withInitial(Worker::new);
        this.decorator = new Decorator(this);
    }

    private int indexOf(final TypedKey<BiomeDefinition> biome) {
        final BiomeInfo info = biomes.find(biome.key().asString());
        return info == null ? -1 : info.index();
    }

    private AquiferSampler.Inputs aquiferInputs(final JsonObject aquifers) {
        return new AquiferSampler.Inputs(
                program.compile(aquifers.get("barrier")),
                program.compile(aquifers.get("fluid_level_floodedness")),
                program.compile(aquifers.get("fluid_level_spread")),
                program.compile(aquifers.get("lava")),
                program.compile(aquifers.get("exclusion")),
                program.compile(aquifers.get("surface_level")));
    }

    public Environment environment() {
        return environment;
    }

    public long seed() {
        return seed;
    }

    public NoiseSettings settings() {
        return settings;
    }

    public HeightContext heights() {
        return heights;
    }

    public PositionalRandom worldRandom() {
        return worldRandom;
    }

    public DensityProgram program() {
        return program;
    }

    public BiomeTable biomes() {
        return biomes;
    }

    public BiomeClimate biomeClimate() {
        return biomeClimate;
    }

    public StatePalette palette() {
        return palette;
    }

    public Key settingsKey() {
        return settingsKey;
    }

    public Key presetKey() {
        return presetKey;
    }

    /**
     * {@return the number of the biome of a cell of 4 by 4 by 4 blocks}
     */
    public char noiseBiome(final int quartX, final int quartY, final int quartZ) {
        final Worker worker = workers.get();
        return worker.biomeSampler.biomeAt(worker.density, quartX, quartY, quartZ);
    }

    /**
     * {@return the biome of a block, the jitter between cells included}
     */
    public BiomeInfo biomeAt(final int x, final int y, final int z) {
        final int clampedY = Math.clamp(y, heights.minY(), heights.maxY());
        final int corner = zoom.nearestCorner(x, clampedY, z);
        final int cellX = JitteredBiomeGrid.cellOf(x) + ((corner & JitteredBiomeGrid.FAR_X) != 0 ? 1 : 0);
        final int cellY = JitteredBiomeGrid.cellOf(clampedY) + ((corner & JitteredBiomeGrid.FAR_Y) != 0 ? 1 : 0);
        final int cellZ = JitteredBiomeGrid.cellOf(z) + ((corner & JitteredBiomeGrid.FAR_Z) != 0 ? 1 : 0);
        return biomes.get(noiseBiome(cellX, cellY, cellZ));
    }

    /**
     * {@return the value of a density function at a block, evaluated with the context of the calling thread}
     */
    public float sample(final Density function, final int x, final int y, final int z) {
        return function.compute(workers.get().density, x, y, z);
    }

    private BiomeInfo carverBiome(final int chunkX, final int chunkZ) {
        // mixed: the chunk key hashes to x ^ z, so the chunks of a diagonal would share a bin
        final long key = HashCommon.mix(ChunkPos.chunkKey(chunkX, chunkZ));
        Character known = carverBiomes.get(key);
        if (known == null) {
            if (carverBiomes.size() > CARVER_BIOME_CACHE) {
                carverBiomes.clear();
            }
            known = noiseBiome(chunkX << 2, 0, chunkZ << 2);
            carverBiomes.put(key, known);
        }
        return biomes.get(known);
    }

    private Carver carver(final String id) {
        // by the identifier as the biome writes it, so that the hundreds of lookups of a chunk skip normalizing it
        final Carver known = carversById.get(id);
        if (known != null) {
            return known;
        }
        final Carver carver = carvers.computeIfAbsent(WorldgenData.normalize(id),
                key -> Carver.parse(environment.data().get(WorldgenData.Section.CARVER, key)));
        carversById.putIfAbsent(id, carver);
        return carver;
    }

    /**
     * The biome cells of a chunk for its {@link NoiseBiomeGrid}, read from the cells of the chunk and of the eight
     * around it. Each chunk samples the climate of its own 4 by 4 columns of cells once, and its neighbours reuse
     * them for their margins, instead of every grid sampling its 6 by 6 columns. One per terrain being built.
     */
    private final class SharedCells implements NoiseBiomeGrid.CellBiome {
        private final Worker worker;
        private final int minQuartY;
        private final int sizeY;
        private int lastChunkX = Integer.MIN_VALUE;
        private int lastChunkZ = Integer.MIN_VALUE;
        private char @Nullable [] last;

        SharedCells(final Worker worker) {
            this.worker = worker;
            this.minQuartY = (heights.minY() >> 2) - 1;
            this.sizeY = (heights.height() >> 2) + 2;
        }

        @Override
        public char biomeAt(final int quartX, final int quartY, final int quartZ) {
            final int quartY0 = quartY - minQuartY;
            if (quartY0 < 0 || quartY0 >= sizeY) {
                return worker.biomeSampler.biomeAt(worker.density, quartX, quartY, quartZ);
            }
            final int chunkX = quartX >> 2;
            final int chunkZ = quartZ >> 2;
            char[] cells = last;
            if (cells == null || chunkX != lastChunkX || chunkZ != lastChunkZ) {
                cells = cellsOf(chunkX, chunkZ);
                last = cells;
                lastChunkX = chunkX;
                lastChunkZ = chunkZ;
            }
            return cells[((quartX & 3) * 4 + (quartZ & 3)) * sizeY + quartY0];
        }

        private char[] cellsOf(final int chunkX, final int chunkZ) {
            final long key = HashCommon.mix(ChunkPos.chunkKey(chunkX, chunkZ));
            final char[] known = biomeCells.get(key);
            if (known != null) {
                return known;
            }
            final char[] cells = new char[16 * sizeY];
            for (int x = 0; x < 4; x++) {
                for (int z = 0; z < 4; z++) {
                    final int column = (x * 4 + z) * sizeY;
                    for (int y = 0; y < sizeY; y++) {
                        cells[column + y] = worker.biomeSampler.biomeAt(worker.density,
                                (chunkX << 2) + x, minQuartY + y, (chunkZ << 2) + z);
                    }
                }
            }
            if (biomeCells.size() > BIOME_CELL_CACHE) {
                biomeCells.clear();
            }
            biomeCells.put(key, cells);
            return cells;
        }
    }

    /**
     * {@return the terrain of a chunk, before any feature: computed once, then kept for a while}
     */
    public GenChunk terrain(final int chunkX, final int chunkZ) {
        return terrainCache.get(ChunkPos.chunkKey(chunkX, chunkZ), key -> buildTerrain(chunkX, chunkZ));
    }

    /**
     * Builds the terrain of a chunk on the calling thread if no thread has started it, without waiting for the
     * thread that has.
     *
     * @return whether this thread built it
     */
    public boolean prefetchTerrain(final int chunkX, final int chunkZ) {
        return terrainCache.prefetch(ChunkPos.chunkKey(chunkX, chunkZ), key -> buildTerrain(chunkX, chunkZ));
    }

    private GenChunk buildTerrain(final int chunkX, final int chunkZ) {
        final Worker worker = workers.get();
        worker.density.reset();
        final NoiseBiomeGrid grid = new NoiseBiomeGrid(chunkX, chunkZ, heights.minY(), heights.height(), zoom,
                new SharedCells(worker));
        final GenChunk chunk = new GenChunk(chunkX, chunkZ, heights.minY(), heights.height(), palette, environment.blocks().air, grid);
        final CarvingMask mask = carvingMask(chunkX, chunkZ);
        worker.builder.fill(chunk, worker.density, mask.isEmpty() ? null : mask);
        return chunk;
    }

    private CarvingMask carvingMask(final int chunkX, final int chunkZ) {
        final CarvingMask mask = new CarvingMask(heights.minY() + 1, heights.maxY() - PROTECTED_TOP);
        final LegacyRandom random = new LegacyRandom(0L);
        for (int dx = -Carver.RANGE; dx <= Carver.RANGE; dx++) {
            for (int dz = -Carver.RANGE; dz <= Carver.RANGE; dz++) {
                final int sourceX = chunkX + dx;
                final int sourceZ = chunkZ + dz;
                final List<String> sourceCarvers = carverBiome(sourceX, sourceZ).carvers();
                for (int index = 0; index < sourceCarvers.size(); index++) {
                    final Carver carver = carver(sourceCarvers.get(index));
                    random.setLargeFeatureSeed(seed + index, sourceX, sourceZ);
                    if (carver.starts(random)) {
                        carver.carve(heights, random, chunkX, chunkZ, sourceX, sourceZ, mask);
                    }
                }
            }
        }
        return mask;
    }

    @Override
    public ChunkColumn generate(final int chunkX, final int chunkZ) {
        final Decorator.Result decorated = decorator.decorate(chunkX, chunkZ);
        final ChunkColumn column = toColumn(decorated.chunk(), decorated.blockEntities());
        final List<PendingTick<FluidType>> fluids = new ArrayList<>();
        final Set<BlockPosition> started = new HashSet<>();
        for (final int[] tick : decorated.fluidTicks()) {
            if (tick[0] >> 4 == chunkX && tick[2] >> 4 == chunkZ) {
                final BlockPosition position = Position.block(tick[0], tick[1], tick[2]);
                if (started.add(position)) {
                    fluids.add(new PendingTick<>(position, tick[3] == 1 ? FluidType.LAVA : FluidType.WATER, 0, TickPriority.NORMAL));
                }
            }
        }
        // the fluids the terrain asked to start, if the decorations left one there
        final GenChunk chunk = decorated.chunk();
        final int baseX = chunkX << 4;
        final int baseZ = chunkZ << 4;
        chunk.forEachFluidUpdate((x, y, z) -> {
            final FluidStateImpl fluid = FluidBlockCodec.fromBlock(chunk.get(x, y, z));
            final BlockPosition position = Position.block(baseX + x, y, baseZ + z);
            if (!fluid.isEmpty() && started.add(position)) {
                fluids.add(new PendingTick<>(position, fluid.type(), 0, TickPriority.NORMAL));
            }
        });
        if (!fluids.isEmpty()) {
            column.setPendingTicks(new ChunkTicks(List.of(), fluids));
        }
        return column;
    }

    /**
     * {@return what the decorations of this generator share}
     */
    public Decorator decorator() {
        return decorator;
    }

    /**
     * Copies a generated chunk into the storage of the server, its biomes and block entities included.
     */
    private ChunkColumn toColumn(final GenChunk chunk, final Map<Integer, CompoundBinaryTag> blockEntityData) {
        final int minY = dimensionType.minY();
        final int height = dimensionType.height();
        final BiomeInfo fallback = biomes.get(chunk.biomes().sectionBiome(0));
        final ChunkColumn column = new ChunkColumn(chunk.chunkX(), chunk.chunkZ(), minY, height, environment.blocks().air, fallback.key());
        final int baseX = chunk.chunkX() << 4;
        final int baseZ = chunk.chunkZ() << 4;
        final int[] indices = new int[ChunkSection.BLOCK_COUNT];
        for (int section = 0; section < chunk.sectionCount(); section++) {
            final int sectionY = (chunk.minY() >> 4) + section;
            if (sectionY < (minY >> 4) || sectionY >= (minY + height) >> 4) {
                continue;
            }
            final PalettedContainer<BlockState> blocks = blockContainer(chunk, section, indices);
            final PalettedContainer<TypedKey<BiomeDefinition>> biomeContainer = biomeContainer(chunk, section, baseX, baseZ, indices);
            column.putSection(new ChunkSection(sectionY, blocks, biomeContainer));
        }
        addBlockEntities(chunk, column, blockEntityData);
        return column;
    }

    private PalettedContainer<BlockState> blockContainer(final GenChunk chunk, final int section, final int[] indices) {
        final char[] data = chunk.sectionData(section);
        if (data == null) {
            return new PalettedContainer<>(ChunkSection.BLOCK_COUNT, 4, palette.state(chunk.uniformId(section)));
        }
        final Char2IntOpenHashMap local = new Char2IntOpenHashMap();
        local.defaultReturnValue(-1);
        final List<BlockState> states = new ArrayList<>();
        char lastId = data[0];
        int lastIndex = -1;
        for (int i = 0; i < data.length; i++) {
            final char id = data[i];
            // runs of the same state are the rule: skip the map for them
            if (id != lastId || lastIndex < 0) {
                int index = local.get(id);
                if (index < 0) {
                    index = states.size();
                    local.put(id, index);
                    states.add(palette.state(id));
                }
                lastId = id;
                lastIndex = index;
            }
            indices[i] = lastIndex;
        }
        if (states.size() == 1) {
            return new PalettedContainer<>(ChunkSection.BLOCK_COUNT, 4, states.getFirst());
        }
        return PalettedContainer.fromNbt(ChunkSection.BLOCK_COUNT, 4, states,
                BitPacking.pack(indices, BitPacking.bitsFor(states.size(), 4)));
    }

    private PalettedContainer<TypedKey<BiomeDefinition>> biomeContainer(final GenChunk chunk, final int section,
                                                                        final int baseX, final int baseZ, final int[] indices) {
        final NoiseBiomeGrid grid = chunk.biomes();
        if (grid.sectionIsUniform(section)) {
            return new PalettedContainer<>(ChunkSection.BIOME_COUNT, 1, biomes.get(grid.sectionBiome(section)).key());
        }
        final int baseY = chunk.minY() + (section << 4);
        final Char2IntOpenHashMap local = new Char2IntOpenHashMap();
        local.defaultReturnValue(-1);
        final List<TypedKey<BiomeDefinition>> keys = new ArrayList<>();
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    final char biome = grid.biome(baseX + x, baseY + y, baseZ + z);
                    int index = local.get(biome);
                    if (index < 0) {
                        index = keys.size();
                        local.put(biome, index);
                        keys.add(biomes.get(biome).key());
                    }
                    indices[y << 8 | z << 4 | x] = index;
                }
            }
        }
        if (keys.size() == 1) {
            return new PalettedContainer<>(ChunkSection.BIOME_COUNT, 1, keys.getFirst());
        }
        return PalettedContainer.fromNbt(ChunkSection.BIOME_COUNT, 1, keys, BitPacking.pack(indices, BitPacking.bitsFor(keys.size(), 1)));
    }

    /**
     * Gives the blocks that need one a block entity, with the data the features wrote for it.
     *
     * @param data the data by {@link BlockEntity#positionKey}
     */
    private void addBlockEntities(final GenChunk chunk, final ChunkColumn column, final Map<Integer, CompoundBinaryTag> data) {
        for (int section = 0; section < chunk.sectionCount(); section++) {
            final char[] blocks = chunk.sectionData(section);
            if (blocks == null) {
                continue;
            }
            final int baseY = chunk.minY() + (section << 4);
            boolean any = false;
            final int sectionIndex = (baseY >> 4) - column.minSectionY();
            if (sectionIndex < 0 || sectionIndex >= column.sectionCount()) {
                continue;
            }
            for (final BlockState state : column.sections()[sectionIndex].blocks().palette()) {
                if (state.blockEntityType() != null) {
                    any = true;
                    break;
                }
            }
            if (!any) {
                continue;
            }
            for (int i = 0; i < blocks.length; i++) {
                final BlockState state = palette.state(blocks[i]);
                final TypedKey<BlockEntityType> type = state.blockEntityType();
                if (type == null) {
                    continue;
                }
                final int x = i & 15;
                final int z = (i >> 4) & 15;
                final int y = baseY + (i >> 8);
                column.putBlockEntity(new BlockEntity(x, y, z, type, data.get(BlockEntity.positionKey(x, y, z))));
            }
        }
    }

    @Override
    public ChunkGeneratorConfig describeForSave() {
        return new ChunkGeneratorConfig.Noise(settingsKey, presetKey);
    }

    @Override
    public NoiseChunkGenerator noise() {
        return this;
    }

    @Override
    public DimensionTypeDefinition dimensionType() {
        return dimensionType;
    }
}
