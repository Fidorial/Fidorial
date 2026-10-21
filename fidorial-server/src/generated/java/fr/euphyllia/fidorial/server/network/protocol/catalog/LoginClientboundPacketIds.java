package fr.euphyllia.fidorial.server.network.protocol.catalog;

import java.util.Map;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for the {@code login/clientbound} state/direction.
 *
 * <p>Generated from Mojang's packets report; do not edit.</p>
 */
public interface LoginClientboundPacketIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:apply_options}
     */
    int APPLY_OPTIONS_ID = 6;

    /**
     * {@code minecraft:cookie_request}
     */
    int COOKIE_REQUEST_ID = 5;

    /**
     * {@code minecraft:custom_query}
     */
    int CUSTOM_QUERY_ID = 4;

    /**
     * {@code minecraft:key}
     */
    int KEY_ID = 1;

    /**
     * {@code minecraft:login_disconnect}
     */
    int LOGIN_DISCONNECT_ID = 0;

    /**
     * {@code minecraft:login_finished}
     */
    int LOGIN_FINISHED_ID = 2;

    /**
     * {@code minecraft:setup_compression}
     */
    int SETUP_COMPRESSION_ID = 3;

    /**
     * Immutable identifier to protocol ID lookup table.
     */
    Map<Key, Integer> BY_IDENTIFIER = Map.ofEntries(
        Map.entry(Key.key("apply_options"), APPLY_OPTIONS_ID),
        Map.entry(Key.key("cookie_request"), COOKIE_REQUEST_ID),
        Map.entry(Key.key("custom_query"), CUSTOM_QUERY_ID),
        Map.entry(Key.key("key"), KEY_ID),
        Map.entry(Key.key("login_disconnect"), LOGIN_DISCONNECT_ID),
        Map.entry(Key.key("login_finished"), LOGIN_FINISHED_ID),
        Map.entry(Key.key("setup_compression"), SETUP_COMPRESSION_ID)
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
