package fr.euphyllia.fidorial.server.world;

import fr.euphyllia.fidorial.server.codecs.networking.NetworkEncoder;
import fr.euphyllia.fidorial.server.network.serialization.PacketBuffer;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.block.blockentity.BlockEntity;
import fr.euphyllia.fidorial.server.world.chunk.BitPacking;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.ChunkSection;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.euphyllia.fidorial.server.world.chunk.PalettedContainer;
import fr.euphyllia.fidorial.server.world.light.ChunkLightData;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.BlockEntityType;
import fr.fidorial.registry.keys.BiomeKeys;
import fr.fidorial.registry.keys.BlockEntityTypeKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import fr.fidorial.world.light.LightType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

public final class ChunkNetworkSerializer {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(ChunkNetworkSerializer.class);
    private static final boolean BIKINI_BOTTOM = Boolean.getBoolean("Fidorial.bikiniBottom");

    private final ServerRegistry<BlockEntityType> blockEntityTypes;
    private final NetworkEncoder<PacketBuffer, PalettedContainer<BlockState>> blockStates;
    private final NetworkEncoder<PacketBuffer, PalettedContainer<TypedKey<BiomeDefinition>>> biomeStates;

    private static final TypedKey<BiomeDefinition> FALLBACK_BIOME = BiomeKeys.PLAINS;

    /**
     * The block entities clients are told nothing of but their type, as in vanilla: what a chest or
     * a barrel holds, and the loot table it has yet to roll, stay on the server.
     */
    private static final Set<TypedKey<BlockEntityType>> SERVER_SIDE_DATA =
            Set.of(BlockEntityTypeKeys.CHEST, BlockEntityTypeKeys.TRAPPED_CHEST, BlockEntityTypeKeys.BARREL);

    public ChunkNetworkSerializer(final BlockStateRegistry blockRegistry, final ServerRegistry<BiomeDefinition> biomes, final ServerRegistry<BlockEntityType> blockEntityTypes) {
        this.blockEntityTypes = blockEntityTypes;
        // the client reads anything wider as registry ids
        this.blockStates = palettedContainer(8, () -> BitPacking.bitsFor(blockRegistry.size(), 1), blockRegistry::networkId);
        this.biomeStates = palettedContainer(8, () -> BitPacking.bitsFor(biomes.size(), 1), biome -> biomeId(biomes, biome));
    }

    private static int biomeId(final ServerRegistry<BiomeDefinition> biomes, final TypedKey<BiomeDefinition> biome) {
        final int id = biomes.networkId(biome);
        if (id >= 0) {
            return id;
        }
        final int fallback = biomes.networkId(FALLBACK_BIOME);
        return Math.max(fallback, 0);
    }

    private static <T> NetworkEncoder<PacketBuffer, PalettedContainer<T>> palettedContainer(final int maxPaletteBits, final IntSupplier registryBits, final ToIntFunction<T> networkId) {
        return (buf, container) -> {
            final PalettedContainer.PackedSnapshot<T> snap = container.packedSnapshot();
            final List<T> palette = snap.palette();
            if (palette.size() == 1) {
                buf.writeByte(0);
                buf.writeVarInt(networkId.applyAsInt(palette.getFirst()));
            } else if (snap.bits() <= maxPaletteBits) {
                buf.writeByte(snap.bits());
                buf.writeVarInt(palette.size());
                for (final T value : palette) {
                    buf.writeVarInt(networkId.applyAsInt(value));
                }
                writeLongs(buf, snap.data(), snap.bits(), container.size());
            } else {
                final int bits = registryBits.getAsInt();
                buf.writeByte(bits);
                writeLongs(buf, container.packedGlobal(bits, networkId), bits, container.size());
            }
        };
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
            if (blockEntityTypes.networkId(blockEntity.type()) >= 0) {
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
            final int networkId = blockEntityTypes.networkId(blockEntity.type());
            p.writeVarInt(networkId);
            p.writeNbt(SERVER_SIDE_DATA.contains(blockEntity.type()) ? null : blockEntity.data());
            LOGGER.debug("BlockEntity : {} protocolId : {}", blockEntity.type(), networkId);
        }
    }

    private void writeHeightmaps(final PacketBuffer p, final ChunkColumn chunk) {
        if (BIKINI_BOTTOM) {
            p.writeVarInt(0);
            return;
        }
        p.writeVarInt(Heightmap.SENT_TO_CLIENTS.size());
        for (final Heightmap type : Heightmap.SENT_TO_CLIENTS) {
            p.writeVarInt(type.networkId());
            p.writeLongArray(chunk.heightmap(type));
        }
    }

    private void writeSection(final PacketBuffer sp, final ChunkSection section) {
        sp.writeShort(section.nonAirCount());
        sp.writeShort(section.fluidCount());
        blockStates.write(sp, section.blocks());
        biomeStates.write(sp, section.biomes());
    }

    public byte[] buildBiomes(final ByteBufAllocator alloc, final ChunkColumn chunk) {
        final ByteBuf buffer = alloc.buffer();
        try {
            final PacketBuffer out = new PacketBuffer(buffer);
            for (final ChunkSection section : chunk.sections()) {
                biomeStates.write(out, section.biomes());
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
