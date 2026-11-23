package fr.euphyllia.fidorial.server.entity.mob.monster.zombie;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.AbstractLivingEntity;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.ai.Brightness;
import fr.euphyllia.fidorial.server.entity.ai.goal.HurtByTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMovingMob;
import fr.euphyllia.fidorial.server.entity.mob.creature.WanderingTrader;
import fr.euphyllia.fidorial.server.entity.mob.misc.IronGolem;
import fr.euphyllia.fidorial.server.entity.mob.misc.Villager;
import fr.euphyllia.fidorial.server.entity.mob.monster.ZombifiedPiglin;
import fr.euphyllia.fidorial.server.entity.mob.water_creature.Axolotl;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.fidorial.entity.mob.monster.Drowned;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.BiomeTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The drowned, the zombie of the waters. It walks on the bottom as on land, and swims, up and down
 * as well, to a target that is in the water or, at night, to the surface. By day it goes back to
 * the water and only cares about what is in it; at night it comes ashore and hunts like a zombie.
 * Water does not change it; powder snow turns it into a zombie.
 */
public final class DrownedImpl extends ZombieImpl implements Drowned {

    public static final float MAX_HEALTH = 20f;

    private static final double STEP_HEIGHT = 1.0;
    private static final double SINKING_PULL = 0.008;
    private static final byte FLAG_SWIMMING = 0x10;
    private static final float RARE_LOOT_CHANCE = 0.11f;

    private static final int RIVER_SPAWN_CHANCE = 15;
    private static final int SEA_SPAWN_CHANCE = 40;
    private static final int SEA_SPAWN_DEPTH = 5;

    private boolean searchingForLand;
    private boolean sentSwimming;

    public DrownedImpl(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    /**
     * @param spawned {@code true} for a drowned that spawns, with the random traits of a zombie that
     *                does
     */
    public DrownedImpl(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.DROWNED, location, spawned, false);
        navigation.setSwims(true);

        goals.add(new DrownedGoToWaterGoal(this, 1, 1.0));
        goals.add(new DrownedAttackGoal(this, 2, 1.0, false));
        goals.add(new DrownedGoToBeachGoal(this, 5, 1.0));
        goals.add(new DrownedSwimUpGoal(this, 6, 1.0));
        goals.add(new RandomStrollGoal(this, 7, this::movementSpeed, RandomStrollGoal.Terrain.WATER_TOO));
        goals.add(new HurtByTargetGoal(this, 1).ignoringDamageFrom(Drowned.class).alertOthers(ZombifiedPiglin.class));
        goals.add(new NearestAttackableTargetGoal<>(this, 2, ServerPlayer.class, 10, true, this::okTarget));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, Villager.class, false));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, WanderingTrader.class, false));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, IronGolem.class, true));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, Axolotl.class, true));
    }

    /**
     * {@return {@code true} if drowned may spawn in a block}: in water with water under it, in the
     * dark, one time out of 15 in a river, and elsewhere one time out of 40 and only more than 5
     * blocks under the surface of the sea
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        if (!isWater(world, x, y - 1, z) || !isWater(world, x, y, z) || !isDarkEnoughToSpawn(world, x, y, z)) {
            return false;
        }
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final TypedKey<BiomeDefinition> biome = BlockView.biomeAt(world, x, y, z);
        if (biome != null && FidorialServer.getInstance().registries().registry(RegistryKey.BIOME)
                .isTagged(biome, BiomeTagKeys.MORE_FREQUENT_DROWNED_SPAWNS)) {
            return random.nextInt(RIVER_SPAWN_CHANCE) == 0;
        }
        return random.nextInt(SEA_SPAWN_CHANCE) == 0 && y < world.seaLevel() - SEA_SPAWN_DEPTH;
    }

    private static boolean isWater(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState state = BlockView.blockAt(world, x, y, z);
        return state != null && state.isWater();
    }

    /**
     * {@return {@code true} if this drowned cares about a target}: any at night, and by day only one
     * that is in the water
     */
    public boolean okTarget(final @Nullable AbstractLivingEntity target) {
        return target != null && (!Brightness.isDay(serverWorld()) || isInWater(target));
    }

    private static boolean isInWater(final AbstractLivingEntity entity) {
        if (entity instanceof final AbstractMovingMob mob) {
            return mob.isInWater();
        }
        if (!(entity.world() instanceof final ServerWorld world)) {
            return false;
        }
        final Location at = entity.location();
        return isWater(world, (int) Math.floor(at.x()), (int) Math.floor(at.y()), (int) Math.floor(at.z()));
    }

    /**
     * {@return {@code true} while this drowned swims up at night, looking for the shore}
     */
    @Override
    public boolean isSearchingForLand() {
        return searchingForLand;
    }

    void setSearchingForLand(final boolean searchingForLand) {
        this.searchingForLand = searchingForLand;
    }

    /**
     * {@return {@code true} if this drowned is shown swimming}: under water, on its way to a target
     * in the water or to the surface
     */
    public boolean isSwimming() {
        return sentSwimming;
    }

    /**
     * {@return {@code true} unless this drowned swims}: a swimming drowned goes against the current
     */
    @Override
    protected boolean isPushedByFluid() {
        return !isSwimming();
    }

    @Override
    protected boolean wantsToSwim() {
        if (searchingForLand) {
            return true;
        }
        final AbstractLivingEntity target = target();
        return target != null && isInWater(target);
    }

    @Override
    protected boolean risesWhileSwimming() {
        final AbstractLivingEntity target = target();
        return searchingForLand || (target != null && target.location().y() > location().y());
    }

    @Override
    protected double sinkingPull() {
        return SINKING_PULL;
    }

    @Override
    protected double stepHeight() {
        return STEP_HEIGHT;
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }
        final boolean swimming = isEyeInWater() && wantsToSwim();
        if (swimming != sentSwimming) {
            sentSwimming = swimming;
            refreshSharedFlags();
        }
    }

    @Override
    protected byte sharedFlags(final boolean onFire) {
        return (byte) (super.sharedFlags(onFire) | (sentSwimming ? FLAG_SWIMMING : 0));
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected ZombieImpl frozenForm(final int entityId, final Location location) {
        return new ZombieImpl(entityId, location, false);
    }

    @Override
    protected Sound.Type frozenSound() {
        return SoundEvents.DROWNED_CONVERTED_TO_ZOMBIE;
    }

    /**
     * A reinforcement of a drowned spawns in the water: in a block of water with water under it, in
     * the dark, with room for its head.
     */
    @Override
    protected boolean canReinforcementSpawnAt(final ServerWorld world, final int x, final int y, final int z) {
        return isWater(world, x, y - 1, z) && isWater(world, x, y, z)
                && BlockView.isPassable(world, x, y + 1, z) && isDarkEnoughToSpawn(world, x, y, z);
    }

    @Override
    protected float rareLootChance() {
        return RARE_LOOT_CHANCE;
    }

    @Override
    protected TypedKey<Item> rareLoot() {
        return ItemKeys.COPPER_INGOT;
    }

    @Override
    protected Sound.Type ambientSound() {
        return isInWater() ? SoundEvents.DROWNED_AMBIENT_WATER : SoundEvents.DROWNED_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return isInWater() ? SoundEvents.DROWNED_HURT_WATER : SoundEvents.DROWNED_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return isInWater() ? SoundEvents.DROWNED_DEATH_WATER : SoundEvents.DROWNED_DEATH;
    }

    @Override
    protected Sound.Type stepSound() {
        return SoundEvents.DROWNED_STEP;
    }
}
