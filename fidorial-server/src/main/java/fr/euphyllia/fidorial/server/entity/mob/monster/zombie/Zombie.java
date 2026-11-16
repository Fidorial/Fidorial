package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.goal.BreakDoorGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtPlayerGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomLookAroundGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.ZombieAttackGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMob;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMonster;
import fr.euphyllia.fidorial.server.entity.mob.MobAttribute;
import fr.euphyllia.fidorial.server.entity.mob.MobFactories;
import fr.euphyllia.fidorial.server.entity.mob.creature.WanderingTrader;
import fr.euphyllia.fidorial.server.entity.mob.misc.IronGolem;
import fr.euphyllia.fidorial.server.entity.mob.misc.Villager;
import fr.euphyllia.fidorial.server.entity.mob.monster.ConversionTimer;
import fr.euphyllia.fidorial.server.entity.mob.monster.LocalDifficulty;
import fr.euphyllia.fidorial.server.entity.mob.monster.ZombieVillager;
import fr.euphyllia.fidorial.server.entity.mob.monster.ZombifiedPiglin;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.EntityType;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.EntityTypeTagKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.ChunkPos;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The zombie, it goes after the players it sees, the villagers and the iron golems,
 * fights back with those of its kind when hurt, burns in the sun, turns into a drowned under water
 * and into a frostbite in powder snow, and on hard difficulty breaks doors and calls for
 * reinforcements. One in twenty is a baby, half as tall and half as fast again.
 *
 * <p>The husk, the drowned and the frostbite are zombies too; they change the hooks of this class.</p>
 */
public class Zombie extends AbstractMonster {

    public static final float MAX_HEALTH = 20f;

    private static final double FOLLOW_RANGE = 35.0;
    private static final double MOVEMENT_SPEED = 0.23;
    private static final double BABY_SPEED_BONUS = 0.5;
    private static final float ATTACK_DAMAGE = 3f;
    private static final float ARMOR_POINTS = 2f;

    private static final double ADULT_HEIGHT = 1.95;

    private static final double ADULT_WIDTH = 0.6;
    private static final double ADULT_EYE_HEIGHT = 1.74;
    private static final double BABY_HEIGHT = 0.98;
    private static final double BABY_WIDTH = 0.49;

    private static final double BABY_EYE_HEIGHT = 0.775;
    private static final float BABY_EXPERIENCE_FACTOR = 2.5f;

    private static final float BABY_CHANCE = 0.05f;
    private static final float LEADER_CHANCE = 0.05f;
    private static final float BREAK_DOORS_CHANCE = 0.1f;
    private static final double MAX_REINFORCEMENT_CHANCE = 0.1;
    private static final double MAX_KNOCKBACK_RESISTANCE_BONUS = 0.05;
    private static final double FOLLOW_RANGE_SPREAD = 0.11485;
    private static final double REINFORCEMENT_CHARGE = -0.05;

    private static final int REINFORCEMENT_ATTEMPTS = 50;
    private static final int REINFORCEMENT_MIN_OFFSET = 7;
    private static final int REINFORCEMENT_MAX_OFFSET = 40;
    private static final double REINFORCEMENT_PLAYER_CLEARANCE = 7.0;

    private static final int SUN_FIRE_TICKS = 160;
    private static final float FIRE_ON_HIT_CHANCE = 0.3f;
    private static final int TICKS_PER_SECOND = 20;
    private static final float STEP_VOLUME = 0.15f;
    private static final float CONVERSION_VOLUME = 2.0f;

    private static final int LOOT_MAX_ROTTEN_FLESH = 2;
    private static final float RARE_LOOT_CHANCE = 0.025f;

    private static final Key RANDOM_SPAWN_BONUS = Key.key("random_spawn_bonus");
    private static final Key ZOMBIE_RANDOM_SPAWN_BONUS = Key.key("zombie_random_spawn_bonus");
    private static final Key LEADER_ZOMBIE_BONUS = Key.key("leader_zombie_bonus");
    private static final Key REINFORCEMENT_CALLER_CHARGE = Key.key("reinforcement_caller_charge");
    private static final Key REINFORCEMENT_CALLEE_CHARGE = Key.key("reinforcement_callee_charge");

    private static final int MD_BABY = 16;
    private static final int MD_DROWNED_CONVERSION = 18;
    private static final int MD_FROSTBITE_CONVERSION = 19;

    private final MobAttribute maxHealthAttribute = new MobAttribute(Key.key("max_health"), MAX_HEALTH);
    private final MobAttribute followRangeAttribute = new MobAttribute(Key.key("follow_range"), FOLLOW_RANGE);
    private final MobAttribute knockbackResistanceAttribute = new MobAttribute(Key.key("knockback_resistance"), 0.0);
    private final MobAttribute reinforcementsAttribute = new MobAttribute(Key.key("spawn_reinforcements"), 0.0);

    private final ConversionTimer drowning = new ConversionTimer("InWaterTime", 600, "DrownedConversionTime", 300);
    private final ConversionTimer freezing = new ConversionTimer("FreezingTime", 140, "FrostbiteConversionTime", 300);
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, 1);
    private final boolean burnsInDaylight;

    private boolean baby;
    private boolean canBreakDoors;
    private boolean sentDrowning;
    private boolean sentFreezing;

    /**
     * Creates a zombie as one that spawns: a baby one time out of twenty, and, the harder the game
     * is where it stands, the more likely one that breaks doors or leads the others.
     */
    public Zombie(final int entityId, final Location location) {
        this(entityId, EntityTypes.ZOMBIE, location, true);
    }

    /**
     * @param spawned {@code true} for a zombie that spawns, with the random traits of one;
     *                {@code false} for a plain adult, as the one another mob turns into
     */
    public Zombie(final int entityId, final Location location, final boolean spawned) {
        this(entityId, EntityTypes.ZOMBIE, location, spawned);
    }

    protected Zombie(final int entityId, final EntityType type, final Location location, final boolean spawned) {
        this(entityId, type, location, spawned, true);
    }

    /**
     * @param behaves {@code true} for a zombie that moves, attacks and picks its targets as the
     *                plain zombie does; {@code false} for a kind that adds goals of its own for
     *                that, and only shares with the others the way they look around
     */
    protected Zombie(final int entityId, final EntityType type, final Location location, final boolean spawned,
                     final boolean behaves) {
        super(entityId, UUID.randomUUID(), type, location, MAX_HEALTH);
        this.burnsInDaylight = server().registries().registry(RegistryKey.ENTITY_TYPE)
                .isTagged(type.key(), EntityTypeTagKeys.BURN_IN_DAYLIGHT);

        goals.add(new LookAtPlayerGoal(this, 8, 8.0));
        goals.add(new RandomLookAroundGoal(this, 8));
        if (behaves) {
            goals.add(new ZombieAttackGoal(this, 3, 1.0, false));
            goals.add(new RandomStrollGoal(this, 7, this::movementSpeed));
            goals.add(new HurtByTargetGoal(this, 1).alertOthers(ZombifiedPiglin.class));
            goals.add(new NearestAttackableTargetGoal<>(this, 2, ServerPlayer.class, true));
            goals.add(new NearestAttackableTargetGoal<>(this, 3, Villager.class, false));
            goals.add(new NearestAttackableTargetGoal<>(this, 3, WanderingTrader.class, false));
            goals.add(new NearestAttackableTargetGoal<>(this, 3, IronGolem.class, true));
        }
        if (spawned) {
            finalizeSpawn();
        }
    }

    /**
     * Gives this zombie the random traits of one that spawns.
     */
    private void finalizeSpawn() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final float special = localDifficulty().specialMultiplier();
        // every mob that spawns notices its targets from a little closer or further than the others
        final double first = random.nextDouble();
        final double second = random.nextDouble();
        followRangeAttribute.put(RANDOM_SPAWN_BONUS, FOLLOW_RANGE_SPREAD * (first - second),
                MobAttribute.Operation.ADD_MULTIPLIED_BASE);
        if (random.nextFloat() < BABY_CHANCE) {
            baby = true;
        }
        setCanBreakDoors(random.nextFloat() < special * BREAK_DOORS_CHANCE);
        rollAttributes(special, true);
    }

    /**
     * Rolls the attributes of this zombie: how likely it calls for reinforcements, how it
     * resists knockback, from how far it notices its targets on the harder difficulties,
     * and whether it is a leader, with up to five times the health.
     *
     * @param special the {@linkplain LocalDifficulty#specialMultiplier() share of special traits}
     *                at the difficulty where the zombie stands
     * @param heals   {@code true} to give a leader its full health, which a converted mob does not get
     */
    protected final void rollAttributes(final float special, final boolean heals) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        reinforcementsAttribute.setBase(random.nextDouble() * MAX_REINFORCEMENT_CHANCE);
        knockbackResistanceAttribute.put(RANDOM_SPAWN_BONUS, random.nextDouble() * MAX_KNOCKBACK_RESISTANCE_BONUS,
                MobAttribute.Operation.ADD_VALUE);
        final double followBonus = random.nextDouble() * 1.5 * special;
        if (followBonus > 1.0) {
            followRangeAttribute.put(ZOMBIE_RANDOM_SPAWN_BONUS, followBonus, MobAttribute.Operation.ADD_MULTIPLIED_TOTAL);
        }
        if (random.nextFloat() < special * LEADER_CHANCE) {
            reinforcementsAttribute.put(LEADER_ZOMBIE_BONUS, random.nextDouble() * 0.25 + 0.5,
                    MobAttribute.Operation.ADD_VALUE);
            maxHealthAttribute.put(LEADER_ZOMBIE_BONUS, random.nextDouble() * 3.0 + 1.0,
                    MobAttribute.Operation.ADD_MULTIPLIED_TOTAL);
            setMaxHealth((float) maxHealthAttribute.value());
            if (heals) {
                setHealth(maxHealth());
            }
            setCanBreakDoors(true);
        }
    }

    /**
     * {@return {@code true} if zombies may spawn on a block}: as any monster, on solid ground in the
     * dark
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkMonsterSpawnRules(world, x, y, z);
    }

    @Override
    public final boolean picksPlayersByItself() {
        return false;
    }

    @Override
    public final boolean isBaby() {
        return baby;
    }

    /**
     * Makes this zombie a baby, or an adult again.
     */
    public final void setBaby(final boolean baby) {
        if (this.baby == baby) {
            return;
        }
        this.baby = baby;
        sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_BABY, baby)));
    }

    /**
     * {@return {@code true} if this zombie leads the others}: it has more health, calls for
     * reinforcements far more often and breaks doors
     */
    public final boolean isLeader() {
        return maxHealthAttribute.has(LEADER_ZOMBIE_BONUS);
    }

    public final boolean canBreakDoors() {
        return canBreakDoors;
    }

    /**
     * Lets this zombie break the wooden doors in its way, which it only does on hard difficulty.
     */
    public final void setCanBreakDoors(final boolean canBreakDoors) {
        if (this.canBreakDoors == canBreakDoors) {
            return;
        }
        this.canBreakDoors = canBreakDoors;
        if (canBreakDoors) {
            goals.add(breakDoorGoal);
        } else {
            goals.remove(breakDoorGoal);
        }
    }

    /**
     * {@return the chance, between {@code 0} and {@code 1}, that this zombie calls a reinforcement
     * when it is hurt on hard difficulty}
     */
    public final double reinforcementChance() {
        return reinforcementsAttribute.value();
    }

    /**
     * Sets how likely this zombie calls for reinforcements, whatever was rolled for it.
     */
    public final void setReinforcementChance(final double chance) {
        reinforcementsAttribute.setBase(chance);
        reinforcementsAttribute.remove(LEADER_ZOMBIE_BONUS);
        reinforcementsAttribute.remove(REINFORCEMENT_CALLER_CHARGE);
        reinforcementsAttribute.remove(REINFORCEMENT_CALLEE_CHARGE);
    }

    @Override
    public double armor() {
        return ARMOR_POINTS;
    }

    @Override
    public double knockbackResistance() {
        return knockbackResistanceAttribute.value();
    }

    @Override
    public float attackDamage() {
        return ATTACK_DAMAGE;
    }

    @Override
    public double movementSpeed() {
        return baby ? MOVEMENT_SPEED * (1.0 + BABY_SPEED_BONUS) : MOVEMENT_SPEED;
    }

    @Override
    public double width() {
        return baby ? BABY_WIDTH : ADULT_WIDTH;
    }

    @Override
    public double height() {
        return baby ? BABY_HEIGHT : ADULT_HEIGHT;
    }

    @Override
    public double eyeHeight() {
        return baby ? BABY_EYE_HEIGHT : ADULT_EYE_HEIGHT;
    }

    @Override
    protected double defaultFollowRange() {
        return followRangeAttribute.value();
    }

    @Override
    public @Nullable TypedKey<Item> disguiseHead() {
        return type() == EntityTypes.ZOMBIE ? ItemKeys.ZOMBIE_HEAD : null;
    }

    @Override
    protected int experienceReward() {
        final int reward = super.experienceReward();
        return baby ? (int) (reward * BABY_EXPERIENCE_FACTOR) : reward;
    }

    @Override
    protected Sound.Type ambientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    protected Sound.Type stepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    @Override
    protected void onStep() {
        playSound(stepSound(), STEP_VOLUME, 1.0f);
    }

    /**
     * {@return {@code true} if staying under water turns this zombie into another}
     */
    protected boolean convertsInWater() {
        return true;
    }

    /**
     * {@return the zombie this one turns into after a while under water, not yet in the world}
     */
    protected Zombie drownedForm(final int entityId, final Location location) {
        return new Drowned(entityId, location, false);
    }

    /**
     * {@return the sound of this zombie turning into its {@linkplain #drownedForm drowned form}}
     */
    protected Sound.Type drownedSound() {
        return SoundEvents.ZOMBIE_CONVERTED_TO_DROWNED;
    }

    /**
     * {@return {@code true} if staying in powder snow turns this zombie into another}
     */
    protected boolean convertsWhenFreezing() {
        return true;
    }

    /**
     * {@return the zombie this one turns into after a while in powder snow, not yet in the world}
     */
    protected Zombie frozenForm(final int entityId, final Location location) {
        return new Frostbite(entityId, location, false);
    }

    /**
     * {@return the sound of this zombie turning into its {@linkplain #frozenForm frozen form}}
     */
    protected Sound.Type frozenSound() {
        return SoundEvents.ZOMBIE_CONVERTED_TO_FROSTBITE;
    }

    /**
     * {@return {@code true} while this zombie shakes under water, about to turn into another}
     */
    public final boolean isUnderWaterConverting() {
        return drowning.isConverting();
    }

    /**
     * {@return {@code true} while this zombie shakes in powder snow, about to turn into another}
     */
    public final boolean isFreezeConverting() {
        return freezing.isConverting();
    }

    /**
     * Sets for how many ticks this zombie has had its eyes under water; it starts to turn into its
     * drowned form at 600.
     */
    public final void setInWaterTime(final int ticks) {
        drowning.setAfflictionTime(ticks);
    }

    /**
     * Sets for how many ticks this zombie has been in powder snow; it starts to turn into its frozen
     * form at 140.
     */
    public final void setFreezingTime(final int ticks) {
        freezing.setAfflictionTime(ticks);
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }
        if (burnsInDaylight && isSunBurnTick()) {
            igniteForTicks(SUN_FIRE_TICKS);
        }

        if (fireTicks() > 0 && isInWater()) {
            setFireTicks(0);
        }

        tickBurning();
        if (isRemoved() || isDead()) {
            return;
        }
        tickConversions();
    }

    @Override
    protected boolean ticksOwnFire() {
        return true;
    }

    private void tickConversions() {
        if (convertsInWater()) {
            if (drowning.tick(isEyeInWater())) {
                convertInto(drownedForm(server().entities().ids().allocate(), location()), drownedSound());
                return;
            }
            if (sentDrowning != drowning.isConverting()) {
                sentDrowning = drowning.isConverting();
                sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                        ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_DROWNED_CONVERSION, sentDrowning)));
            }
        }
        if (convertsWhenFreezing()) {
            if (freezing.tick(isInPowderSnow())) {
                convertInto(frozenForm(server().entities().ids().allocate(), location()), frozenSound());
                return;
            }
            if (sentFreezing != freezing.isConverting()) {
                sentFreezing = freezing.isConverting();
                sendToTrackers(ClientboundSetEntityMetadataPacket.of(entityId(),
                        ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_FROSTBITE_CONVERSION, sentFreezing)));
            }
        }
    }

    /**
     * Turns this zombie into another, as vanilla converts it: the new one is a baby if this one is,
     * breaks doors if this one does, and gets attributes of its own.
     */
    private void convertInto(final Zombie converted, final Sound.Type sound) {
        converted.setBaby(baby);
        if (canBreakDoors) {
            converted.setCanBreakDoors(true);
        }
        converted.rollAttributes(localDifficulty().specialMultiplier(), false);
        convertTo(converted);
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final float first = random.nextFloat();
        final float second = random.nextFloat();
        converted.playPositionalSound(sound, CONVERSION_VOLUME, (first - second) * 0.2f + 1.0f);
    }

    @Override
    public boolean attack(final AbstractLivingEntity target) {
        if (!super.attack(target)) {
            return false;
        }
        // with nothing in its hands, a burning zombie may set fire to what it hits
        final float difficulty = localDifficulty().effective();
        if (fireTicks() > 0 && ThreadLocalRandom.current().nextFloat() < difficulty * FIRE_ON_HIT_CHANCE) {
            target.igniteForTicks(2 * (int) difficulty * TICKS_PER_SECOND);
        }
        onAttackLanded(target);
        return true;
    }

    /**
     * Called when a blow of this zombie hurt its target.
     */
    protected void onAttackLanded(final AbstractLivingEntity target) {
    }

    @Override
    public void onHurt(final DamageSource source, final float amount) {
        super.onHurt(source, amount);
        AbstractLivingEntity enemy = target();
        if (enemy == null && source.causingEntity() instanceof final AbstractLivingEntity attacker) {
            enemy = attacker;
        }
        if (enemy != null && enemy != this && difficulty() == LocalDifficulty.HARD
                && ThreadLocalRandom.current().nextFloat() < reinforcementChance()
                && serverWorld().gameRuleValues().getBoolean(GameRuleKeys.SPAWN_MONSTERS)) {
            callReinforcement(enemy);
        }
    }

    /**
     * Makes another zombie of the same kind spawn nearby and go after an enemy: 50 tries at a
     * place 7 to 40 blocks away along each axis, dark enough for a monster, free, and with no
     * player within 7 blocks. This zombie and the new one then call less often.
     *
     * @return {@code true} if a reinforcement came
     */
    public final boolean callReinforcement(final AbstractLivingEntity enemy) {
        final ServerWorld world = serverWorld();
        final Location self = location();
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < REINFORCEMENT_ATTEMPTS; attempt++) {
            final int x = (int) Math.floor(self.x()) + reinforcementOffset(random);
            final int y = (int) Math.floor(self.y()) + reinforcementOffset(random);
            final int z = (int) Math.floor(self.z()) + reinforcementOffset(random);
            if (!isReinforcementSpot(world, x, y, z)) {
                continue;
            }

            final AbstractMob created = MobFactories.create(type(), server().entities().ids().allocate(),
                    Location.of(world, x + 0.5, y, z + 0.5, 0f, 0f));
            if (!(created instanceof final Zombie reinforcement)) {
                return false;
            }
            reinforcement.setTarget(enemy);
            // the spot may belong to another region than the one this zombie ticks in
            final ChunkPos chunk = reinforcement.location().chunk();
            if (world.scheduler().isOwnedByCurrentThread(world.key(), chunk)) {
                server().entities().spawn(reinforcement);
            } else if (!world.scheduler().execute(world.key(), chunk, () -> server().entities().spawn(reinforcement))) {
                return false;
            }
            reinforcementsAttribute.put(REINFORCEMENT_CALLER_CHARGE,
                    reinforcementsAttribute.amount(REINFORCEMENT_CALLER_CHARGE) + REINFORCEMENT_CHARGE,
                    MobAttribute.Operation.ADD_VALUE);
            reinforcement.reinforcementsAttribute.put(REINFORCEMENT_CALLEE_CHARGE, REINFORCEMENT_CHARGE,
                    MobAttribute.Operation.ADD_VALUE);
            return true;
        }
        return false;
    }

    private static int reinforcementOffset(final ThreadLocalRandom random) {
        return random.nextInt(REINFORCEMENT_MIN_OFFSET, REINFORCEMENT_MAX_OFFSET + 1) * random.nextInt(-1, 2);
    }

    /**
     * {@return {@code true} if a reinforcement of this kind of zombie may spawn with its feet in a
     * block}: on solid ground, in the dark, with room for it and no water
     */
    protected boolean canReinforcementSpawnAt(final ServerWorld world, final int x, final int y, final int z) {
        if (!BlockView.isSolidGround(world, x, y - 1, z) || !isDarkEnoughToSpawn(world, x, y, z)) {
            return false;
        }
        for (int dy = 0; dy < 2; dy++) {
            final BlockState state = BlockView.blockAt(world, x, y + dy, z);
            if (state == null || !BlockView.isPassable(state) || BlockView.isWater(state)) {
                return false;
            }
        }
        return true;
    }

    private boolean isReinforcementSpot(final ServerWorld world, final int x, final int y, final int z) {
        if (!canReinforcementSpawnAt(world, x, y, z)) {
            return false;
        }
        for (final ServerPlayer player : server().players().online()) {
            if (player.isDead() || !player.world().equals(world)) {
                continue;
            }
            final Location at = player.location();
            final double dx = at.x() - x;
            final double dy = at.y() - y;
            final double dz = at.z() - z;
            if (dx * dx + dy * dy + dz * dz < REINFORCEMENT_PLAYER_CLEARANCE * REINFORCEMENT_PLAYER_CLEARANCE) {
                return false;
            }
        }
        return true;
    }

    /**
     * A villager killed by a zombie comes back as a zombie villager: always on hard difficulty, one
     * time out of two on normal, never on easy.
     */
    @Override
    public void onKilledEntity(final AbstractLivingEntity victim) {
        if (!(victim instanceof final Villager villager)) {
            return;
        }
        final int difficulty = difficulty();
        if (difficulty < LocalDifficulty.NORMAL
                || (difficulty != LocalDifficulty.HARD && ThreadLocalRandom.current().nextBoolean())) {
            return;
        }
        final ZombieVillager infected = new ZombieVillager(server().entities().ids().allocate(), villager.location());
        server().entities().despawn(villager);
        server().entities().spawn(infected);
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final float first = random.nextFloat();
        final float second = random.nextFloat();
        playPositionalSound(SoundEvents.ZOMBIE_INFECT, CONVERSION_VOLUME, (first - second) * 0.2f + 1.0f);
    }

    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one rotten flesh per level and a hundredth to the rare loot
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int flesh = random.nextInt(LOOT_MAX_ROTTEN_FLESH + 1);
        if (flesh > 0) {
            dropLootItem(ItemStack.of(ItemKeys.ROTTEN_FLESH, flesh));
        }
        if (wasRecentlyHurtByPlayer() && random.nextFloat() < rareLootChance()) {
            dropLootItem(ItemStack.of(rareLoot(), 1));
        }
    }

    /**
     * {@return the chance, between {@code 0} and {@code 1}, that this zombie leaves its
     * {@linkplain #rareLoot() rare loot} when a player kills it}
     */
    protected float rareLootChance() {
        return RARE_LOOT_CHANCE;
    }

    /**
     * {@return the item this zombie leaves now and then when a player kills it}: an iron ingot, a
     * carrot or a potato, baked if the zombie burns
     */
    protected TypedKey<Item> rareLoot() {
        return switch (ThreadLocalRandom.current().nextInt(3)) {
            case 0 -> ItemKeys.IRON_INGOT;
            case 1 -> ItemKeys.CARROT;
            default -> fireTicks() > 0 ? ItemKeys.BAKED_POTATO : ItemKeys.POTATO;
        };
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsBaby", baby);
        tag.putBoolean("CanBreakDoors", canBreakDoors);
        if (convertsInWater()) {
            drowning.save(tag);
        }
        if (convertsWhenFreezing()) {
            freezing.save(tag);
        }
        MobAttribute.save(tag, maxHealthAttribute, followRangeAttribute, knockbackResistanceAttribute,
                reinforcementsAttribute);
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        setBaby(tag.getBoolean("IsBaby", false));
        setCanBreakDoors(tag.getBoolean("CanBreakDoors", false));
        drowning.load(tag);
        freezing.load(tag);
        // a zombie read back keeps nothing of what was rolled when it was created
        followRangeAttribute.remove(RANDOM_SPAWN_BONUS);
        followRangeAttribute.remove(ZOMBIE_RANDOM_SPAWN_BONUS);
        knockbackResistanceAttribute.remove(RANDOM_SPAWN_BONUS);
        maxHealthAttribute.remove(LEADER_ZOMBIE_BONUS);
        setReinforcementChance(0.0);
        MobAttribute.load(tag, maxHealthAttribute, followRangeAttribute, knockbackResistanceAttribute,
                reinforcementsAttribute);
        setMaxHealth((float) maxHealthAttribute.value());
        final float health = tag.getFloat("Health", maxHealth());
        if (health > 0f) {
            setHealth(health);
        }
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_BABY, baby),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_DROWNED_CONVERSION, drowning.isConverting()),
                ClientboundSetEntityMetadataPacket.Entry.ofBoolean(MD_FROSTBITE_CONVERSION, freezing.isConverting())));
    }
}
