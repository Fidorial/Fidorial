package fr.euphyllia.fidorial.server.entity.mob;

import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.player.ServerPlayer;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundEntityEventPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import fr.euphyllia.fidorial.server.world.entity.AnvilEntitySerializer;
import fr.fidorial.combat.DamageSource;
import fr.fidorial.entity.EntityType;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.keys.BlockTypeKeys;
import fr.fidorial.registry.keys.BlockTypeTagKeys;
import fr.fidorial.registry.keys.GameRuleKeys;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A farm animal as vanilla's {@code Animal}: it is fed to fall in love or to grow faster, breeds
 * with one of its kind, and makes its ambient sound now and then.
 */
public abstract class AbstractAnimal extends AbstractAgeableMob {

    /**
     * For how long an animal that just bred cannot breed again.
     */
    public static final int PARENT_AGE_AFTER_BREEDING = 6000;
    /**
     * For how long a fed animal looks for a partner.
     */
    public static final int LOVE_TICKS = 600;
    /**
     * The lowest light, sky and block light together, animals spawn in: vanilla wants more than 8.
     */
    public static final int MIN_SPAWN_LIGHT = 9;

    private static final int MAX_BREEDING_EXPERIENCE = 7;
    private static final int MAX_EXPERIENCE_REWARD = 3;

    private static final byte ENTITY_EVENT_LOVE_HEARTS = 18;
    private static final int AMBIENT_SOUND_INTERVAL = 120;
    private static final int AMBIENT_SOUND_RANGE = 1000;
    private static final float GRASS_WALK_VALUE = 10.0f;

    private int inLove;
    private @Nullable UUID loveCause;
    private int ambientSoundTime;

    protected AbstractAnimal(final int entityId, final UUID uuid, final EntityType type,
                             final Location location, final float maxHealth, final boolean baby) {
        super(entityId, uuid, type, location, maxHealth, baby);
    }

    /**
     * {@return {@code true} if animals may spawn on a block}: as in Vanilla, on a block of the
     * {@code animals_spawnable_on} tag, in a light above 8
     */
    public static boolean checkAnimalSpawnRules(final ServerWorld world, final int x, final int y, final int z) {
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        return ground != null
                && BlockView.isTagged(BlockTypeTagKeys.ANIMALS_SPAWNABLE_ON, ground)
                && world.lightLevelAt(x, y, z) >= MIN_SPAWN_LIGHT;
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }
        if (age() != 0) {
            inLove = 0;
        }
        if (inLove > 0) {
            inLove--;
        }
        tickAmbientSound();
    }

    private void tickAmbientSound() {
        if (ThreadLocalRandom.current().nextInt(AMBIENT_SOUND_RANGE) >= ambientSoundTime++) {
            return;
        }
        ambientSoundTime = -AMBIENT_SOUND_INTERVAL;
        final Sound.Type sound = ambientSound();
        if (sound != null) {
            playSound(sound, soundVolume(), voicePitch());
        }
    }

    /**
     * {@return the sound this animal makes now and then, {@code null} for none}
     */
    protected Sound.@Nullable Type ambientSound() {
        return null;
    }

    /**
     * {@return {@code true} if this animal eats an item}: it then falls in love, or grows faster
     */
    public abstract boolean isFood(ItemStack stack);

    /**
     * {@return a newborn of this animal and a partner, not yet in the world; {@code null} for none}
     */
    protected abstract @Nullable AbstractAnimal createOffspring(AbstractAnimal partner);

    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (!held.isEmpty() && isFood(held)) {
            if (age() == 0 && canFallInLove()) {
                consumeItem(player, slot);
                setInLove(player);
                playEatingSound();
                return true;
            }
            if (canAgeUp()) {
                consumeItem(player, slot);
                ageUp(speedUpSecondsWhenFeeding(-age()), true);
                playEatingSound();
                return true;
            }
        }
        return super.interact(player, hand);
    }

    protected void playEatingSound() {
    }

    public final boolean canFallInLove() {
        return inLove <= 0;
    }

    public final boolean isInLove() {
        return inLove > 0;
    }

    public final int inLoveTime() {
        return inLove;
    }

    /**
     * Makes this animal look for a partner, hearts rising above it.
     *
     * @param cause the player who fed it, if any
     */
    public final void setInLove(final @Nullable ServerPlayer cause) {
        this.inLove = LOVE_TICKS;
        if (cause != null) {
            this.loveCause = cause.uuid();
        }
        sendToTrackers(new ClientboundEntityEventPacket(entityId(), ENTITY_EVENT_LOVE_HEARTS));
    }

    public final void resetLove() {
        this.inLove = 0;
    }

    /**
     * {@return the player who made this animal fall in love last, {@code null} if unknown}
     */
    public final @Nullable UUID loveCause() {
        return loveCause;
    }

    /**
     * {@return {@code true} if this animal and another may breed}: both of the same kind, both in love
     */
    public boolean canMate(final AbstractAnimal partner) {
        return partner != this
                && partner.getClass() == getClass()
                && isInLove() && partner.isInLove();
    }

    /**
     * {@return {@code true} while this animal runs away from what hurt it}; no animal breeds with it
     * meanwhile
     */
    public boolean isPanicking() {
        return false;
    }

    /**
     * Gives birth to the child of this animal and a partner, where this animal stands. As in Vanilla,
     * both parents then wait {@value #PARENT_AGE_AFTER_BREEDING} ticks before they breed again.
     */
    public final void spawnChildFromBreeding(final AbstractAnimal partner) {
        final AbstractAnimal offspring = createOffspring(partner);
        if (offspring == null) {
            return;
        }
        offspring.setBaby(true);
        setAge(PARENT_AGE_AFTER_BREEDING);
        partner.setAge(PARENT_AGE_AFTER_BREEDING);
        resetLove();
        partner.resetLove();
        sendToTrackers(new ClientboundEntityEventPacket(entityId(), ENTITY_EVENT_LOVE_HEARTS));
        final ServerWorld world = serverWorld();
        if (world.gameRuleValues().getBoolean(GameRuleKeys.MOB_DROPS)) {
            final Location at = location();
            server().entities().spawnExperienceOrb(world, at.x(), at.y(), at.z(),
                    1 + ThreadLocalRandom.current().nextInt(MAX_BREEDING_EXPERIENCE));
        }
        server().entities().spawn(offspring);
    }

    /**
     * {@return where a newborn of this animal appears}: where it stands, looking north
     */
    protected final Location birthLocation() {
        final Location at = location();
        return at.with(at.x(), at.y(), at.z(), 0f, 0f);
    }

    /**
     * {@return what a player gets for killing this animal}: 1 to 3 points, as in Vanilla
     */
    @Override
    protected int experienceReward() {
        return 1 + ThreadLocalRandom.current().nextInt(MAX_EXPERIENCE_REWARD);
    }

    @Override
    public void onHurt(final DamageSource source, final float amount) {
        resetLove();
        super.onHurt(source, amount);
    }

    /**
     * {@return how much this animal likes to walk to a block}: as in Vanilla, onto grass above all,
     * otherwise where it is brighter
     */
    @Override
    public float walkTargetValue(final int x, final int y, final int z) {
        final ServerWorld world = serverWorld();
        final BlockState ground = BlockView.blockAt(world, x, y - 1, z);
        if (ground != null && ground.name().equals(BlockTypeKeys.GRASS_BLOCK)) {
            return GRASS_WALK_VALUE;
        }
        final float light = world.lightLevelAt(x, y, z) / 15.0f;
        return light / (4.0f - 3.0f * light) - 0.5f;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putInt("InLove", inLove);
        if (loveCause != null) {
            tag.putIntArray("LoveCause", AnvilEntitySerializer.uuidToInts(loveCause));
        }
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        this.inLove = tag.getInt("InLove", 0);
        final int[] cause = tag.getIntArray("LoveCause");
        this.loveCause = cause.length == 4 ? AnvilEntitySerializer.uuidFromInts(cause) : null;
    }
}
