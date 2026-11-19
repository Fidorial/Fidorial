package fr.euphyllia.fidorial.server.entity.mob.monster.spider;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.goal.AvoidEntityGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FloatGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LeapAtTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtPlayerGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.MeleeAttackGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomLookAroundGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMonster;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.entity.mob.creature.Armadillo;
import fr.euphyllia.fidorial.server.entity.mob.misc.IronGolem;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.EntityType;
import fr.fidorial.entity.effect.MobEffectInstance;
import fr.fidorial.entity.mob.monster.Spider;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.MobEffect;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The spider: in the dark it goes after the players and the iron golems it sees, leaps at them from
 * 2 to 4 blocks and climbs the walls on its way; in the light it leaves them alone, and now and then
 * gives up the one it chases. It keeps away from the armadillos. On hard difficulty, the harder the
 * game is where it spawns, the more likely it carries an effect that never runs out.
 */
public class SpiderImpl extends AbstractMonster implements Spider {

    public static final float MAX_HEALTH = 16f;

    private static final double MOVEMENT_SPEED = 0.3;
    private static final float ATTACK_DAMAGE = 2f;
    private static final double FOLLOW_RANGE = 16.0;
    private static final double FOLLOW_RANGE_SPREAD = 0.11485;

    private static final double HALF_WIDTH = 0.7;
    private static final double HEIGHT = 0.9;
    private static final double EYE_HEIGHT = 0.65;

    private static final double STROLL_SPEED_MODIFIER = 0.8;
    private static final double AVOID_DISTANCE = 6.0;
    private static final double AVOID_WALK_SPEED_MODIFIER = 1.0;
    private static final double AVOID_RUN_SPEED_MODIFIER = 1.2;
    private static final double LEAP_HEIGHT = 0.4;
    private static final double LOOK_DISTANCE = 8.0;

    /**
     * In a light at least this bright a spider looks for no target, and gives up the one it chases one
     * tick in two hundred.
     */
    private static final float BRIGHT = 0.5f;
    private static final int GIVE_UP_CHANCE = 200;

    private static final float EFFECT_CHANCE = 0.1f;
    private static final float STEP_VOLUME = 0.15f;
    private static final int LOOT_MAX_STRING = 2;
    private static final int LOOT_EYE_CHANCE = 3;

    private static final int MD_SPIDER_FLAGS = 16;
    private static final byte FLAG_CLIMBING = 0x01;

    private static final Key RANDOM_SPAWN_BONUS = Key.key("random_spawn_bonus");

    private final MobAttribute followRangeAttribute = new MobAttribute(Key.key("follow_range"), FOLLOW_RANGE);
    private boolean climbing;

    /**
     * Creates a spider as one that spawns, with the random traits of one.
     */
    public SpiderImpl(final int entityId, final Location location) {
        this(entityId, EntityTypes.SPIDER, location, MAX_HEALTH);
        finalizeSpawn();
    }

    /**
     * Creates a spider of another kind, without the random traits of one that spawns.
     */
    protected SpiderImpl(final int entityId, final EntityType type, final Location location, final float maxHealth) {
        super(entityId, UUID.randomUUID(), type, location, maxHealth);
        navigation.setClimbsWalls(width());
        goals.add(new FloatGoal(this, 1));
        // TODO: an armadillo rolled up in fear is not avoided, once armadillos roll up
        goals.add(new AvoidEntityGoal<>(this, 2, Armadillo.class, AVOID_DISTANCE, AVOID_WALK_SPEED_MODIFIER, AVOID_RUN_SPEED_MODIFIER));
        goals.add(new LeapAtTargetGoal(this, 3, LEAP_HEIGHT));
        goals.add(new SpiderAttackGoal(4));
        goals.add(new RandomStrollGoal(this, 5, () -> movementSpeed() * STROLL_SPEED_MODIFIER));
        goals.add(new LookAtPlayerGoal(this, 6, LOOK_DISTANCE));
        goals.add(new RandomLookAroundGoal(this, 6));
        goals.add(new HurtByTargetGoal(this, 1));
        goals.add(new NearestAttackableTargetGoal<>(this, 2, ServerPlayer.class, true).onlyWhen(this::isInTheDark));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, IronGolem.class, true).onlyWhen(this::isInTheDark));
    }

    /**
     * {@return {@code true} if spiders may spawn on a block}: as any monster, on solid ground in the
     * dark
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkMonsterSpawnRules(world, x, y, z);
    }

    /**
     * Gives this spider the random traits of one that spawns.
     */
    private void finalizeSpawn() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        // every mob that spawns notices its targets from a little closer or further than the others
        final double first = random.nextDouble();
        final double second = random.nextDouble();
        followRangeAttribute.put(RANDOM_SPAWN_BONUS, FOLLOW_RANGE_SPREAD * (first - second),
                MobAttribute.Operation.ADD_MULTIPLIED_BASE);
        // TODO: one spider in a hundred carries a skeleton, once skeletons ride
        if (difficulty() == LocalDifficulty.HARD
                && random.nextFloat() < EFFECT_CHANCE * localDifficulty().specialMultiplier()) {
            addEffect(server().registries().effects().instance(randomEffect(random),
                    MobEffectInstance.INFINITE_DURATION, 0));
        }
    }

    /**
     * {@return the effect a spider may spawn with}: speed two times out of five, strength,
     * regeneration or invisibility one time out of five each
     */
    private static TypedKey<MobEffect> randomEffect(final ThreadLocalRandom random) {
        return switch (random.nextInt(5)) {
            case 0, 1 -> MobEffectKeys.SPEED;
            case 2 -> MobEffectKeys.STRENGTH;
            case 3 -> MobEffectKeys.REGENERATION;
            default -> MobEffectKeys.INVISIBILITY;
        };
    }

    private boolean isInTheDark() {
        return brightness() < BRIGHT;
    }

    @Override
    public double movementSpeed() {
        return MOVEMENT_SPEED;
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
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (!isRemoved()) {
            // a spider pushing against a wall climbs it the next tick
            setClimbing(horizontalCollision());
        }
    }

    /**
     * {@return {@code true} while this spider climbs: it pushed against a wall the last tick}
     */
    @Override
    public final boolean isClimbing() {
        return climbing;
    }

    private void setClimbing(final boolean climbing) {
        if (this.climbing == climbing) {
            return;
        }
        this.climbing = climbing;
        sendToTrackers(flagsPacket());
    }

    @Override
    protected boolean onClimbable(final double x, final double y, final double z) {
        return climbing;
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected void onStep() {
        playSound(SoundEvents.SPIDER_STEP, STEP_VOLUME, 1.0f);
    }

    /**
     * Up to 2 strings, and a spider eye one time out of three when a player killed it.
     */
    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one string and one spider eye per level
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int string = random.nextInt(LOOT_MAX_STRING + 1);
        if (string > 0) {
            dropLootItem(ItemStack.of(ItemKeys.STRING, string));
        }
        if (wasRecentlyHurtByPlayer() && random.nextInt(LOOT_EYE_CHANCE) == 0) {
            dropLootItem(ItemStack.of(ItemKeys.SPIDER_EYE, 1));
        }
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        MobAttribute.save(tag, followRangeAttribute);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        // a spider read back keeps nothing of what was rolled when it was created
        followRangeAttribute.remove(RANDOM_SPAWN_BONUS);
        clearEffectsSilently();
        super.loadAdditional(tag);
        MobAttribute.load(tag, followRangeAttribute);
    }

    private ClientboundSetEntityMetadataPacket flagsPacket() {
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofByte(MD_SPIDER_FLAGS, climbing ? FLAG_CLIMBING : 0));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(flagsPacket());
    }

    /**
     * Makes the spider chase its target, even out of sight; in the light it gives the target up now
     * and then.
     */
    private final class SpiderAttackGoal extends MeleeAttackGoal {

        private SpiderAttackGoal(final int priority) {
            super(SpiderImpl.this, priority, 1.0, true);
        }

        @Override
        public boolean shouldContinue() {
            if (brightness() >= BRIGHT && ThreadLocalRandom.current().nextInt(GIVE_UP_CHANCE) == 0) {
                setTarget(null);
                return false;
            }
            return super.shouldContinue();
        }
    }
}
