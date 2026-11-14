package fr.euphyllia.fidorial.server.entity.mob.creature.chicken;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
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
import fr.euphyllia.fidorial.server.entity.mob.creature.FarmAnimalVariants;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.ChickenSoundVariant;
import fr.fidorial.registry.data.ChickenVariant;
import fr.fidorial.registry.data.Item;
import fr.fidorial.registry.keys.ChickenSoundVariantKeys;
import fr.fidorial.registry.keys.ChickenVariantKeys;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.GameEventKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The chicken, as in Vanilla: it strolls, looks at players, follows seeds, panics when hurt, breeds
 * when fed seeds, flutters down when it falls and lays an egg every five to ten minutes. Its colour
 * comes from the biome it spawned in, and so does the colour of its eggs; its voice is classic or
 * picky.
 */
public final class Chicken extends AbstractAnimal implements Category.Neutral {

    public static final float MAX_HEALTH = 4f;

    private static final double ADULT_HEIGHT = 0.7;
    private static final double ADULT_WIDTH = 0.4;
    private static final double ADULT_EYE_HEIGHT = 0.644;
    private static final double BABY_HEIGHT = 0.4;
    private static final double BABY_WIDTH = 0.3;
    private static final double BABY_EYE_HEIGHT = 0.28125;

    /**
     * How much of its falling speed a chicken keeps each tick, as in Vanilla: it flutters down.
     */
    private static final double FLUTTER_FALL_DRAG = 0.6;

    /**
     * Vanilla's {@code movement_speed} attribute of the chicken; the goals multiply it by their
     * modifier.
     */
    private static final double MOVEMENT_SPEED = 0.25;
    private static final double PANIC_SPEED = MOVEMENT_SPEED * 1.4;
    private static final double BREED_SPEED = MOVEMENT_SPEED;
    private static final double TEMPT_SPEED = MOVEMENT_SPEED;
    private static final double FOLLOW_PARENT_SPEED = MOVEMENT_SPEED * 1.1;
    private static final double STROLL_SPEED = MOVEMENT_SPEED;

    private static final double LOOK_AT_PLAYER_RANGE = 6.0;
    private static final float STEP_VOLUME = 0.15f;

    /**
     * As in Vanilla, a chicken lays an egg every 6000 to 11999 ticks.
     */
    private static final int EGG_MIN_TICKS = 6000;
    private static final int EGG_EXTRA_TICKS = 6000;

    /**
     * Chicken entity data, after the 18 entries every ageable mob has.
     */
    private static final int MD_VARIANT = 18;
    private static final int MD_SOUND_VARIANT = 19;

    /**
     * The ids of the {@code chicken_variant} and {@code chicken_sound_variant} entity data serializers.
     */
    private static final int MD_TYPE_CHICKEN_VARIANT = 30;
    private static final int MD_TYPE_CHICKEN_SOUND_VARIANT = 31;

    private static final int MAX_FEATHERS = 2;
    private static final int JOCKEY_EXPERIENCE = 10;

    private final PanicGoal panicGoal;

    private TypedKey<ChickenVariant> variant = ChickenVariantKeys.TEMPERATE;
    private TypedKey<ChickenSoundVariant> soundVariant = ChickenSoundVariantKeys.CLASSIC;
    private int eggTime = nextEggTime();
    private boolean chickenJockey;

    /**
     * Spawns a chicken as vanilla does out of nowhere, with a spawn egg or {@code /summon}: an
     * adult, of the variant of the biome, with a random voice.
     */
    public Chicken(final int entityId, final Location location) {
        this(entityId, location, false);
        this.variant = FarmAnimalVariants.forBiome(serverWorld(), location,
                ChickenVariantKeys.TEMPERATE, ChickenVariantKeys.WARM, ChickenVariantKeys.COLD);
        this.soundVariant = FarmAnimalVariants.random(RegistryKey.CHICKEN_SOUND_VARIANT, ChickenSoundVariantKeys.CLASSIC);
    }

    /**
     * Creates a temperate chicken with the classic voice, as vanilla does before it sets up a
     * chicken: a newborn keeps that voice.
     */
    public Chicken(final int entityId, final Location location, final boolean baby) {
        super(entityId, UUID.randomUUID(), EntityTypes.CHICKEN, location, MAX_HEALTH, baby);
        navigation.setPathPenalty(PathPenalty.WADING_ANIMAL);

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

    /**
     * {@return {@code true} if chickens may spawn on a block}: as any animal, on grass in the light
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkAnimalSpawnRules(world, x, y, z);
    }

    private static int nextEggTime() {
        return EGG_MIN_TICKS + ThreadLocalRandom.current().nextInt(EGG_EXTRA_TICKS);
    }

    @Override
    public void tick(final long currentTick) {
        super.tick(currentTick);
        if (isRemoved() || isDead()) {
            return;
        }

        if (!isBaby() && !chickenJockey && --eggTime <= 0) {
            layEgg();
            eggTime = nextEggTime();
        }
    }

    /**
     * Lays an egg where this chicken stands, of the colour of its variant, as in Vanilla.
     */
    private void layEgg() {
        dropLootItem(ItemStack.of(egg(), 1));
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final float first = random.nextFloat();
        final float second = random.nextFloat();
        playSound(SoundEvents.CHICKEN_EGG, 1.0f, (first - second) * 0.2f + 1.0f);
        server().debug().gameEvents().emit(world(), GameEventKeys.ENTITY_PLACE, location());
    }

    /**
     * {@return the egg this chicken lays}: brown for the warm variant, blue for the cold one
     */
    public TypedKey<Item> egg() {
        if (variant.equals(ChickenVariantKeys.WARM)) {
            return ItemKeys.BROWN_EGG;
        }
        return variant.equals(ChickenVariantKeys.COLD) ? ItemKeys.BLUE_EGG : ItemKeys.EGG;
    }

    /**
     * {@return in how many ticks this chicken lays its next egg}
     */
    public int eggTime() {
        return eggTime;
    }

    public void setEggTime(final int ticks) {
        this.eggTime = ticks;
    }

    /**
     * {@return {@code true} if a baby zombie rides this chicken}: it then lays no egg and leaves 10
     * experience points
     */
    public boolean isChickenJockey() {
        return chickenJockey;
    }

    public void setChickenJockey(final boolean chickenJockey) {
        // TODO: a chicken jockey despawns far from players, once mobs despawn; nothing rides it yet
        this.chickenJockey = chickenJockey;
    }

    @Override
    protected double adultHeight() {
        return ADULT_HEIGHT;
    }

    @Override
    protected double adultWidth() {
        return ADULT_WIDTH;
    }

    @Override
    protected double babyHeight() {
        return BABY_HEIGHT;
    }

    @Override
    protected double babyWidth() {
        return BABY_WIDTH;
    }

    @Override
    public double eyeHeight() {
        return isBaby() ? BABY_EYE_HEIGHT : ADULT_EYE_HEIGHT;
    }

    @Override
    public double movementSpeed() {
        return MOVEMENT_SPEED;
    }

    @Override
    protected double defaultFollowRange() {
        return 0.0;
    }

    @Override
    protected double fallDrag() {
        return FLUTTER_FALL_DRAG;
    }

    public TypedKey<ChickenVariant> variant() {
        return variant;
    }

    public void setVariant(final TypedKey<ChickenVariant> variant) {
        this.variant = variant;
        sendToTrackers(variantMetadata());
    }

    public TypedKey<ChickenSoundVariant> soundVariant() {
        return soundVariant;
    }

    public void setSoundVariant(final TypedKey<ChickenSoundVariant> soundVariant) {
        this.soundVariant = soundVariant;
        sendToTrackers(variantMetadata());
    }

    private ClientboundSetEntityMetadataPacket variantMetadata() {
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                FarmAnimalVariants.metadata(MD_VARIANT, MD_TYPE_CHICKEN_VARIANT, RegistryKey.CHICKEN_VARIANT, variant),
                FarmAnimalVariants.metadata(MD_SOUND_VARIANT, MD_TYPE_CHICKEN_SOUND_VARIANT,
                        RegistryKey.CHICKEN_SOUND_VARIANT, soundVariant));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(variantMetadata());
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.CHICKEN_FOOD);
    }

    @Override
    protected @Nullable Chicken createOffspring(final AbstractAnimal partner) {
        final Chicken baby = new Chicken(server().entities().ids().allocate(), birthLocation(), true);
        if (partner instanceof final Chicken other) {
            baby.variant = ThreadLocalRandom.current().nextBoolean() ? variant : other.variant;
        }
        return baby;
    }

    @Override
    public boolean isPanicking() {
        return panicGoal.isPanicking();
    }

    @Override
    protected int experienceReward() {
        return chickenJockey ? JOCKEY_EXPERIENCE : super.experienceReward();
    }

    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one more of each per level, once loot knows what killed the mob
        final int feathers = ThreadLocalRandom.current().nextInt(MAX_FEATHERS + 1);
        if (feathers > 0) {
            dropLootItem(ItemStack.of(ItemKeys.FEATHER, feathers));
        }
        dropLootItem(ItemStack.of(fireTicks() > 0 ? ItemKeys.COOKED_CHICKEN : ItemKeys.CHICKEN, 1));
    }

    @Override
    protected void onStep() {
        playSound(voice().step, STEP_VOLUME, 1.0f);
    }

    @Override
    protected Sound.Type ambientSound() {
        return voice().ambient;
    }

    @Override
    protected Sound.Type hurtSound() {
        return voice().hurt;
    }

    @Override
    protected Sound.Type deathSound() {
        return voice().death;
    }

    /**
     * {@return the sounds this chicken makes}: those of its sound variant, or those every chick shares
     */
    private Voice voice() {
        if (isBaby()) {
            return Voice.BABY;
        }
        return soundVariant.equals(ChickenSoundVariantKeys.PICKY) ? Voice.PICKY : Voice.CLASSIC;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putBoolean("IsChickenJockey", chickenJockey);
        tag.putInt("EggLayTime", eggTime);
        tag.putString("variant", variant.key().asString());
        tag.putString("sound_variant", soundVariant.key().asString());
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        this.chickenJockey = tag.getBoolean("IsChickenJockey", false);
        this.eggTime = tag.getInt("EggLayTime", eggTime);
        this.variant = FarmAnimalVariants.saved(RegistryKey.CHICKEN_VARIANT, tag.getString("variant"), variant);
        this.soundVariant = FarmAnimalVariants.saved(RegistryKey.CHICKEN_SOUND_VARIANT, tag.getString("sound_variant"), soundVariant);
    }

    /**
     * The sounds of a chicken, as vanilla's chicken sound variants list them.
     */
    private enum Voice {
        CLASSIC(SoundEvents.CHICKEN_AMBIENT, SoundEvents.CHICKEN_HURT, SoundEvents.CHICKEN_DEATH, SoundEvents.CHICKEN_STEP),
        PICKY(SoundEvents.CHICKEN_PICKY_AMBIENT, SoundEvents.CHICKEN_PICKY_HURT, SoundEvents.CHICKEN_PICKY_DEATH,
                SoundEvents.CHICKEN_PICKY_STEP),
        BABY(SoundEvents.BABY_CHICKEN_AMBIENT, SoundEvents.BABY_CHICKEN_HURT, SoundEvents.BABY_CHICKEN_DEATH,
                SoundEvents.BABY_CHICKEN_STEP);

        private final Sound.Type ambient;
        private final Sound.Type hurt;
        private final Sound.Type death;
        private final Sound.Type step;

        Voice(final Sound.Type ambient, final Sound.Type hurt, final Sound.Type death, final Sound.Type step) {
            this.ambient = ambient;
            this.hurt = hurt;
            this.death = death;
            this.step = step;
        }
    }
}
