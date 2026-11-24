package fr.euphyllia.fidorial.server.entity.mob.monster.endermite;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FloatGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtPlayerGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.MeleeAttackGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomLookAroundGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMonster;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.mob.monster.Endermite;
import fr.fidorial.math.Location;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The endermite: a tiny monster that goes after the players it sees and calls the endermites
 * around when it is hurt. Unless it is persistent, it lives two minutes and then vanishes. Its
 * steps make no sound.
 */
public final class EndermiteEntity extends AbstractMonster implements Endermite {

    public static final float MAX_HEALTH = 8f;

    private static final double MOVEMENT_SPEED = 0.25;
    private static final float ATTACK_DAMAGE = 2f;
    private static final double FOLLOW_RANGE = 16.0;
    private static final double FOLLOW_RANGE_SPREAD = 0.11485;
    private static final int EXPERIENCE = 3;

    private static final double HALF_WIDTH = 0.2;
    private static final double HEIGHT = 0.3;
    private static final double EYE_HEIGHT = 0.13;

    private static final double LOOK_DISTANCE = 8.0;
    /**
     * An endermite does not spawn by itself within this distance of a player in survival or
     * adventure.
     */
    private static final double PLAYER_NEARBY_DISTANCE = 5.0;

    private static final Key RANDOM_SPAWN_BONUS = Key.key("random_spawn_bonus");

    private final MobAttribute followRangeAttribute = new MobAttribute(Key.key("follow_range"), FOLLOW_RANGE);
    private int life;

    /**
     * Creates an endermite as one that spawns, with the random traits of one.
     */
    public EndermiteEntity(final int entityId, final Location location) {
        super(entityId, UUID.randomUUID(), EntityTypes.ENDERMITE, location, MAX_HEALTH);
        goals.add(new FloatGoal(this, 1));
        goals.add(new ClimbOnTopOfPowderSnowGoal(this, 1));
        goals.add(new MeleeAttackGoal(this, 2, 1.0, false));
        goals.add(new RandomStrollGoal(this, 3, this::movementSpeed));
        goals.add(new LookAtPlayerGoal(this, 7, LOOK_DISTANCE));
        goals.add(new RandomLookAroundGoal(this, 8));
        goals.add(new HurtByTargetGoal(this, 1).alertOthers());
        goals.add(new NearestAttackableTargetGoal<>(this, 2, ServerPlayer.class, true));
        // every mob that spawns notices its targets from a little closer or further than the others
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final double first = random.nextDouble();
        final double second = random.nextDouble();
        followRangeAttribute.put(RANDOM_SPAWN_BONUS, FOLLOW_RANGE_SPREAD * (first - second),
                MobAttribute.Operation.ADD_MULTIPLIED_BASE);
    }

    /**
     * {@return {@code true} if an endermite may spawn on a block}: on solid ground in any light,
     * unless the world is peaceful, and away from the players in survival or adventure
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkAnyLightMonsterSpawnRules(world, x, y, z)
                && isAwayFromPlayers(world, x, y, z, PLAYER_NEARBY_DISTANCE);
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved()) {
            return;
        }
        if (!isPersistenceRequired()) {
            life++;
        }
        if (life >= MAX_LIFE) {
            server().entities().despawn(this);
        }
    }

    /**
     * {@return how many ticks this endermite has lived, its time as a persistent one left out}
     */
    @Override
    public int life() {
        return life;
    }

    @Override
    public void setLife(final int ticks) {
        this.life = Math.max(0, ticks);
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
    protected int experienceReward() {
        return EXPERIENCE;
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
    protected Sound.Type ambientSound() {
        return SoundEvents.ENDERMITE_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.ENDERMITE_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.ENDERMITE_DEATH;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putInt("Lifetime", life);
        MobAttribute.save(tag, followRangeAttribute);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        // an endermite read back keeps nothing of what was rolled when it was created
        followRangeAttribute.remove(RANDOM_SPAWN_BONUS);
        super.loadAdditional(tag);
        life = tag.getInt("Lifetime", 0);
        MobAttribute.load(tag, followRangeAttribute);
    }
}
