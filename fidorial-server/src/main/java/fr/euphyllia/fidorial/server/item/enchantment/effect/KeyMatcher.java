package fr.euphyllia.fidorial.server.item.enchantment.effect;

import com.google.gson.JsonElement;
import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.item.enchantment.KeySetImpl;
import fr.euphyllia.fidorial.server.registry.ServerRegistry;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@link KeySetImpl} of a registry, matched against the tags sent to clients.
 *
 * @param registry the registry, such as {@code minecraft:entity_type}
 * @param entries  the tag or the listed ids
 */
public record KeyMatcher(Key registry, KeySetImpl entries) {

    public static KeyMatcher parse(final Key registry, final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new IllegalArgumentException("Missing entries of " + registry.asString());
        }
        return new KeyMatcher(registry, KeySetImpl.parse(element, "Entries of " + registry.asString()));
    }

    public boolean test(final Key value) {
        final Optional<Key> tag = entries.tag();
        if (tag.isPresent()) {
            final ServerRegistry<?> tagged = FidorialServer.getInstance().registries().registry(registry);
            return tagged != null && tagged.isTagged(value, tag.get());
        }
        return entries.keys().contains(value);
    }

    /**
     * {@return every entry, the members of the tag for a tag}
     */
    public List<Key> resolve() {
        final Optional<Key> tag = entries.tag();
        if (tag.isPresent()) {
            final ServerRegistry<?> tagged = FidorialServer.getInstance().registries().registry(registry);
            return tagged == null ? new ArrayList<>() : new ArrayList<>(tagged.tag(tag.get()));
        }
        return new ArrayList<>(entries.keys());
    }
}
