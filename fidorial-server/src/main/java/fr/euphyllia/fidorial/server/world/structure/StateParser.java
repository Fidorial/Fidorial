package fr.euphyllia.fidorial.server.world.structure;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.euphyllia.fidorial.server.world.chunk.BlockState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StateParser {

    private StateParser() {
    }

    public record Parsed(Key name, Map<String, String> properties) {
    }

    public static @Nullable Parsed parseString(final String raw) {
        String text = raw.strip();
        final int nbtStart = text.indexOf('{');
        if (nbtStart >= 0) {
            text = text.substring(0, nbtStart);
        }
        final Map<String, String> properties = new LinkedHashMap<>();
        final int open = text.indexOf('[');
        String name = text;
        if (open >= 0) {
            name = text.substring(0, open);
            final int close = text.lastIndexOf(']');
            final String body = text.substring(open + 1, close > open ? close : text.length());
            for (final String pair : body.split(",")) {
                final int eq = pair.indexOf('=');
                if (eq > 0) {
                    properties.put(pair.substring(0, eq).strip(), pair.substring(eq + 1).strip());
                }
            }
        }
        final Key key = Keys.parse(name);
        return key == null ? null : new Parsed(key, properties);
    }

    public static @Nullable Parsed parseJson(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return parseString(element.getAsString());
        }
        if (!element.isJsonObject()) {
            return null;
        }
        final JsonObject object = element.getAsJsonObject();
        final Key key = Keys.parse(object.has("Name") ? object.get("Name").getAsString() : null);
        if (key == null) {
            return null;
        }
        final Map<String, String> properties = new LinkedHashMap<>();
        if (object.has("Properties") && object.get("Properties").isJsonObject()) {
            for (final Map.Entry<String, JsonElement> entry : object.getAsJsonObject("Properties").entrySet()) {
                properties.put(entry.getKey(), entry.getValue().getAsString());
            }
        }
        return new Parsed(key, properties);
    }

    public static @Nullable Parsed parseNbt(final CompoundBinaryTag compound) {
        final Key key = Keys.parse(compound.getString("Name"));
        if (key == null) {
            return null;
        }
        final Map<String, String> properties = new LinkedHashMap<>();
        final CompoundBinaryTag props = compound.getCompound("Properties");
        for (final String name : props.keySet()) {
            final BinaryTag value = props.get(name);
            if (value instanceof final StringBinaryTag string) {
                properties.put(name, string.value());
            } else if (value != null) {
                properties.put(name, value.toString());
            }
        }
        return new Parsed(key, properties);
    }

    public static @Nullable BlockState resolve(final @Nullable Parsed parsed, final BlockValidator validator) {
        return parsed == null ? null : validator.resolve(parsed.name(), parsed.properties());
    }
}
