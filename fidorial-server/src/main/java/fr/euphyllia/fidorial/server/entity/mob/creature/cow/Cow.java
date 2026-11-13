package fr.euphyllia.fidorial.server.entity.mob.creature.cow;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAnimal;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CowSoundVariant;
import fr.fidorial.registry.data.CowVariant;
import fr.fidorial.registry.keys.BiomeTagKeys;
import fr.fidorial.registry.keys.CowSoundVariantKeys;
import fr.fidorial.registry.keys.CowVariantKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The cow : what every {@linkplain AbstractCow cow} does, in the colour of the biome
 * it spawned in, with a classic or a moody voice.
 */
public final class Cow extends AbstractCow implements Category.Neutral {


    private static final int MD_VARIANT = 18;
    private static final int MD_SOUND_VARIANT = 19;
    /**
     * The ids of the {@code cow_variant} and {@code cow_sound_variant} entity data serializers.
     */
    private static final int MD_TYPE_COW_VARIANT = 23;
    private static final int MD_TYPE_COW_SOUND_VARIANT = 24;

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
        super(entityId, EntityTypes.COW, location, baby);
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
    protected @Nullable Cow createOffspring(final AbstractAnimal partner) {
        final Cow baby = new Cow(server().entities().ids().allocate(), birthLocation(), true);
        if (partner instanceof final Cow other) {
            baby.variant = ThreadLocalRandom.current().nextBoolean() ? variant : other.variant;
        }
        return baby;
    }

    protected boolean isMoody() {
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
