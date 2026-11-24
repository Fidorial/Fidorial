package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RangedAttackGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.ZombieAttackGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.mob.creature.WanderingTrader;
import fr.euphyllia.fidorial.server.entity.mob.misc.IronGolem;
import fr.euphyllia.fidorial.server.entity.mob.misc.Villager;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.entity.mob.monster.ZombifiedPiglin;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.entity.projectile.IceBallEntity;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.mob.monster.Frostbite;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BiomeTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.MobEffectKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;

/**
 * The frostbite, the zombie of the cold: tougher and slower than the zombie, it chills what it
 * hits, and one in four holds an ice ball it throws at a target that is out of reach, 5 blocks away
 * or further. Powder snow does not change it; water turns it into a zombie.
 */
public final class FrostbiteEntity extends ZombieImpl implements Frostbite {

    public static final float MAX_HEALTH = 24f;

    private static final double MOVEMENT_SPEED = 0.21;
    private static final int MELEE_ATTACK_TICKS = 30;
    private static final int FREEZING_TICKS_ON_MELEE = 60;

    private static final float ICE_BALL_CHANCE = 0.25f;
    private static final double ATTACK_RADIUS = 16.0;
    /**
     * A frostbite starts to throw at a target 5 blocks away, and goes back to its fists once the
     * target is closer than 3 blocks.
     */
    private static final double RANGED_MODE_DISTANCE_SQ = 25.0;
    private static final double MELEE_MODE_DISTANCE_SQ = 9.0;
    private static final int TICKS_PER_SECOND = 20;

    private static final double THROW_SPEED = 1.6;
    private static final double THROW_HEIGHT_BELOW_EYES = 0.1;
    private static final double AIM_HEIGHT = 1.0 / 3.0;
    /**
     * How much higher a thrown item is aimed for each block to fly: four times what it falls a tick.
     */
    private static final double AIM_LIFT_PER_BLOCK = 0.12;
    private static final double UNCERTAINTY_UNIT = 0.0172275;
    private static final int BASE_UNCERTAINTY = 14;
    private static final int UNCERTAINTY_PER_DIFFICULTY = 4;

    private static final int LOOT_MAX_ICE_BALLS = 3;
    private static final float HELD_ITEM_DROP_CHANCE = 0.085f;
    private static final Key ICE_BALL_ID = Key.key("ice_ball");

    private boolean holdsIceBall;
    private boolean ranged;
    private float rangedAttackUncertainty = -1f;

    public FrostbiteEntity(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    /**
     * @param spawned {@code true} for a frostbite that spawns, with the random traits of a zombie
     *                that does, and an ice ball in its hand one time out of four
     */
    public FrostbiteEntity(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.FROSTBITE, location, spawned, false);
        setBaseMaxHealth(MAX_HEALTH);
        if (spawned) {
            holdsIceBall = ThreadLocalRandom.current().nextFloat() < ICE_BALL_CHANCE;
        }

        goals.add(new ThrowGoal(this, 1, 1.0, this::rangedAttackIntervalMin, this::rangedAttackIntervalMax));
        goals.add(new ZombieAttackGoal(this, 2, 1.0, false, MELEE_ATTACK_TICKS));
        goals.add(new RandomStrollGoal(this, 3, this::movementSpeed, RandomStrollGoal.Terrain.ANY_GROUND));
        goals.add(new HurtByTargetGoal(this, 1).alertOthers(ZombifiedPiglin.class));
        goals.add(new NearestAttackableTargetGoal<>(this, 2, ServerPlayer.class, true));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, Villager.class, false));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, WanderingTrader.class, false));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, IronGolem.class, true));
    }

    /**
     * {@return {@code true} if frostbites may spawn on a block}: on solid ground in the dark, and,
     * out of the biomes where they spawn underground, under the open sky, powder snow aside
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        if (!checkMonsterSpawnRules(world, x, y, z)) {
            return false;
        }
        final TypedKey<BiomeDefinition> biome = BlockView.biomeAt(world, x, y, z);
        if (biome != null && FidorialServer.getInstance().registries().registry(RegistryKey.BIOME)
                .isTagged(biome, BiomeTagKeys.SPAWNS_COLD_VARIANTS_UNDERGROUND)) {
            return true;
        }
        int sky = y + 1;
        while (isPowderSnow(world, x, sky, z)) {
            sky++;
        }
        return Brightness.canSeeSky(world, x, sky - 1, z);
    }

    private static boolean isPowderSnow(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && BlockView.isPowderSnow(state);
    }

    /**
     * {@return {@code true} if this frostbite holds an ice ball}: it then throws it, again and
     * again, at a target it cannot hit with its fists
     */
    @Override
    public boolean holdsIceBall() {
        return holdsIceBall;
    }

    /**
     * Gives this frostbite an ice ball to throw, or takes it away.
     */
    @Override
    public void setHoldsIceBall(final boolean holdsIceBall) {
        this.holdsIceBall = holdsIceBall;
    }

    /**
     * {@return {@code true} while this frostbite throws ice balls rather than using its fists}
     */
    @Override
    public boolean isRanged() {
        return ranged;
    }

    /**
     * Sets how much the throws of this frostbite stray, in place of what the difficulty gives.
     *
     * @param uncertainty {@code 0} for throws that go straight at the target, a negative value to
     *                    follow the difficulty again
     */
    @Override
    public void setRangedAttackUncertainty(final float uncertainty) {
        this.rangedAttackUncertainty = uncertainty;
    }

    private float rangedAttackUncertainty() {
        return rangedAttackUncertainty < 0f
                ? BASE_UNCERTAINTY - difficulty() * UNCERTAINTY_PER_DIFFICULTY
                : rangedAttackUncertainty;
    }

    /**
     * {@return {@code true} if this frostbite throws at its target rather than walking up to it}:
     * with an ice ball in its hand and the target within 16 blocks, when the target is 5 blocks away
     * or further, 3 once it throws, or closer but out of the reach of its fists with no way to it.
     * Since 26.4-pre-1 a target further than 16 blocks is walked up to instead.
     */
    private boolean shouldThrow() {
        final AbstractLivingEntity target = target();
        if (!holdsIceBall || target == null || target.isDead() || target.isRemoved()) {
            return false;
        }
        final double distanceSq = distanceSqTo(target);
        if (distanceSq > ATTACK_RADIUS * ATTACK_RADIUS) {
            return false;
        }
        if (distanceSq >= (ranged ? MELEE_MODE_DISTANCE_SQ : RANGED_MODE_DISTANCE_SQ)) {
            return true;
        }
        return !isWithinMeleeRange(target) && navigation.isGoalOutOfReach();
    }

    /**
     * Throws an ice ball at a target: from just under the eyes, at a third of the height of the
     * target, a little higher the further it is, and off by what the difficulty leaves to chance.
     */
    private void throwIceBall(final AbstractLivingEntity target, final float power) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Location self = location();
        final Location aim = target.location();
        final double fromY = self.y() + eyeHeight() - THROW_HEIGHT_BELOW_EYES;
        final double dx = aim.x() - self.x();
        final double dz = aim.z() - self.z();
        final double horizontal = Math.sqrt(dx * dx + dz * dz);
        final double dy = aim.y() + heightOf(target) * AIM_HEIGHT - fromY + horizontal * AIM_LIFT_PER_BLOCK;
        final double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-6) {
            return;
        }
        final double spread = UNCERTAINTY_UNIT * rangedAttackUncertainty();
        final double velocityX = (dx / length + triangle(random, spread)) * THROW_SPEED;
        final double velocityY = (dy / length + triangle(random, spread)) * THROW_SPEED;
        final double velocityZ = (dz / length + triangle(random, spread)) * THROW_SPEED;

        final IceBallEntity ball = new IceBallEntity(server().entities().ids().allocate(),
                Location.of(world(), self.x(), fromY, self.z(), yaw(), pitch()), this, velocityX, velocityY, velocityZ);
        server().entities().spawn(ball);
        playSound(SoundEvents.FROSTBITE_THROW, 1.0f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
        swingMainHand();
    }

    private static double triangle(final ThreadLocalRandom random, final double deviation) {
        final double first = random.nextDouble();
        final double second = random.nextDouble();
        return deviation * (first - second);
    }

    private static double heightOf(final AbstractLivingEntity entity) {
        return entity instanceof final AbstractMovingMob mob ? mob.height() : entity.type().height();
    }

    private int rangedAttackIntervalMin() {
        return switch (difficulty()) {
            case LocalDifficulty.EASY -> 3 * TICKS_PER_SECOND;
            case LocalDifficulty.HARD -> TICKS_PER_SECOND * 3 / 4;
            default -> TICKS_PER_SECOND;
        };
    }

    private int rangedAttackIntervalMax() {
        return switch (difficulty()) {
            case LocalDifficulty.EASY -> 3 * TICKS_PER_SECOND;
            case LocalDifficulty.HARD -> TICKS_PER_SECOND * 3 / 2;
            default -> 2 * TICKS_PER_SECOND;
        };
    }

    @Override
    protected double baseMovementSpeed() {
        return MOVEMENT_SPEED;
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.FROSTBITE_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.FROSTBITE_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.FROSTBITE_DEATH;
    }

    @Override
    protected Sound.Type stepSound() {
        return SoundEvents.FROSTBITE_STEP;
    }

    @Override
    protected ZombieImpl drownedForm(final int entityId, final Location location) {
        return new ZombieImpl(entityId, location, false);
    }

    @Override
    protected Sound.Type drownedSound() {
        return SoundEvents.FROSTBITE_CONVERTED_TO_ZOMBIE;
    }

    @Override
    protected boolean convertsWhenFreezing() {
        return false;
    }

    /**
     * What a frostbite hits is chilled for 3 seconds.
     */
    @Override
    protected void onAttackLanded(final AbstractLivingEntity target) {
        if (target.isDead()) {
            return;
        }
        target.addEffect(server().registries().effects().builder(MobEffectKeys.FREEZING)
                .duration(FREEZING_TICKS_ON_MELEE).build());
    }

    @Override
    protected float rareLootChance() {
        return 0f;
    }

    @Override
    protected void dropLoot() {
        super.dropLoot();
        // TODO: Looting adds up to one ice ball per level
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int iceBalls = random.nextInt(LOOT_MAX_ICE_BALLS + 1);
        if (iceBalls > 0) {
            dropLootItem(ItemStack.of(ItemKeys.ICE_BALL, iceBalls));
        }
        // what a mob holds is only left now and then, and to a player
        if (holdsIceBall && wasRecentlyHurtByPlayer() && random.nextFloat() < HELD_ITEM_DROP_CHANCE) {
            dropLootItem(ItemStack.of(ItemKeys.ICE_BALL, 1));
        }
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        if (holdsIceBall) {
            tag.put("equipment", CompoundBinaryTag.builder()
                    .put("mainhand", CompoundBinaryTag.builder()
                            .putString("id", ICE_BALL_ID.asString())
                            .putInt("count", 1)
                            .build())
                    .build());
        }
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        final String held = tag.getCompound("equipment").getCompound("mainhand").getString("id");
        holdsIceBall = Key.parseable(held) && ICE_BALL_ID.equals(Key.key(held));
    }

    /**
     * The throws of a frostbite: it keeps walking towards its target between two throws, and shows
     * its arms raised.
     */
    private static final class ThrowGoal extends RangedAttackGoal {

        private final FrostbiteEntity frostbite;

        private ThrowGoal(final FrostbiteEntity frostbite, final int priority, final double speedModifier,
                          final IntSupplier attackIntervalMin, final IntSupplier attackIntervalMax) {
            super(frostbite, priority, speedModifier, attackIntervalMin, attackIntervalMax, ATTACK_RADIUS,
                    frostbite::throwIceBall);
            this.frostbite = frostbite;
        }

        @Override
        public boolean canStart() {
            return super.canStart() && frostbite.shouldThrow();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && frostbite.shouldThrow();
        }

        @Override
        public void start() {
            frostbite.setAggressive(true);
            frostbite.ranged = true;
        }

        @Override
        public void stop() {
            super.stop();
            frostbite.setAggressive(false);
            frostbite.ranged = false;
        }

        @Override
        protected boolean stopWhenInRange() {
            return false;
        }
    }
}
