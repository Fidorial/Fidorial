package fr.euphyllia.fidorial.server.world.chunk;

import fr.euphyllia.fidorial.server.world.light.BlockLightProperties;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.world.biome.BiomeDefinition;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;
import java.util.function.Predicate;

public final class ChunkSection {

    public static final int BLOCK_COUNT = 4096; // 16^3
    public static final int BIOME_COUNT = 4096;    // 16^3, one biome per block since 26.4

    private final int sectionY; // indice de section (ex. -4 pour y=-64)
    private final PalettedContainer<BlockState> blocks;
    private final PalettedContainer<TypedKey<BiomeDefinition>> biomes;
    private int nonAirCount;
    private int fluidCount;
    /**
     * The last answer of {@link #mayContain}. A plain field holding an immutable record: a thread
     * reads either a whole answer or none, and a missed one is only worked out again.
     */
    private @Nullable PaletteCheck paletteCheck;

    public ChunkSection(final int sectionY, final BlockState fillBlock, final TypedKey<BiomeDefinition> fillBiome) {
        this.sectionY = sectionY;
        this.blocks = new PalettedContainer<>(BLOCK_COUNT, 4, fillBlock);
        this.biomes = new PalettedContainer<>(BIOME_COUNT, 1, fillBiome);
        this.nonAirCount = fillBlock.isAir() ? 0 : BLOCK_COUNT;
        this.fluidCount = fillBlock.isFluid() ? BLOCK_COUNT : 0;
    }

    public ChunkSection(final int sectionY, final PalettedContainer<BlockState> blocks,
                        final PalettedContainer<TypedKey<BiomeDefinition>> biomes) {
        this.sectionY = sectionY;
        this.blocks = blocks;
        this.biomes = biomes;
        recomputeCounts();
    }

    static int blockIndex(final int x, final int y, final int z) {
        return (y << 8) | (z << 4) | x;
    }

    public BitSet matchingBlocks(final Predicate<BlockState> test) {
        return blocks.matching(test);
    }

    public int sectionY() {
        return sectionY;
    }

    public PalettedContainer<BlockState> blocks() {
        return blocks;
    }

    public PalettedContainer<TypedKey<BiomeDefinition>> biomes() {
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

    public BlockState setBlock(final int x, final int y, final int z, final BlockState state) {
        final BlockState previous = blocks.getAndSet(blockIndex(x, y, z), state);

        final boolean wasAir = previous.isAir();
        final boolean isAir = state.isAir();

        if (wasAir && !isAir) nonAirCount++;
        else if (!wasAir && isAir) nonAirCount--;

        final boolean wasFluid = previous.isFluid();
        final boolean isFluid = state.isFluid();

        if (!wasFluid && isFluid) fluidCount++;
        else if (wasFluid && !isFluid) fluidCount--;

        return previous;
    }

    public BlockState getBlock(final int x, final int y, final int z) {
        return blocks.get(blockIndex(x, y, z));
    }

    public void setBiome(final int x, final int y, final int z, final TypedKey<BiomeDefinition> biome) {
        biomes.set(blockIndex(x, y, z), biome);
    }

    public TypedKey<BiomeDefinition> getBiome(final int x, final int y, final int z) {
        return biomes.get(blockIndex(x, y, z));
    }

    public boolean containsEmissiveBlocks() {
        return blocks.contains(BlockLightProperties::hasEmission);
    }

    /**
     * Whether a block of this section may pass a test, worked out from the palette and remembered
     * until the palette grows or the test changes: a cheap check to skip a whole section before
     * looking at its blocks one by one.
     * <p>
     * It may answer {@code true} for a block that is gone from the section, as the palette keeps
     * the states it held, but never {@code false} for a block that is there.
     *
     * @param test    the test, run on each state of the palette; pass the same instance every time,
     *                so that the answer is remembered
     * @param version a number that changes whenever the test may answer differently for a state
     * @return {@code false} if no block of this section passes the test
     */
    public boolean mayContain(final Predicate<BlockState> test, final int version) {
        final int paletteSize = blocks.paletteSize();
        final PaletteCheck last = paletteCheck;
        if (last != null && last.test() == test && last.version() == version && last.paletteSize() == paletteSize) {
            return last.result();
        }
        // a palette that grows in the meantime is caught on the next call, by its size
        final boolean result = blocks.contains(test);
        paletteCheck = new PaletteCheck(test, version, paletteSize, result);
        return result;
    }

    public void recomputeCounts() {
        this.nonAirCount = blocks.count(state -> !state.isAir());
        this.fluidCount = blocks.count(BlockState::isFluid);
    }

    private record PaletteCheck(Predicate<BlockState> test, int version, int paletteSize, boolean result) {
    }
}
