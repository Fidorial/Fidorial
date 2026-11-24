package fr.euphyllia.fidorial.server.entity.mob.monster.cube;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.goal.NearestAttackableTargetGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractMonster;
import fr.euphyllia.fidorial.server.entity.mob.misc.IronGolem;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.mob.monster.MagmaCube;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The magma cube: the cube of the Nether. It goes after players and iron golems as the slime does,
 * but hops four times less often and higher the bigger it is, bounces up out of lava, never burns,
 * wears three points of armor per size, and hurts what it touches even when tiny, two points harder
 * than a slime.
 */
public final class MagmaCubeEntity extends AbstractCubeMob implements MagmaCube {

    private static final double BASE_EYE_HEIGHT = 0.325;
    private static final double TARGET_HEIGHT_RANGE = 4.0;
    private static final int TARGET_INTERVAL = 10;

    private static final int JUMP_DELAY_FACTOR = 4;
    /**
     * How much higher a magma cube jumps off the ground for each size.
     */
    private static final float JUMP_BOOST_PER_SIZE = 0.1f;
    /**
     * How fast a magma cube jumps up in lava: this much, and more for each size.
     */
    private static final float LAVA_JUMP_VELOCITY = 0.22f;
    private static final float LAVA_JUMP_PER_SIZE = 0.05f;

    private static final int ARMOR_PER_SIZE = 3;
    private static final int MAX_ARMOR = 30;
    private static final float EXTRA_DAMAGE = 2.0f;

    /**
     * The size last asked for, before it was kept between 1 and 127: the armor follows it. Assigned
     * by {@link #setSize} while the parent class is built, so it has no initializer.
     */
    private int requestedSize;

    /**
     * Creates a magma cube as one that spawns, with the random traits of one and a random size.
     */
    public MagmaCubeEntity(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    private MagmaCubeEntity(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.MAGMA_CUBE, location, spawned);
        goals.add(new AttackGoal(2));
        goals.add(new NearestAttackableTargetGoal<>(this, 1, ServerPlayer.class, TARGET_INTERVAL, true,
                player -> Math.abs(player.location().y() - location().y()) <= TARGET_HEIGHT_RANGE));
        goals.add(new NearestAttackableTargetGoal<>(this, 3, IronGolem.class, true));
    }

    @Override
    protected AbstractCubeMob newSplitCube(final int entityId, final Location location) {
        return new MagmaCubeEntity(entityId, location, false);
    }

    /**
     * {@return {@code true} if a magma cube may spawn on a block}: on solid ground, in any light,
     * unless the world is peaceful
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return AbstractMonster.checkAnyLightMonsterSpawnRules(world, x, y, z);
    }

    @Override
    public void setSize(final int size, final boolean healed) {
        this.requestedSize = size;
        super.setSize(size, healed);
    }

    /**
     * {@return three points of armor per size asked for, 30 at most}
     */
    @Override
    public double armor() {
        return Math.clamp((long) requestedSize * ARMOR_PER_SIZE, 0, MAX_ARMOR);
    }

    @Override
    public float attackDamage() {
        return size() + EXTRA_DAMAGE;
    }

    @Override
    protected int experienceReward() {
        return size();
    }

    @Override
    protected double baseEyeHeight() {
        return BASE_EYE_HEIGHT;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    /**
     * {@return {@code true}}: a magma cube hurts what it touches, however small
     */
    @Override
    protected boolean canDealDamage() {
        return true;
    }

    @Override
    protected int jumpDelayTicks() {
        return super.jumpDelayTicks() * JUMP_DELAY_FACTOR;
    }

    @Override
    protected double jumpVelocity() {
        return super.jumpVelocity() + size() * JUMP_BOOST_PER_SIZE;
    }

    /**
     * {@return in lava, a bounce that does not add up and grows with the size}; in water, the jump
     * of any mob
     */
    @Override
    protected double swimUpVelocity(final double velocityY, final boolean inLava) {
        return inLava ? LAVA_JUMP_VELOCITY + size() * LAVA_JUMP_PER_SIZE : super.swimUpVelocity(velocityY, false);
    }

    /**
     * Drops what a magma cube leaves: a cube bigger than a tiny one leaves a magma cream one time in
     * four.
     */
    @Override
    protected void dropLoot() {
        if (isTiny()) {
            return;
        }
        // TODO: Looting adds up to one more per level; one a frog eats leaves a froglight instead
        // -2 to 1: nothing three times in four
        final int count = ThreadLocalRandom.current().nextInt(4) - 2;
        if (count > 0) {
            dropLootItem(ItemStack.of(ItemKeys.MAGMA_CREAM, count));
        }
    }

    @Override
    protected Sound.Type jumpSound() {
        return SoundEvents.MAGMA_CUBE_JUMP;
    }

    @Override
    protected Sound.Type squishSound() {
        return isTiny() ? SoundEvents.MAGMA_CUBE_SQUISH_SMALL : SoundEvents.MAGMA_CUBE_SQUISH;
    }

    @Override
    protected Sound.Type hurtSound() {
        return isTiny() ? SoundEvents.MAGMA_CUBE_HURT_SMALL : SoundEvents.MAGMA_CUBE_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return isTiny() ? SoundEvents.MAGMA_CUBE_DEATH_SMALL : SoundEvents.MAGMA_CUBE_DEATH;
    }
}
