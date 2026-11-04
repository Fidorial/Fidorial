package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.gson.JsonElement;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EffectJson;
import fr.fidorial.item.enchantment.KeySet;
import fr.fidorial.registry.RegistryKey;
import fr.fidorial.registry.TagKey;
import fr.fidorial.registry.TypedKey;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Server implementation of {@link KeySet}.
 *
 * @param tagKey  the tag, {@code null} for a list
 * @param entries the listed keys, in order, empty for a tag
 * @param <T>     the type of the entries
 */
public record KeySetImpl<T>(@Nullable TagKey<T> tagKey, Set<TypedKey<T>> entries) implements KeySet<T> {

    private static final KeySetImpl<?> EMPTY = new KeySetImpl<>(null, Set.of());

    public KeySetImpl {
        entries = Collections.unmodifiableSet(new LinkedHashSet<>(entries));
        if (tagKey != null && !entries.isEmpty()) {
            throw new IllegalArgumentException("A key set is either a tag or a list");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> KeySetImpl<T> empty() {
        return (KeySetImpl<T>) EMPTY;
    }

    public static <T> KeySetImpl<T> ofTag(final TagKey<T> tag) {
        return new KeySetImpl<>(tag, Set.of());
    }

    public static <T> KeySetImpl<T> of(final Collection<TypedKey<T>> keys) {
        return new KeySetImpl<>(null, new LinkedHashSet<>(keys));
    }

    @Override
    public Optional<TagKey<T>> tag() {
        return Optional.ofNullable(tagKey);
    }

    @Override
    public Set<TypedKey<T>> keys() {
        return entries;
    }

    /**
     * Reads {@code "#namespace:tag"}, {@code "namespace:id"} or a list of ids.
     */
    public static <T> KeySetImpl<T> parse(final RegistryKey<T> registry, final JsonElement element, final String what) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            final String value = element.getAsString();
            return value.startsWith("#")
                    ? ofTag(TagKey.withRegistry(registry, EffectJson.key(value.substring(1))))
                    : of(Set.of(TypedKey.withRegistry(registry, EffectJson.key(value))));
        }
        final Set<TypedKey<T>> keys = new LinkedHashSet<>();
        for (final JsonElement entry : EffectJson.list(element, what)) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString() || entry.getAsString().startsWith("#")) {
                throw new IllegalArgumentException(what + " lists ids only, got " + entry);
            }
            keys.add(TypedKey.withRegistry(registry, EffectJson.key(entry.getAsString())));
        }
        return of(keys);
    }
}
