package fr.euphyllia.fidorial.server.registry;

import fr.euphyllia.fidorial.server.codecs.adventure.ChatTypeCodecs;
import fr.euphyllia.fidorial.server.codecs.dialog.DialogCodecs;
import fr.euphyllia.fidorial.server.codecs.world.BiomeCodecs;
import fr.euphyllia.fidorial.server.codecs.world.DimensionTypeCodecs;
import fr.euphyllia.fidorial.server.entity.EntityTypes;
import fr.euphyllia.fidorial.server.item.enchantment.EnchantmentBinding;
import fr.euphyllia.fidorial.server.registry.data.RegistryValues;
import fr.fidorial.dialog.DialogDefinition;
import fr.fidorial.entity.EntityType;
import fr.fidorial.item.data.DataComponentType;
import fr.fidorial.item.data.DataComponentTypes;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.world.block.BlockType;
import fr.fidorial.world.block.Blocks;
import net.kyori.adventure.key.Key;

import java.util.Map;
import java.util.function.Function;

/**
 * The registries whose values are API types: the data registries plugins can add to, and the
 * entity types. Every other registry gets the generated {@link RegistryValues}.
 */
final class RegistryBindings {

    private static final Map<Key, RegistryBinding<?>> BOUND = Map.of(
            RegistryKey.BIOME.key(), RegistryBinding.Data.of(RegistryCodec.of(BiomeCodecs::codec)),
            RegistryKey.DIMENSION_TYPE.key(), RegistryBinding.Data.of(RegistryCodec.of(DimensionTypeCodecs::codec)),
            RegistryKey.CHAT_TYPE.key(), RegistryBinding.Data.of(RegistryCodec.of(ChatTypeCodecs::codec)),
            RegistryKey.DIALOG.key(), RegistryBinding.Data.of(RegistryCodec.<DialogDefinition>of(_ -> DialogCodecs.DEFINITION_CODEC)),
            RegistryKey.ENCHANTMENT.key(), EnchantmentBinding.create(),
            RegistryKey.ENTITY_TYPE.key(), new RegistryBinding.Fixed<EntityType>(EntityTypes::get),
            RegistryKey.BLOCK.key(), new RegistryBinding.Fixed<BlockType>(Blocks::type),
            RegistryKey.DATA_COMPONENT_TYPE.key(), new RegistryBinding.Fixed<DataComponentType<?>>(DataComponentTypes::byKey));

    private RegistryBindings() {
        throw new UnsupportedOperationException("RegistryBindings cannot be instantiated.");
    }

    /**
     * @throws IllegalStateException if the registry has neither a binding nor generated values
     */
    @SuppressWarnings("unchecked")
    static <T> RegistryBinding<T> binding(final RegistryKey<T> registry) {
        final RegistryBinding<?> bound = BOUND.get(registry.key());
        if (bound != null) {
            return (RegistryBinding<T>) bound;
        }
        final Function<Key, T> factory = RegistryValues.factory(registry);
        if (factory == null) {
            throw new IllegalStateException("The registry " + registry.key().asString() + " has no values: add it to "
                    + "SupportedRegistries and regenerate, or bind it in RegistryBindings");
        }
        return new RegistryBinding.Fixed<T>(key -> factory.apply(key.key()));
    }
}
