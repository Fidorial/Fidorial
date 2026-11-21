package fr.euphyllia.fidorial.server.entity.mob.monster.cube;

import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.fidorial.entity.mob.monster.SulfurCube;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The sulfur cube: a cube that hurts no one and chases nothing. It is born a baby of size 1 and
 * grows up into an adult of size 2, sooner when fed slime balls; health is four times its size.
 * A baby follows the players holding slime balls, an adult those holding a block it could swallow.
 * Once dead, an adult splits into two babies.
 *
 * <p>It stays in a peaceful world, and sounds like a neutral mob.</p>
 */
public final class SulfurCubeImpl extends AbstractCubeMob implements SulfurCube {

    private static final double BASE_EYE_HEIGHT = 0.175;
    private static final float HEALTH_PER_SIZE = 4.0f;
    private static final int SPLIT_COUNT = 2;
    private static final int TEMPT_PRIORITY = 2;
    private static final double TEMPT_RANGE = 8.0;
    private static final double TEMPT_STOP_DISTANCE = 1.0;
    private static final int MAX_EXTRA_EXPERIENCE = 1;

    /**
     * Creates an adult sulfur cube as one that spawns.
     */
    public SulfurCubeImpl(final int entityId, final Location location) {
        this(entityId, location, true);
    }

    private SulfurCubeImpl(final int entityId, final Location location, final boolean spawned) {
        super(entityId, EntityTypes.SULFUR_CUBE, location, spawned);
        goals.add(new CubeTemptGoal(TEMPT_PRIORITY, this::isTempting, TEMPT_RANGE, TEMPT_STOP_DISTANCE));
    }

    @Override
    protected AbstractCubeMob newSplitCube(final int entityId, final Location location) {
        return new SulfurCubeImpl(entityId, location, false);
    }

    /**
     * {@return {@code true} if an item tempts this cube}: slime balls for a baby, the blocks it
     * could swallow for an adult
     */
    private boolean isTempting(final ItemStack stack) {
        return isBaby() ? isFood(stack) : isSwallowable(stack);
    }

    private boolean isFood(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.SULFUR_CUBE_FOOD);
    }

    private boolean isSwallowable(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.SULFUR_CUBE_SWALLOWABLE);
    }

    /**
     * A baby fed a slime ball grows up a tenth of the time it has left.
     */
    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        if (isBaby()) {
            final int slot = handSlot(player, hand);
            final ItemStack held = player.inventory().get(slot);
            if (!held.isEmpty() && isFood(held) && canAgeUp()) {
                consumeItem(player, slot);
                ageUp(speedUpSecondsWhenFeeding(-age()), true);
                playSound(SoundEvents.SMALL_SULFUR_CUBE_EAT, soundVolume(), voicePitch());
                return true;
            }
        }
        return super.interact(player, hand);
    }

    /**
     * {@inheritDoc} A sulfur cube made of size 1 at full health becomes a baby.
     */
    @Override
    public void setSize(final int size, final boolean healed) {
        super.setSize(size, healed);
        if (healed && size == MIN_SIZE && !isBaby()) {
            setBaby(true);
        }
    }

    @Override
    protected float maxHealthFor(final int size) {
        return HEALTH_PER_SIZE * size;
    }

    /**
     * A sulfur cube that spawns is of size 2, or 1 for a baby.
     */
    @Override
    protected void setSpawnSize() {
        setSize(isBaby() ? MIN_SIZE : ADULT_SIZE, true);
    }

    /**
     * A baby that grows up takes the size of an adult, at full health.
     */
    @Override
    protected void onAgeBoundaryReached() {
        super.onAgeBoundaryReached();
        if (!isBaby()) {
            setSize(ADULT_SIZE, true);
        }
    }

    @Override
    protected int splitCount() {
        return SPLIT_COUNT;
    }

    @Override
    protected void setUpSplitCube(final AbstractCubeMob cube) {
        cube.setBaby(true);
    }

    @Override
    protected boolean canDealDamage() {
        return false;
    }

    @Override
    protected boolean despawnsInPeaceful() {
        return false;
    }

    @Override
    public Sound.Source soundSource() {
        return Sound.Source.NEUTRAL;
    }

    @Override
    protected int experienceReward() {
        return isBaby() ? 0 : 1 + ThreadLocalRandom.current().nextInt(MAX_EXTRA_EXPERIENCE + 1);
    }

    @Override
    protected double baseEyeHeight() {
        return BASE_EYE_HEIGHT;
    }

    @Override
    protected Sound.Type jumpSound() {
        return isTiny() ? SoundEvents.SMALL_SULFUR_CUBE_JUMP : SoundEvents.SULFUR_CUBE_JUMP;
    }

    @Override
    protected Sound.Type squishSound() {
        return isTiny() ? SoundEvents.SMALL_SULFUR_CUBE_SQUISH : SoundEvents.SULFUR_CUBE_SQUISH;
    }

    @Override
    protected Sound.Type hurtSound() {
        return isTiny() ? SoundEvents.SMALL_SULFUR_CUBE_HURT : SoundEvents.SULFUR_CUBE_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return isTiny() ? SoundEvents.SMALL_SULFUR_CUBE_DEATH : SoundEvents.SULFUR_CUBE_DEATH;
    }
}
