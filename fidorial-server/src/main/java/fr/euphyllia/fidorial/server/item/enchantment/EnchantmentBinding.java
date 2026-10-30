package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.gson.JsonElement;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.item.FidorialItemRegistry;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EnchantmentEffectSet;
import fr.euphyllia.fidorial.server.registry.RegistryBinding;
import fr.euphyllia.fidorial.server.registry.RegistryCodec;
import fr.euphyllia.fidorial.server.registry.ServerRegistries;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import fr.fidorial.item.enchantment.EnchantmentDefinition;
import fr.fidorial.item.enchantment.KeySet;
import fr.fidorial.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Reads, writes and checks the entries of the {@code minecraft:enchantment} registry.
 */
public final class EnchantmentBinding implements RegistryCodec<EnchantmentDefinition>,
        RegistryBinding.Validator<EnchantmentDefinition> {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(EnchantmentBinding.class);
    private static final Key ENCHANTED_BOOK = Key.key("enchanted_book");

    private EnchantmentBinding() {
    }

    public static RegistryBinding.Data<EnchantmentDefinition> create() {
        final EnchantmentBinding binding = new EnchantmentBinding();
        return new RegistryBinding.Data<>(binding, binding);
    }

    @Override
    public EnchantmentDefinition decode(final Key key, final JsonElement json) {
        if (!json.isJsonObject()) {
            throw new IllegalArgumentException("Enchantment " + key.asString() + " must be a JSON object");
        }
        return EnchantmentJson.read(key, json.getAsJsonObject(), null);
    }

    /**
     * Plugin items are replaced by the vanilla items clients render them as.
     */
    @Override
    public BinaryTag encode(final Key key, final EnchantmentDefinition value) {
        final FidorialServer server = FidorialServer.getInstance();
        final FidorialItemRegistry items = server.registries().items();
        final ServerRegistry<EnchantmentDefinition> enchantments = server.registries().registry(RegistryKey.ENCHANTMENT);
        try {
            return EnchantmentJson.toNbt(EnchantmentJson.write(value,
                    item -> items.isItem(item) ? items.networkType(item) : null,
                    enchantment -> enchantments.contains(enchantment) ? enchantment : null));
        } catch (final RuntimeException exception) {
            LOGGER.error("Could not encode the enchantment {} for clients, sending a bare one instead",
                    key.asString(), exception);
            return EnchantmentJson.toNbt(EnchantmentJson.write(new EnchantmentDefinitionImpl(key,
                            value.description(), KeySetImpl.of(List.of(ENCHANTED_BOOK)), Optional.empty(), KeySetImpl.EMPTY,
                            value.maxLevel(), value.weight(), value.anvilCost(), value.minCost(),
                            value.maxCost(), value.slots(), null, EnchantmentEffectSet.EMPTY, Optional.empty()),
                    item -> item, enchantment -> null));
        }
    }

    /**
     * The client only knows the item tags of the vanilla data: an unknown one would fail its registry.
     */
    @Override
    public void validate(final ServerRegistries registries, final Key key, final EnchantmentDefinition value) {
        if (!(value instanceof final EnchantmentDefinitionImpl definition)) {
            throw new IllegalArgumentException("Enchantment definitions must come from Enchantments.define(), got "
                    + value.getClass().getName());
        }
        final ServerRegistry<?> items = registries.registry(RegistryKey.ITEM);
        checkItemTag(items, key, definition.supportedItems(), "supported items");
        definition.primaryItems().ifPresent(primary -> checkItemTag(items, key, primary, "primary items"));
    }

    private static void checkItemTag(final ServerRegistry<?> items, final Key key, final KeySet set, final String what) {
        final Optional<Key> tag = set.tag();
        if (tag.isPresent() && !items.tagMap().containsKey(tag.get())) {
            throw new IllegalArgumentException("The " + what + " of " + key.asString() + " name the unknown item tag #"
                    + tag.get().asString());
        }
    }

    /**
     * An exclusive set naming a tag that does not exist yet creates it empty, so that clients find it.
     */
    @Override
    public Set<Key> referencedTags(final EnchantmentDefinition value) {
        return value.exclusiveSet().tag().map(Set::of).orElse(Set.of());
    }
}
