package fr.euphyllia.fidorial.server.entity.mob.ambient;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fr.euphyllia.fidorial.server.combat.DamageSourceFactoryImpl;
import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.mob.AbstractFlyingMob;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.ChunkColumn;
import fr.euphyllia.fidorial.server.world.chunk.Heightmap;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.GameMode;
import fr.fidorial.entity.mob.ambient.Bat;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The bat: it hangs from the ceiling of caves until a player comes near, then flutters about at
 * random, and now and then hangs again from a block above it.
 */
public final class BatEntity extends AbstractFlyingMob implements Category.Ambient, Bat {

    public static final float MAX_HEALTH = 6f;

    private static final double HALF_WIDTH = 0.25;
    private static final double HEIGHT = 0.9;
    private static final double EYE_HEIGHT = 0.45;

    /**
     * A bat falls as any mob, and the air keeps 98% of its vertical speed; the bat then keeps 60% of
     * what is left after every move.
     */
    private static final double GRAVITY = 0.08;
    private static final double VERTICAL_DRAG = 0.98 * 0.6;
    private static final double AIR_DRAG = 0.91;
    private static final double GROUND_DRAG = 0.6 * 0.91;

    private static final int MD_BAT_FLAGS = 16;

    private static final byte FLAG_RESTING = 0x01;
    /**
     * The world event that plays the sound of a bat taking off.
     */
    private static final int LEVEL_EVENT_BAT_TAKEOFF = 1025;

    /**
     * A player within this distance that the bat sees wakes it; one who hides wakes it from closer,
     * {@code MIN_WAKE_UP_RANGE} at least.
     */
    private static final double WAKE_UP_RANGE = 4.0;
    private static final double MIN_WAKE_UP_RANGE = 2.0;

    private static final int ROOST_CHANCE = 100;
    private static final int RETARGET_CHANCE = 30;
    private static final int HEAD_TURN_CHANCE = 200;
    private static final double TARGET_REACHED_DISTANCE = 2.0;
    private static final int WANDER_HORIZONTAL_RANGE = 7;
    private static final int WANDER_VERTICAL_RANGE = 6;
    private static final int WANDER_VERTICAL_OFFSET = 2;

    /**
     * Each tick the bat gains a tenth of the speed it lacks to fly at half a block a tick across and
     * 0.7 up or down, towards a point a tenth of a block above the bottom of the block it aims at.
     */
    private static final double STEER_STRENGTH = 0.1;
    private static final double HORIZONTAL_TARGET_SPEED = 0.5;
    private static final double VERTICAL_TARGET_SPEED = 0.7;

    private static final double TARGET_HEIGHT = 0.1;
    private static final int RESTING_SILENCE_CHANCE = 4;
    private static final float SOUND_VOLUME = 0.1f;
    private static final float VOICE_PITCH = 0.95f;

    /**
     * A bat spawns only where the light is under a random number from 0 to 3.
     */
    private static final int SPAWN_LIGHT_BOUND = 4;

    private static final int LAVA_FIRE_TICKS = 300;
    private static final float LAVA_DAMAGE = 4f;
    private static final float LAVA_BURN_VOLUME = 0.4f;

    /**
     * A new bat hangs: it wakes up at once unless there is a block above it to hang from.
     */
    private boolean resting = true;
    private @Nullable BlockPosition wanderTarget;

    public BatEntity(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.BAT, location, MAX_HEALTH);
    }

    /**
     * {@return {@code true} if a bat may spawn in a block}: under the highest block of the column,
     * half the time, in a light under a random number from 0 to 3, on a block of the
     * {@code bats_spawnable_on} tag
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        final ChunkColumn column = world.loadedColumn(x >> 4, z >> 4);
        if (column == null || y >= column.height(Heightmap.WORLD_SURFACE, x & 15, z & 15)) {
            return false;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextBoolean() || Brightness.light(world, x, y, z) > random.nextInt(SPAWN_LIGHT_BOUND)) {
            return false;
        }
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        return ground != null && ground.isTagged(BlockTypeTagKeys.BATS_SPAWNABLE_ON);
    }

    @Override
    public double height() {
        return HEIGHT;
    }

    @Override
    protected double halfWidth() {
        return HALF_WIDTH;
    }

    @Override
    public double eyeHeight() {
        return EYE_HEIGHT;
    }

    @Override
    protected double gravity() {
        return GRAVITY;
    }

    @Override
    protected double verticalDrag() {
        return VERTICAL_DRAG;
    }

    @Override
    protected double horizontalDrag(final boolean onGround) {
        return onGround ? GROUND_DRAG : AIR_DRAG;
    }

    @Override
    protected boolean isFlightEnabled() {
        return !resting;
    }

    /**
     * {@return {@code true} if this bat hangs from a block}
     */
    @Override
    public boolean isResting() {
        return resting;
    }

    /**
     * Makes this bat hang from the block above it or fly off, without a sound.
     *
     * @param resting {@code true} to hang
     */
    @Override
    public void setResting(final boolean resting) {
        if (this.resting == resting) {
            return;
        }
        this.resting = resting;
        sendToTrackers(flagsPacket());
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }

        tickLava();
        checkDespawn();
    }

    @Override
    protected void tickFlight(final long currentTick) {
        if (resting) {
            tickResting();
        } else {
            tickFlying();
        }
    }

    @SuppressFBWarnings(
            value = "FE_FLOATING_POINT_EQUALITY",
            justification = "Exact floating-point comparison is intentional."
    )
    private void tickResting() {
        final Location self = location();
        final int blockX = (int) Math.floor(self.x());
        final int blockY = (int) Math.floor(self.y());
        final int blockZ = (int) Math.floor(self.z());

        if (!isConductor(blockX, blockY + 1, blockZ)) {
            takeOff();
            return;
        }

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextInt(HEAD_TURN_CHANCE) == 0) {
            setHeadYaw(random.nextInt(360));
        }

        if (seesPlayerNearby()) {
            takeOff();
            return;
        }

        // hangs with the top of its box against the block above
        final double restingY = blockY + 1.0 - height();
        if (self.y() != restingY) {
            setLocation(self.with(self.x(), restingY, self.z(), yaw(), pitch()));
        }
    }

    private void takeOff() {
        setResting(false);
        sendToTrackers(new ClientboundLevelEventPacket(LEVEL_EVENT_BAT_TAKEOFF, location(), 0, false));
    }

    /**
     * {@return {@code true} if a player near enough that is not a spectator is in sight}
     */
    private boolean seesPlayerNearby() {
        for (final ServerPlayer player : server().players().online()) {
            if (player.isRemoved() || player.isDead() || player.gameMode() == GameMode.SPECTATOR
                    || !player.world().equals(world())) {
                continue;
            }
            final double range = Math.max(WAKE_UP_RANGE * Targeting.visibility(player, this), MIN_WAKE_UP_RANGE);
            if (distanceSqTo(player) <= range * range && hasLineOfSightTo(player)) {
                return true;
            }
        }
        return false;
    }

    private void tickFlying() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Location self = location();

        final BlockPosition target = wanderTarget(self, random);

        final double dx = target.x() + 0.5 - self.x();
        final double dy = target.y() + TARGET_HEIGHT - self.y();
        final double dz = target.z() + 0.5 - self.z();
        final double velocityX = velocityX() + (Math.signum(dx) * HORIZONTAL_TARGET_SPEED - velocityX()) * STEER_STRENGTH;
        final double velocityY = velocityY() + (Math.signum(dy) * VERTICAL_TARGET_SPEED - velocityY()) * STEER_STRENGTH;
        final double velocityZ = velocityZ() + (Math.signum(dz) * HORIZONTAL_TARGET_SPEED - velocityZ()) * STEER_STRENGTH;
        setVelocity(velocityX, velocityY, velocityZ);
        setYaw((float) Math.toDegrees(Math.atan2(velocityZ, velocityX)) - 90.0f);

        if (random.nextInt(ROOST_CHANCE) == 0
                && isConductor((int) Math.floor(self.x()), (int) Math.floor(self.y()) + 1, (int) Math.floor(self.z()))) {
            setResting(true);
        }
    }

    /**
     * {@return where this bat flies to}: the same place until it is no longer air, the bat is
     * within two blocks of it or one tick in thirty; then a block up to six away across, two below
     * or three above
     */
    private BlockPosition wanderTarget(final Location self, final ThreadLocalRandom random) {
        final BlockPosition current = wanderTarget;
        if (current != null && !isWithin(current, self) && random.nextInt(RETARGET_CHANCE) != 0
                && current.y() > serverWorld().minY()) {
            final BlockState state = BlockView.blockAt(serverWorld(), current.blockX(), current.blockY(), current.blockZ());
            if (state != null && state.isAir()) {
                return current;
            }
        }

        final BlockPosition next = Position.block(
                self.blockX() + random.nextInt(WANDER_HORIZONTAL_RANGE) - random.nextInt(WANDER_HORIZONTAL_RANGE),
                self.blockY() + random.nextInt(WANDER_VERTICAL_RANGE) - WANDER_VERTICAL_OFFSET,
                self.blockZ() + random.nextInt(WANDER_HORIZONTAL_RANGE) - random.nextInt(WANDER_HORIZONTAL_RANGE));
        wanderTarget = next;
        return next;
    }

    private static boolean isWithin(final BlockPosition position, final Location location) {
        final double dx = position.x() + 0.5 - location.x();
        final double dy = position.y() + 0.5 - location.y();
        final double dz = position.z() + 0.5 - location.z();
        return dx * dx + dy * dy + dz * dz < TARGET_REACHED_DISTANCE * TARGET_REACHED_DISTANCE;
    }

    /**
     * {@return {@code true} if a block conducts redstone power}: the blocks a bat hangs from
     */
    private boolean isConductor(final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(serverWorld(), x, y, z);
        return state != null && BlockSupport.isConductor(server().registries().blockStates().resolve(state));
    }

    /**
     * Burns this bat while it is in lava.
     */
    private void tickLava() {
        final Location self = location();
        final BlockState state = BlockView.blockAt(serverWorld(),
                (int) Math.floor(self.x()), (int) Math.floor(self.y()), (int) Math.floor(self.z()));
        if (state == null || !state.name().equals(BlockTypeKeys.LAVA)) {
            return;
        }
        igniteForTicks(LAVA_FIRE_TICKS);
        if (damage(DamageSourceFactoryImpl.INSTANCE.lava(), LAVA_DAMAGE)) {
            playSound(SoundEvents.GENERIC_BURN, LAVA_BURN_VOLUME, 2.0f + ThreadLocalRandom.current().nextFloat() * 0.4f);
        }
    }

    @Override
    public void onHurt(final DamageSource source, final float amount) {
        setResting(false);
    }

    @Override
    protected void onDeath() {
        setResting(false);
        super.onDeath();
    }

    @Override
    protected Sound.@Nullable Type ambientSound() {
        return resting && ThreadLocalRandom.current().nextInt(RESTING_SILENCE_CHANCE) != 0 ? null : SoundEvents.BAT_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    protected float soundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    protected float voicePitch() {
        return super.voicePitch() * VOICE_PITCH;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putByte("BatFlags", resting ? FLAG_RESTING : 0);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        setResting((tag.getByte("BatFlags", (byte) 0) & FLAG_RESTING) != 0);
    }

    private ClientboundSetEntityMetadataPacket flagsPacket() {
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_BAT_FLAGS, resting ? FLAG_RESTING : 0));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(flagsPacket());
    }
}
