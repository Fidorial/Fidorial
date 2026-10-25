package fr.euphyllia.fidorial.server.entity;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.combat.CombatEngine;
import fr.euphyllia.fidorial.server.configuration.ServerConfiguration;
import fr.euphyllia.fidorial.server.entity.item.ServerItemEntity;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.player.PlayerList;
import fr.euphyllia.fidorial.server.entity.player.PlayerModule;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.lifecycle.ServerModule;
import fr.euphyllia.fidorial.server.lifecycle.ServiceRegistrar;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.schedulers.ThreadedRegionRegionizer;
import fr.euphyllia.fidorial.server.schedulers.WorkerModule;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.CombatService;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.world.ChunkPos;
import fr.fidorial.world.entity.EntitySpawnBridge;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Entities: ids, spawning, tracking by players, ticking and combat.
 */
public final class EntityModule implements ServerModule {

    private static final double STANDING_EYE_HEIGHT = 1.62;
    private static final double SNEAKING_EYE_HEIGHT = 1.27;
    private static final double ITEM_HALF_HEIGHT = 0.125;

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

    /**
     * Drops a stack on the ground the way a block spills what it held: around {@code (x, y, z)},
     * with a small random push.
     *
     * @return the item, already in its world when called from the region thread owning
     *         {@code (x, z)}, added by that thread shortly after otherwise
     */
    public ServerItemEntity dropNaturally(final ServerWorld world, final double x, final double y, final double z,
                                          final ItemStack stack) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double half = 0.25;
        return dropItem(world,
                x + random.nextDouble(-half, half),
                y + random.nextDouble(-half, half) - ITEM_HALF_HEIGHT,
                z + random.nextDouble(-half, half),
                stack,
                random.nextDouble(-0.1, 0.1), 0.2, random.nextDouble(-0.1, 0.1),
                ItemEntity.DEFAULT_PICKUP_DELAY,
                null);
    }

    /**
     * Throws a stack out of a player's hands, where they look: the drop key, an item thrown out of
     * a window, a stack thrown away from the creative inventory.
     */
    public ServerItemEntity dropFromPlayer(final ServerPlayer player, final ItemStack stack) {
        final Location at = player.location();
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double yaw = Math.toRadians(at.yaw());
        final double pitch = Math.toRadians(at.pitch());
        final double speed = 0.3;
        final double spread = random.nextDouble() * Math.PI * 2.0;
        final double wobble = 0.02 * random.nextDouble();
        return dropItem((ServerWorld) player.world(),
                at.x(), at.y() + eyeHeight(player) - 0.3, at.z(),
                stack,
                -Math.sin(yaw) * Math.cos(pitch) * speed + Math.cos(spread) * wobble,
                -Math.sin(pitch) * speed + 0.1 + (random.nextDouble() - random.nextDouble()) * 0.1,
                Math.cos(yaw) * Math.cos(pitch) * speed + Math.sin(spread) * wobble,
                ItemEntity.THROWN_PICKUP_DELAY,
                player);
    }

    /**
     * Scatters a stack around a player who lost it, such as on death: in a random direction, from
     * their eyes.
     */
    public ServerItemEntity scatterFromPlayer(final ServerPlayer player, final ItemStack stack) {
        final Location at = player.location();
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double speed = random.nextDouble() * 0.5;
        final double angle = random.nextDouble() * Math.PI * 2.0;
        return dropItem((ServerWorld) player.world(),
                at.x(), at.y() + eyeHeight(player) - 0.3, at.z(),
                stack,
                -Math.sin(angle) * speed, 0.2, Math.cos(angle) * speed,
                ItemEntity.THROWN_PICKUP_DELAY,
                player);
    }

    /**
     * Creates an item entity and adds it to its world, on the region thread owning its chunk.
     *
     * @param thrower the entity credited with dropping it, or {@code null}
     * @return the item, already in its world when called from the region thread owning
     *         {@code (x, z)}, added by that thread shortly after otherwise
     */
    public ServerItemEntity dropItem(final ServerWorld world, final double x, final double y, final double z,
                                     final ItemStack stack,
                                     final double velocityX, final double velocityY, final double velocityZ,
                                     final int pickupDelay, final @Nullable Entity thrower) {
        final ServerItemEntity item = new ServerItemEntity(ids.allocate(), Location.of(world, x, y, z, 0f, 0f), stack);
        item.setVelocity(velocityX, velocityY, velocityZ);
        item.setPickupDelay(pickupDelay);
        if (thrower != null) {
            item.setThrower(thrower.uuid());
        }
        final ChunkPos chunk = item.chunk();
        if (world.scheduler().isOwnedByCurrentThread(world.key(), chunk)) {
            spawn(item);
        } else {
            world.scheduler().execute(world.key(), chunk, () -> spawn(item));
        }
        return item;
    }

    private static double eyeHeight(final ServerPlayer player) {
        return player.isSneaking() ? SNEAKING_EYE_HEIGHT : STANDING_EYE_HEIGHT;
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
