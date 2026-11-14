package fr.euphyllia.fidorial.server.entity.mob.creature;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.entity.ai.BlockView;
import fr.euphyllia.fidorial.server.network.protocol.packet.clientbound.play.ClientboundSetEntityMetadataPacket;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.euphyllia.fidorial.server.world.ServerWorld;
import fr.fidorial.math.Location;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TypedKey;
import fr.fidorial.registry.keys.BiomeTagKeys;
import fr.fidorial.world.biome.BiomeDefinition;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * What the cow, the pig and the chicken share about their variants: the colour comes from the biome
 * they spawn in, the voice is random, and both are sent to the client and saved the same way.
 */
public final class FarmAnimalVariants {

    private FarmAnimalVariants() {
    }

    /**
     * {@return the variant of the farm animals that spawn somewhere}: the warm one in
     * the biomes of the {@code spawns_warm_variant_farm_animals} tag, the cold one in those of
     * {@code spawns_cold_variant_farm_animals}, the temperate one elsewhere
     */
    public static <T> TypedKey<T> forBiome(final ServerWorld world, final Location location,
                                    final TypedKey<T> temperate, final TypedKey<T> warm, final TypedKey<T> cold) {
        final TypedKey<BiomeDefinition> biome = BlockView.biomeAt(world,
                (int) Math.floor(location.x()), (int) Math.floor(location.y()), (int) Math.floor(location.z()));
        if (biome == null) {
            return temperate;
        }
        final ServerRegistry<BiomeDefinition> biomes = FidorialServer.getInstance().registries().registry(RegistryKey.BIOME);
        if (biomes.isTagged(biome, BiomeTagKeys.SPAWNS_WARM_VARIANT_FARM_ANIMALS)) {
            return warm;
        }
        if (biomes.isTagged(biome, BiomeTagKeys.SPAWNS_COLD_VARIANT_FARM_ANIMALS)) {
            return cold;
        }
        return temperate;
    }

    /**
     * {@return a random entry of a registry, picks the voice of a farm animal}
     *
     * @param fallback what to return if the registry is empty
     */
    public static <T> TypedKey<T> random(final RegistryKey<T> registry, final TypedKey<T> fallback) {
        final List<TypedKey<T>> keys = new ArrayList<>(FidorialServer.getInstance().registries().registry(registry).keys());
        return keys.isEmpty() ? fallback : keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
    }

    /**
     * {@return the entity data entry of a variant}: the id of its registry entry, under the id of
     * the serializer of that registry
     */
    public static <T> ClientboundSetEntityMetadataPacket.Entry metadata(final int index, final int serializerId,
                                                                 final RegistryKey<T> registry, final TypedKey<T> variant) {
        final int id = FidorialServer.getInstance().registries().registry(registry).networkId(variant);
        return ClientboundSetEntityMetadataPacket.Entry.raw(index, serializerId, buffer -> buffer.writeVarInt(id));
    }

    /**
     * {@return the entry of a registry saved under its name, {@code fallback} if it is unknown or
     * absent}
     */
    @SuppressWarnings("PatternValidation")
    public static <T> TypedKey<T> saved(final RegistryKey<T> registry, final String name, final TypedKey<T> fallback) {
        final TypedKey<T> key = parse(registry, name);
        return key != null && FidorialServer.getInstance().registries().registry(registry).contains(key) ? key : fallback;
    }

    @SuppressWarnings("PatternValidation")
    private static <T> @Nullable TypedKey<T> parse(final RegistryKey<T> registry, final String name) {
        return name.isEmpty() || !Key.parseable(name) ? null : TypedKey.withRegistry(registry, Key.key(name));
    }
}
