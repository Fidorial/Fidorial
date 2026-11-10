package fr.euphyllia.fidorial.server.network.protocol.catalog;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    Object2IntMap<Key> BY_IDENTIFIER = Object2IntMaps.unmodifiable(new Object2IntOpenHashMap<>(
        new Key[] {
            Key.key("apply_options"),
            Key.key("cookie_request"),
            Key.key("custom_query"),
            Key.key("key"),
            Key.key("login_disconnect"),
            Key.key("login_finished"),
            Key.key("setup_compression")
        },
        new int[] {
            APPLY_OPTIONS_ID,
            COOKIE_REQUEST_ID,
            CUSTOM_QUERY_ID,
            KEY_ID,
            LOGIN_DISCONNECT_ID,
            LOGIN_FINISHED_ID,
            SETUP_COMPRESSION_ID
        }
    ));

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
