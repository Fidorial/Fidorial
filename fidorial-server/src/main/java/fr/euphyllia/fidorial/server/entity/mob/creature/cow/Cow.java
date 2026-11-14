package fr.euphyllia.fidorial.server.entity.mob.creature.cow;

import fr.euphyllia.fidorial.server.entity.Category;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.entity.mob.AbstractAnimal;
import fr.euphyllia.fidorial.server.entity.mob.creature.FarmAnimalVariants;
import fr.euphyllia.fidorial.server.network.connection.ClientConnection;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.data.CowSoundVariant;
import fr.fidorial.registry.data.CowVariant;
import fr.fidorial.registry.keys.CowSoundVariantKeys;
import fr.fidorial.registry.keys.CowVariantKeys;
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
        this.variant = FarmAnimalVariants.forBiome(serverWorld(), location,
                CowVariantKeys.TEMPERATE, CowVariantKeys.WARM, CowVariantKeys.COLD);
        this.soundVariant = FarmAnimalVariants.random(RegistryKey.COW_SOUND_VARIANT, CowSoundVariantKeys.CLASSIC);
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
        return ClientboundSetEntityMetadataPacket.of(entityId(),
                FarmAnimalVariants.metadata(MD_VARIANT, MD_TYPE_COW_VARIANT, RegistryKey.COW_VARIANT, variant),
                FarmAnimalVariants.metadata(MD_SOUND_VARIANT, MD_TYPE_COW_SOUND_VARIANT,
                        RegistryKey.COW_SOUND_VARIANT, soundVariant));
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
        this.variant = FarmAnimalVariants.saved(RegistryKey.COW_VARIANT, tag.getString("variant"), variant);
        this.soundVariant = FarmAnimalVariants.saved(RegistryKey.COW_SOUND_VARIANT, tag.getString("sound_variant"), soundVariant);
    }
}
