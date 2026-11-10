package fr.euphyllia.fidorial.server.registry.data;

import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for entries in the {@code minecraft:command_suggestion_type} registry.
 *
 * <p>Generated from Mojang's registry report; do not edit.</p>
 */
public interface SuggestionTypeIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:ask_server}
     */
    int ASK_SERVER_SUGGESTION_ID = 0;

    /**
     * {@code minecraft:available_sounds}
     */
    int AVAILABLE_SOUNDS_SUGGESTION_ID = 1;

    /**
     * {@code minecraft:literal}
     */
    int LITERAL_SUGGESTION_ID = 4;

    /**
     * {@code minecraft:post_effects}
     */
    int POST_EFFECTS_SUGGESTION_ID = 3;

    /**
     * {@code minecraft:summonable_entities}
     */
    int SUMMONABLE_ENTITIES_SUGGESTION_ID = 2;

    /**
     * Immutable identifier to protocol ID lookup table.
     */
    Map<Key, Integer> BY_IDENTIFIER = Map.ofEntries(
        Map.entry(Key.key("ask_server"), ASK_SERVER_SUGGESTION_ID),
        Map.entry(Key.key("available_sounds"), AVAILABLE_SOUNDS_SUGGESTION_ID),
        Map.entry(Key.key("literal"), LITERAL_SUGGESTION_ID),
        Map.entry(Key.key("post_effects"), POST_EFFECTS_SUGGESTION_ID),
        Map.entry(Key.key("summonable_entities"), SUMMONABLE_ENTITIES_SUGGESTION_ID)
    );

    /**
     * Resolves the protocol ID for a namespaced identifier.
     *
     * @param identifier namespaced identifier, e.g. {@code Key.key("minecraft", "chest")}
     * @return the protocol ID, or {@link #UNKNOWN} when the identifier is unknown
     */
    static int id(final Key identifier) {
        return BY_IDENTIFIER.getOrDefault(identifier, UNKNOWN);
    }
}
