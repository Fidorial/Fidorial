package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.item.enchantment.KeySetImpl;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@link KeySetImpl} of a registry, matched against the tags sent to clients.
 *
 * @param registry the registry, such as {@code minecraft:entity_type}
 * @param entries  the tag or the listed ids
 * @param <T>      the type of the entries
 */
public record KeyMatcher<T>(RegistryKey<T> registry, KeySetImpl<T> entries) {

    public static <T> KeyMatcher<T> parse(final RegistryKey<T> registry, final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("Missing entries of " + registry.key().asString());
        }
        return new KeyMatcher<>(registry, KeySetImpl.parse(registry, element, "Entries of " + registry.key().asString()));
    }

    public boolean test(final TypedKey<T> value) {
        final Optional<TagKey<T>> tag = entries.tag();
        if (tag.isPresent()) {
            return FidorialServer.getInstance().registries().registry(registry).isTagged(value, tag.get());
        }
        return entries.keys().contains(value);
    }

    /**
     * {@return every entry, the members of the tag for a tag}
     */
    public List<TypedKey<T>> resolve() {
        final Optional<TagKey<T>> tag = entries.tag();
        if (tag.isPresent()) {
            return new ArrayList<>(FidorialServer.getInstance().registries().registry(registry).tag(tag.get()));
        }
        return new ArrayList<>(entries.keys());
    }
}
