package fr.euphyllia.fidorial.server.network.protocol.catalog;

import net.kyori.adventure.key.Key;

/**
 * Packet identifiers for the {@code login/clientbound} state/direction.
 *
 * <p>Generated from Mojang's packets report; do not edit.</p>
 */
public final class LoginClientboundPackets {
    /**
     * {@code minecraft:apply_options}
     */
    public static final Key APPLY_OPTIONS = Key.key("apply_options");

    /**
     * {@code minecraft:cookie_request}
     */
    public static final Key COOKIE_REQUEST = Key.key("cookie_request");

    /**
     * {@code minecraft:custom_query}
     */
    public static final Key CUSTOM_QUERY = Key.key("custom_query");

    /**
     * {@code minecraft:key}
     */
    public static final Key KEY = Key.key("key");

    /**
     * {@code minecraft:login_disconnect}
     */
    public static final Key LOGIN_DISCONNECT = Key.key("login_disconnect");

    /**
     * {@code minecraft:login_finished}
     */
    public static final Key LOGIN_FINISHED = Key.key("login_finished");

    /**
     * {@code minecraft:setup_compression}
     */
    public static final Key SETUP_COMPRESSION = Key.key("setup_compression");

    private LoginClientboundPackets() {
    }
}
