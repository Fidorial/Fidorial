package fr.euphyllia.fidorial.server.entity.mob.monster.creeper;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.goal.AvoidEntityGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FloatGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtPlayerGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.MeleeAttackGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomLookAroundGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMonster;
import fr.euphyllia.fidorial.server.entity.mob.creature.Cat;
import fr.euphyllia.fidorial.server.entity.mob.creature.Goat;
import fr.euphyllia.fidorial.server.entity.mob.creature.Ocelot;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.world.Explosion;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.ItemEntity;
import fr.fidorial.entity.ai.Goal;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemDefaults;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.EntityTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The creeper: it walks up to the players it sees and, once within 3 blocks, swells for a second
 * and a half then explodes, unless its target got more than 7 blocks away or out of sight. It keeps
 * away from cats and ocelots, explodes on its own once lit with flint and steel or a fire charge,
 * and, charged by lightning, explodes twice as far and leaves the head of the mobs it kills.
 */
public final class Creeper extends AbstractMonster {

    public static final float MAX_HEALTH = 20f;
    private static final ComponentLogger LOGGER = ComponentLogger.logger(Creeper.class);

    private static final double MOVEMENT_SPEED = 0.25;
    private static final double STROLL_SPEED_MODIFIER = 0.8;
    private static final double AVOID_DISTANCE = 6.0;
    private static final double AVOID_WALK_SPEED_MODIFIER = 1.0;
    private static final double AVOID_RUN_SPEED_MODIFIER = 1.2;

    private static final short DEFAULT_FUSE = 30;
    private static final byte DEFAULT_EXPLOSION_RADIUS = 3;
    private static final float POWERED_EXPLOSION_MULTIPLIER = 2.0f;
    private static final float PRIMED_SOUND_PITCH = 0.5f;

    private static final int LOOT_MAX_GUNPOWDER = 2;

    private static final int MD_SWELL_DIR = 16;
    private static final int MD_POWERED = 17;
    private static final int MD_IGNITED = 18;
    private int swell;
    private int swellDir = -1;
    private int maxSwell = DEFAULT_FUSE;
    private int explosionRadius = DEFAULT_EXPLOSION_RADIUS;
    private boolean powered;
    private boolean ignited;
    private boolean droppedSkulls;

    public Creeper(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.CREEPER, location, MAX_HEALTH);
        goals.add(new FloatGoal(this, 1));
        goals.add(new SwellGoal(2));
        goals.add(new AvoidEntityGoal<>(this, 3, Ocelot.class, AVOID_DISTANCE, AVOID_WALK_SPEED_MODIFIER, AVOID_RUN_SPEED_MODIFIER));
        goals.add(new AvoidEntityGoal<>(this, 3, Cat.class, AVOID_DISTANCE, AVOID_WALK_SPEED_MODIFIER, AVOID_RUN_SPEED_MODIFIER));
        goals.add(new MeleeAttackGoal(this, 4, 1.0, false));
        goals.add(new RandomStrollGoal(this, 5, () -> movementSpeed() * STROLL_SPEED_MODIFIER));
        goals.add(new LookAtPlayerGoal(this, 6, 8.0));
        goals.add(new RandomLookAroundGoal(this, 6));
        goals.add(new NearestAttackableTargetGoal<>(this, 1, ServerPlayer.class, true));
        // a goat that rams a creeper is not fought back
        goals.add(new HurtByTargetGoal(this, 2).ignoringDamageFrom(Goat.class));
    }

    /**
     * {@return {@code true} if creepers may spawn on a block}: as any monster, on solid ground in the
     * dark
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkMonsterSpawnRules(world, x, y, z);
    }

    @Override
    public double movementSpeed() {
        return MOVEMENT_SPEED;
    }

    /**
     * A creeper does not hurt what it walks into: only its explosion does.
     */
    @Override
    public boolean attack(final AbstractLivingEntity target) {
        return true;
    }

    @Override
    public void tick(final long currentTick) {
        if (!isRemoved() && !isDead()) {
            tickSwell();
            if (isRemoved()) {
                return;
            }
        }
        super.tick(currentTick);
    }

    /**
     * Swells by a tick, or shrinks back; once lit, a creeper only swells. It hisses as it starts to
     * swell, and explodes once fully swollen.
     */
    private void tickSwell() {
        if (ignited) {
            setSwellDir(1);
        }
        if (swellDir > 0 && swell == 0) {
            playSound(SoundEvents.CREEPER_PRIMED, 1.0f, PRIMED_SOUND_PITCH);
            server().debug().gameEvents().emit(world(), GameEventKeys.PRIME_FUSE, location());
        }
        swell = Math.max(0, swell + swellDir);
        if (swell >= maxSwell) {
            swell = maxSwell;
            explode();
        }
    }

    /**
     * Blows this creeper up: everything around is hurt and pushed, blocks break if mobs may grief,
     * and the creeper is gone without leaving anything.
     */
    private void explode() {
        final float power = explosionRadius * (powered ? POWERED_EXPLOSION_MULTIPLIER : 1.0f);
        // TODO: the effects of the creeper linger in a cloud, once there are effect clouds
        Explosion.explode(serverWorld(), location(), power, this);
        server().entities().despawn(this);
    }

    /**
     * {@return in which direction this creeper swells: {@code 1} as it swells, {@code -1} as it
     * shrinks back}
     */
    public int swellDir() {
        return swellDir;
    }

    private void setSwellDir(final int swellDir) {
        if (this.swellDir == swellDir) {
            return;
        }
        this.swellDir = swellDir;
        sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.varInt(MD_SWELL_DIR, swellDir)));
    }

    /**
     * {@return how far this creeper has swollen, in ticks}
     */
    public int swell() {
        return swell;
    }

    /**
     * {@return {@code true} if lightning charged this creeper}
     */
    public boolean isPowered() {
        return powered;
    }

    /**
     * Charges this creeper, as lightning does, or takes its charge away.
     */
    public void setPowered(final boolean powered) {
        if (this.powered == powered) {
            return;
        }
        this.powered = powered;
        sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_POWERED, powered)));
    }

    /**
     * {@return {@code true} once this creeper was lit: it then swells until it explodes}
     */
    public boolean isIgnited() {
        return ignited;
    }

    /**
     * Lights this creeper: it swells until it explodes, whatever happens around it.
     */
    public void ignite() {
        if (ignited) {
            return;
        }
        ignited = true;
        sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_IGNITED, true)));
    }

    /**
     * Flint and steel or a fire charge lights the creeper; the flint and steel wears, the fire charge
     * is used up.
     */
    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (held.isEmpty()
                || !server().registries().registry(RegistryKey.ITEM).isTagged(held.id(), ItemTagKeys.CREEPER_IGNITERS)) {
            return super.interact(player, hand);
        }
        final Sound.Type sound = held.id().equals(ItemKeys.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
        final Location at = location();
        // the player hears it from their own client, which predicts the click
        server().players().broadcastNear(world(), at.x(), at.y(), at.z(), new ClientboundSoundPacket(
                Sound.sound(sound, soundSource(), 1.0f, ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f),
                at.x(), at.y(), at.z()), player);
        ignite();
        if (ItemDefaults.maxDamage(held.id(), held) > 0) {
            wearItem(player, slot);
        } else {
            consumeItem(player, slot);
        }
        return true;
    }

    /**
     * A charged creeper leaves the head of the first zombie, skeleton, wither skeleton, piglin or
     * creeper it kills.
     */
    @Override
    public void onKilledEntity(final AbstractLivingEntity victim) {
        if (!powered || droppedSkulls || !shouldDropLoot()) {
            return;
        }
        final TypedKey<Item> head = headOf(victim.type());
        if (head == null || !(victim.world() instanceof final ServerWorld world)) {
            return;
        }
        final Location at = victim.location();
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        server().entities().dropItem(world, at.x(), at.y(), at.z(), ItemStack.of(head, 1),
                random.nextDouble() * 0.2 - 0.1, 0.2, random.nextDouble() * 0.2 - 0.1,
                ItemEntity.DEFAULT_PICKUP_DELAY, null);
        droppedSkulls = true;
    }

    private static @Nullable TypedKey<Item> headOf(final EntityType type) {
        if (type == EntityTypes.PIGLIN) {
            return ItemKeys.PIGLIN_HEAD;
        }
        if (type == EntityTypes.CREEPER) {
            return ItemKeys.CREEPER_HEAD;
        }
        if (type == EntityTypes.SKELETON) {
            return ItemKeys.SKELETON_SKULL;
        }
        if (type == EntityTypes.WITHER_SKELETON) {
            return ItemKeys.WITHER_SKELETON_SKULL;
        }
        if (type == EntityTypes.ZOMBIE) {
            return ItemKeys.ZOMBIE_HEAD;
        }
        return null;
    }

    /**
     * Up to 2 gunpowder, and a music disc when a skeleton killed it.
     */
    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one gunpowder per level
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int gunpowder = random.nextInt(LOOT_MAX_GUNPOWDER + 1);
        if (gunpowder > 0) {
            dropLootItem(ItemStack.of(ItemKeys.GUNPOWDER, gunpowder));
        }
        final DamageSource killing = lastDamageSource();
        if (killing != null && killing.causingEntity() instanceof final AbstractLivingEntity killer
                && server().registries().registry(RegistryKey.ENTITY_TYPE).isTagged(killer.type().key(), EntityTypeTagKeys.SKELETONS)) {
            final List<TypedKey<Item>> discs = new ArrayList<>(
                    server().registries().registry(RegistryKey.ITEM).tag(ItemTagKeys.CREEPER_DROP_MUSIC_DISCS));
            if (!discs.isEmpty()) {
                dropLootItem(ItemStack.of(discs.get(random.nextInt(discs.size())), 1));
            }
        }
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.CREEPER_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.CREEPER_DEATH;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putBoolean("powered", powered);
        tag.putShort("Fuse", (short) maxSwell);
        tag.putByte("ExplosionRadius", (byte) explosionRadius);
        tag.putBoolean("ignited", ignited);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        setPowered(tag.getBoolean("powered", false));
        maxSwell = tag.getShort("Fuse", DEFAULT_FUSE);
        explosionRadius = tag.getByte("ExplosionRadius", DEFAULT_EXPLOSION_RADIUS);
        if (tag.getBoolean("ignited", false)) {
            ignite();
        }
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.varInt(MD_SWELL_DIR, swellDir),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_POWERED, powered),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_IGNITED, ignited)));
    }

    /**
     * Makes the creeper stop and swell when its target comes within 3 blocks; it shrinks back while
     * the target is more than 7 blocks away or out of sight.
     */
    private final class SwellGoal implements Goal {

        private static final Set<Flag> FLAGS = Set.of(Flag.MOVE);
        private static final double START_DISTANCE_SQ = 9.0;
        private static final double GIVE_UP_DISTANCE_SQ = 49.0;

        private final int priority;
        private @Nullable AbstractLivingEntity target;

        private SwellGoal(final int priority) {
            this.priority = priority;
        }

        @Override
        public int priority() {
            return priority;
        }

        @Override
        public Set<Flag> flags() {
            return FLAGS;
        }

        @Override
        public boolean canStart() {
            final AbstractLivingEntity current = target();
            return swellDir > 0 || (current != null && !current.isDead() && distanceSqTo(current) < START_DISTANCE_SQ);
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }

        @Override
        public void start() {
            navigation().stop();
            target = target();
        }

        @Override
        public void stop() {
            target = null;
        }

        @Override
        public void tick() {
            final AbstractLivingEntity aimed = target;
            if (aimed == null || aimed.isDead() || aimed.isRemoved()
                    || distanceSqTo(aimed) > GIVE_UP_DISTANCE_SQ || !hasLineOfSightTo(aimed)) {
                setSwellDir(-1);
            } else {
                setSwellDir(1);
            }
        }
    }
}
