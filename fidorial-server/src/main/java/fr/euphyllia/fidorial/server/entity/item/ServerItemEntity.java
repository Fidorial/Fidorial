package fr.euphyllia.fidorial.server.entity.item;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.player.PlayerPickupItemEventImpl;
import fr.euphyllia.fidorial.server.inventory.PlayerInventories;
import fr.euphyllia.fidorial.server.network.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundMoveEntityPosPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMotionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTakeItemEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.ItemStackWriter;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.utils.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A stack of items on the ground ({@code minecraft:item}).
 */
public final class ServerItemEntity extends AbstractEntity implements ItemEntity {

    /** Item entity data: the stack, after the 8 entries every entity has. */
    private static final int METADATA_ITEM = 8;
    private static final int METADATA_TYPE_SLOT = 7;

    private static final double HALF_WIDTH = 0.125;
    private static final double HEIGHT = 0.25;

    private static final double GRAVITY = 0.04;
    private static final double AIR_DRAG = 0.98;
    private static final double GROUND_FRICTION = 0.6;
    private static final double WATER_DRAG = 0.99;
    private static final double WATER_LIFT = 5.0E-4;
    private static final double MAX_WATER_RISE = 0.06;

    /** How far a single movement step may go, so that a fast item does not tunnel through a block. */
    private static final double MAX_STEP = 0.2;
    private static final double EPSILON = 1.0E-7;

    /**
     * How fast an item must hit a block for the client to be told its new velocity. Below that, it
     * is only gravity pressing an item on the floor, which the client simulates the same way.
     */
    private static final double IMPACT_THRESHOLD = GRAVITY * 1.5;

    /** How far below the bottom of the world an item falls before it is gone. */
    private static final int VOID_DEPTH = 64;

    /** How close two items must be to merge, on top of touching: half a block sideways. */
    private static final double MERGE_REACH = 0.5;
    private static final int MERGE_INTERVAL_MOVING = 2;
    private static final int MERGE_INTERVAL_STILL = 40;

    /** A player picks an item up from this far around their hitbox, sideways then up and down. */
    private static final double PICKUP_REACH_SIDE = 1.0;
    private static final double PICKUP_REACH_VERTICAL = 0.5;
    private static final double PLAYER_HALF_WIDTH = 0.3;
    private static final double PLAYER_HEIGHT = 1.8;

    /** The longest the client goes without a full position. */
    private static final int POSITION_SYNC_INTERVAL = 60;

    /**
     * Blocks an item falls through even though they are not air: plants, rails, signs and the
     * like. A rough stand-in until blocks carry their collision shapes.
     */
    private static final Set<Key> NO_COLLISION_TAGS = Set.of(
            Key.key("replaceable"),
            Key.key("flowers"),
            Key.key("saplings"),
            Key.key("crops"),
            Key.key("rails"),
            Key.key("buttons"),
            Key.key("pressure_plates"),
            Key.key("all_signs"),
            Key.key("banners"),
            Key.key("fire"));

    private static final Key LAVA = Key.key("lava");
    private static final Key WATER = Key.key("water");

    private volatile ItemStack item;
    private volatile int pickupDelay;
    private volatile int age;
    private volatile boolean unlimitedLifetime;
    private volatile @Nullable UUID owner;
    private volatile @Nullable UUID thrower;

    private volatile double velocityX;
    private volatile double velocityY;
    private volatile double velocityZ;
    private volatile boolean onGround;

    private int ticksExisted;
    private boolean velocityDirty;
    private boolean itemDirty;
    private double sentX;
    private double sentY;
    private double sentZ;
    private boolean sentOnGround;
    private int ticksSinceSync;

    public ServerItemEntity(final int entityId, final UUID uuid, final Location location, final ItemStack item) {
        super(entityId, uuid, EntityTypes.ITEM, location);
        if (item.isEmpty()) {
            throw new IllegalArgumentException("An item entity cannot carry an empty stack");
        }
        this.item = item;
        this.sentX = location.x();
        this.sentY = location.y();
        this.sentZ = location.z();
    }

    public ServerItemEntity(final int entityId, final Location location, final ItemStack item) {
        this(entityId, UUID.randomUUID(), location, item);
    }

    @Override
    public ItemStack item() {
        return item;
    }

    @Override
    public void setItem(final ItemStack item) {
        Objects.requireNonNull(item, "item");
        if (item.isEmpty()) {
            discard();
            return;
        }
        if (!item.equals(this.item)) {
            this.item = item;
            itemDirty = true;
            markSaveDirty();
        }
    }

    @Override
    public int pickupDelay() {
        return pickupDelay;
    }

    @Override
    public void setPickupDelay(final int ticks) {
        this.pickupDelay = Math.clamp(ticks, 0, INFINITE_PICKUP_DELAY);
    }

    @Override
    public int age() {
        return age;
    }

    @Override
    public void setAge(final int ticks) {
        this.age = Math.max(0, ticks);
    }

    @Override
    public boolean hasUnlimitedLifetime() {
        return unlimitedLifetime;
    }

    @Override
    public void setUnlimitedLifetime(final boolean unlimited) {
        this.unlimitedLifetime = unlimited;
    }

    @Override
    public @Nullable UUID owner() {
        return owner;
    }

    @Override
    public void setOwner(final @Nullable UUID owner) {
        this.owner = owner;
    }

    @Override
    public @Nullable UUID thrower() {
        return thrower;
    }

    @Override
    public void setThrower(final @Nullable UUID thrower) {
        this.thrower = thrower;
    }

    @Override
    public double velocityX() {
        return velocityX;
    }

    @Override
    public double velocityY() {
        return velocityY;
    }

    @Override
    public double velocityZ() {
        return velocityZ;
    }

    @Override
    public void setVelocity(final double x, final double y, final double z) {
        this.velocityX = x;
        this.velocityY = y;
        this.velocityZ = z;
        this.velocityDirty = true;
    }

    @Override
    public boolean onGround() {
        return onGround;
    }

    public void setOnGround(final boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public Component displayName() {
        return Component.translatable(item.translationKey());
    }

    @Override
    public Sound.Source soundSource() {
        return Sound.Source.NEUTRAL;
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved() || !(world() instanceof final ServerWorld world)) {
            return;
        }
        ticksExisted++;

        if (pickupDelay > 0 && pickupDelay != INFINITE_PICKUP_DELAY) {
            pickupDelay--;
        }
        if (!unlimitedLifetime && ++age >= DEFAULT_LIFETIME) {
            discard();
            return;
        }

        final Location before = location();
        if (before.y() < world.minY() - VOID_DEPTH) {
            discard();
            return;
        }
        final BlockState inside = BlockView.blockAt(world,
                floor(before.x()), floor(before.y() + HEIGHT / 2.0), floor(before.z()));
        if (inside == null) {
            // Its chunk is going away: stay still until it is saved.
            return;
        }
        if (burnsIn(inside)) {
            burn(before);
            return;
        }

        move(world, before, LAVA.equals(inside.name()) || WATER.equals(inside.name()));
        if (isRemoved()) {
            return;
        }

        final boolean moved = !location().equals(before);
        final int mergeInterval = moved ? MERGE_INTERVAL_MOVING : MERGE_INTERVAL_STILL;
        if (ticksExisted % mergeInterval == 0) {
            mergeWithNeighbours(world);
        }
        if (!isRemoved()) {
            tryPickup(world);
        }
        if (!isRemoved()) {
            syncToClients();
        }
    }

    private void move(final ServerWorld world, final Location from, final boolean inFluid) {
        double vx = velocityX;
        double vy = velocityY;
        double vz = velocityZ;

        if (inFluid) {
            // Items float up slowly through water, and stop sinking.
            vx *= WATER_DRAG;
            vy *= WATER_DRAG;
            vz *= WATER_DRAG;
            if (vy < MAX_WATER_RISE) {
                vy += WATER_LIFT;
            }
        } else {
            vy -= GRAVITY;
        }

        double x = from.x();
        double y = from.y();
        double z = from.z();

        if (collides(world, x, y, z)) {
            // Stuck inside a block: climb out the top when there is room.
            final double top = Math.floor(y) + 1.0;
            if (!collides(world, x, top, z)) {
                y = top;
                vy = 0.0;
                velocityDirty = true;
            }
        }

        final boolean wasOnGround = onGround;
        boolean landed = false;

        // Y first so that an item sliding along the floor is not stopped by the floor itself.
        final double movedY = moveAxis(world, x, y, z, 1, vy);
        if (movedY != vy) {
            if (vy < 0.0) {
                landed = true;
            }
            velocityDirty |= Math.abs(vy) > IMPACT_THRESHOLD;
            vy = 0.0;
        }
        y += movedY;

        final double movedX = moveAxis(world, x, y, z, 0, vx);
        if (movedX != vx) {
            velocityDirty |= Math.abs(vx) > IMPACT_THRESHOLD;
            vx = 0.0;
        }
        x += movedX;

        final double movedZ = moveAxis(world, x, y, z, 2, vz);
        if (movedZ != vz) {
            velocityDirty |= Math.abs(vz) > IMPACT_THRESHOLD;
            vz = 0.0;
        }
        z += movedZ;

        onGround = landed || (vy == 0.0 && collides(world, x, y - EPSILON * 10, z));
        if (onGround != wasOnGround) {
            velocityDirty = true;
        }

        final double horizontalDrag = onGround ? GROUND_FRICTION * AIR_DRAG : AIR_DRAG;
        vx *= horizontalDrag;
        vy *= AIR_DRAG;
        vz *= horizontalDrag;
        if (Math.abs(vx) < 0.003) {
            vx = 0.0;
        }
        if (Math.abs(vz) < 0.003) {
            vz = 0.0;
        }

        velocityX = vx;
        velocityY = vy;
        velocityZ = vz;

        if (x != from.x() || y != from.y() || z != from.z()) {
            final Location to = from.with(x, y, z);
            setLocation(to);
            final ChunkPos fromChunk = from.chunk();
            final ChunkPos toChunk = to.chunk();
            if (!fromChunk.equals(toChunk)) {
                world.entityMoved(this, fromChunk, toChunk);
            }
        }
    }

    /**
     * {@return how far the item gets along one axis, out of {@code distance}, before it hits a
     * block}
     */
    private static double moveAxis(final ServerWorld world, final double x, final double y, final double z,
                                   final int axis, final double distance) {
        if (Math.abs(distance) < EPSILON) {
            return 0.0;
        }
        final int steps = Math.max(1, (int) Math.ceil(Math.abs(distance) / MAX_STEP));
        final double step = distance / steps;
        double done = 0.0;
        for (int i = 0; i < steps; i++) {
            final double next = done + step;
            final double nx = axis == 0 ? x + next : x;
            final double ny = axis == 1 ? y + next : y;
            final double nz = axis == 2 ? z + next : z;
            if (collides(world, nx, ny, nz)) {
                return done + snapToBlock(axis == 0 ? x + done : axis == 1 ? y + done : z + done, step, axis);
            }
            done = next;
        }
        // Exactly the distance asked for, so that callers can tell a free move from a blocked one.
        return distance;
    }

    /**
     * {@return how much further than {@code from} the item gets in the step that hits a block:
     * right up against the block face along {@code axis}}
     */
    private static double snapToBlock(final double from, final double step, final int axis) {
        final double extentLow = axis == 1 ? 0.0 : HALF_WIDTH;
        final double extentHigh = axis == 1 ? HEIGHT : HALF_WIDTH;
        final double face;
        if (step > 0.0) {
            face = Math.floor(from + extentHigh + step) - extentHigh - EPSILON;
        } else {
            face = Math.ceil(from - extentLow + step) + extentLow + EPSILON;
        }
        final double gained = face - from;
        // Never move backwards, nor further than the step.
        return step > 0.0 ? Math.clamp(gained, 0.0, step) : Math.clamp(gained, step, 0.0);
    }

    private static boolean collides(final ServerWorld world, final double x, final double y, final double z) {
        final int minX = floor(x - HALF_WIDTH);
        final int maxX = floor(x + HALF_WIDTH - EPSILON);
        final int minY = floor(y);
        final int maxY = floor(y + HEIGHT - EPSILON);
        final int minZ = floor(z - HALF_WIDTH);
        final int maxZ = floor(z + HALF_WIDTH - EPSILON);
        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    final BlockState state = BlockView.blockAt(world, bx, by, bz);
                    // An unloaded neighbour stops the item rather than letting it fall out of the world.
                    if (state == null || isSolid(state)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSolid(final BlockState state) {
        if (state.isAir() || state.isFluid()) {
            return false;
        }
        for (final Key tag : NO_COLLISION_TAGS) {
            if (BlockView.isTagged(tag, state)) {
                return false;
            }
        }
        return true;
    }

    private boolean burnsIn(final BlockState state) {
        if (!LAVA.equals(state.name()) && !BlockView.isTagged(Key.key("fire"), state)) {
            return false;
        }
        // Netherite and ancient debris shrug off fire and lava, as in vanilla.
        final String path = item.id().value();
        return !path.startsWith("netherite_") && !path.equals("ancient_debris");
    }

    private void burn(final Location at) {
        sendToTrackers(new ClientboundSoundPacket(
                Sound.sound(SoundEvents.GENERIC_BURN, soundSource(), 0.4f,
                        2.0f + ThreadLocalRandom.current().nextFloat() * 0.4f),
                at.x(), at.y(), at.z()));
        discard();
    }

    private void mergeWithNeighbours(final ServerWorld world) {
        if (!canMerge()) {
            return;
        }
        final Location self = location();
        final ChunkPos chunk = self.chunk();
        final List<ServerItemEntity> neighbours = new ArrayList<>(4);
        world.entityManager().forEachInChunkRange(chunk.x(), chunk.z(), 1, entity -> {
            if (entity != this && entity instanceof final ServerItemEntity other && isMergeNeighbour(other)) {
                neighbours.add(other);
            }
        });
        for (final ServerItemEntity other : neighbours) {
            if (isRemoved() || !canMerge()) {
                return;
            }
            if (other.isRemoved() || !other.canMerge() || !other.isOwnedByCurrentThread()) {
                continue;
            }
            tryMerge(other);
        }
    }

    private boolean isMergeNeighbour(final ServerItemEntity other) {
        final Location a = location();
        final Location b = other.location();
        return Math.abs(a.x() - b.x()) <= HALF_WIDTH * 2 + MERGE_REACH
                && Math.abs(a.z() - b.z()) <= HALF_WIDTH * 2 + MERGE_REACH
                && Math.abs(a.y() - b.y()) <= HEIGHT;
    }

    private boolean canMerge() {
        final ItemStack stack = item;
        return !isRemoved()
                && pickupDelay != INFINITE_PICKUP_DELAY
                && (unlimitedLifetime || age < DEFAULT_LIFETIME)
                && stack.count() < maxStackSize(stack);
    }

    private void tryMerge(final ServerItemEntity other) {
        final ItemStack mine = item;
        final ItemStack theirs = other.item;
        if (!mine.isSimilar(theirs) || !Objects.equals(owner, other.owner)) {
            return;
        }
        // The bigger stack swallows the smaller one, like vanilla.
        if (theirs.count() > mine.count()) {
            other.absorb(this);
        } else {
            absorb(other);
        }
    }

    /**
     * Moves as much of {@code from} as fits into this stack, and removes {@code from} once empty.
     */
    private void absorb(final ServerItemEntity from) {
        final ItemStack into = item;
        final ItemStack taken = from.item;
        final int moved = Math.min(maxStackSize(into) - into.count(), taken.count());
        if (moved <= 0) {
            return;
        }
        setItem(into.withCount(into.count() + moved));
        pickupDelay = Math.max(pickupDelay, from.pickupDelay);
        age = Math.min(age, from.age);
        unlimitedLifetime |= from.unlimitedLifetime;
        from.setItem(taken.withCount(taken.count() - moved));
        syncItem();
        if (!from.isRemoved()) {
            from.syncItem();
        }
    }

    private void tryPickup(final ServerWorld world) {
        if (pickupDelay > 0) {
            return;
        }
        final Location self = location();
        final ChunkPos chunk = self.chunk();
        final List<ServerPlayer> candidates = new ArrayList<>(2);
        world.entityManager().forEachInChunkRange(chunk.x(), chunk.z(), 1, entity -> {
            if (entity instanceof final ServerPlayer player && canBePickedUpBy(player, self)) {
                candidates.add(player);
            }
        });
        for (final ServerPlayer player : candidates) {
            if (isRemoved()) {
                return;
            }
            pickUp(player);
        }
    }

    private boolean canBePickedUpBy(final ServerPlayer player, final Location self) {
        if (player.isRemoved() || player.isDead() || player.isAwaitingRespawn()
                || player.gameMode() == GameMode.SPECTATOR
                || !player.isOwnedByCurrentThread()) {
            return false;
        }
        final UUID restrictedTo = owner;
        if (restrictedTo != null && !restrictedTo.equals(player.uuid())) {
            return false;
        }
        final Location feet = player.location();
        final double sideReach = PLAYER_HALF_WIDTH + PICKUP_REACH_SIDE + HALF_WIDTH;
        return Math.abs(self.x() - feet.x()) < sideReach
                && Math.abs(self.z() - feet.z()) < sideReach
                && self.y() + HEIGHT > feet.y() - PICKUP_REACH_VERTICAL
                && self.y() < feet.y() + PLAYER_HEIGHT + PICKUP_REACH_VERTICAL;
    }

    private void pickUp(final ServerPlayer player) {
        final ItemStack stack = item;
        final PlayerInventory inventory = player.inventory();
        final int room = PlayerInventories.roomFor(inventory, stack);
        if (room <= 0) {
            return;
        }
        final ItemStack taken = stack.withCount(room);
        if (server().events().post(new PlayerPickupItemEventImpl(player, this, taken)).isCancelled()) {
            return;
        }

        final ItemStack left = PlayerInventories.add(inventory, player.selectedSlot(), taken);
        final int given = taken.count() - left.count();
        if (given <= 0) {
            return;
        }

        sendToTrackers(new ClientboundTakeItemEntityPacket(entityId(), player.entityId(), given));
        player.updateInventory();

        if (given >= stack.count()) {
            discard();
        } else {
            setItem(stack.withCount(stack.count() - given));
            syncItem();
        }
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        final Location location = location();
        connection.send(new ClientboundAddEntityPacket(
                entityId(),
                uuid(),
                EntityTypes.networkId(type()),
                LocationPositionData.vec3(location),
                new PositionData.VelocityVec3D(velocityX, velocityY, velocityZ),
                0.0f,
                0.0f,
                0.0f,
                0));
        connection.send(itemMetadata());
    }

    private ClientboundSetEntityMetadataPacket itemMetadata() {
        final ItemStack stack = item;
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.raw(METADATA_ITEM, METADATA_TYPE_SLOT,
                        buf -> ItemStackWriter.writeSlot(buf, stack, FidorialServer.getInstance().registries().serverRegistries())));
    }

    private void syncItem() {
        if (itemDirty) {
            itemDirty = false;
            sendToTrackers(itemMetadata());
        }
    }

    private void syncToClients() {
        syncItem();

        final Location current = location();
        final double dx = current.x() - sentX;
        final double dy = current.y() - sentY;
        final double dz = current.z() - sentZ;
        final boolean moved = Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 1.0 / 4096.0;
        ticksSinceSync++;

        if (moved || onGround != sentOnGround || ticksSinceSync >= POSITION_SYNC_INTERVAL) {
            final PositionData.Vec3D currentVec = LocationPositionData.vec3(current);
            final PositionData.DeltaVec3D delta = moved && ticksSinceSync < POSITION_SYNC_INTERVAL
                    ? PositionData.DeltaVec3D.between(new PositionData.Vec3D(sentX, sentY, sentZ), currentVec)
                    : null;
            if (delta != null) {
                sendToTrackers(new ClientboundMoveEntityPosPacket(entityId(), delta, onGround));
                sentX += delta.x() / 4096.0;
                sentY += delta.y() / 4096.0;
                sentZ += delta.z() / 4096.0;
            } else {
                sendToTrackers(new ClientboundEntityPositionSyncPacket(
                        entityId(),
                        new PositionData.LinearPositionPath(currentVec),
                        new PositionData.FloatRotation(0.0f, 0.0f),
                        onGround));
                sentX = current.x();
                sentY = current.y();
                sentZ = current.z();
                ticksSinceSync = 0;
            }
            sentOnGround = onGround;
        }

        if (velocityDirty) {
            velocityDirty = false;
            sendToTrackers(new ClientboundSetEntityMotionPacket(
                    entityId(), new PositionData.VelocityVec3D(velocityX, velocityY, velocityZ)));
        }
    }

    /**
     * Removes this item from its world, on the region thread owning it.
     */
    public void discard() {
        if (isRemoved()) {
            return;
        }
        if (isOwnedByCurrentThread()) {
            server().entities().despawn(this);
        } else {
            execute(() -> server().entities().despawn(this));
        }
    }

    private void markSaveDirty() {
        if (world() instanceof final ServerWorld world) {
            final ChunkPos chunk = chunk();
            world.markEntitiesDirty(chunk.x(), chunk.z());
        }
    }

    private static int maxStackSize(final ItemStack stack) {
        return Math.max(1, ItemDefaults.maxStackSize(stack.id(), stack));
    }

    private static int floor(final double value) {
        return (int) Math.floor(value);
    }
}
