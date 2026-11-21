package fr.euphyllia.fidorial.server.entity.mob.monster.enderman;

import fr.euphyllia.fidorial.server.entity.AbstractEntity;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.ai.PathPenalty;
import fr.euphyllia.fidorial.server.entity.ai.Targeting;
import fr.euphyllia.fidorial.server.entity.ai.goal.FloatGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtPlayerGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.MeleeAttackGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomLookAroundGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.ResetUniversalAngerTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.TargetGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMonster;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.mob.AbstractPathfinderMob;
import fr.euphyllia.fidorial.server.entity.mob.AngerableMob;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.entity.mob.PersistentAnger;
import fr.euphyllia.fidorial.server.entity.mob.monster.endermite.EndermiteImpl;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.events.mob.MobBlockChanges;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundLevelEventPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.block.BlockItems;
import fr.euphyllia.fidorial.server.world.block.BlockSupport;
import fr.euphyllia.fidorial.server.world.block.BlockSurvival;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.chunk.BlockStates;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.Entity;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.LivingEntity;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.entity.mob.monster.Enderman;
import fr.fidorial.entity.player.Player;
import fr.fidorial.inventory.EquipmentSlot;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.BlockPosition;
import fr.fidorial.math.Location;
import fr.fidorial.math.Position;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.block.BlockData;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.farming.FarmingWorld;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * The enderman: a tall monster that leaves everyone alone until provoked. Looking it in the eyes,
 * unless through a carved pumpkin, or hurting it, makes it angry for 20 to 39 seconds; it then
 * screams, hurries and teleports around the one it is angry at. It also goes after the endermites.
 *
 * <p>It teleports away from what hurts it without anyone to blame, such as water, which hurts it,
 * and dodges the projectiles by teleporting. Under the open sky in daylight it now and then
 * teleports somewhere else, forgetting its target. When mobs may change the world, it picks up
 * the blocks of the {@code minecraft:enderman_holdable} tag around it and, now and then, puts the
 * one it carries down; it drops that block when it dies, and does not despawn while it carries
 * one.</p>
 */
public final class EndermanImpl extends AbstractMonster implements AngerableMob, Enderman {

    public static final float MAX_HEALTH = 40f;

    private static final double MOVEMENT_SPEED = 0.3;
    /**
     * What an enderman with a target adds to its movement speed.
     */
    private static final double ATTACKING_SPEED_BONUS = 0.15;
    private static final float ATTACK_DAMAGE = 7f;
    private static final double FOLLOW_RANGE = 64.0;
    private static final double FOLLOW_RANGE_SPREAD = 0.11485;

    private static final double HALF_WIDTH = 0.3;
    private static final double HEIGHT = 2.9;
    private static final double EYE_HEIGHT = 2.55;
    private static final double STEP_HEIGHT = 1.0;

    private static final int ANGER_MIN_TICKS = 20 * 20;
    private static final int ANGER_MAX_TICKS = 39 * 20;
    /**
     * For how long after it took a target an enderman stays where it is in daylight.
     */
    private static final int DAYLIGHT_CALM_TICKS = 600;
    private static final float BRIGHT = 0.5f;

    private static final double TELEPORT_SPREAD = 64.0;
    private static final int TELEPORT_HEIGHT = 64;
    private static final int TELEPORT_ATTEMPTS = 64;
    private static final double TELEPORT_TOWARDS_SPREAD = 8.0;
    private static final int TELEPORT_TOWARDS_HEIGHT = 16;
    private static final double TELEPORT_TOWARDS_DISTANCE = 16.0;
    /**
     * How far apart, in blocks along each axis, the two ends of the particles of a teleport may be.
     */
    private static final int TELEPORT_PARTICLES_RANGE = 127;
    private static final int NO_DODGE_CHANCE = 10;

    /**
     * How wide the cone where a player looks has to be to take in the eyes of an enderman, one
     * block away.
     */
    private static final double STARE_CONE = 0.025;
    private static final float WATER_DAMAGE = 1f;

    private static final int MD_CARRIED_BLOCK = 16;
    private static final int MD_CREEPY = 17;
    private static final int MD_STARED_AT = 18;
    private static final int TYPE_OPTIONAL_BLOCK_STATE = 15;
    private static final int NO_BLOCK_STATE = 0;

    private static final double PLAYER_HEIGHT = 1.8;
    /**
     * The size of the entities that are neither mobs nor players, such as a dropped item.
     */
    private static final double SMALL_ENTITY_SIZE = 0.25;
    private static final String SNOWY = "snowy";

    private static final String TAG_CARRIED_BLOCK = "carriedBlockState";
    private static final String TAG_NAME = "Name";
    private static final String TAG_PROPERTIES = "Properties";

    private static final Key RANDOM_SPAWN_BONUS = Key.key("random_spawn_bonus");

    /**
     * Water stops an enderman: it never paths through it.
     */
    private static final PathPenalty AVOIDS_WATER = (world, x, y, z) -> {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && state.isWater() ? PathPenalty.BLOCKED : 0.0;
    };

    private final MobAttribute followRangeAttribute = new MobAttribute(Key.key("follow_range"), FOLLOW_RANGE);
    private final PersistentAnger anger = new PersistentAnger(this, ANGER_MIN_TICKS, ANGER_MAX_TICKS);

    private @Nullable BlockState carriedBlock;
    private boolean creepy;
    private boolean staredAt;
    private int age;
    private int targetChangeTime;

    /**
     * Creates an enderman as one that spawns, with the random traits of one.
     */
    public EndermanImpl(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.ENDERMAN, location, MAX_HEALTH);
        navigation.setPathPenalty(AVOIDS_WATER);
        goals.add(new FloatGoal(this, 0));
        goals.add(new FreezeWhenLookedAtGoal());
        goals.add(new MeleeAttackGoal(this, 2, 1.0, false));
        goals.add(new RandomStrollGoal(this, 7, this::movementSpeed));
        goals.add(new LookAtPlayerGoal(this, 8, 8.0));
        goals.add(new RandomLookAroundGoal(this, 8));
        goals.add(new LeaveBlockGoal());
        goals.add(new TakeBlockGoal());
        goals.add(new LookForPlayerGoal());
        goals.add(new HurtByTargetGoal(this, 2));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, EndermiteImpl.class, true));
        goals.add(new ResetUniversalAngerTargetGoal<>(this, 4, false));
        // every mob that spawns notices its targets from a little closer or further than the others
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double first = random.nextDouble();
        final double second = random.nextDouble();
        followRangeAttribute.put(RANDOM_SPAWN_BONUS, FOLLOW_RANGE_SPREAD * (first - second),
                MobAttribute.Operation.ADD_MULTIPLIED_BASE);
    }

    public EndermanImpl(final int entityId, final UUID uuid, final EntityType type, final Location location, final float maxHealth, @Nullable final BlockState carriedBlock, final boolean creepy, final boolean staredAt, final int age, final int targetChangeTime) {
        super(entityId, uuid, type, location, maxHealth);
        this.carriedBlock = carriedBlock;
        this.creepy = creepy;
        this.staredAt = staredAt;
        this.age = age;
        this.targetChangeTime = targetChangeTime;
    }

    /**
     * {@return {@code true} if an enderman may spawn on a block}: as any monster, in the dark on solid
     * ground, unless the world is peaceful
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkMonsterSpawnRules(world, x, y, z);
    }

    @Override
    public PersistentAnger anger() {
        return anger;
    }

    /**
     * {@return the block this enderman carries, {@code null} if it carries none}
     */
    public @Nullable BlockState carriedBlockState() {
        return carriedBlock;
    }

    @Override
    public Optional<BlockData> carriedBlock() {
        final BlockState carried = carriedBlock;
        return carried == null ? Optional.empty() : Optional.ofNullable(server().registries().blockStates().resolve(carried));
    }

    @Override
    public void setCarriedBlock(final @Nullable BlockData block) {
        setCarriedBlockState(block == null ? null : server().registries().blockStates().toBlockState(block));
    }

    /**
     * Makes this enderman carry a block, or none.
     *
     * @param block the block, {@code null} for none
     */
    public void setCarriedBlockState(final @Nullable BlockState block) {
        final BlockState carried = block == null || block.isAir() ? null : block;
        if (Objects.equals(carried, carriedBlock)) {
            return;
        }
        carriedBlock = carried;
        sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(), carriedBlockEntry()));
    }

    /**
     * {@return {@code true} while this enderman has a target}: it then opens its mouth and screams
     */
    @Override
    public boolean isScreaming() {
        return creepy;
    }

    /**
     * {@return {@code true} once a player provoked this enderman by staring at it}, until it gives
     * up its target; the client of that player plays the sound of the stare
     */
    @Override
    public boolean hasBeenStaredAt() {
        return staredAt;
    }

    @Override
    public void tick(final long currentTick) {
        if (isRemoved()) {
            return;
        }
        age++;
        if (!isDead()) {
            anger.update(true);
            teleportAwayFromDaylight();
        }
        super.tick(currentTick);
        if (!isRemoved() && !isDead() && (isInWater() || isInRain())) {
            damage(DamageSource.drown(), WATER_DAMAGE);
        }
    }

    @Override
    protected void onTargetSet(final @Nullable AbstractLivingEntity target) {
        if (target == null) {
            targetChangeTime = 0;
            setCreepy(false);
            setStaredAt(false);
        } else {
            targetChangeTime = age;
            setCreepy(true);
        }
    }

    /**
     * Dodges the projectiles by teleporting; anything else hurts it, and it teleports away from what
     * hurts it with no living entity to blame nine times out of ten.
     */
    @Override
    public boolean takeHit(final DamageSource source, final float amount, final double knockback, final BooleanSupplier hurt) {
        if (server().registries().registry(RegistryKey.DAMAGE_TYPE).isTagged(source.type(), DamageTypeTagKeys.IS_PROJECTILE)) {
            repeatedlyTryToTeleport();
            return false;
        }
        final boolean hurtIt = hurt.getAsBoolean();
        if (!(source.causingEntity() instanceof LivingEntity) && ThreadLocalRandom.current().nextInt(NO_DODGE_CHANCE) != 0) {
            teleportRandomly();
        }
        return hurtIt;
    }

    @Override
    public float walkTargetValue(final int x, final int y, final int z) {
        return 0.0f;
    }

    @Override
    protected boolean requiresCustomPersistence() {
        return carriedBlock != null;
    }

    @Override
    protected boolean canRandomlyTeleportTo(final double x, final double y, final double z) {
        final BlockState below = BlockView.blockAt(serverWorld(),
                (int) Math.floor(x), (int) Math.floor(y) - 1, (int) Math.floor(z));
        return below == null || !below.isWater();
    }

    @Override
    public double movementSpeed() {
        return target() != null ? MOVEMENT_SPEED + ATTACKING_SPEED_BONUS : MOVEMENT_SPEED;
    }

    @Override
    public float attackDamage() {
        return ATTACK_DAMAGE;
    }

    @Override
    protected double defaultFollowRange() {
        return followRangeAttribute.value();
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
    protected double stepHeight() {
        return STEP_HEIGHT;
    }

    @Override
    protected Sound.Type ambientSound() {
        return creepy ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one ender pearl per level
        final int pearls = ThreadLocalRandom.current().nextInt(2);
        if (pearls > 0) {
            dropLootItem(ItemStack.of(ItemKeys.ENDER_PEARL, pearls));
        }
        final BlockState carried = carriedBlock;
        if (carried != null) {
            // the block comes off as if broken with silk touch: whole
            dropLootItem(ItemStack.of(BlockItems.item(carried.name()), 1));
        }
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        MobAttribute.save(tag, followRangeAttribute);
        final BlockState carried = carriedBlock;
        if (carried != null) {
            final CompoundBinaryTag.Builder block = CompoundBinaryTag.builder().putString(TAG_NAME, carried.name().key().asString());
            if (!carried.properties().isEmpty()) {
                final CompoundBinaryTag.Builder properties = CompoundBinaryTag.builder();
                carried.properties().forEach(properties::putString);
                block.put(TAG_PROPERTIES, properties.build());
            }
            tag.put(TAG_CARRIED_BLOCK, block.build());
        }
        anger.save(tag);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        // an enderman read back keeps nothing of what was rolled when it was created
        followRangeAttribute.remove(RANDOM_SPAWN_BONUS);
        super.loadAdditional(tag);
        MobAttribute.load(tag, followRangeAttribute);
        setCarriedBlockState(tag.get(TAG_CARRIED_BLOCK) instanceof final CompoundBinaryTag block ? readBlockState(block) : null);
        anger.load(tag);
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(ClientboundSetEntityMetadataPacket.of(entityId(),
                carriedBlockEntry(),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_CREEPY, creepy),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_STARED_AT, staredAt)));
    }

    private ClientboundSetEntityMetadataPacket.Entry carriedBlockEntry() {
        final BlockState carried = carriedBlock;
        final int id = carried == null ? NO_BLOCK_STATE : server().registries().blockStates().networkId(carried);
        return ClientboundSetEntityMetadataPacket.Entry.raw(MD_CARRIED_BLOCK, TYPE_OPTIONAL_BLOCK_STATE, buffer -> buffer.writeVarInt(id));
    }

    private void setCreepy(final boolean creepy) {
        if (this.creepy != creepy) {
            this.creepy = creepy;
            sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                    ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_CREEPY, creepy)));
        }
    }

    private void setStaredAt(final boolean staredAt) {
        if (this.staredAt != staredAt) {
            this.staredAt = staredAt;
            sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                    ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_STARED_AT, staredAt)));
        }
    }

    /**
     * {@return {@code true} if a player stares at the eyes of this enderman}, unless the player wears
     * a carved pumpkin or another item of the {@code minecraft:gaze_disguise_equipment} tag
     */
    @Override
    public boolean isBeingStaredBy(final Player player) {
        if (!(player instanceof final ServerPlayer starer)) {
            return false;
        }
        final ItemStack head = starer.equipment(EquipmentSlot.HEAD);
        if (!head.isEmpty() && server().registries().registry(RegistryKey.ITEM).isTagged(head.id(), ItemTagKeys.GAZE_DISGUISE_EQUIPMENT)) {
            return false;
        }
        return Targeting.isLookedAt(starer, this, STARE_CONE, location().y() + eyeHeight());
    }

    /**
     * Under the open sky in daylight, once it has had its target for 30 seconds, now and then the
     * enderman gives it up and teleports somewhere else.
     */
    private void teleportAwayFromDaylight() {
        final ServerWorld world = serverWorld();
        if (!Brightness.isDay(world) || age < targetChangeTime + DAYLIGHT_CALM_TICKS) {
            return;
        }
        final float brightness = brightness();
        final Location at = location();
        if (brightness > BRIGHT
                && Brightness.canSeeSky(world, (int) Math.floor(at.x()), (int) Math.floor(at.y()), (int) Math.floor(at.z()))
                && ThreadLocalRandom.current().nextFloat() * 30.0f < (brightness - 0.4f) * 2.0f) {
            setTarget(null);
            teleportRandomly();
        }
    }

    private boolean isInRain() {
        final Location at = location();
        final int x = (int) Math.floor(at.x());
        final int z = (int) Math.floor(at.z());
        final FarmingWorld farming = server().worlds().farming().world(serverWorld());
        return farming.isRainingAt(Position.block(x, (int) Math.floor(at.y()), z))
                || farming.isRainingAt(Position.block(x, (int) Math.floor(at.y() + height()), z));
    }

    /**
     * Tries to teleport somewhere within 32 blocks across and up or down.
     */
    @Override
    public boolean teleportRandomly() {
        if (isDead() || isRemoved()) {
            return false;
        }
        final Location at = location();
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        return teleportAt(at.x() + (random.nextDouble() - 0.5) * TELEPORT_SPREAD,
                at.y() + (random.nextInt(TELEPORT_HEIGHT) - TELEPORT_HEIGHT / 2),
                at.z() + (random.nextDouble() - 0.5) * TELEPORT_SPREAD);
    }

    /**
     * Tries to teleport near an entity: 16 blocks closer to it, give or take a little.
     */
    @Override
    public boolean teleportTowards(final Entity entity) {
        final Location at = location();
        final Location other = entity.location();
        double dx = at.x() - other.x();
        double dy = at.y() + height() * 0.5 - (other.y() + AbstractMovingMob.eyeHeightOf(entity));
        double dz = at.z() - other.z();
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length >= 1.0E-4) {
            dx /= length;
            dy /= length;
            dz /= length;
        } else {
            dx = 0.0;
            dy = 0.0;
            dz = 0.0;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        return teleportAt(at.x() + (random.nextDouble() - 0.5) * TELEPORT_TOWARDS_SPREAD - dx * TELEPORT_TOWARDS_DISTANCE,
                at.y() + (random.nextInt(TELEPORT_TOWARDS_HEIGHT) - TELEPORT_TOWARDS_HEIGHT / 2) - dy * TELEPORT_TOWARDS_DISTANCE,
                at.z() + (random.nextDouble() - 0.5) * TELEPORT_TOWARDS_SPREAD - dz * TELEPORT_TOWARDS_DISTANCE);
    }

    /**
     * Tries to teleport to a point, landing on the first block below it it may stand on, out of the
     * water and away from the blocks of the {@code minecraft:enderman_does_not_teleport_to} tag;
     * the particles of the teleport go from where it was to where it lands, and it is heard at both
     * ends.
     */
    public boolean teleportAt(final double x, final double y, final double z) {
        final Location from = location();
        if (!randomTeleport(x, y, z, true, state -> BlockView.isTagged(BlockTypeTagKeys.ENDERMAN_DOES_NOT_TELEPORT_TO, state))) {
            return false;
        }
        final ServerWorld world = serverWorld();
        server().debug().gameEvents().emit(world, GameEventKeys.TELEPORT, from);
        final Location to = location();
        final BlockPosition fromBlock = Position.block((int) Math.floor(from.x()), (int) Math.floor(from.y()), (int) Math.floor(from.z()));
        final int packed = packDifference(fromBlock, (int) Math.floor(to.x()), (int) Math.floor(to.y()), (int) Math.floor(to.z()));
        world.sendToChunkViewers(fromBlock.chunkX(), fromBlock.chunkZ(),
                new ClientboundLevelEventPacket(ClientboundLevelEventPacket.ENDERMAN_TELEPORT, fromBlock, packed, false));
        world.sendToChunkViewers(fromBlock.chunkX(), fromBlock.chunkZ(), new ClientboundSoundPacket(
                Sound.sound(SoundEvents.ENDERMAN_TELEPORT, soundSource(), 1.0f, 1.0f), from.x(), from.y(), from.z()));
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return true;
    }

    private void repeatedlyTryToTeleport() {
        for (int attempt = 0; attempt < TELEPORT_ATTEMPTS; attempt++) {
            if (teleportRandomly()) {
                return;
            }
        }
    }

    /**
     * {@return where a block lies from another, each axis clamped to 127 blocks and shifted by 127
     * into a byte}
     */
    private static int packDifference(final BlockPosition from, final int x, final int y, final int z) {
        final int dx = Math.clamp(x - from.blockX(), -TELEPORT_PARTICLES_RANGE, TELEPORT_PARTICLES_RANGE);
        final int dy = Math.clamp(y - from.blockY(), -TELEPORT_PARTICLES_RANGE, TELEPORT_PARTICLES_RANGE);
        final int dz = Math.clamp(z - from.blockZ(), -TELEPORT_PARTICLES_RANGE, TELEPORT_PARTICLES_RANGE);
        return ((dx + TELEPORT_PARTICLES_RANGE) & 0xFF) << 16
                | ((dy + TELEPORT_PARTICLES_RANGE) & 0xFF) << 8
                | ((dz + TELEPORT_PARTICLES_RANGE) & 0xFF);
    }

    private boolean mobGriefing() {
        return serverWorld().gameRuleValues().getBoolean(GameRuleKeys.MOB_GRIEFING);
    }

    /**
     * {@return {@code true} if this enderman may pick blocks up and put them down}: the game rule and
     * the settings of its world both allow it
     */
    private boolean movesBlocks() {
        return mobGriefing() && serverWorld().configuration().mobs().enderman().movesBlocks();
    }

    private boolean owns(final BlockPosition position) {
        return server().workers().regionizer().isOwnedByCurrentThread(world().key(), position.chunk());
    }

    private static @Nullable BlockState readBlockState(final CompoundBinaryTag tag) {
        final String name = tag.getString(TAG_NAME);
        if (name.isEmpty()) {
            return null;
        }
        final TypedKey<BlockType> block = TypedKey.withRegistry(RegistryKey.BLOCK, name);
        final CompoundBinaryTag properties = tag.getCompound(TAG_PROPERTIES);
        if (properties.keySet().isEmpty()) {
            return BlockState.withoutProperties(block);
        }
        final Map<String, String> values = new TreeMap<>();
        for (final String key : properties.keySet()) {
            final BinaryTag value = properties.get(key);
            if (value instanceof final StringBinaryTag string) {
                values.put(key, string.value());
            }
        }
        return BlockState.withProperties(block, values);
    }

    /**
     * Makes an enderman a player stares at from within 16 blocks, the one it goes after, stop and
     * stare back.
     */
    private final class FreezeWhenLookedAtGoal implements Goal {

        private static final double MAX_DISTANCE_SQ = 256.0;
        private static final Set<Flag> FLAGS = Set.of(Flag.JUMP, Flag.MOVE);

        private @Nullable ServerPlayer starer;

        @Override
        public int priority() {
            return 1;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            starer = target() instanceof final ServerPlayer player
                    && distanceSqTo(player) <= MAX_DISTANCE_SQ && isBeingStaredBy(player) ? player : null;
            return starer != null;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void start() {
            navigation.stop();
        }

        @Override
        public void tick() {
            final ServerPlayer player = starer;
            if (player != null) {
                final Location at = player.location();
                setLookTarget(at.x(), at.y() + player.eyeHeight(), at.z());
            }
        }
    }

    /**
     * Makes the enderman go after a player that stares at it, or that it is angry at: it gets ready
     * for a quarter of a second, staring back, then attacks. While it goes after a player, it
     * teleports away when the player stares at it from closer than 4 blocks, and towards the
     * player when it stayed more than 16 blocks away for a second and a half.
     */
    private final class LookForPlayerGoal extends TargetGoal {

        private static final int AGGRO_DELAY = 5;
        private static final int TELEPORT_TOWARDS_DELAY = 30;
        private static final double TOO_CLOSE_SQ = 16.0;
        private static final double TOO_FAR_SQ = 256.0;
        private static final float STARE_BACK_SPEED = 10.0f;

        private @Nullable ServerPlayer pending;
        private int aggroTime;
        private int teleportTime;

        LookForPlayerGoal() {
            super(EndermanImpl.this, 1, false);
        }

        private boolean isAngerInducing(final ServerPlayer player) {
            return isBeingStaredBy(player) || anger.isAngryAt(player);
        }

        @Override
        public boolean canStart() {
            final double range = followRange();
            final Location at = location();
            ServerPlayer nearest = null;
            double nearestSq = Double.MAX_VALUE;
            for (final ServerPlayer player : server().players().online()) {
                if (!Targeting.test(EndermanImpl.this, player, range, true, true) || !isAngerInducing(player)) {
                    continue;
                }
                final Location other = player.location();
                final double dx = other.x() - at.x();
                final double dy = other.y() - at.y();
                final double dz = other.z() - at.z();
                final double distanceSq = dx * dx + dy * dy + dz * dz;
                if (distanceSq < nearestSq) {
                    nearestSq = distanceSq;
                    nearest = player;
                }
            }
            pending = nearest;
            return pending != null;
        }

        @Override
        public void start() {
            aggroTime = AGGRO_DELAY;
            teleportTime = 0;
            setStaredAt(true);
        }

        @Override
        public void stop() {
            pending = null;
            super.stop();
        }

        @Override
        public boolean shouldContinue() {
            final ServerPlayer player = pending;
            if (player != null) {
                if (!isAngerInducing(player)) {
                    return false;
                }
                setLookTarget(player, STARE_BACK_SPEED, STARE_BACK_SPEED);
                return true;
            }
            // once it attacks, it keeps after the player wherever they go
            if (targetMob != null && Targeting.canAttack(EndermanImpl.this, targetMob)) {
                return true;
            }
            return super.shouldContinue();
        }

        @Override
        public void tick() {
            if (target() == null) {
                targetMob = null;
            }
            final ServerPlayer player = pending;
            if (player != null) {
                if (--aggroTime <= 0) {
                    pending = null;
                    setTarget(player);
                    targetMob = target();
                    super.start();
                }
                return;
            }
            if (targetMob instanceof final ServerPlayer chased) {
                if (isBeingStaredBy(chased)) {
                    if (distanceSqTo(chased) < TOO_CLOSE_SQ) {
                        teleportRandomly();
                    }
                    teleportTime = 0;
                } else if (distanceSqTo(chased) > TOO_FAR_SQ && teleportTime++ >= TELEPORT_TOWARDS_DELAY
                        && teleportTowards(chased)) {
                    teleportTime = 0;
                }
            }
        }
    }

    /**
     * When mobs may change the world, makes an enderman with empty hands now and then pick up a
     * block of the {@code minecraft:enderman_holdable} tag around it, up to 2 blocks across and 3
     * up, that nothing stands in front of.
     */
    private final class TakeBlockGoal implements Goal {

        private static final int CHANCE = 20;
        private static final double REACH_ACROSS = 2.0;
        private static final double REACH_UP = 3.0;

        @Override
        public int priority() {
            return 11;
        }

        @Override
        public Set<Flag> flags() {
            return Set.of();
        }

        @Override
        public boolean canStart() {
            return carriedBlock == null && movesBlocks() && ThreadLocalRandom.current().nextInt(CHANCE) == 0;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final Location at = location();
            final int x = (int) Math.floor(at.x() - REACH_ACROSS + random.nextDouble() * REACH_ACROSS * 2.0);
            final int y = (int) Math.floor(at.y() + random.nextDouble() * REACH_UP);
            final int z = (int) Math.floor(at.z() - REACH_ACROSS + random.nextDouble() * REACH_ACROSS * 2.0);
            final BlockPosition position = Position.block(x, y, z);
            if (!owns(position)) {
                return;
            }
            final ServerWorld world = serverWorld();
            final BlockState state = BlockView.blockAt(world, x, y, z);
            if (state == null || !BlockView.isTagged(BlockTypeTagKeys.ENDERMAN_HOLDABLE, state)
                    || !isReachable((int) Math.floor(at.x()), (int) Math.floor(at.z()), y, x, z)) {
                return;
            }
            if (MobBlockChanges.change(EndermanImpl.this, world, position, state, BlockState.withoutProperties(BlockTypeKeys.AIR))) {
                server().debug().gameEvents().emit(world, GameEventKeys.BLOCK_DESTROY, position);
                setCarriedBlockState(defaultState(state.name()));
            }
        }

        /**
         * {@return {@code true} if a ray across the middle of the blocks at a height, from the column
         * of the enderman to a block, meets no other block on the way}; any block but air, the
         * fluids and the light blocks stops it
         */
        private boolean isReachable(final int fromX, final int fromZ, final int y, final int toX, final int toZ) {
            final double startX = fromX + 0.5;
            final double startZ = fromZ + 0.5;
            final double dx = toX + 0.5 - startX;
            final double dz = toZ + 0.5 - startZ;
            final int stepX = Integer.signum(toX - fromX);
            final int stepZ = Integer.signum(toZ - fromZ);
            final double deltaX = stepX == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dx);
            final double deltaZ = stepZ == 0 ? Double.MAX_VALUE : Math.abs(1.0 / dz);
            // the ray starts in the middle of its block: the first border on each axis is half a block away
            double nextX = deltaX * 0.5;
            double nextZ = deltaZ * 0.5;
            int x = fromX;
            int z = fromZ;
            final ServerWorld world = serverWorld();
            while (true) {
                if (x == toX && z == toZ) {
                    return true;
                }
                final BlockState state = BlockView.blockAt(world, x, y, z);
                if (state == null || stopsRay(state)) {
                    return false;
                }
                if (nextX < nextZ) {
                    x += stepX;
                    nextX += deltaX;
                } else {
                    z += stepZ;
                    nextZ += deltaZ;
                }
            }
        }

        private static boolean stopsRay(final BlockState state) {
            return !state.isAir() && !state.name().equals(BlockTypeKeys.WATER) && !state.name().equals(BlockTypeKeys.LAVA)
                    && !state.name().equals(BlockTypeKeys.BUBBLE_COLUMN) && !state.name().equals(BlockTypeKeys.LIGHT);
        }
    }

    /**
     * When mobs may change the world, makes an enderman that carries a block put it down now and
     * then, once in 2000 ticks on average: within a block across and 2 up, in the air, on a full
     * block other than bedrock, where the block can stand and nothing else is.
     */
    private final class LeaveBlockGoal implements Goal {

        private static final int CHANCE = 2000;
        private static final double REACH_ACROSS = 1.0;
        private static final double REACH_UP = 2.0;

        @Override
        public int priority() {
            return 10;
        }

        @Override
        public Set<Flag> flags() {
            return Set.of();
        }

        @Override
        public boolean canStart() {
            return carriedBlock != null && movesBlocks() && ThreadLocalRandom.current().nextInt(CHANCE) == 0;
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void tick() {
            final BlockState carried = carriedBlock;
            if (carried == null) {
                return;
            }
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final Location at = location();
            final int x = (int) Math.floor(at.x() - REACH_ACROSS + random.nextDouble() * REACH_ACROSS * 2.0);
            final int y = (int) Math.floor(at.y() + random.nextDouble() * REACH_UP);
            final int z = (int) Math.floor(at.z() - REACH_ACROSS + random.nextDouble() * REACH_ACROSS * 2.0);
            final BlockPosition position = Position.block(x, y, z);
            if (!owns(position)) {
                return;
            }
            final ServerWorld world = serverWorld();
            final BlockState placed = withSnowAbove(carried, world, x, y, z);
            final BlockState there = BlockView.blockAt(world, x, y, z);
            if (there != null && canPlace(world, position, placed)
                    && MobBlockChanges.change(EndermanImpl.this, world, position, there, placed)) {
                server().debug().gameEvents().emit(world, GameEventKeys.BLOCK_PLACE, position);
                setCarriedBlockState(null);
            }
        }

        private boolean canPlace(final ServerWorld world, final BlockPosition position, final BlockState placed) {
            final BlockState there = BlockView.blockAt(world, position.blockX(), position.blockY(), position.blockZ());
            final BlockState below = BlockView.blockAt(world, position.blockX(), position.blockY() - 1, position.blockZ());
            return there != null && there.isAir()
                    && below != null && !below.isAir() && !below.name().equals(BlockTypeKeys.BEDROCK)
                    && BlockSupport.isCollisionShapeFullBlock(server().registries().blockStates().resolve(below))
                    && BlockSurvival.canSurvive(world, position, placed)
                    && isEmpty(world, position);
        }

        /**
         * {@return {@code true} if no entity but this enderman takes up room in a block}
         */
        private boolean isEmpty(final ServerWorld world, final BlockPosition position) {
            final AtomicBoolean found = new AtomicBoolean(false);
            world.entityManager().forEachNear(position.chunk(), 2.0, entity -> {
                if (!found.get() && entity != EndermanImpl.this && !entity.isRemoved() && overlaps(entity, position)) {
                    found.set(true);
                }
            });
            return !found.get();
        }

        private static boolean overlaps(final AbstractEntity entity, final BlockPosition position) {
            final Location at = entity.location();
            final double half;
            final double height;
            if (entity instanceof final AbstractMovingMob mob) {
                half = mob.width() / 2.0;
                height = mob.height();
            } else if (entity instanceof final AbstractLivingEntity living) {
                half = AbstractPathfinderMob.halfWidthOf(living);
                height = PLAYER_HEIGHT;
            } else {
                half = SMALL_ENTITY_SIZE / 2.0;
                height = SMALL_ENTITY_SIZE;
            }
            return at.x() + half > position.blockX() && at.x() - half < position.blockX() + 1.0
                    && at.y() + height > position.blockY() && at.y() < position.blockY() + 1.0
                    && at.z() + half > position.blockZ() && at.z() - half < position.blockZ() + 1.0;
        }
    }

    /**
     * {@return a block with the properties it takes from the blocks around}: grass, podzol and
     * mycelium are snowy under snow
     */
    private static BlockState withSnowAbove(final BlockState block, final ServerWorld world, final int x, final int y, final int z) {
        if (!block.properties().containsKey(SNOWY)) {
            return block;
        }
        final BlockState above = BlockView.blockAt(world, x, y + 1, z);
        final boolean snowy = above != null && BlockView.isTagged(BlockTypeTagKeys.SNOW, above);
        final Map<String, String> properties = new TreeMap<>(block.properties());
        properties.put(SNOWY, Boolean.toString(snowy));
        return BlockState.withProperties(block.name(), properties);
    }

    /**
     * {@return the default state of a block}
     */
    private static BlockState defaultState(final TypedKey<BlockType> block) {
        final BlockState state = BlockStates.defaultStateOf(block);
        return state != null ? state : BlockState.withoutProperties(block);
    }
}
