package fr.euphyllia.fidorial.server.world.chunk;

import fr.euphyllia.fidorial.server.world.light.BlockLightProperties;
import net.kyori.adventure.key.Key;

public final class ChunkSection {

    public static final int BLOCK_COUNT = 4096; // 16^3
    public static final int BIOME_COUNT = 4096;    // 16^3, one biome per block since 26.4

    private final int sectionY; // indice de section (ex. -4 pour y=-64)
    private final PalettedContainer<BlockState> blocks;
    private final PalettedContainer<Key> biomes;
    private int nonAirCount;
    private int fluidCount;

    public ChunkSection(final int sectionY, final BlockState fillBlock, final Key fillBiome) {
        this.sectionY = sectionY;
        this.blocks = new PalettedContainer<>(BLOCK_COUNT, 4, fillBlock);
        this.biomes = new PalettedContainer<>(BIOME_COUNT, 1, fillBiome);
        this.nonAirCount = fillBlock.isAir() ? 0 : BLOCK_COUNT;
        this.fluidCount = fillBlock.isFluid() ? BLOCK_COUNT : 0;
    }

    public ChunkSection(final int sectionY, final PalettedContainer<BlockState> blocks,
                        final PalettedContainer<Key> biomes) {
        this.sectionY = sectionY;
        this.blocks = blocks;
        this.biomes = biomes;
        recomputeCounts();
    }

    private static int blockIndex(final int x, final int y, final int z) {
        return (y << 8) | (z << 4) | x;
    }

    public int sectionY() {
        return sectionY;
    }

    public PalettedContainer<BlockState> blocks() {
        return blocks;
    }

    public PalettedContainer<Key> biomes() {
        return biomes;
    }

    public int nonAirCount() {
        return nonAirCount;
    }

    public int fluidCount() {
        return fluidCount;
    }

    public boolean isEmpty() {
        return nonAirCount == 0;
    }

    public void setBlock(final int x, final int y, final int z, final BlockState state) {
        final int i = blockIndex(x, y, z);
        final BlockState previous = blocks.get(i);

        final boolean wasAir = previous.isAir();
        final boolean isAir = state.isAir();

        if (wasAir && !isAir) nonAirCount++;
        else if (!wasAir && isAir) nonAirCount--;

        final boolean wasFluid = previous.isFluid();
        final boolean isFluid = state.isFluid();

        if (!wasFluid && isFluid) fluidCount++;
        else if (wasFluid && !isFluid) fluidCount--;

        blocks.set(i, state);
    }

    public BlockState getBlock(final int x, final int y, final int z) {
        return blocks.get(blockIndex(x, y, z));
    }

    public void setBiome(final int x, final int y, final int z, final Key biome) {
        biomes.set(blockIndex(x, y, z), biome);
    }

    public Key getBiome(final int x, final int y, final int z) {
        return biomes.get(blockIndex(x, y, z));
    }

    public boolean containsEmissiveBlocks() {
        return blocks.contains(BlockLightProperties::hasEmission);
    }

    public void recomputeCounts() {
        final PalettedContainer.PalettedContainerSnapshot<BlockState> snap = blocks.snapshot();
        int nonAir = 0, fluids = 0;
        for (int i = 0; i < BLOCK_COUNT; i++) {
            final BlockState s = snap.get(i);
            if (!s.isAir()) nonAir++;
            if (s.isFluid()) fluids++;
        }
        this.nonAirCount = nonAir;
        this.fluidCount = fluids;
    }
}
