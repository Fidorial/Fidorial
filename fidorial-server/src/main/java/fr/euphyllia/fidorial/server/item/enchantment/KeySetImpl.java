package fr.euphyllia.fidorial.server.item.enchantment;

import com.google.gson.JsonElement;
import fr.euphyllia.fidorial.server.item.enchantment.effect.EffectJson;
import fr.fidorial.item.enchantment.KeySet;
import net.kyori.adventure.key.Key;
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
 */
public record KeySetImpl(@Nullable Key tagKey, Set<Key> entries) implements KeySet {

    public static final KeySetImpl EMPTY = new KeySetImpl(null, Set.of());

    public KeySetImpl {
        entries = Collections.unmodifiableSet(new LinkedHashSet<>(entries));
        if (tagKey != null && !entries.isEmpty()) {
            throw new IllegalArgumentException("A key set is either a tag or a list");
        }
    }

    public static KeySetImpl ofTag(final Key tag) {
        return new KeySetImpl(tag, Set.of());
    }

    public static KeySetImpl of(final Collection<Key> keys) {
        return new KeySetImpl(null, new LinkedHashSet<>(keys));
    }

    @Override
    public Optional<Key> tag() {
        return Optional.ofNullable(tagKey);
    }

    @Override
    public Set<Key> keys() {
        return entries;
    }

    /**
     * Reads {@code "#namespace:tag"}, {@code "namespace:id"} or a list of ids.
     */
    public static KeySetImpl parse(final JsonElement element, final String what) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            final String value = element.getAsString();
            return value.startsWith("#")
                    ? ofTag(EffectJson.key(value.substring(1)))
                    : of(Set.of(EffectJson.key(value)));
        }
        final Set<Key> keys = new LinkedHashSet<>();
        for (final JsonElement entry : EffectJson.list(element, what)) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString() || entry.getAsString().startsWith("#")) {
                throw new IllegalArgumentException(what + " lists ids only, got " + entry);
            }
            keys.add(EffectJson.key(entry.getAsString()));
        }
        return of(keys);
    }
}
