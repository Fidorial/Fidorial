package fr.euphyllia.fidorial.server.entity;

import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.fidorial.world.ChunkPos;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class EntityManager {

    private final Map<Integer, AbstractEntity> byId = new ConcurrentHashMap<>();
    private final Map<UUID, AbstractEntity> byUuid = new ConcurrentHashMap<>();
    private final Map<Long, Set<AbstractEntity>> byChunk = new ConcurrentHashMap<>();
    private final Map<Long, Set<AbstractEntity>> bySection = new ConcurrentHashMap<>();

    private static long sectionKey(final ChunkPos pos) {
        return sectionKey(
                pos.x() >> ThreadedRegionRegionizer.SECTION_SHIFT, pos.z() >> ThreadedRegionRegionizer.SECTION_SHIFT);
    }

    private static long sectionKey(final int sectionX, final int sectionZ) {
        return ((long) sectionZ << 32) | (sectionX & 0xFFFFFFFFL);
    }

    @ApiStatus.Internal
    public void add(final AbstractEntity entity) {
        byId.put(entity.entityId(), entity);
        byUuid.put(entity.uuid(), entity);
        final ChunkPos chunk = entity.chunk();
        byChunk.computeIfAbsent(ChunkPos.chunkKey(chunk), _ -> ConcurrentHashMap.newKeySet()).add(entity);
        bySection
                .computeIfAbsent(sectionKey(chunk), _ -> ConcurrentHashMap.newKeySet())
                .add(entity);
    }

    @ApiStatus.Internal
    public void remove(final AbstractEntity entity) {
        byId.remove(entity.entityId());
        byUuid.remove(entity.uuid());
        final ChunkPos chunk = entity.chunk();
        byChunk.computeIfPresent(ChunkPos.chunkKey(chunk), (_, set) -> {
            set.remove(entity);
            return set.isEmpty() ? null : set;
        });
        bySection.computeIfPresent(sectionKey(chunk), (_, set) -> {
            set.remove(entity);
            return set.isEmpty() ? null : set;
        });
    }

    @ApiStatus.Internal
    public void moved(final AbstractEntity entity, final ChunkPos from, final ChunkPos to) {
        if (from.equals(to)) {
            return;
        }
        byChunk.computeIfPresent(ChunkPos.chunkKey(from), (_, set) -> {
            set.remove(entity);
            return set.isEmpty() ? null : set;
        });
        byChunk.computeIfAbsent(ChunkPos.chunkKey(to), _ -> ConcurrentHashMap.newKeySet()).add(entity);

        final long fromSection = sectionKey(from);
        final long toSection = sectionKey(to);
        if (fromSection != toSection) {
            bySection.computeIfPresent(fromSection, (_, set) -> {
                set.remove(entity);
                return set.isEmpty() ? null : set;
            });
            bySection
                    .computeIfAbsent(toSection, _ -> ConcurrentHashMap.newKeySet())
                    .add(entity);
        }
    }

    public AbstractEntity byId(final int entityId) {
        return byId.get(entityId);
    }

    public AbstractEntity byUuid(final UUID uuid) {
        return byUuid.get(uuid);
    }

    public Set<AbstractEntity> inChunk(final ChunkPos pos) {
        return byChunk.getOrDefault(ChunkPos.chunkKey(pos), Set.of());
    }

    public Set<AbstractEntity> inSection(final int sectionX, final int sectionZ) {
        return bySection.getOrDefault(sectionKey(sectionX, sectionZ), Set.of());
    }

    public Collection<AbstractEntity> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public @Nullable AbstractEntity findInChunkRange(final int chunkX, final int chunkZ, final int chunkRadius, final Predicate<AbstractEntity> filter) {
        for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
                final Set<AbstractEntity> entitySet = byChunk.get(ChunkPos.chunkKey(x, z));
                if (entitySet == null) {
                    continue;
                }
                for (final AbstractEntity entity : entitySet) {
                    if (filter.test(entity)) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }

    public void forEachInChunkRange(final int chunkX, final int chunkZ, final int chunkRadius, final Consumer<AbstractEntity> action) {
        for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
                final Set<AbstractEntity> entitySet = byChunk.get(ChunkPos.chunkKey(x, z));
                if (entitySet == null || entitySet.isEmpty()) {
                    continue;
                }
                for (final AbstractEntity entity : entitySet) {
                    action.accept(entity);
                }
            }
        }
    }

    public void forEachNear(final ChunkPos center, final double blockRadius, final Consumer<AbstractEntity> action) {
        final int chunkRadius = (int) Math.ceil(blockRadius / 16.0);
        forEachInChunkRange(center.x(), center.z(), chunkRadius, action);
    }

    public int count() {
        return byId.size();
    }
}
