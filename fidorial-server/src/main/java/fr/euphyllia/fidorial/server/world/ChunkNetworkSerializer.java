package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.network.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.chunk.BitPacking;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.euphyllia.fidorial.server.world.chunk.PalettedContainer;
import fr.euphyllia.fidorial.server.world.light.ChunkLightData;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.light.LightType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ChunkNetworkSerializer {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ChunkNetworkSerializer.class);
    private static final boolean BIKINI_BOTTOM = Boolean.getBoolean("Fidorial.bikiniBottom");

    private final BlockStateRegistry blockRegistry;
    private final ServerRegistry<BiomeDefinition> biomes;
    private static final int MAX_INDIRECT_BIOME_BITS = 8;

    private static final Key FALLBACK_BIOME = Key.key("plains");

    private static final int HEIGHTMAP_WORLD_SURFACE = 1;
    private static final int HEIGHTMAP_MOTION_BLOCKING = 4;

    public ChunkNetworkSerializer(final BlockStateRegistry blockRegistry, final ServerRegistry<BiomeDefinition> biomes) {
        this.blockRegistry = blockRegistry;
        this.biomes = biomes;
    }

    private int biomeId(final Key biome) {
        final int id = biomes.networkId(biome);
        if (id >= 0) {
            return id;
        }
        final int fallback = biomes.networkId(FALLBACK_BIOME);
        return Math.max(fallback, 0);
    }

    public void writeChunk(final PacketBuffer p, final ByteBufAllocator alloc, final ChunkColumn chunk, final boolean hasSkylight) {
        p.writeInt(chunk.chunkX());
        p.writeInt(chunk.chunkZ());
        writeHeightmaps(p, chunk);

        final ByteBuf sections = alloc.buffer();
        try {
            final PacketBuffer sp = new PacketBuffer(sections);
            for (final ChunkSection section : chunk.sections()) {
                writeSection(sp, section);
            }
            p.writeVarInt(sections.readableBytes());
            p.nettyBuf().writeBytes(sections);
        } finally {
            sections.release();
        }

        writeBlockEntities(p, chunk);

        writeLightData(p, chunk, hasSkylight);
    }

    /**
     * Writes the {@code Block Entities} prefixed array of
     * {@code level_chunk_with_light}.
     *
     * <p>Each entry is a packed XZ byte, the absolute height as a short, the
     * {@code minecraft:block_entity_type} protocol ID as a VarInt, and the block
     * entity NBT stripped of its X, Y and Z values.</p>
     */
    private void writeBlockEntities(final PacketBuffer p, final ChunkColumn chunk) {

        final List<BlockEntity> encodable = new ArrayList<>();

        for (final BlockEntity blockEntity : chunk.blockEntities()) {
            if (blockEntity.isKnown()) {
                encodable.add(blockEntity);
            } else {
                LOGGER.warn("Skipping block entity with unknown type '{}' at {},{},{} in chunk {},{}.",
                        blockEntity.type(),
                        blockEntity.localX(),
                        blockEntity.y(),
                        blockEntity.localZ(),
                        chunk.chunkX(),
                        chunk.chunkZ());
            }
        }

        p.writeVarInt(encodable.size());

        for (final BlockEntity blockEntity : encodable) {
            p.writeByte(blockEntity.packedXz());
            p.writeShort(blockEntity.y());
            p.writeVarInt(blockEntity.protocolId());
            p.writeNbt(blockEntity.data());
            LOGGER.debug("BlockEntity : {} protocolId : {}", blockEntity.type(), blockEntity.protocolId());
        }
    }

    private void writeHeightmaps(final PacketBuffer p, final ChunkColumn chunk) {
        if (BIKINI_BOTTOM) {
            p.writeVarInt(0);
            return;
        }
        // both heightmaps use the same predicate currently
        final long[] motionBlocking = chunk.computeMotionBlockingHeightmap();
        //noinspection UnnecessaryLocalVariable
        final long[] worldSurface = motionBlocking; /*chunk.computeWorldSurfaceHeightmap();*/

        p.writeVarInt(2);

        p.writeVarInt(HEIGHTMAP_WORLD_SURFACE);
        p.writeLongArray(worldSurface);

        p.writeVarInt(HEIGHTMAP_MOTION_BLOCKING);
        p.writeLongArray(motionBlocking);
    }

    private void writeSection(final PacketBuffer sp, final ChunkSection section) {
        final PalettedContainer.PackedSnapshot<BlockState> snap = section.blocks().packedSnapshot();

        sp.writeShort(section.nonAirCount());   // nonEmptyBlockCount
        sp.writeShort(section.fluidCount());    // fluidCount

        if (snap.palette().size() == 1) {
            sp.writeByte(0);           // bitsPerEntry = 0 (single value)
            sp.writeVarInt(blockRegistry.networkId(snap.palette().getFirst()));
        } else {
            sp.writeByte(snap.bits());
            sp.writeVarInt(snap.palette().size());
            for (final BlockState state : snap.palette()) {
                sp.writeVarInt(blockRegistry.networkId(state));
            }
            writeLongs(sp, snap.data(), snap.bits(), ChunkSection.BLOCK_COUNT);
        }

        writeBiomes(sp, section.biomes());
    }

    private void writeBiomes(final PacketBuffer sp, final PalettedContainer<Key> container) {
        final PalettedContainer.PackedSnapshot<Key> snap = container.packedSnapshot();
        final List<Key> palette = snap.palette();

        if (palette.size() == 1) {
            sp.writeByte(0);
            sp.writeVarInt(biomeId(palette.getFirst()));
            return;
        }

        if (snap.bits() <= MAX_INDIRECT_BIOME_BITS) {
            sp.writeByte(snap.bits());
            sp.writeVarInt(palette.size());
            for (final Key biome : palette) {
                sp.writeVarInt(biomeId(biome));
            }
            writeLongs(sp, snap.data(), snap.bits(), ChunkSection.BIOME_COUNT);
            return;
        }

        final PalettedContainer.PalettedContainerSnapshot<Key> full = container.snapshot();
        final int directBits = BitPacking.bitsFor(biomes.size(), 1);
        sp.writeByte(directBits);
        final int[] global = new int[ChunkSection.BIOME_COUNT];
        for (int i = 0; i < global.length; i++) {
            global[i] = biomeId(full.get(i));
        }
        writeLongs(sp, BitPacking.pack(global, directBits), directBits, ChunkSection.BIOME_COUNT);
    }

    public byte[] buildBiomes(final ByteBufAllocator alloc, final ChunkColumn chunk) {
        final ByteBuf buffer = alloc.buffer();
        try {
            final PacketBuffer out = new PacketBuffer(buffer);
            for (final ChunkSection section : chunk.sections()) {
                writeBiomes(out, section.biomes());
            }
            final byte[] encoded = new byte[buffer.readableBytes()];
            buffer.readBytes(encoded);
            return encoded;
        } finally {
            buffer.release();
        }
    }

    private static void writeLongs(final PacketBuffer sp, final long @Nullable [] data, final int bits, final int entries) {
        final int entriesPerLong = 64 / bits;
        final int expectedLongs = (entries + entriesPerLong - 1) / entriesPerLong;
        for (int i = 0; i < expectedLongs; i++) {
            sp.writeLong(data != null && i < data.length ? data[i] : 0L);
        }
    }

    public void writeLightData(final PacketBuffer p, final ChunkColumn chunk, final boolean hasSkylight) {
        final int worldSections = chunk.sectionCount();
        final int lightSections = worldSections + 2;
        final int topIndex = lightSections - 1;

        final ChunkLightData light = chunk.lightData();

        final long[] skyMask = new long[(lightSections + 63) >> 6];
        final long[] blockMask = new long[skyMask.length];
        final long[] emptySkyMask = new long[skyMask.length];
        final long[] emptyBlockMask = new long[skyMask.length];

        final List<byte[]> skyArrays = new ArrayList<>();
        final List<byte[]> blockArrays = new ArrayList<>();

        for (int i = 0; i < lightSections; i++) {
            if (hasSkylight && i == topIndex) {
                setBit(skyMask, i);
                skyArrays.add(ChunkLightData.FULL_LIGHT);
            } else if (hasSkylight) {
                if (i != 0) { // below world so no light
                    final byte[] sky = light.materializeSkySection(i - 1);
                    if (sky == null) {
                        setBit(emptySkyMask, i);
                    } else {
                        setBit(skyMask, i);
                        skyArrays.add(sky);
                    }
                }
            }

            if (!(i == 0) && !(i == topIndex)) { // vanilla seems not to send any mask if it's a boundary
                final byte[] block = light.sectionArray(LightType.BLOCK, i - 1);
                if (block == null || ChunkLightData.isAllZero(block)) {
                    setBit(emptyBlockMask, i);
                } else {
                    setBit(blockMask, i);
                    blockArrays.add(block);
                }
            }
        }

        p.writeBitSet(skyMask);
        p.writeBitSet(blockMask);
        p.writeBitSet(emptySkyMask);
        p.writeBitSet(emptyBlockMask);

        p.writeVarInt(skyArrays.size());
        for (final byte[] array : skyArrays) {
            p.writeVarInt(ChunkLightData.SECTION_BYTES);
            p.writeRawBytes(array);
        }

        p.writeVarInt(blockArrays.size());
        for (final byte[] array : blockArrays) {
            p.writeVarInt(ChunkLightData.SECTION_BYTES);
            p.writeRawBytes(array);
        }
    }

    private static void setBit(final long[] words, final int bit) {
        words[bit >> 6] |= 1L << (bit & 63);
    }
}
