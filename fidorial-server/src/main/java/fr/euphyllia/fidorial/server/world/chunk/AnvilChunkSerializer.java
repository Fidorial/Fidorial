package fr.euphyllia.fidorial.server.world.chunk;

import fr.euphyllia.fidorial.server.VersionConstants;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.fluid.FluidType;
import fr.euphyllia.fidorial.server.world.light.ChunkLightData;
import fr.euphyllia.fidorial.server.world.storage.datafixers.minecraft.V26_4.chunk.V5119;
import fr.euphyllia.fidorial.server.world.storage.datafixers.util.nbt.NbtMapType;
import fr.euphyllia.fidorial.server.world.structure.worldgen.Heightmap;
import fr.euphyllia.fidorial.server.world.tick.ChunkTicks;
import fr.euphyllia.fidorial.server.world.tick.PendingTick;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.light.LightType;
import fr.fidorial.world.redstone.TickPriority;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

public class AnvilChunkSerializer {

    private static final TickPriority[] PRIORITIES = TickPriority.values();

    /**
     * The priorities are saved from {@code -3}, the most urgent, to {@code 3}: normal is {@code 0}.
     */
    private static final int NORMAL_PRIORITY = TickPriority.NORMAL.ordinal();

    private final int dataVersion;

    public AnvilChunkSerializer() {
        this(VersionConstants.DATA_VERSION);
    }

    public AnvilChunkSerializer(final int dataVersion) {
        this.dataVersion = dataVersion;
    }

    /**
     * Writes a chunk.
     *
     * @param chunk     the chunk
     * @param scheduled the ticks scheduled for its blocks and fluids, saved after those it was read
     *                  with and that are not scheduled yet
     * @return its data
     */
    public CompoundBinaryTag toNbt(final ChunkColumn chunk, final ChunkTicks scheduled) {
        final CompoundBinaryTag.Builder root = CompoundBinaryTag.builder();
        root.putInt("DataVersion", dataVersion);
        root.putInt("xPos", chunk.chunkX());
        root.putInt("zPos", chunk.chunkZ());
        root.putInt("yPos", chunk.minSectionY());
        root.putString("status", chunk.status().asString());
        root.putLong("LastUpdate", chunk.lastUpdate());
        root.putLong("InhabitedTime", chunk.inhabitedTime());
        root.putBoolean("isLightOn", chunk.lightPopulated());

        final ChunkLightData light = chunk.lightData();
        final int minSectionY = chunk.minSectionY();

        final ListBinaryTag.Builder<BinaryTag> sections = ListBinaryTag.builder();
        for (final ChunkSection section : chunk.sections()) {
            final int lightIndex = section.sectionY() - minSectionY;
            final byte[] blockLight = light.sectionArray(LightType.BLOCK, lightIndex);
            final byte[] skyLight = light.sectionArray(LightType.SKY, lightIndex);
            sections.add(sectionToNbt(section, blockLight, skyLight));
        }
        root.put("sections", sections.build());

        root.putIntArray("LightHeightmap", light.heightmapSnapshot());

        final boolean full = chunk.status().equals(ChunkColumn.FULL_STATUS);
        final CompoundBinaryTag.Builder heightmaps = CompoundBinaryTag.builder();
        for (final Heightmap type : Heightmap.values()) {
            if (full && type.generationOnly()) {
                continue;
            }
            heightmaps.putLongArray(type.name(), chunk.heightmap(type));
        }
        root.put("Heightmaps", heightmaps.build());

        root.put("block_entities", blockEntitiesToNbt(chunk));
        final ChunkTicks ticks = chunk.pendingTicks().and(scheduled);
        root.put("block_ticks", ticksToNbt(ticks.blocks(), block -> block.key().asString()));
        root.put("fluid_ticks", ticksToNbt(ticks.fluids(), fluid -> fluid.fluidKey().key().asString()));
        return root.build();
    }

    private static <T> ListBinaryTag ticksToNbt(final List<PendingTick<T>> ticks, final Function<T, String> id) {
        if (ticks.isEmpty()) {
            return ListBinaryTag.empty();
        }
        final ListBinaryTag.Builder<BinaryTag> list = ListBinaryTag.builder();
        for (final PendingTick<T> tick : ticks) {
            list.add(CompoundBinaryTag.builder()
                    .putString("i", id.apply(tick.type()))
                    .putInt("x", tick.position().blockX())
                    .putInt("y", tick.position().blockY())
                    .putInt("z", tick.position().blockZ())
                    .putInt("t", tick.delay())
                    .putInt("p", tick.priority().ordinal() - NORMAL_PRIORITY)
                    .build());
        }
        return list.build();
    }

    private static ChunkTicks ticksFromNbt(final CompoundBinaryTag root, final int chunkX, final int chunkZ) {
        return new ChunkTicks(
                ticksFromNbt(root.getList("block_ticks"), chunkX, chunkZ, key -> TypedKey.withRegistry(RegistryKey.BLOCK, key)),
                ticksFromNbt(root.getList("fluid_ticks"), chunkX, chunkZ,
                        key -> FluidType.byFluidKey(TypedKey.withRegistry(RegistryKey.FLUID, key))));
    }

    /**
     * Reads ticks, leaving out those of another chunk, those of unknown priority, and those whose
     * id cannot be read or names nothing ticking.
     */
    private static <T> List<PendingTick<T>> ticksFromNbt(final ListBinaryTag list, final int chunkX, final int chunkZ,
                                                         final Function<Key, @Nullable T> type) {
        final List<PendingTick<T>> ticks = new ArrayList<>();
        for (final BinaryTag tag : list) {
            if (!(tag instanceof final CompoundBinaryTag compound)) {
                continue;
            }
            final String id = compound.getString("i");
            final int x = compound.getInt("x");
            final int z = compound.getInt("z");
            final int priority = compound.getInt("p") + NORMAL_PRIORITY;
            if (id.isEmpty() || !Key.parseable(id) || x >> 4 != chunkX || z >> 4 != chunkZ
                    || priority < 0 || priority >= PRIORITIES.length) {
                continue;
            }
            final T ticked = type.apply(Key.key(id));
            if (ticked != null) {
                ticks.add(new PendingTick<>(Position.block(x, compound.getInt("y"), z), ticked, compound.getInt("t"),
                        PRIORITIES[priority]));
            }
        }
        return ticks;
    }

    /**
     * Keys owned by the Anvil container itself; they are stripped from the
     * payload kept in memory because the protocol sends the block entity data
     * without its X, Y and Z values.
     */
    private static final Set<String> BLOCK_ENTITY_CONTAINER_KEYS = Set.of("id", "x", "y", "z", "keepPacked");

    private ListBinaryTag blockEntitiesToNbt(final ChunkColumn chunk) {
        final ListBinaryTag.Builder<BinaryTag> list = ListBinaryTag.builder();

        for (final BlockEntity blockEntity : chunk.blockEntities()) {
            final CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();

            final CompoundBinaryTag data = blockEntity.data();
            if (data != null) {
                for (final Map.Entry<String, ? extends BinaryTag> entry : data) {
                    if (!BLOCK_ENTITY_CONTAINER_KEYS.contains(entry.getKey())) {
                        compound.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            compound.putString("id", blockEntity.type().key().asString());
            compound.putInt("x", (chunk.chunkX() << 4) + blockEntity.localX());
            compound.putInt("y", blockEntity.y());
            compound.putInt("z", (chunk.chunkZ() << 4) + blockEntity.localZ());
            compound.putBoolean("keepPacked", false);

            list.add(compound.build());
        }

        return list.build();
    }

    private void blockEntitiesFromNbt(final CompoundBinaryTag root, final ChunkColumn chunk) {
        for (final BinaryTag tag : root.getList("block_entities")) {
            if (!(tag instanceof final CompoundBinaryTag compound) || !compound.contains("id")) {
                continue;
            }

            final int localX = compound.getInt("x") & 15;
            final int localZ = compound.getInt("z") & 15;
            final int y = compound.getInt("y");

            final CompoundBinaryTag.Builder data = CompoundBinaryTag.builder();
            for (final Map.Entry<String, ? extends BinaryTag> entry : compound) {
                if (!BLOCK_ENTITY_CONTAINER_KEYS.contains(entry.getKey())) {
                    data.put(entry.getKey(), entry.getValue());
                }
            }
            final CompoundBinaryTag built = data.build();

            chunk.putBlockEntity(new BlockEntity(localX,
                    y,
                    localZ,
                    TypedKey.withRegistry(RegistryKey.BLOCK_ENTITY_TYPE, compound.getString("id")),
                    built.keySet().isEmpty() ? null : built));
        }
    }

    private CompoundBinaryTag sectionToNbt(final ChunkSection section, final byte @Nullable [] blockLight, final byte @Nullable [] skyLight) {
        final CompoundBinaryTag.Builder c = CompoundBinaryTag.builder();
        c.putByte("Y", (byte) section.sectionY());

        if (blockLight != null && !ChunkLightData.isAllZero(blockLight)) {
            c.putByteArray("BlockLight", blockLight);
        }
        if (skyLight != null && !ChunkLightData.isAllZero(skyLight)) {
            c.putByteArray("SkyLight", skyLight);
        }

        // block_states
        final CompoundBinaryTag.Builder blockStates = CompoundBinaryTag.builder();
        final ListBinaryTag.Builder<BinaryTag> blockPalette = ListBinaryTag.builder();
        for (final BlockState state : section.blocks().palette()) {
            blockPalette.add(blockStateToNbt(state));
        }
        blockStates.put("palette", blockPalette.build());
        final long[] blockData = section.blocks().packedData();
        if (blockData != null) {
            blockStates.putLongArray("data", blockData);
        }
        c.put("block_states", blockStates.build());

        // biomes
        final CompoundBinaryTag.Builder biomes = CompoundBinaryTag.builder();
        final ListBinaryTag.Builder<BinaryTag> biomePalette = ListBinaryTag.builder();
        for (final TypedKey<BiomeDefinition> biome : section.biomes().palette()) {
            biomePalette.add(StringBinaryTag.stringBinaryTag(biome.key().asString()));
        }
        biomes.put("palette", biomePalette.build());
        final long[] biomeData = section.biomes().packedData();
        if (biomeData != null) {
            biomes.putLongArray("data", biomeData);
        }
        c.put("biomes", biomes.build());

        return c.build();
    }

    private CompoundBinaryTag blockStateToNbt(final BlockState state) {
        final CompoundBinaryTag.Builder c = CompoundBinaryTag.builder();
        c.putString("Name", state.name().key().asString());
        if (!state.properties().isEmpty()) {
            final CompoundBinaryTag.Builder props = CompoundBinaryTag.builder();
            for (final var e : state.properties().entrySet()) {
                props.putString(e.getKey(), e.getValue());
            }
            c.put("Properties", props.build());
        }
        return c.build();
    }

    public ChunkColumn fromNbt(final CompoundBinaryTag root, final int minY, final int height, final BlockState defaultBlock, final TypedKey<BiomeDefinition> defaultBiome) {
        final int chunkX = root.getInt("xPos");
        final int chunkZ = root.getInt("zPos");

        final ChunkColumn chunk = new ChunkColumn(chunkX, chunkZ, minY, height, defaultBlock, defaultBiome);
        chunk.setStatus(root.contains("status") ? Key.key(root.getString("status")) : ChunkColumn.FULL_STATUS);
        chunk.setInhabitedTime(root.getLong("InhabitedTime"));
        chunk.setLastUpdate(root.getLong("LastUpdate"));

        final ChunkLightData light = chunk.lightData();
        final int minSectionY = chunk.minSectionY();

        for (final BinaryTag tag : root.getList("sections")) {
            if (tag instanceof final CompoundBinaryTag sc) {
                final ChunkSection section = sectionFromNbt(sc, defaultBlock, defaultBiome);
                if (section != null) {
                    chunk.putSection(section);
                    final int lightIndex = section.sectionY() - minSectionY;
                    if (sc.contains("BlockLight")) {
                        light.setSectionArray(LightType.BLOCK, lightIndex, sc.getByteArray("BlockLight"));
                    }
                    if (sc.contains("SkyLight")) {
                        light.setSectionArray(LightType.SKY, lightIndex, sc.getByteArray("SkyLight"));
                    }
                }
            }
        }

        final boolean full = chunk.status().equals(ChunkColumn.FULL_STATUS);
        final CompoundBinaryTag stored = root.getCompound("Heightmaps");
        for (final Heightmap type : Heightmap.values()) {
            if (full && type.generationOnly()) continue; // full chunks have no generation heightmaps
            if (stored.contains(type.name())) {
                chunk.restoreHeightmap(type, stored.getLongArray(type.name()));
            }
        }

        blockEntitiesFromNbt(root, chunk);
        chunk.setPendingTicks(ticksFromNbt(root, chunkX, chunkZ));

        if (root.contains("LightHeightmap") && root.getBoolean("isLightOn")) {
            light.restoreHeightmap(root.getIntArray("LightHeightmap"));
        }
        chunk.setLightPopulated(root.contains("isLightOn") && root.getBoolean("isLightOn"));

        return chunk;
    }

    private @Nullable ChunkSection sectionFromNbt(final CompoundBinaryTag c, final BlockState defaultBlock, final TypedKey<BiomeDefinition> defaultBiome) {
        if (!c.contains("Y")) return null;
        final int sectionY = c.getByte("Y");

        // block_states
        final List<BlockState> blockPalette = new ArrayList<>();
        final CompoundBinaryTag bs = c.getCompound("block_states");
        for (final BinaryTag t : bs.getList("palette")) {
            if (t instanceof final CompoundBinaryTag entry) {
                blockPalette.add(blockStateFromNbt(entry));
            }
        }
        if (blockPalette.isEmpty()) blockPalette.add(defaultBlock);
        final PalettedContainer<BlockState> blocks =
                PalettedContainer.fromNbt(ChunkSection.BLOCK_COUNT, 4, blockPalette, bs.getLongArray("data"));

        // biomes
        final List<TypedKey<BiomeDefinition>> biomePalette = new ArrayList<>();
        final CompoundBinaryTag bio = !c.contains("biomes") && c.contains("noise_biomes")
                ? ((NbtMapType) V5119.expandNoiseBiomes(NbtMapType.of(c.getCompound("noise_biomes")))).toCompound()
                : c.getCompound("biomes");
        for (final BinaryTag t : bio.getList("palette")) {
            if (t instanceof final StringBinaryTag st) biomePalette.add(TypedKey.withRegistry(RegistryKey.BIOME, st.value()));
        }
        if (biomePalette.isEmpty()) biomePalette.add(defaultBiome);
        final PalettedContainer<TypedKey<BiomeDefinition>> biomes =
                PalettedContainer.fromNbt(ChunkSection.BIOME_COUNT, 1, biomePalette, bio.getLongArray("data"));

        return new ChunkSection(sectionY, blocks, biomes);
    }

    private BlockState blockStateFromNbt(final CompoundBinaryTag c) {
        final String name = c.getString("Name");
        final CompoundBinaryTag props = c.getCompound("Properties");
        if (props.keySet().isEmpty()) {
            return BlockState.withoutProperties(TypedKey.withRegistry(RegistryKey.BLOCK, name));
        }
        final Map<String, String> map = new TreeMap<>();
        for (final String key : props.keySet()) {
            if (props.get(key) instanceof final StringBinaryTag st) {
                map.put(key, st.value());
            }
        }
        return BlockState.withProperties(TypedKey.withRegistry(RegistryKey.BLOCK, name), map);
    }
}
