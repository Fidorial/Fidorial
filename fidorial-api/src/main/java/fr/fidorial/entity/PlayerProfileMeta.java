package fr.fidorial.entity;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The bare identity of a player, as cached by the server.
 *
 * @param id   the player identity
 * @param name the player name
 * @since 0.1.0
 */
public record PlayerProfileMeta(UUID id, String name) {

    /**
     * Extracts the identity of a profile.
     *
     * @param profile the profile
     * @since 0.1.0
     */
    public PlayerProfileMeta(final PlayerProfile profile) {
        this(profile.uuid(), profile.name());
    }

    /**
     * Reads an identity from a JSON object holding {@code uuid} and {@code name}.
     *
     * @param object the JSON object
     * @return the identity, or {@code null} if a field is missing or the uuid is malformed
     * @since 0.1.0
     */
    public static @Nullable PlayerProfileMeta fromJson(final JsonObject object) {
        if (!object.has("uuid") || !object.has("name")) {
            return null;
        }

        final UUID uuid;

        try {
            uuid = UUID.fromString(object.get("uuid").getAsString());
        } catch (final Throwable ignored) {
            return null;
        }

        return new PlayerProfileMeta(uuid, object.get("name").getAsString());
    }

    /**
     * Writes this identity as {@code uuid} and {@code name} fields.
     *
     * @param output the JSON object to write into
     * @since 0.1.0
     */
    public void appendTo(final JsonObject output) {
        output.addProperty("uuid", id.toString());
        output.addProperty("name", name);
    }

    /**
     * Derives the identity an offline-mode server gives to a name.
     *
     * @param name the player name
     * @return the identity, whose uuid is derived from {@code OfflinePlayer:<name>}
     * @since 0.1.0
     */
    public static PlayerProfileMeta createOffline(final String name) {
        return new PlayerProfileMeta(
                UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)), name);
    }

}
