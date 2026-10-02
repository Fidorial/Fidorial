package fr.euphyllia.fidorial.server.entity;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.WorldManager;
import fr.fidorial.scheduler.RegionTickHandler;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityTickHandler implements RegionTickHandler {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EntityTickHandler.class);

    private final WorldManager worldManager;
    private final Map<Key, ServerWorld> worldsById = new ConcurrentHashMap<>();
    private final FidorialServer server;

    public EntityTickHandler(final WorldManager worldManager, final FidorialServer fidorialServer) {
        this.worldManager = worldManager;
        this.server = fidorialServer;
    }

    @Override
    public void tick(final Key worldId, final int sectionX, final int sectionZ, final long currentTick) {
        final ServerWorld world = worldById(worldId);
        if (world == null) {
            return;
        }
        final EntityTracker tracker = server.entityTracker();
        final List<ServerPlayer> players = server.players();

        for (final AbstractEntity entity : world.entityManager().inSection(sectionX, sectionZ)) {
            if (entity.isRemoved()) {
                continue;
            }
            try {
                entity.tick(currentTick);
                if (entity instanceof final AbstractMovingMob mob) {
                    server.entityDebug().tick(mob);
                }
            } catch (final Throwable t) {
                LOGGER.error("Error during tick of {}", entity, t);
            }
            if (!players.isEmpty() && EntityTracker.shouldUpdate(entity, currentTick)) {
                try {
                    tracker.update(entity, players);
                } catch (final Throwable t) {
                    LOGGER.error("Error while tracking {}", entity, t);
                }
            }
        }
    }

    private @Nullable ServerWorld worldById(final Key id) {
        final ServerWorld cached = worldsById.get(id);
        if (cached != null) {
            return cached;
        }
        for (final ServerWorld world : worldManager.worlds()) {
            if (world.dimension().id().equals(id)) {
                worldsById.put(id, world);
                return world;
            }
        }
        return null;
    }
}
