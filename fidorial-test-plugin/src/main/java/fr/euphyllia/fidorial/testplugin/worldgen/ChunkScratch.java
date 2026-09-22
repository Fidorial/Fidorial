package fr.euphyllia.fidorial.testplugin.worldgen;

import fr.euphyllia.fidorial.testplugin.worldgen.climate.ClimatePoint;
import fr.euphyllia.fidorial.testplugin.worldgen.shape.TerrainShape;
import fr.fidorial.world.generation.GeneratedChunk;
import net.kyori.adventure.key.Key;

import java.util.Arrays;

public final class ChunkScratch {

    public static final int CELL = 4;

    private static final int AREA = 256;
    private final int[] surfaceY = new int[AREA];
    private final int[] waterTop = new int[AREA];
    private final double[] columnHeight = new double[AREA];
    private final ClimatePoint[] climate = new ClimatePoint[16];
    private final TerrainShape[] cornerShapes = new TerrainShape[25];
    private int chunkX;
    private int chunkZ;
    private int minY;
    private int height;
    private int maxY;
    private int cellCountY;
    private short[] blocks = new short[0];
    private double[] density = new double[0];
    private Key[] biomeCells = new Key[0];

    public void reset(final int newChunkX, final int newChunkZ, final int newMinY, final int newHeight) {
        this.chunkX = newChunkX;
        this.chunkZ = newChunkZ;
        this.minY = newMinY;
        this.height = newHeight;
        this.maxY = newMinY + newHeight - 1;
        this.cellCountY = newHeight / CELL;

        final int volume = AREA * newHeight;
        if (blocks.length != volume) {
            blocks = new short[volume];
        }
        Arrays.fill(blocks, Blk.AIR);

        final int corners = 5 * 5 * (cellCountY + 1);
        if (density.length != corners) {
            density = new double[corners];
        }

        final int biomeSlots = 16 * cellCountY;
        if (biomeCells.length != biomeSlots) {
            biomeCells = new Key[biomeSlots];
        }
        Arrays.fill(biomeCells, null);

        Arrays.fill(surfaceY, newMinY - 1);
        Arrays.fill(waterTop, Integer.MIN_VALUE);
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public int height() {
        return height;
    }

    public int cellCountY() {
        return cellCountY;
    }

    public int originX() {
        return chunkX << 4;
    }

    public int originZ() {
        return chunkZ << 4;
    }

    private int index(final int x, final int y, final int z) {
        return (y - minY) * AREA + (z << 4) + x;
    }

    public boolean inBounds(final int x, final int y, final int z) {
        return x >= 0 && x < 16 && z >= 0 && z < 16 && y >= minY && y <= maxY;
    }

    public short get(final int x, final int y, final int z) {
        if (!inBounds(x, y, z)) {
            return Blk.AIR;
        }
        return blocks[index(x, y, z)];
    }

    public void set(final int x, final int y, final int z, final short block) {
        if (!inBounds(x, y, z)) {
            return;
        }
        blocks[index(x, y, z)] = block;
    }

    public boolean setIfReplaceable(final int x, final int y, final int z, final short block) {
        if (!inBounds(x, y, z)) {
            return false;
        }
        final int i = index(x, y, z);
        if (!Blk.isReplaceableByFeature(blocks[i])) {
            return false;
        }
        blocks[i] = block;
        return true;
    }

    public int surfaceY(final int x, final int z) {
        return surfaceY[(z << 4) + x];
    }

    public void setSurfaceY(final int x, final int z, final int y) {
        surfaceY[(z << 4) + x] = y;
    }

    public int waterTop(final int x, final int z) {
        return waterTop[(z << 4) + x];
    }

    public void setWaterTop(final int x, final int z, final int y) {
        waterTop[(z << 4) + x] = y;
    }

    public double columnHeight(final int x, final int z) {
        return columnHeight[(z << 4) + x];
    }

    public void setColumnHeight(final int x, final int z, final double value) {
        columnHeight[(z << 4) + x] = value;
    }

    public ClimatePoint climate(final int cellX, final int cellZ) {
        return climate[(cellZ << 2) + cellX];
    }

    public void setClimate(final int cellX, final int cellZ, final ClimatePoint point) {
        climate[(cellZ << 2) + cellX] = point;
    }

    public ClimatePoint climateAt(final int x, final int z) {
        return climate[((z >> 2) << 2) + (x >> 2)];
    }

    public TerrainShape cornerShape(final int cornerX, final int cornerZ) {
        return cornerShapes[cornerZ * 5 + cornerX];
    }

    public void setCornerShape(final int cornerX, final int cornerZ, final TerrainShape shape) {
        cornerShapes[cornerZ * 5 + cornerX] = shape;
    }

    public void setDensity(final int cornerX, final int cornerY, final int cornerZ, final double value) {
        density[(cornerY * 5 + cornerZ) * 5 + cornerX] = value;
    }

    public double densityCorner(final int cornerX, final int cornerY, final int cornerZ) {
        return density[(cornerY * 5 + cornerZ) * 5 + cornerX];
    }

    public void setBiomeCell(final int cellX, final int cellY, final int cellZ, final Key biome) {
        biomeCells[(cellY * 4 + cellZ) * 4 + cellX] = biome;
    }

    public Key biomeCell(final int cellX, final int cellY, final int cellZ) {
        return biomeCells[(cellY * 4 + cellZ) * 4 + cellX];
    }

    public Key surfaceBiome(final int x, final int z) {
        final int cellY = Math.clamp((surfaceY(x, z) - minY) / CELL, 0, cellCountY - 1);
        final Key biome = biomeCell(x >> 2, cellY, z >> 2);
        if (biome != null) {
            return biome;
        }
        return biomeCell(x >> 2, Math.clamp((63 - minY) / CELL, 0, cellCountY - 1), z >> 2);
    }

    public void flush(final GeneratedChunk chunk) {
        for (int y = minY; y <= maxY; y++) {
            final int base = (y - minY) * AREA;
            for (int z = 0; z < 16; z++) {
                final int row = base + (z << 4);
                for (int x = 0; x < 16; x++) {
                    final short block = blocks[row + x];
                    if (block != Blk.AIR) {
                        chunk.setBlock(x, y, z, Blk.key(block));
                    }
                }
            }
        }

        for (int cellY = 0; cellY < cellCountY; cellY++) {
            final int worldY = minY + cellY * CELL;
            for (int cellZ = 0; cellZ < 4; cellZ++) {
                for (int cellX = 0; cellX < 4; cellX++) {
                    final Key biome = biomeCell(cellX, cellY, cellZ);
                    for (int dy = 0; dy < CELL; dy++) {
                        for (int dz = 0; dz < CELL; dz++) {
                            for (int dx = 0; dx < CELL; dx++) {
                                chunk.setBiome((cellX << 2) | dx, worldY + dy, (cellZ << 2) | dz, biome);
                            }
                        }
                    }
                }
            }
        }
    }
}
