package fr.euphyllia.fidorial.server.entity.mob.creature;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
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
import fr.euphyllia.fidorial.server.inventory.PlayerInventories;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSoundPacket;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.entity.GameMode;
import fr.fidorial.inventory.EquipmentSlotGroup;
import fr.fidorial.inventory.PlayerInventory;
import fr.fidorial.item.ItemStack;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CowSoundVariant;
import fr.fidorial.registry.data.CowVariant;
import fr.fidorial.registry.keys.BiomeTagKeys;
import fr.fidorial.registry.keys.CowSoundVariantKeys;
import fr.fidorial.registry.keys.CowVariantKeys;
import fr.fidorial.registry.keys.DamageTypeTagKeys;
import fr.fidorial.registry.keys.ItemKeys;
import fr.fidorial.registry.keys.ItemTagKeys;
import fr.fidorial.sound.SoundEvents;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The cow, as in Vanilla: it strolls, looks at players, follows wheat, panics when hurt, breeds
 * when fed wheat and gives milk into a bucket.
 */
public final class Cow extends AbstractAnimal implements Category.Neutral {

    public static final float MAX_HEALTH = 10f;

    private static final double ADULT_HEIGHT = 1.4;
    private static final double ADULT_WIDTH = 0.9;
    private static final double ADULT_EYE_HEIGHT = 1.3;
    private static final double BABY_HEIGHT = 0.7;
    private static final double BABY_WIDTH = 0.45;
    private static final double BABY_EYE_HEIGHT = 0.69;

    /**
     * Vanilla's {@code movement_speed} attribute of the cow; the goals multiply it by their modifier.
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
    private static final int MD_VARIANT = 18;
    private static final int MD_SOUND_VARIANT = 19;
    /**
     * The ids of the {@code cow_variant} and {@code cow_sound_variant} entity data serializers.
     */
    private static final int MD_TYPE_COW_VARIANT = 23;
    private static final int MD_TYPE_COW_SOUND_VARIANT = 24;

    private static final int MAX_LEATHER = 2;
    private static final int MIN_BEEF = 1;
    private static final int MAX_BEEF = 3;

    private final PanicGoal panicGoal;

    private TypedKey<CowVariant> variant = CowVariantKeys.TEMPERATE;
    private TypedKey<CowSoundVariant> soundVariant = CowSoundVariantKeys.CLASSIC;

    /**
     * Spawns a cow as vanilla does out of nowhere, with a spawn egg or {@code /summon}: an adult, of
     * the variant of the biome, with a random voice.
     */
    public Cow(final int entityId, final Location location) {
        this(entityId, location, false);
        this.variant = variantForBiome(BlockView.biomeAt(serverWorld(),
                (int) Math.floor(location.x()), (int) Math.floor(location.y()), (int) Math.floor(location.z())));
        this.soundVariant = randomSoundVariant();
    }

    /**
     * Creates a temperate cow with the classic voice, as vanilla does before it sets up a cow: a
     * newborn keeps that voice.
     */
    public Cow(final int entityId, final Location location, final boolean baby) {
        super(entityId, UUID.randomUUID(), EntityTypes.COW, location, MAX_HEALTH, baby);

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

    /**
     * {@return {@code true} if cows may spawn on a block}: as any animal, on grass in the light
     */
    public static boolean canSpawn(final ServerWorld world, final int x, final int y, final int z) {
        return checkAnimalSpawnRules(world, x, y, z);
    }

    /**
     * {@return the variant of the cows that spawn in a biome}: as in Vanilla, from the
     * {@code spawns_warm_variant_farm_animals} and {@code spawns_cold_variant_farm_animals} biome tags
     */
    public TypedKey<CowVariant> variantForBiome(final @Nullable TypedKey<BiomeDefinition> biome) {
        if (biome == null) {
            return CowVariantKeys.TEMPERATE;
        }
        final ServerRegistry<BiomeDefinition> biomes = server().registries().registry(RegistryKey.BIOME);
        if (biomes.isTagged(biome, BiomeTagKeys.SPAWNS_WARM_VARIANT_FARM_ANIMALS)) {
            return CowVariantKeys.WARM;
        }
        if (biomes.isTagged(biome, BiomeTagKeys.SPAWNS_COLD_VARIANT_FARM_ANIMALS)) {
            return CowVariantKeys.COLD;
        }
        return CowVariantKeys.TEMPERATE;
    }

    private TypedKey<CowSoundVariant> randomSoundVariant() {
        final List<TypedKey<CowSoundVariant>> voices =
                new ArrayList<>(server().registries().registry(RegistryKey.COW_SOUND_VARIANT).keys());
        return voices.isEmpty()
                ? CowSoundVariantKeys.CLASSIC
                : voices.get(ThreadLocalRandom.current().nextInt(voices.size()));
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

    public TypedKey<CowVariant> variant() {
        return variant;
    }

    public void setVariant(final TypedKey<CowVariant> variant) {
        this.variant = variant;
        sendToTrackers(variantMetadata());
    }

    public TypedKey<CowSoundVariant> soundVariant() {
        return soundVariant;
    }

    public void setSoundVariant(final TypedKey<CowSoundVariant> soundVariant) {
        this.soundVariant = soundVariant;
        sendToTrackers(variantMetadata());
    }

    public int variantNetworkId() {
        return server().registries().registry(RegistryKey.COW_VARIANT).networkId(variant);
    }

    private ClientboundSetEntityMetadataPacket variantMetadata() {
        final int variantId = variantNetworkId();
        final int soundVariantId = server().registries().registry(RegistryKey.COW_SOUND_VARIANT).networkId(soundVariant);
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                ClientboundSetEntityMetadataPacket.Entry.raw(MD_VARIANT, MD_TYPE_COW_VARIANT,
                        buffer -> buffer.writeVarInt(variantId)),
                ClientboundSetEntityMetadataPacket.Entry.raw(MD_SOUND_VARIANT, MD_TYPE_COW_SOUND_VARIANT,
                        buffer -> buffer.writeVarInt(soundVariantId)));
    }

    @Override
    public void sendSpawnPackets(final ClientConnection connection) {
        super.sendSpawnPackets(connection);
        connection.send(variantMetadata());
    }

    @Override
    public boolean isFood(final ItemStack stack) {
        return server().registries().registry(RegistryKey.ITEM).isTagged(stack.id(), ItemTagKeys.COW_FOOD);
    }

    @Override
    protected @Nullable Cow createOffspring(final AbstractAnimal partner) {
        final Cow baby = new Cow(server().entities().ids().allocate(), birthLocation(), true);
        if (partner instanceof final Cow other) {
            baby.variant = ThreadLocalRandom.current().nextBoolean() ? variant : other.variant;
        }
        return baby;
    }

    @Override
    protected boolean interact(final ServerPlayer player, final EquipmentSlotGroup hand) {
        final int slot = handSlot(player, hand);
        final ItemStack held = player.inventory().get(slot);
        if (!held.isEmpty() && held.id().equals(ItemKeys.BUCKET) && !isBaby()) {
            milk(player, slot);
            return true;
        }
        return super.interact(player, hand);
    }

    /**
     * Fills a bucket with milk, as vanilla fills an item: the last bucket of the stack becomes the
     * milk bucket, otherwise the milk bucket goes to the inventory, or to the ground if it is full.
     * In creative mode the bucket is kept, and the milk bucket is only given if the player has none.
     */
    private void milk(final ServerPlayer player, final int slot) {
        final Location at = player.location();
        // the player hears the sound from their own client, which predicts the click
        player.sendToTrackers(new ClientboundSoundPacket(
                Sound.sound(SoundEvents.COW_MILK, Sound.Source.PLAYER, 1.0f, 1.0f), at.x(), at.y(), at.z()));

        final PlayerInventory inventory = player.inventory();
        final ItemStack milk = ItemStack.of(ItemKeys.MILK_BUCKET, 1);
        if (player.gameMode() == GameMode.CREATIVE) {
            if (!holds(inventory, milk)) {
                PlayerInventories.add(inventory, player.selectedSlot(), milk);
            }
            return;
        }
        final ItemStack held = inventory.get(slot);
        if (held.count() <= 1) {
            inventory.set(slot, milk);
            return;
        }
        inventory.set(slot, held.withCount(held.count() - 1));
        final ItemStack left = PlayerInventories.add(inventory, player.selectedSlot(), milk);
        if (!left.isEmpty()) {
            server().entities().dropFromPlayer(player, left);
        }
    }

    private static boolean holds(final PlayerInventory inventory, final ItemStack stack) {
        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            final ItemStack other = inventory.get(slot);
            if (!other.isEmpty() && other.id().equals(stack.id())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isPanicking() {
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

    @Override
    protected void onStep() {
        playSound(isMoody() ? SoundEvents.COW_MOODY_STEP : SoundEvents.COW_STEP, STEP_VOLUME, 1.0f);
    }

    @Override
    protected float soundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    protected Sound.Type ambientSound() {
        return isMoody() ? SoundEvents.COW_MOODY_AMBIENT : SoundEvents.COW_AMBIENT;
    }

    @Override
    protected Sound.Type hurtSound() {
        return isMoody() ? SoundEvents.COW_MOODY_HURT : SoundEvents.COW_HURT;
    }

    @Override
    protected Sound.Type deathSound() {
        return isMoody() ? SoundEvents.COW_MOODY_DEATH : SoundEvents.COW_DEATH;
    }

    private boolean isMoody() {
        return soundVariant.equals(CowSoundVariantKeys.MOODY);
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
        final TypedKey<CowVariant> savedVariant = savedKey(RegistryKey.COW_VARIANT, tag.getString("variant"));
        if (savedVariant != null) {
            this.variant = savedVariant;
        }
        final TypedKey<CowSoundVariant> savedVoice = savedKey(RegistryKey.COW_SOUND_VARIANT, tag.getString("sound_variant"));
        if (savedVoice != null) {
            this.soundVariant = savedVoice;
        }
    }

    /**
     * {@return the entry of a registry saved under its name, {@code null} if it is unknown or absent}
     */
    @SuppressWarnings("PatternValidation")
    private <T> @Nullable TypedKey<T> savedKey(final RegistryKey<T> registry, final String name) {
        if (name.isEmpty() || !Key.parseable(name)) {
            return null;
        }
        final TypedKey<T> key = TypedKey.withRegistry(registry, Key.key(name));
        return server().registries().registry(registry).contains(key) ? key : null;
    }
}
