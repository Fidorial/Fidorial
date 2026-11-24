package fr.euphyllia.fidorial.server.entity.projectile;

import fr.euphyllia.fidorial.server.combat.CombatEngine;
import fr.euphyllia.fidorial.server.combat.Hitboxes;
import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.physics.BlockCollider;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.entity.ProjectileHitEventImpl;
import fr.euphyllia.fidorial.server.inventory.PlayerInventories;
import fr.euphyllia.fidorial.server.item.enchantment.EnchantmentEngine;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundAddEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityPositionSyncPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundGameEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMotionPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundTakeItemEntityPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.LocationPositionData;
import fr.euphyllia.fidorial.server.network.protocol.packet.util.PositionData;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.player.Player;
import fr.fidorial.entity.projectile.ArrowProjectile;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.SoundEventKeys;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A projectile that flies like an arrow: it falls and slows down as it flies, deals damage scaling
 * with its speed to the first entity in its way, sticks in the block it hits and can then be picked
 * up again.
 *
 * <p>The client simulates the flight itself: the server sends where it is now and then, and at
 * once when it hits something.</p>
 *
 * <p>It is never saved with the chunk.</p>
 */
public abstract class ArrowProjectileEntity extends AbstractEntity implements ArrowProjectile {

    /**
     * Who may pick up a projectile stuck in the ground.
     */
    public enum PickupRule {
        /** Nobody. */
        NOBODY,
        /** Any player, who gets the item back. */
        ANY_PLAYER,
        /** Only a player in creative mode, who gets nothing. */
        CREATIVE_PLAYERS
    }

    protected static final double DEFAULT_GRAVITY = 0.05;
    private static final double AIR_DRAG = 0.99;
    private static final double WATER_DRAG = 0.6;

    private static final double BLOCK_STEP = 0.1;
    private static final int REFINE_STEPS = 6;
    private static final double POINT = 1.0E-3;
    private static final double ENTITY_MARGIN = 0.3;
    private static final double BACK_OFF = 0.05;

    private static final int DESPAWN_TICKS = 1200;
    private static final int UNLOADED_LIFETIME = 1200;
    private static final int WOBBLE_TICKS = 7;
    private static final int SYNC_INTERVAL = 20;
    private static final int FIRE_SECONDS_ON_HIT = 5;
    private static final double PUNCH_PUSH = 0.6;
    private static final double PUNCH_LIFT = 0.1;
    private static final double BOUNCE = -0.1;

    private static final double PICKUP_REACH_SIDE = 1.0;
    private static final double PICKUP_REACH_UP = 0.5;

    /** The game event a player hears when their arrow hits another player. */
    private static final int ARROW_HIT_PLAYER = 6;

    private static final int MD_FLAGS = 8;
    private static final int MD_PIERCE_LEVEL = 9;
    private static final int MD_STUCK = 10;
    private static final byte FLAG_CRITICAL = 0x01;
    private static final byte FLAG_THROUGH_BLOCKS = 0x02;
    private static final int MD_SHARED_FLAGS = 0;
    private static final byte SHARED_FLAG_ON_FIRE = 0x01;

    private final @Nullable AbstractLivingEntity owner;
    private final ItemStack weapon;
    private ItemStack pickupItem;
    private PickupRule pickup = PickupRule.ANY_PLAYER;

    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private double baseDamage = 2.0;
    private boolean critical;
    private int piercing;
    private final Set<Integer> pierced = new HashSet<>();
    private boolean stuck;
    private @Nullable BlockPosition stuckIn;
    private int stuckTicks;
    private int wobble;
    private int fireTicks;
    private boolean clearOfOwner;
    private boolean throughBlocks;
    private boolean fromCrossbow;
    private int age;
    private int ticksSinceSync;
    private boolean sentOnFire;

    protected ArrowProjectileEntity(final int entityId, final EntityType type, final Location location,
                                    final @Nullable AbstractLivingEntity owner, final ItemStack weapon,
                                    final ItemStack pickupItem) {
        super(entityId, UUID.randomUUID(), type, location);
        this.owner = owner;
        this.weapon = weapon;
        this.pickupItem = pickupItem.isEmpty() ? pickupItem : pickupItem.withCount(1);
    }

    @Override
    public @Nullable Entity owner() {
        return owner;
    }

    /**
     * {@return the living entity that shot this projectile, or {@code null}}
     */
    public @Nullable AbstractLivingEntity livingOwner() {
        return owner;
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
        if (!isRemoved()) {
            syncMotion();
        }
    }

    @Override
    public double baseDamage() {
        return baseDamage;
    }

    @Override
    public void setBaseDamage(final double damage) {
        this.baseDamage = damage;
    }

    @Override
    public boolean isCritical() {
        return critical;
    }

    @Override
    public void setCritical(final boolean critical) {
        this.critical = critical;
        if (!isRemoved()) {
            sendToTrackers(metadata());
        }
    }

    @Override
    public int piercing() {
        return piercing;
    }

    /**
     * @param piercing how many entities this projectile goes through before it stops
     */
    public void setPiercing(final int piercing) {
        this.piercing = Math.clamp(piercing, 0, Byte.MAX_VALUE);
    }

    @Override
    public boolean isStuck() {
        return stuck;
    }

    @Override
    public boolean canBePickedUp() {
        return pickup == PickupRule.ANY_PLAYER;
    }

    @Override
    public ItemStack pickupItem() {
        return pickupItem;
    }

    @Override
    public ItemStack weapon() {
        return weapon;
    }

    public PickupRule pickup() {
        return pickup;
    }

    public void setPickup(final PickupRule pickup) {
        this.pickup = pickup;
    }

    /**
     * @param fromCrossbow {@code true} for a projectile a crossbow shot, which sounds different
     *                         when it hits
     */
    public void setFromCrossbow(final boolean fromCrossbow) {
        this.fromCrossbow = fromCrossbow;
    }

    /**
     * Sets this projectile on fire for at least {@code ticks}, as Flame does.
     */
    public void igniteForTicks(final int ticks) {
        this.fireTicks = Math.max(fireTicks, ticks);
    }

    /**
     * {@return {@code true} while this projectile is on fire}
     */
    public boolean isOnFire() {
        return fireTicks > 0;
    }

    protected final boolean isThroughBlocks() {
        return throughBlocks;
    }

    protected final void setThroughBlocks(final boolean throughBlocks) {
        if (this.throughBlocks != throughBlocks) {
            this.throughBlocks = throughBlocks;
            if (throughBlocks) {
                stuck = false;
                stuckIn = null;
                stuckTicks = 0;
            }
            sendToTrackers(metadata());
        }
    }

    protected final int stuckTicks() {
        return stuckTicks;
    }

    protected final void setPickupItem(final ItemStack pickupItem) {
        this.pickupItem = pickupItem;
    }

    protected final CombatEngine combat() {
        return server().entities().combat();
    }

    @Override
    public Sound.Source soundSource() {
        return Sound.Source.NEUTRAL;
    }

    /**
     * Sends this projectile flying in a direction.
     *
     * @param x          the x component of the direction
     * @param y          the y component of the direction
     * @param z          the z component of the direction
     * @param speed      how fast, in blocks per tick
     * @param inaccuracy how far from the direction it may stray
     */
    public void launch(final double x, final double y, final double z, final double speed, final double inaccuracy) {
        final double length = Math.sqrt(x * x + y * y + z * z);
        if (length < 1.0E-7) {
            return;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double spread = 0.0172275 * inaccuracy;
        final double vx = (x / length + triangle(random, spread)) * speed;
        final double vy = (y / length + triangle(random, spread)) * speed;
        final double vz = (z / length + triangle(random, spread)) * speed;
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        faceVelocity();
    }

    private static double triangle(final ThreadLocalRandom random, final double deviation) {
        final double first = random.nextDouble();
        final double second = random.nextDouble();
        return deviation * (first - second);
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved()) {
            return;
        }
        age++;
        if (!(world() instanceof final ServerWorld world)) {
            discard();
            return;
        }
        if (fireTicks > 0) {
            fireTicks--;
        }
        if (wobble > 0) {
            wobble--;
        }
        if (location().y() < world.minY() - 64) {
            belowWorld();
            return;
        }
        if (beforeMove(world)) {
            syncState();
            return;
        }
        if (stuck) {
            if (stuckIn != null && isFree(world, stuckIn)) {
                // the block it was stuck in went away: it falls again
                stuck = false;
                stuckIn = null;
                final ThreadLocalRandom random = ThreadLocalRandom.current();
                velocityX = random.nextDouble() * 0.2;
                velocityY = random.nextDouble() * 0.2;
                velocityZ = random.nextDouble() * 0.2;
                stuckTicks = 0;
                sendToTrackers(metadata());
                syncMotion();
            } else {
                stuckTicks++;
                whileStuck();
                if (!isRemoved()) {
                    offerToPlayers(world);
                }
                syncState();
                return;
            }
        }
        if (throughBlocks) {
            moveFreely(world);
            offerToPlayers(world);
        } else {
            move(world);
        }
        if (isRemoved()) {
            return;
        }
        if (!stuck && !throughBlocks) {
            final double drag = isInWater(world) ? waterDrag() : AIR_DRAG;
            velocityX *= drag;
            velocityY *= drag;
            velocityZ *= drag;
            velocityY -= gravity();
            faceVelocity();
        }
        syncState();
    }

    /**
     * Runs before this projectile moves.
     *
     * @return {@code true} to skip the rest of the tick
     */
    protected boolean beforeMove(final ServerWorld world) {
        return false;
    }

    /**
     * Runs each tick this projectile spends stuck in a block: it despawns after a minute.
     */
    protected void whileStuck() {
        if (stuckTicks >= DESPAWN_TICKS) {
            discard();
        }
    }

    /**
     * Runs when this projectile falls below the world.
     */
    protected void belowWorld() {
        discard();
    }

    protected double gravity() {
        return DEFAULT_GRAVITY;
    }

    protected double waterDrag() {
        return WATER_DRAG;
    }

    private boolean isInWater(final ServerWorld world) {
        final Location at = location();
        return BlockView.isInWater(world, at.x(), at.y(), at.z());
    }

    private void move(final ServerWorld world) {
        final Location from = location();
        final double x = from.x();
        final double y = from.y();
        final double z = from.z();
        final double dx = velocityX;
        final double dy = velocityY;
        final double dz = velocityZ;

        if (!clearOfOwner) {
            clearOfOwner = owner == null || !Hitboxes.overlap(Hitboxes.box(owner, 1.0),
                    new BlockCollider.Box(x - POINT, y - POINT, z - POINT, x + POINT, y + POINT, z + POINT));
        }

        final BlockHit blockHit = findBlockHit(world, x, y, z, dx, dy, dz);
        if (blockHit == BlockHit.UNLOADED) {
            // it waits at the edge of the loaded chunks, for a minute at most
            if (age > UNLOADED_LIFETIME) {
                discard();
            }
            return;
        }
        final double limit = blockHit == null ? 1.0 : blockHit.at();

        // entities in the way, nearest first, up to the block hit
        for (final EntityHit hit : findEntityHits(world, x, y, z, dx, dy, dz, limit)) {
            final Outcome result = strike(hit.entity());
            if (isRemoved()) {
                return;
            }
            if (result == Outcome.HALT) {
                // the projectile bounced or stopped against the entity: it stays where it was
                moveTo(world, from, x + dx * hit.at(), y + dy * hit.at(), z + dz * hit.at());
                return;
            }
        }

        if (blockHit != null) {
            final double hx = x + dx * blockHit.at();
            final double hy = y + dy * blockHit.at();
            final double hz = z + dz * blockHit.at();
            if (!server().events().post(ProjectileHitEventImpl.block(this, blockHit.block())).isCancelled()) {
                final double speed = Math.sqrt(dx * dx + dy * dy + dz * dz);
                moveTo(world, from, hx - dx / speed * BACK_OFF, hy - dy / speed * BACK_OFF, hz - dz / speed * BACK_OFF);
                stickInto(blockHit.block());
                return;
            }
        }
        moveTo(world, from, x + dx, y + dy, z + dz);
    }

    /**
     * Moves without bumping into anything, as a trident flying back to its thrower.
     */
    private void moveFreely(final ServerWorld world) {
        final Location from = location();
        moveTo(world, from, from.x() + velocityX, from.y() + velocityY, from.z() + velocityZ);
        faceVelocity();
    }

    private record BlockHit(double at, BlockPosition block) {

        static final BlockHit UNLOADED = new BlockHit(-1.0, Position.block(0, 0, 0));
    }

    private record EntityHit(double at, AbstractLivingEntity entity) {
    }

    private static @Nullable BlockHit findBlockHit(final ServerWorld world, final double x, final double y, final double z,
                                                   final double dx, final double dy, final double dz) {
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        final int steps = Math.max(1, (int) Math.ceil(length / BLOCK_STEP));
        double previous = 0.0;
        for (int step = 1; step <= steps; step++) {
            final double at = (double) step / steps;
            final double px = x + dx * at;
            final double py = y + dy * at;
            final double pz = z + dz * at;
            if (BlockView.blockAt(world, (int) Math.floor(px), (int) Math.floor(py), (int) Math.floor(pz)) == null) {
                return BlockHit.UNLOADED;
            }
            if (solidAt(world, px, py, pz)) {
                double low = previous;
                double high = at;
                for (int refine = 0; refine < REFINE_STEPS; refine++) {
                    final double middle = (low + high) / 2.0;
                    if (solidAt(world, x + dx * middle, y + dy * middle, z + dz * middle)) {
                        high = middle;
                    } else {
                        low = middle;
                    }
                }
                return new BlockHit(low, hitBlock(world, x + dx * high, y + dy * high, z + dz * high, dx, dy, dz));
            }
            previous = at;
        }
        return null;
    }

    /**
     * {@return the block a projectile touched at a point}: the point lies on a face of the block, up
     * to {@link #POINT} in front of it, so it can round to the free block in front, such as the air
     * above the ground an arrow lands on. A projectile stuck in a free block falls again at once.
     */
    private static BlockPosition hitBlock(final ServerWorld world, final double x, final double y, final double z,
                                          final double dx, final double dy, final double dz) {
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        final double nudge = length > 0.0 ? 2.0 * POINT / length : 0.0;
        // a little further along the flight is inside the block, unless the projectile grazed it
        final BlockPosition ahead = Position.block((int) Math.floor(x + dx * nudge), (int) Math.floor(y + dy * nudge),
                (int) Math.floor(z + dz * nudge));
        if (!isFree(world, ahead)) {
            return ahead;
        }
        for (final double offsetX : new double[]{-POINT, POINT}) {
            for (final double offsetY : new double[]{-POINT, POINT}) {
                for (final double offsetZ : new double[]{-POINT, POINT}) {
                    final BlockPosition around = Position.block((int) Math.floor(x + offsetX),
                            (int) Math.floor(y + offsetY), (int) Math.floor(z + offsetZ));
                    if (!isFree(world, around)) {
                        return around;
                    }
                }
            }
        }
        return ahead;
    }

    private static boolean solidAt(final ServerWorld world, final double x, final double y, final double z) {
        return BlockCollider.collides(world, BlockCollider.Shapes.DEFAULT,
                new BlockCollider.Box(x - POINT, y - POINT, z - POINT, x + POINT, y + POINT, z + POINT));
    }

    private static boolean isFree(final ServerWorld world, final BlockPosition block) {
        final BlockState state = BlockView.blockAt(world, block.blockX(), block.blockY(), block.blockZ());
        return state != null && !BlockCollider.collides(world, BlockCollider.Shapes.DEFAULT,
                new BlockCollider.Box(block.blockX(), block.blockY(), block.blockZ(),
                        block.blockX() + 1.0, block.blockY() + 1.0, block.blockZ() + 1.0));
    }

    private List<EntityHit> findEntityHits(final ServerWorld world, final double x, final double y, final double z,
                                           final double dx, final double dy, final double dz, final double limit) {
        final List<EntityHit> hits = new ArrayList<>(2);
        world.entityManager().forEachInChunkRange((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4, 1, entity -> {
            if (!(entity instanceof final AbstractLivingEntity living) || !canHit(living)) {
                return;
            }
            final double at = Hitboxes.entry(Hitboxes.box(living, ENTITY_MARGIN), x, y, z, dx, dy, dz);
            if (at >= 0.0 && at <= limit) {
                hits.add(new EntityHit(at, living));
            }
        });
        hits.sort((first, second) -> Double.compare(first.at(), second.at()));
        return hits;
    }

    private boolean canHit(final AbstractLivingEntity entity) {
        if (entity.isRemoved() || entity.isDead() || !entity.isOwnedByCurrentThread()) {
            return false;
        }
        if (entity == owner && !clearOfOwner) {
            return false;
        }
        if (entity instanceof final ServerPlayer player
                && (player.gameMode() == GameMode.SPECTATOR || player.isAwaitingRespawn())) {
            return false;
        }
        return !pierced.contains(entity.entityId());
    }

    /**
     * What becomes of a projectile once it hit an entity.
     */
    protected enum Outcome {
        /** It flies on, through the entity. */
        PASS_THROUGH,
        /** It stops there, or bounced off. */
        HALT
    }

    /**
     * {@return the damage source of a hit of this projectile}
     */
    protected abstract DamageSource damageSource();

    /**
     * {@return the damage of a hit on an entity}: the base damage, changed by the enchantments of
     * the weapon, times the speed, rounded up, plus a random bonus for a critical projectile
     */
    protected int hitDamage(final AbstractLivingEntity victim, final DamageSource source) {
        double base = baseDamage;
        if (owner != null && !weapon.isEmpty()) {
            base = EnchantmentEngine.modifyDamage(owner, weapon, victim, source, (float) base);
        }
        final double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
        int damage = (int) Math.ceil(Math.clamp(speed * base, 0.0, Integer.MAX_VALUE));
        if (critical) {
            final long bonus = ThreadLocalRandom.current().nextInt(damage / 2 + 2);
            damage = (int) Math.min(bonus + damage, Integer.MAX_VALUE);
        }
        return damage;
    }

    /**
     * Hits an entity in the way.
     */
    protected Outcome strike(final AbstractLivingEntity victim) {
        if (server().events().post(ProjectileHitEventImpl.entity(this, victim)).isCancelled()) {
            return Outcome.PASS_THROUGH;
        }
        if (piercing > 0) {
            if (pierced.size() >= piercing + 1) {
                discard();
                return Outcome.HALT;
            }
            pierced.add(victim.entityId());
        }
        final DamageSource source = damageSource();
        final int damage = hitDamage(victim, source);
        final int fireBefore = victim.fireTicks();
        if (isOnFire()) {
            victim.igniteForTicks(FIRE_SECONDS_ON_HIT * 20);
        }
        final boolean hurt = combat().canHarm(owner, victim) && combat().hurt(victim, source, damage, 0.4);
        if (!hurt) {
            victim.setFireTicks(fireBefore);
            bounce();
            return Outcome.HALT;
        }
        punch(victim, source);
        afterHurt(victim);
        if (owner instanceof final ServerPlayer shooter && victim instanceof Player && victim != shooter) {
            shooter.connection().send(new ClientboundGameEventPacket(ARROW_HIT_PLAYER, 0.0f));
        }
        playSound(hitSound(), 1.0f, 1.2f / (ThreadLocalRandom.current().nextFloat() * 0.2f + 0.9f));
        if (piercing <= 0) {
            discard();
            return Outcome.HALT;
        }
        return Outcome.PASS_THROUGH;
    }

    /**
     * Pushes an entity this projectile hurt further, as Punch does.
     */
    private void punch(final AbstractLivingEntity victim, final DamageSource source) {
        if (owner == null || weapon.isEmpty()) {
            return;
        }
        final double punch = EnchantmentEngine.knockbackBonus(owner, weapon, victim, source);
        if (punch <= 0.0) {
            return;
        }
        final double horizontal = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
        if (horizontal < 1.0E-7) {
            return;
        }
        final double scale = punch * PUNCH_PUSH * (1.0 - Math.clamp(victim.knockbackResistance(), 0.0, 1.0));
        combat().addVelocity(victim, velocityX / horizontal * scale, PUNCH_LIFT, velocityZ / horizontal * scale);
    }

    /**
     * Runs once this projectile hurt an entity, before it is gone, as a spectral arrow makes it glow.
     */
    protected void afterHurt(final AbstractLivingEntity victim) {
    }

    /**
     * Bounces back from an entity it could not hurt.
     */
    protected void bounce() {
        velocityX *= BOUNCE;
        velocityY *= BOUNCE;
        velocityZ *= BOUNCE;
        faceVelocity();
        if (velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ < 1.0E-7) {
            if (pickup == PickupRule.ANY_PLAYER && !pickupItem.isEmpty() && world() instanceof final ServerWorld world) {
                final Location at = location();
                server().entities().dropItem(world, at.x(), at.y(), at.z(), pickupItem, 0.0, 0.0, 0.0,
                        ItemEntity.DEFAULT_PICKUP_DELAY, null);
            }
            discard();
            return;
        }
        syncMotion();
    }

    /**
     * Sticks in a block.
     */
    protected void stickInto(final BlockPosition block) {
        stuck = true;
        stuckIn = block;
        stuckTicks = 0;
        wobble = WOBBLE_TICKS;
        critical = false;
        piercing = 0;
        pierced.clear();
        velocityX = 0.0;
        velocityY = 0.0;
        velocityZ = 0.0;
        playSound(blockHitSound(), 1.0f, 1.2f / (ThreadLocalRandom.current().nextFloat() * 0.2f + 0.9f));
        sendToTrackers(metadata());
        syncPosition();
        syncMotion();
    }

    protected Key hitSound() {
        return fromCrossbow ? SoundEventKeys.ITEM_CROSSBOW_HIT.key() : SoundEventKeys.ENTITY_ARROW_HIT.key();
    }

    protected Key blockHitSound() {
        return hitSound();
    }

    protected final void playSound(final Key sound, final float volume, final float pitch) {
        combat().playAt(this, sound, volume, pitch);
    }

    private void offerToPlayers(final ServerWorld world) {
        if (wobble > 0 || (!stuck && !throughBlocks)) {
            return;
        }
        final Location self = location();
        final ChunkPos chunk = self.chunk();
        final List<ServerPlayer> touching = new ArrayList<>(1);
        world.entityManager().forEachInChunkRange(chunk.x(), chunk.z(), 1, entity -> {
            if (entity instanceof final ServerPlayer player && touches(player)) {
                touching.add(player);
            }
        });
        for (final ServerPlayer player : touching) {
            if (isRemoved()) {
                return;
            }
            pickUp(player);
        }
    }

    private boolean touches(final ServerPlayer player) {
        if (player.isRemoved() || player.isDead() || player.isAwaitingRespawn()
                || player.gameMode() == GameMode.SPECTATOR || !player.isOwnedByCurrentThread()) {
            return false;
        }
        final BlockCollider.Box reach = Hitboxes.box(player, 0.0);
        final BlockCollider.Box grown = new BlockCollider.Box(reach.minX() - PICKUP_REACH_SIDE, reach.minY() - PICKUP_REACH_UP,
                reach.minZ() - PICKUP_REACH_SIDE, reach.maxX() + PICKUP_REACH_SIDE, reach.maxY() + PICKUP_REACH_UP,
                reach.maxZ() + PICKUP_REACH_SIDE);
        return Hitboxes.overlap(grown, Hitboxes.box(this, 0.0));
    }

    /**
     * {@return {@code true} if a player may take this projectile}
     */
    protected boolean canBePickedUpBy(final ServerPlayer player) {
        return switch (pickup) {
            case ANY_PLAYER -> true;
            case CREATIVE_PLAYERS -> player.gameMode() == GameMode.CREATIVE;
            case NOBODY -> false;
        };
    }

    private void pickUp(final ServerPlayer player) {
        if (!canBePickedUpBy(player)) {
            return;
        }
        if (pickup == PickupRule.ANY_PLAYER && !pickupItem.isEmpty()) {
            final ItemStack left = PlayerInventories.add(player.inventory(), player.selectedSlot(), pickupItem);
            if (!left.isEmpty()) {
                return;
            }
            player.updateInventory();
        }
        sendToTrackers(new ClientboundTakeItemEntityPacket(entityId(), player.entityId(), 1));
        player.connection().send(new ClientboundTakeItemEntityPacket(entityId(), player.entityId(), 1));
        discard();
    }

    private void moveTo(final ServerWorld world, final Location from, final double x, final double y, final double z) {
        moveWithin(world, from.with(x, y, z));
    }

    /**
     * Turns this projectile to face where it flies.
     */
    protected final void faceVelocity() {
        final double horizontal = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
        if (horizontal < 1.0E-7 && Math.abs(velocityY) < 1.0E-7) {
            return;
        }
        final float yaw = (float) Math.toDegrees(Math.atan2(velocityX, velocityZ));
        final float pitch = (float) Math.toDegrees(Math.atan2(velocityY, horizontal));
        final Location at = location();
        setLocation(at.with(at.x(), at.y(), at.z(), yaw, pitch));
    }

    /**
     * Removes this projectile from its world.
     */
    public final void discard() {
        if (!isRemoved()) {
            server().entities().despawn(this);
        }
    }

    private void syncState() {
        if (sentOnFire != isOnFire()) {
            sentOnFire = isOnFire();
            sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                    ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_SHARED_FLAGS, sentOnFire ? SHARED_FLAG_ON_FIRE : 0)));
        }
        if (throughBlocks) {
            syncPosition();
            syncMotion();
            return;
        }
        if (++ticksSinceSync >= SYNC_INTERVAL) {
            syncPosition();
            if (!stuck) {
                syncMotion();
            }
        }
    }

    protected final void syncPosition() {
        ticksSinceSync = 0;
        final Location at = location();
        sendToTrackers(new ClientboundEntityPositionSyncPacket(
                entityId(),
                new PositionData.LinearPositionPath(LocationPositionData.vec3(at)),
                LocationPositionData.floatRotation(at),
                0.0f,
                false));
    }

    protected final void syncMotion() {
        sendToTrackers(new ClientboundSetEntityMotionPacket(entityId(),
                new PositionData.VelocityVec3D(velocityX, velocityY, velocityZ)));
    }

    /**
     * {@return the data of this projectile the viewers render it with}
     */
    protected ClientboundSetEntityMetadataPacket metadata() {
        return ClientboundSetEntityMetadataPacket.of(entityId(), metadataEntries().toArray(ClientboundSetEntityMetadataPacket.Entry[]::new));
    }

    protected List<ClientboundSetEntityMetadataPacket.Entry> metadataEntries() {
        final List<ClientboundSetEntityMetadataPacket.Entry> entries = new ArrayList<>();
        entries.add(ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_FLAGS,
                (critical ? FLAG_CRITICAL : 0) | (throughBlocks ? FLAG_THROUGH_BLOCKS : 0)));
        entries.add(ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_PIERCE_LEVEL, piercing));
        entries.add(ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_STUCK, stuck));
        return entries;
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
                location.pitch(),
                location.yaw(),
                0.0f,
                owner == null ? 0 : owner.entityId()));
        connection.send(metadata());
        if (isOnFire()) {
            connection.send(ClientboundSetEntityMetadataPacket.of(entityId(),
                    ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_SHARED_FLAGS, SHARED_FLAG_ON_FIRE)));
        }
    }
}
