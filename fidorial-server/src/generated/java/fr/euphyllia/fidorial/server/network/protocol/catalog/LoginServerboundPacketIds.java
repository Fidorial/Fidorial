package fr.euphyllia.fidorial.server.network.protocol.catalog;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for the {@code login/serverbound} state/direction.
 *
 * <p>Generated from Mojang's packets report; do not edit.</p>
 */
public interface LoginServerboundPacketIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:cookie_response}
     */
    int COOKIE_RESPONSE_ID = 4;

    /**
     * {@code minecraft:custom_query_answer}
     */
    int CUSTOM_QUERY_ANSWER_ID = 2;

    /**
     * {@code minecraft:hello}
     */
    int HELLO_ID = 0;

    /**
     * {@code minecraft:key}
     */
    int KEY_ID = 1;

    /**
     * {@code minecraft:login_acknowledged}
     */
    int LOGIN_ACKNOWLEDGED_ID = 3;

    /**
     * {@code minecraft:options_applied}
     */
    int OPTIONS_APPLIED_ID = 5;

    /**
     * Identifier to protocol ID lookup table.
     */
    Object2IntMap<Key> BY_IDENTIFIER = new Object2IntOpenHashMap<>(
        new Key[] {
            Key.key("cookie_response"),
            Key.key("custom_query_answer"),
            Key.key("hello"),
            Key.key("key"),
            Key.key("login_acknowledged"),
            Key.key("options_applied")
        },
        new int[] {
            COOKIE_RESPONSE_ID,
            CUSTOM_QUERY_ANSWER_ID,
            HELLO_ID,
            KEY_ID,
            LOGIN_ACKNOWLEDGED_ID,
            OPTIONS_APPLIED_ID
        }
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
