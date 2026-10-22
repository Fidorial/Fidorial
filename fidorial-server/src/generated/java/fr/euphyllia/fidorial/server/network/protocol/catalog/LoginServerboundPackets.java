package fr.euphyllia.fidorial.server.network.protocol.catalog;

import net.kyori.adventure.key.Key;

/**
 * Packet identifiers for the {@code login/serverbound} state/direction.
 *
 * <p>Generated from Mojang's packets report; do not edit.</p>
 */
public final class LoginServerboundPackets {
    /**
     * {@code minecraft:cookie_response}
     */
    public static final Key COOKIE_RESPONSE = Key.key("cookie_response");

    /**
     * {@code minecraft:custom_query_answer}
     */
    public static final Key CUSTOM_QUERY_ANSWER = Key.key("custom_query_answer");

    /**
     * {@code minecraft:hello}
     */
    public static final Key HELLO = Key.key("hello");

    /**
     * {@code minecraft:key}
     */
    public static final Key KEY = Key.key("key");

    /**
     * {@code minecraft:login_acknowledged}
     */
    public static final Key LOGIN_ACKNOWLEDGED = Key.key("login_acknowledged");

    /**
     * {@code minecraft:options_applied}
     */
    public static final Key OPTIONS_APPLIED = Key.key("options_applied");

    private LoginServerboundPackets() {
    }
}
