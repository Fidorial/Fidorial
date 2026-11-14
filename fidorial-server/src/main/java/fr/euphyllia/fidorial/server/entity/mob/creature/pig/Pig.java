package fr.euphyllia.fidorial.server.entity.mob.creature.pig;

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
import fr.fidorial.registry.data.PigSoundVariant;
import fr.fidorial.registry.data.PigVariant;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.registry.keys.PigSoundVariantKeys;
import fr.fidorial.registry.keys.PigVariantKeys;
import fr.fidorial.sound.SoundEvents;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The pig, as in Vanilla: it strolls, looks at players, follows carrots, potatoes, beetroots and a
 * carrot on a stick, panics when hurt, breeds when fed, and leaves porkchops. Its colour comes from
 * the biome it spawned in; its voice is classic, mini or big.
 */
public final class Pig extends AbstractAnimal implements Category.Neutral {

    public static final float MAX_HEALTH = 10f;

    private static final double ADULT_HEIGHT = 0.9;
    private static final double ADULT_WIDTH = 0.9;
    private static final double BABY_HEIGHT = 0.45;
    private static final double BABY_WIDTH = 0.45;
    private static final double BABY_EYE_HEIGHT = 0.40625;

    /**
     * Vanilla's {@code movement_speed} attribute of the pig; the goals multiply it by their modifier.
     */
    private static final double MOVEMENT_SPEED = 0.25;
    private static final double PANIC_SPEED = MOVEMENT_SPEED * 1.25;
    private static final double BREED_SPEED = MOVEMENT_SPEED;
    private static final double TEMPT_SPEED = MOVEMENT_SPEED * 1.2;
    private static final double FOLLOW_PARENT_SPEED = MOVEMENT_SPEED * 1.1;
    private static final double STROLL_SPEED = MOVEMENT_SPEED;

    private static final double LOOK_AT_PLAYER_RANGE = 6.0;
    private static final float STEP_VOLUME = 0.15f;

    /**
     * Pig entity data, after the 18 entries every ageable mob has.
     */
    private static final int MD_BOOST_TIME = 18;
    private static final int MD_VARIANT = 19;
    private static final int MD_SOUND_VARIANT = 20;
    /**
     * The ids of the {@code pig_variant} and {@code pig_sound_variant} entity data serializers.
     */
    private static final int MD_TYPE_PIG_VARIANT = 28;
    private static final int MD_TYPE_PIG_SOUND_VARIANT = 29;

    private static final int MIN_PORKCHOPS = 1;
    private static final int MAX_PORKCHOPS = 3;

    private final PanicGoal panicGoal;

    private TypedKey<PigVariant> variant = PigVariantKeys.TEMPERATE;
    private TypedKey<PigSoundVariant> soundVariant = PigSoundVariantKeys.CLASSIC;

    /**
     * Spawns a pig as vanilla does out of nowhere, with a spawn egg or {@code /summon}: an adult, of
     * the variant of the biome, with a random voice.
     */

    public Pig(final int entityId, final Location location) {
        this(entityId, location, false);
        this.variant = FarmAnimalVariants.forBiome(serverWorld(), location,
                PigVariantKeys.TEMPERATE, PigVariantKeys.WARM, PigVariantKeys.COLD);
        this.soundVariant = FarmAnimalVariants.random(RegistryKey.PIG_SOUND_VARIANT, PigSoundVariantKeys.CLASSIC);
    }

    /**
     * Creates a temperate pig with the classic voice, as vanilla does before it sets up a pig: a
     * newborn keeps that voice.
     */
    public Pig(final int entityId, final Location location, final boolean baby) {
        super(entityId, UUID.randomUUID(), EntityTypes.PIG, location, MAX_HEALTH, baby);
        navigation.setPathPenalty(PathPenalty.LAND_ANIMAL);

        this.panicGoal = new PanicGoal(this, 1, PANIC_SPEED, DamageTypeTagKeys.PANIC_CAUSES);
        goals.add(new FloatGoal(this, 0));
        goals.add(panicGoal);
        goals.add(new BreedGoal(this, 3, BREED_SPEED));
        goals.add(new TemptGoal(this, 4, TEMPT_SPEED, stack -> stack.id().equals(ItemKeys.CARROT_ON_A_STICK)));
        goals.add(new TemptGoal(this, 4, TEMPT_SPEED, this::isFood));
        goals.add(new FollowParentGoal(this, 5, FOLLOW_PARENT_SPEED));
        goals.add(new RandomStrollGoal(this, 6, STROLL_SPEED));
        goals.add(new LookAtPlayerGoal(this, 7, LOOK_AT_PLAYER_RANGE));
        goals.add(new RandomLookAroundGoal(this, 8));
    }

    /**
     * {@return {@code true} if pigs may spawn on a block}: as any animal, on grass in the light
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkAnimalSpawnRules(world, x, y, z);
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
        return isBaby() ? BABY_EYE_HEIGHT : super.eyeHeight();
    }

    @Override
    public double movementSpeed() {
        return MOVEMENT_SPEED;
    }

    @Override
    protected double defaultFollowRange() {
        return 0.0;
    }

    public TypedKey<PigVariant> variant() {
        return variant;
    }

    public void setVariant(final TypedKey<PigVariant> variant) {
        this.variant = variant;
        sendToTrackers(variantMetadata());
    }

    public TypedKey<PigSoundVariant> soundVariant() {
        return soundVariant;
    }

    public void setSoundVariant(final TypedKey<PigSoundVariant> soundVariant) {
        this.soundVariant = soundVariant;
        sendToTrackers(variantMetadata());
    }

    private ClientboundSetEntityMetadataPacket variantMetadata() {
        // TODO: the boost time of a pig ridden with a carrot on a stick, once mobs can be ridden
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.varInt(MD_BOOST_TIME, 0),
                FarmAnimalVariants.metadata(MD_VARIANT, MD_TYPE_PIG_VARIANT, RegistryKey.PIG_VARIANT, variant),
                FarmAnimalVariants.metadata(MD_SOUND_VARIANT, MD_TYPE_PIG_SOUND_VARIANT,
                        RegistryKey.PIG_SOUND_VARIANT, soundVariant));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(variantMetadata());
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.PIG_FOOD);
    }

    @Override
    protected @Nullable Pig createOffspring(final AbstractAnimal partner) {
        final Pig baby = new Pig(server().entities().ids().allocate(), birthLocation(), true);
        if (partner instanceof final Pig other) {
            baby.variant = ThreadLocalRandom.current().nextBoolean() ? variant : other.variant;
        }
        return baby;
    }

    @Override
    public boolean isPanicking() {
        return panicGoal.isPanicking();
    }

    @Override
    protected void dropLoot() {
        // TODO: Looting adds up to one more per level, once loot knows what killed the mob
        final int porkchops = ThreadLocalRandom.current().nextInt(MIN_PORKCHOPS, MAX_PORKCHOPS + 1);
        dropLootItem(ItemStack.of(fireTicks() > 0 ? ItemKeys.COOKED_PORKCHOP : ItemKeys.PORKCHOP, porkchops));
    }

    @Override
    protected void playEatingSound() {
        playSound(voice().eat, soundVolume(), voicePitch());
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
     * {@return the sounds this pig makes}: those of its sound variant, or those every piglet shares
     */
    private Voice voice() {
        if (isBaby()) {
            return Voice.BABY;
        }
        if (soundVariant.equals(PigSoundVariantKeys.MINI)) {
            return Voice.MINI;
        }
        return soundVariant.equals(PigSoundVariantKeys.BIG) ? Voice.BIG : Voice.CLASSIC;
    }

    @Override
    public void saveAdditional(final CompoundBinaryTag.Builder tag) {
        super.saveAdditional(tag);
        tag.putString("variant", variant.key().asString());
        tag.putString("sound_variant", soundVariant.key().asString());
    }

    @Override
    public void loadAdditional(final CompoundBinaryTag tag) {
        super.loadAdditional(tag);
        this.variant = FarmAnimalVariants.saved(RegistryKey.PIG_VARIANT, tag.getString("variant"), variant);
        this.soundVariant = FarmAnimalVariants.saved(RegistryKey.PIG_SOUND_VARIANT, tag.getString("sound_variant"), soundVariant);
    }

    /**
     * The sounds of a pig, as vanilla's pig sound variants list them.
     */
    private enum Voice {
        CLASSIC(SoundEvents.PIG_AMBIENT, SoundEvents.PIG_HURT, SoundEvents.PIG_DEATH, SoundEvents.PIG_EAT, SoundEvents.PIG_STEP),
        MINI(SoundEvents.PIG_MINI_AMBIENT, SoundEvents.PIG_MINI_HURT, SoundEvents.PIG_MINI_DEATH, SoundEvents.PIG_MINI_EAT,
                SoundEvents.PIG_STEP),
        BIG(SoundEvents.PIG_BIG_AMBIENT, SoundEvents.PIG_BIG_HURT, SoundEvents.PIG_BIG_DEATH, SoundEvents.PIG_BIG_EAT,
                SoundEvents.PIG_STEP),
        BABY(SoundEvents.BABY_PIG_AMBIENT, SoundEvents.BABY_PIG_HURT, SoundEvents.BABY_PIG_DEATH, SoundEvents.BABY_PIG_EAT,
                SoundEvents.BABY_PIG_STEP);

        private final Sound.Type ambient;
        private final Sound.Type hurt;
        private final Sound.Type death;
        private final Sound.Type eat;
        private final Sound.Type step;

        Voice(final Sound.Type ambient, final Sound.Type hurt, final Sound.Type death, final Sound.Type eat,
              final Sound.Type step) {
            this.ambient = ambient;
            this.hurt = hurt;
            this.death = death;
            this.eat = eat;
            this.step = step;
        }
    }
}
