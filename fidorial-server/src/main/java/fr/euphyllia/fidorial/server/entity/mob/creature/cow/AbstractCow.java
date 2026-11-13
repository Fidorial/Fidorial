package fr.euphyllia.fidorial.server.entity.mob.creature.cow;

import fr.euphyllia.fidorial.server.entity.ai.PathPenalty;
import fr.euphyllia.fidorial.server.entity.ai.goal.BreedGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FloatGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.FollowParentGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.LookAtPlayerGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.PanicGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomLookAroundGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.RandomStrollGoal;
import fr.euphyllia.fidorial.server.entity.ai.goal.TemptGoal;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAnimal;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.fidorial.entity.EntityType;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.sound.Sound;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * What the cow and the mooshroom share, as vanilla's {@code AbstractCow}: they stroll, look at
 * players, follow wheat, panic when hurt, breed when fed wheat, give milk into a bucket, and leave
 * leather and beef.
 */
public abstract class AbstractCow extends AbstractAnimal {

    public static final float MAX_HEALTH = 10f;

    private static final double ADULT_HEIGHT = 1.4;
    private static final double ADULT_WIDTH = 0.9;
    private static final double ADULT_EYE_HEIGHT = 1.3;
    private static final double BABY_HEIGHT = 0.7;
    private static final double BABY_WIDTH = 0.45;
    private static final double BABY_EYE_HEIGHT = 0.69;

    /**
     * Vanilla's {@code movement_speed} attribute of the cows; the goals multiply it by their modifier.
     */
    private static final double MOVEMENT_SPEED = 0.2;
    private static final double PANIC_SPEED = MOVEMENT_SPEED * 2.0;
    private static final double BREED_SPEED = MOVEMENT_SPEED;
    private static final double TEMPT_SPEED = MOVEMENT_SPEED * 1.25;
    private static final double FOLLOW_PARENT_SPEED = MOVEMENT_SPEED * 1.25;
    private static final double STROLL_SPEED = MOVEMENT_SPEED;

    private static final double LOOK_AT_PLAYER_RANGE = 6.0;

    private static final float SOUND_VOLUME = 0.4f;
    private static final float STEP_VOLUME = 0.15f;

    private static final int MAX_LEATHER = 2;
    private static final int MIN_BEEF = 1;
    private static final int MAX_BEEF = 3;

    private final PanicGoal panicGoal;

    protected AbstractCow(final int entityId, final EntityType type, final Location location, final boolean baby) {
        super(entityId, UUID.randomUUID(), type, location, MAX_HEALTH, baby);
        navigation.setPathPenalty(PathPenalty.LAND_ANIMAL);

        this.panicGoal = new PanicGoal(this, 1, PANIC_SPEED, DamageTypeTagKeys.PANIC_CAUSES);
        goals.add(new FloatGoal(this, 0));
        goals.add(panicGoal);
        goals.add(new BreedGoal(this, 2, BREED_SPEED));
        goals.add(new TemptGoal(this, 3, TEMPT_SPEED, this::isFood));
        goals.add(new FollowParentGoal(this, 4, FOLLOW_PARENT_SPEED));
        goals.add(new RandomStrollGoal(this, 5, STROLL_SPEED));
        goals.add(new LookAtPlayerGoal(this, 6, LOOK_AT_PLAYER_RANGE));
        goals.add(new RandomLookAroundGoal(this, 7));
    }

    @Override
    protected final double adultHeight() {
        return ADULT_HEIGHT;
    }

    @Override
    protected final double adultWidth() {
        return ADULT_WIDTH;
    }

    @Override
    protected final double babyHeight() {
        return BABY_HEIGHT;
    }

    @Override
    protected final double babyWidth() {
        return BABY_WIDTH;
    }

    @Override
    public final double eyeHeight() {
        return isBaby() ? BABY_EYE_HEIGHT : ADULT_EYE_HEIGHT;
    }

    @Override
    public final double movementSpeed() {
        return MOVEMENT_SPEED;
    }

    @Override
    protected final double defaultFollowRange() {
        return 0.0;
    }

    @Override
    public final boolean isFood(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.COW_FOOD);
    }

    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (!held.isEmpty() && held.id().equals(ItemKeys.BUCKET) && !isBaby()) {
            final Location at = player.location();
            // the player hears the sound from their own client, which predicts the click
            player.sendToTrackers(new ClientboundSoundPacket(
                    Sound.sound(SoundEvents.COW_MILK, Sound.Source.PLAYER, 1.0f, 1.0f), at.x(), at.y(), at.z()));
            fillItem(player, slot, ItemStack.of(ItemKeys.MILK_BUCKET, 1), true);
            return true;
        }
        return super.interact(player, hand);
    }

    @Override
    public final boolean isPanicking() {
        return panicGoal.isPanicking();
    }

    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one more of each per level, once loot knows what killed the mob
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final int leather = random.nextInt(MAX_LEATHER + 1);
        if (leather > 0) {
            dropLootItem(ItemStack.of(ItemKeys.LEATHER, leather));
        }
        final int beef = random.nextInt(MIN_BEEF, MAX_BEEF + 1);
        dropLootItem(ItemStack.of(fireTicks() > 0 ? ItemKeys.COOKED_BEEF : ItemKeys.BEEF, beef));
    }

    /**
     * {@return {@code true} if this cow has the moody voice rather than the classic one}
     */
    protected boolean isMoody() {
        return false;
    }

    @Override
    protected final void onStep() {
        playSound(isMoody() ? SoundEvents.COW_MOODY_STEP : SoundEvents.COW_STEP, STEP_VOLUME, 1.0f);
    }

    @Override
    protected final float soundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    protected final Sound.Type ambientSound() {
        return isMoody() ? SoundEvents.COW_MOODY_AMBIENT : SoundEvents.COW_AMBIENT;
    }

    @Override
    protected final Sound.Type hurtSound() {
        return isMoody() ? SoundEvents.COW_MOODY_HURT : SoundEvents.COW_HURT;
    }

    @Override
    protected final Sound.Type deathSound() {
        return isMoody() ? SoundEvents.COW_MOODY_DEATH : SoundEvents.COW_DEATH;
    }
}
