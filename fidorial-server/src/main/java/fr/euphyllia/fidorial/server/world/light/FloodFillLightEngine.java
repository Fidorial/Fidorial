package fr.euphyllia.fidorial.server.world.light;

import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.light.LightType;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Arrays;

public class FloodFillLightEngine implements LightEngine {

    private static final int MAX_LEVEL = 15;

    // -X, +X, -Z, +Z, -Y, +Y.
    private static final int[] DX = {-1, 1, 0, 0, 0, 0};
    private static final int[] DZ = {0, 0, -1, 1, 0, 0};
    private static final int[] DY = {0, 0, 0, 0, -1, 1};

    private final int minY;
    private final int maxY;

    private final LongIntQueue scratchDecrease = new LongIntQueue();
    private final LongIntQueue scratchIncrease = new LongIntQueue();
    private final LongQueue scratchLongQueue = new LongQueue();

    public FloodFillLightEngine(final int minY, final int height) {
        this.minY = minY;
        this.maxY = minY + height;
    }

    private long packPos(final int x, final int y, final int z) {
        final long yOff = (y - minY) & 0xFFFL;
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | yOff;
    }

    private int unpackX(final long v) {
        final int x = (int) ((v >> 38) & 0x3FFFFFFL);
        return (x << 6) >> 6;
    }

    private int unpackZ(final long v) {
        final int z = (int) ((v >> 12) & 0x3FFFFFFL);
        return (z << 6) >> 6;
    }

    private int unpackY(final long v) {
        return (int) (v & 0xFFFL) + minY;
    }

    @Override
    public LongSet checkBlock(final int x, final int y, final int z, final LightAccess access) {
        final LongSet dirtyChunks = new LongOpenHashSet();

        checkBlockForType(x, y, z, access, dirtyChunks);
        checkSkyColumn(x, y, z, access, dirtyChunks);

        return dirtyChunks;
    }

    private void checkSkyColumn(final int x, final int y, final int z, final LightAccess access, final LongSet dirtyChunks) {
        if (!access.hasSkylight()) {
            return;
        }
        final ChunkLightData data = access.lightAt(x >> 4, z >> 4);
        if (data == null) {
            return;
        }

        final int oldTop = data.topOpaqueY(x & 15, z & 15);
        final BlockState state = access.blockAt(x, y, z);
        final int opacity = BlockLightProperties.opacity(state);
        final long chunkKey = ChunkPos.chunkKey(x >> 4, z >> 4);

        if (y > oldTop) {
            if (opacity == 0) {
                return;
            }
            raiseBoundary(access, data, x, y, z, oldTop, opacity, chunkKey, dirtyChunks);
            return;
        }

        if (y == oldTop) {
            if (opacity > 0) {
                checkBlockForType(LightType.SKY, x, y, z, data, access, dirtyChunks);
                return;
            }
            lowerBoundary(access, data, x, y, z, chunkKey, dirtyChunks);
            return;
        }

        checkBlockForType(LightType.SKY, x, y, z, data, access, dirtyChunks);
    }

    private void checkBlockForType(final int x, final int y, final int z, final LightAccess access, final LongSet dirtyChunks) {
        final ChunkLightData data = access.lightAt(x >> 4, z >> 4);
        if (data == null) {
            return;
        }
        checkBlockForType(LightType.BLOCK, x, y, z, data, access, dirtyChunks);
    }

    private void checkBlockForType(final LightType type, final int x, final int y, final int z, final ChunkLightData data, final LightAccess access, final LongSet dirtyChunks) {
        final LongIntQueue decrease = scratchDecrease;
        final LongIntQueue increase = scratchIncrease;
        decrease.reset();
        increase.reset();

        final int oldLevel = data.get(type, x, y, z);
        final long chunkKey = ChunkPos.chunkKey(x >> 4, z >> 4);
        final BlockState centerState = access.blockAt(x, y, z);
        final int newSourceLevel = sourceLevel(type, x, y, z, centerState, access);

        // a source at least as bright as the old level still lights everything the cell lit, so nothing needs darkening
        if (oldLevel > newSourceLevel) {
            data.set(type, x, y, z, 0);
            dirtyChunks.add(chunkKey);
            decrease.push(x, y, z, oldLevel);
            propagateDecrease(access, type, decrease, increase, dirtyChunks);
        }

        if (newSourceLevel > 0) {
            if (data.get(type, x, y, z) != newSourceLevel) {
                data.set(type, x, y, z, newSourceLevel);
                dirtyChunks.add(chunkKey);
            }
            increase.push(x, y, z, newSourceLevel);
        }

        for (int dir = 0; dir < 6; dir++) {
            final int nx = x + DX[dir];
            final int ny = y + DY[dir];
            final int nz = z + DZ[dir];
            if (ny < minY || ny >= maxY) {
                continue;
            }
            final ChunkLightData nd = access.lightAt(nx >> 4, nz >> 4);
            if (nd == null) {
                continue;
            }
            final int lvl = nd.get(type, nx, ny, nz);
            if (lvl > 1) {
                increase.push(nx, ny, nz, lvl);
            }
        }

        propagateIncrease(access, type, increase, dirtyChunks);
    }

    @Override
    public LongSet checkChunkEdge(final int chunkX, final int chunkZ, final int neighborChunkX, final int neighborChunkZ, final LightAccess access) {
        final LongSet dirtyChunks = new LongOpenHashSet();
        final boolean sky = access.hasSkylight();

        if (!access.isLightPopulated(chunkX, chunkZ) || !access.isLightPopulated(neighborChunkX, neighborChunkZ)) {
            return dirtyChunks;
        }

        final ChunkLightData near = access.lightAt(chunkX, chunkZ);
        final ChunkLightData far = access.lightAt(neighborChunkX, neighborChunkZ);
        if (near == null || far == null) {
            return dirtyChunks;
        }

        // the seam runs along Z when the chunks sit side by side on X, and along X otherwise
        final boolean seamAlongZ = neighborChunkX != chunkX;
        final int nearSide = (seamAlongZ ? neighborChunkX - chunkX : neighborChunkZ - chunkZ) > 0 ? 15 : 0;
        final int farSide = 15 - nearSide;

        final int highestSection = Math.max(access.topNonEmptySectionY(chunkX, chunkZ), access.topNonEmptySectionY(neighborChunkX, neighborChunkZ));
        final int blockTop = Math.min(maxY - 1, (highestSection << 4) + 15);

        for (int along = 0; along < 16; along++) {
            final int nearLx = seamAlongZ ? nearSide : along;
            final int nearLz = seamAlongZ ? along : nearSide;
            final int farLx = seamAlongZ ? farSide : along;
            final int farLz = seamAlongZ ? along : farSide;

            final int nearX = (chunkX << 4) | nearLx;
            final int nearZ = (chunkZ << 4) | nearLz;
            final int farX = (neighborChunkX << 4) | farLx;
            final int farZ = (neighborChunkZ << 4) | farLz;

            final int nearSkyTop = near.topOpaqueY(nearLx, nearLz);
            final int farSkyTop = far.topOpaqueY(farLx, farLz);
            boolean blockLit = false;

            for (int y = minY; y <= blockTop; y++) {
                // with no block light in this section on either side, nothing can cross the seam here
                if (((y - minY) & 15) == 0) {
                    final int section = (y - minY) >> 4;
                    blockLit = near.sectionArray(LightType.BLOCK, section) != null || far.sectionArray(LightType.BLOCK, section) != null;
                }
                if (blockLit) {
                    recheckIfStale(LightType.BLOCK, nearX, y, nearZ, near, access, dirtyChunks);
                    recheckIfStale(LightType.BLOCK, farX, y, farZ, far, access, dirtyChunks);
                }
                if (sky && y <= nearSkyTop) {
                    recheckIfStale(LightType.SKY, nearX, y, nearZ, near, access, dirtyChunks);
                }
                if (sky && y <= farSkyTop) {
                    recheckIfStale(LightType.SKY, farX, y, farZ, far, access, dirtyChunks);
                }
            }
        }

        return dirtyChunks;
    }

    private void recheckIfStale(final LightType type, final int x, final int y, final int z, final ChunkLightData data, final LightAccess access, final LongSet dirtyChunks) {
        if (isStale(type, x, y, z, data.get(type, x, y, z), access)) {
            checkBlockForType(type, x, y, z, data, access, dirtyChunks);
        }
    }

    private int sourceLevel(final LightType type, final int x, final int y, final int z, final BlockState centerState, final LightAccess access) {
        if (type == LightType.BLOCK) {
            return BlockLightProperties.emission(centerState);
        }

        if (BlockLightProperties.occludes(centerState)) {
            return 0;
        }

        final ChunkLightData data = access.lightAt(x >> 4, z >> 4);
        final int top = data == null ? minY - 1 : data.topOpaqueY(x & 15, z & 15);
        return y > top ? MAX_LEVEL : 0;
    }

    private void propagateIncrease(final LightAccess access, final LightType type, final LongIntQueue queue, final LongSet dirtyChunks) {
        long sourceChunkKey = Long.MIN_VALUE;
        ChunkLightData sourceData = null;
        long targetChunkKey = Long.MIN_VALUE;
        ChunkLightData targetData = null;
        LightAccess.BlockColumnAccess targetCol = null;

        while (!queue.isEmpty()) {
            final int x = queue.pollX();
            final int y = queue.pollY();
            final int z = queue.pollZ();
            final int level = queue.pollLevel();

            final long sk = ChunkPos.chunkKey(x >> 4, z >> 4);
            if (sk != sourceChunkKey) {
                sourceChunkKey = sk;
                sourceData = access.lightAt(x >> 4, z >> 4);
            }
            final ChunkLightData self = sourceData;
            if (self == null || self.get(type, x, y, z) != level) {
                continue;
            }

            for (int dir = 0; dir < 6; dir++) {
                final int nx = x + DX[dir];
                final int ny = y + DY[dir];
                final int nz = z + DZ[dir];
                if (ny < minY || ny >= maxY) {
                    continue;
                }

                final long tk = ChunkPos.chunkKey(nx >> 4, nz >> 4);
                if (tk != targetChunkKey) {
                    targetChunkKey = tk;
                    targetData = access.lightAt(nx >> 4, nz >> 4);
                    targetCol = access.columnAt(nx >> 4, nz >> 4);
                }
                final ChunkLightData target = targetData;
                if (target == null) {
                    continue;
                }

                final int current = target.get(type, nx, ny, nz);
                if (current >= level - 1) {
                    continue;
                }

                final BlockState state = targetCol != null
                        ? targetCol.blockAt(nx & 15, ny, nz & 15)
                        : access.blockAt(nx, ny, nz);
                if (BlockLightProperties.occludes(state)) {
                    continue;
                }

                final int targetLevel = level - lightDecrement(state);
                if (targetLevel > current) {
                    target.set(type, nx, ny, nz, targetLevel);
                    dirtyChunks.add(tk);
                    if (targetLevel > 1) {
                        queue.push(nx, ny, nz, targetLevel);
                    }
                }
            }
        }
    }

    private void propagateDecrease(final LightAccess access, final LightType type, final LongIntQueue decreaseQueue, final LongIntQueue increaseQueue, final LongSet dirtyChunks) {
        long targetChunkKey = Long.MIN_VALUE;
        ChunkLightData targetData = null;
        LightAccess.BlockColumnAccess targetCol = null;

        while (!decreaseQueue.isEmpty()) {
            final int x = decreaseQueue.pollX();
            final int y = decreaseQueue.pollY();
            final int z = decreaseQueue.pollZ();
            final int oldLevel = decreaseQueue.pollLevel();

            for (int dir = 0; dir < 6; dir++) {
                final int nx = x + DX[dir];
                final int ny = y + DY[dir];
                final int nz = z + DZ[dir];
                if (ny < minY || ny >= maxY) {
                    continue;
                }

                final long tk = ChunkPos.chunkKey(nx >> 4, nz >> 4);
                if (tk != targetChunkKey) {
                    targetChunkKey = tk;
                    targetData = access.lightAt(nx >> 4, nz >> 4);
                    targetCol = access.columnAt(nx >> 4, nz >> 4);
                }
                final ChunkLightData target = targetData;
                if (target == null) {
                    continue;
                }

                final int neighbourLevel = target.get(type, nx, ny, nz);
                if (neighbourLevel == 0) {
                    continue;
                }

                final BlockState state = targetCol != null
                        ? targetCol.blockAt(nx & 15, ny, nz & 15)
                        : access.blockAt(nx, ny, nz);
                final int decrement = lightDecrement(state);
                final int targetLevel = oldLevel - decrement;

                if (neighbourLevel <= targetLevel) {
                    target.set(type, nx, ny, nz, 0);
                    dirtyChunks.add(tk);
                    // an emitter caught in the darkening keeps its own light, and only darkens around it what it did not light itself
                    final int emission = type == LightType.BLOCK ? BlockLightProperties.emission(state) : 0;
                    if (emission < neighbourLevel) {
                        decreaseQueue.push(nx, ny, nz, neighbourLevel);
                    }
                    if (emission > 0) {
                        target.set(type, nx, ny, nz, emission);
                        increaseQueue.push(nx, ny, nz, emission);
                    }
                } else {
                    increaseQueue.push(nx, ny, nz, neighbourLevel);
                }
            }
        }
    }

    @Override
    public void relight(final LongSet chunks, final LightAccess access) {
        if (chunks.isEmpty()) {
            return;
        }
        final LongIterator it = chunks.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            final ChunkLightData data = access.lightAt((int) (key >> 32), (int) key);
            if (data != null) {
                data.clear();
            }
        }

        computeSky(chunks, access);
        computeBlock(chunks, access);
    }

    private void computeSky(final LongSet chunks, final LightAccess access) {
        if (!access.hasSkylight()) {
            return;
        }
        final LongQueue queue = scratchLongQueue;
        queue.reset();

        final LongIterator it = chunks.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            final int chunkX = (int) (key >> 32);
            final int chunkZ = (int) key;
            final ChunkLightData data = access.lightAt(chunkX, chunkZ);
            final LightAccess.BlockColumnAccess col = access.columnAt(chunkX, chunkZ);
            if (data == null || col == null) continue;

            final int topSection = access.topNonEmptySectionY(chunkX, chunkZ);
            final int scanStart = topSection < (minY >> 4)
                    ? maxY - 1
                    : Math.min(maxY - 1, ((topSection + 1) << 4) + 15);
            data.setSkyFullFromY(scanStart);

            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    int topOpaque = minY - 1;

                    for (int y = scanStart; y >= minY; y--) {
                        if (BlockLightProperties.opacity(col.blockAt(lx, y, lz)) > 0) {
                            topOpaque = y;
                            break;
                        }
                    }
                    data.setTopOpaqueY(lx, lz, topOpaque);
                }
            }
        }

        // seeded once every top is known, so neighboring columns across the batch can be compared
        final LongIterator seedIt = chunks.iterator();
        while (seedIt.hasNext()) {
            final long key = seedIt.nextLong();
            final int chunkX = (int) (key >> 32);
            final int chunkZ = (int) key;
            final ChunkLightData data = access.lightAt(chunkX, chunkZ);
            if (data == null) continue;

            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    final int worldX = (chunkX << 4) | lx;
                    final int worldZ = (chunkZ << 4) | lz;
                    final int top = data.topOpaqueY(lx, lz);
                    // full-strength cells only need to spread where a neighboring column is covered at that height
                    final int highest = highestNeighbourTop(chunks, access, data, chunkX, chunkZ, worldX, worldZ);
                    // the cell right above the top always spreads down into it; an open column has no such cell
                    final int start = top >= minY ? Math.max(highest, top + 1) : highest;
                    for (int y = start; y > top; y--) {
                        queue.add(packPos(worldX, y, worldZ), 5);
                    }
                }
            }
        }

        seedBorders(chunks, access, LightType.SKY, queue);
        propagate(chunks, access, LightType.SKY, queue);
    }

    private int highestNeighbourTop(final LightAccess access, final int x, final int z) {
        int highest = minY - 1;
        for (int dir = 0; dir < 4; dir++) {
            final int nx = x + DX[dir];
            final int nz = z + DZ[dir];
            final ChunkLightData neighbour = access.lightAt(nx >> 4, nz >> 4);
            if (neighbour != null) {
                highest = Math.max(highest, neighbour.topOpaqueY(nx & 15, nz & 15));
            }
        }
        return highest;
    }

    private int highestNeighbourTop(final LongSet chunks, final LightAccess access, final ChunkLightData data, final int chunkX, final int chunkZ, final int worldX, final int worldZ) {
        int highest = minY - 1;
        for (int dir = 0; dir < 4; dir++) {
            final int nx = worldX + DX[dir];
            final int nz = worldZ + DZ[dir];
            final int ncx = nx >> 4;
            final int ncz = nz >> 4;
            final ChunkLightData neighbour;
            if (ncx == chunkX && ncz == chunkZ) {
                neighbour = data;
            } else if (chunks.contains(ChunkPos.chunkKey(ncx, ncz))) {
                neighbour = access.lightAt(ncx, ncz);
            } else {
                // propagation never writes outside the batch
                continue;
            }
            if (neighbour != null) {
                highest = Math.max(highest, neighbour.topOpaqueY(nx & 15, nz & 15));
            }
        }
        return highest;
    }

    private void computeBlock(final LongSet chunks, final LightAccess access) {
        final LongQueue queue = scratchLongQueue;
        queue.reset();

        final int minSection = minY >> 4;
        final LongIterator it = chunks.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            final int chunkX = (int) (key >> 32);
            final int chunkZ = (int) key;
            final ChunkLightData data = access.lightAt(chunkX, chunkZ);
            final LightAccess.BlockColumnAccess col = access.columnAt(chunkX, chunkZ);
            if (data == null || col == null) continue;

            final int baseX = chunkX << 4;
            final int baseZ = chunkZ << 4;
            final int topSection = access.topNonEmptySectionY(chunkX, chunkZ);

            for (int sectionY = minSection; sectionY <= topSection; sectionY++) {
                if (!access.sectionHasEmissiveBlocks(chunkX, sectionY, chunkZ)) {
                    continue;
                }

                final int sectionBaseY = sectionY << 4;
                final int yStart = Math.max(minY, sectionBaseY);
                final int yEnd = Math.min(maxY - 1, sectionBaseY + 15);

                for (int lx = 0; lx < 16; lx++) {
                    for (int lz = 0; lz < 16; lz++) {
                        for (int y = yStart; y <= yEnd; y++) {
                            final BlockState block = col.blockAt(lx, y, lz);
                            final int emission = BlockLightProperties.emission(block);
                            if (emission > 0) {
                                final int worldX = baseX + lx;
                                final int worldZ = baseZ + lz;
                                data.set(LightType.BLOCK, worldX, y, worldZ, emission);
                                queue.add(packPos(worldX, y, worldZ), -1);
                            }
                        }
                    }
                }
            }
        }

        seedBorders(chunks, access, LightType.BLOCK, queue);
        propagate(chunks, access, LightType.BLOCK, queue);
    }

    private void seedBorders(final LongSet chunks, final LightAccess access, final LightType type, final LongQueue queue) {
        final LongIterator it = chunks.iterator();
        while (it.hasNext()) {
            final long key = it.nextLong();
            final int chunkX = (int) (key >> 32);
            final int chunkZ = (int) key;
            final ChunkLightData self = access.lightAt(chunkX, chunkZ);
            if (self == null) {
                continue;
            }

            addBorderColumn(chunks, access, type, queue, self, chunkX - 1, chunkZ, 15, -1);
            addBorderColumn(chunks, access, type, queue, self, chunkX + 1, chunkZ, 0, -1);
            addBorderColumn(chunks, access, type, queue, self, chunkX, chunkZ - 1, -1, 15);
            addBorderColumn(chunks, access, type, queue, self, chunkX, chunkZ + 1, -1, 0);
        }
    }

    private void addBorderColumn(final LongSet chunks, final LightAccess access, final LightType type, final LongQueue queue, final ChunkLightData self, final int neighborChunkX, final int neighborChunkZ, final int fixedLocalX, final int fixedLocalZ) {
        if (chunks.contains(ChunkPos.chunkKey(neighborChunkX, neighborChunkZ))) {
            return;
        }

        final ChunkLightData data = access.lightAt(neighborChunkX, neighborChunkZ);
        if (data == null) {
            return;
        }

        final int baseX = neighborChunkX << 4;
        final int baseZ = neighborChunkZ << 4;
        final int xStart = Math.max(fixedLocalX, 0);
        final int xEnd = fixedLocalX >= 0 ? fixedLocalX : 15;
        final int zStart = Math.max(fixedLocalZ, 0);
        final int zEnd = fixedLocalZ >= 0 ? fixedLocalZ : 15;
        for (int lx = xStart; lx <= xEnd; lx++) {
            for (int lz = zStart; lz <= zEnd; lz++) {
                final int worldX = baseX + lx;
                final int worldZ = baseZ + lz;
                // above the facing column's top every cell is already full strength, so there is nothing to spread into
                final int yEnd = type == LightType.SKY
                        ? self.topOpaqueY(fixedLocalX >= 0 ? 15 - fixedLocalX : lx, fixedLocalZ >= 0 ? 15 - fixedLocalZ : lz)
                        : maxY - 1;
                for (int y = minY; y <= yEnd; y++) {
                    final int level = data.get(type, worldX, y, worldZ);
                    if (level > 1) {
                        queue.add(packPos(worldX, y, worldZ), -1);
                    }
                }
            }
        }
    }

    private void propagate(final LongSet chunks, final LightAccess access, final LightType type, final LongQueue queue) {
        long sourceKey = Long.MIN_VALUE;
        ChunkLightData sourceData = null;
        long targetKey = Long.MIN_VALUE;
        ChunkLightData targetData = null;
        LightAccess.BlockColumnAccess targetCol = null;

        while (!queue.isEmpty()) {
            final long packed = queue.poll();
            final int skipDir = queue.lastSkipDir();
            final int x = unpackX(packed);
            final int y = unpackY(packed);
            final int z = unpackZ(packed);

            final long sk = ChunkPos.chunkKey(x >> 4, z >> 4);
            if (sk != sourceKey) {
                sourceKey = sk;
                sourceData = access.lightAt(x >> 4, z >> 4);
            }
            final ChunkLightData source = sourceData;
            if (source == null) continue;
            final int level = source.get(type, x, y, z);
            if (level <= 1) continue;

            for (int dir = 0; dir < 6; dir++) {
                if (dir == skipDir) continue;
                final int nx = x + DX[dir];
                final int ny = y + DY[dir];
                final int nz = z + DZ[dir];
                if (ny < minY || ny >= maxY) continue;
                final long nChunkKey = ChunkPos.chunkKey(nx >> 4, nz >> 4);
                if (!chunks.contains(nChunkKey)) continue;

                if (nChunkKey != targetKey) {
                    targetKey = nChunkKey;
                    targetData = access.lightAt(nx >> 4, nz >> 4);
                    targetCol = access.columnAt(nx >> 4, nz >> 4);
                }
                final ChunkLightData target = targetData;
                if (target == null) continue;

                final int current = target.get(type, nx, ny, nz);
                if (current >= level - 1) continue;

                final BlockState block = targetCol != null
                        ? targetCol.blockAt(nx & 15, ny, nz & 15)
                        : access.blockAt(nx, ny, nz);
                if (BlockLightProperties.occludes(block)) continue;
                final int candidate = level - lightDecrement(block);
                if (candidate > current) {
                    target.set(type, nx, ny, nz, candidate);
                    if (candidate > 1) {
                        queue.add(packPos(nx, ny, nz), dir ^ 1);
                    }
                }
            }
        }
    }

    private int lightDecrement(final BlockState state) {
        return Math.max(1, BlockLightProperties.opacity(state));
    }

    private boolean isStale(final LightType type, final int x, final int y, final int z, final int stored, final LightAccess access) {
        final BlockState state = access.blockAt(x, y, z);
        int best = sourceLevel(type, x, y, z, state, access);
        if (best > stored) {
            return true;
        }

        if (BlockLightProperties.occludes(state)) {
            return best != stored;
        }

        final int stepCost = lightDecrement(state);
        long lastChunk = Long.MIN_VALUE;
        ChunkLightData lastData = null;

        for (int dir = 0; dir < 6; dir++) {
            final int ny = y + DY[dir];
            if (ny < minY || ny >= maxY) {
                continue;
            }
            final int nx = x + DX[dir];
            final int nz = z + DZ[dir];

            final long chunk = ChunkPos.chunkKey(nx >> 4, nz >> 4);
            if (chunk != lastChunk) {
                lastChunk = chunk;
                lastData = access.lightAt(nx >> 4, nz >> 4);
            }
            final ChunkLightData adjacent = lastData;
            if (adjacent == null) {
                continue;
            }

            final int incoming = adjacent.get(type, nx, ny, nz);
            if (incoming <= best + 1) {
                continue;
            }

            final int through = incoming - stepCost;
            if (through > best) {
                if (through > stored) {
                    return true;
                }
                best = through;
            }
        }

        return best != stored;
    }

    private void raiseBoundary(final LightAccess access, final ChunkLightData data, final int x, final int y, final int z, final int oldTop, final int opacity, final long chunkKey, final LongSet dirtyChunks) {
        final LongIntQueue decrease = scratchDecrease;
        final LongIntQueue increase = scratchIncrease;
        decrease.reset();
        increase.reset();

        data.setTopOpaqueY(x & 15, z & 15, y);
        dirtyChunks.add(chunkKey);

        for (int cy = oldTop + 1; cy <= y; cy++) {
            data.set(LightType.SKY, x, cy, z, 0);
            decrease.push(x, cy, z, MAX_LEVEL);
        }
        propagateDecrease(access, LightType.SKY, decrease, increase, dirtyChunks);

        if (y + 1 < maxY) {
            increase.push(x, y + 1, z, MAX_LEVEL);
        } else {
            final int level = MAX_LEVEL - opacity;
            if (level > 0) {
                data.set(LightType.SKY, x, y, z, level);
                if (level > 1) {
                    increase.push(x, y, z, level);
                }
            }
        }

        propagateIncrease(access, LightType.SKY, increase, dirtyChunks);
    }

    private void lowerBoundary(final LightAccess access, final ChunkLightData data, final int x, final int y, final int z, final long chunkKey, final LongSet dirtyChunks) {
        int newTop = y - 1;
        while (newTop >= minY && BlockLightProperties.opacity(access.blockAt(x, newTop, z)) == 0) {
            newTop--;
        }

        data.setTopOpaqueY(x & 15, z & 15, newTop);
        dirtyChunks.add(chunkKey);

        final LongIntQueue increase = scratchIncrease;
        increase.reset();
        increase.push(x, newTop + 1, z, MAX_LEVEL);

        for (int cy = Math.min(y, highestNeighbourTop(access, x, z)); cy > newTop + 1; cy--) {
            increase.push(x, cy, z, MAX_LEVEL);
        }
        propagateIncrease(access, LightType.SKY, increase, dirtyChunks);
    }

    private static final class LongQueue {
        private long[] data = new long[1024];
        private byte[] dirs = new byte[1024];
        private int head;
        private int tail;
        private byte lastDir;

        void reset() {
            head = 0;
            tail = 0;
        }

        void add(final long value, final int skipDir) {
            if (tail == data.length) grow();
            data[tail] = value;
            dirs[tail] = (byte) skipDir;
            tail++;
        }

        long poll() {
            lastDir = dirs[head];
            return data[head++];
        }

        int lastSkipDir() {
            return lastDir;
        }

        boolean isEmpty() {
            return head == tail;
        }

        private void grow() {
            if (head > 0) {
                final int size = tail - head;
                System.arraycopy(data, head, data, 0, size);
                System.arraycopy(dirs, head, dirs, 0, size);
                head = 0;
                tail = size;
                if (tail < data.length) return;
            }
            data = Arrays.copyOf(data, data.length * 2);
            dirs = Arrays.copyOf(dirs, dirs.length * 2);
        }
    }
}
