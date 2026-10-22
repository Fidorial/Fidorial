package fr.euphyllia.fidorial.server.entity;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.combat.CombatEngine;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.player.PlayerList;
import fr.euphyllia.fidorial.server.entity.player.PlayerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.schedulers.WorkerModule;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.CombatService;
import fr.fidorial.entity.Entity;
import fr.fidorial.world.entity.EntitySpawnBridge;

/**
 * Entities: ids, spawning, tracking by players, ticking and combat.
 */
public final class EntityModule implements ServerModule {

    private final FidorialServer server;
    private final ThreadedRegionRegionizer regionizer;
    private final PlayerList online;
    private final EntityIdAllocator ids = new EntityIdAllocator();
    private final EntityTracker tracker;
    private final CombatEngine combat;

    public EntityModule(
            final FidorialServer server, final ServerConfiguration config,
            final WorkerModule scheduler, final PlayerModule players) {
        this.server = server;
        this.regionizer = scheduler.regionizer();
        this.online = players.connections();
        this.tracker = new EntityTracker(config.general().sendDistance());
        this.combat = new CombatEngine(server);

        online.addListener(new PlayerList.Listener() {
            @Override
            public void onDisconnect(final ClientConnection connection) {
                tracker.removeViewer(connection);
            }
        });
    }

    @Override
    public void registerServices(final ServiceRegistrar services) {
        services.provide(CombatService.class, combat);
    }

    @Override
    public void start() {
        regionizer.registerTickHandler(new EntityTickHandler(server.worlds().manager(), server));
    }

    /**
     * Adds an entity to its world and starts ticking and tracking it.
     *
     * @param entity the entity, whose world must be loaded
     */
    public void spawn(final AbstractEntity entity) {
        if (!(entity.world() instanceof final ServerWorld world)) {
            throw new IllegalArgumentException("Cannot summon an entity into a non-existent or unloaded world :" + entity);
        }

        world.addEntity(entity);

        if (entity instanceof AbstractMob) {
            regionizer.addTicket(world.dimension().id(), entity.chunk());
        }

        tracker.update(entity, online.players());
    }

    /**
     * Removes an entity from its world and stops ticking and tracking it.
     *
     * @param entity the entity
     */
    public void despawn(final AbstractEntity entity) {
        if (entity.world() instanceof final ServerWorld world) {
            world.removeEntity(entity);

            if (entity instanceof AbstractMob) {
                regionizer.removeTicket(world.dimension().id(), entity.chunk());
            }
        }

        entity.remove();

        tracker.untrack(entity);
    }

    /**
     * {@return the bridge keeping region tickets and tracking in sync with the entities that worlds
     * add or remove on their own (chunk loads and unloads)}
     */
    public EntitySpawnBridge spawnBridge() {
        return new EntitySpawnBridge() {
            @Override
            public void onEntityAppear(final Entity entity) {
                if (entity instanceof AbstractMob && entity.world() instanceof final ServerWorld world) {
                    regionizer.addTicket(world.dimension().id(), entity.chunk());
                }
                tracker.update(entity, online.players());
            }

            @Override
            public void onEntityDisappear(final Entity entity) {
                if (entity instanceof AbstractMob && entity.world() instanceof final ServerWorld world) {
                    regionizer.removeTicket(world.dimension().id(), entity.chunk());
                }
                tracker.untrack(entity);
            }
        };
    }

    public EntityIdAllocator ids() {
        return ids;
    }

    public EntityTracker tracker() {
        return tracker;
    }

    public CombatEngine combat() {
        return combat;
    }
}
