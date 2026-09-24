/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.euphyllia.fidorial.server.spark;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.fidorial.entity.EntityType;
import fr.fidorial.gamerule.GameRuleDefinition;
import fr.fidorial.world.ChunkPos;
import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reports entity, block entity and chunk counts to the viewer's world statistics.
 */
public final class SparkWorldInfoProvider implements WorldInfoProvider {

    private final FidorialServer server;

    public SparkWorldInfoProvider(final FidorialServer server) {
        this.server = server;
    }

    @Override
    public CountsResult pollCounts() {
        int entities = 0;
        int blockEntities = 0;
        int chunks = 0;

        for (final ServerWorld world : this.server.worldManager().worlds()) {
            entities += world.entityManager().count();
            chunks += world.loadedChunkCount();

            final AtomicInteger worldBlockEntities = new AtomicInteger();
            world.forEachLoadedChunk(column -> worldBlockEntities.addAndGet(column.blockEntityCount()));
            blockEntities += worldBlockEntities.get();
        }

        return new CountsResult(this.server.playerCount(), entities, blockEntities, chunks);
    }

    @Override
    public ChunksResult<FidorialChunkInfo> pollChunks() {
        final ChunksResult<FidorialChunkInfo> result = new ChunksResult<>();

        for (final ServerWorld world : this.server.worldManager().worlds()) {
            final List<FidorialChunkInfo> chunks = new ArrayList<>(world.loadedChunkCount());
            world.forEachLoadedChunk(column -> chunks.add(new FidorialChunkInfo(world, column)));
            result.put(world.key().asString(), chunks);
        }

        return result;
    }

    @Override
    public GameRulesResult pollGameRules() {
        final GameRulesResult result = new GameRulesResult();

        for (final GameRuleDefinition rule : this.server.gameRules().definitions()) {
            final String name = rule.id();
            result.putDefault(name, rule.format(rule.defaultValue()));
            for (final ServerWorld world : this.server.worldManager().worlds()) {
                result.put(name, world.key().asString(), rule.format(world.gameRuleValues().get(rule)));
            }
        }

        return result;
    }

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
        return List.of();
    }

    @Override
    public boolean mustCallSync() {
        return false;
    }

    public static final class FidorialChunkInfo extends AbstractChunkInfo<EntityType> {

        private final CountMap<EntityType> entityCounts;

        FidorialChunkInfo(final ServerWorld world, final ChunkColumn column) {
            super(column.chunkX(), column.chunkZ());

            this.entityCounts = new CountMap.Simple<>(new HashMap<>());
            for (final AbstractEntity entity :
                    world.entityManager().inChunk(new ChunkPos(column.chunkX(), column.chunkZ()))) {
                this.entityCounts.increment(entity.type());
            }
        }

        @Override
        public CountMap<EntityType> getEntityCounts() {
            return this.entityCounts;
        }

        @Override
        public String entityTypeName(final EntityType type) {
            return type.key().asString();
        }
    }
}
