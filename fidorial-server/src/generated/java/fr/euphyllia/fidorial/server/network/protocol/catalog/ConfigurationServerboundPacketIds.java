package fr.euphyllia.fidorial.server.network.protocol.catalog;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.key.Key;

/**
 * Network IDs for the {@code configuration/serverbound} state/direction.
 *
 * <p>Generated from Mojang's packets report; do not edit.</p>
 */
public interface ConfigurationServerboundPacketIds {
    /**
     * Returned by {@link #id(Key)} when the identifier is unknown.
     */
    int UNKNOWN = -1;

    /**
     * {@code minecraft:accept_code_of_conduct}
     */
    int ACCEPT_CODE_OF_CONDUCT_ID = 9;

    /**
     * {@code minecraft:client_information}
     */
    int CLIENT_INFORMATION_ID = 0;

    /**
     * {@code minecraft:cookie_response}
     */
    int COOKIE_RESPONSE_ID = 1;

    /**
     * {@code minecraft:custom_click_action}
     */
    int CUSTOM_CLICK_ACTION_ID = 8;

    /**
     * {@code minecraft:custom_payload}
     */
    int CUSTOM_PAYLOAD_ID = 2;

    /**
     * {@code minecraft:finish_configuration}
     */
    int FINISH_CONFIGURATION_ID = 3;

    /**
     * {@code minecraft:keep_alive}
     */
    int KEEP_ALIVE_ID = 4;

    /**
     * {@code minecraft:pong}
     */
    int PONG_ID = 5;

    /**
     * {@code minecraft:resource_pack}
     */
    int RESOURCE_PACK_ID = 6;

    /**
     * {@code minecraft:select_known_packs}
     */
    int SELECT_KNOWN_PACKS_ID = 7;

    /**
     * Identifier to protocol ID lookup table.
     */
    Object2IntMap<Key> BY_IDENTIFIER = new Object2IntOpenHashMap<>(
        new Key[] {
            Key.key("accept_code_of_conduct"),
            Key.key("client_information"),
            Key.key("cookie_response"),
            Key.key("custom_click_action"),
            Key.key("custom_payload"),
            Key.key("finish_configuration"),
            Key.key("keep_alive"),
            Key.key("pong"),
            Key.key("resource_pack"),
            Key.key("select_known_packs")
        },
        new int[] {
            ACCEPT_CODE_OF_CONDUCT_ID,
            CLIENT_INFORMATION_ID,
            COOKIE_RESPONSE_ID,
            CUSTOM_CLICK_ACTION_ID,
            CUSTOM_PAYLOAD_ID,
            FINISH_CONFIGURATION_ID,
            KEEP_ALIVE_ID,
            PONG_ID,
            RESOURCE_PACK_ID,
            SELECT_KNOWN_PACKS_ID
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
